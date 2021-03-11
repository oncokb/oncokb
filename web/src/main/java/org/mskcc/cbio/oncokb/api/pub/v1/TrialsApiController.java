package org.mskcc.cbio.oncokb.api.pub.v1;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.mskcc.cbio.oncokb.util.ClinicalTrialsUtils;

/**
 * Created by Yifu Yao on 2020-09-08
 */
@Api(tags = "Trials", description = "Clinical Trials Matching")
@Controller
public class TrialsApiController {

    @PublicApi
    @PremiumPublicApi
    @ApiOperation("Return a list of trials using OncoTree Code and/or treatment")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", responseContainer = "List"),
            @ApiResponse(code = 400, message = "Error", response = String.class) })
    @RequestMapping(value = "/trials", produces = { "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<List<ClinicalTrial>> trialsMatchingGet(
            @ApiParam(value = "", required = true) @RequestParam(value = "", required = true) String oncoTreeCode,
            @ApiParam(value = "", required = false) @RequestParam(value = "", required = false) String treatment)
            throws IOException, ParseException {
        HttpStatus status = HttpStatus.OK;

        JSONObject jsonObject = ClinicalTrialsUtils.getInstance().getMappingObject();

        // JSONObject jsonObject = getMappingObject();

        Tumor tumor = new Tumor();
        if (jsonObject.containsKey(oncoTreeCode)) {
            tumor = ClinicalTrialsUtils.getInstance().getTumor(jsonObject, oncoTreeCode);
            if (treatment == null) {
                return new ResponseEntity<List<ClinicalTrial>>(tumor.getTrials(), status);
            }

            List<ClinicalTrial> trial = ClinicalTrialsUtils.getInstance().filterTrialsByTreatment(tumor.getTrials(), treatment);
            return new ResponseEntity<List<ClinicalTrial>>(trial, status);
        }
        return new ResponseEntity<List<ClinicalTrial>>(new ArrayList<>(), status);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation("Return a list of clinical trials by cancer type, treatment, location and distance")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", responseContainer = "Map"),
            @ApiResponse(code = 400, message = "Error", response = String.class) })
    @RequestMapping(value = "/trials/cancerType", produces = { "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<List<ClinicalTrial>> trialsGetByCancerType(
            @ApiParam(value = "The cancer type that clinical trials belong to. Support cancer type name and OncoTree Code. Example: Glioma or AASTR. Support special cancer types: ALL_TUMORS, ALL_SOLID_TUMORS, ALL_LIQUID_TUMORS.") @RequestParam(value = "cancerType", required = true) String cancerType,
            @ApiParam(value = "Consisted of single/multuple drugs. Support drugs name or drugs' NCIT code. For multiple drugs treatment, use '+' as separator. Example: Everolimus+C95701") @RequestParam(value = "treatment", required = false) String treatment,
            @ApiParam(value = "The address of your location. Support zip code. Must be spcified with country. Example: New York City, NY") @RequestParam(value = "address", required = false) String address,
            @ApiParam(value = "The country of your location. Must be specified with address. Example: United States") @RequestParam(value = "country", required = false) String country,
            @ApiParam(value = "The radius from your location. Must be specified with address and country. Example: 100, which means all trails have any site locates within 100 km from your location. If not specify, the default distance is 100km.") @RequestParam(value = "distance", required = false) Double distance)
            throws UnsupportedEncodingException, IOException, ParseException {
        HttpStatus status = HttpStatus.OK;
        List<ClinicalTrial> result = new ArrayList<>();

        if (treatment != null && treatment.indexOf(",") != -1) {
            status = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(result, status);
        }
        String location = null;
        if (address != null && !address.isEmpty() && country != null && !country.isEmpty()){
            location = String.format("%s, %s", address, country);
        }
        JSONObject jsonObject = ClinicalTrialsUtils.getInstance().getMappingObject();
        // JSONObject jsonObject = getMappingObject();

        if (cancerType.equals(SpecialTumorType.ALL_TUMORS.toString())) {
            Set<String> nctIDSet = new HashSet<>();
            for (Object item : jsonObject.keySet()) {
                String oncoTreeCode = (String) item;
                Tumor tumor = ClinicalTrialsUtils.getInstance().getTumor(jsonObject, oncoTreeCode);
                for (ClinicalTrial curTrial : tumor.getTrials()) {
                    if (!nctIDSet.contains(curTrial.getNctId())) {
                        nctIDSet.add(curTrial.getNctId());
                        result.add(curTrial);
                    }
                }
            }
            result = ClinicalTrialsUtils.getInstance().filterTrialsBytreatmentAndLocation(result, treatment, location, distance);
            return new ResponseEntity<>(result, status);
        }

        Set<String> nctIDSet = new HashSet<>();
        List<ClinicalTrial> trials = new ArrayList<>();
        SpecialTumorType specialTumorType = null;
        try {
            specialTumorType = SpecialTumorType.valueOf(cancerType);
        } catch (Exception ex) {
        }
        if (specialTumorType != null) {
            trials = ClinicalTrialsUtils.getInstance().filterTrialsBySpecialCancerType(jsonObject, specialTumorType);
        } else {
            trials = ClinicalTrialsUtils.getInstance().filterTrialsByCancerType(jsonObject, cancerType);
        }
        for (ClinicalTrial trial : trials) {
            if (!nctIDSet.contains(trial.getNctId())) {
                nctIDSet.add(trial.getNctId());
                result.add(trial);
            }
        }
        result = ClinicalTrialsUtils.getInstance().filterTrialsBytreatmentAndLocation(result, treatment, location, distance);
        return new ResponseEntity<>(result, status);
    }
}