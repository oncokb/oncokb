package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Hongxin on 10/28/16.
 */
@Controller
public class PrivateUtilsApiController implements PrivateUtilsApi {

    @Override
    public ResponseEntity<List<String>> utilsSuggestedVariantsGet() {
        HttpStatus status = HttpStatus.OK;

        List<String> variants = AlterationUtils.getGeneralAlterations();

        return new ResponseEntity<>(variants, status);
    }

    @Override
    public ResponseEntity<Boolean> utilsHotspotMutationGet(
        @ApiParam(value = "Gene hugo symbol") @RequestParam(value = "hugoSymbol") String hugoSymbol
        , @ApiParam(value = "Variant name") @RequestParam(value = "variant") String variant
    ) {
        HttpStatus status = HttpStatus.OK;

        Boolean isHotspot = false;

        Alteration alteration = AlterationUtils.getAlteration(hugoSymbol, variant, null, null, null, null);

        if (alteration != null) {
            isHotspot = HotspotUtils.isHotspot(alteration);
        }

        return new ResponseEntity<>(isHotspot, status);
    }

    @Override
    public ResponseEntity<GeneNumber> utilsNumbersGeneGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation.", required = true) @PathVariable("hugoSymbol") String hugoSymbol
    ) {
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

    @Override
    public ResponseEntity<Set<GeneNumber>> utilsNumbersGenesGet() {

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

    @Override
    public ResponseEntity<MainNumber> utilsNumbersMainGet() {
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

    @Override
    public ResponseEntity<Set<LevelNumber>> utilsNumbersLevelsGet() {
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
