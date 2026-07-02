package org.mskcc.cbio.oncokb.apiModels;

public class AllGenomicIndicator {
    private String hugoSymbol;
    private String name;
    private String associatedVariants;
    private String description;
    private String inheritanceMechanism;

    public AllGenomicIndicator() {
    }

    public AllGenomicIndicator(String hugoSymbol, String name, String associatedVariants, String description, String inheritanceMechanism) {
        this.hugoSymbol = hugoSymbol;
        this.name = name;
        this.associatedVariants = associatedVariants;
        this.description = description;
        this.inheritanceMechanism = inheritanceMechanism;
    }

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssociatedVariants() {
        return associatedVariants;
    }

    public void setAssociatedVariants(String associatedVariants) {
        this.associatedVariants = associatedVariants;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInheritanceMechanism() {
        return inheritanceMechanism;
    }

    public void setInheritanceMechanism(String inheritanceMechanism) {
        this.inheritanceMechanism = inheritanceMechanism;
    }
}
