package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.model.Geneset;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api(tags = "Genesets")
public interface GenesetsApi {
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get list of currently curated genesets.", response = Geneset.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Geneset.class, responseContainer = "List")})
    @RequestMapping(value = "/genesets",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<Geneset>> genesetsGet();

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Find geneset by uuid", response = Geneset.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Geneset.class)})
    @RequestMapping(value = "/genesets/{uuid}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Geneset> genesetsUuidGet(
        @ApiParam(value = "Geneset UUID", required = true) @PathVariable(value = "uuid") String uuid
    );

}
