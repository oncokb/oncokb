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
        if (geneset == null || geneset.getName() == null || geneset.getUuid() == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        Geneset existedGeneSet = ApplicationContextSingleton.getGenesetBo().findGenesetByUuid(geneset.getUuid());
        if (existedGeneSet != null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        ApplicationContextSingleton.getGenesetBo().save(geneset);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/legacy-api/genesets/update/{uuid}", method = RequestMethod.POST)
    public
    @ResponseBody
    synchronized ResponseEntity updateGeneset(@ApiParam(value = "uuid", required = true) @PathVariable("uuid") String uuid,
                                              @RequestBody Geneset body) {
        Geneset geneset = ApplicationContextSingleton.getGenesetBo().findGenesetByUuid(uuid);
        if (geneset == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } else {
            if (body.getName() != null) {
                geneset.setName(body.getName());
            }
            if (body.getGenes() != null) {
                geneset.setGenes(body.getGenes());
            }
            ApplicationContextSingleton.getGenesetBo().update(geneset);
            return new ResponseEntity(HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/legacy-api/genesets/delete/{uuid}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    synchronized ResponseEntity deleteGeneset(@ApiParam(value = "uuid", required = true) @PathVariable("uuid") String uuid) {
        if (uuid == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } else {
            Geneset geneset = ApplicationContextSingleton.getGenesetBo().findGenesetByUuid(uuid);
            if (geneset == null) {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
            ApplicationContextSingleton.getGenesetBo().delete(geneset);
            return new ResponseEntity(HttpStatus.OK);
        }
    }
}
