/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.service.JsonResultFactory;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jgao
 */
@Controller
public class GeneController {

    @RequestMapping(value="/legacy-api/gene.json")
    public @ResponseBody List<Gene> getGene(
            @RequestParam(value="entrezGeneId", required=false) List<Integer> entrezGeneIds
           , @RequestParam(value="hugoSymbol", required=false) List<String> hugoSymbols
        , @RequestParam(value = "fields", required = false) String fields
    ) {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();

        if (entrezGeneIds == null && hugoSymbols == null) {
            return geneBo.findAll();
        }

        List<Gene> genes = new ArrayList<Gene>();
        if (entrezGeneIds != null) {
            genes.addAll(geneBo.findGenesByEntrezGeneId(entrezGeneIds));
        } else if (hugoSymbols != null) {
            genes.addAll(geneBo.findGenesByHugoSymbol(hugoSymbols));
        }

        return JsonResultFactory.getGene(genes, fields);
    }

//    @RequestMapping(value = "/legacy-api/genes/update/{hugoSymbol}", method = RequestMethod.POST)
    public @ResponseBody
    String updateGene(@ApiParam(value = "hugoSymbol", required = true) @PathVariable("hugoSymbol") String hugoSymbol,
                      @RequestBody(required = true) Gene queryGene) {
        if (!hugoSymbol.equalsIgnoreCase(queryGene.getHugoSymbol())) {
            return "error";
        }
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
        gene.setTSG(queryGene.getTSG());
        gene.setOncogene(queryGene.getOncogene());
        geneBo.update(gene);

        return "success";
    }
}
