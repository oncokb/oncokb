package org.mskcc.cbio.oncokb.api.pub.v1;

import org.mskcc.cbio.oncokb.model.OncoKBInfo;
import org.mskcc.cbio.oncokb.model.Version;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.mskcc.cbio.oncokb.util.TumorTypeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import static org.mskcc.cbio.oncokb.Constants.IS_PUBLIC_INSTANCE;

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

        oncoKBInfo.setLevels(LevelUtils.getInfoLevels());
        oncoKBInfo.setDataVersion(version);

        String isPublicInstance = PropertiesUtils.getProperties(IS_PUBLIC_INSTANCE);

        if (isPublicInstance != null && Boolean.valueOf(isPublicInstance)) {
            oncoKBInfo.setPublicInstance(true);
        }

        return new ResponseEntity<>(oncoKBInfo, HttpStatus.OK);
    }
}
