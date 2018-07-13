package org.mskcc.cbio.oncokb.api.pub.v1;

import org.mskcc.cbio.oncokb.model.OncoKBInfo;
import org.mskcc.cbio.oncokb.model.Version;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.mskcc.cbio.oncokb.util.TumorTypeUtils;
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
        OncoKBInfo oncoKBInfo = new OncoKBInfo();
        oncoKBInfo.setOncoTreeVersion(TumorTypeUtils.getOncoTreeVersion());

        Version version = new Version();
        version.setDate(MainUtils.getDataVersionDate());
        version.setVersion(MainUtils.getDataVersion());

        oncoKBInfo.setDataVersion(version);

        return new ResponseEntity<>(oncoKBInfo, HttpStatus.OK);
    }
}
