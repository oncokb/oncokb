package org.mskcc.cbio.oncokb.util;

public class AminoAcidConverterUtils {

    // code copied from https://github.com/genome-nexus/genome-nexus/blob/3b4d0eefa2ff4976b632180d770c4073cc957ecc/component/src/main/java/org/cbioportal/genome_nexus/component/annotation/ProteinChangeResolver.java#L95

    // maps each letter to its corresponding three-letter code
    private static final String AA3TO1[][] = {
        {"Ala", "A"}, {"Arg", "R"}, {"Asn", "N"}, {"Asp", "D"}, {"Asx", "B"}, {"Cys", "C"},
        {"Glu", "E"}, {"Gln", "Q"}, {"Glx", "Z"}, {"Gly", "G"}, {"His", "H"}, {"Ile", "I"},
        {"Leu", "L"}, {"Lys", "K"}, {"Met", "M"}, {"Phe", "F"}, {"Pro", "P"}, {"Ser", "S"},
        {"Thr", "T"}, {"Trp", "W"}, {"Tyr", "Y"}, {"Val", "V"}, {"Xxx", "X"}, {"Ter", "*"}
    };

    // converts a three-letter amino acid code to a one-letter amino acid code
    public static String resolveHgvspShortFromHgvsp(String hgvsp)
    {
        // check if there's a digit in order to not mistakenly convert a normal alteration name
        if (hgvsp == null || !hgvsp.matches(".*\\d.*")) {
            return hgvsp;
        }

        String hgvspShort = hgvsp;

        for (int i = 0; i < 24; i++) {
            if (hgvsp.contains(AA3TO1[i][0])) {
                hgvspShort = hgvspShort.replaceAll(AA3TO1[i][0], AA3TO1[i][1]);
            }
        }

        return hgvspShort;
    }
}

