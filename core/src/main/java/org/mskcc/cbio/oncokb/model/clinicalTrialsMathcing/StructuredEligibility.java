package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

/**
 * Created by Yifu Yao on 3/16/2021
 */
public class StructuredEligibility {
    private boolean accepts_healthy_volunteers;
    private String gender;
    private String max_age;
    private double max_age_in_years;
    private double max_age_number;
    private String max_age_unit;
    private String min_age;
    private double min_age_in_years;
    private double min_age_number;
    private String min_age_unit;
    public boolean isAccepts_healthy_volunteers() {
        return accepts_healthy_volunteers;
    }
    public void setAccepts_healthy_volunteers(boolean accepts_healthy_volunteers) {
        this.accepts_healthy_volunteers = accepts_healthy_volunteers;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getMax_age() {
        return max_age;
    }
    public void setMax_age(String max_age) {
        this.max_age = max_age;
    }
    public double getMax_age_in_years() {
        return max_age_in_years;
    }
    public void setMax_age_in_years(double max_age_in_years) {
        this.max_age_in_years = max_age_in_years;
    }
    public double getMax_age_number() {
        return max_age_number;
    }
    public void setMax_age_number(double max_age_number) {
        this.max_age_number = max_age_number;
    }
    public String getMax_age_unit() {
        return max_age_unit;
    }
    public void setMax_age_unit(String max_age_unit) {
        this.max_age_unit = max_age_unit;
    }
    public String getMin_age() {
        return min_age;
    }
    public void setMin_age(String min_age) {
        this.min_age = min_age;
    }
    public double getMin_age_in_years() {
        return min_age_in_years;
    }
    public void setMin_age_in_years(double min_age_in_years) {
        this.min_age_in_years = min_age_in_years;
    }
    public double getMin_age_number() {
        return min_age_number;
    }
    public void setMin_age_number(double min_age_number) {
        this.min_age_number = min_age_number;
    }
    public String getMin_age_unit() {
        return min_age_unit;
    }
    public void setMin_age_unit(String min_age_unit) {
        this.min_age_unit = min_age_unit;
    }
    
}
