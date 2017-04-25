package org.mskcc.cbio.oncokb.validation;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.JsonUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                            Pattern p = Pattern.compile("([A-Z])([0-9]+)(_([A-Z])([0-9]+))?.*");
                            Matcher m = p.matcher(alteration.getAlteration());

                            // As long as there is match, we should check.
                            if (m.matches()) {
                                validate(gene, m.group(1), m.group(2), ensembl, alteration.getAlteration());
                                validate(gene, m.group(4), m.group(5), ensembl, alteration.getAlteration());
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

    private static void validate(Gene gene, String refStr, String locationStr, Ensembl ensembl, String alteration) {
        if (refStr != null && locationStr != null) {
            String errorMessage = "";
            Integer location = Integer.parseInt(locationStr);
            Character ref = refStr.charAt(0);
            if (ensembl.getSeq().length() < location) {
                errorMessage = "Annotation out of range on variant " + gene.getHugoSymbol() + " " + alteration + ". Current protein start is " + location + " but the length of protein is " + ensembl.getSeq().length();
            } else {
                Character ensemblRef = ensembl.getSeq().charAt(location - 1);
                if (ref != ensemblRef) {
                    errorMessage = "Reference mismatch: " + gene.getHugoSymbol() + " " + alteration + " is supposed to be " + ensemblRef + " at " + location;
                }
            }

            if (!errorMessage.isEmpty()) {
                errorMessage = errorMessage.trim();
                if (!errorMessage.endsWith(".")) {
                    errorMessage = errorMessage + ".";
                }
                errorMessage += " Current ensembl ID (isoform) is " + gene.getCuratedIsoform();
                if (ensembl.getId().equals(gene.getCuratedIsoform())) {
                    errorMessage +=". But grch37 doesn't agree with the isoform, it returns as " + ensembl.getId();
                }

                System.out.println(errorMessage);
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
