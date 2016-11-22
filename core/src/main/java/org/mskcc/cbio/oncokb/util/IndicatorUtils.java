package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
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
        Set<Alteration> relevantAlterations = new HashSet<>();

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

                        Set<Alteration> tmpRelevantAlts = AlterationUtils.getRelevantAlterations(alt);
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

            Set<Alteration> nonVUSRelevantAlts = AlterationUtils.excludeVUS(relevantAlterations);
            Map<String, LevelOfEvidence> highestLevels = new HashMap<>();
            Set<Alteration> alleles = new HashSet<>();
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
                indicatorQuery.setOncogenic(oncogenicity == null ? "" : oncogenicity.getDescription());

                treatmentEvidences = EvidenceUtils.keepHighestLevelForSameTreatments(
                    EvidenceUtils.getRelevantEvidences(query, source, geneStatus,
                        MainUtils.getTreatmentEvidenceTypes(),
                        (levels != null ?
                            new HashSet<LevelOfEvidence>(CollectionUtils.intersection(levels,
                                LevelUtils.getPublicAndOtherIndicationLevels())) : LevelUtils.getPublicAndOtherIndicationLevels())));

                
            } else if (indicatorQuery.getAlleleExist() || indicatorQuery.getVUS()) {
                Oncogenicity oncogenicity = MainUtils.setToAlleleOncogenicity(MainUtils.findHighestOncogenicByEvidences(
                    EvidenceUtils.getEvidence(alleles, Collections.singleton(EvidenceType.ONCOGENIC), null)));
                treatmentEvidences = EvidenceUtils.keepHighestLevelForSameTreatments(
                    EvidenceUtils.convertEvidenceLevel(
                    EvidenceUtils.getEvidence(alleles,
                        MainUtils.getTreatmentEvidenceTypes(),
                        (levels != null ?
                            new HashSet<LevelOfEvidence>(CollectionUtils.intersection(levels,
                                LevelUtils.getPublicAndOtherIndicationLevels())) : LevelUtils.getPublicAndOtherIndicationLevels())), new HashSet<>(oncoTreeTypes)));
                indicatorQuery.setOncogenic(oncogenicity == null ? "" : oncogenicity.getDescription());
            }
            
            if(treatmentEvidences != null) {
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
                    Set<IndicatorQueryTreatment> treatments = getIndicatorQueryTreatments(treatmentEvidences);

                    indicatorQuery.setTreatments(treatments);
                    highestLevels = findHighestLevel(treatments);
                    indicatorQuery.setHighestSensitiveLevel(highestLevels.get("sensitive") == null ? "" : highestLevels.get("sensitive").name());
                    indicatorQuery.setHighestResistanceLevel(highestLevels.get("resistant") == null ? "" : highestLevels.get("resistant").name());
                }
            }
        } else {
            indicatorQuery.setGeneExist(false);
        }
        indicatorQuery.setDataVersion(MainUtils.getDataVersion());
        indicatorQuery.setLastUpdate(MainUtils.getDataVersionDate());

        return indicatorQuery;
    }

    private static Set<IndicatorQueryTreatment> getIndicatorQueryTreatments(Set<Evidence> evidences) {
        Set<IndicatorQueryTreatment> treatments = new HashSet<>();
        if (evidences != null) {
            for (Evidence evidence : evidences) {
                Set<String> pmids = new HashSet<>();
                for (Article article : evidence.getArticles()) {
                    pmids.add(article.getPmid());
                }
                for (Treatment treatment : evidence.getTreatments()) {
                    IndicatorQueryTreatment indicatorQueryTreatment = new IndicatorQueryTreatment();
                    indicatorQueryTreatment.setDrugs(treatment.getDrugs());
                    indicatorQueryTreatment.setApprovedIndications(treatment.getApprovedIndications());
                    indicatorQueryTreatment.setLevel(evidence.getLevelOfEvidence());
                    indicatorQueryTreatment.setPmids(pmids);
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
