package org.mskcc.cbio.oncokb.model;

public enum InheritanceMechanism {
    AUTOSOMAL_DOMINANT("Autosomal Dominant"),
    AUTOSOMAL_RECESSIVE("Autosomal Recessive"),
    X_LINKED_RECESSIVE("X-Linked Recessive"),
    CARRIER("Carrier");

    private final String inheritanceMechanism;

    InheritanceMechanism(String inheritanceMechanism) {
        this.inheritanceMechanism = inheritanceMechanism;
    }

    public String getValue() {
        return inheritanceMechanism;
    }
}
