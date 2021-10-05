package org.mskcc.cbio.oncokb.api.pub.v1;

import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.model.OncoKBInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;


/**
 * Created by Hongxin Zhang on 7/13/18.
 */
@Controller
public class InfoApiController implements InfoApi {
    @Autowired
    CacheFetcher cacheFetcher;

    @Override
    public ResponseEntity<OncoKBInfo> infoGet() {
        return new ResponseEntity<>(this.cacheFetcher.getOncoKBInfo(), HttpStatus.OK);
    }
}
