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
            @RequestParam(value="entrezGeneId", required=false) String entrezGeneId,
            @RequestParam(value="hugoSymbol", required=false) String hugoSymbol,
            @RequestParam(value="alteration", required=false) String alteration,
            @RequestParam(value="evidenceType", required=false) String evidenceType) {
        
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        
        List<Gene> genes = new ArrayList<Gene>(); 
        if (entrezGeneId!=null) {
            for (String id : entrezGeneId.split(",")) {
                Gene gene = geneBo.findGeneByEntrezGeneId(Integer.parseInt(id));
                genes.add(gene);
            }
        } else if (hugoSymbol!=null) {
            for (String symbol : hugoSymbol.split(",")) {
                Gene gene = geneBo.findGeneByHugoSymbol(symbol);
                genes.add(gene);
            }
        }
        
        if (genes.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<EvidenceType> evienceTypes = null;
        if (evidenceType!=null) {
            evienceTypes = new ArrayList<EvidenceType>();
            for (String type : evidenceType.split(",")) {
                EvidenceType et = EvidenceType.valueOf(type);
                evienceTypes.add(et);
            }
        }
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        
        if (alteration==null) {
            genes.removeAll(Collections.singleton(null));
            if (evienceTypes == null) {
                return evidenceBo.findEvidencesByGene(genes);
            } else {
                return evidenceBo.findEvidencesByGene(genes, evienceTypes);
            }
        }
        
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        List<Alteration> alterations = new ArrayList<Alteration>();
        String[] strAlts = alteration.split(",");
        int n = strAlts.length;
        for (int i=0; i<n; i++) {
            if (genes.get(i)!=null) {
                Alteration alt = alterationBo.findAlteration(genes.get(i), AlterationType.MUTATION, strAlts[i]);
                if (alt!=null) {
                    alterations.add(alt);
                }
            }
        }
        
        if (evienceTypes == null) {
            return evidenceBo.findEvidencesByAlteration(alterations);
        } else {
            return evidenceBo.findEvidencesByAlteration(alterations, evienceTypes);
        }
        
    }
}
