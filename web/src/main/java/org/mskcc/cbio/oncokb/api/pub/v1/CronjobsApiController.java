package org.mskcc.cbio.oncokb.api.pub.v1;

import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class CronjobsApiController {

    @RequestMapping(
        value = "/cronjobs/update-clinical-trials-cache",
        produces = { "application/json" },
        method = RequestMethod.GET
    )
    public void removeOldAuditEvents() {
        CacheUtils.updateClinicalTrials();
    }
}
