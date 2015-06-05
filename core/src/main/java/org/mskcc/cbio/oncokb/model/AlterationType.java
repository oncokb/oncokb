/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public enum AlterationType {
    
     MUTATION ("Mutation"),
     COPY_NUMBER_ALTERATION("Copy number alteration"),
     FUSION ("Fusion");

     private AlterationType(String label) {
         this.label = label;
     }

     private String label;

     public String label() {
         return label;
     }
    
}
