package org.mskcc.cbio.oncokb.api.pvt;

import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.mskcc.cbio.oncokb.apiModels.Meta;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.MetaUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.*;

/**
 * Created by Hongxin on 10/28/16.
 */
@Controller
public class PrivateUtilsApiController implements PrivateUtilsApi {

    @Override
    public ResponseEntity<ApiListResp> utilsSuggestedVariantsGet() {
        ApiListResp apiListResp = new ApiListResp();
        Meta meta = MetaUtils.getOKMeta();
        HttpStatus status = HttpStatus.OK;

        List<String> variants = AlterationUtils.getGeneralAlterations();

        apiListResp.setData(variants);
        apiListResp.setMeta(meta);
        return new ResponseEntity<>(apiListResp, status);
    }
}
