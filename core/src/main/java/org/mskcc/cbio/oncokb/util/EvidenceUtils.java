package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;

/**
 * Created by Hongxin on 8/10/15.
 */
public class EvidenceUtils {
    private static EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

    /**
     * Remove evidences if its alteration in the alteration list
     *
     * @param evidences
     * @param alterations
     * @return
     */
    public static List<Evidence> removeByAlterations(List<Evidence> evidences, Collection<Alteration> alterations) {
        if (alterations != null) {
            Iterator<Evidence> i = evidences.iterator();
            while (i.hasNext()) {
                Boolean contain = false;
                Evidence evidence = i.next();
                for (Alteration alteration : alterations) {
                    if (alteration != null) {
                        for (Alteration eviAlt : evidence.getAlterations()) {
                            if (eviAlt != null && alteration.equals(eviAlt)) {
                                contain = true;
                                break;
                            }
                        }
                        if (contain) {
                            i.remove();
                            break;
                        }
                    }
                }
            }
        }
        return evidences;
    }

    public static List<Evidence> getRelevantEvidences(
        Query query, String source, String geneStatus,
        List<EvidenceType> evidenceTypes, List<LevelOfEvidence> levelOfEvidences) {
        Gene gene = null;

        if (query.getEntrezGeneId() != null) {
            gene = GeneUtils.getGeneByEntrezId(query.getEntrezGeneId());
        } else if (query.getHugoSymbol() != null) {
            gene = GeneUtils.getGeneByHugoSymbol(query.getHugoSymbol());
        }

        if (gene != null) {
            String strEntrezId = Integer.toString(gene.getEntrezGeneId());
            String variantId = query.getQueryId() +
                (source != null ? ("&" + source) : "") +
                "&" + evidenceTypes.toString() +
                (levelOfEvidences == null ? "" : ("&" + levelOfEvidences.toString()));

            if (CacheUtils.isEnabled() && CacheUtils.containRelevantEvidences(strEntrezId, variantId)) {
                return CacheUtils.getRelevantEvidences(strEntrezId, variantId);
            }

            List<Alteration> relevantAlterations = AlterationUtils.getRelevantAlterations(
                gene, query.getAlteration(), query.getConsequence(),
                query.getProteinStart(), query.getProteinEnd());

            List<Evidence> relevantEvidences;
            List<TumorType> relevantTumorTypes = new ArrayList<>();
            if (query.getTumorType() != null) {
                relevantTumorTypes = TumorTypeUtils.getTumorTypes(query.getTumorType(), source);
            }
            EvidenceQueryRes evidenceQueryRes = new EvidenceQueryRes();
            evidenceQueryRes.setGene(gene);
            evidenceQueryRes.setQuery(query);
            evidenceQueryRes.setAlterations(relevantAlterations);
            evidenceQueryRes.setTumorTypes(relevantTumorTypes);
            List<EvidenceQueryRes> evidenceQueryResList = new ArrayList<>();
            evidenceQueryResList.add(evidenceQueryRes);
            relevantEvidences = filterEvidence(getEvidence(evidenceQueryResList, evidenceTypes, geneStatus, levelOfEvidences), evidenceQueryRes);

            if (CacheUtils.isEnabled()) {
                CacheUtils.setRelevantEvidences(strEntrezId, variantId, relevantEvidences);
            }
            return relevantEvidences;
        } else {
            return new ArrayList<>();
        }
    }

    private static List<Evidence> getEvidence(List<Alteration> alterations) {
        if (alterations == null || alterations.size() == 0) {
            return new ArrayList<>();
        }
        return evidenceBo.findEvidencesByAlteration(alterations);
    }

    private static List<Evidence> getEvidence(List<Alteration> alterations, List<EvidenceType> evidenceTypes, List<LevelOfEvidence> levelOfEvidences) {
        if (alterations == null || alterations.size() == 0) {
            return new ArrayList<>();
        }
        if (evidenceTypes == null || evidenceTypes.size() == 0) {
            return getEvidence(alterations);
        }
        if (levelOfEvidences == null || levelOfEvidences.size() == 0) {
            return evidenceBo.findEvidencesByAlteration(alterations, evidenceTypes);
        } else {
            return evidenceBo.findEvidencesByAlterationWithLevels(alterations, evidenceTypes, levelOfEvidences);
        }
    }

