package org.mskcc.cbio.oncokb.api.pub.v1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
import org.mskcc.cbio.oncokb.service.S3Service;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.TumorTypeUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.mskcc.cbio.oncokb.util.OpenStreetMapUtils;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;

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

        JSONObject jsonObject = getMappingObject();

        Tumor tumor = new Tumor();
        if (jsonObject.containsKey(oncoTreeCode)) {
            tumor = getTumor(jsonObject, oncoTreeCode);
            if (treatment == null) {
                return new ResponseEntity<List<ClinicalTrial>>(tumor.getTrials(), status);
            }

            List<ClinicalTrial> trial = filterTrialsByTreatment(tumor.getTrials(), treatment);
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
        JSONObject jsonObject = getMappingObject();

        if (cancerType.equals(SpecialTumorType.ALL_TUMORS.toString())) {
            Set<String> nctIDSet = new HashSet<>();
            for (Object item : jsonObject.keySet()) {
                String oncoTreeCode = (String) item;
                Tumor tumor = getTumor(jsonObject, oncoTreeCode);
                for (ClinicalTrial curTrial : tumor.getTrials()) {
                    if (!nctIDSet.contains(curTrial.getNctId())) {
                        nctIDSet.add(curTrial.getNctId());
                        result.add(curTrial);
                    }
                }
            }
            filterTrialsBytreatmentAndLocation(result, treatment, location, distance);
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
            trials = filterTrialsBySpecialCancerType(jsonObject, specialTumorType);
        } else {
            trials = filterTrialsByCancerType(jsonObject, cancerType);
        }
        for (ClinicalTrial trial : trials) {
            if (!nctIDSet.contains(trial.getNctId())) {
                nctIDSet.add(trial.getNctId());
                result.add(trial);
            }
        }
        filterTrialsBytreatmentAndLocation(result, treatment, location, distance);
        return new ResponseEntity<>(result, status);
    }

    private JSONObject getMappingObject() throws UnsupportedEncodingException, IOException, ParseException {
        JSONObject jsonObject = new JSONObject();
        JSONParser jsonParser = new JSONParser();
        if (PropertiesUtils.getProperties("aws.s3.accessKey") != null
                && !PropertiesUtils.getProperties("aws.s3.accessKey").isEmpty()) {
            S3Service s3Service = new S3Service();
            S3ObjectInputStream inputStream = s3Service.getObject("oncokb", "drug-matching/result.json").get()
                    .getObjectContent();
            jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
        } else {
            InputStreamReader inputStream = new InputStreamReader(
                    TumorTypeUtils.class.getResourceAsStream("/data/oncotree/clinical-trials-mapping-result.json"),
                    "UTF-8");
            jsonObject = (JSONObject) jsonParser.parse(inputStream);
        }
        return jsonObject;
    }

    private Tumor getTumor(JSONObject jsonObject, String oncoTreeCode) {
        Tumor tumor = new Tumor();
        if (jsonObject.containsKey(oncoTreeCode)) {
            JSONObject tumorObj = (JSONObject) jsonObject.get(oncoTreeCode);
            Gson gson = new Gson();
            tumor = gson.fromJson(tumorObj.toString(), Tumor.class);
        }

        return tumor;
    }

    private List<ClinicalTrial> filterTrialsByTreatment(List<ClinicalTrial> trials, String treatment) {
        List<ClinicalTrial> res = new ArrayList<>();
        Set<String> drugs = Arrays.stream(treatment.split("\\+")).map(item -> item.trim().toLowerCase())
                .collect(Collectors.toSet());

        res = filterTrialsByDrugNameOrCode(trials, drugs);
        return res;
    }

    private List<ClinicalTrial> filterTrialsByDrugNameOrCode(List<ClinicalTrial> trials, Set<String> drugs) {
        List<ClinicalTrial> res = new ArrayList<>();
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

    private List<ClinicalTrial> filterTrialsBySpecialCancerType(JSONObject tumors, SpecialTumorType specialTumorType) {
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

    private List<ClinicalTrial> filterTrialsByCancerType(JSONObject tumors, String cancerType) {
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

    private List<ClinicalTrial> filterTrialsByLocation(List<ClinicalTrial> trials, String location, Double distance) {
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
                    } else continue;
                }
                if (OpenStreetMapUtils.getInstance().calculateDistance(ori, des) <= distance) {
                    res.add(trial);
                    break;
                }
            }
        }
        return res;
    }

    private void filterTrialsBytreatmentAndLocation(List<ClinicalTrial> trials, String treatment, String location, Double distance){
        if (treatment != null && !treatment.isEmpty()) {
            trials = filterTrialsByTreatment(trials, treatment);
        }
        if (location != null) {
            if (distance == null) distance = 100.0;
            trials = filterTrialsByLocation(trials, location, distance);
        }
    }
}