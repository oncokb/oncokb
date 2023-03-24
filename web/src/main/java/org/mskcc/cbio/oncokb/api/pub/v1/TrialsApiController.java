package org.mskcc.cbio.oncokb.api.pub.v1;

import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.TumorForm;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMatching.*;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/**
 * Created by Yifu Yao on 2020-09-08
 */
@Api(tags = "Trials", description = "Clinical Trials Matching")
@Controller
public class TrialsApiController {

    @PremiumPublicApi
    @ApiOperation("Return a list of trials using OncoTree Code and/or treatment")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error", response = String.class)})
    @RequestMapping(value = "/trials", produces = {"application/json"}, method = RequestMethod.GET)
    public ResponseEntity<List<Trial>> trialsMatchingGet(
        @ApiParam(value = "", required = true) @RequestParam(value = "", required = true) String oncoTreeCode,
        @ApiParam(value = "", required = false) @RequestParam(value = "", required = false) String treatment)
        throws IOException, ParseException {
        HttpStatus status = HttpStatus.OK;

        Map<String, Trial> trialsMapping = CacheUtils.getTrialsMapping();
        Map<String, Tumor> oncotreeMapping = CacheUtils.getOncoTreeMappingTrials();

        Tumor tumor = new Tumor();
        if (oncotreeMapping.containsKey(oncoTreeCode)) {
            tumor = getTumor(oncotreeMapping,trialsMapping, oncoTreeCode);

            if (treatment == null) {
                return new ResponseEntity<List<Trial>>(tumor.getTrials(), status);
            }

            List<Trial> trial = getTrialByTreatment(tumor.getTrials(), treatment);
            return new ResponseEntity<List<Trial>>(trial, status);
        }
        return new ResponseEntity<List<Trial>>(new ArrayList<>(), status);
    }

    @PremiumPublicApi
    @ApiOperation("Return a list of trials using cancer types")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", responseContainer = "Map"),
        @ApiResponse(code = 400, message = "Error", response = String.class)})
    @RequestMapping(value = "/trials/cancerTypes", produces = {"application/json"}, method = RequestMethod.POST)
    public ResponseEntity<Map<String, List<Trial>>> getTrialsByCancerTypes(
        @ApiParam(value = "", required = true) @RequestBody() CancerTypesQuery body)
        throws UnsupportedEncodingException, IOException, ParseException {
        HttpStatus status = HttpStatus.OK;
        Map<String, List<Trial>> result = new HashMap<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            Map<String, Trial> trialsMapping = CacheUtils.getTrialsMapping();
            Map<String, Tumor> oncotreeMapping = CacheUtils.getOncoTreeMappingTrials();

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
                return new ResponseEntity<>(result, status);
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

            if (result.isEmpty()) {
                status = HttpStatus.NOT_FOUND;
                return new ResponseEntity<>(result, status);
            }
        }
        return new ResponseEntity<>(result, status);
    }

    private Tumor getTumor(Map<String, Tumor> oncotreeMapping, Map<String, Trial> trialsMapping, String oncoTreeCode) {
        Tumor tumor = new Tumor();
        if (oncotreeMapping.containsKey(oncoTreeCode)) {
            tumor = oncotreeMapping.get(oncoTreeCode);
            
            List<Trial> trials = tumor.getTrials();
            List<String> nctIDList = new ArrayList<>();
            for (Trial t: trials) {
                String nctID = t.getNctId();
                nctIDList.add(nctID);
            }
    
            List<Trial> trialsInfo = new ArrayList<>();
            for (String nctID: nctIDList) {
                if (trialsMapping.containsKey(nctID)) {
                    Trial trial = trialsMapping.get(nctID);
                    trialsInfo.add(trial);
                }
            }
            tumor.setTrials(trialsInfo);
        }

        return tumor;
    }

    private List<Trial> getTrialByTreatment(List<Trial> trials, String treatment) {
        List<Trial> res = new ArrayList<>();
        Set<String> drugsNames = Arrays.stream(treatment.split(",|\\+")).map(item -> item.trim()).collect(Collectors.toSet());

        res = getTrialByDrugName(trials, drugsNames);
        return res;
    }

    private List<Trial> getTrialByDrugName(List<Trial> trials, Set<String> drugsNames) {
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

    private List<Trial> getTrialsForSpecialCancerType(Map<String, Tumor> oncotreeMapping, Map<String, Trial> trialsMapping, SpecialTumorType specialTumorType) {
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

    private List<Trial> getTrialsByCancerType(Map<String, Tumor> oncotreeMapping, Map<String, Trial> trialsMapping, String cancerType) {
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

    private Set<Trial> getAllTrials(Map<String, Tumor> oncotreeMapping, Map<String, Trial> trialsMapping) {
        Set<Trial> trials = new HashSet<>();

        oncotreeMapping.entrySet().forEach(code -> {
            trials.addAll(getTumor(oncotreeMapping, trialsMapping, (String) code.getKey()).getTrials());
        });
        return trials;
    }
}
