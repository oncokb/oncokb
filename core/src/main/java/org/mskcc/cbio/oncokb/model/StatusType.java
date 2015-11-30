/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;

/**
 * @author Hongxin
 */
public enum StatusType {

    MUTATION("Mutation");

    private StatusType(String label) {
        this.label = label;
    }

    private String label;

    public String label() {
        return label;
    }

}
