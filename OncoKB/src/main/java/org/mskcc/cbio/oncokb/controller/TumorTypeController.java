/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.List;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author jgao
 */
@Controller
public class TumorTypeController {
    
    @RequestMapping(value="/tumorType.json")
    public @ResponseBody List<TumorType> getTumorType(
            @RequestParam(value="tumorTypeId", required=false) List<String> tumorTypeIds) {
        TumorTypeBo tumorTypeBo = TumorTypeBo.class.cast(ApplicationContextSingleton.getApplicationContext().getBean("tumorTypeBo"));
        if (tumorTypeIds!=null) {
            return tumorTypeBo.findTumorTypesById(tumorTypeIds);
        } else {
            return tumorTypeBo.findAll();
        }
    }
}