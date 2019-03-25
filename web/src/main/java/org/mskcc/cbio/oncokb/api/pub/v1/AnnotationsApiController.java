package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.annotation.*;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.IndicatorUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
@Controller
public class AnnotationsApiController implements AnnotationsApi {
    public ResponseEntity<IndicatorQueryResp> annotateMutationsByProteinChangeGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation.") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID. (Higher priority than hugoSymbol)") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "Protein Change") @RequestParam(value = "alteration", required = false) String proteinChange
        , @ApiParam(value = "Consequence. Possible value: feature_truncation, frameshift_variant, inframe_deletion, inframe_insertion, start_lost, missense_variant, splice_region_variant, stop_gained, synonymous_variant") @RequestParam(value = "consequence", required = false) String consequence
        , @ApiParam(value = "Protein Start") @RequestParam(value = "proteinStart", required = false) Integer proteinStart
        , @ApiParam(value = "Protein End") @RequestParam(value = "proteinEnd", required = false) Integer proteinEnd
        , @ApiParam(value = "Tumor type name. OncoTree code is supported.") @RequestParam(value = "tumorType", required = false) String tumorType
    ) {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            Query query = new Query(null, AnnotationQueryType.REGULAR.getName(), entrezGeneId, hugoSymbol, proteinChange, null, null, tumorType, consequence, proteinStart, proteinEnd, null);
            indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false, null);
        }
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    public ResponseEntity<List<IndicatorQueryResp>> annotateMutationsByProteinChangePost(@ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByProteinChangeQuery> body) {
        HttpStatus status = HttpStatus.OK;
        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            for (AnnotateMutationByProteinChangeQuery query : body) {
                result.add(IndicatorUtils.processQuery(new Query(query), null, null, null, false, null));
            }
        }
        return new ResponseEntity<>(result, status);
    }

    public ResponseEntity<IndicatorQueryResp> annotateMutationsByGenomicChangeGet(
        @ApiParam(value = "Genomic Location") @RequestParam(value = "genomicLocation", required = false) String genomicLocation
        , @ApiParam(value = "Tumor type name. OncoTree code is supported.") @RequestParam(value = "tumorType", required = false) String tumorType
    ) {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        indicatorQueryResp = getIndicatorQueryFromGenomicLocation(genomicLocation, tumorType);
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    public ResponseEntity<List<IndicatorQueryResp>> annotateMutationsByGenomicChangePost(@ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByGenomicChangeQuery>  body) {
        HttpStatus status = HttpStatus.OK;
        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            for (AnnotateMutationByGenomicChangeQuery query : body) {
                result.add(getIndicatorQueryFromGenomicLocation(query.getGenomicLocation(), query.getTumorType()));
            }
        }
        return new ResponseEntity<>(result, status);
    }

    private IndicatorQueryResp getIndicatorQueryFromGenomicLocation(String genomicLocation, String tumorType) {
        Alteration alteration = AlterationUtils.getAlterationFromGenomeNexus(GNVariantAnnotationType.GENOMIC_LOCATION, genomicLocation);
        Query query = new Query();
        if (alteration != null) {
            query = new Query(null, AnnotationQueryType.REGULAR.getName(), null, alteration.getGene().getHugoSymbol(), alteration.getAlteration(), null, null, tumorType, alteration.getConsequence().getTerm(), alteration.getProteinStart(), alteration.getProteinEnd(), null);
        }
        return IndicatorUtils.processQuery(query, null, null, null, false, null);
    }

    public ResponseEntity<IndicatorQueryResp> annotateMutationsByHGVSgGet(
        @ApiParam(value = "HGVS genomic format") @RequestParam(value = "hgvsg", required = false) String hgvsg
        , @ApiParam(value = "Tumor type name. OncoTree code is supported.") @RequestParam(value = "tumorType", required = false) String tumorType
    ) {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        if (hgvsg == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            Query query = new Query(null, "regular", null, null, null, null, null, tumorType, null, null, null, hgvsg);
            indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false, null);
        }
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    public ResponseEntity<List<IndicatorQueryResp>> annotateMutationsByHGVSgPost(@ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByHGVSgQuery> body) {
        HttpStatus status = HttpStatus.OK;
        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            for (AnnotateMutationByHGVSgQuery query : body) {
                result.add(IndicatorUtils.processQuery(new Query(query), null, null, null, false, null));
            }
        }
        return new ResponseEntity<>(result, status);
    }

    public ResponseEntity<IndicatorQueryResp> annotateCopyNumberAlterationsGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation.") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID. (Higher priority than hugoSymbol)") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "Copy number alteration type") @RequestParam(value = "copyNameAlterationType", required = true) CopyNumberAlterationType copyNameAlterationType
        , @ApiParam(value = "Tumor type name. OncoTree code is supported.") @RequestParam(value = "tumorType", required = false) String tumorType
    ) {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            Query query = new Query(null, AnnotationQueryType.REGULAR.getName(), entrezGeneId, hugoSymbol, StringUtils.capitalize(copyNameAlterationType.name().toLowerCase()), null, null, tumorType, null, null, null, null);
            indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false, null);
        }
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    public ResponseEntity<List<IndicatorQueryResp>> annotateCopyNumberAlterationsPost(@ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateCopyNumberAlterationQuery> body) {
        HttpStatus status = HttpStatus.OK;
        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            for (AnnotateCopyNumberAlterationQuery query : body) {
                result.add(IndicatorUtils.processQuery(new Query(query), null, null, null, false, null));
            }
        }
        return new ResponseEntity<>(result, status);
    }

    public ResponseEntity<IndicatorQueryResp> annotateStructuralVariantsGet(
        @ApiParam(value = "The gene symbol A used in Human Genome Organisation.") @RequestParam(value = "hugoSymbolA", required = false) String hugoSymbolA
        , @ApiParam(value = "The entrez gene ID A. (Higher priority than hugoSymbolA)") @RequestParam(value = "entrezGeneIdA", required = false) Integer entrezGeneIdA
        , @ApiParam(value = "The gene symbol B used in Human Genome Organisation.") @RequestParam(value = "hugoSymbolB", required = false) String hugoSymbolB
        , @ApiParam(value = "The entrez gene ID B. (Higher priority than hugoSymbolB)") @RequestParam(value = "entrezGeneIdB", required = false) Integer entrezGeneIdB
        , @ApiParam(value = "Structural variant type") @RequestParam(value = "structuralVariantType") StructuralVariantType structuralVariantType
        , @ApiParam(value = "Whether is functional fusion") @RequestParam(value = "isFunctionalFusion", defaultValue = "FALSE") Boolean isFunctionalFusion
        , @ApiParam(value = "Tumor type name. OncoTree code is supported.") @RequestParam(value = "tumorType", required = false) String tumorType
    ) {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        if ((entrezGeneIdA != null && hugoSymbolA != null && !GeneUtils.isSameGene(entrezGeneIdA, hugoSymbolA)) || (entrezGeneIdB != null && hugoSymbolB != null && !GeneUtils.isSameGene(entrezGeneIdB, hugoSymbolB))) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            Gene geneA = GeneUtils.getGene(entrezGeneIdA, hugoSymbolA);
            Gene geneB = GeneUtils.getGene(entrezGeneIdB, hugoSymbolB);
            Query query = new Query(null, AnnotationQueryType.REGULAR.getName(), null, geneA.getHugoSymbol() + "-" + geneB.getHugoSymbol(), null, AlterationType.STRUCTURAL_VARIANT.name(), structuralVariantType, tumorType, isFunctionalFusion ? "fusion" : null, null, null, null);
            indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false, null);
        }
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    public ResponseEntity<List<IndicatorQueryResp>> annotateStructuralVariantsPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody(required = true) List<AnnotateStructuralVariantQuery> body) {
        HttpStatus status = HttpStatus.OK;
        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            for (AnnotateStructuralVariantQuery query : body) {
                result.add(IndicatorUtils.processQuery(new Query(query), null, null, null, false, null));
            }
        }
        return new ResponseEntity<>(result, status);
    }
}
