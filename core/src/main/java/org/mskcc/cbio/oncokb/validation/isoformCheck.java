package org.mskcc.cbio.oncokb.validation;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by Hongxin on 4/21/17.
 */
public class isoformCheck {
    public static void main(String[] args) {
        Set<Gene> genes = GeneUtils.getAllGenes();

        for (Gene gene : genes) {
            if (gene.getCuratedIsoform() != null) {
                String url = "http://grch37.rest.ensembl.org/sequence/id/" + gene.getCuratedIsoform() + "?content-type=application/json;type=protein";
                try {
                    String json = FileUtils.readRemote(url);
                    Ensembl ensembl = parseEnsembl(json);
                    if (ensembl != null && ensembl.getSeq() != null && ensembl.getId() != null) {
                        Set<Alteration> alterations = AlterationUtils.getAllAlterations(gene);
                        for (Alteration alteration : alterations) {
                            if (alteration.getConsequence() != null && alteration.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm("missense_variant"))) {
                                if (alteration.getRefResidues() != null && alteration.getProteinStart() != null) {
                                    if (ensembl.getSeq().length() < alteration.getProteinStart()) {
                                        System.out.println("Annotation out of range on gene " + gene.getHugoSymbol() + ". Current protein start is " + alteration.getProteinStart() + " but the length of protein is " + ensembl.getSeq().length());
                                    } else {
                                        char ref = alteration.getRefResidues().charAt(0);
                                        char ensemblRef = ensembl.getSeq().charAt(alteration.getProteinStart() - 1);
                                        if (ref != ensemblRef) {
                                            System.out.println("Reference mismatch: " + gene.getHugoSymbol() + " " + alteration.getAlteration() + " is supposed to be " + ensemblRef + " at " + alteration.getProteinStart());
                                        }
                                    }
                                } else {
                                    System.out.println(gene.getHugoSymbol() + " " + alteration.getAlteration() + " does not have reference AA or protein start.");
                                }
                            }
                        }
                    } else {
                        System.out.println("Failed to check gene " + gene.getHugoSymbol() + ". Result got from service is " + json);
                    }
                } catch (IOException e) {
                    System.out.println("Failed to load info from ensembl.org about " + gene.getHugoSymbol() + ". URL" + url);
//                    e.printStackTrace();
                }
            } else {
                System.out.println(gene.getHugoSymbol() + " does not have ensembl id.");
            }
        }
    }

    private static Ensembl parseEnsembl(String json) throws IOException {
        Map<String, Object> map = JsonUtils.jsonToMap(json);
        Ensembl ensembl = new Ensembl();
        if (map.containsKey("id")) {
            ensembl.setId((String) map.get("id"));
        }
        if (map.containsKey("seq")) {
            ensembl.setSeq((String) map.get("seq"));
        }
        if (map.containsKey("molecule")) {
            ensembl.setMolecule((String) map.get("molecule"));
        }
        return ensembl;
    }
}
