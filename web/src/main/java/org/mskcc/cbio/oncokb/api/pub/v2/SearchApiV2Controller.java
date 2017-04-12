package org.mskcc.cbio.oncokb.api.pub.v2;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.apiModels.SearchResult;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class SearchApiV2Controller implements SearchApiV2 {

    public ResponseEntity<SearchResult> searchGet(
        @ApiParam(value = "The query ID, user self defined ID which will be returned in the response.")
        @RequestParam(value = "id", required = false) String id,

        @ApiParam(value = "The gene symbol used in Human Genome Organisation.")
        @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol,

        @ApiParam(value = "The Entrez gene ID.")
        @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId,

        @ApiParam(value = "Variant name.")
        @RequestParam(value = "variant", required = false) String variant,

        @ApiParam(value = "Variant Consequence")
        @RequestParam(value = "consequence", required = false) SOTerm consequence,

        @ApiParam(value = "Protein Start")
        @RequestParam(value = "proteinStart", required = false) Integer proteinStart,

        @ApiParam(value = "Protein End")
        @RequestParam(value = "proteinEnd", required = false) Integer proteinEnd,

        @ApiParam(value = "Tumor type name. OncoTree code is supported.")
        @RequestParam(value = "tumorType", required = false) String tumorType,

        @ApiParam(value = "Tumor type source. OncoTree tumor types are the default setting. We may have customized version, like Quest.", defaultValue = "oncotree")
        @RequestParam(value = "source", required = false, defaultValue = "oncotree") String source,

        @ApiParam(value = "Level of evidences.")
        @RequestParam(value = "levels", required = false) Set<LevelOfEvidence> levels,

        @ApiParam(value = "Only show treatments with highest level")
        @RequestParam(value = "highestLevelOnly", required = false, defaultValue = "FALSE") Boolean highestLevelOnly,

        @ApiParam(value = "Query type. There maybe slight differences between different query types. Currently support web or regular.")
        @RequestParam(value = "queryType", required = false, defaultValue = "regular") QueryType queryType
    ) {
        HttpStatus status = HttpStatus.OK;
        SearchResult indicatorQueryResp = null;

        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            Query query = new Query();
            query.setId(id);

            if (entrezGeneId != null) {
                query.setEntrezGeneId(entrezGeneId);
            }
            query.setHugoSymbol(hugoSymbol);
            query.setAlteration(variant);
            query.setTumorType(tumorType);
            query.setConsequence(consequence.getVal());
            query.setType(queryType.name());
            if (proteinStart != null) {
                query.setProteinStart(proteinStart);
            }
            if (proteinEnd != null) {
                query.setProteinEnd(proteinEnd);
            }

            source = source == null ? "oncokb" : source;

            Set<LevelOfEvidence> levelOfEvidences = levels == null ? LevelUtils.getPublicAndOtherIndicationLevels() : levels;
//            indicatorQueryResp = IndicatorUtils.processQuery(query, null, levelOfEvidences, source, highestLevelOnly);
        }
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    public ResponseEntity<List<SearchResult>> searchPost(@ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody(required = true) EvidenceQueries body) {
        HttpStatus status = HttpStatus.OK;

        List<SearchResult> result = new ArrayList<>();

        if (body == null || body.getQueries() == null || body.getQueries().size() == 0) {
            status = HttpStatus.BAD_REQUEST;
        } else {

            String source = body.getSource() == null ? "oncokb" : body.getSource();

//            for (Query query : body.getQueries()) {
//                result.add(IndicatorUtils.processQuery(query, null,
//                    body.getLevels() == null ? LevelUtils.getPublicAndOtherIndicationLevels() : body.getLevels(),
//                    source, body.getHighestLevelOnly()));
//            }
        }
        return new ResponseEntity<>(result, status);
    }
}
