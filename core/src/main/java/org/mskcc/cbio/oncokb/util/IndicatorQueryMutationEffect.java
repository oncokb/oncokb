package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.MutationEffect;

public class IndicatorQueryMutationEffect {
    private MutationEffect mutationEffect;
    private Evidence mutationEffectEvidence;

    public IndicatorQueryMutationEffect() {
    }

    public MutationEffect getMutationEffect() {
        return mutationEffect;
    }

    public Evidence getMutationEffectEvidence() {
        return mutationEffectEvidence;
    }

    public void setMutationEffect(MutationEffect mutationEffect) {
        this.mutationEffect = mutationEffect;
    }

    public void setMutationEffectEvidence(Evidence mutationEffectEvidence) {
        this.mutationEffectEvidence = mutationEffectEvidence;
    }
}
