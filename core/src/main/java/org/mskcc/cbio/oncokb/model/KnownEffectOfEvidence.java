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
         ACTIVATING ("Activating"),
         INACTIVATING("Inactivating"),
         SENSITIVE_TO_DRUG("Sensitive"),
         INSENSITIVE_TO_DRUG("Insensitive");
         
         private KnownEffectOfEvidence(String label) {
             this.label = label;
         }
         
         private final String label;
         
         public String label() {
             return label;
         }
}
