package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;

import java.util.HashSet;
import java.util.Set;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-10T02:49:36.208Z")
public class ClinicalVariant {

    private TumorType oncoTreeType = null;
    private Alteration variant = null;
    private String oncogenic = null;
    private String level = null;
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
    @JsonProperty("cancerType")
    public TumorType getOncoTreeType() {
        return oncoTreeType;
    }

    public void setOncoTreeType(TumorType oncoTreeType) {
        this.oncoTreeType = oncoTreeType;
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
        if (o == null || getClass() != o.getClass()) return false;

        ClinicalVariant that = (ClinicalVariant) o;

        if (oncoTreeType != null ? !oncoTreeType.equals(that.oncoTreeType) : that.oncoTreeType != null) return false;
        if (variant != null ? !variant.equals(that.variant) : that.variant != null) return false;
        if (level != null ? !level.equals(that.level) : that.level != null) return false;
        if (drug != null ? !drug.equals(that.drug) : that.drug != null) return false;
        if (drugPmids != null ? !drugPmids.equals(that.drugPmids) : that.drugPmids != null) return false;
        if (drugAbstracts != null ? !drugAbstracts.equals(that.drugAbstracts) : that.drugAbstracts != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = oncoTreeType != null ? oncoTreeType.hashCode() : 0;
        result = 31 * result + (variant != null ? variant.hashCode() : 0);
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + (drug != null ? drug.hashCode() : 0);
        result = 31 * result + (drugPmids != null ? drugPmids.hashCode() : 0);
        result = 31 * result + (drugAbstracts != null ? drugAbstracts.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClinicalVariant{" +
            "oncoTreeType=" + oncoTreeType +
            ", variant=" + variant +
            ", level='" + level + '\'' +
            ", drug=" + drug +
            ", drugPmids=" + drugPmids +
            ", drugAbstracts=" + drugAbstracts +
            '}';
    }
}
