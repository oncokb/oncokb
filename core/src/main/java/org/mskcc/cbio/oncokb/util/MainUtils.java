package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.*;

import java.util.*;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class MainUtils {
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
            for (String id : entrezGeneId.split(",")) {
                Query requestQuery = new Query();
                requestQuery.setEntrezGeneId(Integer.parseInt(id));
                queries.add(requestQuery);
            }
        } else if (hugoSymbol != null) {
            for (String symbol : hugoSymbol.split(",")) {
                Query requestQuery = new Query();
                requestQuery.setHugoSymbol(symbol);
                queries.add(requestQuery);
            }
        }

        if (evidenceType != null) {
            for (String type : evidenceType.split(",")) {
                EvidenceType et = EvidenceType.valueOf(type);
                evidenceTypes.add(et);
            }
        }

        if (alteration != null) {
            String[] alts = alteration.split(",");
            if (queries.size() == alts.length) {
                String[] consequences = consequence == null ? new String[0] : consequence.split(",");
                String[] proteinStarts = proteinStart == null ? new String[0] : proteinStart.split(",");
                String[] proteinEnds = proteinEnd == null ? new String[0] : proteinEnd.split(",");

                for (int i = 0; i < queries.size(); i++) {
                    queries.get(i).setTumorType(tumorType);
                    queries.get(i).setAlteration(alts[i]);
                    queries.get(i).setConsequence(consequences.length == alts.length ? consequences[i] : null);
                    queries.get(i).setProteinStart(proteinStarts.length == alts.length ? Integer.valueOf(proteinStarts[i]) : null);
                    queries.get(i).setProteinEnd(proteinEnds.length == alts.length ? Integer.valueOf(proteinEnds[i]) : null);
                }
            } else {
                return null;
            }
        }

        if (levels != null) {
            String[] levelStrs = levels.split(",");
            for (int i = 0; i < levelStrs.length; i++) {
                LevelOfEvidence level = LevelOfEvidence.getByName(levelStrs[i]);
                if (level != null) {
                    levelOfEvidences.add(level);
                }
            }
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
}
