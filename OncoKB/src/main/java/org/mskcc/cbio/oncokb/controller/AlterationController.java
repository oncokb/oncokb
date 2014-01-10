/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

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
            @RequestParam(value="entrezGeneId", required=false) Integer entrezGeneId,
            @RequestParam(value="hugoSymbol", required=false) String hugoSymbol) {
        
        ApplicationContext applicationContext = ApplicationContextSingleton.getApplicationContext();
        
        GeneBo geneBo = GeneBo.class.cast(applicationContext.getBean("geneBo"));
        
        Gene gene = null; 
        if (entrezGeneId!=null) {
            gene = geneBo.getGeneByEntrezGeneId(entrezGeneId);
        }
        
        if (hugoSymbol!=null) {
            gene = geneBo.getGeneByHugoSymbol(hugoSymbol);
        }
        
        if (gene == null) {
            return null;
        }
        
        AlterationBo alterationBo = AlterationBo.class.cast(applicationContext.getBean("alterationBo"));
        
        return alterationBo.getAlterationsByGene(gene.getEntrezGeneId());
    }
}