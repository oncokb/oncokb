package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
        Gene gene = query.getEntrezGeneId() == null ? GeneUtils.getGeneByHugoSymbol(query.getHugoSymbol())
            : GeneUtils.getGeneByEntrezId(query.getEntrezGeneId());
        if (gene != null) {
            String variantId = query.getQueryId() +
                (source != null ? ("&" + source) : "") +
                "&" + evidenceTypes.toString() +
                (levelOfEvidences == null ? "" : ("&" + levelOfEvidences.toString()));

            List<Alteration> relevantAlterations = AlterationUtils.getRelevantAlterations(
                gene, query.getAlteration(), query.getConsequence(),
                query.getProteinStart(), query.getProteinEnd());

            List<Evidence> relevantEvidences;
            List<OncoTreeType> relevantTumorTypes = new ArrayList<>();
            if (query.getTumorType() != null) {
                relevantTumorTypes = TumorTypeUtils.getMappedOncoTreeTypesBySource(query.getTumorType(), source);
            }
            EvidenceQueryRes evidenceQueryRes = new EvidenceQueryRes();
            evidenceQueryRes.setGene(gene);
            evidenceQueryRes.setQuery(query);
            evidenceQueryRes.setAlterations(relevantAlterations);
            evidenceQueryRes.setOncoTreeTypes(relevantTumorTypes);
            List<EvidenceQueryRes> evidenceQueryResList = new ArrayList<>();
            evidenceQueryResList.add(evidenceQueryRes);

            if (CacheUtils.isEnabled() && CacheUtils.containRelevantEvidences(gene.getEntrezGeneId(), variantId)) {
                relevantEvidences = CacheUtils.getRelevantEvidences(gene.getEntrezGeneId(), variantId);
            } else {
                relevantEvidences = getEvidence(evidenceQueryResList, evidenceTypes, geneStatus, levelOfEvidences);
                if (CacheUtils.isEnabled()) {
                    CacheUtils.setRelevantEvidences(gene.getEntrezGeneId(), variantId, relevantEvidences);
                }
            }

            return filterEvidence(relevantEvidences, evidenceQueryRes);
        } else {
            return new ArrayList<>();
        }
    }

    private static List<Evidence> getEvidence(List<Alteration> alterations) {
        if (alterations == null || alterations.size() == 0) {
            return new ArrayList<>();
        }
        if (CacheUtils.isEnabled()) {
            return new ArrayList<>(getAlterationEvidences(new HashSet<>(alterations)));
        } else {
            return evidenceBo.findEvidencesByAlteration(alterations);
        }
    }

    public static List<Evidence> getEvidence(List<Alteration> alterations, List<EvidenceType> evidenceTypes, List<LevelOfEvidence> levelOfEvidences) {
        if (alterations == null || alterations.size() == 0) {
            return new ArrayList<>();
        }
        if (evidenceTypes == null || evidenceTypes.size() == 0) {
            return getEvidence(alterations);
        }
        if(CacheUtils.isEnabled()) {
            Set<Evidence> alterationEvidences = getAlterationEvidences(new HashSet<>(alterations));
            List<Evidence> result = new ArrayList<>();

            for (Evidence evidence : alterationEvidences) {
                if(!evidenceTypes.contains(evidence.getEvidenceType())) {
                    continue;
                }
                if (levelOfEvidences != null && levelOfEvidences.size() > 0 && !levelOfEvidences.contains(evidence.getLevelOfEvidence())) {
                    continue;
                }
                result.add(evidence);
            }
            return result;
        }else{
            if (levelOfEvidences == null || levelOfEvidences.size() == 0) {
                return evidenceBo.findEvidencesByAlteration(alterations, evidenceTypes);
            } else {
                return evidenceBo.findEvidencesByAlterationWithLevels(alterations, evidenceTypes, levelOfEvidences);
            }
        }
    }

    private static List<Evidence> getEvidence(List<Alteration> alterations, List<EvidenceType> evidenceTypes, List<OncoTreeType> tumorTypes, List<LevelOfEvidence> levelOfEvidences) {
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
        Set<OncoTreeType> tumorTypes = new HashSet<>();

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

                        for (OncoTreeType tumorType : query.getOncoTreeTypes()) {
                            if (!tumorTypes.contains(tumorType)) {
                                tumorTypes.add(tumorType);
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

            List<Evidence> tumorTypesEvidences = getEvidence(new ArrayList<>(alterations.values()), evidenceTypes, tumorTypes.isEmpty() ? null : new ArrayList<>(tumorTypes), levelOfEvidences);

            evidences.addAll(tumorTypesEvidences);
        }
        return evidences;
    }

    public static Set<Evidence> getAlterationEvidences(Set<Alteration> alterations) {
        Set<Evidence> evidences = new HashSet<>();

        if (CacheUtils.isEnabled()) {
            Set<Evidence> geneEvidences = new HashSet<>();
            for (Alteration alteration : alterations) {
                geneEvidences.addAll(CacheUtils.getEvidences(alteration.getGene()));
            }
            for (Evidence evidence : geneEvidences) {
                if (!Collections.disjoint(evidence.getAlterations(), alterations)) {
                    evidences.add(evidence);
                }
            }
        } else {
            evidences = new HashSet<>(evidenceBo.findEvidencesByAlteration(alterations));
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

    public static Map<Gene, Set<Evidence>> getEvidenceByGenesAndEvidenceTypes(Set<Gene> genes, Set<EvidenceType> evidenceTypes) {
        Map<Gene, Set<Evidence>> result = new HashMap<>();
        if (CacheUtils.isEnabled()) {
            for (Gene gene : genes) {
                if (gene != null) {
                    Set<Evidence> evidences = CacheUtils.getEvidences(gene);
                    Set<Evidence> filtered = new HashSet<>();
                    for (Evidence evidence : evidences) {
                        if (evidenceTypes.contains(evidence.getEvidenceType())) {
                            filtered.add(evidence);
                        }
                    }
                    result.put(gene, filtered);
                }
            }
        } else {
            result = EvidenceUtils.separateEvidencesByGene(genes, new HashSet<Evidence>(ApplicationContextSingleton.getEvidenceBo().findAll()));
            for (Gene gene : genes) {
                Set<Evidence> evidences = result.get(gene);

                for (Evidence evidence : evidences) {
                    if (!evidenceTypes.contains(evidence.getEvidenceType())) {
                        evidences.remove(evidence);
                    }
                }
            }
        }
        return result;
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
                            if (tempEvidence.getOncoTreeType() == null) {
                                if (tempEvidence.getEvidenceType().equals(EvidenceType.ONCOGENIC)) {
                                    if (tempEvidence.getDescription() == null) {
                                        List<Alteration> alterations = new ArrayList<>();
                                        alterations.addAll(tempEvidence.getAlterations());
//                                        tempEvidence.setDescription(SummaryUtils.variantSummary(Collections.singleton(tempEvidence.getGene()), alterations, evidenceQuery.getQueryAlteration(), Collections.singleton(tempEvidence.getTumorType()), evidenceQuery.getQueryTumorType()));
                                    }
                                }
                                filtered.add(tempEvidence);
                            } else {
                                List<OncoTreeType> tumorType = new ArrayList<>();

                                if (tempEvidence.getOncoTreeType() != null) {
                                    tumorType.add(tempEvidence.getOncoTreeType());
                                }

                                if (!Collections.disjoint(evidenceQuery.getOncoTreeTypes(), tumorType)) {
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

    public static String getKnownEffectFromEvidence(EvidenceType evidenceType, Set<Evidence> evidences) {
        Set<String> result = new HashSet<>();

        for (Evidence evidence : evidences) {
            if (evidence.getEvidenceType().equals(evidenceType) && evidence.getKnownEffect() != null) {
                result.add(evidence.getKnownEffect());
            }
        }

        if (evidenceType.equals(EvidenceType.MUTATION_EFFECT) && result.size() > 1) {
            String[] effects = {"Gain-of-function", "Likely Gain-of-function", "Unknown", "Likely Neutral", "Neutral", "Likely Switch-of-function", "Switch-of-function", "Likely Loss-of-function", "Loss-of-function"};
            List<String> list = Arrays.asList(effects);
            Integer index = 100;
            for (String effect : result) {
                if (list.indexOf(effect) < index) {
                    index = list.indexOf(effect);
                }
            }
            if (index == -1) {
                return "Unknown";
            } else {
                return list.get(index);
            }
        }
        return StringUtils.join(result, ", ");
    }

    public static Set<String> getPmids(Set<Evidence> evidences) {
        Set<String> result = new HashSet<>();

        for (Evidence evidence : evidences) {
            for (Article article : evidence.getArticles()) {
                result.add(article.getPmid());
            }
        }
        return result;
    }

    public static Set<String> getDrugs(Set<Evidence> evidences) {
        Set<String> result = new HashSet<>();

        for (Evidence evidence : evidences) {
            for (Treatment treatment : evidence.getTreatments()) {
                Set<String> drugsInTreatment = new HashSet<>();
                for (Drug drug : treatment.getDrugs()) {
                    drugsInTreatment.add(drug.getDrugName());
                }
                result.add(StringUtils.join(drugsInTreatment, " + "));
            }
        }
        return result;
    }

    public static Map<Gene, Set<Evidence>> getAllGeneBasedEvidences() {
        Set<Gene> genes = GeneUtils.getAllGenes();
        Map<Gene, Set<Evidence>> evidences = EvidenceUtils.getEvidenceByGenes(genes);
        return evidences;
    }

    public static Set<Evidence> getEvidenceBasedOnHighestOncogenicity(Set<Evidence> evidences) {
        Set<Evidence> filtered = new HashSet<>();
        Map<Oncogenicity, Set<Evidence>> map = new HashMap<>();

        if (evidences == null || evidences.size() == 0)
            return filtered;

        for (Evidence evidence : evidences) {
            if (evidence.getEvidenceType() != null && evidence.getEvidenceType().equals(EvidenceType.ONCOGENIC)) {
                Oncogenicity oncogenicity = Oncogenicity.getByLevel(evidence.getKnownEffect());

                if (oncogenicity != null) {
                    if (!map.containsKey(oncogenicity))
                        map.put(oncogenicity, new HashSet<Evidence>());

                    map.get(oncogenicity).add(evidence);
                }
            }
        }

        Oncogenicity highestOncogenicity = MainUtils.findHighestOncogenic(new ArrayList<Evidence>(evidences));
        if (map.get(highestOncogenicity) != null)
            filtered = map.get(highestOncogenicity);
        return filtered;
    }
}
