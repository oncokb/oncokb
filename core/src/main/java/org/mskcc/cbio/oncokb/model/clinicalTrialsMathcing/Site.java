package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

import java.util.Date;

public class Site {
    private String recruitmentStatus;
    private Date recruitmentStatusDate;
    private Organization org;
    public String getRecruitmentStatus() {
        return recruitmentStatus;
    }
    public void setRecruitmentStatus(String recruitmentStatus) {
        this.recruitmentStatus = recruitmentStatus;
    }
    public Date getRecruitmentStatusDate() {
        return recruitmentStatusDate;
    }
    public void setRecruitmentStatusDate(Date recruitmentStatusDate) {
        this.recruitmentStatusDate = recruitmentStatusDate;
    }
    public Organization getOrg() {
        return org;
    }
    public void setOrg(Organization org) {
        this.org = org;
    }

}
