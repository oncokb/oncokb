package org.mskcc.cbio.oncokb.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.TumorForm;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMatching.*;

public class ClinicalTrialsUtils {


    public static List<Trial> getTrials(String treatment, String cancerType)
        throws IOException, ParseException {

        JSONObject trialsMapping = CacheUtils.getTrialsMapping();
        JSONObject oncotreeMapping = CacheUtils.getOncoTreeMappingTrials();

        SpecialTumorType specialTumorType = SpecialTumorType.getByTumorType(cancerType);
        if(specialTumorType != null) {
            List<Trial> trials = getTrialsForSpecialCancerType(oncotreeMapping, trialsMapping, specialTumorType);
            if (treatment != null) {
                trials = getTrialByTreatment(trials,treatment);
            }
            return new ArrayList<Trial>(trials); 
        } else {
            List<TumorType> tumorTypes = TumorTypeUtils.findRelevantTumorTypes(cancerType);
            List<Trial> trials = new ArrayList<>();
            for (TumorType tumorType: tumorTypes) {
                trials.addAll(getTrialsByCancerType(oncotreeMapping, trialsMapping, tumorType.getMainType()));
            }
            if (treatment != null) {
                trials = getTrialByTreatment(trials,treatment);
            }
            return new ArrayList<Trial>(trials);
        }
    }

    public Map<String, List<Trial>> getTrialsByCancerTypes(CancerTypesQuery body)
        throws UnsupportedEncodingException, IOException, ParseException {
        Map<String, List<Trial>> result = new HashMap<>();

            JSONObject trialsMapping = CacheUtils.getTrialsMapping();
            JSONObject oncotreeMapping = CacheUtils.getOncoTreeMappingTrials();

            Set<String> cancerTypes = new HashSet<>(body.getCancerTypes());
            if (cancerTypes.contains(SpecialTumorType.ALL_TUMORS.getTumorType())) {
                List<Trial> trials = new ArrayList<>();
                Set<String> nctIDSet = new HashSet<>();
                for (Object item : oncotreeMapping.keySet()) {
                    String oncoTreeCode = (String) item;
                    Tumor tumor = getTumor(oncotreeMapping, trialsMapping, oncoTreeCode);

                    for (Trial curTrial : tumor.getTrials()) {
                        if (!nctIDSet.contains(curTrial.getNctId())) {
                            nctIDSet.add(curTrial.getNctId());
                            trials.add(curTrial);
                        }
                    }
                }
                result.put(SpecialTumorType.ALL_TUMORS.getTumorType(), trials);
                return result;
            }

            for (String cancerType : cancerTypes) {
                Set<String> nctIDSet = new HashSet<>();
                List<Trial> addTrials = new ArrayList<>();
                List<Trial> trials = new ArrayList<>();
                SpecialTumorType specialTumorType = ApplicationContextSingleton.getTumorTypeBo().getSpecialTumorTypeByName(cancerType);
                if (specialTumorType != null) {
                    trials = getTrialsForSpecialCancerType(oncotreeMapping, trialsMapping, specialTumorType);
                } else {
                    trials = getTrialsByCancerType(oncotreeMapping, trialsMapping, cancerType);
                }
                for (Trial trial : trials) {
                    if (!nctIDSet.contains(trial.getNctId())) {
                        nctIDSet.add(trial.getNctId());
                        addTrials.add(trial);
                    }
                }
                result.put(cancerType, addTrials);
        }
        return result;
    }

    private static Tumor getTumor(JSONObject oncotreeMapping, JSONObject trialsMapping, String oncoTreeCode) {
        Tumor tumor = new Tumor();
        if (oncotreeMapping.containsKey(oncoTreeCode)) {
            JSONObject tumorObj = (JSONObject) oncotreeMapping.get(oncoTreeCode);
            Gson gson = new Gson();
            tumor = gson.fromJson(tumorObj.toString(), Tumor.class);

            List<Object> trials = (List<Object>) tumorObj.get("trials");
            List<String> nctIDList = new ArrayList<>();
            for (Object t: trials) {
                JSONObject trial = (JSONObject) t;
                String nctID = (String) trial.get("nctId");
                nctIDList.add(nctID);
            }

            List<Trial> trialsInfo = new ArrayList<>();
            for (String nctID: nctIDList) {
                if (trialsMapping.containsKey(nctID)) {
                    JSONObject trialObj = (JSONObject) trialsMapping.get(nctID);
                    Trial trial = gson.fromJson(trialObj.toString(), Trial.class);
                    trialsInfo.add(trial);
                }
            }
            tumor.setTrials(trialsInfo);
        }

        return tumor;
    }

