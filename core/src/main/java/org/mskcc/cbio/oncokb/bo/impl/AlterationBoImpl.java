/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.VariantConsequenceUtils;

import java.util.*;

/**
 * @author jgao
 */
public class AlterationBoImpl extends GenericBoImpl<Alteration, AlterationDao> implements AlterationBo {

    @Override
    public List<Alteration> findAlterationsByGene(Collection<Gene> genes) {
        List<Alteration> alterations = new ArrayList<Alteration>();
        for (Gene gene : genes) {
            alterations.addAll(getDao().findAlterationsByGene(gene));
        }
        return alterations;
    }

    @Override
    public Alteration findAlteration(Gene gene, AlterationType alterationType, String alteration) {
        return getDao().findAlteration(gene, alterationType, alteration);
    }

    @Override
    public List<Alteration> findMutationsByConsequenceAndPosition(Gene gene, VariantConsequence consequence, int start, int end, List<Alteration> alterations) {
        Set<Alteration> result = new HashSet<>();

        if(gene != null && consequence != null) {
            for(String cons: consequence.getTerm().split("\\s*,\\s*")) {
                VariantConsequence tmpConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm(cons);
                if (alterations != null && alterations.size() > 0) {
                    for (Alteration alteration : alterations) {
                        if (alteration.getGene().equals(gene) && alteration.getConsequence() != null && alteration.getConsequence().equals(tmpConsequence) && alteration.getProteinStart() <= start && alteration.getProteinEnd() >= end) {
                            result.add(alteration);
                        }
                    }
                } else {
                    List<Alteration> queryResult = getDao().findMutationsByConsequenceAndPosition(gene, tmpConsequence, start, end);
                    if(queryResult != null) {
                        result.addAll(queryResult);
                    }
                }
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * Find all relevant alterations. The order is important. The list should be generated based on priority.
     * @param alteration
     * @param fullAlterations
     * @return
     */
    @Override
    public List<Alteration> findRelevantAlterations(Alteration alteration, List<Alteration> fullAlterations) {
        List<Alteration> alterations = new ArrayList<Alteration>();
        Alteration matchedAlt = findAlteration(alteration.getGene(), alteration.getAlterationType(), alteration.getAlteration());
        if (matchedAlt != null) {
            alterations.add(matchedAlt);
        }
        if (alteration.getConsequence() != null) {
            if(alteration.getConsequence().getTerm().equals("synonymous_variant")) {
                return new ArrayList<>();
            }
            // we need to develop better way to match mutation
            if (alteration.getProteinStart() != null) {
                List<Alteration> alts = findMutationsByConsequenceAndPosition(alteration.getGene(), alteration.getConsequence(), alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations);
                if (!alteration.getConsequence().getTerm().equals("missense_variant")) {
                    alterations.addAll(alts);
                } else {
                    for (Alteration alt : alts) {
                        if (!alt.getAlteration().matches("[A-Z][0-9]+[A-Z]")) {
                            alterations.add(alt);
                        }
                    }
                }
            }

            if (alteration.getConsequence().getIsGenerallyTruncating()) {
                VariantConsequence truncatingVariantConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm("feature_truncation");
                alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), truncatingVariantConsequence, alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations));
            }
        }

        VariantConsequence anyConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm("any");
        alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), anyConsequence, alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations));

        //If alteration contains 'fusion'
        if (alteration.getAlteration() != null && alteration.getAlteration().toLowerCase().contains("fusion")) {
            boolean matchFusionForBothPatnerGenes = false;
            if (matchFusionForBothPatnerGenes) {
                GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
                String fusion = alteration.getAlteration(); // e.g. TRB-NKX2-1 fusion
                int ix = fusion.toLowerCase().indexOf("fusion");
                if (ix >= 0) {
                    Gene gene = alteration.getGene();
                    if (gene.getEntrezGeneId() > 0) {
                        // find fusions annotated in the other gene
                        String symbol = gene.getHugoSymbol();
                        String genes = fusion.substring(0, ix);
                        int ixg = genes.indexOf(symbol);
                        if (ixg < 0) {
                            System.err.println(fusion + " was under " + symbol);
                        } else {
                            String theOtherGene = genes.replace(symbol, "")
                                .replaceAll("-", " ").trim() // trim -
                                .replaceAll(" ", "-");

                            Gene tog = geneBo.findGeneByHugoSymbol(theOtherGene);
                            if (tog != null) {
                                String reverse;
                                if (ixg == 0) {
                                    reverse = tog.getHugoSymbol() + "-" + symbol + " fusion";
                                } else {
                                    reverse = symbol + "-" + tog.getHugoSymbol() + " fusion";
                                }

                                Alteration toa = findAlteration(tog, alteration.getAlterationType(), reverse);
                                if (toa != null) {
                                    alterations.add(toa);
                                }

                                toa = findAlteration(tog, alteration.getAlterationType(), "fusions");
                                if (toa != null) {
                                    alterations.add(toa);
                                }
                            }
                        }
                    } else {
                        if (gene.getGeneAliases().size() == 2) {
                            String[] aliases = gene.getGeneAliases().toArray(new String[0]);
                            Gene gene0 = geneBo.findGeneByHugoSymbol(aliases[0]);
                            Gene gene1 = geneBo.findGeneByHugoSymbol(aliases[1]);
                            Alteration toa = findAlteration(gene1, alteration.getAlterationType(), aliases[0] + "-" + aliases[1] + " fusion");
                            if (toa != null) {
                                alterations.add(toa);
                            }
                            toa = findAlteration(gene0, alteration.getAlterationType(), aliases[1] + "-" + aliases[0] + " fusion");
                            if (toa != null) {
                                alterations.add(toa);
                            }
                        }
                    }
                }
            }

            //the alteration 'fusions' should be injected into alteration list
            Alteration alt = findAlteration(alteration.getGene(), alteration.getAlterationType(), "fusions");
            if (alt != null) {
                alterations.add(alt);
            }
        }

        // Looking for inferred mutations.
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> mutationEffectEvs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_EFFECT));
        Set<String> effects = new HashSet<>();

        for (Evidence evidence : mutationEffectEvs) {
            String effect = evidence.getKnownEffect();
            if (effect != null) {
                effect = effect.toLowerCase();
                effect = effect.replaceAll("likely", "");
                effect = effect.replaceAll(" ", "");

                effects.add(effect);
            }
        }

        for(String effect : effects) {
            Alteration alt = findAlteration(alteration.getGene(), alteration.getAlterationType(), effect + " mutations");
            if (alt != null) {
                alterations.add(alt);
            }
        }

        // Looking for oncogenic mutations
        for(Alteration alt : alterations) {
            if(AlterationUtils.isOncogenicAlteration(alt)) {
                Alteration oncogenicMutations = findAlteration(alt.getGene(), alt.getAlterationType(), "oncogenic mutations");
                if (oncogenicMutations != null) {
                    alterations.add(oncogenicMutations);
                }
                break;
            }
        }

        // Match Truncating Mutations section to Deletion if no Deletion section specifically curated
        Alteration truncAlt = AlterationUtils.findAlteration(alteration.getGene(), "Truncating Mutations");
        if (truncAlt != null && !alterations.contains(truncAlt)) {
            Alteration deletion = AlterationUtils.findAlteration(alteration.getGene(), "Deletion");
            if (deletion == null && alteration.getAlteration().toLowerCase().matches("deletion")) {
                alterations.add(truncAlt);
            }
        }
        return alterations;
    }
}
