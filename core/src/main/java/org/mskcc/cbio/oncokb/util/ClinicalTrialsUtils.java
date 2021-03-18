package org.mskcc.cbio.oncokb.util;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Arms;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Coordinates;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Site;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Tumor;

/**
 * Created by Yifu Yao on 3/9/2021
 */
public class ClinicalTrialsUtils {

    private static ClinicalTrialsUtils instance = null;

    public ClinicalTrialsUtils() {}

    public static ClinicalTrialsUtils getInstance() {
        if (instance == null) {
            instance = new ClinicalTrialsUtils();
        }
        return instance;
    }

    private static final String S3_DIR = "drug-matching/";
    private static final String S3_BUCKET = "oncokb";
    private static final String LOCAL_DIR = "/data/clinicalTrials/";
    private static final String MAPPING_RESULT_FILE = "result.json.gz";
    private static final String SITES_FILE = "sites.json.gz";
    private static final String TRIALS_FILE = "trials.json.gz";

    public Map<String, Tumor> loadMappingResult()
        throws UnsupportedEncodingException, IOException, ParseException {
        Map<String, Tumor> result = new HashMap<>();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (S3Utils.getInstance().isPropertiesConfigured()) {
            S3ObjectInputStream inputStream = S3Utils
                .getInstance()
                .getObject(S3_BUCKET, S3_DIR + MAPPING_RESULT_FILE)
                .get()
                .getObjectContent();
            GzipUtils.deCompress(inputStream, os);
        } else {
            GzipUtils.deCompress(
                ClinicalTrialsUtils.class.getResourceAsStream(
                        LOCAL_DIR + MAPPING_RESULT_FILE
                    ),
                os
            );
        }
        result =
            new Gson()
            .fromJson(
                    os.toString("UTF-8"),
                    new TypeToken<Map<String, Tumor>>() {}.getType()
                );
        os.close();
        return result;
    }

    public Map<String, Site> loadSitesMap()
        throws UnsupportedEncodingException, IOException, ParseException {
        Map<String, Site> sites = new HashMap<>();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (S3Utils.getInstance().isPropertiesConfigured()) {
            S3ObjectInputStream inputStream = S3Utils
                .getInstance()
                .getObject(S3_BUCKET, S3_DIR + SITES_FILE)
                .get()
                .getObjectContent();
            GzipUtils.deCompress(inputStream, os);
        } else {
            GzipUtils.deCompress(
                ClinicalTrialsUtils.class.getResourceAsStream(
                        LOCAL_DIR + SITES_FILE
                    ),
                os
            );
        }
        sites =
            new Gson()
            .fromJson(
                    os.toString("UTF-8"),
                    new TypeToken<HashMap<String, Site>>() {}.getType()
                );
        os.close();
        return sites;
    }

