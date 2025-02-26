package org.mskcc.cbio.oncokb.model;

import org.mskcc.cbio.oncokb.apiModels.Implication;
import org.mskcc.cbio.oncokb.apiModels.MutationEffectResp;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

public class IndicatorQueryResp implements java.io.Serializable {
    private Query query;

    @ApiModelProperty(value = "Indicates whether the gene is curated by OncoKB")
    private Boolean geneExist = false;

    @ApiModelProperty(value = "Indicates whether an exact match for the queried variant is curated")
    private Boolean variantExist = false;

    @ApiModelProperty(value = "Indicates whether the alternate allele has been curated. See SOP Protocol 9.1")
    private Boolean alleleExist = false;

    @ApiModelProperty(value = "The oncogenicity status of the variant. Defaulted to \"Unknown\".", allowableValues = "Oncogenic, Likely Oncogenic, Likely Neutral, Inconclusive, Resistance, Unknown")
    private String oncogenic;

    private MutationEffectResp mutationEffect;

    @ApiModelProperty(value = "(Nullable) The highest sensitivity level from a list of therapeutic evidences.", allowableValues = "LEVEL_1, LEVEL_2, LEVEL_3A, LEVEL_3B, LEVEL_4")
    private LevelOfEvidence highestSensitiveLevel;

    @ApiModelProperty(value = "(Nullable) The highest resistance level from a list of therapeutic evidences.", allowableValues = "LEVEL_R1, LEVEL_R2")
    private LevelOfEvidence highestResistanceLevel;

    @ApiModelProperty(value = "(Nullable) The highest diagnostic level from a list of diagnostic evidences.", allowableValues = "LEVEL_Dx1, LEVEL_Dx2, LEVEL_Dx3.")
    private LevelOfEvidence highestDiagnosticImplicationLevel;

    @ApiModelProperty(value = "(Nullable) The highest prognostic level from a list of prognostic evidences.", allowableValues = "LEVEL_Px1, LEVEL_Px2, LEVEL_Px3")
    private LevelOfEvidence highestPrognosticImplicationLevel;

    @ApiModelProperty(value = "(Nullable) The highest FDA level from a list of therapeutic evidences.", allowableValues = "LEVEL_Fda1, LEVEL_Fda2, LEVEL_Fda3")
    private LevelOfEvidence highestFdaLevel;

    @ApiModelProperty(value = "DEPRECATED", allowableValues = "")
    private List<LevelOfEvidence> otherSignificantSensitiveLevels = new ArrayList<>();
    @ApiModelProperty(value = "DEPRECATED", allowableValues = "")
    private List<LevelOfEvidence> otherSignificantResistanceLevels = new ArrayList<>();

    @ApiModelProperty(value = "Indicated whether it is a variant of unknown significance (investigated and data not found). See SOP Sub-Protocol 3.2")
    private Boolean VUS = false;
    @ApiModelProperty(value = "Whether variant is recurrently found in cancer with statistical significance, as defined in Chang et al. (2017). See SOP Protocol 9.2")
    private Boolean hotspot = false;

    @ApiModelProperty(value = "Gene summary. Defaulted to \"\"")
    private String geneSummary = "";
    @ApiModelProperty(value = "Variant summary. Defaulted to \"\"")
    private String variantSummary = "";
    @ApiModelProperty(value = "Tumor type summary. Defaulted to \"\"")
    private String tumorTypeSummary = "";
    @ApiModelProperty(value = "Prognostic summary. Defaulted to \"\"")
    private String prognosticSummary = "";
    @ApiModelProperty(value = "Diagnostic summary. Defaulted to \"\"")
    private String diagnosticSummary = "";

    @ApiModelProperty(value = "List of diagnostic implications. Defaulted to empty list")
    private List<Implication> diagnosticImplications = new ArrayList<>();
    @ApiModelProperty(value = "List of prognostic implications. Defaulted to empty list")
    private List<Implication> prognosticImplications = new ArrayList<>();
    @ApiModelProperty(value = "List of therapeutic implications implications. Defaulted to empty list")
    private List<IndicatorQueryTreatment> treatments = new ArrayList<>();

    @ApiModelProperty(value = "OncoKB data version. See www.oncokb.org/news", example = "v4.25")
    private String dataVersion;
    @ApiModelProperty(value = "OncoKB data release date. Formatted as MM/DD/YYYY", example = "01/30/2025")
    private String lastUpdate;

    public IndicatorQueryResp() {
    }

