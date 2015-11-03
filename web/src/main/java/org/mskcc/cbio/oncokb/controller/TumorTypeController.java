/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.ArrayList;
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
    
    @RequestMapping(value="/api/tumorType.json")
    public @ResponseBody List<TumorType> getTumorType(
            @RequestParam(value="tumorTypeId", required=false) List<String> tumorTypeIds) {
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        if (tumorTypeIds!=null) {
            List<TumorType> tumorTypes = new ArrayList<TumorType>(tumorTypeIds.size());
            for (String tumorTypeId : tumorTypeIds) {
                TumorType tumorType = tumorTypeBo.findTumorTypeById(tumorTypeId);
                if (tumorType!=null) {
                    tumorTypes.add(tumorType);
                }
            }
            return tumorTypes;
        } else {
            return tumorTypeBo.findAll();
        }
    }
}