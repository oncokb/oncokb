/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.quest.VariantAnnotationXML;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author jgao
 */
@Controller
public class VariantAnnotationXMLController {
    @RequestMapping(value="/var_annotation", produces="application/xml;charset=UTF-8")//plain/text
    public @ResponseBody String getVariantAnnotation(
            @RequestParam(value="entrezGeneId", required=false) Integer entrezGeneId,
            @RequestParam(value="hugoSymbol", required=false) String hugoSymbol,
            @RequestParam(value="alterationType", required=false) String alterationType,
            @RequestParam(value="alteration", required=false) String alteration,
            @RequestParam(value="consequence", required=false) String consequence,
            @RequestParam(value="proteinStart", required=false) Integer proteinStart,
            @RequestParam(value="proteinEnd", required=false) Integer proteinEnd,
            @RequestParam(value="tumorType", required=false) String tumorType) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xlm>\n" 
                + VariantAnnotationXML.annotate(entrezGeneId, hugoSymbol,
                   alterationType, alteration, consequence, proteinStart, proteinEnd, tumorType)
                + "\n</xml>";
    }
}
