package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
import org.mskcc.cbio.oncokb.model.Drug;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-14T18:47:53.991Z")

@Api(value = "drugs", description = "the drugs API")
public interface DrugsApi {

    @ApiOperation(value = "", notes = "Get all curated drugs.", response = Drug.class, responseContainer = "List", tags = {"Drugs",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Drug.class, responseContainer = "List")})
    @RequestMapping(value = "/drugs",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<Drug>> drugsGet();


    @ApiOperation(value = "Add a new drug", nickname = "addDrug", notes = "", tags = {"Drugs",})
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/drugs",
        produces = {"application/json"},
        consumes = {"application/x-www-form-urlencoded"},
        method = RequestMethod.POST)
    ResponseEntity<Void> addDrug(@ApiParam(value = "Prefer drug name") @RequestParam(value = "name", required = false) String name, @ApiParam(value = "NCIT Code") @RequestParam(value = "ncitCode", required = false) String ncitCode);


    @ApiOperation(value = "", notes = "Search drugs.", response = Drug.class, responseContainer = "List", tags = {"Drugs", "Search",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Drug.class, responseContainer = "List")})
    @RequestMapping(value = "/drugs/lookup",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<Drug>> drugsLookupGet(
        @ApiParam(value = "Drug Name") @RequestParam(value = "name", required = false) String name
        , @ApiParam(value = "NCI Thesaurus Code") @RequestParam(value = "ncitCode", required = false) String ncitCode
        , @ApiParam(value = "Drug Synonyms") @RequestParam(value = "synonym", required = false) String synonym
        , @ApiParam(value = "Exactly Match", required = true) @RequestParam(value = "exactMatch", required = true, defaultValue = "true") Boolean exactMatch
    );

    @ApiOperation(value = "Delete a drug", nickname = "deleteDrug", notes = "", tags = {"Drugs",})
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Drug not found"),
    })
    @RequestMapping(value = "/drugs/{drugId}",
        produces = {"application/json"},
        method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteDrug(@ApiParam(value = "Drug id to delete", required = true) @PathVariable("drugId") Integer drugId);


    @ApiOperation(value = "Find drug by ID", nickname = "getDrugById", notes = "Returns a single drug", response = Drug.class, tags = {"Drugs",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "successful operation", response = Drug.class),
        @ApiResponse(code = 404, message = "Drug not found")
    })
    @RequestMapping(value = "/drugs/{drugId}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Drug> getDrugById(@ApiParam(value = "ID of drug to return", required = true) @PathVariable("drugId") Integer drugId);


    @ApiOperation(value = "Update a drug", nickname = "updateDrugWithForm", notes = "", tags = {"Drugs",})
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid name supplied"),
        @ApiResponse(code = 404, message = "Drug not found"),
    })
    @RequestMapping(value = "/drugs/{drugId}",
        produces = {"application/json"},
        consumes = {"application/x-www-form-urlencoded"},
        method = RequestMethod.POST)
    ResponseEntity<Void> updateDrugWithForm(@ApiParam(value = "ID of drug that needs to be updated", required = true) @PathVariable("drugId") Integer drugId, @ApiParam(value = "Updated name of the pet") @RequestParam(value = "name", required = false) String name);


}
