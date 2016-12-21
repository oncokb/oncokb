package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Hongxin on 10/28/16.
 */
public class AnnotatedVariant {
    String gene;
    String variant;
    String oncogenicity;
    String mutationEffect;
    String mutationEffectPmids;
    String mutationEffectAbstracts;

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getOncogenicity() {
        return oncogenicity;
    }

    public void setOncogenicity(String oncogenicity) {
        this.oncogenicity = oncogenicity;
    }

    public String getMutationEffect() {
        return mutationEffect;
    }

    public void setMutationEffect(String mutationEffect) {
        this.mutationEffect = mutationEffect;
    }

    public String getMutationEffectPmids() {
        return mutationEffectPmids;
    }

    public void setMutationEffectPmids(String mutationEffectPmids) {
        this.mutationEffectPmids = mutationEffectPmids;
    }

    public String getMutationEffectAbstracts() {
        return mutationEffectAbstracts;
    }

    public void setMutationEffectAbstracts(String mutationEffectAbstracts) {
        this.mutationEffectAbstracts = mutationEffectAbstracts;
    }

    public AnnotatedVariant(String gene, String variant, String oncogenicity, String mutationEffect, String mutationEffectPmids, String mutationEffectAbstracts) {
        this.gene = gene;
        this.variant = variant;
        this.oncogenicity = oncogenicity;
        this.mutationEffect = mutationEffect;
        this.mutationEffectPmids = mutationEffectPmids;
        this.mutationEffectAbstracts = mutationEffectAbstracts;
    }
}
