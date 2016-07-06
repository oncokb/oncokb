package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.Oncogenicity;
import org.mskcc.cbio.oncokb.model.Query;

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
        } else {
            return null;
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

    public static String findHighestOncogenic(Set<Oncogenicity> oncogenic) {
        String level = "";
        Integer index = -2;

        for (Oncogenicity datum : oncogenic) {
            if (datum != null) {
                Integer oncogenicIndex = Integer.parseInt(datum.getOncogenic());
                if (index < oncogenicIndex) {
                    index = oncogenicIndex;
                }
            }
        }

        return index == -2 ? "" : Oncogenicity.getByLevel(index.toString()).getDescription();
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
