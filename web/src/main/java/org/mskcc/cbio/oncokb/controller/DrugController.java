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

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

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
    ) throws IOException {
        if (preferredName == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        Drug drug = drugBo.findDrugsByNcitCode(ncitCode);
        if (drug != null) {
            Set<Gene> genes = GeneUtils.getGenesWithDrug(drug);
            DrugUtils.updateDrugName(drug, preferredName);
            drugBo.update(drug);
            CacheUtils.updateGene(genes.stream().map(gene -> gene.getEntrezGeneId()).collect(Collectors.toSet()), true);
        }

        return new ResponseEntity(HttpStatus.OK);
    }


}
