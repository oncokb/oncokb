package org.mskcc.cbio.oncokb.util;

import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.ClinicalTrialMap;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.GenericMapClass;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Site;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.TumorMap;

public class ClinicalTrialsUtilsTest {

    @Test
    public void testIfFilesConfigured(){
        assertTrue("No files configurations were found", ClinicalTrialsUtils.getInstance().isFilesConfigured());
    }

    @Test
    public void testIsLocalFilesExisted(){
        assertTrue("Local Files don't existed", ClinicalTrialsUtils.getInstance().isLocalFilesExisted());
    }

    @Test
    public void testLoadingMappingResult() {
        Map<String, TumorMap> result = new HashMap<>();
        try {
            result = ClinicalTrialsUtils.getInstance().loadMappingResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue("Loading mapping result failed", !result.isEmpty());
    }

    @Test
    public void testLoadingSites() {
        Map<String, Site> sites = new HashMap<>();
        try {
            sites = ClinicalTrialsUtils.getInstance().loadSitesMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue("Loading sites failed", !sites.isEmpty());
    }

    @Test
    public void testLoadingTrials() {
        Map<String, ClinicalTrialMap> trials = new HashMap<>();
        try {
            trials = ClinicalTrialsUtils.getInstance().loadTrialsMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue("Loading trials failed", !trials.isEmpty());
    }

    @Test
    public void testGetAllMappingResult() {
        Map<String, TumorMap> res = ClinicalTrialsUtils
            .getInstance()
            .getAllMappingResult();
        assertTrue("Getting mapping result failed", res.size() != 0);
    }

    @Test
    public void testGetAllSites() {
        Map<String, Site> sites = ClinicalTrialsUtils
            .getInstance()
            .getAllSites();
        assertTrue("Getting sites failed", sites.size() != 0);
    }

    @Test
    public void testGetAllTrials() {
        Map<String, ClinicalTrialMap> trials = ClinicalTrialsUtils
            .getInstance()
            .getAllTrials();
        assertTrue("Getting trials failed", trials.size() != 0);
    }

    @Test
    public void testFilterByCancerType() {
        List<ClinicalTrialMap> trials = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByCancerType("MEL");
        assertTrue("Filter by cancer type failed", trials.size() != 0);
    }

    @Test
    public void testFilterTrialsByTreatment() {
        List<ClinicalTrialMap> trials = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByCancerType("Melanoma");
        List<ClinicalTrialMap> res = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByTreatment(trials, "Dabrafenib");
        System.out.println(res.size());
        assertTrue("Filter by treatment failed", res.size() != 0);
    }

    @Test
    public void testFilterTrialsByDrugNameOrCode() {
        Set<String> drugs = new HashSet<>();
        drugs.add("Dabrafenib");
        List<ClinicalTrialMap> trials = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByCancerType("Melanoma");
        List<ClinicalTrialMap> res = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByDrugNameOrCode(trials, drugs);
        assertTrue("Filter by drug name Or code failed", res.size() != 0);
    }

    @Test
    public void testFilterTrialsByLocation() {
        List<ClinicalTrialMap> trials = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByCancerType("MEL");
        List<ClinicalTrialMap> res = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByLocation(
                trials,
                "toronto, Canada",
                100.0
            );
        assertTrue("Filter by location failed", res.size() != 0);       
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        try {
            FileWriter file = new FileWriter(
                "C:\\Users\\Yifu\\Desktop\\test.json"
            );
            file.write(gson.toJson(ClinicalTrialsUtils.getInstance().replaceKeysWithSites(res)));
            file.flush();
            file.close();
            System.out.println("Finished");
        } catch (IOException e) {}
    }

    @Test
    public void testReplaceKeysWithSites(){
        List<ClinicalTrialMap> trials = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByCancerType("Melanoma");
        trials =
            ClinicalTrialsUtils
                .getInstance()
                .filterTrialsByTreatmentAndLocation(
                    trials,
                    "Dabrafenib",
                    "St. Louis, MO, United States",
                    100.0
                );
        List<ClinicalTrial> res = ClinicalTrialsUtils.getInstance().replaceKeysWithSites(trials);
        assertTrue("Sites replace failed", res.size() != 0);
    }

    @Test
    public void testFilterTrialsByTreatmentAndLocation() {
        List<ClinicalTrialMap> trials = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByCancerType("Melanoma");
        trials =
            ClinicalTrialsUtils
                .getInstance()
                .filterTrialsByTreatmentAndLocation(
                    trials,
                    "Dabrafenib",
                    "St. Louis, MO, United States",
                    100.0
                );
        System.out.println(trials.size());
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        try {
            FileWriter file = new FileWriter(
                "C:\\Users\\Yifu\\Desktop\\test.json"
            );
            file.write(gson.toJson(trials));
            file.flush();
            file.close();
            System.out.println("Finished");
        } catch (IOException e) {}
    }
}
