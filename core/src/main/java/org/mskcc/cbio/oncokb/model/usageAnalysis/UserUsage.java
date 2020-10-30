package org.mskcc.cbio.oncokb.model.usageAnalysis;

/**
 * Created by Yifu Yao on 2020-10-28
 */
public class UserUsage {
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String licenseType;
    private String jobTitle;
    private String company;
    private UsageSummary summary;
    
    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public UsageSummary getSummary() {
        return summary;
    }

    public void setSummary(UsageSummary summary) {
        this.summary = summary;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
