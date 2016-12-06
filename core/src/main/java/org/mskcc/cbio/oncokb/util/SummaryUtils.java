package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Hongxin on 8/10/15.
 */
public class SummaryUtils {

    public static long lastUpdateVariantSummaries = new Date().getTime();

    private static String[] SpecialMutations = {"amplification", "deletion", "fusion", "fusions", "activating mutations", "inactivating mutations", "all mutations", "truncating mutations"};

    public static String variantTumorTypeSummary(Gene gene, List<Alteration> alterations, String queryAlteration, Set<OncoTreeType> relevantTumorTypes, String queryTumorType) {
        if (gene == null) {
            return "";
        }
        String geneId = Integer.toString(gene.getEntrezGeneId());
        String key = geneId + "&&" + queryAlteration + "&&" + queryTumorType;
        if (CacheUtils.isEnabled() && CacheUtils.containVariantSummary(gene.getEntrezGeneId(), key)) {
            return CacheUtils.getVariantSummary(gene.getEntrezGeneId(), key);
        }

        StringBuilder sb = new StringBuilder();

        //Mutation summary (MUTATION_SUMMARY: Deprecated)
//            List<Evidence> mutationSummaryEvs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_SUMMARY));
//            if (!mutationSummaryEvs.isEmpty()) {
//                Evidence ev = mutationSummaryEvs.get(0);
//                String mutationSummary = ev.getShortDescription();
//
//                if (mutationSummary == null) {
//                    mutationSummary = ev.getDescription();
//                }
//                if (mutationSummary != null) {
//                    mutationSummary = StringEscapeUtils.escapeXml(mutationSummary).trim();
//                    sb.append(mutationSummary)
//                            .append(" ");
//                }
//            } else {
//            }

        String os = oncogenicSummary(gene, alterations, queryAlteration, false);
        if (os != null && !os.equals("")) {
            sb.append(" " + os);
        }

        String ts = tumorTypeSummary(gene, queryAlteration, alterations, queryTumorType, relevantTumorTypes);
        if (ts != null && !ts.equals("")) {
            sb.append(" " + ts);
        }

        if (CacheUtils.isEnabled()) {
            CacheUtils.setVariantSummary(gene.getEntrezGeneId(), key, sb.toString().trim());
        }
        return sb.toString().trim();
    }

    public static String variantCustomizedSummary(Set<Gene> genes, List<Alteration> alterations, String queryAlteration, Set<OncoTreeType> relevantTumorTypes, String queryTumorType) {
        String geneId = Integer.toString(genes.iterator().next().getEntrezGeneId());
        Gene gene = GeneUtils.getGeneByEntrezId(Integer.parseInt(geneId));

        StringBuilder sb = new StringBuilder();

        sb.append(geneSummary(genes.iterator().next()));

        String os = oncogenicSummary(gene, alterations, queryAlteration, false);
        if (os != null && !os.equals("")) {
            sb.append(" " + os);
        }

        return sb.toString().trim();
    }

    public static String tumorTypeSummary(Gene gene, String queryAlteration, List<Alteration> alterations, String queryTumorType, Set<OncoTreeType> relevantTumorTypes) {
        //Tumor type summary
        Boolean ttSummaryNotGenerated = true;
        String tumorTypeSummary = null;

        queryTumorType = queryTumorType != null ? StringUtils.isAllUpperCase(queryTumorType) ? queryTumorType : queryTumorType.toLowerCase() : null;

        if (queryAlteration != null) {
            queryAlteration = queryAlteration.trim();
        }

        if (queryTumorType != null) {
            queryTumorType = queryTumorType.trim();
            if (queryTumorType.endsWith(" tumor")) {
                queryTumorType = queryTumorType.substring(0, queryTumorType.lastIndexOf(" tumor")) + " tumors";
            }
        }

        if (isSpecialMutation(queryAlteration, true)) {
            queryAlteration = queryAlteration.toLowerCase();
        }

        if (AlterationUtils.isSingularGeneralAlteration(queryAlteration)) {
            queryAlteration = queryAlteration + "s";
        }

        Boolean appendThe = appendThe(queryAlteration);

        if (gene == null || alterations == null || relevantTumorTypes == null) {
            return "";
        }

        if (CacheUtils.isEnabled() && CacheUtils.containVariantTumorTypeSummary(gene.getEntrezGeneId(), queryAlteration, queryTumorType)) {
            return CacheUtils.getVariantTumorTypeSummary(gene.getEntrezGeneId(), queryAlteration, queryTumorType);
        }

        // Get all tumor type summary evidences specifically for the alteration
        if (ttSummaryNotGenerated) {
            Alteration alteration = AlterationUtils.findAlteration(gene, queryAlteration);
            if (alteration != null) {
                tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), relevantTumorTypes, null));
                if (tumorTypeSummary != null) {
                    ttSummaryNotGenerated = false;
                }

