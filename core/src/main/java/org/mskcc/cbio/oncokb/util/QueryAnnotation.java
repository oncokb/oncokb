package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
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

        if (query.getSource() == null || query.getSource().isEmpty()) {
            query.setSource("oncotree");
        }

        if (query.getHighestLevelOnly() == null) {
            query.setHighestLevelOnly(false);
        }

        if (query.getLevels() == null) {
            query.setLevels(LevelUtils.getPublicAndOtherIndicationLevels());
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

    public static SearchResult annotateSearchQuery(Query query) {
        SearchResult result = new SearchResult();
        OtherSources otherSources = new OtherSources();
        result.setOtherSources(otherSources);
        result.setQuery(query);

        try {
            if (query == null) {
                throw new Exception();
            }
            EnrichQuery(query);

            // Check whether query gene is annotated.
            Gene gene = GeneUtils.getGeneByQuery(query);
            if (gene != null) {
                result.setGeneAnnotated(true);
                result.setGeneSummary(SummaryUtils.geneSummary(gene));
            } else {
                result.setGeneAnnotated(false);
                throw new Exception();
            }

            // Check whether query variant is annotated.
            Alteration alteration = AlterationUtils.getAlterationByQuery(query);

            // Whether alteration is hotpot from Matt's list
            result.getOtherSources().setHotspot(HotspotUtils.isHotspot(alteration));

            // Get alternative alleles info
            List<Alteration> alleles = AlterationUtils.getAlleleAlterations(alteration);
            result.setAlterativeVariantAlleleAnnotated(!(alleles == null || alleles.size() == 0));

            // Get relevant alterations
            List<Alteration> relevantAlterations = AlterationUtils.getRelevantAlterationsByQuery(query);

            // Get list of alterations which are not VUS
            List<Alteration> nonVUSRelevantAlts = new ArrayList<>();
            nonVUSRelevantAlts.addAll(AlterationUtils.excludeVUS(relevantAlterations));

            if (relevantAlterations.size() == 0) {
                result.setVariantAnnotated(false);
                throw new Exception();
            }

            result.setVariantAnnotated(true);


            List<TumorType> oncoTreeTypes = new ArrayList<>();
            if (MainUtils.isNotNullOrEmpty(query.getTumorType())) {
                oncoTreeTypes.addAll(TumorTypeUtils.getMappedOncoTreeTypesBySource(query.getTumorType(), query.getSource()));
            }

            Set<Evidence> treatmentEvidences = new HashSet<>();

            if (nonVUSRelevantAlts.size() > 0) {
                Oncogenicity oncogenicity = null;

                // Find alteration specific oncogenicity
                Set<Evidence> oncogenicEvis = new HashSet<>(EvidenceUtils.getEvidence(Collections.singletonList(alteration),
                    Collections.singleton(EvidenceType.ONCOGENIC), null));
                if (oncogenicEvis != null) {
                    oncogenicity = MainUtils.findHighestOncogenicByEvidences(oncogenicEvis);

                }
                // If there is no oncogenic info available for this variant, find oncogenicity from relevant variants
                if (oncogenicity == null) {
                    oncogenicEvis = EvidenceUtils.getRelevantEvidences(query, query.getSource(), null,
                        Collections.singleton(EvidenceType.ONCOGENIC), null);
                    oncogenicity = MainUtils.findHighestOncogenicByEvidences(oncogenicEvis);
                }
                if (oncogenicity != null) {
                    Evidence evidence = MainUtils.findEvidenceByHighestOncogenicityInEvidence(oncogenicEvis, oncogenicity);
                    result.setOncogenic(getKnownEffect(oncogenicity, evidence));
                }

                treatmentEvidences = EvidenceUtils.keepHighestLevelForSameTreatments(
                    EvidenceUtils.getRelevantEvidences(query, query.getSource(), null,
                        MainUtils.getTreatmentEvidenceTypes(),
                        (query.getLevels() != null ?
                            new HashSet<>(CollectionUtils.intersection(query.getLevels(),
                                LevelUtils.getPublicAndOtherIndicationLevels())) : LevelUtils.getPublicAndOtherIndicationLevels())));
            } else if (result.getAlterativeVariantAlleleAnnotated() || (result.getVUS() != null && result.getVUS().getStatus())) {
                Alteration oncogenicAllele = AlterationUtils.findOncogenicAllele(alleles);
                List<Alteration> alleleAndRelevantAlterations = new ArrayList<>();
                Set<Alteration> oncogenicMutations = null;

                alleleAndRelevantAlterations.addAll(alleles);
                if (oncogenicAllele != null) {
                    oncogenicMutations = AlterationUtils.getOncogenicMutations(oncogenicAllele);
                    alleleAndRelevantAlterations.addAll(oncogenicMutations);
                }

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
                    result.setOncogenic(getKnownEffect(MainUtils.setToAlleleOncogenicity(oncogenicity), evidence));
                }
            }
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
                    ;
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
                result.setTumorTypeSummary(SummaryUtils.tumorTypeSummary(gene, query,
                    new ArrayList<>(relevantAlterations),
                    new HashSet<>(oncoTreeTypes)));
            }

            // Mutation summary
            result.setVariantSummary(SummaryUtils.oncogenicSummary(gene,
                new ArrayList<>(relevantAlterations), query));

            // This is special case for KRAS wildtype. May need to come up with a better plan for this.
            if ((gene.getHugoSymbol().equals("KRAS") || gene.getHugoSymbol().equals("NRAS"))
                && query.getAlteration() != null
                && StringUtils.containsIgnoreCase(query.getAlteration(), "wildtype")) {
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

            //TODO: isVUS
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            result.updateLastUpdate();
            return result;
        }
    }

    private static KnownEffect getKnownEffect(Oncogenicity oncogenicity, Evidence evidence) {
        List<String> pmids = new ArrayList<>();
        List<Abstract> abstracts = new ArrayList<>();
        if (evidence.getArticles() != null) {
            for (Article article : evidence.getArticles()) {
                if (article.getAbstractContent() != null) {
                    abstracts.add(new Abstract(article.getAbstractContent(), article.getLink()));
                } else if (article.getPmid() != null) {
                    pmids.add(article.getPmid());
                }
            }
        }
        return new KnownEffect(oncogenicity.getOncogenic(), pmids, abstracts, evidence.getDescription(), evidence.getLastEdit());
    }

    private static List<TreatmentInfo> getTreatmentsInfo(Set<Evidence> evidences) {
        List<TreatmentInfo> treatments = new ArrayList<>();
        if (evidences != null) {
            List<Evidence> sortedEvidence = EvidenceUtils.sortEvidenceByLevenAndId(evidences);

            for (Evidence evidence : sortedEvidence) {
                List<Article> pmids = new ArrayList<>();
                List<Article> abstracts = new ArrayList<>();
                for (Article article : evidence.getArticles()) {
                    if (article.getPmid() != null) {
                        pmids.add(article);
                    }
                    if (article.getAbstractContent() != null) {
                        abstracts.add(article);
                    }
                }
                for (Treatment treatment : evidence.getTreatments()) {
                    TreatmentInfo treatmentInfo = new TreatmentInfo(
                        treatment.getDrugs(),
                        treatment.getApprovedIndications(),
                        evidence.getLevelOfEvidence(),
                        pmids,
                        abstracts,
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
}
