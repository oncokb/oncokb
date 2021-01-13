package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hongxin Zhang on 2019-07-18.
 */
public class VariantAnnotationTumorType {
    boolean relevantTumorType = false;
    TumorType tumorType;
    List<Evidence> evidences = new ArrayList<>();

    public boolean isRelevantTumorType() {
        return relevantTumorType;
    }

    public void setRelevantTumorType(boolean relevantTumorType) {
        this.relevantTumorType = relevantTumorType;
    }

    public TumorType getTumorType() {
        return tumorType;
    }

    public void setTumorType(TumorType tumorType) {
        this.tumorType = tumorType;
    }

    public List<Evidence> getEvidences() {
        return evidences;
    }

    public void setEvidences(List<Evidence> evidences) {
        this.evidences = evidences;
    }
}
