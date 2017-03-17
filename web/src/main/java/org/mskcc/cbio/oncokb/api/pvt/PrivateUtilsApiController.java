package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.HotspotUtils;
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
    public ResponseEntity<List<String>> utilsSuggestedVariantsGet() {
        HttpStatus status = HttpStatus.OK;

        List<String> variants = AlterationUtils.getGeneralAlterations();

        return new ResponseEntity<>(variants, status);
    }

    @Override
    public ResponseEntity<Boolean> utilsHotspotMutationGet(
        @ApiParam(value = "Gene hugo symbol") @RequestParam(value = "hugoSymbol") String hugoSymbol
        , @ApiParam(value = "Variant name") @RequestParam(value = "variant") String variant
    ) {
        HttpStatus status = HttpStatus.OK;

        Boolean isHotspot = false;

        Alteration alteration = AlterationUtils.getAlteration(hugoSymbol, variant, null, null, null, null);

        if (alteration != null) {
            isHotspot = HotspotUtils.isHotspot(alteration);
        }

        return new ResponseEntity<>(isHotspot, status);
    }
}
