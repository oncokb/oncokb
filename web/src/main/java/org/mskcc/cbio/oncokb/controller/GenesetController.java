package org.mskcc.cbio.oncokb.controller;

import io.swagger.annotations.ApiParam;
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
public class GenesetController {

    @RequestMapping(value = "/legacy-api/genesets/create", method = RequestMethod.POST)
    public
    @ResponseBody
    synchronized ResponseEntity createGeneset(@RequestBody Geneset geneset) {
        if (geneset == null || geneset.getName() == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        Geneset existedGeneSet = ApplicationContextSingleton.getGenesetBo().findGenesetByName(geneset.getName());
        if (existedGeneSet != null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        ApplicationContextSingleton.getGenesetBo().save(geneset);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/legacy-api/genesets/update/{id}", method = RequestMethod.POST)
    public
    @ResponseBody
    synchronized ResponseEntity updateGeneset(@ApiParam(value = "id", required = true) @PathVariable("id") Integer id,
                                              @RequestBody Geneset body) {
        Geneset geneset = ApplicationContextSingleton.getGenesetBo().findGenesetById(id);
        if (geneset == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } else {
            ApplicationContextSingleton.getGenesetBo().update(body);
            return new ResponseEntity(HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/legacy-api/genesets/delete/{id}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    synchronized ResponseEntity deleteGeneset(@ApiParam(value = "id", required = true) @PathVariable("id") Integer id) {
        if (id == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } else {
            Geneset geneset = ApplicationContextSingleton.getGenesetBo().findGenesetById(id);
            if (geneset == null) {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
            ApplicationContextSingleton.getGenesetBo().delete(geneset);
            return new ResponseEntity(HttpStatus.OK);
        }
    }
}
