/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jgao
 */
@Controller
@RequestMapping(value = "/legacy-api/evidence.json")
public class EvidenceController {
    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    List<List<Evidence>> getEvidence(
            HttpMethod method,
            @RequestParam(value = "entrezGeneId", required = false) String entrezGeneId,
            @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol,
            @RequestParam(value = "alteration", required = false) String alteration,
            @RequestParam(value = "tumorType", required = false) String tumorType,
            @RequestParam(value = "evidenceType", required = false) String evidenceType,
            @RequestParam(value = "consequence", required = false) String consequence,
            @RequestParam(value = "proteinStart", required = false) String proteinStart,
            @RequestParam(value = "proteinEnd", required = false) String proteinEnd,
            @RequestParam(value = "geneStatus", required = false) String geneStatus,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "levels", required = false) String levels) {

        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<List<Evidence>> evidences = new ArrayList<>();

        if (entrezGeneId == null && hugoSymbol == null) {
            evidences.add(new ArrayList<>(evidenceBo.findAll()));
            return evidences;
        }

        Map<String, Object> requestQueries = MainUtils.GetRequestQueries(entrezGeneId, hugoSymbol, alteration,
                tumorType, evidenceType, consequence, proteinStart, proteinEnd, geneStatus, source, levels);

        List<EvidenceQueryRes> evidenceQueries = new ArrayList<>();

        String[] genes = {};

        if (requestQueries == null) {
            return new ArrayList<>();
        }

        evidenceQueries = processRequest(
                (List<Query>) requestQueries.get("queries"),
                (List<EvidenceType>) requestQueries.get("evidenceTypes"),
                geneStatus, source, (List<LevelOfEvidence>) requestQueries.get("levels"));

        for (EvidenceQueryRes query : evidenceQueries) {
            evidences.add(query.getEvidences());
        }

        return evidences;
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    List<EvidenceQueryRes> getEvidence(
            @RequestBody EvidenceQueries body) {

        List<EvidenceQueryRes> result = new ArrayList<>();
        if (body.getQueries().size() > 0) {
            List<Query> requestQueries = body.getQueries();
            List<EvidenceType> evidenceTypes = new ArrayList<>();

            if (body.getEvidenceTypes() != null) {
                for (String type : body.getEvidenceTypes().split(",")) {
                    EvidenceType et = EvidenceType.valueOf(type);
                    evidenceTypes.add(et);
                }
            } else {
                evidenceTypes.add(EvidenceType.GENE_SUMMARY);
                evidenceTypes.add(EvidenceType.GENE_BACKGROUND);
            }

            result = processRequest(requestQueries, evidenceTypes, body.getGeneStatus(), body.getSource(), body.getLevels());
        }

        return result;
    }

    private List<EvidenceQueryRes> processRequest(List<Query> requestQueries, List<EvidenceType> evidenceTypes, String geneStatus, String source, List<LevelOfEvidence> levelOfEvidences) {
        List<EvidenceQueryRes> evidenceQueries = new ArrayList<>();

        if (source == null) {
            source = "quest";
        }

        for (Query requestQuery : requestQueries) {
            EvidenceQueryRes query = new EvidenceQueryRes();

            query.setQuery(requestQuery);
            query.setGene(getGene(requestQuery.getEntrezGeneId(), requestQuery.getHugoSymbol()));

            if (query.getGene() != null) {
                if (requestQuery.getAlteration() != null) {
                    query.setAlterations(AlterationUtils.getRelevantAlterations(query.getGene(), requestQuery.getAlteration(), requestQuery.getConsequence(), requestQuery.getProteinStart(), requestQuery.getProteinEnd()));
                }

                query.setTumorTypes(TumorTypeUtils.getTumorTypes(requestQuery.getTumorType(), source));

                evidenceQueries.add(query);
            }
        }

        return assignEvidence(EvidenceUtils.getEvidence(evidenceQueries, evidenceTypes, geneStatus, levelOfEvidences), evidenceQueries);
    }

    private Gene getGene(Integer entrezGeneId, String hugoSymbol) {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Gene gene = null;

        if (entrezGeneId != null) {
            gene = geneBo.findGeneByEntrezGeneId(entrezGeneId);
        } else if (hugoSymbol != null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
        }

        return gene;
    }


    private List<EvidenceQueryRes> assignEvidence(List<Evidence> evidences, List<EvidenceQueryRes> evidenceQueries) {
        for (EvidenceQueryRes query : evidenceQueries) {
            query.setEvidences(EvidenceUtils.filterEvidence(evidences, query));
        }
        return evidenceQueries;
    }
}