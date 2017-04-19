package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.oncotree.model.TumorType;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class IndicatorUtils {
    public static IndicatorQueryResp processQuery(Query query, String geneStatus,
                                                  Set<LevelOfEvidence> levels, String source, Boolean highestLevelOnly) {
        geneStatus = geneStatus != null ? geneStatus : "complete";
        highestLevelOnly = highestLevelOnly == null ? false : highestLevelOnly;

        IndicatorQueryResp indicatorQuery = new IndicatorQueryResp();
        indicatorQuery.setQuery(query);

        if (query == null) {
            return indicatorQuery;
        }

        // Set the alteration to empty string in order to get relevant variants.
        if (query.getAlteration() == null) {
            query.setAlteration("");
        }

        source = source == null ? "oncokb" : source;

        // Deal with fusion without primary gene
        // TODO: support entrezGeneId fusion
        QueryAnnotation.EnrichQuery(query);

        // Check whether query gene is annotated.
        Gene gene = GeneUtils.getGeneByQuery(query);

        // Check whether query variant is annotated.
        Alteration alteration = AlterationUtils.findAlteration(gene, query.getAlteration());

        if (alteration == null) {
            alteration = AlterationUtils.getAlterationByQuery(query);
        }

        // Get relevant alterations
        List<Alteration> relevantAlterations = AlterationUtils.getRelevantAlterationsByQuery(query);

        if (gene != null) {
            query.setHugoSymbol(gene.getHugoSymbol());
            query.setEntrezGeneId(gene.getEntrezGeneId());

            indicatorQuery.setGeneExist(true);

            // Gene summary
            indicatorQuery.setGeneSummary(SummaryUtils.geneSummary(gene).getSummary());

            List<Alteration> nonVUSRelevantAlts = AlterationUtils.excludeVUS(relevantAlterations);
            Map<String, LevelOfEvidence> highestLevels = new HashMap<>();
            List<Alteration> alleles = new ArrayList<>();
            List<TumorType> oncoTreeTypes = new ArrayList<>();

            if (relevantAlterations == null || relevantAlterations.size() == 0) {
                indicatorQuery.setVariantExist(false);
            } else {
                indicatorQuery.setVariantExist(true);
            }
            alleles = AlterationUtils.getAlleleAlterations(alteration);

            // Whether alteration is hotpot from Matt's list
            if (query.getProteinEnd() == null || query.getProteinStart() == null) {
                indicatorQuery.setHotspot(HotspotUtils.isHotspot(alteration));
            } else {
                indicatorQuery.setHotspot(HotspotUtils.isHotspot(alteration));
            }

            if (query.getTumorType() != null) {
                oncoTreeTypes = TumorTypeUtils.getMappedOncoTreeTypesBySource(query.getTumorType(), source);
            }

            indicatorQuery.setVUS(MainUtils.isVUS(
                EvidenceUtils.getRelevantEvidences(query, source,
                    geneStatus, Collections.singleton(EvidenceType.VUS), null)
            ));

            if (alleles == null || alleles.size() == 0) {
                indicatorQuery.setAlleleExist(false);
            } else {
                indicatorQuery.setAlleleExist(true);
            }

            Set<Evidence> treatmentEvidences = new HashSet<>();

            if (nonVUSRelevantAlts.size() > 0) {
                Oncogenicity oncogenicity = null;


                // Find alteration specific oncogenicity
                List<Evidence> selfAltOncogenicEvis = EvidenceUtils.getEvidence(Collections.singletonList(alteration),
                    Collections.singleton(EvidenceType.ONCOGENIC), null);
                if (selfAltOncogenicEvis != null) {
                    oncogenicity = MainUtils.findHighestOncogenicByEvidences(new HashSet<>(selfAltOncogenicEvis));
                }

                // If there is no oncogenic info availble for this variant, find oncogenicity from relevant variants
                if (oncogenicity == null) {
                    oncogenicity = MainUtils.findHighestOncogenicByEvidences(
                        EvidenceUtils.getRelevantEvidences(query, source, geneStatus,
                            Collections.singleton(EvidenceType.ONCOGENIC), null)
                    );
                }

                // Only set oncogenicity if no previous data assigned.
                if (indicatorQuery.getOncogenic() == null && oncogenicity != null) {
                    indicatorQuery.setOncogenic(oncogenicity.getOncogenic());
                }

                treatmentEvidences = EvidenceUtils.keepHighestLevelForSameTreatments(
                    EvidenceUtils.getRelevantEvidences(query, source, geneStatus,
                        MainUtils.getTreatmentEvidenceTypes(),
                        (levels != null ?
                            new HashSet<LevelOfEvidence>(CollectionUtils.intersection(levels,
                                LevelUtils.getPublicAndOtherIndicationLevels())) : LevelUtils.getPublicAndOtherIndicationLevels())));


            } else if (indicatorQuery.getAlleleExist() || indicatorQuery.getVUS()) {
                Alteration oncogenicAllele = AlterationUtils.findOncogenicAllele(alleles);
                List<Alteration> alleleAndRelevantAlterations = new ArrayList<>();
                Set<Alteration> oncogenicMutations = null;

                alleleAndRelevantAlterations.addAll(alleles);
                if (oncogenicAllele != null) {
                    oncogenicMutations = AlterationUtils.getOncogenicMutations(oncogenicAllele);
                    alleleAndRelevantAlterations.addAll(oncogenicMutations);
                }

                Oncogenicity oncogenicity = MainUtils.setToAlleleOncogenicity(MainUtils.findHighestOncogenicByEvidences(new HashSet<>(EvidenceUtils.getEvidence(new ArrayList<>(alleleAndRelevantAlterations), Collections.singleton(EvidenceType.ONCOGENIC), null))));
                treatmentEvidences = EvidenceUtils.keepHighestLevelForSameTreatments(
                    EvidenceUtils.convertEvidenceLevel(
                        EvidenceUtils.getEvidence(new ArrayList<>(alleles),
                            MainUtils.getSensitiveTreatmentEvidenceTypes(),
                            (levels != null ?
                                new HashSet<LevelOfEvidence>(CollectionUtils.intersection(levels,
                                    LevelUtils.getPublicAndOtherIndicationLevels())) : LevelUtils.getPublicAndOtherIndicationLevels())), new HashSet<>(oncoTreeTypes)));

                if (oncogenicMutations != null) {
                    treatmentEvidences.addAll(EvidenceUtils.keepHighestLevelForSameTreatments(
                        EvidenceUtils.convertEvidenceLevel(
                            EvidenceUtils.getEvidence(new ArrayList<>(oncogenicMutations),
                                MainUtils.getTreatmentEvidenceTypes(),
                                (levels != null ?
                                    new HashSet<LevelOfEvidence>(CollectionUtils.intersection(levels,
                                        LevelUtils.getPublicAndOtherIndicationLevels())) : LevelUtils.getPublicAndOtherIndicationLevels())), new HashSet<>(oncoTreeTypes))));
                }

                // Only set oncogenicity if no previous data assigned.
                if (indicatorQuery.getOncogenic() == null && oncogenicity != null) {
                    indicatorQuery.setOncogenic(oncogenicity.getOncogenic());
                }
            }

            // Set hotspot oncogenicity to Predicted Oncogenic
            if (indicatorQuery.getOncogenic() == null
                && indicatorQuery.getHotspot()) {
                indicatorQuery.setOncogenic(Oncogenicity.PREDICTED.getOncogenic());

                // Check whether the gene has Oncogenic Mutations annotated
                Alteration oncogenicMutation = AlterationUtils.findAlteration(gene, "Oncogenic Mutations");
                if (oncogenicMutation != null) {
                    relevantAlterations.add(oncogenicMutation);
                    treatmentEvidences.addAll(EvidenceUtils.keepHighestLevelForSameTreatments(
                        EvidenceUtils.convertEvidenceLevel(
                            EvidenceUtils.getEvidence(Collections.singletonList(oncogenicMutation),
                                MainUtils.getTreatmentEvidenceTypes(),
                                (levels != null ?
                                    new HashSet<>(CollectionUtils.intersection(levels,
                                        LevelUtils.getPublicAndOtherIndicationLevels())) : LevelUtils.getPublicAndOtherIndicationLevels())), new HashSet<>(oncoTreeTypes))));
                }
            }

            if (treatmentEvidences != null && !treatmentEvidences.isEmpty()) {
                if (highestLevelOnly) {
                    treatmentEvidences = EvidenceUtils.keepHighestSensitiveResistanceTreatmentEvidences(treatmentEvidences);
                }
                if (!treatmentEvidences.isEmpty()) {
                    List<IndicatorQueryTreatment> treatments = getIndicatorQueryTreatments(treatmentEvidences);
                    indicatorQuery.setTreatments(treatments);
                    highestLevels = findHighestLevel(new HashSet<>(treatments));
                    indicatorQuery.setHighestSensitiveLevel(highestLevels.get("sensitive"));
                    indicatorQuery.setHighestResistanceLevel(highestLevels.get("resistant"));
                    indicatorQuery.setOtherSignificantSensitiveLevels(
                        new ArrayList<>(
                            LevelUtils.getLevelsFromEvidenceByLevels(
                                new HashSet<>(
                                    getOtherSignificantLevelsEvidences(
                                        indicatorQuery.getHighestSensitiveLevel(), "sensitive", treatmentEvidences
                                    )
                                ), null
                            )
                        )
                    );
                    indicatorQuery.setOtherSignificantResistanceLevels(
                        new ArrayList<>(
                            LevelUtils.getLevelsFromEvidenceByLevels(
                                new HashSet<>(
                                    getOtherSignificantLevelsEvidences(
                                        indicatorQuery.getHighestResistanceLevel(), "resistance", treatmentEvidences
                                    )
                                ), null
                            )
                        )
                    );
                }
            }

            // Tumor type summary
            if (query.getTumorType() != null) {
                indicatorQuery.setTumorTypeSummary(SummaryUtils.tumorTypeSummary(gene, query,
                    new ArrayList<>(relevantAlterations),
                    new HashSet<>(oncoTreeTypes)).getSummary());
            }

            // Mutation summary
            indicatorQuery.setVariantSummary(SummaryUtils.oncogenicSummary(gene,
                new ArrayList<>(relevantAlterations), query).getSummary());

            // This is special case for KRAS wildtype. May need to come up with a better plan for this.
            if (gene != null && (gene.getHugoSymbol().equals("KRAS") || gene.getHugoSymbol().equals("NRAS"))
                && query.getAlteration() != null
                && StringUtils.containsIgnoreCase(query.getAlteration(), "wildtype")) {
                if (oncoTreeTypes.contains(TumorTypeUtils.getOncoTreeCancerType("Colorectal Cancer"))) {
                    indicatorQuery.setGeneSummary("RAS (KRAS/NRAS) which is wildtype (not mutated) in this sample, encodes an upstream activator of the pro-oncogenic MAP- and PI3-kinase pathways and is mutated in approximately 40% of late stage colorectal cancers.");
                    indicatorQuery.setVariantSummary("The absence of a mutation in the RAS genes is clinically important because it expands approved treatments available to treat this tumor. RAS status in stage IV colorectal cancer influences patient responses to the anti-EGFR antibody therapies cetuximab and panitumumab.");
                    indicatorQuery.setTumorTypeSummary("These drugs are FDA-approved for the treatment of KRAS wildtype colorectal tumors together with chemotherapy or alone following progression through standard chemotherapy.");
                } else {
                    indicatorQuery.setVariantSummary("");
                    indicatorQuery.setTumorTypeSummary("");
                    indicatorQuery.setTreatments(new ArrayList<IndicatorQueryTreatment>());
                    indicatorQuery.setHighestResistanceLevel(null);
                    indicatorQuery.setHighestSensitiveLevel(null);
                }
            }
        } else {
            indicatorQuery.setGeneExist(false);
        }
        indicatorQuery.setDataVersion(MainUtils.getDataVersion());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        indicatorQuery.setLastUpdate(sdf.format(MainUtils.getDataVersionDate()));

        // Give default oncogenicity if no data has been assigned.
        if (indicatorQuery.getOncogenic() == null) {
            indicatorQuery.setOncogenic("");
        }
        return indicatorQuery;
    }

    public static List<Evidence> getOtherSignificantLevelsEvidences(LevelOfEvidence highestLevel, String type, Set<Evidence> evidences) {
        List<Evidence> otherSignificantLevelsEvidences = new ArrayList<>();
        if (type != null && highestLevel != null && evidences != null) {
            if (type.equals("sensitive")) {
                if (highestLevel.equals(LevelOfEvidence.LEVEL_2B)) {
                    Map<LevelOfEvidence, Set<Evidence>> levels = EvidenceUtils.separateEvidencesByLevel(evidences);
                    if (levels.containsKey(LevelOfEvidence.LEVEL_3A)) {
                        for (Evidence evidence : evidences) {
                            if (evidence.getLevelOfEvidence() != null && evidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_3A)) {
                                otherSignificantLevelsEvidences.add(evidence);
                            }
                        }
                    }
                }
            } else if (type.equals("resistance")) {

            }
        }
        return otherSignificantLevelsEvidences;
    }

    private static List<IndicatorQueryTreatment> getIndicatorQueryTreatments(Set<Evidence> evidences) {
        List<IndicatorQueryTreatment> treatments = new ArrayList<>();
        if (evidences != null) {
            List<Evidence> sortedEvidence = EvidenceUtils.sortEvidenceByLevenAndId(evidences);

            for (Evidence evidence : sortedEvidence) {
                Set<String> pmids = new HashSet<>();
                Set<ArticleAbstract> abstracts = new HashSet<>();
                for (Article article : evidence.getArticles()) {
                    if (article.getPmid() != null) {
                        pmids.add(article.getPmid());
                    }
                    if (article.getAbstractContent() != null) {
                        ArticleAbstract articleAbstract = new ArticleAbstract();
                        articleAbstract.setAbstractContent(article.getAbstractContent());
                        articleAbstract.setLink(article.getLink());
                        abstracts.add(articleAbstract);
                    }
                }
                for (Treatment treatment : evidence.getTreatments()) {
                    IndicatorQueryTreatment indicatorQueryTreatment = new IndicatorQueryTreatment();
                    indicatorQueryTreatment.setDrugs(treatment.getDrugs());
                    indicatorQueryTreatment.setApprovedIndications(treatment.getApprovedIndications());
                    indicatorQueryTreatment.setLevel(evidence.getLevelOfEvidence());
                    indicatorQueryTreatment.setPmids(pmids);
                    indicatorQueryTreatment.setAbstracts(abstracts);
                    treatments.add(indicatorQueryTreatment);
                }
            }
        }
        return treatments;
    }

    private static Map<String, LevelOfEvidence> findHighestLevel(Set<IndicatorQueryTreatment> treatments) {
        int levelSIndex = -1;
        int levelRIndex = -1;

        Map<String, LevelOfEvidence> levels = new HashMap<>();

        if (treatments != null) {
            for (IndicatorQueryTreatment treatment : treatments) {
                LevelOfEvidence levelOfEvidence = treatment.getLevel();
                if (levelOfEvidence != null) {
                    int _index = -1;
                    if (LevelUtils.isSensitiveLevel(levelOfEvidence)) {
                        _index = LevelUtils.SENSITIVE_LEVELS.indexOf(levelOfEvidence);
                        if (_index > levelSIndex) {
                            levelSIndex = _index;
                        }
                    } else if (LevelUtils.isResistanceLevel(levelOfEvidence)) {
                        _index = LevelUtils.RESISTANCE_LEVELS.indexOf(levelOfEvidence);
                        if (_index > levelRIndex) {
                            levelRIndex = _index;
                        }
                    }
                }
            }
        }
        levels.put("sensitive", levelSIndex > -1 ? LevelUtils.SENSITIVE_LEVELS.get(levelSIndex) : null);
        levels.put("resistant", levelRIndex > -1 ? LevelUtils.RESISTANCE_LEVELS.get(levelRIndex) : null);
        return levels;
    }
}
