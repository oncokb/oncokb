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
            @RequestParam(value="entrezGeneId", required=false) Integer entrezGeneId,
            @RequestParam(value="hugoSymbol", required=false) String hugoSymbol) {
        
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        
        Gene gene = null; 
        if (entrezGeneId!=null) {
            gene = geneBo.findGeneByEntrezGeneId(entrezGeneId);
        } else if (hugoSymbol!=null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
        }
        
        if (gene == null) {
            return Collections.emptyList();
        }
        
        EvidenceBlobBo EvidenceBlobBo = ApplicationContextSingleton.getEvidenceBlobBo();
        
        return EvidenceBlobBo.findEvidenceBlobsByGene(gene);
    }
}
