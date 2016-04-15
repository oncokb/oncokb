package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

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
            }else {
                return null;
            }
        }

        if(levels != null) {
            String[] levelStrs = levels.split(",");
            for (int i = 0; i < levelStrs.length; i++) {
                LevelOfEvidence level = LevelOfEvidence.getByName(levelStrs[i]);
                if(level != null) {
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
}
