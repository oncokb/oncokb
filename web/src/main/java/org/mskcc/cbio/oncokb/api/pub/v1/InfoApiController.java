package org.mskcc.cbio.oncokb.api.pub.v1;

import org.mskcc.cbio.oncokb.model.OncoKBInfo;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;


/**
 * Created by Hongxin Zhang on 7/13/18.
 */
@Controller
public class InfoApiController implements InfoApi {
    @Override
    public ResponseEntity<OncoKBInfo> infoGet() {
        return new ResponseEntity<>(new OncoKBInfo(CacheUtils.getInfo()), HttpStatus.OK);
    }
}
