package org.mskcc.cbio.oncokb.util;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.gson.Gson;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.ClinicalTrial;

public class ClinicalTrialsUtilsTest{
    @Test
    public void testGetMappingObject(){
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject1 = new JSONObject();
        // System.out.println(System.getProperty("user.dir"));
        try {
            if (PropertiesUtils.getProperties("aws.s3.accessKey") != null
            && !PropertiesUtils.getProperties("aws.s3.accessKey").isEmpty()){
                System.out.println("Reading from S3");
                S3Utils s3Utils = new S3Utils();
                S3ObjectInputStream inputStream = s3Utils.getObject("oncokb", "drug-matching/result.json").get()
                        .getObjectContent();
                jsonObject1 = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
            }
            else {
                System.out.println("Reading locally");
                jsonObject1 = (JSONObject)jsonParser.parse(new FileReader("./src/main/resources/data/oncotree/clinical-trials-mapping-result.json"));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject2 = new JSONObject();
        try {
            jsonObject2 = ClinicalTrialsUtils.getInstance().getMappingObject();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        assertEquals(jsonObject1, jsonObject2);
    }

    @Test
    public void testFilterTrialsByTreatment(){
        Gson gson = new Gson();
        try {
            JSONObject jsonObject = ClinicalTrialsUtils.getInstance().getMappingObject();
            List<ClinicalTrial> trials = ClinicalTrialsUtils.getInstance().filterTrialsByCancerType(jsonObject, "Melanoma");
            List<ClinicalTrial> res = ClinicalTrialsUtils.getInstance().filterTrialsByTreatment(trials, "Dabrafenib");
            System.out.println(res.size());         
            try {
                FileWriter file = new FileWriter("C:\\Users\\Yifu\\Desktop\\test.json");
                file.write(gson.toJson(res));
                file.flush();
                file.close();
                System.out.println("FInished");
            } catch (IOException e) {
            }
        } catch (IOException | ParseException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }

    @Test
    public void testFilterTrialsByDrugNameOrCode(){
        Set<String> drugs = new HashSet<>();
        drugs.add("Dabrafenib");
        Gson gson = new Gson();
        try {
            JSONObject jsonObject = ClinicalTrialsUtils.getInstance().getMappingObject();
            List<ClinicalTrial> trials = ClinicalTrialsUtils.getInstance().filterTrialsByCancerType(jsonObject, "Melanoma");
            List<ClinicalTrial> res = ClinicalTrialsUtils.getInstance().filterTrialsByDrugNameOrCode(trials, drugs);
            System.out.println(res.size());         
            try {
                FileWriter file = new FileWriter("C:\\Users\\Yifu\\Desktop\\test.json");
                file.write(gson.toJson(res));
                file.flush();
                file.close();
                System.out.println("Finished");
            } catch (IOException e) {
            }
        } catch (IOException | ParseException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }

    @Test
    public void testFilterTrialsByLocation(){
        Gson gson = new Gson();
        try {
            JSONObject jsonObject = ClinicalTrialsUtils.getInstance().getMappingObject();
            long start = System.currentTimeMillis();
            List<ClinicalTrial> trials = ClinicalTrialsUtils.getInstance().filterTrialsByCancerType(jsonObject, "Melanoma");
            System.out.println(trials.size());
            List<ClinicalTrial> res = ClinicalTrialsUtils.getInstance().filterTrialsByLocation(trials, "Columbia, MO, United States", 100.0);
            long end = System.currentTimeMillis();
            System.out.println(end - start + " s : " + res.size());         
            try {
                FileWriter file = new FileWriter("C:\\Users\\Yifu\\Desktop\\test1.json");
                file.write(gson.toJson(res));
                file.flush();
                file.close();
                System.out.println("FInished");
            } catch (IOException e) {
            }
        } catch (IOException | ParseException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }

    @Test
    public void testFilterTrialsByTreatmentAndLocation(){
        Gson gson = new Gson();
        try {
            JSONObject jsonObject = ClinicalTrialsUtils.getInstance().getMappingObject();
            List<ClinicalTrial> trials = ClinicalTrialsUtils.getInstance().filterTrialsByCancerType(jsonObject, "Melanoma");
            trials = ClinicalTrialsUtils.getInstance().filterTrialsBytreatmentAndLocation(trials, "Dabrafenib", "Columbia, MO, United States", 100.0);
            System.out.println(trials.size());        
            try {
                FileWriter file = new FileWriter("C:\\Users\\Yifu\\Desktop\\test2.json");
                file.write(gson.toJson(trials));
                file.flush();
                file.close();
                System.out.println("Finished");
            } catch (IOException e) {
            }
        } catch (IOException | ParseException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }
}