    private static List<Evidence> getEvidence(List<Alteration> alterations, List<EvidenceType> evidenceTypes, List<TumorType> tumorTypes, List<LevelOfEvidence> levelOfEvidences) {
        if (alterations == null || alterations.size() == 0) {
            return new ArrayList<>();
        }
        if (evidenceTypes == null || evidenceTypes.size() == 0) {
            return getEvidence(alterations);
        }
        if (tumorTypes == null || tumorTypes.size() == 0) {
            return getEvidence(alterations, evidenceTypes, levelOfEvidences);
        }
        if (levelOfEvidences == null || levelOfEvidences.size() == 0) {
            return evidenceBo.findEvidencesByAlteration(alterations, evidenceTypes, tumorTypes);
        } else {
            return evidenceBo.findEvidencesByAlteration(alterations, evidenceTypes, tumorTypes, levelOfEvidences);
        }
    }

    public static List<Evidence> getEvidence(List<EvidenceQueryRes> queries, List<EvidenceType> evidenceTypes, String geneStatus, List<LevelOfEvidence> levelOfEvidences) {
        List<Evidence> evidences = new ArrayList<>();
        List<EvidenceType> filteredETs = new ArrayList<>();

        Map<Integer, Gene> genes = new HashMap<>(); //Get gene evidences
        Map<Integer, Alteration> alterations = new HashMap<>();
        Map<Integer, Alteration> alterationsME = new HashMap<>(); //Mutation effect only
        Map<String, TumorType> tumorTypes = new HashMap<>();

        for (EvidenceQueryRes query : queries) {
            if (query.getGene() != null) {
                int entrezGeneId = query.getGene().getEntrezGeneId();
                if (!genes.containsKey(entrezGeneId)) {
                    genes.put(entrezGeneId, query.getGene());
                }

                for (Alteration alt : query.getAlterations()) {
                    int altId = alt.getAlterationId();

//                    if (geneStatus == null || geneStatus == "") {
                    geneStatus = "all";
//                    }
                    geneStatus = geneStatus.toLowerCase();
                    if (geneStatus.equals("all") || query.getGene().getStatus().toLowerCase().equals(geneStatus)) {
                        if (!alterations.containsKey(altId)) {
                            alterations.put(altId, alt);
                        }

                        for (TumorType tumorType : query.getTumorTypes()) {
                            String tumorTypeId = tumorType.getTumorTypeId();
                            if (!tumorTypes.containsKey(tumorTypeId)) {
                                tumorTypes.put(tumorTypeId, tumorType);
                            }
                        }
                    } else {
                        if (!alterationsME.containsKey(altId)) {
                            alterationsME.put(altId, alt);
                        }
                    }
                }
            }
        }

        if (evidenceTypes.contains(EvidenceType.GENE_SUMMARY)) {
            filteredETs.add(EvidenceType.GENE_SUMMARY);
        }
        if (evidenceTypes.contains(EvidenceType.GENE_BACKGROUND)) {
            filteredETs.add(EvidenceType.GENE_BACKGROUND);
        }
        evidences.addAll(evidenceBo.findEvidencesByGene(genes.values(), filteredETs));

        List<Alteration> alts = new ArrayList<>();
        alts.addAll(alterations.values());
        alts.addAll(alterationsME.values());

        if (evidenceTypes.contains(EvidenceType.MUTATION_EFFECT)) {
            filteredETs.add(EvidenceType.MUTATION_EFFECT);
            evidences.addAll(getEvidence(alts, Arrays.asList(EvidenceType.MUTATION_EFFECT), null));
        }
        if (evidenceTypes.contains(EvidenceType.ONCOGENIC)) {
            filteredETs.add(EvidenceType.ONCOGENIC);
            evidences.addAll(getEvidence(alts, Arrays.asList(EvidenceType.ONCOGENIC), null));
        }
        if (evidenceTypes.contains(EvidenceType.VUS)) {
            filteredETs.add(EvidenceType.VUS);
            evidences.addAll(getEvidence(alts, Arrays.asList(EvidenceType.VUS), null));
        }
        if (evidenceTypes.size() != filteredETs.size()) {
            //Include all level 1 evidences
            List<EvidenceType> tmpTypes = new ArrayList<>();
            tmpTypes.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
            tmpTypes.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
            evidences.addAll(getEvidence(new ArrayList<>(alterations.values()), tmpTypes, levelOfEvidences));

            evidenceTypes.remove(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
            evidenceTypes.remove(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
            evidences.addAll(getEvidence(new ArrayList<>(alterations.values()), evidenceTypes, tumorTypes.isEmpty() ? null : new ArrayList<>(tumorTypes.values()), levelOfEvidences))
            ;
        }
        return evidences;
    }

    public static Map<Gene, Set<Evidence>> getEvidenceByGenes(Set<Gene> genes) {
        Map<Gene, Set<Evidence>> evidences = new HashMap<>();
        if (CacheUtils.isEnabled()) {
            for (Gene gene : genes) {
                if (gene != null) {
                    evidences.put(gene, CacheUtils.getEvidences(gene));
                }
            }
        } else {
            evidences = EvidenceUtils.separateEvidencesByGene(genes, new HashSet<Evidence>(ApplicationContextSingleton.getEvidenceBo().findAll()));
        }
        return evidences;
    }

    public static List<Evidence> filterEvidence(List<Evidence> evidences, EvidenceQueryRes evidenceQuery) {
        List<Evidence> filtered = new ArrayList<>();

        if (evidenceQuery.getGene() != null) {
            for (Evidence evidence : evidences) {
                Evidence tempEvidence = new Evidence(evidence);
                if (tempEvidence.getGene().equals(evidenceQuery.getGene())) {
                    //Add all gene specific evidences
                    if (tempEvidence.getAlterations().isEmpty()) {
                        filtered.add(tempEvidence);
                    } else {
                        if (!CollectionUtils.intersection(tempEvidence.getAlterations(), evidenceQuery.getAlterations()).isEmpty()) {
                            if (tempEvidence.getTumorType() == null) {
                                if (tempEvidence.getEvidenceType().equals(EvidenceType.ONCOGENIC)) {
                                    if (tempEvidence.getDescription() == null) {
                                        List<Alteration> alterations = new ArrayList<>();
                                        alterations.addAll(tempEvidence.getAlterations());
//                                        tempEvidence.setDescription(SummaryUtils.variantSummary(Collections.singleton(tempEvidence.getGene()), alterations, evidenceQuery.getQueryAlteration(), Collections.singleton(tempEvidence.getTumorType()), evidenceQuery.getQueryTumorType()));
                                    }
                                }
                                filtered.add(tempEvidence);
                            } else {
                                if (evidenceQuery.getTumorTypes().contains(tempEvidence.getTumorType())) {
                                    filtered.add(tempEvidence);
                                } else {
                                    if (tempEvidence.getLevelOfEvidence() != null) {
                                        if (tempEvidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_1) ||
                                            tempEvidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_2A)) {
                                            tempEvidence.setLevelOfEvidence(LevelOfEvidence.LEVEL_2B);
                                            filtered.add(tempEvidence);
                                        } else if (tempEvidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_3A)) {
                                            tempEvidence.setLevelOfEvidence(LevelOfEvidence.LEVEL_3B);
                                            filtered.add(tempEvidence);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return filtered;
    }

    public static List<Evidence> filterAlteration(List<Evidence> evidences, List<Alteration> alterations) {
        for (Evidence evidence : evidences) {
            Set<Alteration> filterEvidences = new HashSet<>();
            for (Alteration alt : evidence.getAlterations()) {
                if (alterations.contains(alt)) {
                    filterEvidences.add(alt);
                }
            }
            evidence.getAlterations().clear();
            evidence.setAlterations(filterEvidences);
        }

        return evidences;
    }

    public static Map<Gene, Set<Evidence>> separateEvidencesByGene(Set<Gene> genes, Set<Evidence> evidences) {
        Map<Gene, Set<Evidence>> result = new HashMap<>();

        for (Gene gene : genes) {
            result.put(gene, new HashSet<Evidence>());
        }

        for (Evidence evidence : evidences) {
            result.get(evidence.getGene()).add(evidence);
        }
        return result;
    }
}
