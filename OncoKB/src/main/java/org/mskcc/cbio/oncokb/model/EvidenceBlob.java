
package org.mskcc.cbio.oncokb.model;

import java.util.HashSet;
import java.util.Set;

/**
 * A collection of evidence
 * @author jgao
 */
public class EvidenceBlob {
    private Integer evidenceBlobId;
    private EvidenceType evidenceType;
    private TumorType tumorType;
    private Gene gene;
    private Alteration alteration;
    private String description;
    private Set<Evidence> evidences = new HashSet<Evidence>(0);

    public EvidenceBlob() {
    }

    public EvidenceBlob(EvidenceType evidenceType, TumorType tumorType, Gene gene, Alteration alteration, String description, Set<Evidence> evidences) {
        this.evidenceType = evidenceType;
        this.tumorType = tumorType;
        this.gene = gene;
        this.alteration = alteration;
        this.description = description;
        this.evidences = evidences;
    }

    public Integer getEvidenceBlobId() {
        return evidenceBlobId;
    }

    public void setEvidenceBlobId(Integer evidenceId) {
        this.evidenceBlobId = evidenceId;
    }

    public EvidenceType getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(EvidenceType evidenceType) {
        this.evidenceType = evidenceType;
    }

    public TumorType getTumorType() {
        return tumorType;
    }

    public void setTumorType(TumorType tumorType) {
        this.tumorType = tumorType;
    }

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public Alteration getAlteration() {
        return alteration;
    }

    public void setAlteration(Alteration alteration) {
        this.alteration = alteration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Evidence> getEvidences() {
        return evidences;
    }

    public void setEvidences(Set<Evidence> evidences) {
        this.evidences = evidences;
    }
    
    
}
