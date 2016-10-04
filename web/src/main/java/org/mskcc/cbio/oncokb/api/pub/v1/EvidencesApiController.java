package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.mskcc.cbio.oncokb.apiModels.ApiObjectResp;
import org.mskcc.cbio.oncokb.apiModels.Meta;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.mskcc.cbio.oncokb.util.MetaUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Controller
public class EvidencesApiController implements EvidencesApi {

    public ResponseEntity<ApiObjectResp> evidencesIdGet(
        @ApiParam(value = "Unique identifier.", required = true) @PathVariable("id") Integer id
    ) {
        ApiObjectResp apiObjectResp = new ApiObjectResp();
        HttpStatus status = HttpStatus.OK;
        Meta meta = MetaUtils.getOKMeta();
        if (id == null) {
            meta = MetaUtils.getBadRequestMeta("Please specify evidence id.");
            status = HttpStatus.BAD_REQUEST;
        } else {
            apiObjectResp.setData(EvidenceUtils.getEvidenceByEvidenceId(id));
        }
        apiObjectResp.setMeta(meta);
        return new ResponseEntity<ApiObjectResp>(apiObjectResp, status);
    }

    public ResponseEntity<ApiListResp> evidencesLookupGet(
        @ApiParam(value = "The entrez gene ID. Use comma to seperate multi-queries.") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "The gene symbol used in Human Genome Organisation. Use comma to seperate multi-queries.") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "Variant name. Use comma to seperate multi-queries.") @RequestParam(value = "variant", required = false) String variant
        , @ApiParam(value = "Tumor type name. OncoTree code is supported. Use comma to seperate multi-queries.") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = "Consequence. Use comma to seperate multi-queries. Possible value: feature_truncation, frameshift_variant, inframe_deletion, inframe_insertion, initiator_codon_variant, missense_variant, splice_region_variant, stop_gained, synonymous_variant") @RequestParam(value = "consequence", required = false) String consequence
        , @ApiParam(value = "Protein Start. Use comma to seperate multi-queries.") @RequestParam(value = "proteinStart", required = false) String proteinStart
        , @ApiParam(value = "Protein End. Use comma to seperate multi-queries.") @RequestParam(value = "proteinEnd", required = false) String proteinEnd
        , @ApiParam(value = "Tumor type source. OncoTree tumor types are the default setting. We may have customized version, like Quest.", defaultValue = "oncotree") @RequestParam(value = "source", required = false, defaultValue = "oncotree") String source
        , @ApiParam(value = "Only show highest level evidences") @RequestParam(value = "highestLevelOnly", required = false, defaultValue = "FALSE") Boolean highestLevelOnly
        , @ApiParam(value = "Separate by comma. LEVEL_1, LEVEL_2A, LEVEL_2B, LEVEL_3A, LEVEL_3B, LEVEL_4, LEVEL_R1, LEVEL_R2, LEVEL_R3") @RequestParam(value = "levelOfEvidence", required = false) String levels
        , @ApiParam(value = "Separate by comma. Evidence type includes GENE_SUMMARY, GENE_BACKGROUND, MUTATION_SUMMARY, ONCOGENIC, MUTATION_EFFECT, VUS, PREVALENCE, PROGNOSTIC_IMPLICATION, TUMOR_TYPE_SUMMARY, NCCN_GUIDELINES, STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY, STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE, CLINICAL_TRIAL") @RequestParam(value = "evidenceTypes", required = false) String evidenceTypes
    ) {
        ApiListResp apiListResp = new ApiListResp();
        HttpStatus status = HttpStatus.OK;
        Meta meta = MetaUtils.getOKMeta();
        List<List<Evidence>> evidences = new ArrayList<>();

        Map<String, Object> requestQueries = MainUtils.GetRequestQueries(entrezGeneId == null ? null : Integer.toString(entrezGeneId), hugoSymbol, variant,
            tumorType, evidenceTypes, consequence, proteinStart, proteinEnd, null, source, levels);

        if (requestQueries == null) {
            apiListResp.setData(new ArrayList());
        }

        List<EvidenceQueryRes> evidenceQueries = EvidenceUtils.processRequest(
            (List<Query>) requestQueries.get("queries"),
            new HashSet<>((List<EvidenceType>) requestQueries.get("evidenceTypes")),
            null, source, new HashSet<>((List<LevelOfEvidence>) requestQueries.get("levels")), highestLevelOnly);

        if (evidenceQueries != null) {
            for (EvidenceQueryRes query : evidenceQueries) {
                evidences.add(query.getEvidences());
            }
        }

        apiListResp.setData(evidences);
        apiListResp.setMeta(meta);
        return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.OK);
    }

    public ResponseEntity<ApiListResp> evidencesLookupPost(@ApiParam(value = "List of queries. Please see swagger.json for request body format. Please use JSON string.", required = true) @RequestBody(required = true) EvidenceQueries body) {
        ApiListResp apiListResp = new ApiListResp();
        HttpStatus status = HttpStatus.OK;
        Meta meta = MetaUtils.getOKMeta();
        List<EvidenceQueryRes> result = new ArrayList<>();
        if (body.getQueries().size() > 0) {
            List<Query> requestQueries = body.getQueries();
            Set<EvidenceType> evidenceTypes = new HashSet<>();

            if (body.getEvidenceTypes() != null) {
                for (String type : body.getEvidenceTypes().split("\\s*,\\s*")) {
                    EvidenceType et = EvidenceType.valueOf(type);
                    evidenceTypes.add(et);
                }
            } else {
                evidenceTypes.add(EvidenceType.GENE_SUMMARY);
                evidenceTypes.add(EvidenceType.GENE_BACKGROUND);
            }

            result = EvidenceUtils.processRequest(requestQueries, evidenceTypes, null, body.getSource(),
                body.getLevels(), body.getHighestLevelOnly());
        }

        apiListResp.setData(result);
        apiListResp.setMeta(meta);
        return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.OK);
    }

    public ResponseEntity<ApiListResp> evidencesPost(@ApiParam(value = "List of unique identifier for each model. Separated by comma.", required = true) @RequestParam(value = "ids", required = true) String ids
    ) {
        ApiListResp apiListResp = new ApiListResp();
        HttpStatus status = HttpStatus.OK;
        Meta meta = MetaUtils.getOKMeta();

        if (ids == null) {
            meta = MetaUtils.getBadRequestMeta("Please specify evidence ids.");
            status = HttpStatus.BAD_REQUEST;
        } else {
            List<Integer> matchIds = MainUtils.stringToIntegers(ids);
            if (matchIds != null) {
                Set<Evidence> evidences = EvidenceUtils.getEvidenceByEvidenceIds(new HashSet<Integer>(matchIds));
                if (evidences != null) {
                    apiListResp.setData(new ArrayList(evidences));
                }
            }
        }
        apiListResp.setMeta(meta);
        return new ResponseEntity<ApiListResp>(apiListResp, status);
    }
}
