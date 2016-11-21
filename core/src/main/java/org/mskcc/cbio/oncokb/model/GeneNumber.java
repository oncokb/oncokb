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
    private String highestLevel = null;

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


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("highestLevel")
    public String getHighestLevel() {
        return highestLevel;
    }

    public void setHighestLevel(String highestLevel) {
        this.highestLevel = highestLevel;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneNumber that = (GeneNumber) o;

        if (gene != null ? !gene.equals(that.gene) : that.gene != null) return false;
        if (alteration != null ? !alteration.equals(that.alteration) : that.alteration != null) return false;
        if (tumorType != null ? !tumorType.equals(that.tumorType) : that.tumorType != null) return false;
        if (highestLevel != null ? !highestLevel.equals(that.highestLevel) : that.highestLevel != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = gene != null ? gene.hashCode() : 0;
        result = 31 * result + (alteration != null ? alteration.hashCode() : 0);
        result = 31 * result + (tumorType != null ? tumorType.hashCode() : 0);
        result = 31 * result + (highestLevel != null ? highestLevel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GeneNumber{" +
            "gene=" + gene +
            ", alteration=" + alteration +
            ", tumorType=" + tumorType +
            ", highestLevel='" + highestLevel + '\'' +
            '}';
    }
}
