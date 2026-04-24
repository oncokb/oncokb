package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.Alteration;

/**
 * Stores transcript-mapped OncoKB variant details for a query variant that
 * resolves to an equivalent OncoKB-annotated protein change.
 */
public class AlternativeOncoKbVariant {
    private Alteration foundAlteration;
    private String gene;
    private String inputVariant;
    private String transcriptId;

    public AlternativeOncoKbVariant() {
    }

    public AlternativeOncoKbVariant(Alteration foundAlteration, String gene, String inputVariant, String transcriptId) {
        this.foundAlteration = foundAlteration;
        this.gene = gene;
        this.inputVariant = inputVariant;
        this.transcriptId = transcriptId;
    }

    public Alteration getFoundAlteration() {
        return foundAlteration;
    }

    public void setFoundAlteration(Alteration foundAlteration) {
        this.foundAlteration = foundAlteration;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getInputVariant() {
        return inputVariant;
    }

    public void setInputVariant(String inputVariant) {
        this.inputVariant = inputVariant;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }
}