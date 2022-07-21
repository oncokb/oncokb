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

    private Set<TumorType> cancerTypes = null;
    private Alteration variant = null;
    private String oncogenic = null;
    private String level = null;
    private String fdaLevel = null;
    private Set<String> drug = new HashSet<String>();
    private Set<String> drugPmids = new HashSet<String>();
    private Set<ArticleAbstract> drugAbstracts = new HashSet<>();


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
            Objects.equals(getDrug(), that.getDrug()) &&
            Objects.equals(getDrugPmids(), that.getDrugPmids()) &&
            Objects.equals(getDrugAbstracts(), that.getDrugAbstracts());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCancerTypes(), getVariant(), getOncogenic(), getLevel(), getDrug(), getDrugPmids(), getDrugAbstracts());
    }

    @Override
    public String toString() {
        return "ClinicalVariant{" +
            "cancerTypes=" + cancerTypes +
            ", variant=" + variant +
            ", oncogenic='" + oncogenic + '\'' +
            ", level='" + level + '\'' +
            ", fdaLevel='" + fdaLevel + '\'' +
            ", drug=" + drug +
            ", drugPmids=" + drugPmids +
            ", drugAbstracts=" + drugAbstracts +
            '}';
    }
}