                // Get Other Tumor Types summary within this alteration
                if (ttSummaryNotGenerated) {
                    tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), Collections.singleton(TumorTypeUtils.getMappedSpecialTumor(SpecialTumorType.OTHER_TUMOR_TYPES)), null));
                    if (tumorTypeSummary != null) {
                        ttSummaryNotGenerated = false;
                    }
                }
            }
        }

        // Get all tumor type summary evidence for relevant alterations
        if (ttSummaryNotGenerated) {
            // Base on the priority of relevant alterations
            for(Alteration alteration : alterations) {
                tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), relevantTumorTypes, null));
                if (tumorTypeSummary != null) {
                    ttSummaryNotGenerated = false;
                    break;
                }
            }
        }

        // Get Other Tumor Types summary
        if (ttSummaryNotGenerated) {
            // Base on the priority of relevant alterations
            for(Alteration alteration : alterations) {
                tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), Collections.singleton(TumorTypeUtils.getMappedSpecialTumor(SpecialTumorType.OTHER_TUMOR_TYPES)), null));
                if (tumorTypeSummary != null) {
                    ttSummaryNotGenerated = false;
                    break;
                }
            }
        }

        if (ttSummaryNotGenerated) {
            StringBuilder sb = new StringBuilder();
            Set<EvidenceType> sensitivityEvidenceTypes =
                EnumSet.of(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY,
                    EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
            Map<LevelOfEvidence, List<Evidence>> evidencesByLevel = groupEvidencesByLevel(new ArrayList<Evidence>(
                EvidenceUtils.getEvidence(alterations,
                    sensitivityEvidenceTypes, relevantTumorTypes, null)));
            List<Evidence> evidences = new ArrayList<>();
            //                if (!evidencesByLevel.get(LevelOfEvidence.LEVEL_0).isEmpty()) {
            //                    evidences.addAll(evidencesByLevel.get(LevelOfEvidence.LEVEL_0));
            //                }

            String altName = getGeneMutationNameInTumorTypeSummary(gene, queryAlteration);
            if (!evidencesByLevel.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                // if there are FDA approved drugs in the patient tumor type with the variant
                evidences.addAll(evidencesByLevel.get(LevelOfEvidence.LEVEL_1));
                sb.append(treatmentsToStringByTumorType(evidences, altName, queryTumorType, true, true, false, false))
                    .append(".");
            } else if (!evidencesByLevel.get(LevelOfEvidence.LEVEL_2A).isEmpty()) {
                // if there are NCCN guidelines in the patient tumor type with the variant
                //                Map<LevelOfEvidence, List<Evidence>> otherEvidencesByLevel = groupEvidencesByLevel(
                //                        evidenceBo.findEvidencesByAlteration(alterations, sensitivityEvidenceTypes)
                //                );
                //                if (!otherEvidencesByLevel.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                //                    // FDA approved drugs in other tumor type with the variant
                //                    sb.append("There are FDA approved drugs ")
                //                        .append(treatmentsToStringbyTumorType(otherEvidencesByLevel.get(LevelOfEvidence.LEVEL_1), altName))
                //                        .append(". ");
                //                }
                evidences.addAll(evidencesByLevel.get(LevelOfEvidence.LEVEL_2A));
                sb.append(treatmentsToStringByTumorType(evidences, altName, queryTumorType, true, false, true, false))
                    .append(".");
            } else {
                // no FDA or NCCN in the patient tumor type with the variant
                Map<LevelOfEvidence, List<Evidence>> evidencesByLevelOtherTumorType = groupEvidencesByLevel(
                    EvidenceUtils.getEvidence(alterations, sensitivityEvidenceTypes, null)
                );
                evidences.clear();
                //                    if (!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_0).isEmpty()) {
                //                        evidences.addAll(evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_0));
                //                    }

                if (!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                    // if there are FDA approved drugs in other tumor types with the variant
                    evidences.addAll(evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_1));
                    sb.append("While ")
                        .append(treatmentsToStringByTumorType(evidences, altName, queryTumorType, false, true, false, true))
                        .append(", the clinical utility for patients with ")
                        .append(queryTumorType == null ? "tumors" : queryTumorType)
                        .append(" harboring the " + altName)
                        .append(" is unknown.");
                } else if (!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_2A).isEmpty()) {
                    // if there are NCCN drugs in other tumor types with the variant
                    evidences.addAll(evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_2A));
                    sb.append(treatmentsToStringByTumorType(evidences, altName, queryTumorType, true, false, true, true))
                        .append(", the clinical utility for patients with ")
                        .append(queryTumorType == null ? "tumors" : queryTumorType)
                        .append(" harboring the " + altName)
                        .append(" is unknown.");
                } else if (gene.getHugoSymbol().equals("EGFR")) {
                    // Special summary specifically designed for investigated/non-investigate VUSs
                    sb.append("While EGFR tyrosine kinase inhibitors such as erlotinib, gefitinib and afatinib are " +
                        "FDA-approved for the treatment of patients with non-small cell lung cancer (NSCLC), " +
                        "their clinical utility in patients with " + altName + " " + queryTumorType + " is unknown.");
                } else {
                    // no FDA or NCCN drugs for the variant in any tumor type -- remove wild type evidence
                    Set<Evidence> evs = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, sensitivityEvidenceTypes);
                    Alteration alt = AlterationUtils.getAlteration(gene.getHugoSymbol(), "wildtype", AlterationType.MUTATION.label(), null, null, null);
                    EvidenceUtils.removeByAlterations(new ArrayList<Evidence>(evs), Collections.singleton(alt));
                    Map<LevelOfEvidence, List<Evidence>> evidencesByLevelGene = groupEvidencesByLevel(new ArrayList<Evidence>(evs));

                    evidences.clear();
                    //                        if (!evidencesByLevelGene.get(LevelOfEvidence.LEVEL_0).isEmpty()) {
                    //                            evidences.addAll(evidencesByLevelGene.get(LevelOfEvidence.LEVEL_0));
                    //                        }
                    if (!evidencesByLevelGene.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                        // if there are FDA approved drugs for different variants in the same gene (either same tumor type or different ones) .. e.g. BRAF K601E
                        evidences.addAll(evidencesByLevelGene.get(LevelOfEvidence.LEVEL_1));
                        sb.append("While ")
                            .append(treatmentsToStringByTumorType(evidences, null, queryTumorType, false, true, false, true))
                            .append(", the clinical utility for patients with ")
                            .append(queryTumorType == null ? "tumors" : queryTumorType)
                            .append(" harboring the " + altName)
                            .append(" is unknown.");
                    } else if (!evidencesByLevelGene.get(LevelOfEvidence.LEVEL_2A).isEmpty()) {
                        // if there are NCCN drugs for different variants in the same gene (either same tumor type or different ones) .. e.g. BRAF K601E
                        evidences.addAll(evidencesByLevelGene.get(LevelOfEvidence.LEVEL_2A));
                        sb.append(treatmentsToStringByTumorType(evidences, null, queryTumorType, true, false, true, true))
                            .append(", the clinical utility for patients with ")
                            .append(queryTumorType == null ? "tumors" : queryTumorType)
                            .append(" harboring the " + altName)
                            .append(" is unknown.");
                    } else {
                        // if there is no FDA or NCCN drugs for the gene at all
                        sb.append("There are no FDA-approved or NCCN-compendium listed treatments specifically for patients with ")
                            .append(queryTumorType == null ? "tumors" : queryTumorType)
                            .append(" harboring ");
                        if (appendThe) {
                            sb.append("the ");
                        }
                        sb.append(altName)
                            .append(".");
                    }
                }

                //                sb.append("Please refer to the clinical trials section. ");
            }
            tumorTypeSummary = sb.toString();
        } else {
            tumorTypeSummary = replaceSpecialCharacterInTumorTypeSummary(tumorTypeSummary, gene, queryAlteration, queryTumorType);
        }

        if (CacheUtils.isEnabled()) {
            CacheUtils.setVariantTumorTypeSummary(gene.getEntrezGeneId(), queryAlteration, queryTumorType, tumorTypeSummary);
        }

        return tumorTypeSummary;
    }

    public static String unknownOncogenicSummary() {
        return "The oncogenic activity of this variant is unknown and it has not been specifically investigated by the OncoKB team.";
    }

    public static String oncogenicSummary(Gene gene, List<Alteration> alterations, String queryAlteration, Boolean addition) {
        StringBuilder sb = new StringBuilder();

        if (gene == null || alterations == null || alterations.isEmpty() || AlterationUtils.excludeVUS(alterations).size() == 0) {
            if (gene != null && queryAlteration != null) {
                Alteration alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), queryAlteration, AlterationType.MUTATION.label(), null, null, null);
                if (alteration == null) {
                    alteration = new Alteration();
                    alteration.setGene(gene);
                    alteration.setAlterationType(AlterationType.MUTATION);
                    alteration.setAlteration(queryAlteration);
                    alteration.setName(queryAlteration);
                    AlterationUtils.annotateAlteration(alteration, queryAlteration);
                }

                if (AlterationUtils.hasAlleleAlterations(alteration)) {
                    sb.append(" " + alleleSummary(alteration));
                } else if (AlterationUtils.excludeVUS(alterations).size() == 0) {
                    List<Evidence> evidences = EvidenceUtils.getEvidence(alterations, Collections.singleton(EvidenceType.VUS), null);
                    Date lastEdit = null;
                    for (Evidence evidence : evidences) {
                        if (evidence.getLastEdit() == null) {
                            continue;
                        }
                        if (lastEdit == null) {
                            lastEdit = evidence.getLastEdit();
                        } else if (lastEdit.compareTo(evidence.getLastEdit()) < 0) {
                            lastEdit = evidence.getLastEdit();
                        }
                    }
                    if (lastEdit == null) {
                        sb.append(unknownOncogenicSummary());
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                        sb.append("As of " + sdf.format(lastEdit) + ", no functional data about this variant was available.");
                    }
                } else {
                    sb.append(unknownOncogenicSummary());
                }
            } else {
                sb.append(unknownOncogenicSummary());
            }
        } else {
            if (isSpecialMutation(queryAlteration, false)) {
                if (isSpecialMutation(queryAlteration, true)) {
                    queryAlteration = queryAlteration.substring(0, 1).toUpperCase() + queryAlteration.substring(1);
                }
            }

            String altName = getGeneMutationNameInVariantSummary(gene, queryAlteration);

            if (gene == null || alterations == null) {
                return null;
            }
            String geneId = Integer.toString(gene.getEntrezGeneId());
            String key = geneId + "&&" + queryAlteration + "&&" + addition.toString();
            if (CacheUtils.isEnabled() && CacheUtils.containVariantSummary(gene.getEntrezGeneId(), key)) {
                return CacheUtils.getVariantSummary(gene.getEntrezGeneId(), key);
            }

            Boolean appendThe = appendThe(queryAlteration);
            Boolean isPlural = false;

            if (queryAlteration.toLowerCase().contains("fusions")) {
                isPlural = true;
            }

            int oncogenic = -1;
            for (Alteration a : alterations) {
                List<Evidence> oncogenicEvidences = EvidenceUtils.getEvidence(Collections.singletonList(a), Collections.singleton(EvidenceType.ONCOGENIC), null);
                if (oncogenicEvidences != null && oncogenicEvidences.size() > 0) {
                    Evidence evidence = oncogenicEvidences.iterator().next();
                    if (evidence != null && evidence.getKnownEffect() != null && Integer.parseInt(evidence.getKnownEffect()) >= 0) {
                        oncogenic = Integer.parseInt(evidence.getKnownEffect());
                        break;
                    }
                }
            }
            if (oncogenic >= 0) {
                if (appendThe) {
                    sb.append("The ");
                }
                sb.append(altName);

                if (isPlural) {
                    sb.append(" are");
                } else {
                    sb.append(" is");
                }

                if (oncogenic == 0) {
                    sb.append(" likely neutral.");
                } else {
                    if (oncogenic == 2) {
                        sb.append(" likely");
                    } else if (oncogenic == 1) {
                        sb.append(" known to be");
                    }

                    sb.append(" oncogenic.");
                }
            } else {
                sb.append("It is unknown whether ");
                if (appendThe) {
                    sb.append("the ");
                }

                sb.append(altName);

                if (isPlural) {
                    sb.append(" are");
                } else {
                    sb.append(" is");
                }
                sb.append(" oncogenic.");
            }

            if (addition) {
                List<Evidence> oncogenicEvs = EvidenceUtils.getEvidence(alterations, Collections.singleton(EvidenceType.ONCOGENIC), null);
                List<String> clinicalSummaries = new ArrayList<>();

                for (Evidence evidence : oncogenicEvs) {
                    if (evidence.getDescription() != null && !evidence.getDescription().isEmpty()) {
                        clinicalSummaries.add(evidence.getDescription());
                    }
                }

                if (clinicalSummaries.size() > 1) {
                    sb.append("Warning: variant has multiple clinical summaries.");
                } else if (clinicalSummaries.size() == 1) {
                    sb.append(clinicalSummaries.get(0));
                }
            }

            if (CacheUtils.isEnabled()) {
                CacheUtils.setVariantSummary(gene.getEntrezGeneId(), key, sb.toString().trim());
            }
        }

        return sb.toString();
    }

    public static String geneSummary(Gene gene) {
        Set<Evidence> geneSummaryEvs = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.GENE_SUMMARY));
        String summary = "";
        if (!geneSummaryEvs.isEmpty()) {
            Evidence ev = geneSummaryEvs.iterator().next();
            if (ev != null) {
                summary = ev.getDescription();

                if (summary != null) {
                    summary = StringEscapeUtils.escapeXml(summary).trim();
                }
            }
        }

        summary = summary.trim();
        summary = summary.endsWith(".") ? summary : summary + ".";
        return summary;
    }

    public static String fullSummary(Gene gene, List<Alteration> alterations, String queryAlteration, Set<OncoTreeType> relevantTumorTypes, String queryTumorType) {
        StringBuilder sb = new StringBuilder();

        sb.append(geneSummary(gene));

        String vts = SummaryUtils.variantTumorTypeSummary(gene, alterations, queryAlteration, relevantTumorTypes, queryTumorType);
        if (vts != null && !vts.equals("")) {
            sb.append(" " + vts);
        }

        return sb.toString();
    }

    public static String alleleSummary(Alteration alteration) {
        StringBuilder sb = new StringBuilder();

        String geneStr = alteration.getGene().getHugoSymbol();
        String altStr = alteration.getAlteration();

        sb.append(geneStr + " " + altStr);
        sb.append(" has not been functionally or clinically validated.");

        Set<Alteration> alleles = new HashSet<>(AlterationUtils.getAlleleAlterations(alteration));

        Map<String, Object> map = geAlterationsWithHighestOncogenicity(new HashSet<>(alleles));
        Oncogenicity highestOncogenicity = (Oncogenicity) map.get("oncogenicity");
        Set<Alteration> highestAlts = (Set<Alteration>) map.get("alterations");

        if (highestOncogenicity != null && (highestOncogenicity.getOncogenic().equals("1") || highestOncogenicity.getOncogenic().equals("2"))) {

            sb.append(" However, ");
            sb.append(allelesToStr(highestAlts));
            sb.append((highestAlts.size() > 1 ? " are" : " is"));
            if (highestOncogenicity.getOncogenic().equals("1")) {
                sb.append(" known to be " + highestOncogenicity.getDescription().toLowerCase());
            } else {
                sb.append(" " + highestOncogenicity.getDescription().toLowerCase());
            }
            sb.append(", and therefore " + geneStr + " " + altStr + " is considered likely oncogenic.");
        }

        // Detemin whether allele alterations have treatments
        List<Evidence> treatmentsEvis = EvidenceUtils.getEvidence(new ArrayList<>(alleles), MainUtils.getSensitiveTreatmentEvidenceTypes(), null);
        LevelOfEvidence highestLevel = LevelUtils.getHighestLevelFromEvidence(new HashSet<>(treatmentsEvis));
        Set<Treatment> treatments = new HashSet<>();
        Set<Alteration> highestLevelTreatmentRelatedAlts = new HashSet<>();

        // If there are no treatments for the alleles, try to find whether there are alleles are oncogenic or likely oncogenic
        if (treatmentsEvis != null && treatmentsEvis.size() > 0) {
            for (Evidence evidence : treatmentsEvis) {
                if (evidence.getLevelOfEvidence() != null && evidence.getLevelOfEvidence().equals(highestLevel)) {
                    treatments.addAll(evidence.getTreatments());
                    for (Alteration alt : evidence.getAlterations()) {
                        if (alleles.contains(alt)) {
                            highestLevelTreatmentRelatedAlts.add(alt);
                        }
                    }
                }
            }
        }

        if (treatments.size() > 0) {
            String treatmentStr = "";
            if (treatments.size() > 1) {
                treatmentStr = "multiple targeted therapies";
            } else {
                Set<String> drugs = new HashSet<>();
                for (Drug drug : treatments.iterator().next().getDrugs()) {
                    drugs.add(drug.getDrugName());
                }
                treatmentStr = StringUtils.join(drugs, " + ");
            }

            sb.append(" " + geneStr + " " + allelesToStr(highestLevelTreatmentRelatedAlts) + " mutant tumors have demonstrated sensitivity to "
                + treatmentStr + ", therefore "
                + geneStr + " " + altStr + " is considered likely sensitive to"
                + (treatments.size() > 1 ? " these therapies." : " this therapy."));
        }

        return sb.toString();
    }

    private static String alleleNamesStr(Set<Alteration> alterations) {
        if (alterations != null && alterations.size() > 0) {
            Alteration tmp = alterations.iterator().next();
            String residue = tmp.getRefResidues();
            String location = Integer.toString(tmp.getProteinStart());
            Set<String> variantResidue = new TreeSet<>();
            Set<Alteration> withoutVariantResidues = new HashSet<>();

            for (Alteration alteration : alterations) {
                if (alteration.getVariantResidues() == null) {
                    withoutVariantResidues.add(alteration);
                } else {
                    variantResidue.add(alteration.getVariantResidues());
                }
            }

            StringBuilder sb = new StringBuilder();

            if (variantResidue.size() > 0) {
                sb.append(residue + location + StringUtils.join(variantResidue, "/"));
            }

            if (withoutVariantResidues.size() > 0) {
                List<String> alterationNames = new ArrayList<>();
                for (Alteration alteration : withoutVariantResidues) {
                    alterationNames.add(alteration.getName());
                }
                if (variantResidue.size() > 0) {
                    sb.append(", ");
                }
                sb.append(MainUtils.listToString(alterationNames, ", "));
            }


            return sb.toString();
        } else {
            return "";
        }
    }

    private static String allelesToStr(Set<Alteration> alterations) {
        List<String> alterationNames = new ArrayList<>();
        Map<Integer, Set<Alteration>> locationBasedAlts = new HashMap<>();

        for (Alteration alteration : alterations) {
            if (!locationBasedAlts.containsKey(alteration.getProteinStart()))
                locationBasedAlts.put(alteration.getProteinStart(), new HashSet<Alteration>());

            locationBasedAlts.get(alteration.getProteinStart()).add(alteration);
        }

        for (Map.Entry entry : locationBasedAlts.entrySet()) {
            alterationNames.add(alleleNamesStr((Set<Alteration>) entry.getValue()));
        }

        return MainUtils.listToString(alterationNames, " and ");
    }

    private static Map<String, Object> geAlterationsWithHighestOncogenicity(Set<Alteration> alleles) {
        Map<Oncogenicity, Set<Alteration>> oncoCate = new HashMap<>();

        // Get oncogenicity info in alleles
        for (Alteration alt : alleles) {
            Set<EvidenceType> evidenceTypes = new HashSet<>();
            evidenceTypes.add(EvidenceType.ONCOGENIC);
            List<Evidence> allelesOnco = EvidenceUtils.getEvidence(Collections.singletonList(alt), evidenceTypes, null);

            for (Evidence evidence : allelesOnco) {
                String oncoStr = evidence.getKnownEffect();
                if (oncoStr == null)
                    continue;

                Oncogenicity oncogenicity = Oncogenicity.getByLevel(oncoStr);
                if (!oncoCate.containsKey(oncogenicity))
                    oncoCate.put(oncogenicity, new HashSet<Alteration>());

                oncoCate.get(oncogenicity).add(alt);
            }
        }

        Oncogenicity oncogenicity = MainUtils.findHighestOncogenic(oncoCate.keySet());
        Map<String, Object> result = new HashMap<>();
        result.put("oncogenicity", oncogenicity);
        result.put("alterations", oncoCate != null ? oncoCate.get(oncogenicity) : new HashSet<>());
        return result;
    }

    private static Map<LevelOfEvidence, List<Evidence>> groupEvidencesByLevel(List<Evidence> evidences) {
        Map<LevelOfEvidence, List<Evidence>> map = new EnumMap<LevelOfEvidence, List<Evidence>>(LevelOfEvidence.class);
        for (LevelOfEvidence level : LevelOfEvidence.values()) {
            map.put(level, new ArrayList<Evidence>());
        }
        for (Evidence ev : evidences) {
            if (ev.getLevelOfEvidence() == null || ev.getTreatments().isEmpty()) continue;
            map.get(ev.getLevelOfEvidence()).add(ev);
        }
        return map;
    }

    // According to following rules
    //    IF ≤2 SAME drug for ≤2 different cancer types
    //            include
    //
    //    e.g. While the drugs dabrafenib, trametinib and vemurafenib are FDA-approved for patients with BRAF V600E mutant melanoma, bladder or breast cancer, the clinical utility for these agents in patients with BRAF V600E mutant low grade gliomas is unknown.
    //
    //    IF >2 SAME drug for >2 different cancer types
    //            include
    //
    //    While there are FDA-approved drugs for patients with specific cancers harboring the BRAF V600E mutation (please refer to FDA-approved drugs in Other Tumor types section), the clinical utility for these agents in patients with BRAF V600E mutant low grade gliomas is unknown.
    //
    //    IF <2 DIFFERENT drugs for <2 different tumor types
    //
    //    While there are FDA-approved drugs for patients with lung and colorectal cancers harboring the EGFR L858R mutation (please refer to FDA-approved drugs in Other Tumor types section), the clinical utility for these agents in patients with EGFR L858R mutant low grade gliomas is unknown.
    private static String treatmentsToStringByTumorType(List<Evidence> evidences, String queryAlteration, String queryTumorType, boolean capFirstLetter, boolean fda, boolean nccn, boolean inOtherTumorType) {
        // Tumor type -> drug -> LevelOfEvidence and alteration set
        Map<String, Map<String, Map<String, Object>>> map = new TreeMap<>();
        Set<String> drugs = new HashSet<>();
        Map<String, Set<String>> levelZeroDrugs = new HashMap<>();
        List<String> list = new ArrayList<String>();

        for (Evidence ev : evidences) {
            String tt = null;
            if (ev.getSubtype() != null) {
                tt = ev.getSubtype().toLowerCase();
            } else if (ev.getCancerType() != null) {
                tt = ev.getCancerType().toLowerCase();
            }

            if (tt == null) {
                continue;
            }

            Map<String, Map<String, Object>> ttMap = map.get(tt);
            if (ttMap == null && !ev.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_0)) {
                ttMap = new TreeMap<String, Map<String, Object>>();
                map.put(tt, ttMap);
            }

            for (Treatment t : ev.getTreatments()) {
                for (Drug drug : t.getDrugs()) {
                    String drugName = drug.getDrugName().toLowerCase();
                    if (ev.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_0)) {
                        if (!levelZeroDrugs.containsKey(drugName)) {
                            levelZeroDrugs.put(drugName, new HashSet<String>());
                        }
                        if (!levelZeroDrugs.get(drugName).contains(tt)) {
                            levelZeroDrugs.get(drugName).add(tt);
                        }
                    } else {
                        Map<String, Object> drugMap = ttMap.get(drugName);
                        if (!drugs.contains(drugName)) {
                            drugs.add(drugName);
                        }
                        if (drugMap == null) {
                            drugMap = new TreeMap<>();
                            ttMap.put(drugName, drugMap);
//                            drugMap.put("approvedIndications", t.getApprovedIndications());
                            drugMap.put("level", ev.getLevelOfEvidence());
                            drugMap.put("alteration", ev.getAlterations());
                        }
                    }
                }
            }
        }

        if (map.size() > 2) {
            list.add(treatmentsToStringAboveLimit(drugs, capFirstLetter, fda, nccn, null));
        } else {
            boolean first = true;
            for (Map.Entry<String, Map<String, Map<String, Object>>> entry : map.entrySet()) {
                String tt = entry.getKey();
                list.add(treatmentsToString(entry.getValue(), tt, queryAlteration, first & capFirstLetter, fda, nccn));
                first = false;
            }
        }
