package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-10T02:49:36.208Z")
public class ClinicalVariant {

    private Set<TumorType> cancerTypes = new HashSet<>();
    private Set<TumorType> excludedCancerTypes = new HashSet<>();
    private Alteration variant = null;
    private String oncogenic = null;
    private String level = null;
    private String fdaLevel = null;
    private String solidPropagationLevel = "";
    private String liquidPropagationLevel = "";
    private Set<String> drug = new HashSet<>();
    private Set<String> drugPmids = new HashSet<>();
    private Set<ArticleAbstract> drugAbstracts = new HashSet<>();
    private String drugDescription = "";


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("variant")
    public Alteration getVariant() {
        return variant;
    }

    public void setVariant(Alteration variant) {
        this.variant = variant;
    }


    public String getOncogenic() {
        return oncogenic;
    }

    public void setOncogenic(String oncogenic) {
        this.oncogenic = oncogenic;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("cancerTypes")
    public Set<TumorType> getCancerTypes() {
        return cancerTypes;
    }

    public void setCancerTypes(Set<TumorType> cancerTypes) {
        this.cancerTypes = cancerTypes;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("excludedCancerTypes")
    public Set<TumorType> getExcludedCancerTypes() {
        return excludedCancerTypes;
    }

    public void setExcludedCancerTypes(Set<TumorType> excludedCancerTypes) {
        this.excludedCancerTypes = excludedCancerTypes;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("level")
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("fdaLevel")
    public String getFdaLevel() {
        return fdaLevel;
    }

    public void setFdaLevel(String fdaLevel) {
        this.fdaLevel = fdaLevel;
    }

    public String getSolidPropagationLevel() {
        return solidPropagationLevel;
    }

    public void setSolidPropagationLevel(String solidPropagationLevel) {
        this.solidPropagationLevel = solidPropagationLevel;
    }

    public String getLiquidPropagationLevel() {
        return liquidPropagationLevel;
    }

    public void setLiquidPropagationLevel(String liquidPropagationLevel) {
        this.liquidPropagationLevel = liquidPropagationLevel;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("drug")
    public Set<String> getDrug() {
        return drug;
    }

    public void setDrug(Set<String> drug) {
        this.drug = drug;
    }


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("drugPmids")
    public Set<String> getDrugPmids() {
        return drugPmids;
    }

    public void setDrugPmids(Set<String> drugPmids) {
        this.drugPmids = drugPmids;
    }


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("drugAbstracts")
    public Set<ArticleAbstract> getDrugAbstracts() {
        return drugAbstracts;
    }

    public void setDrugAbstracts(Set<ArticleAbstract> drugAbstracts) {
        this.drugAbstracts = drugAbstracts;
    }


    public String getDrugDescription() {
        return drugDescription;
    }

    public void setDrugDescription(String drugDescription) {
        this.drugDescription = drugDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClinicalVariant)) return false;
        ClinicalVariant that = (ClinicalVariant) o;
        return Objects.equals(getCancerTypes(), that.getCancerTypes()) &&
            Objects.equals(getVariant(), that.getVariant()) &&
            Objects.equals(getOncogenic(), that.getOncogenic()) &&
            Objects.equals(getLevel(), that.getLevel()) &&
            Objects.equals(getFdaLevel(), that.getFdaLevel()) &&
            Objects.equals(getSolidPropagationLevel(), that.getSolidPropagationLevel()) &&
            Objects.equals(getLiquidPropagationLevel(), that.getLiquidPropagationLevel()) &&
            Objects.equals(getDrug(), that.getDrug()) &&
            Objects.equals(getDrugPmids(), that.getDrugPmids()) &&
            Objects.equals(getDrugAbstracts(), that.getDrugAbstracts()) &&
            Objects.equals(getDrugDescription(), that.getDrugDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCancerTypes(), getVariant(), getOncogenic(), getLevel(), getDrug(), getDrugPmids(), getDrugAbstracts(), getDrugDescription());
    }

    @Override
    public String toString() {
        return "ClinicalVariant{" +
            "cancerTypes=" + cancerTypes +
            ", variant=" + variant +
            ", oncogenic='" + oncogenic + '\'' +
            ", level='" + level + '\'' +
            ", fdaLevel='" + fdaLevel + '\'' +
            ", solidPropagationLevel='" + solidPropagationLevel + '\'' +
            ", liquidPropagationLevel='" + liquidPropagationLevel + '\'' +
            ", drug=" + drug +
            ", drugPmids=" + drugPmids +
            ", drugAbstracts=" + drugAbstracts +
            ", drugDescription=" + drugDescription +
            '}';
    }
}
