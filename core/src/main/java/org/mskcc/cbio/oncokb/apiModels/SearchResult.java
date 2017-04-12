package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Hongxin on 4/12/17.
 */
public class SearchResult {
    private Query query;
    private Boolean geneAnnotated;
    private Boolean variantAnnotated;
    private Boolean alterativeVariantAlleleAnnotated;
    private KnownEffect oncogenic;
    private KnownEffect mutationEffect;
    private LevelOfEvidenceWithTime highestSensitiveLevel;
    private LevelOfEvidenceWithTime highestResistanceLevel;
    private List<LevelOfEvidenceWithTime> otherSignificantSensitiveLevels;
    private List<LevelOfEvidenceWithTime> otherSignificantResistanceLevels;
    private Summary geneSummary;
    private Summary variantSummary;
    private Summary tumorTypeSummary;
    private List<TreatmentInfo> treatments = new ArrayList<>();
    private VUSStatus VUS;
    private OtherSources otherSources;
    private String dataVersion;
    private Date lastUpdate;

    public SearchResult() {
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Boolean getGeneAnnotated() {
        return geneAnnotated;
    }

    public void setGeneAnnotated(Boolean geneAnnotated) {
        this.geneAnnotated = geneAnnotated;
    }

    public Boolean getVariantAnnotated() {
        return variantAnnotated;
    }

    public void setVariantAnnotated(Boolean variantAnnotated) {
        this.variantAnnotated = variantAnnotated;
    }

    public Boolean getAlterativeVariantAlleleAnnotated() {
        return alterativeVariantAlleleAnnotated;
    }

    public void setAlterativeVariantAlleleAnnotated(Boolean alterativeVariantAlleleAnnotated) {
        this.alterativeVariantAlleleAnnotated = alterativeVariantAlleleAnnotated;
    }

    public KnownEffect getOncogenic() {
        return oncogenic;
    }

    public void setOncogenic(KnownEffect oncogenic) {
        this.oncogenic = oncogenic;
    }

    public KnownEffect getMutationEffect() {
        return mutationEffect;
    }

    public void setMutationEffect(KnownEffect mutationEffect) {
        this.mutationEffect = mutationEffect;
    }

    public LevelOfEvidenceWithTime getHighestSensitiveLevel() {
        return highestSensitiveLevel;
    }

    public void setHighestSensitiveLevel(LevelOfEvidenceWithTime highestSensitiveLevel) {
        this.highestSensitiveLevel = highestSensitiveLevel;
    }

    public LevelOfEvidenceWithTime getHighestResistanceLevel() {
        return highestResistanceLevel;
    }

    public void setHighestResistanceLevel(LevelOfEvidenceWithTime highestResistanceLevel) {
        this.highestResistanceLevel = highestResistanceLevel;
    }

    public List<LevelOfEvidenceWithTime> getOtherSignificantSensitiveLevels() {
        return otherSignificantSensitiveLevels;
    }

    public void setOtherSignificantSensitiveLevels(List<LevelOfEvidenceWithTime> otherSignificantSensitiveLevels) {
        this.otherSignificantSensitiveLevels = otherSignificantSensitiveLevels;
    }

    public List<LevelOfEvidenceWithTime> getOtherSignificantResistanceLevels() {
        return otherSignificantResistanceLevels;
    }

    public void setOtherSignificantResistanceLevels(List<LevelOfEvidenceWithTime> otherSignificantResistanceLevels) {
        this.otherSignificantResistanceLevels = otherSignificantResistanceLevels;
    }

    public Summary getGeneSummary() {
        return geneSummary;
    }

    public void setGeneSummary(Summary geneSummary) {
        this.geneSummary = geneSummary;
    }

    public Summary getVariantSummary() {
        return variantSummary;
    }

    public void setVariantSummary(Summary variantSummary) {
        this.variantSummary = variantSummary;
    }

    public Summary getTumorTypeSummary() {
        return tumorTypeSummary;
    }

    public void setTumorTypeSummary(Summary tumorTypeSummary) {
        this.tumorTypeSummary = tumorTypeSummary;
    }

    public List<TreatmentInfo> getTreatments() {
        return treatments;
    }

    public void setTreatments(List<TreatmentInfo> treatments) {
        this.treatments = treatments;
    }

    public VUSStatus getVUS() {
        return VUS;
    }

    public void setVUS(VUSStatus VUS) {
        this.VUS = VUS;
    }

    public OtherSources getOtherSources() {
        return otherSources;
    }

    public void setOtherSources(OtherSources otherSources) {
        this.otherSources = otherSources;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
