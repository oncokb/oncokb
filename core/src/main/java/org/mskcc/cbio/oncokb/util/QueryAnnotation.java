package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.*;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.oncotree.model.TumorType;

import java.util.*;

/**
 * Created by Hongxin on 4/14/17.
 */
public class QueryAnnotation {
    public static void EnrichQuery(Query query) {
        // Set the alteration to empty string in order to get relevant variants.
        if (query.getAlteration() == null) {
            query.setAlteration("");
        }

        if (QueryUtils.isFusionQuery(query)) {
            // If the query only indicates this is a fusion event with associated genes but no alteration specified,
            // need to attach Fusions to the query.
            if (query.getAlteration() == null || query.getAlteration().isEmpty()) {
                query.setAlteration("Fusions");
            }

            Set<Gene> genes = GeneUtils.getUniqueGenesFromString(query.getHugoSymbol(), "-");
            if (genes.size() > 0) {
                query.setAlteration(query.getHugoSymbol() + " fusion");
            }
        }
    }

    public static void EnrichQueryV2(QueryV2 query2) {
        // Set the alteration to empty string in order to get relevant variants.
        if (query2.getVariant() == null) {
            query2.setVariant("");
        }

        if (query2.getSource() == null || query2.getSource().isEmpty()) {
            query2.setSource("oncotree");
        }

        if (query2.getHighestLevelOnly() == null) {
            query2.setHighestLevelOnly(false);
        }

        if (query2.getLevels() == null) {
            query2.setLevels(LevelUtils.getPublicAndOtherIndicationLevels());
        }

        if (QueryUtils.isFusionQuery(new Query(query2))) {
            // If the query2 only indicates this is a fusion event with associated genes but no alteration specified,
            // need to attach Fusions to the query2.
            if (query2.getVariant() == null || query2.getVariant().isEmpty()) {
                query2.setVariant("Fusions");
            }

            Set<Gene> genes = GeneUtils.getUniqueGenesFromString(query2.getHugoSymbol(), "-");
            if (genes.size() > 0) {
                query2.setVariant(query2.getHugoSymbol() + " fusion");
            }
        }
    }

    public static SearchResult annotateSearchQuery(QueryV2 query) {
        SearchResult searchResult = annotateQuery(query);
        if (searchResult != null) {
            // Only keep reserved properties in SUMMARY projection
            if (query.getProjection() != null && query.getProjection().equals(Projection.SUMMARY)) {
                searchResult.setTreatments(null);
                searchResult.setGeneSummary(null);
                searchResult.setVariantSummary(null);
                searchResult.setTumorTypeSummary(null);
                searchResult.setGeneBackground(null);
            }

            // Always return Unknown if no oncogenicity specified
            if (searchResult.getOncogenic() == null) {
                searchResult.setOncogenic(new KnownEffect(Oncogenicity.UNKNOWN.getOncogenic()));
            }

            // Always return Unknown if no mutation effect specified
            if (searchResult.getMutationEffect() == null) {
                searchResult.setMutationEffect(new KnownEffect(MutationEffect.UNKNOWN.getMutationEffect()));
            }
            searchResult.updateLastUpdate();
        }
        return searchResult;
    }

