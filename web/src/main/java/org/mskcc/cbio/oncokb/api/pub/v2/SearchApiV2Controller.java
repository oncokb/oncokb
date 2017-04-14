package org.mskcc.cbio.oncokb.api.pub.v2;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.apiModels.SearchResult;
import org.mskcc.cbio.oncokb.config.annotation.V2Api;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.model.QueryType;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.QueryAnnotation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@V2Api
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
        @RequestParam(value = "consequence", required = false) String consequence,

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
        SearchResult searchResult = null;

        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            Query query = new Query(hugoSymbol, entrezGeneId, variant, tumorType, consequence, proteinStart, proteinEnd, levels, highestLevelOnly, null, null, id, queryType, source);
            searchResult = QueryAnnotation.annotateSearchQuery(query);
        }
        return new ResponseEntity<>(searchResult, status);
    }

    public ResponseEntity<List<SearchResult>> searchPost(@ApiParam(value = "List of queries.", required = true) @RequestBody() List<Query> body) {
        HttpStatus status = HttpStatus.OK;

        List<SearchResult> result = new ArrayList<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            for (Query query : body) {
                result.add(QueryAnnotation.annotateSearchQuery(query));
            }
        }
        return new ResponseEntity<>(result, status);
    }
}
