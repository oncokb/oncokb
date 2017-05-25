package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.oncotree.model.TumorType;

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

        Gene gene = null;
        List<Alteration> relevantAlterations = new ArrayList<>();

        // Queried alteration
        Alteration alteration;

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
        if (query.getHugoSymbol() != null
            && query.getAlterationType() != null &&
            query.getAlterationType().equalsIgnoreCase("fusion")) {
            List<String> geneStrsList = Arrays.asList(query.getHugoSymbol().split("-"));
            Set<String> geneStrsSet = new HashSet<>();

            // If the query only indicates this is a fusion event with associated genes but no alteration specified,
            // need to attach Fusions to the query.
            if (query.getAlteration() == null || query.getAlteration().isEmpty()) {
                query.setAlteration("Fusions");
            }

            if (geneStrsList != null) {
                geneStrsSet = new HashSet<>(geneStrsList);
            }

            // Deal with two different genes fusion event.
            if (geneStrsSet.size() == 2) {
                List<Gene> tmpGenes = new ArrayList<>();
                for (String geneStr : geneStrsSet) {
                    Gene tmpGene = GeneUtils.getGeneByHugoSymbol(geneStr);
                    if (tmpGene != null) {
                        tmpGenes.add(tmpGene);
                    }
                }
                if (tmpGenes.size() > 0) {
                    query.setAlteration(query.getHugoSymbol() + " fusion");
                    for (Gene tmpGene : tmpGenes) {
                        Alteration alt = AlterationUtils.getAlteration(tmpGene.getHugoSymbol(), query.getAlteration(),
                            null, null, null, null);
                        AlterationUtils.annotateAlteration(alt, alt.getAlteration());

                        List<Alteration> tmpRelevantAlts = AlterationUtils.getRelevantAlterations(alt);
                        if (tmpRelevantAlts != null && tmpRelevantAlts.size() > 0) {
                            gene = tmpGene;
                            relevantAlterations = tmpRelevantAlts;
                            break;
                        }
                    }
                    // None of relevant alterations found in both genes.
                    if (gene == null) {
                        gene = tmpGenes.get(0);
                    }
                }
            } else if (geneStrsSet.size() > 0) {
                String geneStr = geneStrsSet.iterator().next();
                if (geneStr != null) {
                    Gene tmpGene = GeneUtils.getGeneByHugoSymbol(geneStr);
                    if (tmpGene != null) {
                        gene = tmpGene;
                        Alteration alt = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getAlteration(),
                            null, null, null, null);
                        AlterationUtils.annotateAlteration(alt, alt.getAlteration());
                        relevantAlterations = AlterationUtils.getRelevantAlterations(alt);

                        // Map Truncating Mutations to single gene fusion event
                        Alteration truncatingMutations = AlterationUtils.getTruncatingMutations(gene);
                        if (truncatingMutations != null && !relevantAlterations.contains(truncatingMutations)) {
                            relevantAlterations.add(truncatingMutations);
                        }
                    }
                }
            }
        } else {
            gene = GeneUtils.getGene(query.getEntrezGeneId(), query.getHugoSymbol());
            if (gene != null) {
                Alteration alt = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getAlteration(),
                    null, query.getConsequence(), query.getProteinStart(), query.getProteinEnd());

                AlterationUtils.annotateAlteration(alt, alt.getAlteration());

                relevantAlterations = AlterationUtils.getRelevantAlterations(alt);
            }
        }


        if (gene != null) {
            query.setHugoSymbol(gene.getHugoSymbol());
            query.setEntrezGeneId(gene.getEntrezGeneId());

            indicatorQuery.setGeneExist(true);

            // Gene summary
            indicatorQuery.setGeneSummary(SummaryUtils.geneSummary(gene));

            alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getAlteration(),
                null, query.getConsequence(), query.getProteinStart(), query.getProteinEnd());
            AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());

            List<Alteration> nonVUSRelevantAlts = AlterationUtils.excludeVUS(relevantAlterations);
            Map<String, LevelOfEvidence> highestLevels = new HashMap<>();
            List<Alteration> alleles = AlterationUtils.getAlleleAlterations(alteration);
            List<TumorType> oncoTreeTypes = new ArrayList<>();

            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
            Alteration matchedAlt = alterationBo.findAlteration(alteration.getGene(), alteration.getAlterationType(), alteration.getAlteration());
            indicatorQuery.setVariantExist(matchedAlt != null);

            // Whether alteration is hotpot from Matt's list
            if (query.getProteinEnd() == null || query.getProteinStart() == null) {
                indicatorQuery.setHotspot(HotspotUtils.isHotspot(alteration));
            } else {
                indicatorQuery.setHotspot(HotspotUtils.isHotspot(alteration));
            }

            if (query.getTumorType() != null) {
                oncoTreeTypes = TumorTypeUtils.getMappedOncoTreeTypesBySource(query.getTumorType(), source);
            }

            indicatorQuery.setVUS(isVUS(matchedAlt == null ? alteration : matchedAlt));

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

                // Find Oncogenicity from alternative alleles
                if (oncogenicity == null && indicatorQuery.getAlleleExist()) {
                    oncogenicity = MainUtils.setToAlleleOncogenicity(MainUtils.findHighestOncogenicByEvidences(new HashSet<>(EvidenceUtils.getEvidence(new ArrayList<>(alleles), Collections.singleton(EvidenceType.ONCOGENIC), null))));
                }

                // If there is no oncogenic info available for this variant, find oncogenicity from relevant variants
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
                    Set<Evidence> filteredEvis = new HashSet<>();
                    // Get highest sensitive evidences
                    Set<Evidence> sensitiveEvidences = EvidenceUtils.getSensitiveEvidences(treatmentEvidences);
                    filteredEvis.addAll(EvidenceUtils.getOnlySignificantLevelsEvidences(sensitiveEvidences));

                    // Get highest resistance evidences
                    Set<Evidence> resistanceEvidences = EvidenceUtils.getResistanceEvidences(treatmentEvidences);
                    filteredEvis.addAll(EvidenceUtils.getOnlyHighestLevelEvidences(resistanceEvidences));

                    treatmentEvidences = filteredEvis;
                }
                if (!treatmentEvidences.isEmpty()) {
                    List<IndicatorQueryTreatment> treatments = getIndicatorQueryTreatments(treatmentEvidences);

                    indicatorQuery.setTreatments(treatments);
                    highestLevels = findHighestLevel(new HashSet<>(treatments));
                    indicatorQuery.setHighestSensitiveLevel(highestLevels.get("sensitive"));
                    indicatorQuery.setHighestResistanceLevel(highestLevels.get("resistant"));
                    indicatorQuery.setOtherSignificantSensitiveLevels(getOtherSignificantLevels(indicatorQuery.getHighestSensitiveLevel(), "sensitive", treatmentEvidences));
                    indicatorQuery.setOtherSignificantResistanceLevels(getOtherSignificantLevels(indicatorQuery.getHighestResistanceLevel(), "resistance", treatmentEvidences));
                }
            }

            // Tumor type summary
            if (query.getTumorType() != null) {
                indicatorQuery.setTumorTypeSummary(SummaryUtils.tumorTypeSummary(gene, query,
                    new ArrayList<>(relevantAlterations),
                    new HashSet<>(oncoTreeTypes)));
            }

            // Mutation summary
            indicatorQuery.setVariantSummary(SummaryUtils.oncogenicSummary(gene, matchedAlt,
                new ArrayList<>(relevantAlterations), query));

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
        indicatorQuery.setLastUpdate(MainUtils.getDataVersionDate());

        // Give default oncogenicity if no data has been assigned.
        if (indicatorQuery.getOncogenic() == null) {
            indicatorQuery.setOncogenic("");
        }
        return indicatorQuery;
    }

    private static List<LevelOfEvidence> getOtherSignificantLevels(LevelOfEvidence highestLevel, String type, Set<Evidence> evidences) {
        List<LevelOfEvidence> otherSignificantLevels = new ArrayList<>();
        if (type != null && highestLevel != null && evidences != null) {
            if (type.equals("sensitive")) {
                if (highestLevel.equals(LevelOfEvidence.LEVEL_2B)) {
                    Map<LevelOfEvidence, Set<Evidence>> levels = EvidenceUtils.separateEvidencesByLevel(evidences);
                    if (levels.containsKey(LevelOfEvidence.LEVEL_3A)) {
                        otherSignificantLevels.add(LevelOfEvidence.LEVEL_3A);
                    }
                }
            } else if (type.equals("resistance")) {

            }
        }
        return otherSignificantLevels;
    }

    private static List<IndicatorQueryTreatment> getIndicatorQueryTreatments(Set<Evidence> evidences) {
        List<IndicatorQueryTreatment> treatments = new ArrayList<>();
        if (evidences != null) {
            List<Evidence> sortedEvidence = new ArrayList<>(evidences);

            Collections.sort(sortedEvidence, new Comparator<Evidence>() {
                public int compare(Evidence e1, Evidence e2) {
                    Integer comparison = LevelUtils.compareLevel(e1.getLevelOfEvidence(), e2.getLevelOfEvidence());

                    if (comparison != 0) {
                        return comparison;
                    }

                    if (e1.getId() == null) {
                        if (e2.getId() == null) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                    if (e2.getId() == null)
                        return -1;
                    return e1.getId() - e2.getId();
                }
            });

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

    private static Boolean isVUS(Alteration alteration) {
        if (alteration == null) {
            return false;
        }
        List<Alteration> alterations = AlterationUtils.excludeVUS(Collections.singletonList(alteration));
        return alterations.size() == 0;
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
