package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class MainNumber {

    private Integer gene = null;
    private Integer alteration = null;
    private Integer tumorType = null;
    private Integer drug = null;
    private List<MainNumberLevel> level = new ArrayList<MainNumberLevel>();


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("gene")
    public Integer getGene() {
        return gene;
    }

    public void setGene(Integer gene) {
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
    @JsonProperty("drug")
    public Integer getDrug() {
        return drug;
    }

    public void setDrug(Integer drug) {
        this.drug = drug;
    }


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("level")
    public List<MainNumberLevel> getLevel() {
        return level;
    }

    public void setLevel(List<MainNumberLevel> level) {
        this.level = level;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MainNumber mainNumber = (MainNumber) o;
        return Objects.equals(gene, mainNumber.gene) &&
            Objects.equals(alteration, mainNumber.alteration) &&
            Objects.equals(tumorType, mainNumber.tumorType) &&
            Objects.equals(drug, mainNumber.drug) &&
            Objects.equals(level, mainNumber.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gene, alteration, tumorType, drug, level);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MainNumber {\n");

        sb.append("  gene: ").append(gene).append("\n");
        sb.append("  alteration: ").append(alteration).append("\n");
        sb.append("  tumorType: ").append(tumorType).append("\n");
        sb.append("  drug: ").append(drug).append("\n");
        sb.append("  level: ").append(level).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