    private static SearchResult annotateQuery(QueryV2 query) {
        SearchResult result = new SearchResult();
        OtherSources otherSources = new OtherSources();
        result.setOtherSources(otherSources);
        result.setQuery(query);

        if (query == null) {
            return result;
        }
        EnrichQueryV2(query);

        // Check whether query gene is annotated.
        Gene gene = GeneUtils.getGeneByQuery(new Query(query));
        if (gene != null) {
            result.setGeneAnnotated(true);
            result.setGeneSummary(SummaryUtils.geneSummary(gene));
        } else {
            result.setGeneAnnotated(false);
            return result;
        }

        // Get gene background
        getGeneBackground(result);

        // Check whether query variant is annotated.
        Alteration alteration = getAlteration(gene, query);

        // Whether alteration is hotpot from Matt's list
        result.getOtherSources().setHotspot(HotspotUtils.isHotspot(alteration));

        // Get alternative alleles info
        List<Alteration> alleles = AlterationUtils.getAlleleAlterations(alteration);
        if(alleles == null || alleles.size() == 0) {
            result.setAnnotatedAlternativeAlleleVariants(new ArrayList<Alteration>());
        }else{
            result.setAnnotatedAlternativeAlleleVariants(alleles);
        }

        // Get relevant alterations
        List<Alteration> relevantAlterations = AlterationUtils.getRelevantAlterationsByQuery(new Query(query));
        result.setAnnotatedMatchingVariants(relevantAlterations);

        // Get list of alterations which are not VUS
        List<Alteration> nonVUSRelevantAlts = new ArrayList<>();
        nonVUSRelevantAlts.addAll(AlterationUtils.excludeVUS(relevantAlterations));

        // Get VUS info
        annotateVusInfo(result);

        List<TumorType> oncoTreeTypes = new ArrayList<>();
        if (MainUtils.isNotNullOrEmpty(query.getTumorType())) {
            oncoTreeTypes.addAll(TumorTypeUtils.getMappedOncoTreeTypesBySource(query.getTumorType(), query.getSource()));
        }

        Set<Evidence> treatmentEvidences = new HashSet<>();

        if (nonVUSRelevantAlts.size() > 0) {
            annotateKnownAltOncogenicity(result, alteration);

            treatmentEvidences = EvidenceUtils.keepHighestLevelForSameTreatments(
                EvidenceUtils.getRelevantEvidences(new Query(query), query.getSource(), null,
                    MainUtils.getTreatmentEvidenceTypes(),
                    (query.getLevels() != null ?
                        new HashSet<>(CollectionUtils.intersection(query.getLevels(),
                            LevelUtils.getPublicAndOtherIndicationLevels())) : LevelUtils.getPublicAndOtherIndicationLevels())));
        } else if (result.getAnnotatedAlternativeAlleleVariants().size() > 0 || (result.getVUS() != null && result.getVUS().getStatus())) {
            Alteration oncogenicAllele = AlterationUtils.findOncogenicAllele(alleles);
            List<Alteration> alleleAndRelevantAlterations = new ArrayList<>();
            Set<Alteration> oncogenicMutations = null;

            alleleAndRelevantAlterations.addAll(alleles);
            if (oncogenicAllele != null) {
                oncogenicMutations = AlterationUtils.getOncogenicMutations(oncogenicAllele);
                alleleAndRelevantAlterations.addAll(oncogenicMutations);
            }

            treatmentEvidences = EvidenceUtils.keepHighestLevelForSameTreatments(
                EvidenceUtils.convertEvidenceLevel(
                    EvidenceUtils.getEvidence(new ArrayList<>(alleles),
                        MainUtils.getSensitiveTreatmentEvidenceTypes(),
                        (query.getLevels() != null ?
                            new HashSet<LevelOfEvidence>(CollectionUtils.intersection(query.getLevels(),
                                LevelUtils.getPublicAndOtherIndicationLevels())) : LevelUtils.getPublicAndOtherIndicationLevels())), new HashSet<>(oncoTreeTypes)));

            if (oncogenicMutations != null) {
                treatmentEvidences.addAll(EvidenceUtils.keepHighestLevelForSameTreatments(
                    EvidenceUtils.convertEvidenceLevel(
                        EvidenceUtils.getEvidence(new ArrayList<>(oncogenicMutations),
                            MainUtils.getTreatmentEvidenceTypes(),
                            (query.getLevels() != null ?
                                new HashSet<>(CollectionUtils.intersection(query.getLevels(),
                                    LevelUtils.getPublicAndOtherIndicationLevels())) : LevelUtils.getPublicAndOtherIndicationLevels())), new HashSet<>(oncoTreeTypes))));
            }

            Set<Evidence> oncogenicEvis = new HashSet<>(EvidenceUtils.getEvidence(new ArrayList<>(alleleAndRelevantAlterations), Collections.singleton(EvidenceType.ONCOGENIC), null));
            Oncogenicity oncogenicity = MainUtils.findHighestOncogenicByEvidences(oncogenicEvis);

            if (oncogenicity != null) {
                Evidence evidence = MainUtils.findEvidenceByHighestOncogenicityInEvidence(oncogenicEvis, oncogenicity);
                Oncogenicity alternativeAllelOncogenicity = MainUtils.setToAlleleOncogenicity(oncogenicity);
                if (alternativeAllelOncogenicity != null) {
                    result.setOncogenic(getKnownEffect(MainUtils.setToAlleleOncogenicity(oncogenicity).getOncogenic(), evidence));
                }
            }
        }

        // Get mutation effect info
        annotateKnownAltMutationEffect(result, alteration);

        // Set hotspot oncogenicity to Predicted Oncogenic
        if (result.getOncogenic() == null
            && result.getOtherSources().getHotspot()) {
            result.setOncogenic(new KnownEffect(Oncogenicity.PREDICTED.getOncogenic()));

            // Check whether the gene has Oncogenic Mutations annotated
            Alteration oncogenicMutation = AlterationUtils.findAlteration(gene, "Oncogenic Mutations");
            if (oncogenicMutation != null) {
                relevantAlterations.add(oncogenicMutation);
                treatmentEvidences.addAll(EvidenceUtils.keepHighestLevelForSameTreatments(
                    EvidenceUtils.convertEvidenceLevel(
                        EvidenceUtils.getEvidence(Collections.singletonList(oncogenicMutation),
                            MainUtils.getTreatmentEvidenceTypes(),
                            (query.getLevels() != null ?
                                new HashSet<>(CollectionUtils.intersection(query.getLevels(),
                                    LevelUtils.getPublicAndOtherIndicationLevels())) : LevelUtils.getPublicAndOtherIndicationLevels())), new HashSet<>(oncoTreeTypes))));
            }
        }

        if (treatmentEvidences != null && !treatmentEvidences.isEmpty()) {
            if (query.getHighestLevelOnly()) {
                treatmentEvidences = EvidenceUtils.keepHighestSensitiveResistanceTreatmentEvidences(treatmentEvidences);
            }
            if (!treatmentEvidences.isEmpty()) {
                List<TreatmentInfo> treatments = getTreatmentsInfo(treatmentEvidences);
                result.setTreatments(treatments);

                Map<String, LevelOfEvidenceWithTime> highestLevels = findHighestLevel(new HashSet<>(treatments));
                result.setHighestSensitiveLevel(highestLevels.get("sensitive"));
                result.setHighestResistanceLevel(highestLevels.get("resistant"));

                List<Evidence> sensitiveList = new ArrayList<>();
                if (result.getHighestSensitiveLevel() != null)
                    sensitiveList.addAll(IndicatorUtils.getOtherSignificantLevelsEvidences(result.getHighestSensitiveLevel().getLevel(), "sensitive", treatmentEvidences));

                List<Evidence> resistanceList = new ArrayList<>();
                if (result.getHighestResistanceLevel() != null)
                    resistanceList.addAll(IndicatorUtils.getOtherSignificantLevelsEvidences(result.getHighestResistanceLevel().getLevel(), "resistance", treatmentEvidences));

                List<LevelOfEvidenceWithTime> sensitiveLevels = new ArrayList<>();
                for (Evidence evidence : sensitiveList) {
                    sensitiveLevels.add(new LevelOfEvidenceWithTime(evidence.getLevelOfEvidence(), evidence.getLastEdit()));
                }
                result.setOtherSignificantSensitiveLevels(sensitiveLevels);


                List<LevelOfEvidenceWithTime> resistanceLevels = new ArrayList<>();
                for (Evidence evidence : resistanceList) {
                    resistanceLevels.add(new LevelOfEvidenceWithTime(evidence.getLevelOfEvidence(), evidence.getLastEdit()));
                }
                result.setOtherSignificantResistanceLevels(resistanceLevels);
            }
        }

        // Tumor type summary
        if (query.getTumorType() != null) {
            result.setTumorTypeSummary(SummaryUtils.tumorTypeSummary(gene, new Query(query),
                new ArrayList<>(relevantAlterations),
                new HashSet<>(oncoTreeTypes)));
        }

        // Mutation summary
        result.setVariantSummary(SummaryUtils.oncogenicSummary(gene,
            new ArrayList<>(relevantAlterations), new Query(query)));

        // This is special case for KRAS wildtype. May need to come up with a better plan for this.
        if ((gene.getHugoSymbol().equals("KRAS") || gene.getHugoSymbol().equals("NRAS"))
            && query.getVariant() != null
            && StringUtils.containsIgnoreCase(query.getVariant(), "wildtype")) {
            if (oncoTreeTypes.contains(TumorTypeUtils.getOncoTreeCancerType("Colorectal Cancer"))) {
                result.setGeneSummary(new Summary("RAS (KRAS/NRAS) which is wildtype (not mutated) in this sample, encodes an upstream activator of the pro-oncogenic MAP- and PI3-kinase pathways and is mutated in approximately 40% of late stage colorectal cancers."));
                result.setVariantSummary(new Summary("The absence of a mutation in the RAS genes is clinically important because it expands approved treatments available to treat this tumor. RAS status in stage IV colorectal cancer influences patient responses to the anti-EGFR antibody therapies cetuximab and panitumumab."));
                result.setTumorTypeSummary(new Summary("These drugs are FDA-approved for the treatment of KRAS wildtype colorectal tumors together with chemotherapy or alone following progression through standard chemotherapy."));
            } else {
                result.setVariantSummary(new Summary(""));
                result.setTumorTypeSummary(new Summary(""));
                result.setTreatments(new ArrayList<TreatmentInfo>());
                result.setHighestResistanceLevel(null);
                result.setHighestSensitiveLevel(null);
            }
        }

        return result;
    }

