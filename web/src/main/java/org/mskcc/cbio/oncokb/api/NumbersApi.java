package org.mskcc.cbio.oncokb.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.GeneNumber;
import org.mskcc.cbio.oncokb.model.LevelNumber;
import org.mskcc.cbio.oncokb.model.MainNumber;
import org.mskcc.cbio.oncokb.model.RespMeta;
import org.mskcc.cbio.oncokb.response.ApiNumbersGene;
import org.mskcc.cbio.oncokb.response.ApiNumbersGenes;
import org.mskcc.cbio.oncokb.response.ApiNumbersLeves;
import org.mskcc.cbio.oncokb.response.ApiNumbersMain;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.mskcc.cbio.oncokb.util.NumberUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(value = "/api/public/v1/numbers", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/numbers", description = "the numbers API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class NumbersApi {


    @ApiOperation(value = "", notes = "Get gene related numbers", response = ApiNumbersGene.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        @io.swagger.annotations.ApiResponse(code = 204, message = "")})
    @RequestMapping(value = "/gene/{hugoSymbol}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiNumbersGene> numbersGeneGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation.", required = true) @PathVariable("hugoSymbol") String hugoSymbol
    )
        throws NotFoundException {

        ApiNumbersGene apiNumbersGene = new ApiNumbersGene();
        RespMeta meta = new RespMeta();
        HttpStatus status = HttpStatus.OK;

        Set<GeneNumber> geneNumbers = NumberUtils.getGeneNumberList(Collections.singleton(GeneUtils.getGeneByHugoSymbol(hugoSymbol)));
        if (geneNumbers.size() == 1) {
            apiNumbersGene.setData(geneNumbers.iterator().next());
        } else {
            status = HttpStatus.NO_CONTENT;
            if(geneNumbers.size() > 1) {
                meta.setError_message("Found duplicate genes.");
            }else {
                meta.setError_message("No gene found.");
            }
        }
        meta.setCode(status.value());
        apiNumbersGene.setRespMeta(meta);

        return new ResponseEntity<ApiNumbersGene>(apiNumbersGene, status);
    }

    @ApiOperation(value = "", notes = "Get gene related numbers of all genes. This is for main page word cloud.", response = ApiNumbersGenes.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/genes/",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiNumbersGenes> numbersGenesGet()
        throws NotFoundException {
        Long oldTime = new Date().getTime();
        oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Start the servlet");

        ApiNumbersGenes apiNumbersGenes = new ApiNumbersGenes();

        Set<GeneNumber> genes = NumberUtils.getGeneNumberList();
        oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Get all genes");
        apiNumbersGenes.setData(genes);

        RespMeta meta = new RespMeta();
        meta.setCode(HttpStatus.OK.value());
        apiNumbersGenes.setRespMeta(meta);

        oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "End the servlet");
        return new ResponseEntity<ApiNumbersGenes>(apiNumbersGenes, HttpStatus.OK);
    }

    @ApiOperation(value = "", notes = "Get numbers served for the main page dashboard.", response = ApiNumbersMain.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/main/",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiNumbersMain> numbersMainGet()
        throws NotFoundException {

        ApiNumbersMain apiNumbersMain = new ApiNumbersMain();

        MainNumber mainNumber = new MainNumber();
        mainNumber.setGene(ApplicationContextSingleton.getGeneBo().countAll());
        mainNumber.setAlteration(ApplicationContextSingleton.getAlterationBo().countAll());
        mainNumber.setTumorType(ApplicationContextSingleton.getTumorTypeBo().countAll());
        mainNumber.setDrug(NumberUtils.getDrugsCount());
        apiNumbersMain.setData(mainNumber);

        RespMeta meta = new RespMeta();
        meta.setCode(HttpStatus.OK.value());
        apiNumbersMain.setRespMeta(meta);

        return new ResponseEntity<ApiNumbersMain>(apiNumbersMain, HttpStatus.OK);
    }

    @ApiOperation(value = "", notes = "Get gene related numbers of all genes. This is for main page word cloud.", response = ApiNumbersGenes.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/levels/",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiNumbersLeves> numbersLevelsGet()
        throws NotFoundException {
        
        ApiNumbersLeves apiNumbersGenes = new ApiNumbersLeves();

        Set<LevelNumber> genes = NumberUtils.getLevelNumberList();
        apiNumbersGenes.setData(genes);

        RespMeta meta = new RespMeta();
        meta.setCode(HttpStatus.OK.value());
        apiNumbersGenes.setRespMeta(meta);
        
        return new ResponseEntity<ApiNumbersLeves>(apiNumbersGenes, HttpStatus.OK);
    }
}
