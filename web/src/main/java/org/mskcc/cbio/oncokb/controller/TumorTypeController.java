/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.util.TumorTypeUtils;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author jgao
 */
@Controller
public class TumorTypeController {

    @RequestMapping(value = "/legacy-api/tumorType.json")
    public
    @ResponseBody
    List<TumorType> getTumorType(
        @RequestParam(value = "tumorTypeId", required = false) List<String> tumorTypeIds) {
        return TumorTypeUtils.getAllTumorTypes();
    }

    @RequestMapping(value = "/legacy-api/tumorType")
    public
    @ResponseBody
    List<TumorType> getRelevantTumorType(
        @RequestParam(value = "query", required = false) String query
    ) {
        return TumorTypeUtils.findTumorTypes(query);
    }
}
