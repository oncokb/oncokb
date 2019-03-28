/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Hongxin Zhang
 */
@Controller
public class DrugController {
    @RequestMapping(value = "/legacy-api/drugs/update/{ncitCode}", method = RequestMethod.POST)
    public
    @ResponseBody
    synchronized ResponseEntity updateEvidence(
        @ApiParam(value = "ncitCode", required = true) @PathVariable("ncitCode") String ncitCode
        , @ApiParam(value = "preferredName", required = true) @RequestParam(value = "preferredName", required = true) String preferredName
    ) {
        if (preferredName == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        Drug drug = drugBo.findDrugsByNcitCode(ncitCode);
        if (drug != null) {
            drug.setDrugName(preferredName);
            drugBo.update(drug);
        }

        return new ResponseEntity(HttpStatus.OK);
    }


}
