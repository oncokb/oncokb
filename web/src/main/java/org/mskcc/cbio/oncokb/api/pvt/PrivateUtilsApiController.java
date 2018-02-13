package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.apiModels.MatchVariantRequest;
import org.mskcc.cbio.oncokb.apiModels.MatchVariant;
import org.mskcc.cbio.oncokb.apiModels.MatchVariantResult;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

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
                Set<Gene> allGenes = GeneUtils.getAllGenes();
                Integer numRealGenes = 0;
                for (Gene gene : allGenes) {
                    if (gene.getEntrezGeneId() > 0)
                        numRealGenes++;
                }
                mainNumber.setGene(numRealGenes);

                List<Alteration> alterations = new ArrayList<>(AlterationUtils.getAllAlterations());
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

    @Override
    public ResponseEntity<Map<String, Boolean>> validateTrials(@ApiParam(value = "NCTID list") @RequestParam(value = "nctIds") List<String> nctIds) throws ParserConfigurationException, SAXException, IOException {
        return new ResponseEntity<>(MainUtils.validateTrials(nctIds), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<MatchVariantResult>> validateVariantExamplePost(@ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody(required = true) MatchVariantRequest body) {
        List<MatchVariantResult> results = new ArrayList<>();

        for (Query query : body.getQueries()) {
            MatchVariantResult matchVariantResult = new MatchVariantResult();
            matchVariantResult.setQuery(query);
            Set<MatchVariant> match = new HashSet<>();

            for (MatchVariant matchVariantRequestVariant : body.getOncokbVariants()) {
                if(query.getHugoSymbol().equals(matchVariantRequestVariant.getHugoSymbol())) {
                    boolean isMatch = matchVariant(query.getHugoSymbol(), matchVariantRequestVariant.getAlteration(), query.getAlteration());
                    if (isMatch) {
                        match.add(matchVariantRequestVariant);
                    }
                }
            }
            matchVariantResult.setResult(match);
            results.add(matchVariantResult);
        }
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Boolean>> validateVariantExampleGet(String hugoSymbol, String variant, String examples) throws ParserConfigurationException, SAXException, IOException {
        Map<String, Boolean> validation = new HashMap<>();
        for (String example : examples.split(",")) {
            validation.put(example, matchVariant(hugoSymbol, variant, example));
        }
        return new ResponseEntity<>(validation, HttpStatus.OK);
    }

    private boolean matchVariant(String hugoSymbol, String variant, String example) {
        Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
        boolean isMatched = false;
        if (gene != null) {
            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
            boolean isGeneralAlteration = AlterationUtils.isGeneralAlterations(variant);
            Alteration oncokbVariant = new Alteration();
            if (!isGeneralAlteration) {            // Annotate OncoKB variant
                oncokbVariant.setGene(gene);
                oncokbVariant.setAlteration(variant);
                oncokbVariant.setName(variant);
                AlterationUtils.annotateAlteration(oncokbVariant, variant);
            }

            example = example.trim();
            Alteration exampleVariant = new Alteration();
            exampleVariant.setGene(gene);
            exampleVariant.setAlteration(example);
            exampleVariant.setName(example);
            AlterationUtils.annotateAlteration(exampleVariant, example);

            LinkedHashSet<Alteration> relevantAlterations = new LinkedHashSet<>();
            if (isGeneralAlteration) {
                relevantAlterations = alterationBo.findRelevantAlterations(exampleVariant, true);
                for (Alteration alteration : relevantAlterations) {
                    if (alteration.getAlteration().toLowerCase().equals(variant.toLowerCase())) {
                        isMatched = true;
                        break;
                    }
                }
            } else {
                relevantAlterations = alterationBo.findRelevantAlterations(exampleVariant, Collections.singleton(oncokbVariant), false);
                isMatched = relevantAlterations.size() > 0;
            }
        }
        return isMatched;
    }
}
