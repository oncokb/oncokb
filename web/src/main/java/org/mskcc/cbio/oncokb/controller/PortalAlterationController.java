/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.PortalAlterationBo;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author jiaojiao
 */
@Controller
public class PortalAlterationController {

//    @RequestMapping(value = "/legacy-api/portalAlterationSampleCount")
    public @ResponseBody
    List<PortalAlteration> getPortalAlteration(@RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId,
            @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol) {
        PortalAlterationBo portalAlterationBo = ApplicationContextSingleton.getPortalAlterationBo();
        List<PortalAlteration> portalAlterations = new ArrayList<>();
        if (hugoSymbol == null && entrezGeneId == null) {
            portalAlterations.addAll(portalAlterationBo.findPortalAlterationCount());
        } else {
            Gene gene = entrezGeneId == null ? GeneUtils.getGeneByHugoSymbol(hugoSymbol) :
                GeneUtils.getGeneByEntrezId(entrezGeneId);
            portalAlterations.addAll(portalAlterationBo.findPortalAlterationCountByGene(gene));

        }
        return portalAlterations;
    }
    
//    @RequestMapping(value = "/legacy-api/mutationMapperData")
    public @ResponseBody
    List<PortalAlteration> getMutationMapperData(@RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId,
            @RequestParam(value = "hugoSymbol", required = true) String hugoSymbol) {
        PortalAlterationBo portalAlterationBo = ApplicationContextSingleton.getPortalAlterationBo();
        List<PortalAlteration> portalAlterations = new ArrayList<>();
        Gene gene = entrezGeneId == null ? GeneUtils.getGeneByHugoSymbol(hugoSymbol) :
            GeneUtils.getGeneByEntrezId(entrezGeneId);
        portalAlterations.addAll(portalAlterationBo.findMutationMapperData(gene));
        return portalAlterations;
    }
}
