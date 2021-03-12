package org.mskcc.cbio.oncokb.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.gson.Gson;

import org.json.simple.parser.ParseException;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Arms;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Coordinates;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Site;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Tumor;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by Yifu Yao on 3/9/2021
 */
public class ClinicalTrialsUtils {
    private static ClinicalTrialsUtils instance = null;

    public ClinicalTrialsUtils() {

    }

    public static ClinicalTrialsUtils getInstance() {
        if (instance == null) {
            instance = new ClinicalTrialsUtils();
        }
        return instance;
    }

    public JSONObject getMappingObject() throws UnsupportedEncodingException, IOException, ParseException {
        JSONObject jsonObject = new JSONObject();
        JSONParser jsonParser = new JSONParser();
        if (PropertiesUtils.getProperties("aws.s3.accessKey") != null
                && !PropertiesUtils.getProperties("aws.s3.accessKey").isEmpty()) {
            S3Utils s3Utils = new S3Utils();
            S3ObjectInputStream inputStream = s3Utils.getObject("oncokb", "drug-matching/result.json").get()
                    .getObjectContent();
            jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
        } else {
            InputStreamReader inputStream = new InputStreamReader(
                ClinicalTrialsUtils.class.getResourceAsStream("/data/oncotree/clinical-trials-mapping-result.json"),
                    "UTF-8");
            jsonObject = (JSONObject) jsonParser.parse(inputStream);
        }

        return jsonObject;
    }

    public Tumor getTumor(JSONObject jsonObject, String oncoTreeCode) {
        Tumor tumor = new Tumor();
        if (jsonObject.containsKey(oncoTreeCode)) {
            JSONObject tumorObj = (JSONObject) jsonObject.get(oncoTreeCode);
            Gson gson = new Gson();
            tumor = gson.fromJson(tumorObj.toString(), Tumor.class);
        }

        return tumor;
    }

    public List<ClinicalTrial> filterTrialsByTreatment(List<ClinicalTrial> trials, String treatment) {
        if (treatment != null && !treatment.isEmpty()) {
            List<ClinicalTrial> res = new ArrayList<>();
            Set<String> drugs = Arrays.stream(treatment.split("\\+")).map(item -> item.trim().toLowerCase())
            .collect(Collectors.toSet());
            res = filterTrialsByDrugNameOrCode(trials, drugs);
            return res;
        }
        return trials;
    }

    public List<ClinicalTrial> filterTrialsByDrugNameOrCode(List<ClinicalTrial> trials, Set<String> drugs) {
        if (!drugs.isEmpty()){
            List<ClinicalTrial> res = new ArrayList<>();
            drugs = drugs.stream().map(drug -> drug.toLowerCase()).collect(Collectors.toSet());
            for (ClinicalTrial trial : trials) {
                for (Arms arm : trial.getArms()) {
                    if (Stream
                            .concat(arm.getDrugs().stream().map(drug -> drug.getNcitCode().toLowerCase()),
                                    arm.getDrugs().stream().map(drug -> drug.getDrugName().toLowerCase()))
                            .collect(Collectors.toSet()).containsAll(drugs)) {
                        res.add(trial);
                        break;
                    }
                }
            }
            return res;
        }
        return trials;
    }

    public List<ClinicalTrial> filterTrialsBySpecialCancerType(JSONObject tumors, SpecialTumorType specialTumorType) {
        List<ClinicalTrial> trials = new ArrayList<>();
        if (specialTumorType == null)
            return trials;

        TumorType matchedSpecialTumorType = TumorTypeUtils.getBySpecialTumor(specialTumorType);
        if (matchedSpecialTumorType == null)
            return trials;

        switch (specialTumorType) {
        case ALL_TUMORS:
            return new ArrayList<>(getAllTrials(tumors));
        case ALL_SOLID_TUMORS:
        case ALL_LIQUID_TUMORS:
            return TumorTypeUtils.getAllTumorTypes().stream()
                    .filter(tumorType -> tumorType.getTumorForm() != null
                            && tumorType.getTumorForm().equals(matchedSpecialTumorType.getTumorForm()))
                    .map(tumorType -> filterTrialsByCancerType(tumors,
                            StringUtils.isNotEmpty(tumorType.getSubtype()) ? tumorType.getSubtype()
                                    : tumorType.getMainType()))
                    .flatMap(Collection::stream).collect(Collectors.toList());
        default:
            return trials;
        }
    }

