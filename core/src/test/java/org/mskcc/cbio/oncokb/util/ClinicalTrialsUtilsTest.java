package org.mskcc.cbio.oncokb.util;

import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
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
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Site;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Tumor;

public class ClinicalTrialsUtilsTest {

    @Test
    public void testLoadingMappingResult() {
        Map<String, Tumor> result = new HashMap<>();
        try {
            result = ClinicalTrialsUtils.getInstance().loadMappingResult();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        assertTrue("Loading mapping result failed", !result.isEmpty());
    }

    @Test
    public void testLoadingSites() {
        Map<String, Site> sites = new HashMap<>();
        try {
            sites = ClinicalTrialsUtils.getInstance().loadSitesMap();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        assertTrue("Loading sites failed", !sites.isEmpty());
    }

    @Test
    public void testLoadingTrials() {
        Map<String, ClinicalTrial> trials = new HashMap<>();
        try {
            trials = ClinicalTrialsUtils.getInstance().loadTrialsMap();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        assertTrue("Loading sites failed", !trials.isEmpty());
    }

    @Test
    public void testGetAllMappingResult() {
        Map<String, Tumor> res = ClinicalTrialsUtils
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
        Map<String, ClinicalTrial> trials = ClinicalTrialsUtils
            .getInstance()
            .getAllTrials();
        assertTrue("Getting trials failed", trials.size() != 0);
    }

    @Test
    public void testFilterByCancerType() {
        List<ClinicalTrial> trials = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByCancerType("Melanoma");
        assertTrue("Filter by cancer type failed", trials.size() != 0);
    }

    @Test
    public void testFilterTrialsByTreatment() {
        List<ClinicalTrial> trials = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByCancerType("Melanoma");
        List<ClinicalTrial> res = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByTreatment(trials, "Dabrafenib");
        System.out.println(res.size());
        assertTrue("Filter by treatment failed", res.size() != 0);
    }

    @Test
    public void testFilterTrialsByDrugNameOrCode() {
        Set<String> drugs = new HashSet<>();
        drugs.add("Dabrafenib");
        List<ClinicalTrial> trials = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByCancerType("Melanoma");
        List<ClinicalTrial> res = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByDrugNameOrCode(trials, drugs);
        assertTrue("Filter by drug name Or code failed", res.size() != 0);
    }

    @Test
    public void testFilterTrialsByLocation() {
        List<ClinicalTrial> trials = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByCancerType("Melanoma");
        List<ClinicalTrial> res = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByLocation(
                trials,
                "Columbia, MO, United States",
                100.0
            );
        assertTrue("Filter by location failed", res.size() != 0);
    }

    @Test
    public void testFilterTrialsByTreatmentAndLocation() {
        Gson gson = new Gson();
        List<ClinicalTrial> trials = ClinicalTrialsUtils
            .getInstance()
            .filterTrialsByCancerType("Melanoma");
        trials =
            ClinicalTrialsUtils
                .getInstance()
                .filterTrialsBytreatmentAndLocation(
                    trials,
                    "Dabrafenib",
                    "St. Louis, MO, United States",
                    100.0
                );
        System.out.println(trials.size());
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
