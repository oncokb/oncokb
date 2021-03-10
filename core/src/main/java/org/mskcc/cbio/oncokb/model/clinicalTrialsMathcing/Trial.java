package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

import java.util.List;

/**
 * Created by Yifu Yao on 2020-09-08
 */


public class Trial {
    private String briefTitle;
    private String currentTrialStatus;
    private String nctId;
    private List<Arms> arms;
    private Boolean isUSTrial = null;
    private String principalInvestigator = null;

    public Boolean getIsUSTrial() {
        return isUSTrial;
    }

    public void setIsUSTrial(Boolean isUSTrial) {
        this.isUSTrial = isUSTrial;
    }

    public String getBriefTitle() {
        return briefTitle;
    }

    public void setBriefTitle(String briefTitle) {
        this.briefTitle = briefTitle;
    }

    public String getCurrentTrialStatus() {
        return currentTrialStatus;
    }

    public void setCurrentTrialStatus(String currentTrialStatus) {
        this.currentTrialStatus = currentTrialStatus;
    }

    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    public String getNctId() {
        return nctId;
    }

    public void setNctId(String nctId) {
        this.nctId = nctId;
    }

    public List<Arms> getArms() {
        return arms;
    }

    public void setArms(List<Arms> arms) {
        this.arms = arms;
    }

}
