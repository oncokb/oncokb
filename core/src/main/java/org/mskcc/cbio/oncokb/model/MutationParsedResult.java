package org.mskcc.cbio.oncokb.model;

import java.util.Set;

/**
 * Created by Hongxin Zhang on 7/23/20.
 */
public class MutationParsedResult {
    String alteration;
    String displayName;
    Set<ReferenceGenome> referenceGenomes;

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<ReferenceGenome> getReferenceGenomes() {
        return referenceGenomes;
    }

    public void setReferenceGenomes(Set<ReferenceGenome> referenceGenomes) {
        this.referenceGenomes = referenceGenomes;
    }
}
