/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

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
    public @ResponseBody Gene getGene(
            @RequestParam(value="entrezGeneId", required=false) Integer entrezGeneId,
            @RequestParam(value="hugoSymbol", required=false) String hugoSymbol) {
        
        GeneBo geneBo = GeneBo.class.cast(ApplicationContextSingleton.getApplicationContext().getBean("geneBo"));
        if (entrezGeneId!=null) {
            return geneBo.getGeneByEntrezGeneId(entrezGeneId);
        }
        
        if (hugoSymbol!=null) {
            return geneBo.getGeneByHugoSymbol(hugoSymbol);
        }
        
        return null;
    }
}