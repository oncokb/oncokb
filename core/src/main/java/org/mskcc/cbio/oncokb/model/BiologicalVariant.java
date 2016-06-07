package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-10T02:49:36.208Z")
public class BiologicalVariant {

    private String variant = null;
    private String mutationEffect = null;
    private Set<String> mutationEffectPmids = new HashSet<>();
    private String oncogenic = null;
    private Set<String> oncogenicPmids = new HashSet<>();


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("variant")
    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("mutationEffect")
    public String getMutationEffect() {
        return mutationEffect;
    }

    public void setMutationEffect(String mutationEffect) {
        this.mutationEffect = mutationEffect;
    }


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("mutationEffectPmids")
    public Set<String> getMutationEffectPmids() {
        return mutationEffectPmids;
    }

    public void setMutationEffectPmids(Set<String> mutationEffectPmids) {
        this.mutationEffectPmids = mutationEffectPmids;
    }


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("oncogenic")
    public String getOncogenic() {
        return oncogenic;
    }

    public void setOncogenic(String oncogenic) {
        this.oncogenic = oncogenic;
    }


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("oncogenicPmids")
    public Set<String> getOncogenicPmids() {
        return oncogenicPmids;
    }

    public void setOncogenicPmids(Set<String> oncogenicPmids) {
        this.oncogenicPmids = oncogenicPmids;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BiologicalVariant biologicalVariant = (BiologicalVariant) o;
        return Objects.equals(variant, biologicalVariant.variant) &&
            Objects.equals(mutationEffect, biologicalVariant.mutationEffect) &&
            Objects.equals(mutationEffectPmids, biologicalVariant.mutationEffectPmids) &&
            Objects.equals(oncogenic, biologicalVariant.oncogenic) &&
            Objects.equals(oncogenicPmids, biologicalVariant.oncogenicPmids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variant, mutationEffect, mutationEffectPmids, oncogenic, oncogenicPmids);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BiologicalVariant {\n");

        sb.append("  variant: ").append(variant).append("\n");
        sb.append("  mutationEffect: ").append(mutationEffect).append("\n");
        sb.append("  mutationEffectPmids: ").append(mutationEffectPmids).append("\n");
        sb.append("  oncogenic: ").append(oncogenic).append("\n");
        sb.append("  oncogenicPmids: ").append(oncogenicPmids).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
