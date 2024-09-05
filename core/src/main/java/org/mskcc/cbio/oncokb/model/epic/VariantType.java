package org.mskcc.cbio.oncokb.model.epic;

public enum VariantType {
    SIMPLE("Simple"),
    STRUCTURAL("Structural"),
    COPY_NUMBER_ALTERATION("Copy number variation");

    public final String label;

    private VariantType(String label) {
        this.label = label;
    }
}
