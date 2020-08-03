/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.Geneset;
import org.mskcc.cbio.oncokb.service.JsonResultFactory;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author jgao
 */
@Controller
public class GeneController {

    @RequestMapping(value = "/legacy-api/gene.json")
    public @ResponseBody
    List<Gene> getGene(
        @RequestParam(value = "entrezGeneId", required = false) List<Integer> entrezGeneIds
        , @RequestParam(value = "hugoSymbol", required = false) List<String> hugoSymbols
        , @RequestParam(value = "fields", required = false) String fields
    ) {
        if (entrezGeneIds == null && hugoSymbols == null) {
            return new ArrayList<>(GeneUtils.getAllGenes());
        }

        Set<Gene> genes = new HashSet<>();
        if (entrezGeneIds != null) {
            for (Integer enterz : entrezGeneIds) {
                Gene gene = GeneUtils.getGeneByEntrezId(enterz);
                if (gene != null) {
                    genes.add(gene);
                }
            }
        } else if (hugoSymbols != null) {
            for (String hugoSymbol : hugoSymbols) {
                Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
                if (gene != null) {
                    genes.add(gene);
                }
            }
        }

        return JsonResultFactory.getGene(new ArrayList<>(genes), fields);
    }

    @RequestMapping(value = "/legacy-api/genes/update/{hugoSymbol}", method = RequestMethod.POST)
    public @ResponseBody
    String updateGene(@ApiParam(value = "hugoSymbol", required = true) @PathVariable("hugoSymbol") String hugoSymbol,
                      @RequestBody(required = true) Gene queryGene) {
        if (!hugoSymbol.equalsIgnoreCase(queryGene.getHugoSymbol())) {
            return "error";
        }
        Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
        if (gene != null) {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            gene.setTSG(queryGene.getTSG());
            gene.setOncogene(queryGene.getOncogene());
            geneBo.update(gene);
        }
        return "success";
    }

    @RequestMapping(value = "/legacy-api/genes/create", method = RequestMethod.POST)
    public @ResponseBody
    String updateGene(@RequestBody(required = true) Gene queryGene) {
        if (queryGene == null) {
            return "error";
        }
        Gene gene = GeneUtils.getGene(queryGene.getEntrezGeneId(), queryGene.getHugoSymbol());
        if (gene == null) {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            geneBo.save(queryGene);
            CacheUtils.updateGene(Collections.singleton(gene.getEntrezGeneId()), true);
        }
        return "success";
    }

    @RequestMapping(value = "/legacy-api/genes/remove/{hugoSymbol}", method = RequestMethod.POST)
    public @ResponseBody
    String updateGene(@ApiParam(value = "hugoSymbol", required = true) @PathVariable("hugoSymbol") String hugoSymbol) {
        if (hugoSymbol == null) {
            return "error";
        }
        Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
        if (gene != null) {
            ApplicationContextSingleton.getEvidenceBo().deleteAll(new ArrayList<>(CacheUtils.getEvidences(gene)));
            ApplicationContextSingleton.getAlterationBo().deleteAll(new ArrayList<>(AlterationUtils.getAllAlterations(null, gene)));
            ApplicationContextSingleton.getGeneBo().delete(gene);
            CacheUtils.updateGene(Collections.singleton(gene.getEntrezGeneId()), true);
        }
        return "success";
    }

    @RequestMapping(value = "/legacy-api/genes/{hugoSymbol}/genesets/update", method = RequestMethod.POST)
    public @ResponseBody
    String updateGene(
        @ApiParam(value = "hugoSymbol", required = true) @PathVariable("hugoSymbol") String hugoSymbol
        , @RequestBody Set<Geneset> genesets
    ) {
        if (hugoSymbol == null) {
            return "error";
        }
        Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
        if (gene != null) {
            gene.setGenesets(genesets);
            ApplicationContextSingleton.getGeneBo().update(gene);
        }
        return "success";
    }
}
