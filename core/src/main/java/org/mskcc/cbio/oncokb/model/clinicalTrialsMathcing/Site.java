package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

public class Site {
    private String recruitmentStatus;
    private String recruitmentStatusDate;
    private OrganizationInfo org;

    public String getRecruitmentStatus() {
        return recruitmentStatus;
    }

    public void setRecruitmentStatus(String recruitmentStatus) {
        this.recruitmentStatus = recruitmentStatus;
    }

    public String getRecruitmentStatusDate() {
        return recruitmentStatusDate;
    }

    public void setRecruitmentStatusDate(String recruitmentStatusDate) {
        this.recruitmentStatusDate = recruitmentStatusDate;
    }

    public OrganizationInfo getOrg() {
        return org;
    }

    public void setOrg(OrganizationInfo org) {
        this.org = org;
    }

}
