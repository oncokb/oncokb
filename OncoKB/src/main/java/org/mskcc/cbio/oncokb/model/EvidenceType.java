/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public enum EvidenceType {
         ACTIVITY ("Activity"),
         DRUG_SENSITIVITY("Drug sensivity"),
         PREVALENCE ("Prevalence");
         
         private EvidenceType(String label) {
             this.label = label;
         }
         
         private final String label;
         
         public String label() {
             return label;
         }
}
