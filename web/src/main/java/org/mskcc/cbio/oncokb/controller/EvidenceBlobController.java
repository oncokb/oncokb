/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.EvidenceBlobBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.EvidenceBlob;
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
public class EvidenceBlobController {
    
    @RequestMapping(value="/EvidenceBlob.json")
    public @ResponseBody List<EvidenceBlob> getEvidenceBlob(
            @RequestParam(value="entrezGeneId", required=false) List<Integer> entrezGeneIds,
            @RequestParam(value="hugoSymbol", required=false) List<String> hugoSymbols) {
        
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        
        List<Gene> genes = new ArrayList<Gene>(); 
        if (entrezGeneIds!=null) {
            genes.addAll(geneBo.findGenesByEntrezGeneId(entrezGeneIds));
        }
        
        if (hugoSymbols!=null) {
            genes.addAll(geneBo.findGenesByHugoSymbol(hugoSymbols));
        }
        
        if (genes.isEmpty()) {
            return Collections.emptyList();
        }
        
        EvidenceBlobBo EvidenceBlobBo = ApplicationContextSingleton.getEvidenceBlobBo();
        
        return EvidenceBlobBo.findEvidenceBlobsByGene(genes);
    }
}
