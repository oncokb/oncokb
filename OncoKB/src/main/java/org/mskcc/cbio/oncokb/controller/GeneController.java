/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.GeneBo;
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
public class GeneController {
    
    @RequestMapping(value="/gene.json")
    public @ResponseBody List<Gene> getGene(
            @RequestParam(value="entrezGeneId", required=false) List<Integer> entrezGeneIds,
            @RequestParam(value="hugoSymbol", required=false) List<String> hugoSymbols) {
        
        GeneBo geneBo = GeneBo.class.cast(ApplicationContextSingleton.getApplicationContext().getBean("geneBo"));
        
        if (entrezGeneIds == null && hugoSymbols == null) {
            return geneBo.findAll();
        }
        
        List<Gene> genes = new ArrayList<Gene>();
        if (entrezGeneIds!=null) {
            genes.addAll(geneBo.findGenesByEntrezGeneId(entrezGeneIds));
        } else if (hugoSymbols!=null) {
            genes.addAll(geneBo.findGenesByHugoSymbol(hugoSymbols));
        }
        
        return genes;
    }
}