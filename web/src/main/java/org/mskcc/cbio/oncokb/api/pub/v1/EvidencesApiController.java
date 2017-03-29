package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.MainUtils;
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

    public ResponseEntity<Evidence> evidencesIdGet(
        @ApiParam(value = "Unique identifier.", required = true) @PathVariable("id") Integer id
    ) {
        HttpStatus status = HttpStatus.OK;
        Evidence evidence = null;
        if (id == null) {
            status = HttpStatus.BAD_REQUEST;
        }else{
            evidence = EvidenceUtils.getEvidenceByEvidenceId(id);
        }
        return new ResponseEntity<>(evidence, status);
    }

    public ResponseEntity<List<Evidence>> evidencesLookupGet(
        @ApiParam(value = "The entrez gene ID.") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "The gene symbol used in Human Genome Organisation.") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "Variant name.") @RequestParam(value = "variant", required = false) String variant
        , @ApiParam(value = "Tumor type name. OncoTree code is supported.") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = "Consequence. Possible value: feature_truncation, frameshift_variant, inframe_deletion, inframe_insertion, initiator_codon_variant, missense_variant, splice_region_variant, stop_gained, synonymous_variant") @RequestParam(value = "consequence", required = false) String consequence
        , @ApiParam(value = "Protein Start.") @RequestParam(value = "proteinStart", required = false) String proteinStart
        , @ApiParam(value = "Protein End.") @RequestParam(value = "proteinEnd", required = false) String proteinEnd
        , @ApiParam(value = "Tumor type source. OncoTree tumor types are the default setting. We may have customized version, like Quest.", defaultValue = "oncotree") @RequestParam(value = "source", required = false, defaultValue = "oncotree") String source
        , @ApiParam(value = "Only show highest level evidences") @RequestParam(value = "highestLevelOnly", required = false, defaultValue = "FALSE") Boolean highestLevelOnly
        , @ApiParam(value = "Separate by comma. LEVEL_1, LEVEL_2A, LEVEL_2B, LEVEL_3A, LEVEL_3B, LEVEL_4, LEVEL_R1, LEVEL_R2, LEVEL_R3") @RequestParam(value = "levelOfEvidence", required = false) String levels
        , @ApiParam(value = "Separate by comma. Evidence type includes GENE_SUMMARY, GENE_BACKGROUND, MUTATION_SUMMARY, ONCOGENIC, MUTATION_EFFECT, VUS, PREVALENCE, PROGNOSTIC_IMPLICATION, TUMOR_TYPE_SUMMARY, NCCN_GUIDELINES, STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY, STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE, CLINICAL_TRIAL") @RequestParam(value = "evidenceTypes", required = false) String evidenceTypes
    ) {
        HttpStatus status = HttpStatus.OK;
        List<Evidence> evidences = new ArrayList<>();

        Map<String, Object> requestQueries = MainUtils.GetRequestQueries(entrezGeneId == null ? null : Integer.toString(entrezGeneId), hugoSymbol, variant,
            tumorType, evidenceTypes, consequence, proteinStart, proteinEnd, null, source, levels);

        if (requestQueries == null) {
            return new ResponseEntity<>(evidences, HttpStatus.OK);
        }

        List<EvidenceQueryRes> evidenceQueries = EvidenceUtils.processRequest(
            (List<Query>) requestQueries.get("queries"),
            new HashSet<>((List<EvidenceType>) requestQueries.get("evidenceTypes")),
            null, source, new HashSet<>((List<LevelOfEvidence>) requestQueries.get("levels")), highestLevelOnly);

        if (evidenceQueries != null) {
            for (EvidenceQueryRes query : evidenceQueries) {
                evidences.addAll(query.getEvidences());
            }
        }

        return new ResponseEntity<>(evidences, HttpStatus.OK);
    }

    public ResponseEntity<List<EvidenceQueryRes>> evidencesLookupPost(@ApiParam(value = "List of queries. Please see swagger.json for request body format. Please use JSON string.", required = true) @RequestBody(required = true) EvidenceQueries body) {
        HttpStatus status = HttpStatus.OK;
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

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public ResponseEntity<List<Evidence>> evidencesPost(@ApiParam(value = "List of unique identifier for each model. Separated by comma.", required = true) @RequestParam(value = "ids", required = true) String ids
    ) {
        HttpStatus status = HttpStatus.OK;
        List<Evidence> evidenceList = new ArrayList<>();
        if (ids == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            List<Integer> matchIds = MainUtils.stringToIntegers(ids);
            if (matchIds != null) {
                Set<Evidence> evidences = EvidenceUtils.getEvidenceByEvidenceIds(new HashSet<Integer>(matchIds));
                if (evidences != null) {
                    evidenceList = new ArrayList<>(evidences);
                }
            }
        }
        return new ResponseEntity<>(evidenceList, status);
    }
}
