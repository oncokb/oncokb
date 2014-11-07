/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Evidence;
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
            @RequestParam(value="entrezGeneId", required=false) String entrezGeneId,
            @RequestParam(value="hugoSymbol", required=false) String hugoSymbol,
            @RequestParam(value="alteration", required=false) String alt) {
        
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        String requestparams = "";
        
        List<String> entrezGeneIds = new ArrayList();
        List<String> hugoSymbols = new ArrayList();
        
        if(entrezGeneId != null){
            entrezGeneIds= Arrays.asList(entrezGeneId.split(","));
            requestparams += entrezGeneId;
        }else if(hugoSymbol != null){
            hugoSymbols= Arrays.asList(hugoSymbol.split(","));
            requestparams += hugoSymbol;
        }
        
        List<String> alts = new ArrayList();
        if(alt != null){
            requestparams += alt;
            alts = Arrays.asList(alt.split(","));
        }
        
        List<Evidence> evidence = new ArrayList();
        
        if(requestparams.equals("")) {
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            evidence = evidenceBo.findAll();
        }else {
            if(!alts.isEmpty()) {
                if(!entrezGeneIds.isEmpty() && hugoSymbols.size() == alts.size()) {
                    for(String entrezGeneIdDataum : entrezGeneIds) {
                        evidence = mergeEvidence(evidence, singleQuery(Integer.parseInt(entrezGeneIdDataum), null, alt));
                    }
                }else if(!hugoSymbols.isEmpty() && hugoSymbols.size() == alts.size()){
                    for(String hugoSymbolDatum : hugoSymbols) {
                        evidence = mergeEvidence(evidence, singleQuery(null, hugoSymbolDatum, alt));
                    }
                }else {
                    evidence = Collections.emptyList();
                }
            }else {
                evidence = Collections.emptyList();
            }
        }
        
        return evidence;
    }
    
    private List<Evidence> mergeEvidence(List<Evidence> target, List<Evidence> src) {
        for(Evidence evidence : src) {
            target.add(evidence);
        }
        return target;
    }
    
    private List<Evidence> singleQuery(Integer entrezGeneId, String hugoSymbol, String alt) {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        
        Gene gene = null; 
        if (entrezGeneId!=null) {
            gene = geneBo.findGeneByEntrezGeneId(entrezGeneId);
        } else if (hugoSymbol!=null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
        }
        
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        List<Alteration> alteration = new ArrayList<Alteration>();
        
        if (alt != null && gene != null) {
            Alteration alterationDatum = alterationBo.findAlteration(gene, AlterationType.MUTATION, alt);
            if(alterationDatum != null) {
                alteration.add(alterationDatum);
            }
        }
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        
        List<Evidence> evidence= new ArrayList();
        
        if (gene != null && !alteration.isEmpty()) {
            evidence = evidenceBo.findEvidencesByAlteration(alteration);
        }else if(gene != null && alteration.isEmpty()) {
            evidence = evidenceBo.findEvidencesByGene(gene);
        }else {
            evidence = Collections.emptyList();
        }
        return evidence;
    }
}
