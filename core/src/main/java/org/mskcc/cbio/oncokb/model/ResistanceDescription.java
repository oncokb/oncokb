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
        if (LevelOfEvidence.LEVEL_R1.equals(highestResistanceLevel)) {
            if (Oncogenicity.YES.equals(o) || Oncogenicity.LIKELY.equals(o) || Oncogenicity.RESISTANCE.equals(o)) {
                return KNOWN;
            }
        }

        if (LevelOfEvidence.LEVEL_R2.equals(highestResistanceLevel)) {
            if (Oncogenicity.YES.equals(o) || Oncogenicity.LIKELY.equals(o) || Oncogenicity.RESISTANCE.equals(o)) {
                return POTENTIAL;
            }
        }

        if (Oncogenicity.RESISTANCE.equals(o)) {
            return LIMITED;
        }

        return null;
    }
}