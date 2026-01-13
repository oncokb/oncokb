package org.mskcc.cbio.oncokb.model;

import io.swagger.annotations.ApiModelProperty;

public class SomaticIndicatorQueryResp extends IndicatorQueryRespBase {
    @ApiModelProperty(value = "The oncogenicity status of the variant. Defaulted to \"Unknown\".", allowableValues = "Oncogenic, Likely Oncogenic, Likely Neutral, Inconclusive, Resistance, Unknown")
    private String oncogenic;

    @ApiModelProperty(value = "Whether variant is recurrently found in cancer with statistical significance, as defined in Chang et al. (2017). See SOP Protocol 9.2")
    private Boolean hotspot = false;

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
}
