/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.controller;

import java.io.IOException;
import org.mskcc.cbio.oncokb.quest.VariantAnnotationXMLV2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author jgao
 */
@Controller
public class VariantAnnotationXMLV2Controller {
//    @RequestMapping(value="/legacy-api/var_annotation_v2", method=RequestMethod.POST,
//            produces="application/xml;charset=UTF-8")
    public @ResponseBody String getVariantAnnotation(
            @RequestParam(value="file", required=true) MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                return VariantAnnotationXMLV2.getVariantAnnotation(file.getInputStream());
            } catch (IOException ex) {
                return "<xml>Failed to upload file.</xml>";
            }
        } else {
            return "<xml>Failed to upload file.</xml>";
        }
    }
    
}
