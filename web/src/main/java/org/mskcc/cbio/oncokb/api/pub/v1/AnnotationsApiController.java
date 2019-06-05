package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.annotation.*;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.GeneAnnotatorMyGeneInfo2;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.IndicatorUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
@Api(tags = "Annotations", description = "Providing annotation services")
@Controller
public class AnnotationsApiController {
    // Annotate mutations by protein change
    @ApiOperation(value = "", notes = "Annotate mutation by protein change.", response = IndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byProteinChange",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<IndicatorQueryResp> annotateMutationsByProteinChangeGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation. Example: BRAF") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID. (Higher priority than hugoSymbol). Example: 673") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "Protein Change. Example: V600E") @RequestParam(value = "alteration", required = false) String proteinChange
        , @ApiParam(value = "Consequence. Exacmple: missense_variant", allowableValues = "feature_truncation, frameshift_variant, inframe_deletion, inframe_insertion, start_lost, missense_variant, splice_region_variant, stop_gained, synonymous_variant") @RequestParam(value = "consequence", required = false) String consequence
        , @ApiParam(value = "Protein Start. Example: 600") @RequestParam(value = "proteinStart", required = false) Integer proteinStart
        , @ApiParam(value = "Protein End. Example: 600") @RequestParam(value = "proteinEnd", required = false) Integer proteinEnd
        , @ApiParam(value = "OncoTree(http://oncotree.mskcc.org) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
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

    @ApiOperation(value = "", notes = "Annotate mutations by protein change.", response = IndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byProteinChange",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<IndicatorQueryResp>> annotateMutationsByProteinChangePost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByProteinChangeQuery> body
    ) {
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

    // Annotate mutations by genomic change
    @ApiOperation(value = "", notes = "Annotate mutation by genomic change.", response = IndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byGenomicChange",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<IndicatorQueryResp> annotateMutationsByGenomicChangeGet(
        @ApiParam(value = "Genomic location. Example: 7,140453136,140453136,A,T", required = true) @RequestParam(value = "genomicLocation", required = true) String genomicLocation
        , @ApiParam(value = "OncoTree(http://oncotree.mskcc.org) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
    ) {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        indicatorQueryResp = getIndicatorQueryFromGenomicLocation(genomicLocation, tumorType);
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    @ApiOperation(value = "", notes = "Annotate mutations by genomic change.", response = IndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byGenomicChange",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<IndicatorQueryResp>> annotateMutationsByGenomicChangePost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByGenomicChangeQuery> body
    ) {
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
            query = new Query(null, AnnotationQueryType.REGULAR.getName(), null, alteration.getGene().getHugoSymbol(), alteration.getAlteration(), null, null, tumorType, alteration.getConsequence() == null ? null : alteration.getConsequence().getTerm(), alteration.getProteinStart(), alteration.getProteinEnd(), null);
        }
        return IndicatorUtils.processQuery(query, null, null, null, false, null);
    }

    // Annotate mutations by HGVSg
    @ApiOperation(value = "", notes = "Annotate mutation by HGVSg.", response = IndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byHGVSg",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<IndicatorQueryResp> annotateMutationsByHGVSgGet(
        @ApiParam(value = "HGVS genomic format. Example: 7:g.140453136A>T", required = true) @RequestParam(value = "hgvsg", required = true) String hgvsg
        , @ApiParam(value = "OncoTree(http://oncotree.mskcc.org) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
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

    @ApiOperation(value = "", notes = "Annotate mutations by genomic change.", response = IndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byHGVSg",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<IndicatorQueryResp>> annotateMutationsByHGVSgPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByHGVSgQuery> body
    ) {
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

    // Annotate copy number alterations
    @ApiOperation(value = "", notes = "Annotate copy number alteration.", response = IndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/copyNumberAlterations",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<IndicatorQueryResp> annotateCopyNumberAlterationsGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation. Example: BRAF") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID. (Higher priority than hugoSymbol). Example: 673") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "Copy number alteration type", required = true) @RequestParam(value = "copyNameAlterationType", required = true) CopyNumberAlterationType copyNameAlterationType
        , @ApiParam(value = "OncoTree(http://oncotree.mskcc.org) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
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

    @ApiOperation(value = "", notes = "Annotate copy number alterations.", response = IndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/copyNumberAlterations",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<IndicatorQueryResp>> annotateCopyNumberAlterationsPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateCopyNumberAlterationQuery> body
    ) {
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

    // Annotate structural variants
    @ApiOperation(value = "", notes = "Annotate structural variant.", response = IndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/structuralVariants",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<IndicatorQueryResp> annotateStructuralVariantsGet(
        @ApiParam(value = "The gene symbol A used in Human Genome Organisation. Example: ABL1") @RequestParam(value = "hugoSymbolA", required = false) String hugoSymbolA
        , @ApiParam(value = "The entrez gene ID A. (Higher priority than hugoSymbolA) Example: 25") @RequestParam(value = "entrezGeneIdA", required = false) Integer entrezGeneIdA
        , @ApiParam(value = "The gene symbol B used in Human Genome Organisation.Example: BCR ") @RequestParam(value = "hugoSymbolB", required = false) String hugoSymbolB
        , @ApiParam(value = "The entrez gene ID B. (Higher priority than hugoSymbolB) Example: 613") @RequestParam(value = "entrezGeneIdB", required = false) Integer entrezGeneIdB
        , @ApiParam(value = "Structural variant type", required = true) @RequestParam(value = "structuralVariantType", required = true) StructuralVariantType structuralVariantType
        , @ApiParam(value = "Whether is functional fusion", required = true) @RequestParam(value = "isFunctionalFusion", defaultValue = "FALSE", required = true) Boolean isFunctionalFusion
        , @ApiParam(value = "OncoTree(http://oncotree.mskcc.org) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
    ) {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        if ((entrezGeneIdA != null && hugoSymbolA != null && !GeneUtils.isSameGene(entrezGeneIdA, hugoSymbolA)) || (entrezGeneIdB != null && hugoSymbolB != null && !GeneUtils.isSameGene(entrezGeneIdB, hugoSymbolB))) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            Gene geneA = GeneUtils.getGene(entrezGeneIdA, hugoSymbolA);
            Gene geneB = GeneUtils.getGene(entrezGeneIdB, hugoSymbolB);

            if (geneA == null) {
                geneA = GeneAnnotatorMyGeneInfo2.findGeneFromCBioPortal(entrezGeneIdA == null ? hugoSymbolA : entrezGeneIdA.toString());
            }
            if (geneB == null) {
                geneB = GeneAnnotatorMyGeneInfo2.findGeneFromCBioPortal(entrezGeneIdB == null ? hugoSymbolB : entrezGeneIdB.toString());
            }

            if (geneA != null) {
                hugoSymbolA = geneA.getHugoSymbol();
            }
            if (geneB != null) {
                hugoSymbolB = geneB.getHugoSymbol();
            }
            if (hugoSymbolA == null || hugoSymbolB == null) {
                status = HttpStatus.BAD_REQUEST;
            } else {
                Query query = new Query(null, AnnotationQueryType.REGULAR.getName(), null, hugoSymbolA + "-" + hugoSymbolB, null, AlterationType.STRUCTURAL_VARIANT.name(), structuralVariantType, tumorType, isFunctionalFusion ? "fusion" : null, null, null, null);
                indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false, null);
            }
        }
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    @ApiOperation(value = "", notes = "Annotate structural variants.", response = IndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/structuralVariants",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<IndicatorQueryResp>> annotateStructuralVariantsPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody(required = true) List<AnnotateStructuralVariantQuery> body
    ) {
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
