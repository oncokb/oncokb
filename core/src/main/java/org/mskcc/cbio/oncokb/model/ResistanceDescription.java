package org.mskcc.cbio.oncokb.model;

public enum ResistanceDescription {
    KNOWN("Known Resistance Mutation"),
    POTENTIAL("Potential Resistance Implications"),
    LIMITED("Limited Resistance Evidence");

    private String description;

    ResistanceDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static ResistanceDescription deriveFromOncogenicityAndLevels(Oncogenicity o, LevelOfEvidence highestTherapeuticLevel, LevelOfEvidence highestResistanceLevel) {
        if (highestResistanceLevel.equals(LevelOfEvidence.LEVEL_R1)) {
            if (o.equals(Oncogenicity.YES) || o.equals(Oncogenicity.LIKELY) || o.equals(Oncogenicity.RESISTANCE)) {
                return KNOWN;
            }
        }

        if (highestResistanceLevel.equals(LevelOfEvidence.LEVEL_R2)) {
            if (o.equals(Oncogenicity.YES) || o.equals(Oncogenicity.LIKELY) || o.equals(Oncogenicity.RESISTANCE)) {
                return POTENTIAL;
            }
        }

        if (o.equals(Oncogenicity.RESISTANCE)) {
            return LIMITED;
        }

        return null;
    }
}