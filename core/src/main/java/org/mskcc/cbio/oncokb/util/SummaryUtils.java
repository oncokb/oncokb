package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hongxin on 8/10/15.
 */
public class SummaryUtils {

    public static long lastUpdateVariantSummaries = new Date().getTime();

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

        queryTumorType = convertTumorTypeNameInSummary(queryTumorType);

        if (queryAlteration != null) {
            queryAlteration = queryAlteration.trim();
        }

        if (queryTumorType != null) {
            queryTumorType = queryTumorType.trim();
            if (queryTumorType.endsWith(" tumor")) {
                queryTumorType = queryTumorType.substring(0, queryTumorType.lastIndexOf(" tumor")) + " tumors";
            }
        }

        if (AlterationUtils.isGeneralAlterations(queryAlteration, true)) {
            queryAlteration = queryAlteration.toLowerCase();
        }

        Boolean appendThe = appendThe(queryAlteration);

        if (gene == null || alterations == null || relevantTumorTypes == null) {
            return "";
        }

        if (CacheUtils.isEnabled() && CacheUtils.containVariantTumorTypeSummary(gene.getEntrezGeneId(), queryAlteration, queryTumorType)) {
            return CacheUtils.getVariantTumorTypeSummary(gene.getEntrezGeneId(), queryAlteration, queryTumorType);
        }

        if (gene.getHugoSymbol().equals("KIT")) {
            tumorTypeSummary = getKITtumorTypeSummaries(queryAlteration, alterations, queryTumorType, relevantTumorTypes);
        } else {
            // Get all tumor type summary evidences specifically for the alteration
            if (tumorTypeSummary == null) {
                Alteration alteration = AlterationUtils.findAlteration(gene, queryAlteration);
                if (alteration != null) {
                    tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), relevantTumorTypes, null));