    public IndicatorQueryResp copy() {
        IndicatorQueryResp newResp = new IndicatorQueryResp();
        newResp.setQuery(this.query.copy());
        newResp.setGeneExist(this.geneExist);
        newResp.setVariantExist(this.variantExist);
        newResp.setAlleleExist(this.alleleExist);
        newResp.setOncogenic(this.oncogenic);
        newResp.setMutationEffect(this.mutationEffect);
        newResp.setHighestSensitiveLevel(this.highestSensitiveLevel);
        newResp.setHighestResistanceLevel(this.highestResistanceLevel);
        newResp.setHighestDiagnosticImplicationLevel(this.highestDiagnosticImplicationLevel);
        newResp.setHighestPrognosticImplicationLevel(this.highestPrognosticImplicationLevel);
        newResp.setOtherSignificantSensitiveLevels(new ArrayList<>(this.otherSignificantSensitiveLevels));
        newResp.setOtherSignificantResistanceLevels(new ArrayList<>(this.otherSignificantResistanceLevels));
        newResp.setVUS(this.VUS);
        newResp.setHotspot(this.hotspot);
        newResp.setGeneSummary(this.geneSummary);
        newResp.setVariantSummary(this.variantSummary);
        newResp.setTumorTypeSummary(this.tumorTypeSummary);
        newResp.setPrognosticSummary(this.prognosticSummary);
        newResp.setDiagnosticSummary(this.diagnosticSummary);
        newResp.setDiagnosticImplications(new ArrayList<>(diagnosticImplications));
        newResp.setPrognosticImplications(new ArrayList<>(prognosticImplications));
        newResp.setTreatments(new ArrayList<>(treatments));
        newResp.setDataVersion(dataVersion);
        newResp.setLastUpdate(lastUpdate);
        return newResp;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Boolean getGeneExist() {
        return geneExist;
    }

    public void setGeneExist(Boolean geneExist) {
        this.geneExist = geneExist;
    }

    public Boolean getVariantExist() {
        return variantExist;
    }

    public void setVariantExist(Boolean variantExist) {
        this.variantExist = variantExist;
    }

    public String getOncogenic() {
        return oncogenic;
    }

    public void setOncogenic(String oncogenic) {
        this.oncogenic = oncogenic;
    }

    public MutationEffectResp getMutationEffect() {
        return mutationEffect;
    }

    public void setMutationEffect(MutationEffectResp mutationEffect) {
        this.mutationEffect = mutationEffect;
    }

    public LevelOfEvidence getHighestSensitiveLevel() {
        return highestSensitiveLevel;
    }

    public void setHighestSensitiveLevel(LevelOfEvidence highestSensitiveLevel) {
        this.highestSensitiveLevel = highestSensitiveLevel;
    }

    public LevelOfEvidence getHighestResistanceLevel() {
        return highestResistanceLevel;
    }

    public void setHighestResistanceLevel(LevelOfEvidence highestResistanceLevel) {
        this.highestResistanceLevel = highestResistanceLevel;
    }

    public LevelOfEvidence getHighestDiagnosticImplicationLevel() {
        return highestDiagnosticImplicationLevel;
    }

    public void setHighestDiagnosticImplicationLevel(LevelOfEvidence highestDiagnosticImplicationLevel) {
        this.highestDiagnosticImplicationLevel = highestDiagnosticImplicationLevel;
    }

    public LevelOfEvidence getHighestPrognosticImplicationLevel() {
        return highestPrognosticImplicationLevel;
    }

    public void setHighestPrognosticImplicationLevel(LevelOfEvidence highestPrognosticImplicationLevel) {
        this.highestPrognosticImplicationLevel = highestPrognosticImplicationLevel;
    }

    public LevelOfEvidence getHighestFdaLevel() {
        return highestFdaLevel;
    }

    public void setHighestFdaLevel(LevelOfEvidence highestFdaLevel) {
        this.highestFdaLevel = highestFdaLevel;
    }

    public List<LevelOfEvidence> getOtherSignificantSensitiveLevels() {
        return otherSignificantSensitiveLevels;
    }

    public void setOtherSignificantSensitiveLevels(List<LevelOfEvidence> otherSignificantSensitiveLevels) {
        this.otherSignificantSensitiveLevels = otherSignificantSensitiveLevels;
    }

    public List<LevelOfEvidence> getOtherSignificantResistanceLevels() {
        return otherSignificantResistanceLevels;
    }

    public void setOtherSignificantResistanceLevels(List<LevelOfEvidence> otherSignificantResistanceLevels) {
        this.otherSignificantResistanceLevels = otherSignificantResistanceLevels;
    }

    public Boolean getVUS() {
        return VUS;
    }

    public void setVUS(Boolean VUS) {
        this.VUS = VUS;
    }

    public Boolean getHotspot() {
        return hotspot;
    }

    public void setHotspot(Boolean hotspot) {
        this.hotspot = hotspot;
    }

    public Boolean getAlleleExist() {
        return alleleExist;
    }

    public void setAlleleExist(Boolean alleleExist) {
        this.alleleExist = alleleExist;
    }

    public String getGeneSummary() {
        return geneSummary;
    }

    public void setGeneSummary(String geneSummary) {
        this.geneSummary = geneSummary;
    }

    public String getVariantSummary() {
        return variantSummary;
    }

    public void setVariantSummary(String variantSummary) {
        this.variantSummary = variantSummary;
    }

    public String getTumorTypeSummary() {
        return tumorTypeSummary;
    }

    public void setTumorTypeSummary(String tumorTypeSummary) {
        this.tumorTypeSummary = tumorTypeSummary;
    }

    public String getPrognosticSummary() {
        return prognosticSummary;
    }

    public void setPrognosticSummary(String prognosticSummary) {
        this.prognosticSummary = prognosticSummary;
    }

    public String getDiagnosticSummary() {
        return diagnosticSummary;
    }

    public void setDiagnosticSummary(String diagnosticSummary) {
        this.diagnosticSummary = diagnosticSummary;
    }

    public List<Implication> getDiagnosticImplications() {
        return diagnosticImplications;
    }

    public void setDiagnosticImplications(List<Implication> diagnosticImplications) {
        this.diagnosticImplications = diagnosticImplications;
    }

    public List<Implication> getPrognosticImplications() {
        return prognosticImplications;
    }

    public void setPrognosticImplications(List<Implication> prognosticImplications) {
        this.prognosticImplications = prognosticImplications;
    }

    public List<IndicatorQueryTreatment> getTreatments() {
        return treatments;
    }

    public void setTreatments(List<IndicatorQueryTreatment> treatments) {
        this.treatments = treatments;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}


