/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.controller;

import org.mskcc.cbio.oncogkb.model.Gene;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author jgao
 */
@Controller
public class GeneController {
    
    @RequestMapping(value="/gene.json")
    public @ResponseBody Gene getGene(
            @RequestParam(value="gene", required=true) String hugoSymbol) {
        Gene gene = new Gene(123, hugoSymbol);
        gene.setAliases("test");
        return gene;
    }
}