    private static List<Trial> getTrialByTreatment(List<Trial> trials, String treatment) {
        List<Trial> res = new ArrayList<>();
        Set<String> drugsNames = Arrays.stream(treatment.split(",|\\+")).map(item -> item.trim()).collect(Collectors.toSet());

        res = getTrialsByDrugName(trials, drugsNames);
        return res;
    }

    private static List<Trial> getTrialsByDrugName(List<Trial> trials, Set<String> drugsNames) {
        List<Trial> res = new ArrayList<>();
        for (Trial trial : trials) {
            List<Arm> arms = trial.getArms();
            if(arms != null && !arms.isEmpty()) {
                for (Arm arm : trial.getArms()) {
                    List<Drug> drugs = arm.getDrugs();
                    if (drugs != null && !drugs.isEmpty()) {
                        if (arm.getDrugs().stream().map(Drug::getDrugName).collect(Collectors.toSet()).containsAll(drugsNames)) {
                            res.add(trial);
                            break;
                        }
                    }
                }
            }
        }
        return res;
    }

    private static List<Trial> getTrialsForSpecialCancerType(JSONObject oncotreeMapping, JSONObject trialsMapping, SpecialTumorType specialTumorType) {
        List<Trial> trials = new ArrayList<>();
        if(specialTumorType == null) return trials;

        TumorType matchedSpecialTumorType = ApplicationContextSingleton.getTumorTypeBo().getBySpecialTumor(specialTumorType);
        if (matchedSpecialTumorType == null) return trials;

        switch (specialTumorType) {
            case ALL_TUMORS:
                return new ArrayList<>(getAllTrials(oncotreeMapping, trialsMapping));
            case ALL_SOLID_TUMORS:
            case ALL_LIQUID_TUMORS:
                return ApplicationContextSingleton.getTumorTypeBo().getAllTumorTypes().stream()
                    .filter(tumorType -> tumorType.getTumorForm() != null && (tumorType.getTumorForm().equals(matchedSpecialTumorType.getTumorForm()) || tumorType.getTumorForm().equals(TumorForm.MIXED)))
                    .map(tumorType -> getTrialsByCancerType(oncotreeMapping, trialsMapping, StringUtils.isNotEmpty(tumorType.getSubtype()) ? tumorType.getSubtype() : tumorType.getMainType()))
                    .flatMap(Collection::stream).collect(Collectors.toList());
            default:
                return trials;
        }
    }

    private static List<Trial> getTrialsByCancerType(JSONObject oncotreeMapping, JSONObject trialsMapping, String cancerType) {
        List<Trial> trials = new ArrayList<>();

        Set<String> tumorCodesByMainType = new HashSet<>();
        List<TumorType> allOncoTreeSubtypes = ApplicationContextSingleton.getTumorTypeBo().getAllSubtypes();
        for (TumorType oncoTreeType : allOncoTreeSubtypes) {
            if (oncoTreeType.getMainType() != null && oncoTreeType.getMainType() != null && cancerType.equalsIgnoreCase(oncoTreeType.getMainType())) {
                tumorCodesByMainType.add(oncoTreeType.getCode());
            }
        }
        if (tumorCodesByMainType.size() > 0) {
            for (String code : tumorCodesByMainType) {
                if (oncotreeMapping.containsKey(code))
                    trials.addAll(getTumor(oncotreeMapping, trialsMapping, code).getTrials());
            }
        } else {
            TumorType matchedSubtype = ApplicationContextSingleton.getTumorTypeBo().getBySubtype(cancerType);
            if (matchedSubtype != null) {
                String codeByName = matchedSubtype.getCode();
                if (oncotreeMapping.containsKey(codeByName))
                    trials.addAll(getTumor(oncotreeMapping, trialsMapping, codeByName).getTrials());
            }
        }

        return trials;
    }

    private static Set<Trial> getAllTrials(JSONObject oncotreeMapping, JSONObject trialsMapping) {
        Set<Trial> trials = new HashSet<>();

        oncotreeMapping.keySet().forEach(code -> {
            trials.addAll(getTumor(oncotreeMapping, trialsMapping, (String) code).getTrials());
        });
        return trials;
    }
}
