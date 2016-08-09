/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;
/**
 *
 * @author jiaojiao
 */
public enum MutationEffect {
    GAIN_OF_FUNCTION ("Gain-of-function"),
    UNKNOWN ("Unknown"),
    LOSS_OF_FUNCTION ("Loss-of-function"),
    LIKELY_LOSS_OF_FUNCTION ("Likely Loss-of-function"),
    LIKELY_GAIN_OF_FUNCTION ("Likely Gain-of-function"),
    NEUTRAL ("Neutral"),
    LIKELY_SWITCH_OF_FUNCTION ("Likely Switch-of-function"),
    SWITCH_OF_FUNCTION ("Switch-of-function"),
    LIKELY_NEUTRAL ("Likely Neutral");
    
    
    private MutationEffect(String mutation_effect) {
        this.mutation_effect = mutation_effect;
    }
    
    private final String mutation_effect;

    public String getMutation_effect() {
        return mutation_effect;
    }

    

}
