/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.List;

import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.model.OncoTreeType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

import org.mskcc.cbio.oncokb.util.TumorTypeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author jgao
 */
@Controller
public class TumorTypeController {

//    @RequestMapping(value = "/legacy-api/tumorType.json")
    public
    @ResponseBody
    List<OncoTreeType> getTumorType(
        @RequestParam(value = "tumorTypeId", required = false) List<String> tumorTypeIds) {
        return TumorTypeUtils.getAllTumorTypes();
    }
}