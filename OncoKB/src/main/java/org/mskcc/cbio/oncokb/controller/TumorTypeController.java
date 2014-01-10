/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.Collections;
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
    
    @RequestMapping(value="/tumor_type.json")
    public @ResponseBody List<TumorType> getTumorType(
            @RequestParam(value="tumor_type_id", required=false) String tumorTypeId) {
        TumorTypeBo tumorTypeBo = TumorTypeBo.class.cast(ApplicationContextSingleton.getApplicationContext().getBean("tumorTypeBo"));
        if (tumorTypeId!=null) {
            TumorType tumorType = tumorTypeBo.findTumorTypeById(tumorTypeId);
            if (tumorType==null) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(tumorType);
            }
        } else {
            return tumorTypeBo.findAll();
        }
    }
}