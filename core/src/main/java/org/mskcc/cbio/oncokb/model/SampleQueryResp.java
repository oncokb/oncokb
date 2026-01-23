package org.mskcc.cbio.oncokb.model;

import java.util.List;

public class SampleQueryResp implements java.io.Serializable {
    private String id;
    private String tumorType;
    private List<SomaticIndicatorQueryResp> structuralVariants;
    private List<SomaticIndicatorQueryResp> copyNumberAlterations;
    private List<SomaticIndicatorQueryResp> mutations;

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

    public List<SomaticIndicatorQueryResp> getStructuralVariants() {
        return structuralVariants;
    }

    public void setStructuralVariants(List<SomaticIndicatorQueryResp> structuralVariants) {
        this.structuralVariants = structuralVariants;
    }

    public List<SomaticIndicatorQueryResp> getCopyNumberAlterations() {
        return copyNumberAlterations;
    }

    public void setCopyNumberAlterations(List<SomaticIndicatorQueryResp> copyNumberAlterations) {
        this.copyNumberAlterations = copyNumberAlterations;
    }

    public List<SomaticIndicatorQueryResp> getMutations() {
        return mutations;
    }

    public void setMutations(List<SomaticIndicatorQueryResp> mutations) {
        this.mutations = mutations;
    }
}