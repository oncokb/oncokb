/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public enum KnownEffectOfEvidence {
         GAIN_OF_FUNCTION ("Gain of function"),
         LOSS_OF_FUNCTION("Loss of function"),
         SWITCH_OF_FUNCTION("Switch of function"),
         SENSITIVE_TO_DRUG("Sensitive"),
         INSENSITIVE_TO_DRUG("Insensitive");
         
         private KnownEffectOfEvidence(String label) {
             this.label = label;
         }
         
         private String label;
         
         public String label() {
             return label;
         }
}