    public List<ClinicalTrial> filterTrialsByCancerType(JSONObject tumors, String cancerType) {
        List<ClinicalTrial> trials = new ArrayList<>();

        Set<String> tumorCodesByMainType = new HashSet<>();
        List<TumorType> allOncoTreeSubtypes = TumorTypeUtils.getAllSubtypes();
        for (TumorType oncoTreeType : allOncoTreeSubtypes) {
            if ((oncoTreeType.getMainType() != null && cancerType.equalsIgnoreCase(oncoTreeType.getMainType()))
                    || (oncoTreeType.getCode() != null && cancerType.equalsIgnoreCase(oncoTreeType.getCode()))) {
                tumorCodesByMainType.add(oncoTreeType.getCode());
            }
        }
        if (tumorCodesByMainType.size() > 0) {
            for (String code : tumorCodesByMainType) {
                if (tumors.containsKey(code))
                    trials.addAll(getTumor(tumors, code).getTrials());
            }
        } else {
            TumorType matchedSubtype = TumorTypeUtils.getBySubtype(cancerType);
            if (matchedSubtype != null) {
                String codeByName = matchedSubtype.getCode();
                if (tumors.containsKey(codeByName))
                    trials.addAll(getTumor(tumors, codeByName).getTrials());
            }
            else{ // cancertype is oncotree code
                if (tumors.containsKey(cancerType))
                    trials.addAll(getTumor(tumors, cancerType).getTrials());
            }
        }

        return trials;
    }

    private Set<ClinicalTrial> getAllTrials(JSONObject tumors) {
        Set<ClinicalTrial> trials = new HashSet<>();
        tumors.entrySet().forEach(code -> {
            trials.addAll(getTumor(tumors, (String) code).getTrials());
        });
        return trials;
    }

    public List<ClinicalTrial> filterTrialsByLocation(List<ClinicalTrial> trials, String location, Double distance) {
        if (location != null){
            if (distance == null) distance = 100.0;
            List<ClinicalTrial> res = new ArrayList<>();
            Coordinates ori = OpenStreetMapUtils.getInstance().getCoordinates(location);
            for (ClinicalTrial trial : trials) {
                for (Site site : trial.getSites()) {
                    Coordinates des = site.getOrg().getCoordinates();
                    if (des == null) {
                        if (site.getOrg().getCity() != null && site.getOrg().getState() != null
                                && site.getOrg().getCountry() != null) {
                            String address = String.format("%s, %s, %s", site.getOrg().getCity(), site.getOrg().getState(),
                                    site.getOrg().getCountry());
                            des = OpenStreetMapUtils.getInstance().getCoordinates(address);
                        } else
                            continue;
                    }
                    if (OpenStreetMapUtils.getInstance().calculateDistance(ori, des) <= distance) {
                        res.add(trial);
                        break;
                    }
                }
            }
            return res;
        }
        return trials;
    }

    public List<ClinicalTrial> filterTrialsBytreatmentAndLocation(List<ClinicalTrial> trials, String treatment, String location,
            Double distance) {
        trials = filterTrialsByTreatment(trials, treatment);
        trials = filterTrialsByLocation(trials, location, distance);
        return trials;
    }

    public List<ClinicalTrial> filterTrialsByTreatmentForIndicatorQueryTreatment(String cancerType, Set<String> drugs) throws UnsupportedEncodingException, IOException, ParseException{
        JSONObject object = getMappingObject();
        List<ClinicalTrial> trials = filterTrialsByCancerType(object, cancerType);
        List<ClinicalTrial> res = filterTrialsByDrugNameOrCode(trials, drugs);
        return res;
    }
}
