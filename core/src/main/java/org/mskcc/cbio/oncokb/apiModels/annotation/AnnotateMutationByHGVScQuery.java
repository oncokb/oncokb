package org.mskcc.cbio.oncokb.apiModels.annotation;

import org.mskcc.cbio.oncokb.model.InheritanceMechanism;

import java.util.List;
import java.util.Objects;


public class AnnotateMutationByHGVScQuery extends AnnotationQuery implements java.io.Serializable{
    private String hgvsc;

    // List of inheritance mechanisms (including the special value CARRIER) used to filter the returned
    // genomic indicators.
    private List<InheritanceMechanism> inheritanceMechanisms;

    public String getHgvsc() {
        return hgvsc;
    }

    public void setHgvsc(String hgvsc) {
        this.hgvsc = hgvsc;
    }

    public List<InheritanceMechanism> getInheritanceMechanisms() {
        return inheritanceMechanisms;
    }

    public void setInheritanceMechanisms(List<InheritanceMechanism> inheritanceMechanisms) {
        this.inheritanceMechanisms = inheritanceMechanisms;
    }

    public String getGene() {
        return hgvsc.split(":")[0];
    }

    public String getAlteration() {
        return hgvsc.split(":")[1];
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getHgvsc());
    }
}
