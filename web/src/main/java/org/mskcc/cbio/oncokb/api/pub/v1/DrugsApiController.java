package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.mskcc.cbio.oncokb.apiModels.Meta;
import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.DrugUtils;
import org.mskcc.cbio.oncokb.util.MetaUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-14T18:47:53.991Z")

@Controller
public class DrugsApiController implements DrugsApi {

    public ResponseEntity<ApiListResp> drugsGet() {
        ApiListResp resp = new ApiListResp();
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        List<Drug> drugs = new ArrayList<>(DrugUtils.getAllDrugs());
        resp.setData(drugs);

        Meta meta = MetaUtils.getOKMeta();

        return new ResponseEntity<ApiListResp>(resp, HttpStatus.OK);
    }

    public ResponseEntity<ApiListResp> drugsLookupGet(
        @ApiParam(value = "Drug Name") @RequestParam(value = "name", required = false) String name
//        , @ApiParam(value = "") @RequestParam(value = "fdaApproved", required = false) String fdaApproved
        , @ApiParam(value = "ATC Code") @RequestParam(value = "atcCode", required = false) String atcCode
        , @ApiParam(value = "Drug Synonyms") @RequestParam(value = "synonym", required = false) String synonym
        , @ApiParam(value = "Exactly Match", required = true) @RequestParam(value = "exactMatch", required = true, defaultValue = "true") Boolean exactMatch
    ) {
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        ApiListResp apiListResp = new ApiListResp();
        Meta meta = MetaUtils.getOKMeta();
        Set<Drug> drugs = null;

        if (exactMatch == null) {
            exactMatch = true;
        }
        
        if (name != null) {
            drugs = DrugUtils.getDrugsByNames(Collections.singleton(name), !exactMatch);
        }

        if (atcCode != null) {
            Set<Drug> result = DrugUtils.getDrugsBySAtcCodes(Collections.singleton(atcCode), !exactMatch);
            if (result != null) {
                if (drugs == null) {
                    drugs = result;
                } else {
                    drugs = new HashSet<>(CollectionUtils.intersection(drugs, result));
                }
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

        if (drugs == null) {
            meta = MetaUtils.getBadRequestMeta("No parameter speficied.");
        } else {
            apiListResp.setData(new ArrayList(drugs));
        }
        apiListResp.setMeta(meta);
        return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.OK);
    }

}
