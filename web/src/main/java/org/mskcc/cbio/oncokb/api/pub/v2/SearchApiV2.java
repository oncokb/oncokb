package org.mskcc.cbio.oncokb.api.pub.v2;

import io.swagger.annotations.*;
import org.mskcc.cbio.oncokb.apiModels.Projection;
import org.mskcc.cbio.oncokb.apiModels.SearchResult;
import org.mskcc.cbio.oncokb.model.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Api(value = "search", description = "The search API")
public interface SearchApiV2 {

    @ApiOperation(value = "", notes = "General search", response = SearchResult.class, tags = {"Search",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SearchResult.class)})
    @RequestMapping(value = {"/v2/search"},
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    ResponseEntity<SearchResult> searchGet(
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
        @RequestParam(value = "queryType", required = false, defaultValue = "regular") QueryType queryType,

        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "DETAILED") Projection projection
    );


    @ApiOperation(value = "", notes = "General search.", response = SearchResult.class, responseContainer = "List", tags = {"Search",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SearchResult.class, responseContainer = "List")})
    @RequestMapping(value = "/v2/search",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
    ResponseEntity<List<SearchResult>> searchPost(@ApiParam(value = "List of queries.") @RequestBody() List<Query> body
    );
}
