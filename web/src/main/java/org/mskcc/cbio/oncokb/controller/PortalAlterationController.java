/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.PortalAlterationBo;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
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

    @RequestMapping(value = "/api/portalAlterationSampleCount")
    public @ResponseBody
    List<PortalAlteration> getPortalAlteration(
            @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId) {
        PortalAlterationBo portalAlterationBo = ApplicationContextSingleton.getPortalAlterationBo();
        List<PortalAlteration> portalAlterations = new ArrayList<PortalAlteration>();
        if (entrezGeneId == null) {
            portalAlterations.addAll(portalAlterationBo.findPortalAlterationCount());
        } else {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            Gene gene = geneBo.findGeneByEntrezGeneId(entrezGeneId);

            portalAlterations.addAll(portalAlterationBo.findPortalAlterationCountByGene(gene));

        }
        return portalAlterations;
    }
}
