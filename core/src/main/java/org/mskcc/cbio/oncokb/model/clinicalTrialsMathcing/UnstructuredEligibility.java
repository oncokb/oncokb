package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

/**
 * Created by Yifu Yao on 3/16/2021
 */
public class UnstructuredEligibility {
    private String description;
    private int displayOrder;
    private boolean inclusionIndicator;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getDisplayOrder() {
        return displayOrder;
    }
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
    public boolean isInclusionIndicator() {
        return inclusionIndicator;
    }
    public void setInclusionIndicator(boolean inclusionIndicator) {
        this.inclusionIndicator = inclusionIndicator;
    }

}
