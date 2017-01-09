package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.mskcc.cbio.oncokb.apiModels.ApiObjectResp;
import org.mskcc.cbio.oncokb.apiModels.Meta;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.HotspotUtils;
import org.mskcc.cbio.oncokb.util.MetaUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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

    @Override
    public ResponseEntity<ApiObjectResp> utilsHotspotMutationGet(
        @ApiParam(value = "Gene hugo symbol") @RequestParam(value = "hugoSymbol") String hugoSymbol
        , @ApiParam(value = "Variant name") @RequestParam(value = "variant") String variant
    ) {
        ApiObjectResp apiObjectResp = new ApiObjectResp();
        Meta meta = MetaUtils.getOKMeta();
        HttpStatus status = HttpStatus.OK;

        Boolean isHotspot = false;

        Alteration alteration = AlterationUtils.getAlteration(hugoSymbol, variant, null, null, null, null);

        if (alteration != null) {
            isHotspot = HotspotUtils.isHotspot(
                alteration.getGene().getHugoSymbol(), alteration.getProteinStart(), alteration.getProteinEnd());
        }

        apiObjectResp.setData(isHotspot);
        apiObjectResp.setMeta(meta);
        return new ResponseEntity<>(apiObjectResp, status);
    }
}
