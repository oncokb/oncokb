package org.mskcc.cbio.oncokb.api.legacy;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.GeneNumber;
import org.mskcc.cbio.oncokb.model.LevelNumber;
import org.mskcc.cbio.oncokb.model.MainNumber;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(value = "/public-api/v1/numbers", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/numbers", description = "the numbers API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class NumbersApi {


    @ApiOperation(value = "", notes = "Get gene related numbers", response = GeneNumber.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        @io.swagger.annotations.ApiResponse(code = 204, message = "")})
    @RequestMapping(value = "/gene/{hugoSymbol}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<GeneNumber> numbersGeneGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation.", required = true) @PathVariable("hugoSymbol") String hugoSymbol
    )
        throws NotFoundException {
        HttpStatus status = HttpStatus.OK;
        Set<GeneNumber> geneNumbers = NumberUtils.getGeneNumberListWithLevels(Collections.singleton(GeneUtils.getGeneByHugoSymbol(hugoSymbol)), LevelUtils.getPublicLevels());
        GeneNumber geneNumber = null;

        if (geneNumbers.size() == 1) {
            geneNumber = geneNumbers.iterator().next();
        } else {
            status = HttpStatus.NO_CONTENT;
        }

        return new ResponseEntity<>(geneNumber, status);
    }

    @ApiOperation(value = "", notes = "Get gene related numbers of all genes. This is for main page word cloud.", response = GeneNumber.class, responseContainer = "Set")
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/genes/",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Set<GeneNumber>> numbersGenesGet()
        throws NotFoundException {
        Set<GeneNumber> genes = new HashSet<>();

        if (CacheUtils.isEnabled()) {
            if (CacheUtils.getNumbers("genes") == null) {
                genes = NumberUtils.getAllGeneNumberListByLevels(LevelUtils.getPublicLevels());
                CacheUtils.setNumbers("genes", genes);
            } else {
                genes = (Set<GeneNumber>) CacheUtils.getNumbers("genes");
            }
        } else {
            genes = NumberUtils.getAllGeneNumberListByLevels(LevelUtils.getPublicLevels());
        }

        return new ResponseEntity<>(genes, HttpStatus.OK);
    }

    @ApiOperation(value = "", notes = "Get numbers served for the main page dashboard.", response = MainNumber.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/main/",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<MainNumber> numbersMainGet()
        throws NotFoundException {

        MainNumber mainNumber = new MainNumber();

        if (CacheUtils.isEnabled()) {
            if (CacheUtils.getNumbers("main") == null) {
                mainNumber.setGene(ApplicationContextSingleton.getGeneBo().countAll());

                List<Alteration> alterations = ApplicationContextSingleton.getAlterationBo().findAll();
                alterations = AlterationUtils.excludeVUS(alterations);
                alterations = AlterationUtils.excludeInferredAlterations(alterations);

                mainNumber.setAlteration(alterations.size());
                mainNumber.setTumorType(TumorTypeUtils.getAllTumorTypes().size());
                mainNumber.setDrug(NumberUtils.getDrugsCountByLevels(LevelUtils.getPublicLevels()));
                CacheUtils.setNumbers("main", mainNumber);
            } else {
                mainNumber = (MainNumber) CacheUtils.getNumbers("main");
            }
        } else {
            List<Alteration> alterations = ApplicationContextSingleton.getAlterationBo().findAll();
            List<Alteration> excludeVUS = AlterationUtils.excludeVUS(alterations);

            mainNumber.setAlteration(excludeVUS.size());
            mainNumber.setTumorType(TumorTypeUtils.getAllTumorTypes().size());
            mainNumber.setDrug(NumberUtils.getDrugsCountByLevels(LevelUtils.getPublicLevels()));
        }

        return new ResponseEntity<>(mainNumber, HttpStatus.OK);
    }

    @ApiOperation(value = "", notes = "Get gene related numbers of all genes. This is for main page word cloud.", response = LevelNumber.class, responseContainer = "Set")
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/levels/",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Set<LevelNumber>> numbersLevelsGet()
        throws NotFoundException {

        Set<LevelNumber> genes = new HashSet<>();

        if (CacheUtils.isEnabled()) {
            if (CacheUtils.getNumbers("levels") == null) {
                genes = NumberUtils.getLevelNumberListByLevels(LevelUtils.getPublicLevels());
                CacheUtils.setNumbers("levels", genes);
            } else {
                genes = (Set<LevelNumber>) CacheUtils.getNumbers("levels");
            }
        } else {
            genes = NumberUtils.getLevelNumberListByLevels(LevelUtils.getPublicLevels());
        }


        return new ResponseEntity<>(genes, HttpStatus.OK);
    }
}