    public Map<String, ClinicalTrial> loadTrialsMap()
        throws UnsupportedEncodingException, IOException, ParseException {
        Map<String, ClinicalTrial> trials = new HashMap<>();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (S3Utils.getInstance().isPropertiesConfigured()) {
            S3ObjectInputStream inputStream = S3Utils
                .getInstance()
                .getObject(S3_BUCKET, S3_DIR + TRIALS_FILE)
                .get()
                .getObjectContent();
            GzipUtils.deCompress(inputStream, os);
        } else {
            GzipUtils.deCompress(
                ClinicalTrialsUtils.class.getResourceAsStream(
                        LOCAL_DIR + TRIALS_FILE
                    ),
                os
            );
        }
        trials =
            new Gson()
            .fromJson(
                    os.toString("UTF-8"),
                    new TypeToken<Map<String, ClinicalTrial>>() {}.getType()
                );
        os.close();
        return trials
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().getPreviousTrialStatus() == null)
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey(),
                    entry -> entry.getValue()
                )
            );
    }

    public Map<String, Tumor> getAllMappingResult() {
        return CacheUtils.getAllClinicalTrialsMappingResult();
    }

    public Map<String, Site> getAllSites() {
        return CacheUtils.getAllClinicalTrialsSites();
    }

    public Map<String, ClinicalTrial> getAllTrials() {
        return CacheUtils.getAllClinicalTrials();
    }

    public Tumor getTumor(String oncoTreeCode) {
        Map<String, Tumor> tumors = getAllMappingResult();
        Tumor tumor = new Tumor();
        if (tumors.containsKey(oncoTreeCode)) {
            tumor = tumors.get(oncoTreeCode);
        }
        return tumor;
    }

    public Site getSite(String siteKey) {
        Map<String, Site> sites = getAllSites();
        Site site = new Site();
        if (sites.containsKey(siteKey)) {
            site = sites.get(siteKey);
        }
        return site;
    }

    public ClinicalTrial getTrial(String nctID) {
        Map<String, ClinicalTrial> trials = getAllTrials();
        ClinicalTrial trial = new ClinicalTrial();
        if (trials.containsKey(nctID)) {
            trial = trials.get(nctID);
        }
        return trial;
    }

    public List<ClinicalTrial> filterTrialsByTreatment(
        List<ClinicalTrial> trials,
        String treatment
    ) {
        if (treatment != null && !treatment.isEmpty()) {
            List<ClinicalTrial> res = new ArrayList<>();
            Set<String> drugs = Arrays
                .stream(treatment.split("\\+"))
                .map(item -> item.trim().toLowerCase())
                .collect(Collectors.toSet());
            res = filterTrialsByDrugNameOrCode(trials, drugs);
            return res;
        }
        return trials;
    }

    public List<ClinicalTrial> filterTrialsByDrugNameOrCode(
        List<ClinicalTrial> trials,
        Set<String> drugs
    ) {
        if (!drugs.isEmpty()) {
            List<ClinicalTrial> res = new ArrayList<>();
            drugs =
                drugs
                    .stream()
                    .map(drug -> drug.toLowerCase())
                    .collect(Collectors.toSet());
            for (ClinicalTrial trial : trials) {
                for (Arms arm : trial.getArms()) {
                    if (
                        Stream
                            .concat(
                                arm
                                    .getDrugs()
                                    .stream()
                                    .map(
                                        drug -> drug.getNcitCode().toLowerCase()
                                    ),
                                arm
                                    .getDrugs()
                                    .stream()
                                    .map(
                                        drug -> drug.getDrugName().toLowerCase()
                                    )
                            )
                            .collect(Collectors.toSet())
                            .containsAll(drugs)
                    ) {
                        res.add(trial);
                        break;
                    }
                }
            }
            return res;
        }
        return trials;
    }

    public List<ClinicalTrial> filterTrialsBySpecialCancerType(
        SpecialTumorType specialTumorType
    ) {
        List<ClinicalTrial> trials = new ArrayList<>();
        if (specialTumorType == null) return trials;

        TumorType matchedSpecialTumorType = TumorTypeUtils.getBySpecialTumor(
            specialTumorType
        );
        if (matchedSpecialTumorType == null) return trials;

        switch (specialTumorType) {
            case ALL_TUMORS:
                return getAllTrials()
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            case ALL_SOLID_TUMORS:
            case ALL_LIQUID_TUMORS:
                return TumorTypeUtils
                    .getAllTumorTypes()
                    .stream()
                    .filter(
                        tumorType ->
                            tumorType.getTumorForm() != null &&
                            tumorType
                                .getTumorForm()
                                .equals(matchedSpecialTumorType.getTumorForm())
                    )
                    .map(
                        tumorType ->
                            filterTrialsByCancerType(
                                StringUtils.isNotEmpty(tumorType.getSubtype())
                                    ? tumorType.getSubtype()
                                    : tumorType.getMainType()
                            )
                    )
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            default:
                return trials;
        }
    }

    public List<ClinicalTrial> filterTrialsByCancerType(String cancerType) {
        Map<String, Tumor> tumors = getAllMappingResult();
        List<ClinicalTrial> trials = new ArrayList<>();

        Set<String> tumorCodesByMainType = new HashSet<>();
        List<TumorType> allOncoTreeSubtypes = TumorTypeUtils.getAllSubtypes();
        for (TumorType oncoTreeType : allOncoTreeSubtypes) {
            if (
                (
                    oncoTreeType.getMainType() != null &&
                    cancerType.equalsIgnoreCase(oncoTreeType.getMainType())
                ) ||
                (
                    oncoTreeType.getCode() != null &&
                    cancerType.equalsIgnoreCase(oncoTreeType.getCode())
                )
            ) {
                tumorCodesByMainType.add(oncoTreeType.getCode());
            }
        }
        if (tumorCodesByMainType.size() > 0) {
            for (String code : tumorCodesByMainType) {
                if (tumors.containsKey(code)) {
                    trials.addAll(
                        getTrialsByIDList(tumors.get(code).getTrials())
                    );
                }
            }
        } else {
            TumorType matchedSubtype = TumorTypeUtils.getBySubtype(cancerType);
            if (matchedSubtype != null) {
                String codeByName = matchedSubtype.getCode();
                if (tumors.containsKey(codeByName)) {
                    trials.addAll(
                        getTrialsByIDList(tumors.get(codeByName).getTrials())
                    );
                }
            } else { // cancertype is oncotree code
                if (tumors.containsKey(cancerType)) {
                    trials.addAll(
                        getTrialsByIDList(tumors.get(cancerType).getTrials())
                    );
                }
            }
        }

        return trials;
    }

    public List<ClinicalTrial> getTrialsByIDList(List<String> ids) {
        return ids
            .stream()
            .map(id -> getAllTrials().get(id))
            .collect(Collectors.toList());
    }

    public List<ClinicalTrial> filterTrialsByLocation(
        List<ClinicalTrial> trials,
        String location,
        Double distance
    ) {
        if (location != null) {
            if (distance == null) distance = 100.0;
            List<ClinicalTrial> res = new ArrayList<>();
            Coordinates ori = OpenStreetMapUtils
                .getInstance()
                .getCoordinates(location);
            for (ClinicalTrial trial : trials) {
                for (String siteKey : trial.getSites()) {
                    Site site = getSite(siteKey);
                    Coordinates des = site.getOrg().getCoordinates();
                    if (des == null) {
                        if (
                            site.getOrg().getCity() != null &&
                            site.getOrg().getState() != null &&
                            site.getOrg().getCountry() != null
                        ) {
                            String address = String.format(
                                "%s, %s, %s",
                                site.getOrg().getCity(),
                                site.getOrg().getState(),
                                site.getOrg().getCountry()
                            );
                            des =
                                OpenStreetMapUtils
                                    .getInstance()
                                    .getCoordinates(address);
                        } else continue;
                    }
                    if (
                        OpenStreetMapUtils
                            .getInstance()
                            .calculateDistance(ori, des) <=
                        distance
                    ) {
                        res.add(trial);
                        break;
                    }
                }
            }
            return res;
        }
        return trials;
    }

    public List<ClinicalTrial> filterTrialsBytreatmentAndLocation(
        List<ClinicalTrial> trials,
        String treatment,
        String location,
        Double distance
    ) {
        trials = filterTrialsByTreatment(trials, treatment);
        trials = filterTrialsByLocation(trials, location, distance);
        return trials;
    }

    public List<ClinicalTrial> filterTrialsByTreatmentForIndicatorQueryTreatment(
        String cancerType,
        Set<String> drugs
    ) {
        List<ClinicalTrial> trials = filterTrialsByCancerType(cancerType);
        List<ClinicalTrial> res = filterTrialsByDrugNameOrCode(trials, drugs);
        return res;
    }
}
