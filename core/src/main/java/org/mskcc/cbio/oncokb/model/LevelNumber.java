package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class LevelNumber {

    private Set<Gene> genes = null;
    private LevelOfEvidence level = null;

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("genes")
    public Set<Gene> getGenes() {
        return genes;
    }

    public void setGenes(Set<Gene> genes) {
        this.genes = genes;
    }
    
    
    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("level")
    public LevelOfEvidence getLevel() {
        return level;
    }

    public void setLevel(LevelOfEvidence level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LevelNumber that = (LevelNumber) o;

        if (genes != null ? !genes.equals(that.genes) : that.genes != null) return false;
        if (level != that.level) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = genes != null ? genes.hashCode() : 0;
        result = 31 * result + (level != null ? level.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LevelNumber{" +
            "genes=" + genes +
            ", level=" + level +
            '}';
    }
}
