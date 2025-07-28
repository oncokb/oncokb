package org.mskcc.cbio.oncokb.model;

import java.util.List;

public class SampleQueryResp implements java.io.Serializable {
    private String id;
    private String tumorType;
    private List<IndicatorQueryResp> structuralVariants;
    private List<IndicatorQueryResp> copyNumberAlterations;
    private List<IndicatorQueryResp> mutations;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTumorType() {
        return tumorType;
    }

    public void setTumorType(String tumorType) {
        this.tumorType = tumorType;
    }

    public List<IndicatorQueryResp> getStructuralVariants() {
        return structuralVariants;
    }

    public void setStructuralVariants(List<IndicatorQueryResp> structuralVariants) {
        this.structuralVariants = structuralVariants;
    }

    public List<IndicatorQueryResp> getCopyNumberAlterations() {
        return copyNumberAlterations;
    }

    public void setCopyNumberAlterations(List<IndicatorQueryResp> copyNumberAlterations) {
        this.copyNumberAlterations = copyNumberAlterations;
    }

    public List<IndicatorQueryResp> getMutations() {
        return mutations;
    }

    public void setMutations(List<IndicatorQueryResp> mutations) {
        this.mutations = mutations;
    }
}