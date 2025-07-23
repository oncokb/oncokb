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
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpError;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;
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
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.*;
import org.mskcc.cbio.oncokb.model.TumorType;
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
        @ApiResponse(code = 400, message = "Error", response = ApiHttpError.class)})
    @RequestMapping(value = "/trials", produces = {"application/json"}, method = RequestMethod.GET)
    public ResponseEntity<List<Trial>> trialsMatchingGet(
        @ApiParam(value = "", required = true) @RequestParam(value = "", required = true) String oncoTreeCode,
        @ApiParam(value = "", required = false) @RequestParam(value = "", required = false) String treatment)
        throws IOException, ParseException {

        JSONObject trialsJSON = CacheUtils.getTrialsJSON();
        JSONObject oncotreeMappingJSON = CacheUtils.getOncoTreeMappingTrials();

        Tumor tumor = new Tumor();
        if (oncotreeMappingJSON.containsKey(oncoTreeCode)) {
            tumor = getTumor(oncotreeMappingJSON,trialsJSON, oncoTreeCode);

            if (treatment == null) {
                return new ResponseEntity<List<Trial>>(tumor.getTrials(), HttpStatus.OK);
            }

            List<Trial> trial = getTrialByTreatment(tumor.getTrials(), treatment);
            return new ResponseEntity<List<Trial>>(trial, HttpStatus.OK);
        }
        return new ResponseEntity<List<Trial>>(new ArrayList<>(), HttpStatus.OK);
    }

    @PremiumPublicApi
    @ApiOperation("Return a list of trials using cancer types")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", responseContainer = "Map"),
        @ApiResponse(code = 400, message = "Error", response = ApiHttpError.class),
        @ApiResponse(code = 404, message = "Error", responseContainer = "Map")})
    @RequestMapping(value = "/trials/cancerTypes", produces = {"application/json"}, method = RequestMethod.POST)
    public ResponseEntity<Map<String, List<Trial>>> trialsGetByCancerTypes(
        @ApiParam(value = "", required = true) @RequestBody() CancerTypesQuery body)
        throws UnsupportedEncodingException, IOException, ParseException, ApiHttpErrorException {
        Map<String, List<Trial>> result = new HashMap<>();

        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        } else {
            JSONObject trialsJSON = CacheUtils.getTrialsJSON();
            JSONObject oncotreeMappingJSON = CacheUtils.getOncoTreeMappingTrials();

            Set<String> cancerTypes = new HashSet<>(body.getCancerTypes());
            if (cancerTypes.contains(SpecialTumorType.ALL_TUMORS.getTumorType())) {
                List<Trial> trials = new ArrayList<>();
                Set<String> nctIDSet = new HashSet<>();
                for (Object item : oncotreeMappingJSON.keySet()) {
                    String oncoTreeCode = (String) item;
                    Tumor tumor = getTumor(oncotreeMappingJSON, trialsJSON, oncoTreeCode);

                    for (Trial curTrial : tumor.getTrials()) {
                        if (!nctIDSet.contains(curTrial.getNctId())) {
                            nctIDSet.add(curTrial.getNctId());
                            trials.add(curTrial);
                        }
                    }
                }
                result.put(SpecialTumorType.ALL_TUMORS.getTumorType(), trials);
                return new ResponseEntity<>(result, HttpStatus.OK);
            }

            for (String cancerType : cancerTypes) {
                Set<String> nctIDSet = new HashSet<>();
                List<Trial> addTrials = new ArrayList<>();
                List<Trial> trials = new ArrayList<>();
                SpecialTumorType specialTumorType = ApplicationContextSingleton.getTumorTypeBo().getSpecialTumorTypeByName(cancerType);
                if (specialTumorType != null) {
                    trials = getTrialsForSpecialCancerType(oncotreeMappingJSON, trialsJSON, specialTumorType);
                } else {
                    trials = getTrialsByCancerType(oncotreeMappingJSON, trialsJSON, cancerType);
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
                return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private Tumor getTumor(JSONObject oncotreeMappingJSON, JSONObject trialsJSON, String oncoTreeCode) {
        Tumor tumor = new Tumor();
        if (oncotreeMappingJSON.containsKey(oncoTreeCode)) {
            JSONObject tumorObj = (JSONObject) oncotreeMappingJSON.get(oncoTreeCode);
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
                if (trialsJSON.containsKey(nctID)) {
                    JSONObject trialObj = (JSONObject) trialsJSON.get(nctID);
                    Trial trial = gson.fromJson(trialObj.toString(), Trial.class);
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
            List<Arms> arms = trial.getArms();
            if(arms != null && !arms.isEmpty()) {
                for (Arms arm : trial.getArms()) {
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

    private List<Trial> getTrialsForSpecialCancerType(JSONObject tumors, JSONObject trialData, SpecialTumorType specialTumorType) {
        List<Trial> trials = new ArrayList<>();
        if(specialTumorType == null) return trials;

        TumorType matchedSpecialTumorType = ApplicationContextSingleton.getTumorTypeBo().getBySpecialTumor(specialTumorType);
        if (matchedSpecialTumorType == null) return trials;

        switch (specialTumorType) {
            case ALL_TUMORS:
                return new ArrayList<>(getAllTrials(tumors, trialData));
            case ALL_SOLID_TUMORS:
            case ALL_LIQUID_TUMORS:
                return ApplicationContextSingleton.getTumorTypeBo().getAllTumorTypes().stream()
                    .filter(tumorType -> tumorType.getTumorForm() != null && tumorType.getTumorForm().equals(matchedSpecialTumorType.getTumorForm()))
                    .map(tumorType -> getTrialsByCancerType(tumors, trialData, StringUtils.isNotEmpty(tumorType.getSubtype()) ? tumorType.getSubtype() : tumorType.getMainType()))
                    .flatMap(Collection::stream).collect(Collectors.toList());
            default:
                return trials;
        }
    }

    private List<Trial> getTrialsByCancerType(JSONObject tumors, JSONObject trialData, String cancerType) {
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
                if (tumors.containsKey(code))
                    trials.addAll(getTumor(tumors, trialData, code).getTrials());
            }
        } else {
            TumorType matchedSubtype = ApplicationContextSingleton.getTumorTypeBo().getBySubtype(cancerType);
            if (matchedSubtype != null) {
                String codeByName = matchedSubtype.getCode();
                if (tumors.containsKey(codeByName))
                    trials.addAll(getTumor(tumors, trialData, codeByName).getTrials());
            }
        }

        return trials;
    }

    private Set<Trial> getAllTrials(JSONObject tumors, JSONObject trialData) {
        Set<Trial> trials = new HashSet<>();

        tumors.entrySet().forEach(code -> {
            trials.addAll(getTumor(tumors, trialData, (String) code).getTrials());
        });
        return trials;
    }
}
