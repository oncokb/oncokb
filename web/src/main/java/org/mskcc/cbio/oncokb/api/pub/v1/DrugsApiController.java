package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.dao.DrugDao;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.DrugUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.*;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-14T18:47:53.991Z")

@Controller
public class DrugsApiController implements DrugsApi {

    public ResponseEntity<List<Drug>> drugsGet() {
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        List<Drug> drugs = new ArrayList<>(DrugUtils.getAllDrugs());

        return new ResponseEntity<>(drugs, HttpStatus.OK);
    }

    public ResponseEntity<List<Drug>> drugsLookupGet(
        @ApiParam(value = "Drug Name") @RequestParam(value = "name", required = false) String name
        , @ApiParam(value = "NCI Thesaurus Code") @RequestParam(value = "ncitCode", required = false) String ncitCode
        , @ApiParam(value = "Drug Synonyms") @RequestParam(value = "synonym", required = false) String synonym
        , @ApiParam(value = "Exactly Match", required = true) @RequestParam(value = "exactMatch", required = true, defaultValue = "true") Boolean exactMatch
    ) {
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        Set<Drug> drugs = null;

        if (exactMatch == null) {
            exactMatch = true;
        }

        if (name != null) {
            drugs = DrugUtils.getDrugsByNames(Collections.singleton(name), !exactMatch);
        }

        if (ncitCode != null) {
            Drug drug = DrugUtils.getDrugByNcitCode(ncitCode);
            if (drug != null) {
                drugs = new HashSet<>(Collections.singleton(drug));
            }
        }

        if (synonym != null) {
            Set<Drug> result = DrugUtils.getDrugsBySynonyms(Collections.singleton(synonym), !exactMatch);
            if (result != null) {
                if (drugs == null) {
                    drugs = result;
                } else {
                    drugs = new HashSet<>(CollectionUtils.intersection(drugs, result));
                }
            }
        }

        List<Drug> drugList = new ArrayList();
        if (drugs != null) {
            drugList.addAll(drugs);
        }
        return new ResponseEntity<>(drugList, HttpStatus.OK);
    }
}
