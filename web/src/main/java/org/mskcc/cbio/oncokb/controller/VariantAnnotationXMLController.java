/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.quest.VariantAnnotationXML;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author jgao
 */
@Controller
public class VariantAnnotationXMLController {
//    @RequestMapping(value = "/legacy-api/var_annotation", produces = "application/xml;charset=UTF-8")//plain/text
    public
    @ResponseBody
    String getVariantAnnotation(
        @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId,
        @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol,
        @RequestParam(value = "alterationType", required = false) String alterationType,
        @RequestParam(value = "alteration", required = false) String alteration,
        @RequestParam(value = "consequence", required = false) String consequence,
        @RequestParam(value = "proteinStart", required = false) Integer proteinStart,
        @RequestParam(value = "proteinEnd", required = false) Integer proteinEnd,
        @RequestParam(value = "cancerType", required = false) String cancerType,
        @RequestParam(value = "subtype", required = false) String subtype,
        @RequestParam(value = "tumorType", required = false) String tumorType) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<xml>\n");

        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();

        Gene gene = null;
        if (entrezGeneId != null) {
            gene = geneBo.findGeneByEntrezGeneId(entrezGeneId);
        } else if (hugoSymbol != null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
        }

        if (entrezGeneId == null && hugoSymbol == null) {
            sb.append("<!-- no gene was specified --></xml>");
            return sb.toString();
        }

        if (gene == null) {
            sb.append("<!-- cound not find gene --></xml>");
            return sb.toString();
        }

        if (alteration != null) {
            alteration = AlterationUtils.trimAlterationName(alteration);
        }

        if (subtype != null) {
            tumorType = subtype;
        } else if (cancerType != null) {
            tumorType = cancerType;
        }

        Alteration alt = AlterationUtils.getAlteration(gene.getHugoSymbol(), alteration, alterationType, consequence, proteinStart, proteinEnd);

        sb.append(VariantAnnotationXML.annotate(alt, tumorType));

        sb.append("\n</xml>");

        return sb.toString();
    }
}