                    // Get Other Tumor Types summary within this alteration
                    if (tumorTypeSummary == null) {
                        tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), Collections.singleton(TumorTypeUtils.getMappedSpecialTumor(SpecialTumorType.OTHER_TUMOR_TYPES)), null));
                    }
                }
            }

            // Get all tumor type summary evidences for the alternate alleles
            if (tumorTypeSummary == null) {
                Alteration alteration = AlterationUtils.findAlteration(gene, queryAlteration);
                if (alteration == null) {
                    alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), queryAlteration, null, null, null, null);
                    AlterationUtils.annotateAlteration(alteration, queryAlteration);
                }
                if (alteration.getConsequence() != null) {
                    if (alteration.getConsequence().getTerm().equals("missense_variant")) {
                        List<Alteration> alternateAlleles = AlterationUtils.getAlleleAndRelevantAlterations(alteration);

                        // Special case for PDGFRA: don't match D842V as alternative allele
                        if (gene.getHugoSymbol().equals("PDGFRA") && alteration.getProteinStart() == 842) {
                            Alteration specialAllele = AlterationUtils.findAlteration(gene, "D842V");
                            alternateAlleles.remove(specialAllele);
                        }

                        // Special case for AKT1 E17K alleles
                        if (gene.getHugoSymbol().equals("AKT1") && alteration.getProteinStart().equals(17)) {
                            OncoTreeType breastCancer = TumorTypeUtils.getOncoTreeCancerType("Breast Cancer");
                            OncoTreeType ovarianCancer = TumorTypeUtils.getOncoTreeCancerType("Ovarian Cancer");

                            if (relevantTumorTypes.contains(breastCancer)) {
                                tumorTypeSummary = "There is compelling clinical data supporting the use of AKT-targeted inhibitors such as AZD-5363 in patients with AKT1 E17K mutant breast cancer. Therefore, [[gene]] [[mutation]] [[mutant]] breast cancer is considered likely sensitive to the same inhibitors.";
                            } else if (relevantTumorTypes.contains(ovarianCancer)) {
                                tumorTypeSummary = "There is compelling clinical data supporting the use of AKT-targeted inhibitors such as AZD-5363 in patients with AKT1 E17K mutant ovarian cancer. Therefore, [[gene]] [[mutation]] [[mutant]] ovarian cancer is considered likely sensitive to the same inhibitors.";
                            } else {
                                tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(AlterationUtils.findAlteration(gene, "E17K")), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), Collections.singleton(TumorTypeUtils.getMappedSpecialTumor(SpecialTumorType.OTHER_TUMOR_TYPES)), null));
                            }
                        } else if (gene.getHugoSymbol().equals("ARAF") && alteration.getProteinStart().equals(214)) {
                            // Special case for ARAF S214A/F
                            OncoTreeType histiocytosis = TumorTypeUtils.getOncoTreeCancerType("Histiocytosis");
                            OncoTreeType NSCLC = TumorTypeUtils.getOncoTreeCancerType("Non-Small Cell Lung Cancer");

                            if (relevantTumorTypes.contains(histiocytosis)) {
                                tumorTypeSummary = "There is compelling clinical data supporting the use of sorafenib in patients with ARAF S214A mutant non-Langherhans cell histiocytic disease. Therefore, [[gene]] [[mutation]] [[mutant]] non-Langherhans cell histiocytic disease is considered likely sensitive to this inhibitor.";
                            } else if (relevantTumorTypes.contains(NSCLC)) {
                                tumorTypeSummary = "There is compelling clinical data supporting the use of sorafenib in patients with ARAF S214F mutant lung cancer. Therefore, [[gene]] [[mutation]] [[mutant]] lung cancer is considered likely sensitive to this inhibitor.";
                            } else {
                                tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(AlterationUtils.findAlteration(gene, "S214A")), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), Collections.singleton(TumorTypeUtils.getMappedSpecialTumor(SpecialTumorType.OTHER_TUMOR_TYPES)), null));
                            }
                        } else if (gene.getHugoSymbol().equals("MTOR") && alteration.getProteinStart().equals(2014)) {
                            // Special case for MTOR E2014K
                            OncoTreeType bladderCancer = TumorTypeUtils.getOncoTreeCancerType("Bladder Cancer");
                            if (relevantTumorTypes.contains(bladderCancer)) {
                                tumorTypeSummary = "There is compelling clinical data supporting the use of everolimus in patients with MTOR E2014K mutant bladder cancer. Therefore, [[gene]] [[mutation]] [[mutant]] bladder cancer is considered likely sensitive to the same inhibitor.";
                            } else {
                                tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(AlterationUtils.findAlteration(gene, "E2014K")), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), Collections.singleton(TumorTypeUtils.getMappedSpecialTumor(SpecialTumorType.OTHER_TUMOR_TYPES)), null));
                            }
                        } else {
                            for (Alteration allele : alternateAlleles) {
                                tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(allele), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), relevantTumorTypes, null));
                                if (tumorTypeSummary != null) {
                                    break;
                                }

                                // Get Other Tumor Types summary
                                tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(allele), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), Collections.singleton(TumorTypeUtils.getMappedSpecialTumor(SpecialTumorType.OTHER_TUMOR_TYPES)), null));
                                if (tumorTypeSummary != null) {
                                    break;
                                }
                            }
                        }
                    } else if (alteration.getConsequence().getTerm().equals("synonymous_variant")) {
                        // No summary for synonymous variant
                        return "";
                    }
                }
            }

            // Get all tumor type summary evidence for relevant alterations
            if (tumorTypeSummary == null) {
                // Base on the priority of relevant alterations
                for (Alteration alteration : alterations) {
                    tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), relevantTumorTypes, null));
                    if (tumorTypeSummary != null) {
                        break;
                    }

                    // Get Other Tumor Types summary
                    tumorTypeSummary = getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), Collections.singleton(TumorTypeUtils.getMappedSpecialTumor(SpecialTumorType.OTHER_TUMOR_TYPES)), null));
                    if (tumorTypeSummary != null) {
                        break;
                    }
                }
            }
        }

        if (tumorTypeSummary == null) {
            tumorTypeSummary = "There are no FDA-approved or NCCN-compendium listed treatments specifically for patients with [[variant]].";
        }

        tumorTypeSummary = replaceSpecialCharacterInTumorTypeSummary(tumorTypeSummary, gene, queryAlteration, queryTumorType);
        if (CacheUtils.isEnabled()) {
            CacheUtils.setVariantTumorTypeSummary(gene.getEntrezGeneId(), queryAlteration, queryTumorType, tumorTypeSummary);
        }

        return tumorTypeSummary;
    }

    public static String unknownOncogenicSummary(Gene gene) {
        String str = gene == null ? "variant" : (gene.getHugoSymbol() + " alteration");
        return "The oncogenic activity of this " + str + " is unknown as it has not specifically been investigated by the OncoKB team.";
    }

    public static String synonymousSummary() {
        return "This is a synonymous mutation and is not annotated by OncoKB.";
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

                if (alteration.getConsequence() != null && alteration.getConsequence().getTerm().equals("synonymous_variant")) {
                    sb.append(synonymousSummary());
                } else if (AlterationUtils.hasAlleleAlterations(alteration)) {
                    sb.append(alleleSummary(alteration));
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
                        sb.append(unknownOncogenicSummary(gene));
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                        sb.append("As of " + sdf.format(lastEdit) + ", no functional data about this alteration was available.");
                    }
                } else {
                    sb.append(unknownOncogenicSummary(gene));
                }
            } else {
                sb.append(unknownOncogenicSummary(gene));
            }
        } else {
            if (AlterationUtils.isGeneralAlterations(queryAlteration, false)) {
                if (AlterationUtils.isGeneralAlterations(queryAlteration, true)) {
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

            Oncogenicity oncogenic = null;
            for (Alteration a : alterations) {
                List<Evidence> oncogenicEvidences = EvidenceUtils.getEvidence(Collections.singletonList(a), Collections.singleton(EvidenceType.ONCOGENIC), null);
                if (oncogenicEvidences != null && oncogenicEvidences.size() > 0) {
                    Evidence evidence = oncogenicEvidences.iterator().next();
                    if (evidence != null ) {
                        oncogenic = Oncogenicity.getByEvidence(evidence);
                        break;
                    }
                }
            }
            if (oncogenic != null && !oncogenic.equals(Oncogenicity.INCONCLUSIVE)) {
                if (appendThe) {
                    sb.append("The ");
                }
                sb.append(altName);

                if (isPlural) {
                    sb.append(" are");
                } else {
                    sb.append(" is");
                }

                if (oncogenic.equals(Oncogenicity.LIKELY_NEUTRAL)) {
                    sb.append(" likely neutral.");
                } else {
                    if (oncogenic.equals(Oncogenicity.LIKELY)) {
                        sb.append(" likely");
                    } else if (oncogenic.equals(Oncogenicity.YES)) {
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

        String altStr = getGeneMutationNameInVariantSummary(alteration.getGene(), alteration.getAlteration());

        sb.append("The " + altStr + " has not been functionally or clinically validated.");

        Set<Alteration> alleles = new HashSet<>(AlterationUtils.getAlleleAndRelevantAlterations(alteration));

        Map<String, Object> map = geAlterationsWithHighestOncogenicity(new HashSet<>(alleles));
        Oncogenicity highestOncogenicity = (Oncogenicity) map.get("oncogenicity");
        Set<Alteration> highestAlts = (Set<Alteration>) map.get("alterations");

        if (highestOncogenicity != null && (highestOncogenicity.equals(Oncogenicity.YES) || highestOncogenicity.equals(Oncogenicity.LIKELY))) {

            sb.append(" However, ");
            sb.append(alteration.getGene().getHugoSymbol() + " " + allelesToStr(highestAlts));
            sb.append((highestAlts.size() > 1 ? " are" : " is"));
            if (highestOncogenicity.equals(Oncogenicity.YES)) {
                sb.append(" known to be " + highestOncogenicity.getOncogenic().toLowerCase());
            } else {
                sb.append(" " + highestOncogenicity.getOncogenic().toLowerCase());
            }
            sb.append(", and therefore " + alteration.getGene().getHugoSymbol() + " " + alteration.getAlteration() + " is considered likely oncogenic.");
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

                Oncogenicity oncogenicity = Oncogenicity.getByEffect(oncoStr);
                if (!oncoCate.containsKey(oncogenicity))
                    oncoCate.put(oncogenicity, new HashSet<Alteration>());

                oncoCate.get(oncogenicity).add(alt);
            }
        }

        Oncogenicity oncogenicity = MainUtils.findHighestOncogenicity(oncoCate.keySet());
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
        Map<Gene, Set<String>> mapGeneVariants = new HashedMap();
        for (Alteration alteration : alterations) {
            Gene gene = alteration.getGene();
            Set<String> variants = mapGeneVariants.get(gene);
            if (variants == null) {
                variants = new HashSet<>();
                mapGeneVariants.put(gene, variants);
            }
            variants.add(alteration.getName());
        }

        List<String> list = new ArrayList<>();
        for (Map.Entry<Gene, Set<String>> entry : mapGeneVariants.entrySet()) {
            for (String variant : entry.getValue()) {
                list.add(getGeneMutationNameInVariantSummary(alterations.iterator().next().getGene(), variant));
            }
        }

        String ret = MainUtils.listToString(list, " or ");

        return ret;
    }

    private static Boolean appendThe(String queryAlteration) {
        Boolean appendThe = true;

        if (queryAlteration.toLowerCase().contains("deletion")
            || queryAlteration.toLowerCase().contains("amplification")
            || queryAlteration.toLowerCase().contains("fusions")) {
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
        if (AlterationUtils.isGeneralAlterations(queryAlteration, true)) {
            sb.append(gene.getHugoSymbol() + " " + queryAlteration.toLowerCase());
        } else if (StringUtils.containsIgnoreCase(queryAlteration, "fusion")) {
            queryAlteration = queryAlteration.replace("Fusion", "fusion");
            sb.append(queryAlteration);
        } else if (AlterationUtils.isGeneralAlterations(queryAlteration, false)
            || (alteration.getConsequence() != null
            && (alteration.getConsequence().getTerm().equals("inframe_deletion")
            || alteration.getConsequence().getTerm().equals("inframe_insertion")))
            || StringUtils.containsIgnoreCase(queryAlteration, "indel")
            || StringUtils.containsIgnoreCase(queryAlteration, "dup")
            || StringUtils.containsIgnoreCase(queryAlteration, "del")
            || StringUtils.containsIgnoreCase(queryAlteration, "ins")
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
                queryAlteration = gene.getHugoSymbol() + " fusion";
            }
            queryAlteration = queryAlteration.replace("Fusion", "fusion");
            sb.append(queryAlteration + " positive");
        } else {
            sb.append(gene.getHugoSymbol() + " ");
            if (AlterationUtils.isGeneralAlterations(queryAlteration, true)) {
                sb.append(queryAlteration.toLowerCase());
            } else if (AlterationUtils.isGeneralAlterations(queryAlteration, false)
                || (alteration.getConsequence() != null
                && (alteration.getConsequence().getTerm().equals("inframe_deletion")
                || alteration.getConsequence().getTerm().equals("inframe_insertion")))
                || StringUtils.containsIgnoreCase(queryAlteration, "indel")
                || StringUtils.containsIgnoreCase(queryAlteration, "dup")
                || StringUtils.containsIgnoreCase(queryAlteration, "del")
                || StringUtils.containsIgnoreCase(queryAlteration, "ins")
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

        // In case of miss typed
        summary = summary.replace("[[gene]] [[mutation]] [[mutation]]", alterationName);
        summary = summary.replace("[[gene]] [[mutation]] [[mutant]]", altName);
        summary = summary.replace("[[gene]]", gene.getHugoSymbol());

        summary = summary.replace("[[mutation]] [[mutant]]", altName);
        summary = summary.replace("[[mutation]] [[[mutation]]]", alterationName);
        // In case of miss typed
        summary = summary.replace("[[mutation]] [[mutation]]", alterationName);
        summary = summary.replace("[[mutation]]", queryAlteration);
        summary = summary.replace("[[tumorType]]", queryTumorType);
        summary = summary.replace("[[tumor type]]", queryTumorType);
        summary = summary.replace("[[fusion name]]", altName);
        summary = summary.replace("[[fusion name]]", altName);
        return summary;
    }

    private static String getTumorTypeSummaryFromPickedTreatment(Gene gene, Alteration alteration, Set<OncoTreeType> relevantTumorTypes) {
        String tumorTypeSummary = null;
        List<Evidence> evidences = EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), relevantTumorTypes, null);
        Evidence pickedTreatment = pickSpecialGeneTreatmentEvidence(gene, EvidenceUtils.getEvidence(Collections.singletonList(alteration), MainUtils.getTreatmentEvidenceTypes(), relevantTumorTypes, null));

        if (pickedTreatment != null && evidences != null) {
            for (Evidence evidence : evidences) {
                if (evidence.getAlterations().equals(pickedTreatment.getAlterations())) {
                    tumorTypeSummary = getTumorTypeSummaryFromEvidences(Collections.singletonList(evidence));
                    if (tumorTypeSummary != null) {
                        break;
                    }
                }
            }
        }

        if (tumorTypeSummary == null) {
            evidences = EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), Collections.singleton(TumorTypeUtils.getMappedSpecialTumor(SpecialTumorType.OTHER_TUMOR_TYPES)), null);
            for (Evidence evidence : evidences) {
                tumorTypeSummary = getTumorTypeSummaryFromEvidences(Collections.singletonList(evidence));
                if (tumorTypeSummary != null) {
                    break;
                }
            }
        }
        return tumorTypeSummary;
    }

    private static String getKITtumorTypeSummaries(String queryAlteration, List<Alteration> alterations, String queryTumorType, Set<OncoTreeType> relevantTumorTypes) {
        Gene gene = GeneUtils.getGeneByHugoSymbol("KIT");
        String tumorTypeSummary = null;
        Evidence pickedTreatment = null;

        // Get all tumor type summary evidences specifically for the alteration
        if (tumorTypeSummary == null) {
            Alteration alteration = AlterationUtils.findAlteration(gene, queryAlteration);
            if (alteration != null) {
                tumorTypeSummary = getTumorTypeSummaryFromPickedTreatment(gene, alteration, relevantTumorTypes);
            }
        }

        // Get all tumor type summary evidences for the alternate alleles
        if (tumorTypeSummary == null) {
            Alteration alteration = AlterationUtils.findAlteration(gene, queryAlteration);
            if (alteration == null) {
                alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), queryAlteration, null, null, null, null);
                AlterationUtils.annotateAlteration(alteration, queryAlteration);
            }
            if (alteration.getConsequence() != null) {
                if (alteration.getConsequence().getTerm().equals("missense_variant")) {
                    List<Alteration> alternateAlleles = AlterationUtils.getAlleleAndRelevantAlterations(alteration);

                    // Send all allele tumor types treatment to pick up the unique one.
                    pickedTreatment = pickSpecialGeneTreatmentEvidence(gene, EvidenceUtils.getEvidence(alternateAlleles, MainUtils.getTreatmentEvidenceTypes(), relevantTumorTypes, null));
                    if (pickedTreatment != null) {
                        tumorTypeSummary = getTumorTypeSummaryFromPickedTreatment(gene, (Alteration) CollectionUtils.intersection(alternateAlleles, new ArrayList<>(pickedTreatment.getAlterations())).iterator().next(), relevantTumorTypes);
                        if (tumorTypeSummary != null) {
                            // Only keep the first sentence for alternate allele
                            tumorTypeSummary = tumorTypeSummary.split("\\.")[0] + ".";
                        }
                    } else {
                        // If all alternate alleles don't have any treatment, check whether they have tumor type summaries.
                        for (Alteration allele : alternateAlleles) {
                            tumorTypeSummary = getTumorTypeSummaryFromPickedTreatment(gene, allele, relevantTumorTypes);
                            if (tumorTypeSummary != null) {
                                // Only keep the first sentence for alternate allele
                                tumorTypeSummary = tumorTypeSummary.split("\\.")[0] + ".";
                                break;
                            }
                        }
                    }
                } else if (alteration.getConsequence().getTerm().equals("synonymous_variant")) {
                    // No summary for synonymous variant
                    return "";
                }
            }
        }

        // Get all tumor type summary evidence for relevant alterations
        if (tumorTypeSummary == null) {
            // Base on the priority of relevant alterations
            for (Alteration alteration : alterations) {
                tumorTypeSummary = getTumorTypeSummaryFromPickedTreatment(gene, alteration, relevantTumorTypes);
                if (tumorTypeSummary != null) {
                    break;
                }
            }
        }
        return tumorTypeSummary;
    }

    private static Evidence pickSpecialGeneTreatmentEvidence(Gene gene, List<Evidence> treatments) {
        Evidence pickedTreatment = null;

        if (gene != null && treatments != null) {
            List<String> TREATMENTS = null;
            String hugoSymbol = gene.getHugoSymbol();
            if (hugoSymbol.equals("KIT")) {
                TREATMENTS = Arrays.asList("imatinib", "sunitinib", "regorafenib", "sorafenib", "nilotinib", "dasatinib");
            }

            Integer index = 1000;

            for (Evidence treatment : treatments) {
                String treatmentName = TreatmentUtils.getTreatmentName(treatment.getTreatments(), false);
                if (treatmentName != null) {
                    Integer _index = TREATMENTS.indexOf(treatmentName.toLowerCase());
                    if (_index != -1 && _index < index) {
                        index = _index;
                        pickedTreatment = treatment;
                    }
                }
            }
        }

        return pickedTreatment;
    }

    private static String convertTumorTypeNameInSummary(String summary) {
        if (summary != null) {
            String[] specialWords = {"Wilms"};
            List<String> specialWordsList = Arrays.asList(specialWords);
            String lowerCaseStr = summary.toLowerCase();

            StringBuilder sb = new StringBuilder(lowerCaseStr);

            for (String item : specialWordsList) {
                Integer startIndex = summary.indexOf(item);
                if (startIndex != -1) {
                    sb.replace(startIndex, startIndex + item.length(), item);
                }
            }

            // Find all uppercased string
            Pattern p = Pattern.compile("(\\b[A-Z]+\\b)");
            Matcher m = p.matcher(summary);

            while (m.find()) {
                sb.replace(m.start(), m.end(), m.group(1));
            }

            summary = sb.toString();
        }
        return summary;
    }
}
