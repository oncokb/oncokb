package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

/**
 * @author Selcuk Onur Sumer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotspotMutation {
    private String hugoSymbol;
    private String residue;
    private Map<String, Integer> variantAminoAcid;
    private Integer tumorTypeCount;
    private Integer tumorCount;
    private Map<String, Integer> tumorTypeComposition;
    private IntegerRange aminoAcidPosition;

    @ApiModelProperty(value = "Hugo gene symbol", required = true)
    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    @ApiModelProperty(value = "Residue", required = true)
    public String getResidue() {
        return residue;
    }

    public void setResidue(String residue) {
        this.residue = residue;
    }

    @ApiModelProperty(value = "Variant Amino Acid", required = true)
    public Map<String, Integer> getVariantAminoAcid() {
        return variantAminoAcid;
    }

    public void setVariantAminoAcid(Map<String, Integer> variantAminoAcid) {
        this.variantAminoAcid = variantAminoAcid;
    }

    @ApiModelProperty(value = "Number of Tumors", required = true)
    public Integer getTumorCount() {
        return tumorCount;
    }

    public void setTumorCount(Integer tumorCount) {
        this.tumorCount = tumorCount;
    }

    @ApiModelProperty(value = "Tumor Type Composition", required = true)
    public Map<String, Integer> getTumorTypeComposition() {
        return tumorTypeComposition;
    }

    public void setTumorTypeComposition(Map<String, Integer> tumorTypeComposition) {
        this.tumorTypeComposition = tumorTypeComposition;
    }

    @ApiModelProperty(value = "Number of Distinct Tumor Types", required = false)
    public Integer getTumorTypeCount() {
        return tumorTypeCount;
    }

    public void setTumorTypeCount(Integer tumorTypeCount) {
        this.tumorTypeCount = tumorTypeCount;
    }

    @ApiModelProperty(value = "Amino Acid Position", required = false)
    public IntegerRange getAminoAcidPosition() {
        return aminoAcidPosition;
    }

    public void setAminoAcidPosition(IntegerRange aminoAcidPosition) {
        this.aminoAcidPosition = aminoAcidPosition;
    }

}
