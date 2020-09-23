package org.mskcc.cbio.oncokb.api.pub.v1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.gson.Gson;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.*;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.mskcc.cbio.oncokb.util.TumorTypeUtils;

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

    static final Set<String> SpecialTumorTypes = new HashSet<>(Arrays.asList("All Tumors","All Liquid Tumors","All Solid Tumors"));
    static final Map<String, String> TumorFormMap;
    static
    {
        TumorFormMap = new HashMap<String, String>();  
        TumorFormMap.put(SpecialTumorType.ALL_LIQUID_TUMORS.getTumorType(), "LIQUID");
        TumorFormMap.put(SpecialTumorType.ALL_SOLID_TUMORS.getTumorType(), "SOLID");
    }
    final String s3AccessKey = PropertiesUtils.getProperties("aws.s3.accessKey");
    final String s3SecretKey = PropertiesUtils.getProperties("aws.s3.secretKey");
    final String s3Region = PropertiesUtils.getProperties("aws.s3.region");

    @PremiumPublicApi
    @ApiOperation("Return a list of trials using OncoTree Code and/or treatment")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", responseContainer = "List"),
            @ApiResponse(code = 400, message = "Error", response = String.class) })
    @RequestMapping(value = "/trials", produces = { "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<List<Trial>> trialsMatchingGet(
            @ApiParam(value = "", required = true) @RequestParam(value = "", required = true) String oncoTreeCode,
            @ApiParam(value = "", required = false) @RequestParam(value = "", required = false) String treatment)
            throws IOException, ParseException {
        HttpStatus status = HttpStatus.OK;

        AWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(s3Region).build();

        S3Object s3object = s3client.getObject("oncokb", "drug-matching/result.json");
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));

        Tumor tumor = new Tumor();
        if (jsonObject.containsKey(oncoTreeCode)){
            tumor = getTumor(jsonObject, oncoTreeCode);
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
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", responseContainer = "Map"),
            @ApiResponse(code = 400, message = "Error", response = String.class) })
    @RequestMapping(value = "/trials/cancerTypes", produces = { "application/json" }, method = RequestMethod.POST)
    public ResponseEntity<Map<String, List<Trial>>> trialsGetByCancerTypes(
            @ApiParam(value = "", required = true) @RequestBody() CancerTypesQuery body)
            throws UnsupportedEncodingException, IOException, ParseException {
        HttpStatus status = HttpStatus.OK;
        Map<String, List<Trial>> result = new HashMap<>();
        
        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            AWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
            AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(s3Region)
                .build();
            S3Object s3object = s3client.getObject("oncokb", "drug-matching/result.json");
            S3ObjectInputStream inputStream = s3object.getObjectContent();
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject)jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
            
            Set<String> cancerTypes = new HashSet<>(body.getCancerTypes());
            if (cancerTypes.contains(SpecialTumorType.ALL_TUMORS.getTumorType())){
                List<Trial> trials = new ArrayList<>();
                Set<String> nctIDSet = new HashSet<>();
                for (Object item: jsonObject.keySet()){
                    String oncoTreeCode = (String)item;
                    Tumor tumor = getTumor(jsonObject, oncoTreeCode);
                    for (Trial curTrial: tumor.getTrials()){
                        if (!nctIDSet.contains(curTrial.getNctId())){
                            nctIDSet.add(curTrial.getNctId());
                            trials.add(curTrial);
                        }
                    }
                }
                result.put(SpecialTumorType.ALL_TUMORS.getTumorType(), trials);
                return new ResponseEntity<>(result, status);
            }

            for (String cancerType: cancerTypes){
                Set<String> nctIDSet = new HashSet<>();
                List<Trial> addTrials = new ArrayList<>();
                List<Trial> trials = new ArrayList<>();
                if (SpecialTumorTypes.contains(cancerType)){
                    trials = getTrialsForSpecialCancerType(jsonObject, cancerType);
                }
                else{
                    trials = getTrialsByCancerType(jsonObject, cancerType);
                }
                for (Trial trial: trials){
                    if (!nctIDSet.contains(trial.getNctId())){
                        nctIDSet.add(trial.getNctId());
                        addTrials.add(trial);
                    }
                }
                result.put(cancerType, addTrials);
            }

            if (result.isEmpty()){
                status = HttpStatus.NOT_FOUND;
                return new ResponseEntity<>(result, status);
            }
        }
        return new ResponseEntity<>(result, status);
    }

    private Tumor getTumor(JSONObject jsonObject, String oncoTreeCode){
        Tumor tumor = new Tumor();
        if (jsonObject.containsKey(oncoTreeCode)){
            JSONObject tumorObj = (JSONObject)jsonObject.get(oncoTreeCode);
            Gson gson = new Gson();
            tumor = gson.fromJson(tumorObj.toString(), Tumor.class);
        }

        return tumor;
    }

    private List<Trial> getTrialByTreatment(List<Trial> trials, String treatment){
        List<Trial> res = new ArrayList<>();
        Set<String> drugsNames = Arrays.stream(treatment.split(",|\\+")).map(item-> item.trim()).collect(Collectors.toSet());

        res = getTrialByDrugName(trials, drugsNames);
        return res;
    }

    private List<Trial> getTrialByDrugName(List<Trial> trials, Set<String> drugsNames){
        List<Trial> res = new ArrayList<>();
        for (Trial trial: trials){
            for (Arms arm: trial.getArms()){
                if (arm.getDrugs().stream().map(Drug::getDrugName).collect(Collectors.toSet()).containsAll(drugsNames)){
                    res.add(trial);
                    break;
                }
            }
        }
        return res;
    }

    private List<Trial> getTrialsForSpecialCancerType(JSONObject tumors, String cancerType){
        List<Trial> trials = new ArrayList<>();
        Set<String> reMappedCancerTypes = getTumorNamesBySpecialTumorType(cancerType);
        for (String type: reMappedCancerTypes){
            trials.addAll(getTrialsByCancerType(tumors, type));
        }
        return trials;
    }

    private Set<String> getTumorNamesBySpecialTumorType(String cancerType){
        Set<String> tumorTypes = new HashSet<>();
        List<TumorType> allOncoTreeTypes = TumorTypeUtils.getAllOncoTreeTypes();
        for (TumorType type: allOncoTreeTypes){
            if (type.getMainType() != null && type.getMainType().getTumorForm() != null && TumorFormMap.get(cancerType).equalsIgnoreCase(type.getMainType().getTumorForm().toString())){
                String name = TumorTypeUtils.getTumorTypeName(type);
                if (name.length()>0) tumorTypes.add(name);
            }
        }       
        return tumorTypes;
    }

    private List<Trial> getTrialsByCancerType(JSONObject tumors, String cancerType){
        List<Trial> trials = new ArrayList<>();

        Set<String> tumorCodesByMainType = new HashSet<>();
        List<TumorType> allOncoTreeSubtypes = TumorTypeUtils.getAllOncoTreeSubtypes();
        for (TumorType oncoTreeType: allOncoTreeSubtypes){
            if(oncoTreeType.getMainType() != null && oncoTreeType.getMainType().getName() != null && cancerType.equalsIgnoreCase(oncoTreeType.getMainType().getName())){
                tumorCodesByMainType.add(oncoTreeType.getCode());
            }
        }
        if (tumorCodesByMainType.size() > 0){
            for (String code: tumorCodesByMainType){
                    if (tumors.containsKey(code))
                        trials.addAll(getTumor(tumors, code).getTrials());
            }
        }
        else{
            List<TumorType> matchedSubtypes = TumorTypeUtils.getOncoTreeSubtypesByName(Collections.singletonList(cancerType));
            if (matchedSubtypes != null && matchedSubtypes.size() > 0) {
                String codeByName = matchedSubtypes.get(0).getCode();
                if (tumors.containsKey(codeByName))
                    trials.addAll(getTumor(tumors, codeByName).getTrials());
            }
        }

        return trials;
    }
}