    private static KnownEffect getKnownEffect(String knowEffect, Evidence evidence) {
        References references = MainUtils.getReferencesFromArticles(evidence.getArticles());
        return new KnownEffect(knowEffect, references, evidence.getDescription(), evidence.getLastEdit());
    }

    private static void getGeneBackground(SearchResult searchResult) {
        Set<Evidence> geneBackground = EvidenceUtils.getRelevantEvidences(new Query(searchResult.getQuery()), searchResult.getQuery().getSource(), null, Collections.singleton(EvidenceType.GENE_BACKGROUND), null);
        if (geneBackground != null && geneBackground.size() > 0) {
            Evidence evidence = geneBackground.iterator().next();
            References references = MainUtils.getReferencesFromArticles(evidence.getArticles());
            Summary summary = new Summary(evidence.getDescription(), references, evidence.getLastEdit());
            searchResult.setGeneBackground(summary);
        }
    }

    private static List<TreatmentInfo> getTreatmentsInfo(Set<Evidence> evidences) {
        List<TreatmentInfo> treatments = new ArrayList<>();
        if (evidences != null) {
            List<Evidence> sortedEvidence = EvidenceUtils.sortEvidenceByLevenAndId(evidences);

            for (Evidence evidence : sortedEvidence) {
                References reference = MainUtils.getReferencesFromArticles(evidence.getArticles());
                for (Treatment treatment : evidence.getTreatments()) {
                    TreatmentInfo treatmentInfo = new TreatmentInfo(
                        treatment.getDrugs(),
                        treatment.getApprovedIndications(),
                        evidence.getLevelOfEvidence(),
                        reference,
                        evidence.getNccnGuidelines() == null ? new ArrayList<NccnGuideline>() : new ArrayList<>(evidence.getNccnGuidelines()),
                        evidence.getAlterations() == null ? new ArrayList<Alteration>() : new ArrayList<>(evidence.getAlterations()),
                        evidence.getOncoTreeType() == null ? new ArrayList<TumorType>() : Collections.singletonList(evidence.getOncoTreeType()),
                        evidence.getDescription(),
                        evidence.getLastEdit()
                    );
                    treatments.add(treatmentInfo);
                }
            }
        }
        return treatments;
    }

