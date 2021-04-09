package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.oncokb.Constants;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.*;
import org.mskcc.cbio.oncokb.util.ClinicalTrialsUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by Yifu Yao on 2020-09-08
 */
@Api(tags = "Trials", description = "Clinical Trials Matching")
@Controller
public class TrialsApiController {

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(
        "Return a list of trials using OncoTree Code and/or treatment"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                code = 200,
                message = "OK",
                responseContainer = "List"
            ),
            @ApiResponse(
                code = 400,
                message = "Error",
                response = String.class
            ),
        }
    )
    @RequestMapping(
        value = "/trials",
        produces = { "application/json" },
        method = RequestMethod.GET
    )
    public ResponseEntity<List<ClinicalTrial>> trialsMatchingGet(
        @ApiParam(value = "", required = true) @RequestParam(
            value = "",
            required = true
        ) String oncoTreeCode,
        @ApiParam(value = "", required = false) @RequestParam(
            value = "",
            required = false
        ) String treatment
    )
        throws IOException, ParseException {
        if (!ClinicalTrialsUtils.getInstance().isFilesConfigured()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpStatus status = HttpStatus.OK;
        Map<String, TumorMap> tumors = ClinicalTrialsUtils
            .getInstance()
            .getAllMappingResult();
        TumorMap tumor = new TumorMap();
        if (tumors.containsKey(oncoTreeCode)) {
            tumor = tumors.get(oncoTreeCode);
            if (treatment == null) {
                return new ResponseEntity<List<ClinicalTrial>>(
                    ClinicalTrialsUtils
                        .getInstance()
                        .replaceKeysWithSites(
                            ClinicalTrialsUtils
                                .getInstance()
                                .getTrialsByIDList(tumor.getTrials())
                        ),
                    status
                );
            }
            List<ClinicalTrialMap> trial = ClinicalTrialsUtils
                .getInstance()
                .filterTrialsByTreatment(
                    ClinicalTrialsUtils
                        .getInstance()
                        .getTrialsByIDList(tumor.getTrials()),
                    treatment
                );
            return new ResponseEntity<List<ClinicalTrial>>(
                ClinicalTrialsUtils.getInstance().replaceKeysWithSites(trial),
                status
            );
        }
        return new ResponseEntity<List<ClinicalTrial>>(
            new ArrayList<>(),
            status
        );
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(
        "Return a list of clinical trials by cancer type, treatment, location and distance"
    )
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", responseContainer = "Map"),
            @ApiResponse(
                code = 400,
                message = "Error",
                response = String.class
            ),
        }
    )
    @RequestMapping(
        value = "/trials-by-cancer-type",
        produces = { "application/json" },
        method = RequestMethod.GET
    )
    public ResponseEntity<List<ClinicalTrial>> trialsGetByCancerType(
        @ApiParam(
            value = "The cancer type that clinical trials belong to. Support cancer type name and OncoTree Code. Example: Glioma or AASTR. Support special cancer types: ALL_TUMORS, ALL_SOLID_TUMORS, ALL_LIQUID_TUMORS."
        ) @RequestParam(
            value = "cancerType",
            required = true
        ) String cancerType,
        @ApiParam(
            value = "Consisted of single/multiple drugs. Support drug name or NCIT code. For multiple drugs treatment, use '+' as separator. Example: Binimetinib+Ribociclib"
        ) @RequestParam(value = "treatment", required = false) String treatment,
        @ApiParam(
            value = Constants.CLINICAL_TRIAL_ADDRESS_DESCRIPTION
        ) @RequestParam(value = "address", required = false) String address,
        @ApiParam(
            value = Constants.CLINICAL_TRIAL_COUNTRY_DESCRIPTION
        ) @RequestParam(value = "country", required = false) String country,
        @ApiParam(
            value = Constants.CLINICAL_TRIAL_DISTANCE_DESCRIPTION
        ) @RequestParam(value = "distance", required = false) Double distance
    )
        throws UnsupportedEncodingException, IOException, ParseException {
        if (!ClinicalTrialsUtils.getInstance().isFilesConfigured()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpStatus status = HttpStatus.OK;
        List<ClinicalTrialMap> result = new ArrayList<>();

        if (treatment != null && treatment.indexOf(",") != -1) {
            status = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(new ArrayList<ClinicalTrial>(), status);
        }
        String location = null;
        if (
            address != null &&
            !address.isEmpty() &&
            country != null &&
            !country.isEmpty()
        ) {
            location = String.format("%s, %s", address, country);
        }

        Map<String, TumorMap> tumors = ClinicalTrialsUtils
            .getInstance()
            .getAllMappingResult();
        if (cancerType.equals(SpecialTumorType.ALL_TUMORS.toString())) {
            Set<String> nctIDSet = new HashSet<>();
            for (String oncoTreeCode : tumors.keySet()) {
                TumorMap tumor = tumors.get(oncoTreeCode);
                for (String curTrialID : tumor.getTrials()) {
                    if (!nctIDSet.contains(curTrialID)) {
                        nctIDSet.add(curTrialID);
                        result.add(
                            ClinicalTrialsUtils
                                .getInstance()
                                .getAllTrials()
                                .get(curTrialID)
                        );
                    }
                }
            }
            result =
                ClinicalTrialsUtils
                    .getInstance()
                    .filterTrialsByTreatmentAndLocation(
                        result,
                        treatment,
                        location,
                        distance
                    );
            return new ResponseEntity<>(
                ClinicalTrialsUtils.getInstance().replaceKeysWithSites(result),
                status
            );
        }

        Set<String> nctIDSet = new HashSet<>();
        List<ClinicalTrialMap> trials = new ArrayList<>();
        SpecialTumorType specialTumorType = null;
        try {
            specialTumorType = SpecialTumorType.valueOf(cancerType);
        } catch (Exception ex) {}
        if (specialTumorType != null) {
            trials =
                ClinicalTrialsUtils
                    .getInstance()
                    .filterTrialsBySpecialCancerType(specialTumorType);
        } else {
            trials =
                ClinicalTrialsUtils
                    .getInstance()
                    .filterTrialsByCancerType(cancerType);
        }
        for (ClinicalTrialMap trial : trials) {
            if (!nctIDSet.contains(trial.getNctId())) {
                nctIDSet.add(trial.getNctId());
                result.add(trial);
            }
        }
        result =
            ClinicalTrialsUtils
                .getInstance()
                .filterTrialsByTreatmentAndLocation(
                    result,
                    treatment,
                    location,
                    distance
                );
        return new ResponseEntity<>(
            ClinicalTrialsUtils.getInstance().replaceKeysWithSites(result),
            status
        );
    }
}
