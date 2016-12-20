package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;

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

        if (query == null) {
            return indicatorQuery;
        }
        // Deal with fusion without primary gene
        // TODO: support entrezGeneId fusion
        if (query.getHugoSymbol() != null
            && query.getAlterationType() != null &&
            query.getAlterationType().equalsIgnoreCase("fusion")) {
            List<String> geneStrsList = Arrays.asList(query.getHugoSymbol().split("-"));
            Set<String> geneStrsSet = new HashSet<>();
            if (geneStrsList != null) {
                geneStrsSet = new HashSet<>(geneStrsList);
            }
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
            }
        } else {
            gene = query.getEntrezGeneId() == null ? GeneUtils.getGeneByHugoSymbol(query.getHugoSymbol()) :
                GeneUtils.getGeneByHugoSymbol(query.getHugoSymbol());
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

            List<Alteration> nonVUSRelevantAlts = AlterationUtils.excludeVUS(relevantAlterations);
            Map<String, LevelOfEvidence> highestLevels = new HashMap<>();
            List<Alteration> alleles = new ArrayList<>();
            List<OncoTreeType> oncoTreeTypes = new ArrayList<>();

            if (relevantAlterations == null || relevantAlterations.size() == 0) {
                indicatorQuery.setVariantExist(false);

                Alteration alteration = AlterationUtils.getAlteration(query.getHugoSymbol(), query.getAlteration(),
                    null, query.getConsequence(), query.getProteinStart(), query.getProteinEnd());
                if (alteration != null) {
                    alleles = AlterationUtils.getAlleleAlterations(alteration);
                }
            } else {
                indicatorQuery.setVariantExist(true);
                if (!relevantAlterations.isEmpty()) {
                    for (Alteration alteration : relevantAlterations) {
                        alleles.addAll(AlterationUtils.getAlleleAlterations(alteration));
                    }
                }
            }

            if (query.getProteinEnd() == null || query.getProteinStart() == null) {
                Alteration alteration = AlterationUtils.getAlteration(query.getHugoSymbol(), query.getAlteration(),
                    null, query.getConsequence(), query.getProteinStart(), query.getProteinEnd());
                AlterationUtils.annotateAlteration(alteration, query.getAlteration());
                indicatorQuery.setHotspot(HotspotUtils.isHotspot(gene.getHugoSymbol(), alteration.getProteinStart(), alteration.getProteinEnd()));
            } else {
                indicatorQuery.setHotspot(HotspotUtils.isHotspot(gene.getHugoSymbol(), query.getProteinStart(), query.getProteinEnd()));
            }

            if (query.getTumorType() != null) {
                oncoTreeTypes = TumorTypeUtils.getMappedOncoTreeTypesBySource(query.getTumorType(), source);
                // Tumor type summary
                indicatorQuery.setTumorTypeSummary(SummaryUtils.tumorTypeSummary(gene, query.getAlteration(),
                    new ArrayList<Alteration>(relevantAlterations), query.getTumorType(),
                    new HashSet<OncoTreeType>(oncoTreeTypes)));
            }

            // Mutation summary
            indicatorQuery.setVariantSummary(SummaryUtils.oncogenicSummary(gene,
                new ArrayList<Alteration>(relevantAlterations), query.getAlteration(), false));

            indicatorQuery.setVUS(isVUS(
                EvidenceUtils.getRelevantEvidences(query, source,
                    geneStatus, Collections.singleton(EvidenceType.VUS), null)
            ));

            if (alleles == null || alleles.size() == 0) {
                indicatorQuery.setAlleleExist(false);
            } else {
                indicatorQuery.setAlleleExist(true);
            }

            Set<Evidence> treatmentEvidences = null;

            if (nonVUSRelevantAlts.size() > 0) {
                Oncogenicity oncogenicity = MainUtils.findHighestOncogenicByEvidences(
                    EvidenceUtils.getRelevantEvidences(query, source, geneStatus,
                        Collections.singleton(EvidenceType.ONCOGENIC), null)
                );
                indicatorQuery.setOncogenic(oncogenicity == null ? "" : oncogenicity.getOncogenic());

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

                indicatorQuery.setOncogenic(oncogenicity == null ? "" : oncogenicity.getOncogenic());
            }

            if (treatmentEvidences != null) {
                if (highestLevelOnly) {
                    Set<Evidence> filteredEvis = new HashSet<>();
                    // Get highest sensitive evidences
                    Set<Evidence> sensitiveEvidences = EvidenceUtils.getSensitiveEvidences(treatmentEvidences);
                    filteredEvis.addAll(EvidenceUtils.getOnlyHighestLevelEvidences(sensitiveEvidences));

                    // Get highest resistance evidences
                    Set<Evidence> resistanceEvidences = EvidenceUtils.getResistanceEvidences(treatmentEvidences);
                    filteredEvis.addAll(EvidenceUtils.getOnlyHighestLevelEvidences(resistanceEvidences));

                    treatmentEvidences = filteredEvis;
                }
                if (treatmentEvidences != null) {
                    List<IndicatorQueryTreatment> treatments = getIndicatorQueryTreatments(treatmentEvidences);

                    indicatorQuery.setTreatments(treatments);
                    highestLevels = findHighestLevel(new HashSet<>(treatments));
                    indicatorQuery.setHighestSensitiveLevel(highestLevels.get("sensitive") == null ? "" : highestLevels.get("sensitive").name());
                    indicatorQuery.setHighestResistanceLevel(highestLevels.get("resistant") == null ? "" : highestLevels.get("resistant").name());
                }
            }

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
                    indicatorQuery.setHighestResistanceLevel("");
                    indicatorQuery.setHighestSensitiveLevel("");
                }
            }
        } else {
            indicatorQuery.setGeneExist(false);
        }
        indicatorQuery.setDataVersion(MainUtils.getDataVersion());
        indicatorQuery.setLastUpdate(MainUtils.getDataVersionDate());

        return indicatorQuery;
    }

    private static List<IndicatorQueryTreatment> getIndicatorQueryTreatments(Set<Evidence> evidences) {
        List<IndicatorQueryTreatment> treatments = new ArrayList<>();
        if (evidences != null) {
            List<Evidence> sortedEvidence = new ArrayList<>(evidences);

            Collections.sort(sortedEvidence, new Comparator<Evidence>() {
                public int compare(Evidence e1, Evidence e2) {
                    if (e1.getId() == null)
                        return 1;
                    if (e2.getId() == null)
                        return -1;
                    return e1.getId() - e2.getId();
                }
            });

            for (Evidence evidence : sortedEvidence) {
                Set<String> pmids = new HashSet<>();
                Set<ArticleAbstract> abstracts = new HashSet<>();
                for (Article article : evidence.getArticles()) {
                    if(article.getPmid() != null) {
                        pmids.add(article.getPmid());
                    }
                    if(article.getAbstractContent() != null) {
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

    private static Boolean isVUS(Set<Evidence> evidenceList) {
        for (Evidence evidence : evidenceList) {
            if (evidence.getEvidenceType().equals(EvidenceType.VUS)) {
                return true;
            }
        }
        return false;
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
