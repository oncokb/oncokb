package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.*;

import java.util.*;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class MainUtils {
    static String DataVersion = null;
    static String DataVersionDate = null;

    public static Map<String, Object> GetRequestQueries(
        String entrezGeneId, String hugoSymbol, String alteration, String tumorType,
        String evidenceType, String consequence, String proteinStart, String proteinEnd,
        String geneStatus, String source, String levels) {

        Map<String, Object> requestQueries = new HashMap<>();

        List<Query> queries = new ArrayList<>();
        List<EvidenceType> evidenceTypes = new ArrayList<>();
        List<LevelOfEvidence> levelOfEvidences = new ArrayList<>();
        String[] genes = {};

        if (entrezGeneId != null) {
            for (String id : entrezGeneId.trim().split("\\s*,\\s*")) {
                Query requestQuery = new Query();
                requestQuery.setEntrezGeneId(Integer.parseInt(id));
                queries.add(requestQuery);
            }
        } else if (hugoSymbol != null) {
            for (String symbol : hugoSymbol.trim().split("\\s*,\\s*")) {
                Query requestQuery = new Query();
                requestQuery.setHugoSymbol(symbol.toUpperCase());
                queries.add(requestQuery);
            }
        }

        if (evidenceType != null) {
            for (String type : evidenceType.trim().split("\\s*,\\s*")) {
                EvidenceType et = EvidenceType.valueOf(type);
                evidenceTypes.add(et);
            }
        } else {
            evidenceTypes = getAllEvidenceTypes();
        }

        if (alteration != null) {
            String[] alts = alteration.trim().split("\\s*,\\s*");
            if (queries.size() == alts.length) {
                String[] consequences = consequence == null ? new String[0] : consequence.trim().split("\\s*,\\s*");
                String[] proteinStarts = proteinStart == null ? new String[0] : proteinStart.trim().split("\\s*,\\s*");
                String[] proteinEnds = proteinEnd == null ? new String[0] : proteinEnd.trim().split("\\s*,\\s*");

                for (int i = 0; i < queries.size(); i++) {
                    queries.get(i).setAlteration(alts[i]);
                    queries.get(i).setConsequence(consequences.length == alts.length ? consequences[i] : null);
                    queries.get(i).setProteinStart(proteinStarts.length == alts.length ? Integer.valueOf(proteinStarts[i]) : null);
                    queries.get(i).setProteinEnd(proteinEnds.length == alts.length ? Integer.valueOf(proteinEnds[i]) : null);
                }
            } else {
                return null;
            }
        }

        String[] tumorTypes = tumorType == null ? new String[0] : tumorType.trim().split("\\s*,\\s*");
        if (tumorTypes.length > 0) {
            if (tumorTypes.length == 1) {
                for (int i = 0; i < queries.size(); i++) {
                    queries.get(i).setTumorType(tumorTypes[0]);
                }
            } else if (queries.size() == tumorTypes.length) {
                for (int i = 0; i < queries.size(); i++) {
                    queries.get(i).setTumorType(tumorTypes[i]);
                }
            }
        }

        if (levels != null) {
            String[] levelStrs = levels.trim().split("\\s*,\\s*");
            for (int i = 0; i < levelStrs.length; i++) {
                LevelOfEvidence level = LevelOfEvidence.getByName(levelStrs[i]);
                if (level != null) {
                    levelOfEvidences.add(level);
                }
            }
        } else {
            levelOfEvidences = new ArrayList<>(LevelUtils.getPublicLevels());
        }

        requestQueries.put("queries", queries);
        requestQueries.put("evidenceTypes", evidenceTypes);
        requestQueries.put("source", source == null ? "quest" : source);
        requestQueries.put("geneStatus", geneStatus == null ? "complete" : geneStatus);
        requestQueries.put("levels", levelOfEvidences);
        return requestQueries;
    }

    public static Long printTimeDiff(Long oldDate, Long newDate, String message) {
        System.out.println(message + ": " + (newDate - oldDate));
        return newDate;
    }

    public static String findHighestMutationEffect(Set<String> mutationEffect) {
        String[] effects = {"Gain-of-function", "Likely Gain-of-function", "Unknown", "Likely Neutral", "Neutral", "Likely Switch-of-function", "Switch-of-function", "Likely Loss-of-function", "Loss-of-function"};
        List<String> list = Arrays.asList(effects);
        Integer index = 100;
        for (String effect : mutationEffect) {
            if (list.indexOf(effect) < index) {
                index = list.indexOf(effect);
            }
        }
        return index == 100 ? "" : list.get(index);
    }

    public static Oncogenicity findHighestOncogenic(Set<Oncogenicity> oncogenic) {
        String[] effects = {"-1", "0", "2", "1"};
        List<String> list = Arrays.asList(effects);
        String level = "";
        Integer index = -2;

        for (Oncogenicity datum : oncogenic) {
            if (datum != null) {
                Integer oncogenicIndex = list.indexOf(datum.getOncogenic());
                if (index < oncogenicIndex) {
                    index = oncogenicIndex;
                }
            }
        }

        return index == -2 ? null : Oncogenicity.getByLevel(list.get(index));
    }

    public static String idealOncogenicityByMutationEffect(String mutationEffect) {
        if (mutationEffect == null) {
            return "";
        }

        mutationEffect = mutationEffect.toLowerCase();
        String oncogenic;

        switch (mutationEffect) {
            case "gain-of-function":
                oncogenic = "Oncogenic";
                break;
            case "likely gain-of-function":
                oncogenic = "Likely Oncogenic";
                break;
            case "loss-of-function":
                oncogenic = "Oncogenic";
                break;
            case "likely loss-of-function":
                oncogenic = "Likely Oncogenic";
                break;
            case "switch-of-function":
                oncogenic = "Oncogenic";
                break;
            case "likely switch-of-function":
                oncogenic = "Likely Oncogenic";
                break;
            case "neutral":
                oncogenic = "Likely Neutral";
                break;
            case "likely neutral":
                oncogenic = "Likely Neutral";
                break;
            case "unknown":
                oncogenic = "Unknown";
                break;
            default:
                oncogenic = "";
        }
        return oncogenic;
    }

    public static Map<String, String> matchOncogenicMutation(String mutationEffect, String oncogenic) {
        Map<String, String> match = new HashMap<>();

        mutationEffect = mutationEffect == null ? "" : mutationEffect;
        oncogenic = oncogenic == null ? "" : oncogenic;

        if (!hasInfoForEffect(mutationEffect) && hasInfoForEffect(oncogenic)) {
            //TODO: how to handle this situation
        } else if (!hasInfoForEffect(oncogenic) && hasInfoForEffect(mutationEffect)) {
            oncogenic = idealOncogenicityByMutationEffect(mutationEffect);
        }

        match.put("oncogenic", oncogenic);
        match.put("mutationEffect", mutationEffect);

        return match;
    }

    public static Set<EvidenceType> getTreatmentEvidenceTypes() {
        Set<EvidenceType> types = new HashSet<>();
        types.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
        types.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE);
        types.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE);
        types.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);

        return types;
    }

    public static List<EvidenceType> getAllEvidenceTypes() {
        return Arrays.asList(EvidenceType.values());
    }

    public static Set<EvidenceType> getSensitiveTreatmentEvidenceTypes() {
        Set<EvidenceType> types = new HashSet<>();
        types.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
        types.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
        return types;
    }

    public static Oncogenicity findHighestOncogenicByEvidences(Set<Evidence> evidences) {
        List<String> levels = Arrays.asList("-1", "0", "2", "1");

        int index = -1;

        if (evidences != null) {
            for (Evidence evidence : evidences) {
                if (evidence.getKnownEffect() != null) {
                    int _index = -1;
                    _index = levels.indexOf(evidence.getKnownEffect());
                    if (_index > index) {
                        index = _index;
                    }
                }
            }
        }

        return index > -1 ? Oncogenicity.getByLevel(levels.get(index)) : null;
    }

    public static Oncogenicity setToAlleleOncogenicity(Oncogenicity oncogenicity) {
        Set<Oncogenicity> eligibleList = new HashSet<>();
        eligibleList.add(Oncogenicity.getByLevel("1"));
        eligibleList.add(Oncogenicity.getByLevel("2"));

        if (oncogenicity == null) {
            return null;
        }

        if (eligibleList.contains(oncogenicity)) {
            return Oncogenicity.getByLevel("2");
        }

        return null;
    }

    public static String getAlleleConflictsMutationEffect(Set<String> mutationEffects) {
        Set<String> clean = new HashSet<>();

        for (String mutationEffect : mutationEffects) {
            if (mutationEffect != null) {
                mutationEffect = mutationEffect.replaceAll("(?i)likely", "");
                mutationEffect = mutationEffect.replaceAll("\\s", "");
                clean.add(mutationEffect);
            }
        }

        if (clean.size() > 1) {
            return "Unknown";
        } else if (clean.size() == 1) {
            return "Likely " + clean.iterator().next();
        } else {
            return "";
        }
    }


    public static Long getCurrentTimestamp() {
        return new Date().getTime();
    }

    public static Long getTimestampDiff(Long old) {
        return new Date().getTime() - old;
    }

    public static String getDataVersion() {
        if (DataVersion == null) {
            DataVersion = getProperty("data.version");
        }
        return DataVersion;
    }

    public static String getDataVersionDate() {
        if (DataVersionDate == null) {
            DataVersionDate = getProperty("data.version_date");
        }
        return DataVersionDate;
    }

    private static String getProperty(String propertyName) {
        String version = "";
        if (propertyName != null) {
            try {
                String tmpData = PropertiesUtils.getProperties(propertyName);
                if (tmpData != null) {
                    version = tmpData;
                }
            } catch (Exception e) {
            }
        }
        return version;
    }

    private static Boolean hasInfoForEffect(String effect) {
        if (effect == null) {
            return false;
        }

        if (effect.equalsIgnoreCase("unknown") || effect.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public static String listToString(List<String> list, String separator) {
        if (list.isEmpty()) {
            return "";
        }

        int n = list.size();
        StringBuilder sb = new StringBuilder();
        sb.append(list.get(0));
        if (n == 1) {
            return sb.toString();
        }

        for (int i = 1; i < n; i++) {
            sb.append(separator).append(list.get(i));
        }

        return sb.toString();
    }

    public static List<Integer> stringToIntegers(String ids) {
        if (ids == null) {
            return null;
        }
        List<Integer> result = new ArrayList<>();
        for (String id : ids.trim().split("\\s*,\\s*")) {
            Integer match = Integer.parseInt(id);

            if (match != null) {
                result.add(match);
            }
        }
        return result;
    }

    public static List<EvidenceType> stringToEvidenceTypes(String string, String separator) {
        List<EvidenceType> evidenceTypes = new ArrayList<>();
        if (string != null) {
            if (separator == null) {
                separator = ",";
            }
            for (String type : string.trim().split("\\s*" + separator + "\\s*")) {
                EvidenceType et = EvidenceType.valueOf(type);
                evidenceTypes.add(et);
            }
        } else {
            return null;
        }
        return evidenceTypes;
    }

    public static Set<BiologicalVariant> getBiologicalVariants(Gene gene) {
        Set<BiologicalVariant> variants = new HashSet<>();
        if (gene != null) {
            Long oldTime = new Date().getTime();
            Set<Alteration> alterations = new HashSet<>(AlterationUtils.getAllAlterations(gene));

            alterations = AlterationUtils.excludeVUS(gene, alterations);
            alterations = AlterationUtils.excludeGeneralAlterations(alterations);

//                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Get all alterations for " + hugoSymbol);

            Set<EvidenceType> evidenceTypes = new HashSet<EvidenceType>() {{
                add(EvidenceType.MUTATION_EFFECT);
                add(EvidenceType.ONCOGENIC);
            }};
            Map<Alteration, Map<EvidenceType, Set<Evidence>>> evidences = new HashMap<>();

            for (Alteration alteration : alterations) {
                Map<EvidenceType, Set<Evidence>> map = new HashMap<>();
                map.put(EvidenceType.ONCOGENIC, new HashSet<Evidence>());
                map.put(EvidenceType.MUTATION_EFFECT, new HashSet<Evidence>());
                evidences.put(alteration, map);
            }
//                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Initialize evidences.");

            Map<Gene, Set<Evidence>> geneEvidences =
                EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(Collections.singleton(gene), evidenceTypes);
//                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Get all gene evidences.");

            for (Evidence evidence : geneEvidences.get(gene)) {
                for (Alteration alteration : evidence.getAlterations()) {
                    if (evidences.containsKey(alteration)) {
                        evidences.get(alteration).get(evidence.getEvidenceType()).add(evidence);
                    }
                }
            }
//                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Seperate evidences.");

            for (Map.Entry<Alteration, Map<EvidenceType, Set<Evidence>>> entry : evidences.entrySet()) {
                Alteration alteration = entry.getKey();
                Map<EvidenceType, Set<Evidence>> map = entry.getValue();

                BiologicalVariant variant = new BiologicalVariant();
                variant.setVariant(alteration);
                Oncogenicity oncogenicity = Oncogenicity.getByLevel(EvidenceUtils.getKnownEffectFromEvidence(EvidenceType.ONCOGENIC, map.get(EvidenceType.ONCOGENIC)));

                Map<String, String> properMapping = MainUtils.matchOncogenicMutation(EvidenceUtils.getKnownEffectFromEvidence(EvidenceType.MUTATION_EFFECT, map.get(EvidenceType.MUTATION_EFFECT)), oncogenicity == null ? null : oncogenicity.getDescription());
                variant.setOncogenic(properMapping.get("oncogenic"));
                variant.setMutationEffect(properMapping.get("mutationEffect"));
                variant.setOncogenicPmids(EvidenceUtils.getPmids(map.get(EvidenceType.ONCOGENIC)));
                variant.setMutationEffectPmids(EvidenceUtils.getPmids(map.get(EvidenceType.MUTATION_EFFECT)));
                variants.add(variant);
            }
//                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Created biological annotations.");
        }
        return variants;
    }

    public static Set<ClinicalVariant> getClinicalVariants(Gene gene) {
        Set<ClinicalVariant> variants = new HashSet<>();
        if (gene != null) {
            Set<Alteration> alterations = new HashSet<>(AlterationUtils.getAllAlterations(gene));
            alterations = AlterationUtils.excludeVUS(gene, alterations);
            Set<EvidenceType> evidenceTypes = new HashSet<EvidenceType>() {{
                add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
                add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE);
                add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
            }};
            Map<Alteration, Map<OncoTreeType, Map<LevelOfEvidence, Set<Evidence>>>> evidences = new HashMap<>();
            Set<LevelOfEvidence> publicLevels = LevelUtils.getPublicLevels();

            for (Alteration alteration : alterations) {
                evidences.put(alteration, new HashMap<OncoTreeType, Map<LevelOfEvidence, Set<Evidence>>>());
            }

            Map<Gene, Set<Evidence>> geneEvidences =
                EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(Collections.singleton(gene), evidenceTypes);

            for (Evidence evidence : geneEvidences.get(gene)) {
                OncoTreeType oncoTreeType = evidence.getOncoTreeType();

                if (oncoTreeType != null) {
                    for (Alteration alteration : evidence.getAlterations()) {
                        if (evidences.containsKey(alteration)) {
                            if (!evidences.get(alteration).containsKey(oncoTreeType)) {
                                evidences.get(alteration).put(oncoTreeType, new HashMap<LevelOfEvidence, Set<Evidence>>());
                            }
                            if (publicLevels.contains(evidence.getLevelOfEvidence())) {
                                LevelOfEvidence levelOfEvidence = evidence.getLevelOfEvidence();
                                if (!evidences.get(alteration).get(oncoTreeType).containsKey(levelOfEvidence)) {
                                    evidences.get(alteration).get(oncoTreeType).put(levelOfEvidence, new HashSet<Evidence>());
                                }
                                evidences.get(alteration).get(oncoTreeType).get(levelOfEvidence).add(evidence);
                            }
                        }
                    }
                }
            }

            for (Map.Entry<Alteration, Map<OncoTreeType, Map<LevelOfEvidence, Set<Evidence>>>> entry : evidences.entrySet()) {
                Alteration alteration = entry.getKey();
                Map<OncoTreeType, Map<LevelOfEvidence, Set<Evidence>>> map = entry.getValue();

                for (Map.Entry<OncoTreeType, Map<LevelOfEvidence, Set<Evidence>>> _entry : map.entrySet()) {
                    OncoTreeType oncoTreeType = _entry.getKey();

                    for (Map.Entry<LevelOfEvidence, Set<Evidence>> __entry : _entry.getValue().entrySet()) {
                        ClinicalVariant variant = new ClinicalVariant();
                        variant.setOncoTreeType(oncoTreeType);
                        variant.setVariant(alteration);
                        variant.setLevel(__entry.getKey().getLevel());
                        variant.setDrug(EvidenceUtils.getDrugs(__entry.getValue()));
                        variant.setDrugPmids(EvidenceUtils.getPmids(__entry.getValue()));
                        variants.add(variant);
                    }
                }
            }
        }
        return variants;
    }
}
