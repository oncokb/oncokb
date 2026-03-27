package org.mskcc.cbio.oncokb.model;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

public class SomaticIndicatorQueryResp extends IndicatorQueryRespBase {
    @ApiModelProperty(value = "The oncogenicity status of the variant. Defaulted to \"Unknown\".", allowableValues = "Oncogenic, Likely Oncogenic, Likely Neutral, Inconclusive, Resistance, Unknown")
    private String oncogenic;

    @ApiModelProperty(value = "Whether variant is recurrently found in cancer with statistical significance, as defined in Chang et al. (2017). See SOP Protocol 9.2")
    private Boolean hotspot = false;

    @ApiModelProperty(value = "(Nullable) The highest FDA level from a list of therapeutic evidences.", allowableValues = "LEVEL_Fda1, LEVEL_Fda2, LEVEL_Fda3")
    private LevelOfEvidence highestFdaLevel;

    @ApiModelProperty(value = "DEPRECATED", allowableValues = "")
    private List<LevelOfEvidence> otherSignificantSensitiveLevels = new ArrayList<>();

    @ApiModelProperty(value = "DEPRECATED", allowableValues = "")
    private List<LevelOfEvidence> otherSignificantResistanceLevels = new ArrayList<>();

    @Deprecated
    @ApiModelProperty(value = "DEPRECATED. (Nullable) The affected exon of this variant, if applicable (currently only supported when annotating via HGVSg or Genomic Location)")
    private String exon;

    public SomaticIndicatorQueryResp() {
        super();
    }

    @Override
    public SomaticIndicatorQueryResp copy() {
        SomaticIndicatorQueryResp newResp = new SomaticIndicatorQueryResp();
        newResp.setQuery(this.getQuery() == null ? null : this.getQuery().copy());
        newResp.setGeneExist(this.getGeneExist());
        newResp.setVariantExist(this.getVariantExist());
        newResp.setAlleleExist(this.getAlleleExist());
        newResp.setMutationEffect(this.getMutationEffect());
        newResp.setHighestSensitiveLevel(this.getHighestSensitiveLevel());
        newResp.setHighestResistanceLevel(this.getHighestResistanceLevel());
        newResp.setHighestDiagnosticImplicationLevel(this.getHighestDiagnosticImplicationLevel());
        newResp.setHighestPrognosticImplicationLevel(this.getHighestPrognosticImplicationLevel());
        newResp.setHighestFdaLevel(this.getHighestFdaLevel());
        newResp.setOtherSignificantSensitiveLevels(new java.util.ArrayList<>(this.getOtherSignificantSensitiveLevels()));
        newResp.setOtherSignificantResistanceLevels(new java.util.ArrayList<>(this.getOtherSignificantResistanceLevels()));
        newResp.setVUS(this.getVUS());
        newResp.setExon(this.getExon());
        newResp.setGeneSummary(this.getGeneSummary());
        newResp.setVariantSummary(this.getVariantSummary());
        newResp.setTumorTypeSummary(this.getTumorTypeSummary());
        newResp.setPrognosticSummary(this.getPrognosticSummary());
        newResp.setDiagnosticSummary(this.getDiagnosticSummary());
        newResp.setDiagnosticImplications(new java.util.ArrayList<>(this.getDiagnosticImplications()));
        newResp.setPrognosticImplications(new java.util.ArrayList<>(this.getPrognosticImplications()));
        newResp.setTreatments(new java.util.ArrayList<>(this.getTreatments()));
        newResp.setDataVersion(this.getDataVersion());
        newResp.setLastUpdate(this.getLastUpdate());
        newResp.setOncogenic(this.oncogenic);
        newResp.setHotspot(this.hotspot);
        return newResp;
    }

    public String getOncogenic() {
        return oncogenic;
    }

    public void setOncogenic(String oncogenic) {
        this.oncogenic = oncogenic;
    }

    public Boolean getHotspot() {
        return hotspot;
    }

    public void setHotspot(Boolean hotspot) {
        this.hotspot = hotspot;
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

    @Deprecated
    public String getExon() {
        return exon;
    }

    @Deprecated
    public void setExon(String exon) {
        this.exon = exon;
    }
}
