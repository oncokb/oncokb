/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author jgao
 */
@Controller
public class EvidenceController {
    
    @RequestMapping(value="/evidence.json")
    public @ResponseBody List<Evidence> getEvidence(
            @RequestParam(value="entrezGeneId", required=false) Integer entrezGeneId,
            @RequestParam(value="hugoSymbol", required=false) String hugoSymbol,
            @RequestParam(value="alteration", required=false) String alt,
            @RequestParam(value="type", required=false) String evidenceType) {
        
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        String requestparams = "";
        
        Gene gene = null; 
        if (entrezGeneId!=null) {
            gene = geneBo.findGeneByEntrezGeneId(entrezGeneId);
            requestparams += entrezGeneId;
        } else if (hugoSymbol!=null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
            requestparams += hugoSymbol;
        }
        
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        List<Alteration> alteration = new ArrayList<Alteration>();
        
        if(alt != null) {
            requestparams += alt;
        }
        if (alt != null && gene != null) {
            Alteration alterationDatum = alterationBo.findAlteration(gene, AlterationType.MUTATION, alt);
            if(alterationDatum != null) {
                alteration.add(alterationDatum);
            }
        }
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        
        EvidenceType type = null;
        if(evidenceType != null) {
            EvidenceType[] types = EvidenceType.values();
            for (EvidenceType type1 : types) {
                if (evidenceType.toLowerCase().equals(type1.label().toLowerCase())) {
                    type = type1;
                    break;
                }
            }
        }
        
        if (gene != null && !alteration.isEmpty()) {
            if(type != null) {
                return evidenceBo.findEvidencesByAlteration(alteration, type);
            }else {
                return evidenceBo.findEvidencesByAlteration(alteration);     
            }       
        }else if(gene != null && alteration.isEmpty()) {
            if(type != null) {
                return evidenceBo.findEvidencesByGene(gene, type);
            }else {
                return evidenceBo.findEvidencesByGene(gene);
            }
        }else {
            if(requestparams.equals("")) {
                return evidenceBo.findAll();
            }else {
                return Collections.emptyList();
            }
        }
    }
}
