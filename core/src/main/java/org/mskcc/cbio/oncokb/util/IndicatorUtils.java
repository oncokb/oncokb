package org.mskcc.cbio.oncokb.util;

import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.Citations;
import org.mskcc.cbio.oncokb.apiModels.Implication;
import org.mskcc.cbio.oncokb.apiModels.MutationEffectResp;
import org.mskcc.cbio.oncokb.apiModels.NCITDrug;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static org.mskcc.cbio.oncokb.util.LevelUtils.getTherapeuticLevelsWithPriorityLIstIterator;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class IndicatorUtils {
    public static IndicatorQueryResp processQuery(Query query,
                                                  Set<LevelOfEvidence> levels, Boolean highestLevelOnly,
                                                  Set<EvidenceType> evidenceTypes) {
        highestLevelOnly = highestLevelOnly == null ? false : highestLevelOnly;

        levels = levels == null ? LevelUtils.getPublicLevels() :
            new HashSet<>(CollectionUtils.intersection(levels, LevelUtils.getPublicLevels()));

        Set<EvidenceType> selectedTreatmentEvidence = new HashSet<>();
        if (evidenceTypes == null || evidenceTypes.isEmpty()) {
            evidenceTypes = new HashSet<>(EvidenceTypeUtils.getAllEvidenceTypes());
            selectedTreatmentEvidence = EvidenceTypeUtils.getTreatmentEvidenceTypes();
        } else {
            selectedTreatmentEvidence = Sets.intersection(evidenceTypes, EvidenceTypeUtils.getTreatmentEvidenceTypes());
        }

        boolean hasTreatmentEvidence = !selectedTreatmentEvidence.isEmpty();
        boolean hasDiagnosticImplicationEvidence = evidenceTypes.contains(EvidenceType.DIAGNOSTIC_IMPLICATION);
        boolean hasPrognosticImplicationEvidence = evidenceTypes.contains(EvidenceType.PROGNOSTIC_IMPLICATION);
        boolean hasOncogenicEvidence = evidenceTypes.contains(EvidenceType.ONCOGENIC);
        boolean hasMutationEffectEvidence = evidenceTypes.contains(EvidenceType.MUTATION_EFFECT);

        IndicatorQueryResp indicatorQuery = new IndicatorQueryResp();
        indicatorQuery.setQuery(query);

        Gene gene = null;
        List<Alteration> relevantAlterations = new ArrayList<>();

        Set<Evidence> allQueryRelatedEvidences = new HashSet<>();

        // Queried alteration
        Alteration alteration;

        if (query == null) {
            return indicatorQuery;
        }

        query.enrich();

        // Temporary forward previous production annotation
        if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(query.getAlteration()) && query.getAlteration().equals("EGFRvIII")) {
            query.setAlteration("vIII");
        }


        // For intragenic annotation, convert it to structural variant and mark as Deletion
        if (StringUtils.containsIgnoreCase(query.getAlteration(), "intragenic")) {
            query.setAlterationType(AlterationType.STRUCTURAL_VARIANT.name());
            query.setSvType(StructuralVariantType.DELETION);
            query.setAlteration("");
        }


        Boolean isStructuralVariantEvent = false;
        // Deal with fusion without primary gene, and this is only for legacy fusion event
        // The latest fusion event has been integrated with alteration type. Please see next if-else condition
        // for more info.
        // TODO: support entrezGeneId fusion
        AlterationType alterationType = AlterationType.getByName(query.getAlterationType());
        Map<String, Object> fusionGeneAltsMap = new HashMap<>();
        if (query.getHugoSymbol() != null
            && alterationType != null &&
            alterationType.equals(AlterationType.FUSION)) {
            isStructuralVariantEvent = true;
            fusionGeneAltsMap = findFusionGeneAndRelevantAlts(query);

            // Dup: For single gene deletion event. We should map to Deletion instead of Truncating Mutation when Deletion has been curated
            if (query.getSvType() != null && query.getSvType().equals(StructuralVariantType.DELETION)) {
                Set<String> queryFusionGenes = (Set<String>) fusionGeneAltsMap.get("queryFusionGenes");
                if (queryFusionGenes.size() == 1) {
                    Gene queryFusionGene = GeneUtils.getGeneByHugoSymbol(queryFusionGenes.iterator().next());
                    if (queryFusionGene != null) {
                        Alteration deletion = AlterationUtils.findAlteration(queryFusionGene, "Deletion");
                        if (deletion != null) {
                            query.setAlteration("deletion");
                            query.setConsequence("feature_truncation");
                            fusionGeneAltsMap = findFusionGeneAndRelevantAlts(query);
                        }
                    }
                }
            }

            gene = (Gene) fusionGeneAltsMap.get("pickedGene");
            relevantAlterations = (List<Alteration>) fusionGeneAltsMap.get("relevantAlts");
            Set<Gene> allGenes = (LinkedHashSet<Gene>) fusionGeneAltsMap.get("allGenes");
        } else if (alterationType != null && alterationType.equals(AlterationType.STRUCTURAL_VARIANT)) {
            isStructuralVariantEvent = true;
            VariantConsequence variantConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm(query.getConsequence());
            Boolean isFunctionalFusion = variantConsequence != null && variantConsequence.getTerm().equals("fusion");

            if (isFunctionalFusion || !com.mysql.jdbc.StringUtils.isNullOrEmpty(query.getAlteration())) {
                fusionGeneAltsMap = findFusionGeneAndRelevantAlts(query);
                gene = (Gene) fusionGeneAltsMap.get("pickedGene");
                relevantAlterations = (List<Alteration>) fusionGeneAltsMap.get("relevantAlts");
            } else {
                query.setAlteration("truncating mutation");
                query.setConsequence("feature_truncation");

                fusionGeneAltsMap = findFusionGeneAndRelevantAlts(query);

                // For single gene deletion event. We should map to Deletion instead of Truncating Mutation when Deletion has been curated
                if (query.getSvType() != null && query.getSvType().equals(StructuralVariantType.DELETION)) {
                    Set<String> queryFusionGenes = (Set<String>) fusionGeneAltsMap.get("queryFusionGenes");
                    if (queryFusionGenes.size() == 1) {
                        Gene queryFusionGene = GeneUtils.getGeneByHugoSymbol(queryFusionGenes.iterator().next());
                        if (queryFusionGene != null) {
                            Alteration deletion = AlterationUtils.findAlteration(queryFusionGene, "Deletion");
                            if (deletion != null) {
                                query.setAlteration("deletion");
                                fusionGeneAltsMap = findFusionGeneAndRelevantAlts(query);
                            }
                        }
                    }
                }

                gene = (Gene) fusionGeneAltsMap.get("pickedGene");
                fusionGeneAltsMap = new HashMap<>();
                // As long as this is a structural variant event, we need to attach the Truncating Mutation
                Alteration truncatingMutations = AlterationUtils.getTruncatingMutations(gene);
                if (truncatingMutations != null && !relevantAlterations.contains(truncatingMutations)) {
                    relevantAlterations.add(truncatingMutations);
                    List<Alteration> truncMutRelevants = AlterationUtils.getRelevantAlterations(truncatingMutations);
                    for (Alteration alt : truncMutRelevants) {
                        if (!relevantAlterations.contains(alt)) {
                            relevantAlterations.add(alt);
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

        // For fusions
        if (fusionGeneAltsMap.containsKey("hasRelevantAltsGenes")) {
            // If there are more than two genes have matches we need to compare the highest level, then oncogenicity
            TreeSet<IndicatorQueryResp> result = new TreeSet<>(new IndicatorQueryRespComp());
            for (Gene tmpGene : (List<Gene>) fusionGeneAltsMap.get("hasRelevantAltsGenes")) {
                Query tmpQuery = new Query(query.getId(), query.getType(), tmpGene.getEntrezGeneId(),
                    tmpGene.getHugoSymbol(), query.getAlteration(), null, query.getSvType(),
                    query.getTumorType(), query.getConsequence(), query.getProteinStart(),
                    query.getProteinEnd(), query.getHgvs());
                result.add(IndicatorUtils.processQuery(tmpQuery, levels, highestLevelOnly, evidenceTypes));
            }
            return result.iterator().next();
        }

        if (gene != null) {
            query.setHugoSymbol(gene.getHugoSymbol());
            query.setEntrezGeneId(gene.getEntrezGeneId());

            // Gene exist should only be set to true if entrezGeneId is bigger than 0
            indicatorQuery.setGeneExist(gene.getEntrezGeneId() > 0);

            // Gene summary

            if (evidenceTypes.contains(EvidenceType.GENE_SUMMARY)) {
                indicatorQuery.setGeneSummary(SummaryUtils.geneSummary(gene));
                allQueryRelatedEvidences.addAll(EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.GENE_SUMMARY)));
            }

            alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getAlteration(),
                null, query.getConsequence(), query.getProteinStart(), query.getProteinEnd());
            AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());

            List<Alteration> nonVUSRelevantAlts = AlterationUtils.excludeVUS(relevantAlterations);
            Map<String, LevelOfEvidence> highestLevels = new HashMap<>();
            List<TumorType> relevantUpwardTumorTypes = new ArrayList<>();
            List<TumorType> relevantDownwardTumorTypes = new ArrayList<>();

            Alteration matchedAlt = null;

            LinkedHashSet<Alteration> matchedAlterations = AlterationUtils.findMatchedAlterations(alteration);
            if (matchedAlterations.size() > 1) {
                matchedAlt = pickMatchedAlteration(new ArrayList<>(matchedAlterations), query, levels, highestLevelOnly, evidenceTypes);
            } else if (matchedAlterations.size() == 1) {
                matchedAlt = matchedAlterations.iterator().next();
            }

            if (matchedAlt == null && isStructuralVariantEvent) {
                matchedAlt = AlterationUtils.getRevertFusions(alteration);
            }

            indicatorQuery.setVariantExist(matchedAlt != null);

            if(matchedAlt == null) {
                matchedAlt = alteration;
            }

            List<Alteration> alleles = AlterationUtils.getAlleleAlterations(matchedAlt);

            // This is for tumor type level info. We do not want to map the alternative alleles on tumor type level
            List<Alteration> relevantAlterationsWithoutAlternativeAlleles = new ArrayList<>(relevantAlterations);
            AlterationUtils.removeAlternativeAllele(matchedAlt, relevantAlterationsWithoutAlternativeAlleles);

            // Whether alteration is hotpot from Matt's list
            if (query.getProteinEnd() == null || query.getProteinStart() == null) {
                indicatorQuery.setHotspot(HotspotUtils.isHotspot(matchedAlt));
            } else {
                indicatorQuery.setHotspot(HotspotUtils.isHotspot(matchedAlt));
            }

            if (query.getTumorType() != null) {
                relevantUpwardTumorTypes = TumorTypeUtils.getMappedOncoTreeTypesBySource(query.getTumorType());
            }

            relevantDownwardTumorTypes = TumorTypeUtils.findTumorTypes(query.getTumorType(), RelevantTumorTypeDirection.DOWNWARD);

            indicatorQuery.setVUS(isVUS(matchedAlt));

            if (indicatorQuery.getVUS()) {
                List<Evidence> vusEvidences = EvidenceUtils.getEvidence(Collections.singletonList(matchedAlt), Collections.singleton(EvidenceType.VUS), null);
                if (vusEvidences != null) {
                    allQueryRelatedEvidences.addAll(vusEvidences);
                }
            }

            if (alleles == null || alleles.size() == 0) {
                indicatorQuery.setAlleleExist(false);
            } else {
                indicatorQuery.setAlleleExist(true);
            }

            Set<Evidence> treatmentEvidences = new HashSet<>();

            if (nonVUSRelevantAlts.size() > 0) {
                if (hasOncogenicEvidence) {
                    IndicatorQueryOncogenicity indicatorQueryOncogenicity = getOncogenicity(matchedAlt, alleles, nonVUSRelevantAlts);

                    if (indicatorQueryOncogenicity.getOncogenicityEvidence() != null) {
                        allQueryRelatedEvidences.add(indicatorQueryOncogenicity.getOncogenicityEvidence());
                    }

                    // Only set oncogenicity if no previous data assigned.
                    if (indicatorQuery.getOncogenic() == null && indicatorQueryOncogenicity.getOncogenicity() != null) {
                        indicatorQuery.setOncogenic(indicatorQueryOncogenicity.getOncogenicity().getOncogenic());
                    }
                }

                if (hasMutationEffectEvidence) {
                    IndicatorQueryMutationEffect indicatorQueryMutationEffect = getMutationEffect(matchedAlt, alleles, nonVUSRelevantAlts);

                    if (indicatorQueryMutationEffect.getMutationEffectEvidence() != null) {
                        allQueryRelatedEvidences.add(indicatorQueryMutationEffect.getMutationEffectEvidence());
                    }

                    // Only set oncogenicity if no previous data assigned.
                    if (indicatorQuery.getMutationEffect() == null && indicatorQueryMutationEffect.getMutationEffect() != null) {
                        MutationEffectResp mutationEffectResp = new MutationEffectResp();
                        mutationEffectResp.setKnownEffect(indicatorQueryMutationEffect.getMutationEffect().getMutationEffect());
                        if (indicatorQueryMutationEffect.getMutationEffectEvidence() != null) {
                            mutationEffectResp.setDescription(indicatorQueryMutationEffect.getMutationEffectEvidence().getDescription());
                            mutationEffectResp.setCitations(MainUtils.getCitationsByEvidence(indicatorQueryMutationEffect.getMutationEffectEvidence()));
                        }
                        indicatorQuery.setMutationEffect(mutationEffectResp);
                    }
                }

                if (hasTreatmentEvidence) {
                    treatmentEvidences = EvidenceUtils.keepHighestLevelForSameTreatments(
                        EvidenceUtils.getRelevantEvidences(query, matchedAlt,
                            selectedTreatmentEvidence, levels, relevantAlterationsWithoutAlternativeAlleles, alleles), matchedAlt);
                }

                if (hasDiagnosticImplicationEvidence) {
                    indicatorQuery.setDiagnosticImplications(getImplications(matchedAlt, alleles, relevantAlterationsWithoutAlternativeAlleles, EvidenceType.DIAGNOSTIC_IMPLICATION, new HashSet<>(relevantDownwardTumorTypes)));
                    if (indicatorQuery.getDiagnosticImplications().size() > 0) {
                        indicatorQuery.setHighestDiagnosticImplicationLevel(LevelUtils.getHighestDiagnosticImplicationLevel(indicatorQuery.getDiagnosticImplications().stream().map(implication -> implication.getLevelOfEvidence()).collect(Collectors.toSet())));
                    }
                }

                if (hasPrognosticImplicationEvidence) {
                    indicatorQuery.setPrognosticImplications(getImplications(matchedAlt, alleles, relevantAlterationsWithoutAlternativeAlleles, EvidenceType.PROGNOSTIC_IMPLICATION, new HashSet<>(relevantUpwardTumorTypes)));
                    if (indicatorQuery.getPrognosticImplications().size() > 0) {
                        indicatorQuery.setHighestPrognosticImplicationLevel(LevelUtils.getHighestPrognosticImplicationLevel(indicatorQuery.getPrognosticImplications().stream().map(implication -> implication.getLevelOfEvidence()).collect(Collectors.toSet())));
                    }
                }
            }

            // Set hotspot oncogenicity to Predicted Oncogenic
            if (indicatorQuery.getHotspot() && !MainUtils.isValidHotspotOncogenicity(Oncogenicity.getByEffect(indicatorQuery.getOncogenic()))) {
                indicatorQuery.setOncogenic(Oncogenicity.PREDICTED.getOncogenic());

                // Check whether the gene has Oncogenic Mutations annotated
                Alteration oncogenicMutation = AlterationUtils.findAlteration(gene, "Oncogenic Mutations");
                if (oncogenicMutation != null) {
                    relevantAlterations.add(oncogenicMutation);
                    if (hasTreatmentEvidence) {
                        treatmentEvidences.addAll(EvidenceUtils.keepHighestLevelForSameTreatments(
                            EvidenceUtils.convertEvidenceLevel(
                                EvidenceUtils.getEvidence(Collections.singletonList(oncogenicMutation),
                                    selectedTreatmentEvidence, levels), new HashSet<>(relevantUpwardTumorTypes)), matchedAlt));
                    }
                }
            }

            if (hasTreatmentEvidence && treatmentEvidences != null && !treatmentEvidences.isEmpty()) {
                if (highestLevelOnly) {
                    Set<Evidence> filteredEvis = new HashSet<>();
                    // Get highest sensitive evidences
                    Set<Evidence> sensitiveEvidences = EvidenceUtils.getSensitiveEvidences(treatmentEvidences);
                    filteredEvis.addAll(EvidenceUtils.getOnlySignificantLevelsEvidences(sensitiveEvidences));

                    // Get highest resistance evidences
                    Set<Evidence> resistanceEvidences = EvidenceUtils.getResistanceEvidences(treatmentEvidences);
                    filteredEvis.addAll(EvidenceUtils.getOnlyHighestLevelEvidences(resistanceEvidences, matchedAlt));

                    treatmentEvidences = filteredEvis;
                }
                if (!treatmentEvidences.isEmpty()) {
                    List<IndicatorQueryTreatment> treatments = getIndicatorQueryTreatments(treatmentEvidences);

                    // Make sure the treatment in KIT is always sorted.
                    if (gene.getHugoSymbol().equals("KIT")) {
                        CustomizeComparator.sortKitTreatment(treatments);
                    }
                    indicatorQuery.setTreatments(treatments);
                    highestLevels = findHighestLevel(new HashSet<>(treatments));
                    indicatorQuery.setHighestSensitiveLevel(highestLevels.get("sensitive"));
                    indicatorQuery.setHighestResistanceLevel(highestLevels.get("resistant"));
                    indicatorQuery.setOtherSignificantSensitiveLevels(getOtherSignificantLevels(indicatorQuery.getHighestSensitiveLevel(), "sensitive", treatmentEvidences));
                    indicatorQuery.setOtherSignificantResistanceLevels(getOtherSignificantLevels(indicatorQuery.getHighestResistanceLevel(), "resistance", treatmentEvidences));

                    allQueryRelatedEvidences.addAll(treatmentEvidences);
                }
            }

            // Tumor type summary
            if (evidenceTypes.contains(EvidenceType.TUMOR_TYPE_SUMMARY) && query.getTumorType() != null) {
                Map<String, Object> tumorTypeSummary = SummaryUtils.tumorTypeSummary(EvidenceType.TUMOR_TYPE_SUMMARY, gene, query, matchedAlt,
                    new ArrayList<>(relevantAlterationsWithoutAlternativeAlleles),
                    relevantUpwardTumorTypes);
                if (tumorTypeSummary != null) {
                    indicatorQuery.setTumorTypeSummary((String) tumorTypeSummary.get("summary"));
                    Date lateEdit = tumorTypeSummary.get("lastEdit") == null ? null : (Date) tumorTypeSummary.get("lastEdit");
                    if (lateEdit != null) {
                        Evidence lastEditTTSummary = new Evidence();
                        lastEditTTSummary.setLastEdit(lateEdit);
                        allQueryRelatedEvidences.add(lastEditTTSummary);
                    }
                }
            }

            // Mutation summary
            if (evidenceTypes.contains(EvidenceType.MUTATION_SUMMARY)) {
                indicatorQuery.setVariantSummary(SummaryUtils.variantSummary(gene, matchedAlt,
                    new ArrayList<>(relevantAlterations), query));
            }

            // Diagnostic summary
            if (evidenceTypes.contains(EvidenceType.DIAGNOSTIC_SUMMARY)) {
                Map<String, Object> diagnosticSummary = SummaryUtils.tumorTypeSummary(EvidenceType.DIAGNOSTIC_SUMMARY, gene, query, matchedAlt,
                    new ArrayList<>(relevantAlterationsWithoutAlternativeAlleles),
                    relevantUpwardTumorTypes);
                if (diagnosticSummary != null) {
                    indicatorQuery.setDiagnosticSummary((String) diagnosticSummary.get("summary"));
                    Date lateEdit = diagnosticSummary.get("lastEdit") == null ? null : (Date) diagnosticSummary.get("lastEdit");
                    if (lateEdit != null) {
                        Evidence lastEditTTSummary = new Evidence();
                        lastEditTTSummary.setLastEdit(lateEdit);
                        allQueryRelatedEvidences.add(lastEditTTSummary);
                    }
                }
            }

            // Prognostic summary
            if (evidenceTypes.contains(EvidenceType.PROGNOSTIC_SUMMARY)) {
                Map<String, Object> prognosticSummary = SummaryUtils.tumorTypeSummary(EvidenceType.PROGNOSTIC_SUMMARY, gene, query, matchedAlt,
                    new ArrayList<>(relevantAlterationsWithoutAlternativeAlleles),
                    relevantUpwardTumorTypes);
                if (prognosticSummary != null) {
                    indicatorQuery.setPrognosticSummary((String) prognosticSummary.get("summary"));
                    Date lateEdit = prognosticSummary.get("lastEdit") == null ? null : (Date) prognosticSummary.get("lastEdit");
                    if (lateEdit != null) {
                        Evidence lastEditTTSummary = new Evidence();
                        lastEditTTSummary.setLastEdit(lateEdit);
                        allQueryRelatedEvidences.add(lastEditTTSummary);
                    }
                }
            }

            // This is special case for KRAS wildtype. May need to come up with a better plan for this.
            if (gene != null && (gene.getHugoSymbol().equals("KRAS") || gene.getHugoSymbol().equals("NRAS"))
                && query.getAlteration() != null
                && StringUtils.containsIgnoreCase(query.getAlteration(), "wildtype")) {
                if (relevantUpwardTumorTypes.contains(TumorTypeUtils.getOncoTreeCancerType("Colorectal Cancer"))) {
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

        if (indicatorQuery.getMutationEffect() == null) {
            indicatorQuery.setMutationEffect(getDefaultMutationEffectResponse());
        }

        indicatorQuery.setDataVersion(MainUtils.getDataVersion());

        Date lastUpdate = getLatestDateFromEvidences(allQueryRelatedEvidences);
        indicatorQuery.setLastUpdate(lastUpdate == null ? MainUtils.getDataVersionDate() :
            new SimpleDateFormat("MM/dd/yyy").format(lastUpdate));

        // Give default oncogenicity if no data has been assigned.
        if (indicatorQuery.getOncogenic() == null) {
            indicatorQuery.setOncogenic("");
        }
        return indicatorQuery;
    }

    // This should used by only for delins range missense mutation
    private static Alteration pickMatchedAlteration(List<Alteration> alterations, Query originalQuery, Set<LevelOfEvidence> levels, Boolean highestLevelOnly, Set<EvidenceType> evidenceTypes) {
        if (alterations == null || alterations.size() == 0) {
            return null;
        }
        Map<Oncogenicity, List<Alteration>> groupedOncogenicities = new HashedMap();
        Map<LevelOfEvidence, List<Alteration>> groupedLevel = new HashedMap();
        for (Alteration alteration : alterations) {
            Query tmpQuery = new Query(null, null, alteration.getGene().getEntrezGeneId(),
                alteration.getGene().getHugoSymbol(), alteration.getAlteration(), null, null,
                originalQuery.getTumorType(), alteration.getConsequence().getTerm(), alteration.getProteinStart(),
                alteration.getProteinEnd(), null);

            // Add oncogenicity
            IndicatorQueryOncogenicity indicatorQueryOncogenicity = getOncogenicity(alteration, new ArrayList<>(), new ArrayList<>());
            Oncogenicity oncogenicity = indicatorQueryOncogenicity.oncogenicity;
            if (!groupedOncogenicities.containsKey(oncogenicity)) {
                groupedOncogenicities.put(oncogenicity, new ArrayList<>());
            }
            groupedOncogenicities.get(oncogenicity).add(alteration);

        }
        Oncogenicity highestOncogenicity = MainUtils.findHighestOncogenicity(groupedOncogenicities.keySet());
        if (highestOncogenicity == null) {
            return alterations.iterator().next();
        }
        // when the oncogenicity is the same, then the therapeutic info will be the same as well.
        return groupedOncogenicities.get(highestOncogenicity).get(0);
    }

    private static Implication getImplicationFromEvidence(Evidence evidence) {
        if (evidence == null) {
            return null;
        }
        Implication implication = new Implication();
        implication.setLevelOfEvidence(evidence.getLevelOfEvidence());
        implication.setAlterations(evidence.getAlterations().stream().map(alteration -> alteration.getName() == null ? alteration.getAlteration() : alteration.getAlteration()).collect(Collectors.toSet()));
        implication.setTumorType(evidence.getOncoTreeType());
        implication.setDescription(evidence.getDescription());
        return implication;
    }

    private static MutationEffectResp getDefaultMutationEffectResponse() {
        MutationEffectResp mutationEffectResp = new MutationEffectResp();
        mutationEffectResp.setKnownEffect(MutationEffect.UNKNOWN.getMutationEffect());
        return mutationEffectResp;
    }

    private static List<Implication> getImplicationFromEvidence(List<Evidence> evidences) {
        List<Implication> implications = new ArrayList<>();
        if (evidences == null) {
            return implications;
        }
        for (Evidence evidence : evidences) {
            implications.add(getImplicationFromEvidence(evidence));
        }
        return implications;
    }

    private static List<Implication> getImplications(Alteration matchedAlt, List<Alteration> alternativeAlleles, List<Alteration> relevantAlterations, EvidenceType evidenceType, Set<TumorType> tumorTypes) {
        List<Implication> implications = new ArrayList<>();

        // Find alteration specific evidence
        List<Evidence> selfAltEvis = EvidenceUtils.getEvidence(Collections.singletonList(matchedAlt), Collections.singleton(evidenceType), tumorTypes, null);
        if (selfAltEvis != null && selfAltEvis.size() > 0) {
            implications.addAll(getImplicationFromEvidence(selfAltEvis));
        }

        // Find Oncogenicity from alternative alleles
        if (alternativeAlleles.size() > 0) {
            for (Alteration allele : alternativeAlleles) {
                List<Evidence> allelesEvis = EvidenceUtils.getEvidence(Collections.singletonList(allele), Collections.singleton(evidenceType), tumorTypes, null);
                if (allelesEvis != null && allelesEvis.size() > 0) {
                    implications.addAll(getImplicationFromEvidence(allelesEvis));
                }
            }
        }

        // If there is no oncogenic info available for this variant, find oncogenicity from relevant variants
        List<Alteration> listToBeRemoved = new ArrayList<>(alternativeAlleles);
        listToBeRemoved.add(matchedAlt);


        for (Alteration alt : AlterationUtils.removeAlterationsFromList(relevantAlterations, listToBeRemoved)) {
            List<Evidence> altEvis = EvidenceUtils.getEvidence(Collections.singletonList(alt), Collections.singleton(evidenceType), tumorTypes, null);
            if (altEvis != null && altEvis.size() > 0) {
                implications.addAll(getImplicationFromEvidence(altEvis));
            }
        }
        return filterImplication(implications);
    }


    public static List<Implication> filterImplication(List<Implication> implications) {
        return implications.stream().filter(implication -> implication.getLevelOfEvidence() != null)
            .map(UniqueImplication::new)
            .distinct()
            .map(UniqueImplication::unwrap)
            .collect(Collectors.toList());
    }


    private static Set<Evidence> removeNoneTumorTypeRelatedEvidence(List<Evidence> evidences, Set<TumorType> tumorTypes) {
        return new HashSet<>();
    }

    public static IndicatorQueryOncogenicity getOncogenicity(Alteration alteration, List<Alteration> alternativeAllele, List<Alteration> relevantAlterations) {
        Oncogenicity oncogenicity = null;
        Evidence oncogenicityEvidence = null;


        // Find alteration specific oncogenicity
        List<Evidence> selfAltOncogenicEvis = EvidenceUtils.getEvidence(Collections.singletonList(alteration),
            Collections.singleton(EvidenceType.ONCOGENIC), null);
        if (selfAltOncogenicEvis != null) {
            oncogenicityEvidence = MainUtils.findHighestOncogenicEvidenceByEvidences(new HashSet<>(selfAltOncogenicEvis));
            if (oncogenicityEvidence != null) {
                oncogenicity = Oncogenicity.getByEffect(oncogenicityEvidence.getKnownEffect());
            }
        }

        if (oncogenicity == null || oncogenicity.equals(Oncogenicity.UNKNOWN)) {
            // Find Oncogenicity from alternative alleles
            if (alternativeAllele.size() > 0) {
                oncogenicityEvidence = MainUtils.findHighestOncogenicEvidenceByEvidences(new HashSet<>(EvidenceUtils.getEvidence(new ArrayList<>(alternativeAllele), Collections.singleton(EvidenceType.ONCOGENIC), null)));
                if (oncogenicityEvidence != null) {
                    Oncogenicity tmpOncogenicity = MainUtils.setToAlleleOncogenicity(Oncogenicity.getByEffect(oncogenicityEvidence.getKnownEffect()));
                    if (tmpOncogenicity != null) {
                        oncogenicity = tmpOncogenicity;
                    }
                }
            }

            // If there is no oncogenic info available for this variant, find oncogenicity from relevant variants
            // This also includes inconclusive check cause if the best oncogenicity from alternative allele, we should continue find better one in other relevant alterations
            if (oncogenicity == null || oncogenicity.equals(Oncogenicity.UNKNOWN) || oncogenicity.equals(Oncogenicity.INCONCLUSIVE)) {
                List<Alteration> listToBeRemoved = new ArrayList<>(alternativeAllele);
                listToBeRemoved.add(alteration);

                oncogenicityEvidence = MainUtils.findHighestOncogenicEvidenceByEvidences(
                    new HashSet<>(EvidenceUtils.getEvidence(new ArrayList<>(AlterationUtils.removeAlterationsFromList(relevantAlterations, listToBeRemoved)), Collections.singleton(EvidenceType.ONCOGENIC), null))
                );
                if (oncogenicityEvidence != null) {
                    Oncogenicity tmpOncogenicity = Oncogenicity.getByEffect(oncogenicityEvidence.getKnownEffect());
                    if (tmpOncogenicity != null) {
                        oncogenicity = tmpOncogenicity;
                    }
                }
            }
        }

        if (oncogenicity == null) {
            oncogenicity = Oncogenicity.UNKNOWN;
        }
        return new IndicatorQueryOncogenicity(oncogenicity, oncogenicityEvidence);
    }

    private static IndicatorQueryMutationEffect getMutationEffect(Alteration alteration, List<Alteration> alternativeAllele, List<Alteration> relevantAlterations) {
        IndicatorQueryMutationEffect indicatorQueryMutationEffect = new IndicatorQueryMutationEffect();
        // Find alteration specific mutation effect
        List<Evidence> selfAltMEEvis = EvidenceUtils.getEvidence(Collections.singletonList(alteration),
            Collections.singleton(EvidenceType.MUTATION_EFFECT), null);
        if (selfAltMEEvis != null) {
            indicatorQueryMutationEffect = MainUtils.findHighestMutationEffectByEvidence(new HashSet<>(selfAltMEEvis));
        }

        if (indicatorQueryMutationEffect.getMutationEffect() == null || indicatorQueryMutationEffect.getMutationEffect().equals(MutationEffect.UNKNOWN)) {
            // Find mutation effect from alternative alleles
            if (alternativeAllele.size() > 0) {
                indicatorQueryMutationEffect =
                    MainUtils.setToAlternativeAlleleMutationEffect(
                        MainUtils.findHighestMutationEffectByEvidence(
                            new HashSet<>(
                                EvidenceUtils.getEvidence(
                                    new ArrayList<>(alternativeAllele)
                                    , Collections.singleton(EvidenceType.MUTATION_EFFECT)
                                    , null
                                )
                            )
                        )
                    );
            }

            // If there is no mutation effect info available for this variant, find mutation effect from relevant variants
            // This also includes inconclusive check cause if the best mutation effect from alternative allele, we should continue find better one in other relevant alterations
            if (indicatorQueryMutationEffect.getMutationEffect() == null || indicatorQueryMutationEffect.getMutationEffect().equals(MutationEffect.INCONCLUSIVE) || indicatorQueryMutationEffect.getMutationEffect().equals(MutationEffect.UNKNOWN)) {
                List<Alteration> listToBeRemoved = new ArrayList<>(alternativeAllele);
                listToBeRemoved.add(alteration);

                indicatorQueryMutationEffect = MainUtils.findHighestMutationEffectByEvidence(
                    new HashSet<>(EvidenceUtils.getEvidence(AlterationUtils.removeAlterationsFromList(relevantAlterations, listToBeRemoved), Collections.singleton(EvidenceType.MUTATION_EFFECT), null))
                );
            }

        }
        if (indicatorQueryMutationEffect.getMutationEffect() == null) {
            indicatorQueryMutationEffect.setMutationEffect(MutationEffect.UNKNOWN);
        }
        return indicatorQueryMutationEffect;
    }

    private static Date getLatestDateFromEvidences(Set<Evidence> evidences) {
        Date date = null;
        if (evidences != null) {
            for (Evidence evidence : evidences) {
                if (evidence.getLastEdit() != null) {
                    if (date == null) {
                        date = evidence.getLastEdit();
                    } else if (date.before(evidence.getLastEdit())) {
                        date = evidence.getLastEdit();
                    }
                }
            }
        }
        return date;
    }

    private static List<LevelOfEvidence> getOtherSignificantLevels(LevelOfEvidence highestLevel, String type, Set<Evidence> evidences) {
        List<LevelOfEvidence> otherSignificantLevels = new ArrayList<>();
        if (type != null && highestLevel != null && evidences != null) {
            if (type.equals("sensitive")) {
//                if (highestLevel.equals(LevelOfEvidence.LEVEL_2B)) {
//                    Map<LevelOfEvidence, Set<Evidence>> levels = EvidenceUtils.separateEvidencesByLevel(evidences);
//                    if (levels.containsKey(LevelOfEvidence.LEVEL_3A)) {
//                        otherSignificantLevels.add(LevelOfEvidence.LEVEL_3A);
//                    }
//                }
            } else if (type.equals("resistance")) {

            }
        }
        return otherSignificantLevels;
    }

    private static List<IndicatorQueryTreatment> getIndicatorQueryTreatments(Set<Evidence> evidences) {
        List<IndicatorQueryTreatment> treatments = new ArrayList<>();
        if (evidences != null) {
            Map<LevelOfEvidence, Set<Evidence>> evidenceSetMap = EvidenceUtils.separateEvidencesByLevel(evidences);

            ListIterator<LevelOfEvidence> li = getTherapeuticLevelsWithPriorityLIstIterator();
            while (li.hasPrevious()) {
                LevelOfEvidence level = li.previous();
                if (evidenceSetMap.containsKey(level)) {
                    Set<Treatment> sameLevelTreatments = new HashSet<>();
                    Map<Treatment, Set<String>> pmidsMap = new HashMap<>();
                    Map<Treatment, Set<ArticleAbstract>> abstractsMap = new HashMap<>();
                    Map<Treatment, List<String>> alterationsMap = new HashMap<>();
                    Map<Treatment, TumorType> tumorTypeMap = new HashMap<>();
                    Map<Treatment, String> descriptionMap = new HashMap<>();

                    for (Evidence evidence : evidenceSetMap.get(level)) {
                        Citations citations = MainUtils.getCitationsByEvidence(evidence);
                        for (Treatment treatment : evidence.getTreatments()) {
                            if (!pmidsMap.containsKey(treatment)) {
                                pmidsMap.put(treatment, new HashSet<String>());
                            }
                            if (!abstractsMap.containsKey(treatment)) {
                                abstractsMap.put(treatment, new HashSet<ArticleAbstract>());
                            }
                            if (!alterationsMap.containsKey(treatment)) {
                                alterationsMap.put(treatment, new ArrayList<>());
                            }
                            pmidsMap.put(treatment, citations.getPmids());
                            abstractsMap.put(treatment, citations.getAbstracts());
                            alterationsMap.put(treatment, evidence.getAlterations().stream().map(alteration -> alteration.getName()).collect(Collectors.toList()));
                            tumorTypeMap.put(treatment, evidence.getOncoTreeType());
                            descriptionMap.put(treatment, evidence.getDescription());
                        }
                        sameLevelTreatments.addAll(evidence.getTreatments());
                    }
                    List<Treatment> list = new ArrayList<>(sameLevelTreatments);
                    TreatmentUtils.sortTreatmentsByPriority(list);
                    for (Treatment treatment : list) {
                        if (!treatmentExist(treatments, level, treatment.getDrugs())) {
                            IndicatorQueryTreatment indicatorQueryTreatment = new IndicatorQueryTreatment();
                            indicatorQueryTreatment.setDrugs(treatment.getDrugs());
                            indicatorQueryTreatment.setApprovedIndications(treatment.getApprovedIndications());
                            indicatorQueryTreatment.setLevel(level);
                            indicatorQueryTreatment.setPmids(pmidsMap.get(treatment));
                            indicatorQueryTreatment.setAbstracts(abstractsMap.get(treatment));
                            indicatorQueryTreatment.setAlterations(alterationsMap.get(treatment));
                            indicatorQueryTreatment.setLevelAssociatedCancerType(tumorTypeMap.get(treatment));
                            indicatorQueryTreatment.setDescription(descriptionMap.get(treatment));
                            treatments.add(indicatorQueryTreatment);
                        }
                    }
                }
            }
        }
        return treatments;
    }

    private static boolean treatmentExist(List<IndicatorQueryTreatment> treatments, LevelOfEvidence newTreatmentLevel,  List<Drug> newTreatment) {
        boolean exists = false;
        // Info level treatment can be included even the drug(s) is the same
        for (IndicatorQueryTreatment treatment : treatments) {
            if (getSortedTreatmentName(treatment.getDrugs()).equals(getSortedTreatmentName(newTreatment)) && !LevelUtils.INFO_LEVELS.contains(newTreatmentLevel)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    private static String getSortedTreatmentName(List<Drug> drugs) {
        List<Drug> drugsCopy = new ArrayList<>(drugs);
        drugsCopy.sort(new Comparator<Drug>() {
            @Override
            public int compare(Drug o1, Drug o2) {
                return o1.getDrugName().compareTo(o2.getDrugName());
            }
        });
        return drugsCopy.stream().map(drug -> drug.getDrugName()).collect(Collectors.joining("+"));
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
                        _index = LevelUtils.getSensitiveLevelIndex(levelOfEvidence);
                        if (_index > levelSIndex) {
                            levelSIndex = _index;
                        }
                    } else if (LevelUtils.isResistanceLevel(levelOfEvidence)) {
                        _index = LevelUtils.getResistanceLevelIndex(levelOfEvidence);
                        if (_index > levelRIndex) {
                            levelRIndex = _index;
                        }
                    }
                }
            }
        }
        levels.put("sensitive", levelSIndex > -1 ? LevelUtils.getSensitiveLevelByIndex(levelSIndex) : null);
        levels.put("resistant", levelRIndex > -1 ? LevelUtils.getResistanceLevelByIndex(levelRIndex) : null);
        return levels;
    }

    public static Map<String, LevelOfEvidence> findHighestLevelByEvidences(Set<Evidence> treatmentEvidences) {
        List<IndicatorQueryTreatment> treatments = getIndicatorQueryTreatments(treatmentEvidences);
        return findHighestLevel(new HashSet<>(treatments));
    }

    private static List<Alteration> findRelevantAlts(Gene gene, String alteration) {
        Set<Alteration> relevantAlts = new LinkedHashSet<>();
        Alteration alt = AlterationUtils.getAlteration(gene.getHugoSymbol(), alteration,
            null, null, null, null);
        AlterationUtils.annotateAlteration(alt, alt.getAlteration());

        relevantAlts.addAll(AlterationUtils.getRelevantAlterations(alt));

        Alteration revertAlt = AlterationUtils.getRevertFusions(alt);
        if (revertAlt != null) {
            relevantAlts.addAll(AlterationUtils.getRelevantAlterations(revertAlt));
        }
        return new ArrayList<>(relevantAlts);
    }

    public static Map<String, Object> findFusionGeneAndRelevantAlts(Query query) {
        List<String> geneStrsList = new ArrayList<>();
        if (query.getEntrezGeneId() != null) {
            Gene gene = GeneUtils.getGeneByEntrezId(query.getEntrezGeneId());
            if (gene != null) {
                geneStrsList.add(gene.getHugoSymbol());
            }
        } else {
            geneStrsList.addAll(Arrays.asList(query.getHugoSymbol().split("-")));
        }
        Set<String> geneStrsSet = new HashSet<>();
        Gene gene = null;
        List<Alteration> fusionPair = new ArrayList<>();
        List<Alteration> relevantAlterations = new ArrayList<>();
        Map<String, Object> map = new HashedMap();

        if (geneStrsList != null) {
            geneStrsSet = new HashSet<>(geneStrsList);
            map.put("queryFusionGenes", geneStrsSet);
        }

        // Deal with two different genes fusion event.
        if (geneStrsSet.size() >= 2) {
            Set<Gene> tmpGenes = new LinkedHashSet<>();
            for (String geneStr : geneStrsSet) {
                Gene tmpGene = GeneUtils.getGeneByHugoSymbol(geneStr);
                if (tmpGene != null) {
                    tmpGenes.add(tmpGene);
                }
            }
            if (tmpGenes.size() > 0) {

                List<Gene> hasRelevantAltsGenes = new ArrayList<>();
                for (Gene tmpGene : tmpGenes) {
                    List<Alteration> tmpRelevantAlts = findRelevantAlts(tmpGene, query.getHugoSymbol() + " Fusion");
                    if (tmpRelevantAlts != null && tmpRelevantAlts.size() > 0) {
                        hasRelevantAltsGenes.add(tmpGene);
                    }
                }

                if (hasRelevantAltsGenes.size() > 1) {
                    map.put("hasRelevantAltsGenes", hasRelevantAltsGenes);
                } else if (hasRelevantAltsGenes.size() == 1) {
                    gene = hasRelevantAltsGenes.iterator().next();
                    if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(query.getAlteration())) {
                        relevantAlterations = findRelevantAlts(gene, query.getAlteration());
                    } else {
                        relevantAlterations = findRelevantAlts(gene, query.getHugoSymbol() + " Fusion");
                    }
                }

                // None of relevant alterations found in both genes.
                if (gene == null) {
                    gene = tmpGenes.iterator().next();
                }
                map.put("allGenes", tmpGenes);
            }
        } else if (geneStrsSet.size() == 1) {
            String geneStr = geneStrsSet.iterator().next();
            if (geneStr != null) {
                Gene tmpGene = GeneUtils.getGeneByHugoSymbol(geneStr);
                if (tmpGene != null) {
                    gene = tmpGene;
                    Alteration alt = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getAlteration(),
                        AlterationType.getByName(query.getAlterationType()), query.getConsequence(), null, null);
                    AlterationUtils.annotateAlteration(alt, alt.getAlteration());
                    if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(query.getAlteration())) {
                        relevantAlterations = findRelevantAlts(gene, query.getAlteration());
                    } else {
                        relevantAlterations = AlterationUtils.getRelevantAlterations(alt);

                        // Map Truncating Mutations to single gene fusion event
                        Alteration truncatingMutations = AlterationUtils.getTruncatingMutations(gene);
                        if (truncatingMutations != null && !relevantAlterations.contains(truncatingMutations)) {
                            relevantAlterations.add(truncatingMutations);
                        }
                    }
                }
            }
            LinkedHashSet<Gene> allGenes = new LinkedHashSet<>();
            for (String subGeneStr : geneStrsSet) {
                Gene tmpGene = GeneUtils.getGeneByHugoSymbol(subGeneStr);
                if (tmpGene != null) {
                    allGenes.add(tmpGene);
                }
            }
            map.put("allGenes", allGenes);
        }

        map.put("pickedGene", gene);
        map.put("relevantAlts", relevantAlterations);
        return map;
    }
}