//        if(levelZeroDrugs.size() > 0) {
//            list.add(treatmentsToStringLevelZero(levelZeroDrugs, list.size()==0 & capFirstLetter));
//        }
        return MainUtils.listToString(list, " and ");
    }

    private static String treatmentsToStringLevelZero(Map<String, Set<String>> drugs, Boolean capFirstLetter) {
        StringBuilder sb = new StringBuilder();
        Set<String> tumorTypes = new HashSet<>();
        boolean sameDrugs = true;

        for (String drugName : drugs.keySet()) {
            if (tumorTypes.isEmpty()) {
                tumorTypes = drugs.get(drugName);
            } else {
                if (tumorTypes.size() != drugs.get(drugName).size()) {
                    sameDrugs = false;
                    break;
                }
                for (String tt : drugs.get(drugName)) {
                    if (!tumorTypes.contains(tt)) {
                        sameDrugs = false;
                        break;
                    }
                }
            }
        }

        if (sameDrugs) {
            sb.append(drugStr(drugs.keySet(), capFirstLetter, true, false, null));
        } else {
            sb.append(capFirstLetter ? "T" : "t")
                .append("here are multiple FDA-approved agents");
        }
        sb.append(" for treatment of patients with ");
        sb.append(tumorTypes.size() > 2 ? "different tumor types" : MainUtils.listToString(new ArrayList<String>(tumorTypes), " and "))
            .append(" irrespective of mutation status");
        return sb.toString();
    }

    private static String treatmentsToStringAboveLimit(Set<String> drugs, boolean capFirstLetter, boolean fda, boolean nccn, String approvedIndication) {
        StringBuilder sb = new StringBuilder();
        sb.append(drugStr(drugs, capFirstLetter, fda, nccn, null));
        sb.append(" for treatment of patients with different tumor types harboring specific mutations");
        return sb.toString();
    }

    private static String treatmentsToString(Map<String, Map<String, Object>> map, String tumorType, String alteration, boolean capFirstLetter, boolean fda, boolean nccn) {
        Set<String> drugs = map.keySet();
        Map<String, Object> drugAltMap = drugsAreSameByAlteration(map);
        StringBuilder sb = new StringBuilder();
        Map<String, Object> drugMap = map.get(drugs.iterator().next());
//        Set<String> approvedIndications = (Set<String>) drugMap.get("approvedIndications");
        String aiStr = null;

//        for (String ai : approvedIndications) {
//            if (ai != null && !ai.isEmpty()) {
//                aiStr = ai;
//                break;
//            }
//        }

        sb.append(drugStr(drugs, capFirstLetter, fda, nccn, aiStr))
            .append(" for treatment of patients ")
            .append(tumorType == null ? "" : ("with " + tumorType + " "))
            .append("harboring ");

        if (alteration != null) {
            sb.append("the ").append(alteration);
        } else if ((Boolean) drugAltMap.get("isSame")) {
            Set<Alteration> alterations = (Set<Alteration>) drugAltMap.get("alterations");

            if (alterations.size() <= 2) {
                sb.append("the ").append(alterationsToString(alterations));
            } else {
                sb.append("specific mutations");
            }

        } else {
            sb.append("specific mutations");
        }
        return sb.toString();
    }

    private static String drugStr(Set<String> drugs, boolean capFirstLetter, boolean fda, boolean nccn, String approvedIndication) {
        int drugLimit = 3;

        StringBuilder sb = new StringBuilder();

        if (drugs.size() > drugLimit) {
            sb.append(capFirstLetter ? "T" : "t").append("here");
        } else {
            sb.append(capFirstLetter ? "T" : "t").append("he drug");
            if (drugs.size() > 1) {
                sb.append("s");
            }
            sb.append(" ");
            sb.append(MainUtils.listToString(new ArrayList<String>(drugs), " and "));
        }
        if (fda || nccn) {
            sb.append(" ");
            if (drugs.size() > 1) {
                sb.append("are");
            } else {
                sb.append("is");
            }
        }

        if (fda) {
            sb.append(" FDA-approved");
        } else if (nccn) {
            if (approvedIndication != null) {
                sb.append(" FDA-approved for the treatment of ")
                    .append(approvedIndication)
                    .append(" and");
            }

            if (drugs.size() > drugLimit || approvedIndication != null) {
                sb.append(" NCCN-compendium listed");
            } else if (drugs.size() <= drugLimit) {
                sb.append(" listed by NCCN-compendium");
            }
        }

        if (drugs.size() > drugLimit) {
            sb.append(" drugs");
        }

        return sb.toString();
    }

    private static Map<String, Object> drugsAreSameByAlteration(Map<String, Map<String, Object>> drugs) {
        Set<Alteration> alterations = new HashSet<>();
        Map<String, Object> map = new HashMap<>();

        map.put("isSame", true);
        map.put("alterations", alterations);

        for (String drugName : drugs.keySet()) {
            Map<String, Object> drug = drugs.get(drugName);
            Set<Alteration> alts = (Set<Alteration>) drug.get("alteration");
            if (alterations.isEmpty()) {
                alterations = alts;
            } else {
                if (alterations.size() != alts.size()) {
                    map.put("isSame", false);
                    return map;
                }

                for (Alteration alt : alts) {
                    if (!alterations.contains(alt)) {
                        map.put("isSame", false);
                        return map;
                    }
                }
            }
        }
        map.put("alterations", alterations);
        return map;
    }

    private static String alterationsToString(Collection<Alteration> alterations) {
        Map<String, Set<String>> mapGeneVariants = new TreeMap<String, Set<String>>();
        for (Alteration alteration : alterations) {
            String gene = alteration.getGene().getHugoSymbol();
            Set<String> variants = mapGeneVariants.get(gene);
            if (variants == null) {
                variants = new TreeSet<String>();
                mapGeneVariants.put(gene, variants);
            }
            variants.add(alteration.getName());
        }

        List<String> list = new ArrayList<String>();
        for (Map.Entry<String, Set<String>> entry : mapGeneVariants.entrySet()) {
            list.add(entry.getKey() + " " + MainUtils.listToString(new ArrayList<String>(entry.getValue()), " and "));
        }

        String gene = alterations.iterator().next().getGene().getHugoSymbol();

        String ret = MainUtils.listToString(list, " or ");

        if (!ret.startsWith(gene)) {
            ret = gene + " " + ret;
        }

        String retLow = ret.toLowerCase();
        if (retLow.endsWith("mutation") || retLow.endsWith("mutations")) {
            return ret;
        }

        return ret + " mutation" + (alterations.size() > 1 ? "s" : "");
    }

    private static Boolean isSpecialMutation(String mutationStr, Boolean exactMatch) {
        exactMatch = exactMatch || false;
        mutationStr = mutationStr.toString();
        if (exactMatch) {
            return stringIsFromList(mutationStr, SpecialMutations);
        } else if (stringContainsItemFromList(mutationStr, SpecialMutations)
            && itemFromListAtEndString(mutationStr, SpecialMutations)) {
            return true;
        }
        return false;
    }

    private static Boolean appendThe(String queryAlteration) {
        Boolean appendThe = true;

        if (queryAlteration.toLowerCase().contains("deletion")
            || queryAlteration.toLowerCase().contains("amplification")
            || queryAlteration.toLowerCase().contains("fusion")) {
            appendThe = false;
        }
        return appendThe;
    }

    private static String getTumorTypeSummaryFromEvidences(List<Evidence> evidences) {
        String summary = null;
        if (evidences != null && evidences.size() > 0) {
            // Sort all tumor type summaries, the more specific tumor type summary will be picked. 
            Collections.sort(evidences, new Comparator<Evidence>() {
                public int compare(Evidence x, Evidence y) {
                    if (x.getAlterations() == null) {
                        return 1;
                    }
                    if (y.getAlterations() == null) {
                        return -1;
                    }
                    return x.getAlterations().size() - y.getAlterations().size();
                }
            });

            Evidence ev = evidences.get(0);
            String tumorTypeSummary = ev.getDescription();
            if (tumorTypeSummary != null) {
                summary = StringEscapeUtils.escapeXml(tumorTypeSummary).trim();
            }
        }
        return summary;
    }

    private static String getGeneMutationNameInVariantSummary(Gene gene, String queryAlteration) {
        StringBuilder sb = new StringBuilder();
        Alteration alteration = AlterationUtils.findAlteration(gene, queryAlteration);
        if (alteration == null) {
            alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), queryAlteration, null, null, null, null);
            AlterationUtils.annotateAlteration(alteration, queryAlteration);
        }
        if (isSpecialMutation(queryAlteration, true)) {
            sb.append(gene.getHugoSymbol() + " " + queryAlteration.toLowerCase());
        } else if (StringUtils.containsIgnoreCase(queryAlteration, "fusion")) {
            sb.append(queryAlteration);
        } else if (isSpecialMutation(queryAlteration, false)
            || (alteration.getConsequence() != null
            && (alteration.getConsequence().getTerm().equals("inframe_deletion")
            || alteration.getConsequence().getTerm().equals("inframe_insertion")))
            || StringUtils.containsIgnoreCase(queryAlteration, "indel")
            || StringUtils.containsIgnoreCase(queryAlteration, "splice")) {
            sb.append(gene.getHugoSymbol() + " " + queryAlteration + " alteration");
        } else {
            sb.append(gene.getHugoSymbol() + " " + queryAlteration + " mutation");
        }
        return sb.toString();
    }

    private static String getGeneMutationNameInTumorTypeSummary(Gene gene, String queryAlteration) {
        StringBuilder sb = new StringBuilder();
        Alteration alteration = AlterationUtils.findAlteration(gene, queryAlteration);
        if (alteration == null) {
            alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), queryAlteration, null, null, null, null);
            AlterationUtils.annotateAlteration(alteration, queryAlteration);
        }
        if (StringUtils.containsIgnoreCase(queryAlteration, "fusion")) {
            if (queryAlteration.toLowerCase().equals("fusions")) {
                queryAlteration = gene.getHugoSymbol() +  " fusion";
            }
            sb.append(queryAlteration + " positive");
        } else {
            sb.append(gene.getHugoSymbol() + " ");
            if (isSpecialMutation(queryAlteration, true)) {
                sb.append(queryAlteration.toLowerCase());
            } else if (isSpecialMutation(queryAlteration, false)
                || (alteration.getConsequence() != null
                && (alteration.getConsequence().getTerm().equals("inframe_deletion")
                || alteration.getConsequence().getTerm().equals("inframe_insertion")))
                || StringUtils.containsIgnoreCase(queryAlteration, "indel")
                || StringUtils.containsIgnoreCase(queryAlteration, "delins")
                || StringUtils.containsIgnoreCase(queryAlteration, "splice")
                ) {
                sb.append(queryAlteration + " altered");
            } else {
                sb.append(queryAlteration + " mutant");
            }
        }
        return sb.toString();
    }

    private static String replaceSpecialCharacterInTumorTypeSummary(String summary, Gene gene, String queryAlteration, String queryTumorType) {
        String altName = getGeneMutationNameInTumorTypeSummary(gene, queryAlteration);
        String alterationName = getGeneMutationNameInVariantSummary(gene, queryAlteration);
        summary = summary.replace("[[variant]]", altName + " " + queryTumorType);
        summary = summary.replace("[[gene]] [[mutation]] [[[mutation]]]", alterationName);
        summary = summary.replace("[[gene]] [[mutation]] [[mutant]]", altName);
        summary = summary.replace("[[mutation]] [[mutant]]", altName);
        summary = summary.replace("[[gene]]", gene.getHugoSymbol());
        summary = summary.replace("[[mutation]] [[[mutation]]]", alterationName);
        summary = summary.replace("[[mutation]]", queryAlteration);
        summary = summary.replace("[[tumorType]]", queryTumorType);
        summary = summary.replace("[[tumor type]]", queryTumorType);
        summary = summary.replace("[[fusion name]]", altName);
        summary = summary.replace("[[fusion name]]", altName);
        return summary;
    }

    public static boolean stringContainsItemFromList(String inputString, String[] items) {
        for (int i = 0; i < items.length; i++) {
            if (inputString.contains(items[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean stringIsFromList(String inputString, String[] items) {
        for (int i = 0; i < items.length; i++) {
            if (inputString.equalsIgnoreCase(items[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean itemFromListAtEndString(String inputString, String[] items) {
        for (int i = 0; i < items.length; i++) {
            if (inputString.endsWith(items[i])) {
                return true;
            }
        }
        return false;
    }
}