    private static void annotateKnownAltOncogenicity(SearchResult searchResult, Alteration alteration) {
        Oncogenicity oncogenicity = null;

        // Find alteration specific oncogenicity
        Set<Evidence> oncogenicEvis = new HashSet<>(EvidenceUtils.getEvidence(Collections.singletonList(alteration),
            Collections.singleton(EvidenceType.ONCOGENIC), null));
        if (oncogenicEvis != null) {
            oncogenicity = MainUtils.findHighestOncogenicByEvidences(oncogenicEvis);

        }
        // If there is no oncogenic info available for this variant, find oncogenicity from relevant variants
        if (oncogenicity == null) {
            oncogenicEvis = EvidenceUtils.getRelevantEvidences(new Query(searchResult.getQuery()), searchResult.getQuery().getSource(), null,
                Collections.singleton(EvidenceType.ONCOGENIC), null);
            oncogenicity = MainUtils.findHighestOncogenicByEvidences(oncogenicEvis);
        }
        if (oncogenicity != null) {
            Evidence evidence = MainUtils.findEvidenceByHighestOncogenicityInEvidence(oncogenicEvis, oncogenicity);
            searchResult.setOncogenic(getKnownEffect(oncogenicity.getOncogenic(), evidence));
        }
    }

    private static void annotateKnownAltMutationEffect(SearchResult searchResult, Alteration alteration) {
        Map<String, Object> map = new HashedMap();

        // Only annotate mutation effect if it hasn't been annotated.
        // Possible scenario: Oncogenicity is from alternative allele. We need to match mutation effect inside of
        // function annotateKnownAltOncogenicity.
//        if(searchResult.getMutationEffect() == null) {
        // Find alteration specific mutation effects
        Set<Evidence> mutationEffectEvis = new HashSet<>(EvidenceUtils.getEvidence(Collections.singletonList(alteration),
            Collections.singleton(EvidenceType.MUTATION_EFFECT), null));

        if (searchResult != null) {
            map = EvidenceUtils.getMutationEffectMapFromEvidence(mutationEffectEvis);
        }
        // If there is no oncogenic info available for this variant, find oncogenicity from relevant variants
        if (!map.containsKey("mutationEffect")) {
            mutationEffectEvis = EvidenceUtils.getRelevantEvidences(new Query(searchResult.getQuery()), searchResult.getQuery().getSource(), null,
                Collections.singleton(EvidenceType.MUTATION_EFFECT), null);
            map = EvidenceUtils.getMutationEffectMapFromEvidence(mutationEffectEvis);
        }
        if (map.containsKey("mutationEffect")) {
            MutationEffect mutationEffect = (MutationEffect) map.get("mutationEffect");
            searchResult.setMutationEffect(getKnownEffect(mutationEffect.getMutationEffect(), (Evidence) map.get("evidence")));
        }
//        }
    }

