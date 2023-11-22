package org.mskcc.cbio.oncokb.model;

import java.util.List;

public class GermlineVariant {
    List<String> genomicIdentifiers;
    String penetrance;
    String pathogenic;
    String inheritanceMechanism;
    String cancerRisk;
    String description;
    String clinVarId;

    public List<String> getGenomicIdentifiers() {
        return genomicIdentifiers;
    }

    public void setGenomicIdentifiers(List<String> genomicIdentifiers) {
        this.genomicIdentifiers = genomicIdentifiers;
    }

    public String getPenetrance() {
        return penetrance;
    }

    public void setPenetrance(String penetrance) {
        this.penetrance = penetrance;
    }

    public String getPathogenic() {
        return pathogenic;
    }

    public void setPathogenic(String pathogenic) {
        this.pathogenic = pathogenic;
    }

    public String getInheritanceMechanism() {
        return inheritanceMechanism;
    }

    public void setInheritanceMechanism(String inheritanceMechanism) {
        this.inheritanceMechanism = inheritanceMechanism;
    }

    public String getCancerRisk() {
        return cancerRisk;
    }

    public void setCancerRisk(String cancerRisk) {
        this.cancerRisk = cancerRisk;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClinVarId() {
        return clinVarId;
    }

    public void setClinVarId(String clinVarId) {
        this.clinVarId = clinVarId;
    }
}
