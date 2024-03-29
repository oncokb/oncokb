package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-10T02:49:36.208Z")
public class BiologicalVariant {

    private Alteration variant = null;
    private String mutationEffect = null;
    private Set<String> mutationEffectPmids = new HashSet<>();
    private Set<ArticleAbstract> mutationEffectAbstracts = new HashSet<>();
    private String mutationEffectDescription = null;
    private String oncogenic = null;
    private Set<String> oncogenicPmids = new HashSet<>();
    private Set<ArticleAbstract> oncogenicAbstracts = new HashSet<>();


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

    public String getMutationEffectDescription() {
        return mutationEffectDescription;
    }

    public void setMutationEffectDescription(String mutationEffectDescription) {
        this.mutationEffectDescription = mutationEffectDescription;
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

    @ApiModelProperty(value = "")
    @JsonProperty("mutationEffectAbstracts")
    public Set<ArticleAbstract> getMutationEffectAbstracts() {
        return mutationEffectAbstracts;
    }

    public void setMutationEffectAbstracts(Set<ArticleAbstract> mutationEffectAbstracts) {
        this.mutationEffectAbstracts = mutationEffectAbstracts;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("oncogenicAbstracts")
    public Set<ArticleAbstract> getOncogenicAbstracts() {
        return oncogenicAbstracts;
    }

    public void setOncogenicAbstracts(Set<ArticleAbstract> oncogenicAbstracts) {
        this.oncogenicAbstracts = oncogenicAbstracts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BiologicalVariant that = (BiologicalVariant) o;

        if (variant != null ? !variant.equals(that.variant) : that.variant != null) return false;
        if (mutationEffect != null ? !mutationEffect.equals(that.mutationEffect) : that.mutationEffect != null)
            return false;
        if (mutationEffectPmids != null ? !mutationEffectPmids.equals(that.mutationEffectPmids) : that.mutationEffectPmids != null)
            return false;
        if (mutationEffectAbstracts != null ? !mutationEffectAbstracts.equals(that.mutationEffectAbstracts) : that.mutationEffectAbstracts != null)
            return false;
        if (mutationEffectDescription != null ? !mutationEffectDescription.equals(that.mutationEffectDescription) : that.mutationEffectDescription != null)
            return false;
        if (oncogenic != null ? !oncogenic.equals(that.oncogenic) : that.oncogenic != null) return false;
        if (oncogenicPmids != null ? !oncogenicPmids.equals(that.oncogenicPmids) : that.oncogenicPmids != null)
            return false;
        if (oncogenicAbstracts != null ? !oncogenicAbstracts.equals(that.oncogenicAbstracts) : that.oncogenicAbstracts != null)
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "BiologicalVariant{" +
            "variant=" + variant +
            ", mutationEffect='" + mutationEffect + '\'' +
            ", mutationEffectPmids=" + mutationEffectPmids +
            ", mutationEffectAbstracts=" + mutationEffectAbstracts +
            ", mutationEffectDescription=" + mutationEffectDescription +
            ", oncogenic='" + oncogenic + '\'' +
            ", oncogenicPmids=" + oncogenicPmids +
            ", oncogenicAbstracts=" + oncogenicAbstracts +
            '}';
    }
}
