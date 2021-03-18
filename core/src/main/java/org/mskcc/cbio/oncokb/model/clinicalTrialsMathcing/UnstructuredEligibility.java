package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

/**
 * Created by Yifu Yao on 3/16/2021
 */
public class UnstructuredEligibility {
    private String description;
    private int display_order;
    private boolean inclusion_indicator;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getDisplay_order() {
        return display_order;
    }
    public void setDisplay_order(int display_order) {
        this.display_order = display_order;
    }
    public boolean isInclusion_indicator() {
        return inclusion_indicator;
    }
    public void setInclusion_indicator(boolean inclusion_indicator) {
        this.inclusion_indicator = inclusion_indicator;
    }
}
