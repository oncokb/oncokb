/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiaojiao
 */
public enum MutationEffect {
    GAIN_OF_FUNCTION("Gain-of-function"),
    INCONCLUSIVE("Inconclusive"),
    LOSS_OF_FUNCTION("Loss-of-function"),
    LIKELY_LOSS_OF_FUNCTION("Likely Loss-of-function"),
    LIKELY_GAIN_OF_FUNCTION("Likely Gain-of-function"),
    NEUTRAL("Neutral"),
    UNKNOWN("Unknown"),
    LIKELY_SWITCH_OF_FUNCTION("Likely Switch-of-function"),
    SWITCH_OF_FUNCTION("Switch-of-function"),
    LIKELY_NEUTRAL("Likely Neutral");


    private MutationEffect(String mutation_effect) {
        this.mutation_effect = mutation_effect;
    }

    private final String mutation_effect;

    public String getMutationEffect() {
        return mutation_effect;
    }

    private static final Map<String, MutationEffect> map = new HashMap<String, MutationEffect>();
    static {
        for (MutationEffect mutationEffect : MutationEffect.values()) {
            map.put(mutationEffect.getMutationEffect(), mutationEffect);
        }
    }

    public static MutationEffect getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return map.get(name);
    }
}
