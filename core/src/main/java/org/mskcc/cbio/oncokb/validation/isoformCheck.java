package org.mskcc.cbio.oncokb.validation;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.*;

import java.io.IOException;
import java.util.*;
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
                                List<Evidence> evidences = EvidenceUtils.getAlterationEvidences(Collections.singletonList(alteration));

                                validate(gene, m.group(1), m.group(2), ensembl, alteration.getAlteration(), evidences);
                                validate(gene, m.group(4), m.group(5), ensembl, alteration.getAlteration(), evidences);
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

    private static void validate(Gene gene, String refStr, String locationStr, Ensembl ensembl, String alteration, List<Evidence> evidences) {
        if (refStr != null && locationStr != null) {
            StringBuilder errorMessage = new StringBuilder();
            Integer location = Integer.parseInt(locationStr);
            Character ref = refStr.charAt(0);
            if (ensembl.getSeq().length() < location) {
                errorMessage.append("Annotation out of range on variant " + gene.getHugoSymbol() + " " + alteration + ". Current protein start is " + location + " but the length of protein is " + ensembl.getSeq().length());
            } else {
                Character ensemblRef = ensembl.getSeq().charAt(location - 1);
                if (ref != ensemblRef) {
                    errorMessage.append("Reference mismatch: " + gene.getHugoSymbol() + " " + alteration + " is supposed to be " + ensemblRef + " at " + location);
                }
            }

            if (errorMessage.length() > 0) {
                errorMessage.append(" Current ensembl ID (isoform) is " + gene.getCuratedIsoform());
                if (ensembl.getId().equals(gene.getCuratedIsoform())) {
                    errorMessage.append(". But grch37 doesn't agree with the isoform, it returns as " + ensembl.getId());
                }

                if (evidences != null && evidences.size() > 0) {
                    for (Evidence evidence : evidences) {
                        if (evidence.getArticles() != null && evidence.getArticles().size() > 0) {
                            Set<String> pmids = EvidenceUtils.getPmids(Collections.singleton(evidence));
                            if (pmids != null && pmids.size() > 0) {
                                errorMessage.append(" Evidence type: " + evidence.getEvidenceType().name() + " pmids: " + StringUtils.join(pmids, ", "));
                            }
                        }
                    }

                    if (evidences.size() > 1) {
                        Set<String> pmids = EvidenceUtils.getPmids(new HashSet<>(evidences));
                        if (pmids != null && pmids.size() > 0) {
                            errorMessage.append(" All pmids: " + StringUtils.join(pmids, ", "));
                        }
                    }
                }
                System.out.println(errorMessage.toString());
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
