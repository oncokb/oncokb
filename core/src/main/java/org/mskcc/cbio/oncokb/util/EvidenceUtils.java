package org.mskcc.cbio.oncokb.util;

import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.ArticleBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.oncotree.TumorType;

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
        Query query, String source, String geneStatus, Alteration matchedAlt,
        Set<EvidenceType> evidenceTypes, Set<LevelOfEvidence> levelOfEvidences) {
        if (query == null) {
            return new HashSet<>();
        }
        Gene gene = GeneUtils.getGene(query.getEntrezGeneId(), query.getHugoSymbol());
        if (gene != null) {
            String variantId = query.getQueryId() +
                (source != null ? ("&" + source) : "") +
                "&" + evidenceTypes.toString() +
                (levelOfEvidences == null ? "" : ("&" + levelOfEvidences.toString()));
            if (matchedAlt == null) {
                matchedAlt = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getAlteration(),
                    AlterationType.getByName(query.getAlterationType()), query.getConsequence(), query.getProteinStart(), query.getProteinEnd());
                AlterationUtils.annotateAlteration(matchedAlt, matchedAlt.getAlteration());
            }
            List<Alteration> relevantAlterations = AlterationUtils.getRelevantAlterations(matchedAlt);
            List<Alteration> alleles = AlterationUtils.getAlleleAlterations(matchedAlt);

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

            relevantEvidences = getEvidence(evidenceQueryResList, evidenceTypes, geneStatus, levelOfEvidences);

            Set<Evidence> evidencesToRemove = new HashSet<>();
            Set<Alteration> excludeAlternativeAlleles = new HashSet<>();
            for (Evidence tempEvidence : relevantEvidences) {
                if (LevelUtils.isResistanceLevel(tempEvidence.getLevelOfEvidence())) {
                    excludeAlternativeAlleles.addAll(Sets.intersection(tempEvidence.getAlterations(), new HashSet<>(alleles)));
                }
            }

            for (Evidence tempEvidence : relevantEvidences) {
                if (!Collections.disjoint(excludeAlternativeAlleles, tempEvidence.getAlterations())) {
                    evidencesToRemove.add(tempEvidence);
                }
            }
            relevantEvidences.removeAll(evidencesToRemove);

            return filterEvidence(relevantEvidences, evidenceQueryRes);
        } else {
            return new HashSet<>();
        }
    }

    public static Set<Evidence> getEvidenceByEvidenceTypesAndLevels(Set<EvidenceType> types, Set<LevelOfEvidence> levels) {
        List<Evidence> evidences = new ArrayList<>();
        for (Evidence evidence : CacheUtils.getAllEvidences()) {
            if (types != null && types.size() > 0 && !types.contains(evidence.getEvidenceType())) {
                continue;
            }
            if (levels != null && levels.size() > 0 && !levels.contains(evidence.getLevelOfEvidence())) {
                continue;
            }
            evidences.add(evidence);
        }
        return new HashSet<>(evidences);
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

    private static Set<Evidence> getEvidence(List<EvidenceQueryRes> queries, Set<EvidenceType> evidenceTypes, String geneStatus, Set<LevelOfEvidence> levelOfEvidences) {
        Set<Evidence> evidences = new HashSet<>();

        Map<Integer, Gene> genes = new HashMap<>(); //Get gene evidences
        Map<Integer, Alteration> alterations = new HashMap<>();
        Set<TumorType> tumorTypes = new HashSet<>();

        for (EvidenceQueryRes query : queries) {
            if (query.getGene() != null) {
                int entrezGeneId = query.getGene().getEntrezGeneId();
                if (!genes.containsKey(entrezGeneId)) {
                    genes.put(entrezGeneId, query.getGene());
                }


                Set<Alteration> allAlts = new HashSet<>();
                if (query.getAlterations() != null) {
                    allAlts.addAll(query.getAlterations());
                }
                if (query.getAlleles() != null) {
                    allAlts.addAll(query.getAlleles());
                }

                for (Alteration alt : allAlts) {
                    int altId = alt.getId();
                    if (!alterations.containsKey(altId)) {
                        alterations.put(altId, alt);
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

        // Get all gene related evidences
        Map<Gene, Set<Evidence>> mappedEvidences =
            EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(new HashSet<>(genes.values()), Sets.intersection(EvidenceTypeUtils.getGeneEvidenceTypes(), evidenceTypes));
        for (Map.Entry<Gene, Set<Evidence>> cursor : mappedEvidences.entrySet()) {
            evidences.addAll(cursor.getValue());
        }

        List<Alteration> uniqueAlterations = new ArrayList<>(alterations.values());
        // Get all mutation related evidences

        Set<EvidenceType> common = Sets.intersection(EvidenceTypeUtils.getMutationEvidenceTypes(), evidenceTypes);
        if (common.size() > 0) {
            evidences.addAll(getEvidence(uniqueAlterations, common, null));
        }

        // For sensitive evidences, get all ignore tumor types. They will be propagated to other tumor types
        // in assignEvidence function
        common = Sets.intersection(EvidenceTypeUtils.getSensitiveTreatmentEvidenceTypes(), evidenceTypes);
        if (common.size() > 0) {
            evidences.addAll(getEvidence(uniqueAlterations, common, levelOfEvidences));
        }

        // Get other tumor type related evidences
        Set<EvidenceType> restTTevidenceTypes = EvidenceTypeUtils.getTumorTypeEvidenceTypes();
        restTTevidenceTypes.removeAll(EvidenceTypeUtils.getSensitiveTreatmentEvidenceTypes());
        common = Sets.intersection(restTTevidenceTypes, evidenceTypes);
        if (common.size() > 0) {
            evidences.addAll(getEvidence(uniqueAlterations, common, tumorTypes, levelOfEvidences));
        }

        return evidences;
    }

    public static List<Evidence> getAlterationEvidences(List<Alteration> alterations) {
        List<Evidence> evidences = new ArrayList<>();

        if (CacheUtils.isEnabled()) {
            Set<Evidence> geneEvidences = getAllEvidencesByAlterationsGenes(alterations);
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
        if (evidenceTypes == null && evidenceTypes.isEmpty())
            return result;
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
            if (Collections.disjoint(Collections.singleton(tmpEvidence.getOncoTreeType()), tumorTypes)) {
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

    private static Set<Evidence> filterEvidence(Set<Evidence> evidences, EvidenceQueryRes evidenceQuery) {
        Set<Evidence> filtered = new HashSet<>();

        if (evidenceQuery.getGene() != null) {
            for (Evidence evidence : evidences) {

                if (evidence.getGene().equals(evidenceQuery.getGene())) {
                    //Add all gene specific evidences
                    if (evidence.getAlterations().isEmpty()) {
                        filtered.add(evidence);
                    } else {
                        boolean hasjointed = !Collections.disjoint(evidence.getAlterations(), evidenceQuery.getAlterations());
                        if (!hasjointed) {
                            hasjointed = !Collections.disjoint(evidence.getAlterations(), evidenceQuery.getAlleles());
                        }
                        if (hasjointed) {
                            if (evidence.getOncoTreeType() == null) {
                                if (evidence.getEvidenceType().equals(EvidenceType.ONCOGENIC)) {
                                    if (evidence.getDescription() == null) {
                                        List<Alteration> alterations = new ArrayList<>();
                                        alterations.addAll(evidence.getAlterations());
//                                        tempEvidence.setDescription(SummaryUtils.variantSummary(Collections.singleton(tempEvidence.getGene()), alterations, evidenceQuery.getQueryAlteration(), Collections.singleton(tempEvidence.getTumorType()), evidenceQuery.getQueryTumorType()));
                                    }
                                }
                                filtered.add(evidence);
                            } else {
                                List<TumorType> tumorType = new ArrayList<>();

                                if (evidence.getOncoTreeType() != null) {
                                    tumorType.add(evidence.getOncoTreeType());
                                }

                                hasjointed = !Collections.disjoint(evidenceQuery.getOncoTreeTypes(), tumorType);
                                if (hasjointed || com.mysql.jdbc.StringUtils.isNullOrEmpty(evidenceQuery.getQuery().getTumorType())) {
                                    filtered.add(evidence);
                                } else {
                                    if (evidence.getLevelOfEvidence() != null && evidence.getPropagation() != null) {
                                        LevelOfEvidence propagationLevel = LevelOfEvidence.getByName(evidence.getPropagation());

                                        if (propagationLevel != null) {
                                            if (evidenceQuery.getLevelOfEvidences() == null
                                                || evidenceQuery.getLevelOfEvidences().size() == 0
                                                || evidenceQuery.getLevelOfEvidences().contains(propagationLevel)) {
                                                Evidence tempEvidence = new Evidence(evidence, evidence.getId());
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

    public static Set<Evidence> getOnlyHighestLevelEvidences(Set<Evidence> evidences, Alteration exactMatch) {
        Map<LevelOfEvidence, Set<Evidence>> levels = separateEvidencesByLevel(evidences);
        Set<LevelOfEvidence> keys = levels.keySet();

        LevelOfEvidence highestLevel = LevelUtils.getHighestLevel(keys);
        LevelOfEvidence highestSensitiveLevel = LevelUtils.getHighestSensitiveLevel(keys);

        // When resistance level is not null, we need to consider whether the sensitive/resistance level is alteration specific
        // if so the resistance level is broader
        if (highestLevel != highestSensitiveLevel && highestSensitiveLevel != null) {
            LevelOfEvidence highestResistanceLevel = LevelUtils.getHighestResistanceLevel(keys);
            Set<Alteration> sensitiveAlterations = AlterationUtils.getEvidencesAlterations(levels.get(highestSensitiveLevel));
            Set<Alteration> resistanceAlterations = AlterationUtils.getEvidencesAlterations(levels.get(highestResistanceLevel));
            Set<Alteration> resistanceRelevantAlts = new HashSet<>();
            for (Alteration alteration : resistanceAlterations) {
                List<Alteration> relevantAlterations = AlterationUtils.getRelevantAlterations(alteration);

                // we need to remove the ranges that overlap but not fully cover the alteration
                Iterator<Alteration> i = relevantAlterations.iterator();
                while (i.hasNext()) {
                    Alteration relAlt = i.next();
                    if (relAlt.getConsequence().equals(alteration.getConsequence())) {
                        if (relAlt.getProteinStart() > alteration.getProteinStart() || relAlt.getProteinEnd() < alteration.getProteinEnd()) {
                            i.remove();
                        }
                    }
                }
                resistanceRelevantAlts.addAll(relevantAlterations);
            }
            if (exactMatch != null && Collections.disjoint(resistanceRelevantAlts, sensitiveAlterations) && sensitiveAlterations.contains(exactMatch)) {
                highestLevel = highestSensitiveLevel;
            }
        }

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

    private static Map<String, Object> getBiggerItem(List<String> query, Set<List<String>> keys) {
        Map<String, Object> map = new HashMap<>();
        map.put("exist", false);
        map.put("origin", false);
        map.put("result", query);
        for (List<String> key : keys) {
            // Check whether key has all elements in query;
            if (key.containsAll(query)) {
                map.put("exist", true);
                map.put("result", key);
                return map;
            }

            // Check whether query has all elements in key;
            if (query.containsAll(key)) {
                map.put("exist", true);
                map.put("origin", true);
                map.put("result", key);
                return map;
            }
        }
        return map;
    }

    public static Set<Evidence> keepHighestLevelForSameTreatments(Set<Evidence> evidences, Alteration exactMatch) {
        Map<List<String>, Set<Evidence>> maps = new HashedMap();
        Set<Evidence> filtered = new HashSet<>();

        for (Evidence evidence : evidences) {
            if (evidence.getTreatments() != null && evidence.getTreatments().size() > 0) {
                List<String> treatments = TreatmentUtils.getTreatments(new HashSet<>(evidence.getTreatments()));

                Map<String, Object> map = getBiggerItem(treatments, maps.keySet());
                if ((boolean) map.get("exist")) {
                    if ((boolean) map.get("origin")) {
                        maps.get(map.get("result")).add(evidence);
                    } else {
                        maps.put(treatments, maps.get(map.get("result")));
                        maps.get(treatments).add(evidence);
                    }
                } else {
                    maps.put(treatments, new HashSet<Evidence>());
                    maps.get(treatments).add(evidence);
                }
            } else {
                // Keep all un-treatment evidences
                filtered.add(evidence);
            }
        }

        for (Map.Entry<List<String>, Set<Evidence>> entry : maps.entrySet()) {
            Set<Evidence> highestEvis = EvidenceUtils.getOnlyHighestLevelEvidences(entry.getValue(), exactMatch);

            // If highestEvis has more than 1 items, find highest original level if the level is 2B, 3B
            if (highestEvis.size() > 1) {
                Set<LevelOfEvidence> checkLevels = new HashSet<>();
                checkLevels.add(LevelOfEvidence.LEVEL_2B);
                checkLevels.add(LevelOfEvidence.LEVEL_3B);

                for (Evidence highestEvi : highestEvis) {
                    if (checkLevels.contains(highestEvi.getLevelOfEvidence())) {
                        Set<Integer> evidenceIds = new HashSet<>();
                        Set<Gene> genes = new HashSet<>();

                        for (Evidence evidence : highestEvis) {
                            evidenceIds.add(evidence.getId());
                            genes.add(evidence.getGene());
                        }

                        Set<Evidence> originalEvis = EvidenceUtils.getEvidencesByGenesAndIds(genes, evidenceIds);

                        Set<Evidence> highestOriginalEvis = EvidenceUtils.getOnlyHighestLevelEvidences(originalEvis, exactMatch);
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
                        filtered.add(highestEvi);
                    }
                }
            } else {
                filtered.addAll(highestEvis);
            }
        }
        return filtered;
    }

    public static Set<Evidence> getEvidencesByUUID(String uuid) {
        if (uuid == null) {
            return new HashSet<>();
        }
        if (CacheUtils.isEnabled()) {
            return CacheUtils.getEvidencesByUUID(uuid);
        } else {
            return new HashSet<>(evidenceBo.findEvidenceByUUIDs(Collections.singletonList(uuid)));
        }
    }

    public static Set<Evidence> getEvidencesByUUIDs(Set<String> uuids) {
        if (uuids == null) {
            return new HashSet<>();
        }
        if (CacheUtils.isEnabled()) {
            return CacheUtils.getEvidencesByUUIDs(uuids);
        } else {
            return new HashSet<>(evidenceBo.findEvidenceByUUIDs(new ArrayList<>(uuids)));
        }
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

    public static Set<Evidence> getEvidencesByGenesAndIds(Set<Gene> genes, Set<Integer> ids) {
        if (ids == null) {
            return new HashSet<>();
        }
        if (CacheUtils.isEnabled()) {
            return CacheUtils.getEvidencesByGenesAndIds(genes, ids);
        } else {
            return new HashSet<>(evidenceBo.findEvidencesByIds(new ArrayList<>(ids)));
        }
    }

    public static Set<Evidence> getEvidenceByEvidenceIds(Set<Integer> ids) {
        if (ids == null) {
            return new HashSet<>();
        }
        if (CacheUtils.isEnabled()) {
            return CacheUtils.getEvidencesByIds(ids);
        } else {
            return new HashSet<>(evidenceBo.findEvidencesByIds(new ArrayList<>(ids)));
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
            evidenceTypes = new HashSet<>(EvidenceTypeUtils.getAllEvidenceTypes());
        }

        levelOfEvidences = levelOfEvidences == null ? levelOfEvidences :
            new HashSet<>(CollectionUtils.intersection(levelOfEvidences, LevelUtils.getPublicLevels()));

        // when the LoE and ET are empty, no info should be returned
        if ((levelOfEvidences != null && levelOfEvidences.size() == 0) || evidenceTypes.size() == 0) {
            if (requestQueries == null || requestQueries.size() == 0) {
                EvidenceQueryRes query = new EvidenceQueryRes();
                return Collections.singletonList(query);
            } else {
                List<EvidenceQueryRes> evidenceQueryRes = new ArrayList<>();
                for (Query query : requestQueries) {
                    evidenceQueries.add(new EvidenceQueryRes());
                }
                return evidenceQueryRes;
            }
        }

        if (requestQueries == null || requestQueries.size() == 0) {
            Set<Evidence> evidences = new HashSet<>();
            if ((evidenceTypes != null && evidenceTypes.size() > 0) ||
                levelOfEvidences.size() > 0) {
                evidences = EvidenceUtils.getEvidenceByEvidenceTypesAndLevels(evidenceTypes, levelOfEvidences);
            }
            EvidenceQueryRes query = new EvidenceQueryRes();
            query.setEvidences(new ArrayList<>(evidences));
            return Collections.singletonList(query);
        } else {
            for (Query requestQuery : requestQueries) {
                EvidenceQueryRes query = new EvidenceQueryRes();

                requestQuery.enrich();

                query.setQuery(requestQuery);

                query.setGene(GeneUtils.getGene(requestQuery.getEntrezGeneId(), requestQuery.getHugoSymbol()));

                if (query.getGene() != null) {
                    if (requestQuery.getTumorType() != null && !requestQuery.getTumorType().isEmpty()) {
                        query.setOncoTreeTypes(
                            TumorTypeUtils.getMappedOncoTreeTypesBySource(requestQuery.getTumorType(), source));
                    }

                    if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(requestQuery.getAlteration())) {
                        Alteration alt = AlterationUtils.findAlteration(query.getGene(), requestQuery.getAlteration());

                        if (alt == null) {
                            alt = AlterationUtils.getAlteration(query.getGene().getHugoSymbol(),
                                requestQuery.getAlteration(), null, requestQuery.getConsequence(),
                                requestQuery.getProteinStart(), requestQuery.getProteinEnd());
                            AlterationUtils.annotateAlteration(alt, alt.getAlteration());
                        }else{
                            query.setExactMatchedAlteration(alt);
                        }
                        List<Alteration> relevantAlts = AlterationUtils.getRelevantAlterations(alt);

                        // Look for Oncogenic Mutations if no relevantAlt found for alt and alt is hotspot
                        if (relevantAlts.isEmpty()
                            && HotspotUtils.isHotspot(alt)) {
                            Alteration oncogenicMutations = AlterationUtils.findAlteration(alt.getGene(), "Oncogenic Mutations");
                            if (oncogenicMutations != null) {
                                relevantAlts.add(oncogenicMutations);
                            }
                        }

                        Alteration alteration = AlterationUtils.getAlteration(query.getGene().getHugoSymbol(), requestQuery.getAlteration(), AlterationType.MUTATION, requestQuery.getConsequence(), requestQuery.getProteinStart(), requestQuery.getProteinEnd());
                        AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());
                        List<Alteration> allelesAlts = AlterationUtils.getAlleleAlterations(alteration);
                        relevantAlts.removeAll(allelesAlts);
                        query.setAlterations(relevantAlts);

                        query.setAlleles(new ArrayList<>(allelesAlts));
                    } else {
                        // if no alteration assigned, but has tumor type
                        query.setAlterations(new ArrayList<Alteration>(AlterationUtils.getAllAlterations(query.getGene())));
                    }
                }
                query.setLevelOfEvidences(levelOfEvidences == null ? null : new ArrayList<LevelOfEvidence>(levelOfEvidences));
                evidenceQueries.add(query);
            }
        }

        return assignEvidence(getEvidence(evidenceQueries, evidenceTypes, geneStatus, levelOfEvidences),
            evidenceQueries, highestLevelOnly);
    }

    private static List<EvidenceQueryRes> assignEvidence(Set<Evidence> evidences, List<EvidenceQueryRes> evidenceQueries,
                                                         Boolean highestLevelOnly) {
        highestLevelOnly = highestLevelOnly == null ? false : highestLevelOnly;

        for (EvidenceQueryRes query : evidenceQueries) {
            if (highestLevelOnly) {
                Set<Evidence> allEvidences = new HashSet<>(query.getEvidences());
                List<Evidence> filteredEvidences = new ArrayList<>();

                // Get highest sensitive evidences
                Set<Evidence> sensitiveEvidences = EvidenceUtils.getSensitiveEvidences(allEvidences);
                filteredEvidences.addAll(EvidenceUtils.getOnlyHighestLevelEvidences(sensitiveEvidences, query.getExactMatchedAlteration()));

                // Get highest resistance evidences
                Set<Evidence> resistanceEvidences = EvidenceUtils.getResistanceEvidences(allEvidences);
                filteredEvidences.addAll(EvidenceUtils.getOnlyHighestLevelEvidences(resistanceEvidences, query.getExactMatchedAlteration()));


                // Also include all non-treatment evidences
                for (Evidence evidence : allEvidences) {
                    if (!sensitiveEvidences.contains(evidence) && !resistanceEvidences.contains(evidence)) {
                        filteredEvidences.add(evidence);
                    }
                }

                query.setEvidences(filteredEvidences);
            } else {
                query.setEvidences(
                    new ArrayList<>(keepHighestLevelForSameTreatments(filterEvidence(evidences, query), query.getExactMatchedAlteration())));
            }
            CustomizeComparator.sortEvidenceBasedOnPriority(query.getEvidences(), LevelUtils.getIndexedTherapeuticLevels());
            if (query.getGene() != null && query.getGene().getHugoSymbol().equals("KIT")) {
                CustomizeComparator.sortKitTreatmentByEvidence(query.getEvidences());
            }
        }
        return evidenceQueries;
    }

    public static void annotateEvidence(Evidence evidence) throws ParserConfigurationException {
        // If evidence does not have gene info, we can not help with anything here.
        if (evidence.getGene() == null) {
            return;
        }

        // If the gene does not match with any one in our database, we can not help with anything here.
        Gene gene = GeneUtils.getGene(evidence.getGene().getEntrezGeneId(), evidence.getGene().getHugoSymbol());
        if (gene == null) {
            return;
        }

        evidence.setGene(gene);
        Set<Alteration> queryAlterations = evidence.getAlterations();
        if (queryAlterations != null && !queryAlterations.isEmpty()) {
            AlterationType type = AlterationType.MUTATION;
            Set<Alteration> alterations = new HashSet<Alteration>();
            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
            for (Alteration alt : queryAlterations) {
                String proteinChange = alt.getAlteration();
                String displayName = alt.getName();
                Alteration alteration = alterationBo.findAlterationFromDao(gene, type, proteinChange, displayName);
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

        Set<Article> articles = evidence.getArticles();

        if (evidence.getSubtype() != null && evidence.getSubtype().isEmpty()) {
            evidence.setSubtype(null);
        }
        if (evidence.getCancerType() != null && evidence.getCancerType().isEmpty()) {
            evidence.setCancerType(null);
        }
        if (articles != null && !articles.isEmpty()) {
            ArticleBo articleBo = ApplicationContextSingleton.getArticleBo();
            Set<Article> annotatedArticles = new HashSet<>();
            Set<String> articlesToBeAdded = new HashSet<>();
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
                        articlesToBeAdded.add(tempPMID);
                    } else {
                        annotatedArticles.add(tempAT);
                    }
                }
            }

            if (!articlesToBeAdded.isEmpty()) {
                for (Article article : NcbiEUtils.readPubmedArticles(articlesToBeAdded)) {
                    articleBo.save(article);
                    annotatedArticles.add(article);
                }
            }

            evidence.setArticles(annotatedArticles);
        }
    }

    /**
     * @param evidences
     * @param isDesc    default is false
     * @return
     */
    public static List<Evidence> sortTumorTypeEvidenceBasedNumOfAlts(List<Evidence> evidences, Boolean isDesc) {
        // Default multiplier for the sorting
        int flag = 1;
        if (evidences == null) {
            return new ArrayList<>();
        }
        if (isDesc) {
            flag = -1;
        }

        final int multiplier = flag;
        final Map<Evidence, Integer> originalIndices = new HashedMap();
        for (int i = 0; i < evidences.size(); i++) {
            originalIndices.put(evidences.get(i), i);
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
                if (result.equals(0)) {
                    return originalIndices.get(x) - originalIndices.get(y);
                }
                return multiplier * result;
            }
        });
        return evidences;
    }

    public static Set<Evidence> getAllEvidencesByAlterationsGenes(Collection<Alteration> alterations) {
        Set<Gene> genes = new HashSet<>();
        Set<Evidence> evidences = new HashSet<>();
        for (Alteration alteration : alterations) {
            genes.add(alteration.getGene());
        }
        if (genes.size() == 1) {
            return CacheUtils.getEvidences(genes.iterator().next());
        }
        for (Gene gene : genes) {
            evidences.addAll(CacheUtils.getEvidences(gene));
        }
        return evidences;
    }
}
