package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class GeneNumber {

    private Gene gene = null;
    private Integer alteration = null;
    private Integer tumorType = null;
    private String highestSensitiveLevel = null;
    private String highestResistanceLevel = null;

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("gene")
    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("alteration")
    public Integer getAlteration() {
        return alteration;
    }

    public void setAlteration(Integer alteration) {
        this.alteration = alteration;
    }


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("tumorType")
    public Integer getTumorType() {
        return tumorType;
    }

    public void setTumorType(Integer tumorType) {
        this.tumorType = tumorType;
    }


    public String getHighestSensitiveLevel() {
        return highestSensitiveLevel;
    }

    public void setHighestSensitiveLevel(String highestSensitiveLevel) {
        this.highestSensitiveLevel = highestSensitiveLevel;
    }

    public String getHighestResistanceLevel() {
        return highestResistanceLevel;
    }

    public void setHighestResistanceLevel(String highestResistanceLevel) {
        this.highestResistanceLevel = highestResistanceLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneNumber)) return false;

        GeneNumber that = (GeneNumber) o;

        if (getGene() != null ? !getGene().equals(that.getGene()) : that.getGene() != null) return false;
        if (getAlteration() != null ? !getAlteration().equals(that.getAlteration()) : that.getAlteration() != null)
            return false;
        if (getTumorType() != null ? !getTumorType().equals(that.getTumorType()) : that.getTumorType() != null)
            return false;
        if (getHighestSensitiveLevel() != null ? !getHighestSensitiveLevel().equals(that.getHighestSensitiveLevel()) : that.getHighestSensitiveLevel() != null)
            return false;
        if (getHighestResistenceLevel() != null ? !getHighestResistenceLevel().equals(that.getHighestResistenceLevel()) : that.getHighestResistenceLevel() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getGene() != null ? getGene().hashCode() : 0;
        result = 31 * result + (getAlteration() != null ? getAlteration().hashCode() : 0);
        result = 31 * result + (getTumorType() != null ? getTumorType().hashCode() : 0);
        result = 31 * result + (getHighestSensitiveLevel() != null ? getHighestSensitiveLevel().hashCode() : 0);
        result = 31 * result + (getHighestResistenceLevel() != null ? getHighestResistenceLevel().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GeneNumber{" +
            "gene=" + gene +
            ", alteration=" + alteration +
            ", tumorType=" + tumorType +
            ", highestSensitiveLevel='" + highestSensitiveLevel + '\'' +
            ", highestResistanceLevel='" + highestResistanceLevel + '\'' +
            '}';
    }
}