    private static void annotateVusInfo(SearchResult searchResult) {
        VUSStatus vusStatus = new VUSStatus();
        Set<Evidence> vusEvidences = EvidenceUtils.getRelevantEvidences(new Query(searchResult.getQuery()), searchResult.getQuery().getSource(),
            null, Collections.singleton(EvidenceType.VUS), null);

        if (vusEvidences != null && vusEvidences.size() > 0) {
            // It's supposed to only return no more than one evidence
            Evidence evidence = vusEvidences.iterator().next();
            vusStatus.setStatus(true);
            vusStatus.setLastUpdate(evidence.getLastEdit());
        } else {
            vusStatus.setStatus(false);
        }
        searchResult.setVUS(vusStatus);
    }

    private static Map<String, LevelOfEvidenceWithTime> findHighestLevel(Set<TreatmentInfo> treatments) {
        int levelSIndex = -1;
        int levelRIndex = -1;
        TreatmentInfo levelSTreatment = null;
        TreatmentInfo levelRTreatment = null;


        Map<String, LevelOfEvidenceWithTime> levels = new HashMap<>();

        if (treatments != null) {
            for (TreatmentInfo treatment : treatments) {
                LevelOfEvidence levelOfEvidence = treatment.getLevel();
                if (levelOfEvidence != null) {
                    int _index = -1;
                    if (LevelUtils.isSensitiveLevel(levelOfEvidence)) {
                        _index = LevelUtils.SENSITIVE_LEVELS.indexOf(levelOfEvidence);
                        if (_index > levelSIndex) {
                            levelSIndex = _index;
                            levelSTreatment = treatment;
                        }
                    } else if (LevelUtils.isResistanceLevel(levelOfEvidence)) {
                        _index = LevelUtils.RESISTANCE_LEVELS.indexOf(levelOfEvidence);
                        if (_index > levelRIndex) {
                            levelRIndex = _index;
                            levelRTreatment = treatment;
                        }
                    }
                }
            }
        }

        levels.put("sensitive", levelSIndex > -1 ? new LevelOfEvidenceWithTime(LevelUtils.SENSITIVE_LEVELS.get(levelSIndex), levelSTreatment.getLastUpdate()) : null);
        levels.put("resistant", levelRIndex > -1 ? new LevelOfEvidenceWithTime(LevelUtils.RESISTANCE_LEVELS.get(levelRIndex), levelRTreatment.getLastUpdate()) : null);
        return levels;
    }

    private static Alteration getAlteration(Gene gene, QueryV2 query) {

        Alteration alteration = AlterationUtils.findAlteration(gene, query.getVariant());

        if (alteration == null) {
            alteration = AlterationUtils.getAlterationByQuery(new Query(query));
        }
        return alteration;
    }
}
