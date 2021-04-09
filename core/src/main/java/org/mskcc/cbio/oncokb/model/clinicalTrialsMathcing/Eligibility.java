package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

import java.util.List;

/**
 * Created by Yifu Yao on 3/16/2021
 */
public class Eligibility {
    private StructuredEligibility structured;
    private List<UnstructuredEligibility> unstructured;
    public StructuredEligibility getStructured() {
        return structured;
    }
    public void setStructured(StructuredEligibility structured) {
        this.structured = structured;
    }
    public List<UnstructuredEligibility> getUnstructured() {
        return unstructured;
    }
    public void setUnstructured(List<UnstructuredEligibility> unstructured) {
        this.unstructured = unstructured;
    }

}
