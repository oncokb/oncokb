package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.*;

import java.io.*;
import java.util.*;

class GeneSequence {
    int entrezGeneId;
    ReferenceGenome referenceGenome;
    String isoform;
    String sequence;

    public GeneSequence() {
    }

    public GeneSequence(int entrezGeneId, ReferenceGenome referenceGenome, String isoform, String sequence) {
        this.entrezGeneId = entrezGeneId;
        this.referenceGenome = referenceGenome;
        this.isoform = isoform;
        this.sequence = sequence;
    }

    public int getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(int entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public ReferenceGenome getReferenceGenome() {
        return referenceGenome;
    }

    public void setReferenceGenome(ReferenceGenome referenceGenome) {
        this.referenceGenome = referenceGenome;
    }

    public String getIsoform() {
        return isoform;
    }

    public void setIsoform(String isoform) {
        this.isoform = isoform;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}

public class SequenceUtils {
    private static final String SEQUENCE_FILE_PATH = "/data/seq.tsv";
    private static List<GeneSequence> sequences;

    static {
        System.out.println("Cache all hotspots at " + MainUtils.getCurrentTime());
        getSequencesFromDataFile();
    }

    public static String getAminoAcid(ReferenceGenome referenceGenome, int entrezGeneId, int positionStart, int length) {
        String sequence = getSequence(referenceGenome, entrezGeneId);

        if (sequence.length() >= (positionStart + length - 1)) {
            return sequence.substring(positionStart - 1, positionStart + length - 1);
        }
        return "";
    }

    public static String getSequence(ReferenceGenome referenceGenome, int entrezGeneId) {
        if (sequences != null && referenceGenome != null && entrezGeneId > 0) {
            Optional<GeneSequence> geneSequence = sequences.stream().filter(sequence -> sequence.getEntrezGeneId() == entrezGeneId && sequence.getReferenceGenome().equals(referenceGenome)).findAny();
            return geneSequence.isPresent() ? geneSequence.get().getSequence() : "";
        } else {
            return "";
        }
    }

    private static void getSequencesFromDataFile() {
        if (sequences != null) {
            return;
        }
        try {
            List<GeneSequence> recoreds = new ArrayList<>();
            List<String> lines = FileUtils.readTrimedLinesStream(
                NCITDrugUtils.class.getResourceAsStream(SEQUENCE_FILE_PATH));

            for (String line : lines) {
                if (!line.startsWith("#") && line.trim().length() > 0) {
                    try {
                        String parts[] = line.split("\t");
                        if (parts.length < 4) {
                            throw new IllegalArgumentException("Missing parts: " + parts.length);
                        }

                        String entrezGeneId = parts[0];
                        String hugoSymbol = parts[1];
                        String referenceGenome = parts[2];
                        String isoform = parts[3];
                        String sequence = parts[4];

                        recoreds.add(new GeneSequence(Integer.valueOf(entrezGeneId), ReferenceGenome.valueOf(referenceGenome), isoform, sequence));
                    } catch (Exception e) {
                        System.err.println("Could not add line '" + line + "'. " + e);
                    }
                }
            }
            sequences = recoreds;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