class IndicatorQueryRespComp implements Comparator<IndicatorQueryResp> {

    public IndicatorQueryRespComp() {
    }

    @Override
    public int compare(IndicatorQueryResp e1, IndicatorQueryResp e2) {
        Integer result = LevelUtils.compareLevel(e1.getHighestSensitiveLevel(), e2.getHighestSensitiveLevel());
        if (result != 0) {
            return result;
        }

        result = LevelUtils.compareLevel(e1.getHighestResistanceLevel(), e2.getHighestResistanceLevel());
        if (result != 0) {
            return result;
        }

        result = MainUtils.compareOncogenicity(Oncogenicity.getByEffect(e1.getOncogenic()), Oncogenicity.getByEffect(e2.getOncogenic()), true);

        if (result != 0) {
            return result;
        }

        if (e1.getGeneExist() == null || !e1.getGeneExist()) {
            return 1;
        }

        if (e2.getGeneExist() == null || !e2.getGeneExist()) {
            return -1;
        }
        return -1;
    }
}

class IndicatorQueryOncogenicity {
    Oncogenicity oncogenicity;
    Evidence oncogenicityEvidence;

    public IndicatorQueryOncogenicity(Oncogenicity oncogenicity, Evidence oncogenicityEvidence) {
        this.oncogenicity = oncogenicity;
        this.oncogenicityEvidence = oncogenicityEvidence;
    }

    public Oncogenicity getOncogenicity() {
        return oncogenicity;
    }

    public Evidence getOncogenicityEvidence() {
        return oncogenicityEvidence;
    }
}


class IndicatorQueryMutationEffect {
    MutationEffect mutationEffect;
    Evidence mutationEffectEvidence;

    public IndicatorQueryMutationEffect() {
    }

    public MutationEffect getMutationEffect() {
        return mutationEffect;
    }

    public Evidence getMutationEffectEvidence() {
        return mutationEffectEvidence;
    }

    public void setMutationEffect(MutationEffect mutationEffect) {
        this.mutationEffect = mutationEffect;
    }

    public void setMutationEffectEvidence(Evidence mutationEffectEvidence) {
        this.mutationEffectEvidence = mutationEffectEvidence;
    }
}

class UniqueImplication {
    private Implication e;

    public UniqueImplication(Implication e) {
        this.e = e;
    }

    public Implication unwrap() {
        return this.e;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniqueImplication that = (UniqueImplication) o;
        return Objects.equals(e.getLevelOfEvidence(), that.e.getLevelOfEvidence()) && Objects.equals(e.getTumorType(), that.e.getTumorType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(e.getLevelOfEvidence()) + Objects.hashCode(e.getTumorType());
    }
}
