package org.mskcc.cbio.oncokb.apiModels;

/**
 * Created by Hongxin Zhang on 2/21/18.
 */
public class MutationEffectResp {
    String knownEffect = "";
    String description = "";
    References references;

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

    public References getReferences() {
        return references;
    }

    public void setReferences(References references) {
        this.references = references;
    }
}
