/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.Collections;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.springframework.context.ApplicationContext;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author jgao
 */
@Controller
public class AlterationController {
    
    @RequestMapping(value="/alteration.json")
    public @ResponseBody List<Alteration> getAlteration(
            @RequestParam(value="entrez_gene_id", required=false) Integer entrezGeneId,
            @RequestParam(value="hugo_symbol", required=false) String hugoSymbol) {
        
        ApplicationContext applicationContext = ApplicationContextSingleton.getApplicationContext();
        
        GeneBo geneBo = GeneBo.class.cast(applicationContext.getBean("geneBo"));
        
        Gene gene = null; 
        if (entrezGeneId!=null) {
            gene = geneBo.findGeneByEntrezGeneId(entrezGeneId);
        }
        
        if (hugoSymbol!=null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
        }
        
        if (gene == null) {
            return Collections.emptyList();
        }
        
        AlterationBo alterationBo = AlterationBo.class.cast(applicationContext.getBean("alterationBo"));
        
        return alterationBo.findAlterationsByGene(gene.getEntrezGeneId());
    }
}