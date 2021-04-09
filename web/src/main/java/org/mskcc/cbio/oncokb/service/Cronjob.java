package org.mskcc.cbio.oncokb.service;

import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class Cronjob {
    @Scheduled(cron = "0 0 7 ? * TUE")
    public void updateClinicalTrialsCache(){
        CacheUtils.updateClinicalTrials();
    }
}
