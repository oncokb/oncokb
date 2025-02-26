package org.mskcc.cbio.oncokb.apiModels;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Hongxin Zhang on 2/21/18.
 */
public class MutationEffectResp implements Serializable {
    @ApiModelProperty(value = "Indicates the effect of the mutation on the gene. Defaulted to \"\"", 
    allowableValues = "Gain-of-function, Inconclusive, Loss-of-function, Likely Loss-of-function, " +
    "Likely Gain-of-function, Neutral, Unknown, Likely Switch-of-function, " +
    "Switch-of-function, Likely Neutral")
    String knownEffect = "";

    @ApiModelProperty(value = "A brief overview of the biological and oncogenic effect of the variant. Defaulted to \"\"")
    String description = "";

    @ApiModelProperty(value = "Mutation effect-relevant PMIDs, Abstracts, and other citation sources")
    Citations citations = new Citations();

    public String getKnownEffect() {
        return knownEffect;
    }

    public void setKnownEffect(String knownEffect) {
        this.knownEffect = knownEffect;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Citations getCitations() {
        return citations;
    }

    public void setCitations(Citations citations) {
        this.citations = citations;
    }
}
