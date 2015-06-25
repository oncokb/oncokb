package org.mskcc.cbio.oncokb.model;

import java.util.Set;

/**
 * Created by zhangh2 on 6/25/15.
 */
public class ClinicalTrialMapping {
    private Integer mappingId;
    private Alteration alteration;
    private TumorType tumorType;

    public ClinicalTrialMapping() {
    }

    public Integer getMappingId() {
        return mappingId;
    }

    public void setMappingId(Integer mappingId) {
        this.mappingId = mappingId;
    }

    public Alteration getAlteration() {
        return alteration;
    }

    public void setAlteration(Alteration alteration) {
        this.alteration = alteration;
    }

    public TumorType getTumorType() {
        return tumorType;
    }

    public void setTumorType(TumorType tumorType) {
        this.tumorType = tumorType;
    }
}
