package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.bo.*;
import org.mskcc.cbio.oncokb.importer.ClinicalTrialsImporter;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.oncotree.model.TumorType;

import javax.xml.parsers.ParserConfigurationException;
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

    public static Set<Evidence> getRelevantEvidences(
        Query query, String source, String geneStatus,
        Set<EvidenceType> evidenceTypes, Set<LevelOfEvidence> levelOfEvidences) {
        if (query == null) {
            return new HashSet<>();
        }
        Gene gene = query.getEntrezGeneId() == null ? GeneUtils.getGeneByHugoSymbol(query.getHugoSymbol())
            : GeneUtils.getGeneByEntrezId(query.getEntrezGeneId());
        if (gene != null) {
            String variantId = query.getQueryId() +
                (source != null ? ("&" + source) : "") +
                "&" + evidenceTypes.toString() +
                (levelOfEvidences == null ? "" : ("&" + levelOfEvidences.toString()));
            Alteration alt = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getAlteration(),
                null, query.getConsequence(), query.getProteinStart(), query.getProteinEnd());
            List<Alteration> relevantAlterations = AlterationUtils.getRelevantAlterations(alt);

            Set<Evidence> relevantEvidences;
            List<TumorType> relevantTumorTypes = new ArrayList<>();
            if (query.getTumorType() != null) {
                relevantTumorTypes = TumorTypeUtils.getMappedOncoTreeTypesBySource(query.getTumorType(), source);
            }
            EvidenceQueryRes evidenceQueryRes = new EvidenceQueryRes();
            evidenceQueryRes.setGene(gene);
            evidenceQueryRes.setQuery(query);
            evidenceQueryRes.setAlterations(relevantAlterations);
            evidenceQueryRes.setOncoTreeTypes(relevantTumorTypes);
            evidenceQueryRes.setLevelOfEvidences(levelOfEvidences == null ? null : new ArrayList<>(levelOfEvidences));
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
            return new HashSet<>();
        }
    }

    public static Set<Evidence> getEvidenceByEvidenceTypesAndLevels(Set<EvidenceType> types, Set<LevelOfEvidence> levels) {
        if (CacheUtils.isEnabled()) {
            String levelStr = levels.toString();
            String typeStr = types.toString();
            StringBuilder sb = new StringBuilder();

            if (types != null) {
                sb.append(typeStr);
            }
            if (levelStr != null) {
                sb.append(levelStr);
            }
            String variantId = sb.toString();

            if (!CacheUtils.containRelevantEvidences(-1, variantId)) {
                Set<Alteration> alterations = AlterationUtils.getAllAlterations();
                List<Evidence> evidences = EvidenceUtils.getEvidence(new ArrayList<>(alterations), types, levels);
                CacheUtils.setRelevantEvidences(-1, variantId, new HashSet<>(evidences));
            }
            return CacheUtils.getRelevantEvidences(-1, variantId);
        } else {
            Set<Alteration> alterations = AlterationUtils.getAllAlterations();
            List<Evidence> evidences = EvidenceUtils.getEvidence(new ArrayList<>(alterations), types, levels);
            return new HashSet<>(evidences);
        }
    }

    private static List<Evidence> getEvidence(List<Alteration> alterations) {
        if (alterations == null || alterations.size() == 0) {
            return new ArrayList<>();
        }
        if (CacheUtils.isEnabled()) {
            return getAlterationEvidences(alterations);
        } else {
            return evidenceBo.findEvidencesByAlteration(alterations);
        }
    }

    public static List<Evidence> getEvidence(List<Alteration> alterations, Set<EvidenceType> evidenceTypes, Set<LevelOfEvidence> levelOfEvidences) {
        if (alterations == null) {
            alterations = new ArrayList<>();
        }
        if (evidenceTypes == null) {
            evidenceTypes = new HashSet<>();
        }
        if (levelOfEvidences == null) {
            levelOfEvidences = new HashSet<>();
        }
        if (alterations.size() == 0) {
            return new ArrayList<>();
        }
        if (evidenceTypes.size() == 0 && levelOfEvidences.size() == 0) {
            return getEvidence(alterations);
        }
        if (CacheUtils.isEnabled()) {
            List<Evidence> alterationEvidences = getAlterationEvidences(alterations);
            List<Evidence> result = new ArrayList<>();

            for (Evidence evidence : alterationEvidences) {
                if (evidenceTypes.size() > 0 && !evidenceTypes.contains(evidence.getEvidenceType())) {
                    continue;
                }
                if (levelOfEvidences.size() > 0 && !levelOfEvidences.contains(evidence.getLevelOfEvidence())) {
                    continue;
                }
                result.add(evidence);
            }
            return result;
        } else {
            if (levelOfEvidences.size() == 0) {
                return evidenceBo.findEvidencesByAlteration(alterations, evidenceTypes);
            } else {
                return evidenceBo.findEvidencesByAlterationWithLevels(alterations, evidenceTypes, levelOfEvidences);
            }
        }
    }

    public static List<Evidence> getEvidence(List<Alteration> alterations, Set<EvidenceType> evidenceTypes, Set<TumorType> tumorTypes, Set<LevelOfEvidence> levelOfEvidences) {
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

    public static Set<Evidence> getEvidence(List<EvidenceQueryRes> queries, Set<EvidenceType> evidenceTypes, String geneStatus, Set<LevelOfEvidence> levelOfEvidences) {
        Set<Evidence> evidences = new HashSet<>();
        List<EvidenceType> filteredETs = new ArrayList<>();

        Map<Integer, Gene> genes = new HashMap<>(); //Get gene evidences
        Map<Integer, Alteration> alterations = new HashMap<>();
        Map<Integer, Alteration> alterationsME = new HashMap<>(); //Mutation effect only
        Set<TumorType> tumorTypes = new HashSet<>();

        for (EvidenceQueryRes query : queries) {
            if (query.getGene() != null) {
                int entrezGeneId = query.getGene().getEntrezGeneId();
                if (!genes.containsKey(entrezGeneId)) {
                    genes.put(entrezGeneId, query.getGene());
                }

                if (query.getAlterations() != null) {
                    for (Alteration alt : query.getAlterations()) {
                        int altId = alt.getId();
                        if (!alterations.containsKey(altId)) {
                            alterations.put(altId, alt);
                        }
                    }
                }

                if (query.getOncoTreeTypes() != null) {
                    for (TumorType tumorType : query.getOncoTreeTypes()) {
                        if (!tumorTypes.contains(tumorType)) {
                            tumorTypes.add(tumorType);
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
        if (filteredETs.size() > 0) {
            Map<Gene, Set<Evidence>> mappedEvidences =
                EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(new HashSet<>(genes.values()), new HashSet<>(filteredETs));

            for (Map.Entry<Gene, Set<Evidence>> cursor : mappedEvidences.entrySet()) {
                evidences.addAll(cursor.getValue());
            }
        }

        Set<Alteration> alts = new HashSet<>();
        alts.addAll(alterations.values());
        alts.addAll(alterationsME.values());

        if (evidenceTypes.contains(EvidenceType.MUTATION_EFFECT)) {
            filteredETs.add(EvidenceType.MUTATION_EFFECT);
            evidences.addAll(getEvidence(new ArrayList<>(alts), Collections.singleton(EvidenceType.MUTATION_EFFECT), null));
        }
        if (evidenceTypes.contains(EvidenceType.ONCOGENIC)) {
            filteredETs.add(EvidenceType.ONCOGENIC);
            evidences.addAll(getEvidence(new ArrayList<>(alts), Collections.singleton(EvidenceType.ONCOGENIC), null));
        }
        if (evidenceTypes.contains(EvidenceType.VUS)) {
            filteredETs.add(EvidenceType.VUS);
            evidences.addAll(getEvidence(new ArrayList<>(alts), Collections.singleton(EvidenceType.VUS), null));
        }
        if (evidenceTypes.size() != filteredETs.size()) {
            //Include all level 1 evidences
            Set<EvidenceType> tmpTypes = new HashSet<>();
            tmpTypes.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
            tmpTypes.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
            evidences.addAll(getEvidence(new ArrayList<>(alterations.values()), tmpTypes, levelOfEvidences));

            evidenceTypes.remove(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
            evidenceTypes.remove(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);

            List<Evidence> tumorTypesEvidences = getEvidence(new ArrayList<>(alterations.values()), evidenceTypes, tumorTypes.isEmpty() ? null : tumorTypes, levelOfEvidences);

            evidences.addAll(tumorTypesEvidences);
        }
        return evidences;
    }

    public static List<Evidence> getAlterationEvidences(List<Alteration> alterations) {
        List<Evidence> evidences = new ArrayList<>();

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
            evidences = evidenceBo.findEvidencesByAlteration(alterations);
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

    public static Set<Evidence> getEvidenceByGeneAndEvidenceTypes(Gene gene, Set<EvidenceType> evidenceTypes) {
        Set<Evidence> result = new HashSet<>();
        if (gene != null) {
            if (CacheUtils.isEnabled()) {
                Set<Evidence> evidences = CacheUtils.getEvidences(gene);
                for (Evidence evidence : evidences) {
                    if (evidenceTypes.contains(evidence.getEvidenceType())) {
                        result.add(evidence);
                    }
                }
            } else {
                List<Evidence> evidences = evidenceBo.findEvidencesByGene(Collections.singleton(gene), evidenceTypes);
                if (evidences != null) {
                    result = new HashSet<>(evidences);
                }
            }
        }
        return result;
    }

    public static Set<Evidence> convertEvidenceLevel(List<Evidence> evidences, Set<TumorType> tumorTypes) {
        Set<Evidence> tmpEvidences = new HashSet<>();

        for (Evidence evidence : evidences) {
            Evidence tmpEvidence = new Evidence(evidence, evidence.getId());
            Boolean flag = true;
            if (CollectionUtils.intersection(Collections.singleton(tmpEvidence.getOncoTreeType()), tumorTypes).isEmpty()) {
                if (tmpEvidence.getLevelOfEvidence() != null) {
                    if (tmpEvidence.getPropagation() != null) {
                        LevelOfEvidence propagationLevel = LevelOfEvidence.getByName(tmpEvidence.getPropagation());
                        if (propagationLevel != null) {
                            tmpEvidence.setLevelOfEvidence(propagationLevel);
                        } else {
                            flag = false;
                        }
                    }

                    // Don't include any resistance evidence if tumor type is not matched.
                    if (LevelUtils.getResistanceLevels().contains(tmpEvidence.getLevelOfEvidence())) {
                        flag = false;
                    }
                }
            }

            if (flag) {
                tmpEvidences.add(tmpEvidence);
            }
        }
        return tmpEvidences;
    }

    public static Set<Evidence> filterEvidence(Set<Evidence> evidences, EvidenceQueryRes evidenceQuery){
        Set<Evidence> filtered = new HashSet<>();

        if (evidenceQuery.getGene() != null) {
            for (Evidence evidence : evidences) {
                Evidence tempEvidence = new Evidence(evidence, evidence.getId());
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
                                List<TumorType> tumorType = new ArrayList<>();

                                if (tempEvidence.getOncoTreeType() != null) {
                                    tumorType.add(tempEvidence.getOncoTreeType());
                                }

                                if (!Collections.disjoint(evidenceQuery.getOncoTreeTypes(), tumorType)) {
                                    filtered.add(tempEvidence);
                                } else {
                                    if (tempEvidence.getLevelOfEvidence() != null && tempEvidence.getPropagation() != null) {
                                        LevelOfEvidence propagationLevel = LevelOfEvidence.getByName(tempEvidence.getPropagation());

                                        if (propagationLevel != null) {
                                            if (evidenceQuery.getLevelOfEvidences() == null
                                                || evidenceQuery.getLevelOfEvidences().size() == 0
                                                || evidenceQuery.getLevelOfEvidences().contains(propagationLevel)) {
                                                tempEvidence.setLevelOfEvidence(propagationLevel);
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
            if (result.containsKey(evidence.getGene())) {
                result.get(evidence.getGene()).add(evidence);
            }
        }
        return result;
    }

    public static MutationEffect getMutationEffectFromEvidence(Set<Evidence> evidences) {
        Set<MutationEffect> result = new HashSet<>();

        for (Evidence evidence : evidences) {
            if (evidence.getKnownEffect() != null) {
                result.add(MutationEffect.getByName(evidence.getKnownEffect()));
            }
        }

        if (result.size() > 1) {
            return MainUtils.findHighestMutationEffect(result);
        } else if (result.size() == 1) {
            return result.iterator().next();
        } else {
            return null;
        }
    }

    public static Oncogenicity getOncogenicityFromEvidence(Set<Evidence> evidences) {
        Set<Oncogenicity> result = new HashSet<>();

        for (Evidence evidence : evidences) {
            if (evidence.getKnownEffect() != null) {
                result.add(Oncogenicity.getByEvidence(evidence));
            }
        }

        if (result.size() > 1) {
            return MainUtils.findHighestOncogenicity(result);
        } else if (result.size() == 1) {
            return result.iterator().next();
        } else {
            return null;
        }
    }

    public static Set<String> getPmids(Set<Evidence> evidences) {
        Set<String> result = new HashSet<>();

        for (Evidence evidence : evidences) {
            for (Article article : evidence.getArticles()) {
                if (article.getPmid() != null) {
                    result.add(article.getPmid());
                }
            }
        }
        return result;
    }

    public static Set<ArticleAbstract> getAbstracts(Set<Evidence> evidences) {
        Set<ArticleAbstract> result = new HashSet<>();

        for (Evidence evidence : evidences) {
            for (Article article : evidence.getArticles()) {
                if (article.getAbstractContent() != null) {
                    ArticleAbstract articleAbstract = new ArticleAbstract();
                    articleAbstract.setAbstractContent(article.getAbstractContent());
                    articleAbstract.setLink(article.getLink());
                    result.add(articleAbstract);
                }
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
                    if (drug.getDrugName() != null) {
                        drugsInTreatment.add(drug.getDrugName());
                    }
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
                Oncogenicity oncogenicity = Oncogenicity.getByEvidence(evidence);

                if (oncogenicity != null) {
                    if (!map.containsKey(oncogenicity))
                        map.put(oncogenicity, new HashSet<Evidence>());

                    map.get(oncogenicity).add(evidence);
                }
            }
        }

        Oncogenicity highestOncogenicity = MainUtils.findHighestOncogenicByEvidences(evidences);
        if (map.get(highestOncogenicity) != null)
            filtered = map.get(highestOncogenicity);
        return filtered;
    }

    public static Set<Evidence> getOnlyHighestLevelEvidences(Set<Evidence> evidences) {
        Map<LevelOfEvidence, Set<Evidence>> levels = separateEvidencesByLevel(evidences);

        Set<LevelOfEvidence> keys = levels.keySet();

        LevelOfEvidence highestLevel = LevelUtils.getHighestLevel(keys);
        if (highestLevel != null) {
            return levels.get(highestLevel);
        } else {
            return new HashSet<>();
        }
    }

    public static Set<Evidence> getOnlySignificantLevelsEvidences(Set<Evidence> evidences) {
        Map<LevelOfEvidence, Set<Evidence>> levels = separateEvidencesByLevel(evidences);
        Set<LevelOfEvidence> keys = levels.keySet();
        LevelOfEvidence highestLevel = LevelUtils.getHighestLevel(keys);

        Set<Evidence> result = new HashSet<>();

        if (highestLevel != null) {
            if (highestLevel.equals(LevelOfEvidence.LEVEL_2B) && levels.containsKey(LevelOfEvidence.LEVEL_3A)) {
                result.addAll(levels.get(LevelOfEvidence.LEVEL_3A));
            }
            result.addAll(levels.get(highestLevel));
        }
        return result;
    }

    public static Map<LevelOfEvidence, Set<Evidence>> separateEvidencesByLevel(Set<Evidence> evidences) {
        Map<LevelOfEvidence, Set<Evidence>> levels = new HashMap<>();

        for (Evidence evidence : evidences) {
            if (evidence.getLevelOfEvidence() != null) {
                if (!levels.containsKey(evidence.getLevelOfEvidence())) {
                    levels.put(evidence.getLevelOfEvidence(), new HashSet<Evidence>());
                }
                levels.get(evidence.getLevelOfEvidence()).add(evidence);
            }
        }
        return levels;
    }

    public static Set<Evidence> keepHighestLevelForSameTreatments(Set<Evidence> evidences) {
        Map<String, Set<Evidence>> maps = new HashedMap();
        Set<Evidence> filtered = new HashSet<>();

        for (Evidence evidence : evidences) {
            if (evidence.getTreatments() != null && evidence.getTreatments().size() > 0) {
                String treatmentsName = TreatmentUtils.getTreatmentName(evidence.getTreatments(), true);
                if (!maps.containsKey(treatmentsName)) {
                    maps.put(treatmentsName, new HashSet<Evidence>());
                }
                maps.get(treatmentsName).add(evidence);
            } else {
                // Keep all un-treatment evidences
                filtered.add(evidence);
            }
        }

        for (Map.Entry<String, Set<Evidence>> entry : maps.entrySet()) {
            Set<Evidence> highestEvis = EvidenceUtils.getOnlyHighestLevelEvidences(entry.getValue());

            // If highestEvis has more than 1 items, find highest original level if the level is 2B, 3B
            if (highestEvis.size() > 1) {
                Set<LevelOfEvidence> checkLevels = new HashSet<>();
                checkLevels.add(LevelOfEvidence.LEVEL_2B);
                checkLevels.add(LevelOfEvidence.LEVEL_3B);
                if (checkLevels.contains(highestEvis.iterator().next().getLevelOfEvidence())) {
                    Set<Integer> evidenceIds = new HashSet<>();
                    for (Evidence evidence : highestEvis) {
                        evidenceIds.add(evidence.getId());
                    }
                    Set<Evidence> originalEvis = EvidenceUtils.getEvidenceByEvidenceIds(evidenceIds);
                    Set<Evidence> highestOriginalEvis = EvidenceUtils.getOnlyHighestLevelEvidences(originalEvis);
                    Set<Integer> filteredIds = new HashSet<>();
                    for (Evidence evidence : highestOriginalEvis) {
                        filteredIds.add(evidence.getId());
                    }
                    for (Evidence evidence : highestEvis) {
                        if (filteredIds.contains(evidence.getId())) {
                            filtered.add(evidence);
                            // Only add one
                            break;
                        }
                    }
                } else {
                    filtered.add(highestEvis.iterator().next());
                }
            } else {
                filtered.addAll(highestEvis);
            }
        }
        return filtered;
    }


    public static Evidence getEvidenceByEvidenceId(Integer id) {
        if (id == null) {
            return null;
        }
        Set<Evidence> evidences = new HashSet<>();
        if (CacheUtils.isEnabled()) {
            evidences = CacheUtils.getEvidencesByIds(Collections.singleton(id));
        } else {
            List<Evidence> evidenceList = evidenceBo.findEvidencesByIds(Collections.singletonList(id));
            if (evidenceList == null) {
                evidences = null;
            } else {
                evidences = new HashSet<>(evidenceList);
            }
        }
        if (evidences == null || evidences.size() > 1) {
            return null;
        }
        return evidences.iterator().next();
    }

    public static Set<Evidence> getEvidenceByEvidenceIds(Set<Integer> ids) {
        if (ids == null) {
            return new HashSet<>();
        }
        if (CacheUtils.isEnabled()) {
            return CacheUtils.getEvidencesByIds(ids);
        } else {
            return new HashSet<>(evidenceBo.findEvidencesByIds(new ArrayList<Integer>(ids)));
        }
    }

    public static Set<Evidence> filterEvidenceByKnownEffect(Set<Evidence> evidences, String knownEffect) {
        if (knownEffect == null) {
            return null;
        }
        Set<Evidence> result = new HashSet<>();
        for (Evidence evidence : evidences) {
            if (evidence.getKnownEffect() != null
                && evidence.getKnownEffect().equalsIgnoreCase(knownEffect)) {
                result.add(evidence);
            }
        }
        return result;
    }

    public static Set<Evidence> getSensitiveEvidences(Set<Evidence> evidences) {
        return filterEvidenceByKnownEffect(evidences, "sensitive");
    }

    public static Set<Evidence> getResistanceEvidences(Set<Evidence> evidences) {
        return filterEvidenceByKnownEffect(evidences, "resistant");
    }

    // Temporary move evidence process methods here in order to share the code between new APIs and legacies
    public static List<EvidenceQueryRes> processRequest(List<Query> requestQueries, Set<EvidenceType> evidenceTypes,
                                                        String geneStatus, String source,
                                                        Set<LevelOfEvidence> levelOfEvidences, Boolean highestLevelOnly) {
        List<EvidenceQueryRes> evidenceQueries = new ArrayList<>();

        if (source == null) {
            source = "quest";
        }

        if (evidenceTypes == null) {
            evidenceTypes = new HashSet<>(MainUtils.getAllEvidenceTypes());
        }

        if (requestQueries == null || requestQueries.size() == 0) {
            Set<Evidence> evidences = new HashSet<>();
            if ((evidenceTypes != null && evidenceTypes.size() > 0) ||
                (levelOfEvidences != null && levelOfEvidences.size() > 0)) {
                evidences = EvidenceUtils.getEvidenceByEvidenceTypesAndLevels(evidenceTypes, levelOfEvidences);
            }
            EvidenceQueryRes query = new EvidenceQueryRes();
            query.setEvidences(new ArrayList<>(evidences));
            return Collections.singletonList(query);
        } else {
            if (levelOfEvidences == null) {
                levelOfEvidences = LevelUtils.getPublicLevels();
            }
            for (Query requestQuery : requestQueries) {
                EvidenceQueryRes query = new EvidenceQueryRes();

                query.setQuery(requestQuery);
                query.setGene(getGene(requestQuery.getEntrezGeneId(), requestQuery.getHugoSymbol()));

                if (query.getGene() != null) {
                    query.setOncoTreeTypes(TumorTypeUtils.getMappedOncoTreeTypesBySource(requestQuery.getTumorType(), source));

                    if (requestQuery.getAlteration() != null) {
                        Alteration alt = AlterationUtils.getAlteration(query.getGene().getHugoSymbol(),
                            requestQuery.getAlteration(), null, requestQuery.getConsequence(),
                            requestQuery.getProteinStart(), requestQuery.getProteinEnd());
                        List<Alteration> relevantAlts = AlterationUtils.getRelevantAlterations(alt);

                        // Look for Oncogenic Mutations if no relevantAlt found for alt and alt is hotspot
                        if (relevantAlts.isEmpty()
                            && HotspotUtils.isHotspot(alt)) {
                            Alteration oncogenicMutations = AlterationUtils.findAlteration(alt.getGene(), "Oncogenic Mutations");
                            if (oncogenicMutations != null) {
                                relevantAlts.add(oncogenicMutations);
                            }
                        }
                        query.setAlterations(relevantAlts);

                        Alteration alteration = AlterationUtils.getAlteration(requestQuery.getHugoSymbol(), requestQuery.getAlteration(), AlterationType.MUTATION.name(), requestQuery.getConsequence(), requestQuery.getProteinStart(), requestQuery.getProteinEnd());
                        List<Alteration> allelesAlts = AlterationUtils.getAlleleAlterations(alteration);
                        query.setAlleles(new ArrayList<>(allelesAlts));
                    } else if (query.getOncoTreeTypes() != null && query.getOncoTreeTypes().size() > 0) {
                        // if no alteration assigned, but has tumor type
                        query.setAlterations(new ArrayList<Alteration>(AlterationUtils.getAllAlterations(query.getGene())));
                    }
                }
                if (levelOfEvidences != null) {
                    query.setLevelOfEvidences(new ArrayList<LevelOfEvidence>(levelOfEvidences));
                }
                evidenceQueries.add(query);
            }
        }

        return assignEvidence(EvidenceUtils.getEvidence(evidenceQueries, evidenceTypes, geneStatus, levelOfEvidences),
            evidenceQueries, highestLevelOnly);
    }

    private static Gene getGene(Integer entrezGeneId, String hugoSymbol) {
        Gene entrezGene = null;
        Gene hugoGene = null;

        if (entrezGeneId != null) {
            entrezGene = GeneUtils.getGeneByEntrezId(entrezGeneId);
        }
        if (hugoSymbol != null) {
            hugoGene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
        }
        if (entrezGene != null) {
            if (hugoGene != null && !entrezGene.equals(hugoGene)) {
                return null;
            } else {
                return entrezGene;
            }
        }
        return hugoGene;
    }


    private static List<EvidenceQueryRes> assignEvidence(Set<Evidence> evidences, List<EvidenceQueryRes> evidenceQueries,
                                                         Boolean highestLevelOnly) {
        highestLevelOnly = highestLevelOnly == null ? false : highestLevelOnly;

        for (EvidenceQueryRes query : evidenceQueries) {
            query.setEvidences(
                new ArrayList<>(
                    EvidenceUtils.keepHighestLevelForSameTreatments(EvidenceUtils.filterEvidence(evidences, query))));

            // Attach evidence if query doesn't contain any alteration and has alleles.
            if ((query.getAlterations() == null || query.getAlterations().isEmpty() || AlterationUtils.excludeVUS(query.getGene(), query.getAlterations()).size() == 0) && (query.getAlleles() != null && !query.getAlleles().isEmpty())) {
                // Get oncogenic and mutation effect evidences
                List<Alteration> alleles = query.getAlleles();
                List<Alteration> allelesAndRelevantAlterations = new ArrayList<>();
                Set<Alteration> oncogenicMutations = new HashSet<>();

                allelesAndRelevantAlterations.addAll(alleles);

                Alteration oncogenicAllele = AlterationUtils.findOncogenicAllele(alleles);

                if (oncogenicAllele != null) {
                    oncogenicMutations = AlterationUtils.getOncogenicMutations(oncogenicAllele);
                    allelesAndRelevantAlterations.addAll(oncogenicMutations);
                }

                List<Evidence> oncogenics = EvidenceUtils.getEvidence(allelesAndRelevantAlterations, Collections.singleton(EvidenceType.ONCOGENIC), null);
                Oncogenicity highestOncogenic = MainUtils.findHighestOncogenicByEvidences(new HashSet<>(oncogenics));
                if (highestOncogenic != null) {
                    Evidence recordMatchHighestOncogenicity = null;

                    for (Evidence evidence : oncogenics) {
                        if (evidence.getKnownEffect() != null) {
                            Oncogenicity oncogenicity = Oncogenicity.getByEvidence(evidence);
                            if (oncogenicity != null && oncogenicity.equals(highestOncogenic)) {
                                recordMatchHighestOncogenicity = evidence;
                                break;
                            }
                        }
                    }

                    if (recordMatchHighestOncogenicity != null) {
                        Oncogenicity alleleOncogenicity = MainUtils.setToAlleleOncogenicity(highestOncogenic);
                        Evidence evidence = new Evidence();
                        evidence.setId(recordMatchHighestOncogenicity.getId());
                        evidence.setGene(recordMatchHighestOncogenicity.getGene());
                        evidence.setEvidenceType(EvidenceType.ONCOGENIC);
                        evidence.setKnownEffect(alleleOncogenicity == null ? "" : alleleOncogenicity.getOncogenic());
                        query.getEvidences().add(evidence);
                    }
                }

                Set<Alteration> altsWithHighestOncogenicity = new HashSet<>();

                for (Evidence evidence : EvidenceUtils.getEvidenceBasedOnHighestOncogenicity(new HashSet<Evidence>(oncogenics))) {
                    for (Alteration alt : evidence.getAlterations()) {
                        if (allelesAndRelevantAlterations.contains(alt)) {
                            altsWithHighestOncogenicity.add(alt);
                        }
                    }
                }

                List<Evidence> mutationEffectsEvis = EvidenceUtils.getEvidence(new ArrayList<>(altsWithHighestOncogenicity), Collections.singleton(EvidenceType.MUTATION_EFFECT), null);
                if (mutationEffectsEvis != null && mutationEffectsEvis.size() > 0) {
                    Set<String> effects = new HashSet<>();

                    for (Evidence mutationEffectEvi : mutationEffectsEvis) {
                        effects.add(mutationEffectEvi.getKnownEffect());
                    }

                    Evidence mutationEffect = new Evidence();
                    Evidence example = mutationEffectsEvis.iterator().next();
                    mutationEffect.setId(example.getId());
                    mutationEffect.setGene(example.getGene());
                    mutationEffect.setEvidenceType(EvidenceType.MUTATION_EFFECT);
                    mutationEffect.setKnownEffect(MainUtils.getAlleleConflictsMutationEffect(effects));
                    query.getEvidences().add(mutationEffect);
                }

                // Get alternate allele treatment evidences, only match sensitive treatments
                List<Evidence> alleleEvidences = EvidenceUtils.getEvidence(new ArrayList<>(alleles), MainUtils.getSensitiveTreatmentEvidenceTypes(), LevelUtils.getPublicLevels());
                if (oncogenicMutations != null) {
                    alleleEvidences.addAll(EvidenceUtils.getEvidence(new ArrayList<>(oncogenicMutations), MainUtils.getTreatmentEvidenceTypes(), LevelUtils.getPublicLevels()));
                }

                List<Evidence> alleleEvidencesCopy = new ArrayList<>();
                if (alleleEvidences != null) {
                    for (Evidence evidence : alleleEvidences) {
                        Evidence tmpEvidence = new Evidence(evidence, evidence.getId());
                        LevelOfEvidence levelOfEvidence = LevelUtils.setToAlleleLevel(evidence.getLevelOfEvidence(), CollectionUtils.intersection(Collections.singleton(evidence.getOncoTreeType()), query.getOncoTreeTypes()).size() > 0);
                        if (levelOfEvidence != null) {
                            tmpEvidence.setLevelOfEvidence(levelOfEvidence);
                            alleleEvidencesCopy.add(tmpEvidence);
                        }
                    }

                    query.getEvidences().addAll(convertEvidenceLevel(alleleEvidencesCopy, new HashSet<>(query.getOncoTreeTypes())));
                }
            }

            if (highestLevelOnly) {
                Set<Evidence> allEvidences = new HashSet<>(query.getEvidences());
                List<Evidence> filteredEvidences = new ArrayList<>();

                // Get highest sensitive evidences
                Set<Evidence> sensitiveEvidences = EvidenceUtils.getSensitiveEvidences(allEvidences);
                filteredEvidences.addAll(EvidenceUtils.getOnlyHighestLevelEvidences(sensitiveEvidences));

                // Get highest resistance evidences
                Set<Evidence> resistanceEvidences = EvidenceUtils.getResistanceEvidences(allEvidences);
                filteredEvidences.addAll(EvidenceUtils.getOnlyHighestLevelEvidences(resistanceEvidences));


                // Also include all non-treatment evidences
                for (Evidence evidence : allEvidences) {
                    if (!sensitiveEvidences.contains(evidence) && !resistanceEvidences.contains(evidence)) {
                        filteredEvidences.add(evidence);
                    }
                }

                query.setEvidences(filteredEvidences);
            }
        }
        return evidenceQueries;
    }

    public static void annotateEvidence(Evidence evidence) throws ParserConfigurationException {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Gene gene = geneBo.findGeneByHugoSymbol(evidence.getGene().getHugoSymbol());
        evidence.setGene(gene);
        if(gene == null) {
            return;
        }
        Set<Alteration> queryAlterations = evidence.getAlterations();
        if(queryAlterations != null && !queryAlterations.isEmpty()) {
            AlterationType type = AlterationType.MUTATION;
            Set<Alteration> alterations = new HashSet<Alteration>();
            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
            for (Alteration alt : queryAlterations) {
                String proteinChange = alt.getAlteration();
                String displayName = alt.getName();
                Alteration alteration = alterationBo.findAlteration(gene, type, proteinChange);
                if (alteration == null) {
                    alteration = new Alteration();
                    alteration.setGene(gene);
                    alteration.setAlterationType(type);
                    alteration.setAlteration(proteinChange);
                    alteration.setName(displayName);
                    AlterationUtils.annotateAlteration(alteration, proteinChange);
                    alterationBo.save(alteration);
                }
                alterations.add(alteration);
            }
            evidence.setAlterations(alterations);
        }

        Set<ClinicalTrial> trials = evidence.getClinicalTrials();
        Set<Article> articles = evidence.getArticles();
        Set<Treatment> treatments = evidence.getTreatments();
        Set<NccnGuideline> nccnGuidelines = evidence.getNccnGuidelines();

        if (evidence.getSubtype() != null && evidence.getSubtype().isEmpty()) {
            evidence.setSubtype(null);
        }
        if (evidence.getCancerType() != null && evidence.getCancerType().isEmpty()) {
            evidence.setCancerType(null);
        }
        if (trials != null && !trials.isEmpty()) {
            ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();
            Set<ClinicalTrial> annotatedTrials = new HashSet<>();
            Set<String> nctIds = new HashSet<String>();
            for (ClinicalTrial trial : trials) {
                String tempNctID = trial.getNctId();
                ClinicalTrial tempCT = clinicalTrialBo.findClinicalTrialByNctId(tempNctID);
                if (tempCT == null) {
                    nctIds.add(tempNctID);
                } else {
                    annotatedTrials.add(tempCT);
                }
            }
            annotatedTrials.addAll(ClinicalTrialsImporter.importTrials(nctIds));
            evidence.setClinicalTrials(annotatedTrials);
        }
        if (articles != null && !articles.isEmpty()) {
            ArticleBo articleBo = ApplicationContextSingleton.getArticleBo();
            Set<Article> annotatedArticles = new HashSet<>();
            for (Article article : articles) {
                String tempPMID = article.getPmid();
                if (tempPMID == null) {
                    Article tempAT = articleBo.findArticleByAbstract(article.getAbstractContent());
                    if (tempAT == null) {
                        articleBo.save(article);
                        annotatedArticles.add(article);
                    } else {
                        annotatedArticles.add(tempAT);
                    }
                } else {
                    Article tempAT = articleBo.findArticleByPmid(tempPMID);
                    if (tempAT == null) {
                        Article newArticle = NcbiEUtils.readPubmedArticle(tempPMID);
                        if (newArticle != null) {
                            articleBo.save(newArticle);
                            annotatedArticles.add(newArticle);
                        }
                    } else {
                        annotatedArticles.add(tempAT);
                    }
                }
            }
            evidence.setArticles(annotatedArticles);
        }

        if (treatments != null && !treatments.isEmpty()) {
            DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
            TreatmentBo treatmentBo = ApplicationContextSingleton.getTreatmentBo();
            for (Treatment treatment : treatments) {
                Set<Drug> drugs = treatment.getDrugs();
                if (drugs != null && !drugs.isEmpty()) {
                    Set<Drug> drugsFromDB = new HashSet<>();
                    for (Drug drug : drugs) {
                        Drug tempDrug = drugBo.findDrugByName(drug.getDrugName());
                        if (tempDrug == null) {
                            drugBo.save(drug);
                            drugsFromDB.add(drug);
                        } else {
                            drugsFromDB.add(tempDrug);
                        }
                    }
                    treatment.setDrugs(drugsFromDB);
                }
                treatmentBo.saveOrUpdate(treatment);
            }
        }
        if (nccnGuidelines != null && !nccnGuidelines.isEmpty()) {
            NccnGuidelineBo nccnGuidelineBo = ApplicationContextSingleton.getNccnGuidelineBo();
            Set<NccnGuideline> nccnFromDB = new HashSet<>();
            for (NccnGuideline nccnGuideline : nccnGuidelines) {
                NccnGuideline tempNccnGuideline = nccnGuidelineBo.findNccnGuideline(nccnGuideline.getTherapy(), nccnGuideline.getDisease(), nccnGuideline.getVersion(), nccnGuideline.getPages());
                if (tempNccnGuideline == null) {
                    nccnGuidelineBo.saveOrUpdate(nccnGuideline);
                    nccnFromDB.add(nccnGuideline);
                } else {
                    nccnFromDB.add(tempNccnGuideline);
                }
            }
            evidence.setNccnGuidelines(nccnFromDB);
        }
    }

    public static List<Evidence> sortTumorTypeEvidenceBasedNumOfAlts(List<Evidence> evidences, Boolean isDesc) {

        if (evidences == null) {
            return new ArrayList<>();
        }
        if (isDesc == null) {
            isDesc = false;
        }

        // Sort all tumor type summaries, the more specific tumor type summary will be picked.
        Collections.sort(evidences, new Comparator<Evidence>() {
            public int compare(Evidence x, Evidence y) {
                if (x.getAlterations() == null) {
                    return 1;
                }
                if (y.getAlterations() == null) {
                    return -1;
                }
                Integer result = x.getAlterations().size() - y.getAlterations().size();
                return result;
            }
        });

        if (isDesc) {
            Collections.reverse(evidences);
        }
        return evidences;
    }
}
