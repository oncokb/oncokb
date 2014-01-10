/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.Collections;
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
            @RequestParam(value="entrez_gene_id", required=false) Integer entrezGeneId,
            @RequestParam(value="hugo_symbol", required=false) String hugoSymbol) {
        
        GeneBo geneBo = GeneBo.class.cast(ApplicationContextSingleton.getApplicationContext().getBean("geneBo"));
        
        Gene gene = null;
        if (entrezGeneId!=null) {
            gene = geneBo.findGeneByEntrezGeneId(entrezGeneId);
        } else if (hugoSymbol!=null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
        }
        
        if (gene == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(gene);
        }
    }
}