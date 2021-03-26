package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

/**
 * Created by Yifu Yao on 3/16/2021
 */
public class StructuredEligibility {
    private boolean acceptsHealthyVolunteers;
    private String gender;
    private String maxAge;
    private double maxAgeInYears;
    private double maxAgeNumber;
    private String maxAgeUnit;
    private String minAge;
    private double minAgeInYears;
    private double minAgeNumber;
    private String minAgeUnit;
    public boolean isAcceptsHealthyVolunteers() {
        return acceptsHealthyVolunteers;
    }
    public void setAcceptsHealthyVolunteers(boolean acceptsHealthyVolunteers) {
        this.acceptsHealthyVolunteers = acceptsHealthyVolunteers;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getMaxAge() {
        return maxAge;
    }
    public void setMaxAge(String maxAge) {
        this.maxAge = maxAge;
    }
    public double getMaxAgeInYears() {
        return maxAgeInYears;
    }
    public void setMaxAgeInYears(double maxAgeInYears) {
        this.maxAgeInYears = maxAgeInYears;
    }
    public double getMaxAgeNumber() {
        return maxAgeNumber;
    }
    public void setMaxAgeNumber(double maxAgeNumber) {
        this.maxAgeNumber = maxAgeNumber;
    }
    public String getMaxAgeUnit() {
        return maxAgeUnit;
    }
    public void setMaxAgeUnit(String maxAgeUnit) {
        this.maxAgeUnit = maxAgeUnit;
    }
    public String getMinAge() {
        return minAge;
    }
    public void setMinAge(String minAge) {
        this.minAge = minAge;
    }
    public double getMinAgeInYears() {
        return minAgeInYears;
    }
    public void setMinAgeInYears(double minAgeInYears) {
        this.minAgeInYears = minAgeInYears;
    }
    public double getMinAgeNumber() {
        return minAgeNumber;
    }
    public void setMinAgeNumber(double minAgeNumber) {
        this.minAgeNumber = minAgeNumber;
    }
    public String getMinAgeUnit() {
        return minAgeUnit;
    }
    public void setMinAgeUnit(String minAgeUnit) {
        this.minAgeUnit = minAgeUnit;
    }
}
