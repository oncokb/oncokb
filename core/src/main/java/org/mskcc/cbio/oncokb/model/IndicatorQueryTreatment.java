package org.mskcc.cbio.oncokb.model;


import org.mskcc.cbio.oncokb.apiModels.TumorType;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IndicatorQueryTreatment implements java.io.Serializable {
    @ApiModelProperty(value = "List of alterations associated with therapeutic implication")
    private List<String> alterations = new ArrayList<>();

    @ApiModelProperty(value = "List of drugs associated with therapeutic implication")
    private List<Drug> drugs = new ArrayList<>(0);

    // Indication is no longer supported on curation platform. We may still have
    // lingering indication strings that we should remove from database.
    @ApiModelProperty(value = "DEPRECATED")
    private Set<String> approvedIndications = new HashSet<>(0);

    @ApiModelProperty(value = "Therapeutic level associated with implication")
    private LevelOfEvidence level;

    @ApiModelProperty(value = "FDA level associated with implication")
    private LevelOfEvidence fdaLevel;

    @ApiModelProperty(value = "Cancer type associated with level of evidence")
    private TumorType levelAssociatedCancerType;
    @ApiModelProperty(value = "Excluded cancer types. Defaulted to empty list")
    private Set<TumorType> levelExcludedCancerTypes = new HashSet<>();

    @ApiModelProperty(value = "List of PubMed IDs cited in the treatment description. Defaulted to empty list")
    private Set<String> pmids = new HashSet<>(0);
    @ApiModelProperty(value = "List of abstracts cited in the treatment description. Defaulted to empty list")
    private Set<ArticleAbstract> abstracts = new HashSet<>(0);

    @ApiModelProperty(value = "Treatment description. Defaulted to \"\"")
    private String description = "";

    public IndicatorQueryTreatment() {
    }

    public List<String> getAlterations() {
        return alterations;
    }

    public void setAlterations(List<String> alterations) {
        this.alterations = alterations;
    }

    public TumorType getLevelAssociatedCancerType() {
        return levelAssociatedCancerType;
    }

    public void setLevelAssociatedCancerType(TumorType levelAssociatedCancerType) {
        this.levelAssociatedCancerType = levelAssociatedCancerType;
    }

    public Set<TumorType> getLevelExcludedCancerTypes() {
        return levelExcludedCancerTypes;
    }

    public void setLevelExcludedCancerTypes(Set<TumorType> levelExcludedCancerTypes) {
        this.levelExcludedCancerTypes = levelExcludedCancerTypes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Drug> getDrugs() {
        return drugs;
    }

    public void setDrugs(List<Drug> drugs) {
        this.drugs = drugs;
    }

    public Set<String> getApprovedIndications() {
        return approvedIndications;
    }

    public void setApprovedIndications(Set<String> approvedIndications) {
        this.approvedIndications = approvedIndications;
    }

    public LevelOfEvidence getLevel() {
        return level;
    }

    public void setLevel(LevelOfEvidence level) {
        this.level = level;
    }

    public LevelOfEvidence getFdaLevel() {
        return fdaLevel;
    }

    public void setFdaLevel(LevelOfEvidence fdaLevel) {
        this.fdaLevel = fdaLevel;
    }

    public Set<String> getPmids() {
        return pmids;
    }

    public void setPmids(Set<String> pmids) {
        this.pmids = pmids;
    }

    public Set<ArticleAbstract> getAbstracts() {
        return abstracts;
    }

    public void setAbstracts(Set<ArticleAbstract> abstracts) {
        this.abstracts = abstracts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndicatorQueryTreatment)) return false;

        IndicatorQueryTreatment treatment = (IndicatorQueryTreatment) o;

        if (getDrugs() != null ? !getDrugs().equals(treatment.getDrugs()) : treatment.getDrugs() != null) return false;
        if (getApprovedIndications() != null ? !getApprovedIndications().equals(treatment.getApprovedIndications()) : treatment.getApprovedIndications() != null)
            return false;
        if (getLevel() != treatment.getLevel()) return false;
        if (getPmids() != null ? !getPmids().equals(treatment.getPmids()) : treatment.getPmids() != null) return false;
        return getAbstracts() != null ? getAbstracts().equals(treatment.getAbstracts()) : treatment.getAbstracts() == null;

    }

    @Override
    public int hashCode() {
        int result = getDrugs() != null ? getDrugs().hashCode() : 0;
        result = 31 * result + (getApprovedIndications() != null ? getApprovedIndications().hashCode() : 0);
        result = 31 * result + (getLevel() != null ? getLevel().hashCode() : 0);
        result = 31 * result + (getPmids() != null ? getPmids().hashCode() : 0);
        result = 31 * result + (getAbstracts() != null ? getAbstracts().hashCode() : 0);
        return result;
    }
}


