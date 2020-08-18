package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

;

/**
 * Created by Hongxin on 8/10/15.
 */
public class SummaryUtils {
    public static final String TERT_PROMOTER_MUTATION_SUMMARY = "Select hotspot mutations in the TERT promoter have been shown to be oncogenic.";
    public static final String TERT_PROMOTER_NO_THERAPY_TUMOR_TYPE_SUMMARY = "There are no FDA-approved or NCCN-compendium listed treatments specifically for patients with TERT promoter mutations in [[tumor type]].";
    public static final String ONCOGENIC_MUTATIONS_DEFAULT_SUMMARY = "Oncogenic Mutations includes all variants annotated as oncogenic and likely oncogenic.";

    public static Map<String, Object> tumorTypeSummary(EvidenceType evidenceType, Gene gene, Query query, Alteration exactMatchedAlt, List<Alteration> alterations, List<TumorType> relevantTumorTypes) {
        Map<String, Object> tumorTypeSummary = newTumorTypeSummary();
        String queryTumorType = query.getTumorType();
        String key = query.getQueryId();
        queryTumorType = convertTumorTypeNameInSummary(queryTumorType);

        if (queryTumorType != null) {
            queryTumorType = queryTumorType.trim();
            if (queryTumorType.endsWith(" tumor")) {
                queryTumorType = queryTumorType.substring(0, queryTumorType.lastIndexOf(" tumor")) + " tumors";
            }
        }

        if (gene == null || alterations == null || relevantTumorTypes == null || queryTumorType == null) {
            Map<String, Object> map = newTumorTypeSummary();
            return map;
        }

        query.setTumorType(queryTumorType);
        tumorTypeSummary = getTumorTypeSummarySubFunc(evidenceType, gene, query, exactMatchedAlt, alterations, relevantTumorTypes);

        return tumorTypeSummary;
    }

    private static Map<String, Object> getTumorTypeSummarySubFunc(EvidenceType evidenceType, Gene gene, Query query, Alteration exactMatchedAlt, List<Alteration> relevantAlterations, List<TumorType> relevantTumorTypes) {
        Map<String, Object> tumorTypeSummary = newTumorTypeSummary();
        Alteration alteration = null;

        //        if (gene.getHugoSymbol().equals("KIT")) {
//            tumorTypeSummary = getKITtumorTypeSummaries(queryAlteration, alterations, queryTumorType, relevantTumorTypes);
//        } else {
        // Get all tumor type summary evidences specifically for the alteration

        if (exactMatchedAlt != null) {
            alteration = exactMatchedAlt;
        } else {
            alteration = AlterationUtils.getAlteration(query.getHugoSymbol(), query.getAlteration(), AlterationType.getByName(query.getAlterationType()), query.getConsequence(), query.getProteinStart(), query.getProteinEnd());
            AlterationUtils.annotateAlteration(alteration, query.getAlteration());
        }

        if (alteration.getConsequence().getTerm().equals("synonymous_variant")) {
            // No summary for synonymous variant
            return tumorTypeSummary;
        }

        tumorTypeSummary = null;

        List<Alteration> alternativeAlleles = new ArrayList<>();
        alternativeAlleles.add(alteration);
        alternativeAlleles.addAll(AlterationUtils.getPositionedAlterations(alteration));

        alternativeAlleles = ListUtils.intersection(alternativeAlleles, relevantAlterations);

        // Get all tumor type summary evidences for the exact alteration + alternative alleles
        // Tumor type has high priority. Get relevant tumor type summary across all alternative alleles, then look for other tumor types summary
        if (tumorTypeSummary == null) {
            for (TumorType tumorType : relevantTumorTypes) {
                for (Alteration allele : alternativeAlleles) {
                    tumorTypeSummary = getRelevantTumorTypeSummaryByAlt(evidenceType, allele, Collections.singleton(tumorType));
                    if (tumorTypeSummary != null) {
                        break;
                    }
                }
                if (tumorTypeSummary != null) {
                    break;
                }
            }

            if (tumorTypeSummary == null) {
                for (Alteration allele : alternativeAlleles) {
                    tumorTypeSummary = getOtherTumorTypeSummaryByAlt(evidenceType, allele, new HashSet<>(relevantTumorTypes));
                    if (tumorTypeSummary != null) {
                        break;
                    }
                }
            }
        }

        // Get all tumor type summary evidence for relevant alterations.
        // Alteration has high priority. Get relevant tumor type summary, then other tumor type summary, then next relevant alteration
        if (tumorTypeSummary == null) {
            // Sort all tumor type summaries, the more specific tumor type summary will be picked.
            // Deal with KIT, give Exon annotation highers priority
            relevantAlterations.removeAll(alternativeAlleles);

            if (gene.getHugoSymbol().equals("KIT")) {
                Collections.sort(relevantAlterations, new Comparator<Alteration>() {
                    public int compare(Alteration x, Alteration y) {
                        Integer result = 0;
                        // TODO: need more comprehensive method to determine the order.
                        String nameX = (x.getName() != null ? x.getName() : x.getAlteration()).toLowerCase();
                        String nameY = (y.getName() != null ? y.getName() : y.getAlteration()).toLowerCase();
                        if (nameX.contains("exon")) {
                            if (nameY.contains("exon")) {
                                result = 0;
                            } else {
                                result = -1;
                            }
                        } else {
                            if (nameY.contains("exon")) {
                                result = 1;
                            } else {
                                result = 0;
                            }
                        }
                        return result;
                    }
                });
            }

            // Base on the priority of relevant alterations
            for (Alteration alt : relevantAlterations) {
                tumorTypeSummary = getRelevantTumorTypeSummaryByAlt(evidenceType, alt, new HashSet<>(relevantTumorTypes));
                if (tumorTypeSummary != null) {
                    break;
                }

                // Get Other Tumor Types summary
                tumorTypeSummary = getOtherTumorTypeSummaryByAlt(evidenceType, alt, new HashSet<>(relevantTumorTypes));
                if (tumorTypeSummary != null) {
                    break;
                }
            }
        }
//        }

        if (tumorTypeSummary == null) {
            tumorTypeSummary = newTumorTypeSummary();
            String tmpSummary = "";
            if (evidenceType.equals(EvidenceType.TUMOR_TYPE_SUMMARY)) {
                if (query.getAlteration().toLowerCase().contains("truncating mutation")) {
                    tmpSummary = "There are no FDA-approved or NCCN-compendium listed treatments specifically for patients with [[tumor type]] harboring " + getGeneArticle(gene) + " [[gene]] truncating mutation.";
                } else if (gene.getHugoSymbol().equals("TERT") && query.getAlteration().trim().equalsIgnoreCase("promoter")) {
                    tmpSummary = TERT_PROMOTER_NO_THERAPY_TUMOR_TYPE_SUMMARY;
                } else {
                    tmpSummary = "There are no FDA-approved or NCCN-compendium listed treatments specifically for patients with [[variant]].";
                }
            }
            tumorTypeSummary.put("summary", tmpSummary);
        }

        tumorTypeSummary.put("summary", replaceSpecialCharacterInTumorTypeSummary((String) tumorTypeSummary.get("summary"), gene, query.getAlteration(), query.getTumorType()));

        return tumorTypeSummary;
    }

    private static Map<String, Object> getRelevantTumorTypeSummaryByAlt(EvidenceType evidenceType, Alteration alteration, Set<TumorType> relevantTumorTypes) {
        return getTumorTypeSummaryFromEvidences(EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(evidenceType), relevantTumorTypes, null));
    }

    private static Map<String, Object> getOtherTumorTypeSummaryByAlt(EvidenceType evidenceType, Alteration alteration, Set<TumorType> relevantTumorTypes) {
        // Check other tumor types summary based on tumor form
        List<SpecialTumorType> specialTumorTypes = new ArrayList<>();
        TumorForm tumorForm = TumorTypeUtils.checkTumorForm(relevantTumorTypes);
        if (tumorForm != null) {
            specialTumorTypes.add(tumorForm.equals(TumorForm.SOLID) ?
                SpecialTumorType.OTHER_SOLID_TUMOR_TYPES : SpecialTumorType.OTHER_LIQUID_TUMOR_TYPES);
        }

        specialTumorTypes.add(SpecialTumorType.OTHER_TUMOR_TYPES);

        for (SpecialTumorType specialTumorType : specialTumorTypes) {

            List<Evidence> evidences = EvidenceUtils.getEvidence(
                Collections.singletonList(alteration),
                Collections.singleton(evidenceType),
                Collections.singleton(TumorTypeUtils.getMappedSpecialTumor(specialTumorType)), null);
            if (evidences.size() > 0) {
                return getTumorTypeSummaryFromEvidences(evidences);
            }
        }
        return null;
    }

    public static String unknownOncogenicSummary(Gene gene, String queryAlteration) {
        String str = gene == null ? "variant" : getGeneMutationNameInVariantSummary(gene, queryAlteration);
        return "The biologic significance of the " + str + " is unknown.";
    }

    public static String synonymousSummary() {
        return "This is a synonymous mutation and is not annotated by OncoKB.";
    }

    public static String variantSummary(Gene gene, Alteration exactMatchAlteration, List<Alteration> alterations, Query query) {
        if (!StringUtils.isEmpty(query.getAlteration()) && query.getAlteration().equalsIgnoreCase(InferredMutation.ONCOGENIC_MUTATIONS.getVariant())) {
            return ONCOGENIC_MUTATIONS_DEFAULT_SUMMARY;
        }
        return getOncogenicSummarySubFunc(gene, exactMatchAlteration, alterations, query);
    }

    private static String getOncogenicSummarySubFunc(Gene gene, Alteration exactMatchAlteration, List<Alteration> alterations, Query query) {
        StringBuilder sb = new StringBuilder();

        Oncogenicity oncogenic = null;

        Boolean isHotspot = false;
        String queryAlteration = query.getAlteration();
        Alteration alteration = null;

        if (AlterationUtils.isGeneralAlterations(queryAlteration, true)) {
            queryAlteration = queryAlteration.substring(0, 1).toUpperCase() + queryAlteration.substring(1);
        }

        // if the gene is Other Biomarker, return the mutation effect description for alteration instead
        if (gene.getHugoSymbol().equals(SpecialStrings.OTHERBIOMARKERS)) {
            if (exactMatchAlteration != null) {
                List<Evidence> evidences = EvidenceUtils.getEvidence(Collections.singletonList(exactMatchAlteration), Collections.singleton(EvidenceType.MUTATION_EFFECT), null);

                // Technically the list should only contain no more than one record.
                for (Evidence evidence : evidences) {
                    if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(evidence.getDescription())) {
                        return evidence.getDescription();
                    }
                }
                return "";
            } else {
                return "";
            }
        }

        // Give predefined TERT promoter summary
        if(gene.getHugoSymbol().equals("TERT") && query.getAlteration().trim().equalsIgnoreCase("promoter")) {
            return TERT_PROMOTER_MUTATION_SUMMARY;
        }

        if (exactMatchAlteration != null) {
            // Synonymous Summary
            if (exactMatchAlteration.getConsequence().getTerm().equals("synonymous_variant")) {
                return synonymousSummary();
            }

            // Find oncogenic info from exact matched variant
            List<Evidence> oncogenicEvidences = EvidenceUtils.getEvidence(Collections.singletonList(exactMatchAlteration), Collections.singleton(EvidenceType.ONCOGENIC), null);
            if (oncogenicEvidences != null && oncogenicEvidences.size() > 0) {
                Set<Oncogenicity> oncogenicities = new HashSet<>();
                for (Evidence evidence : oncogenicEvidences) {
                    Oncogenicity tmpOncogenic = Oncogenicity.getByEvidence(evidence);
                    if (tmpOncogenic != null) {
                        oncogenicities.add(tmpOncogenic);
                    }
                }
                oncogenic = MainUtils.findHighestOncogenicity(oncogenicities);
            }
            alteration = exactMatchAlteration;
        } else {
            alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getAlteration(),
                AlterationType.getByName(query.getAlterationType()), query.getConsequence(), query.getProteinStart(), query.getProteinEnd());
            AlterationUtils.annotateAlteration(alteration, queryAlteration);
        }

        isHotspot = HotspotUtils.isHotspot(alteration);

        if (oncogenic == null || oncogenic.equals(Oncogenicity.UNKNOWN)) {
            // Get oncogenic summary from alternative alleles
            List<Alteration> alternativeAlleles = AlterationUtils.getAlleleAlterations(alteration);
            List<Alteration> alternativeAllelesWithoutVUS = AlterationUtils.excludeVUS(gene, alternativeAlleles);

            // VUS alternative alleles are not accounted into oncogenic summary calculation
            if (alternativeAllelesWithoutVUS.size() > 0) {
                sb.append(alleleSummary(alteration));
                return sb.toString();
            }

            // Get oncogenic info from rest of relevant alterations except AA
            alterations.removeAll(alternativeAlleles);
            Set<Oncogenicity> oncogenicities = new HashSet<>();
            for (Alteration a : alterations) {
                List<Evidence> oncogenicEvidences = EvidenceUtils.getEvidence(Collections.singletonList(a), Collections.singleton(EvidenceType.ONCOGENIC), null);
                if (oncogenicEvidences != null && oncogenicEvidences.size() > 0) {
                    Evidence evidence = oncogenicEvidences.iterator().next();
                    if (evidence != null) {
                        oncogenicities.add(Oncogenicity.getByEvidence(evidence));
                    }
                }
            }

            // Rank oncogenicities from relevant variants
            Oncogenicity tmpOncogenicity = MainUtils.findHighestOncogenicity(oncogenicities);
            if (tmpOncogenicity != null) {
                oncogenic = tmpOncogenicity;
            }
        }

        if (query.getAlteration().toLowerCase().contains("truncating mutation")) {
            if (gene.getOncogene()) {
                return gene.getHugoSymbol() + " is considered an oncogene and truncating mutations in oncogenes are typically nonfunctional.";
            } else if (!gene.getTSG() && oncogenic == null) {
                return "It is unknown whether a truncating mutation in " + gene.getHugoSymbol() + " is oncogenic.";
            }
        }

        if (oncogenic != null) {
            return getOncogenicSummaryFromOncogenicity(oncogenic, alteration, query, isHotspot);
        }

        if (alteration != null && MainUtils.isVUS(alteration)) {
            return vusAndHotspotSummary(alteration, query, isHotspot);
        }

        if (isHotspot) {
            return hotspotSummary(alteration, query, false);
        }

        return unknownOncogenicSummary(gene, query.getAlteration());
    }

    private static String getVUSOncogenicSummary(Alteration alteration) {
        List<Evidence> evidences = EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.VUS), null);
        StringBuilder sb = new StringBuilder();
        sb.append("The biologic significance of the ");
        sb.append(getGeneMutationNameInVariantSummary(alteration.getGene(), alteration.getAlteration()));
        sb.append(" is unknown");

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
        if (lastEdit != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            sb.append(" (last reviewed ");
            sb.append(sdf.format(lastEdit));
            sb.append(")");
        }
        sb.append(".");
        return StringUtils.capitalize(sb.toString());
    }

    private static String getOncogenicSummaryFromOncogenicity(Oncogenicity oncogenicity, Alteration alteration, Query query, Boolean isHotspot) {
        StringBuilder sb = new StringBuilder();
        String queryAlteration = query.getAlteration();
        String altName = getGeneMutationNameInVariantSummary(alteration.getGene(), queryAlteration);
        Boolean appendThe = appendThe(queryAlteration);
        Boolean isPlural = false;

        if (isHotspot == null) {
            isHotspot = false;
        }
        if (queryAlteration.toLowerCase().contains("fusions") || queryAlteration.toLowerCase().endsWith("mutations")) {
            isPlural = true;
        }
        if (oncogenicity != null) {
            if (query.getAlteration().toLowerCase().contains("truncating mutation") && query.getSvType() != null) {
                return "This " + alteration.getGene().getHugoSymbol() + " " + query.getSvType().name().toLowerCase() + " may be a truncating alteration and is " + getOncogenicSubTextFromOncogenicity(oncogenicity) + ".";
            }

            if (oncogenicity.equals(Oncogenicity.INCONCLUSIVE)) {
                if (isHotspot) {
                    return inconclusiveHotSpotSummary(alteration, query);
                } else {
                    return inconclusiveSummary(alteration.getGene(), queryAlteration);
                }
            }
            if (appendThe) {
                sb.append("The ");
            }
            sb.append(altName);

            if (isPlural) {
                sb.append(" are");
            } else {
                sb.append(" is");
            }

            if (oncogenicity.equals(Oncogenicity.LIKELY_NEUTRAL)) {
                sb.append(" likely neutral.");
            } else {
                if (oncogenicity.equals(Oncogenicity.LIKELY)) {
                    sb.append(" likely");
                } else if (oncogenicity.equals(Oncogenicity.YES)) {
                    sb.append(" known to be");
                }

                sb.append(" oncogenic.");
            }
        }
        return sb.toString();
    }

    private static String getOncogenicSubTextFromOncogenicity(Oncogenicity oncogenicity) {
        if (oncogenicity == null)
            return "";
        StringBuilder sb = new StringBuilder();
        if (oncogenicity.equals(Oncogenicity.LIKELY_NEUTRAL)) {
            sb.append("considered likely neutral.");
        } else {
            if (oncogenicity.equals(Oncogenicity.LIKELY)) {
                sb.append("considered likely");
            } else if (oncogenicity.equals(Oncogenicity.YES)) {
                sb.append("known to be");
            } else if (oncogenicity.equals(Oncogenicity.PREDICTED)) {
                sb.append("predicted to be");
            } else {
                // For Unknown
                return "";
            }
            sb.append(" oncogenic");
        }
        return sb.toString();
    }

    public static String geneSummary(Gene gene) {
        if (gene != null && gene.getHugoSymbol().equals(SpecialStrings.OTHERBIOMARKERS)) {
            return "";
        }
        Set<Evidence> geneSummaryEvs = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.GENE_SUMMARY));
        String summary = "";
        if (!geneSummaryEvs.isEmpty()) {
            Evidence ev = geneSummaryEvs.iterator().next();
            if (ev != null) {
                summary = ev.getDescription();
            }
        }

        if (summary == null) {
            summary = "";
        }
        summary = summary.trim();
        summary = summary.endsWith(".") ? summary : summary + ".";
        return summary;
    }

    public static String alleleSummary(Alteration alteration) {
        StringBuilder sb = new StringBuilder();

        String altStr = getGeneMutationNameInVariantSummary(alteration.getGene(), alteration.getAlteration());

        sb.append("The " + altStr + " has not been functionally or clinically validated.");

        Set<Alteration> alleles = new HashSet<>(AlterationUtils.getAlleleAlterations(alteration));

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

    public static String inconclusiveSummary(Gene gene, String queryAlteration) {
        StringBuilder sb = new StringBuilder();
        sb.append("There is conflicting and/or weak data describing the biological significance of the ");
        sb.append(getGeneMutationNameInVariantSummary(gene, queryAlteration));
        sb.append(".");
        return sb.toString();
    }

    public static String inconclusiveHotSpotSummary(Alteration alteration, Query query) {
        StringBuilder sb = new StringBuilder();
        sb.append(inconclusiveSummary(alteration.getGene(), query.getAlteration()));
        sb.append(" However, ");
        String hotspotSummary = hotspotSummary(alteration, query, true);
        sb.append(StringUtils.uncapitalize(hotspotSummary));
        return sb.toString();
    }

    public static String hotspotSummary(Alteration alteration, Query query, Boolean usePronoun) {
        StringBuilder sb = new StringBuilder();
        if (usePronoun == null) {
            usePronoun = false;
        }
        if (usePronoun) {
            sb.append("It");
        } else {
            sb.append("The " + getGeneMutationNameInVariantSummary(alteration.getGene(), query.getAlteration()));
        }
        sb.append(" has been identified as a statistically significant hotspot and is predicted to be oncogenic");
        sb.append(hotspotLink(query));
        sb.append(".");
        return sb.toString();
    }

    private static String hotspotLink(Query query) {
        StringBuilder sb = new StringBuilder();
        if (query.getType() != null && query.getType().equals("web")) {
            String cancerHotspotsLink = "";
            try {
                cancerHotspotsLink = PropertiesUtils.getProperties("cancerhotspots.website.link");
                if (com.mysql.jdbc.StringUtils.isNullOrEmpty(cancerHotspotsLink))
                    throw new Exception();
            } catch (Exception e) {
                cancerHotspotsLink = "http://cancerhotspots.org";
            }
            cancerHotspotsLink = cancerHotspotsLink.trim();
            if (!cancerHotspotsLink.isEmpty()) {
                sb.append(" (");
                sb.append(cancerHotspotsLink);
                sb.append(")");
            }
        }
        return sb.toString();
    }

    private static String vusAndHotspotSummary(Alteration alteration, Query query, Boolean isHotspot) {
        StringBuilder sb = new StringBuilder();
        sb.append(getVUSOncogenicSummary(alteration));

        if (isHotspot) {
            sb.append(" However, it has been identified as a statistically significant hotspot and is predicted to be oncogenic");
            sb.append(hotspotLink(query));
            sb.append(".");
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
        Set<Alteration> specialAlts = new HashSet<>();

        for (Alteration alteration : alterations) {
            if (alteration.getProteinStart() != null && alteration.getProteinEnd() != null &&
                alteration.getProteinStart().equals(alteration.getProteinEnd())) {
                if (!locationBasedAlts.containsKey(alteration.getProteinStart()))
                    locationBasedAlts.put(alteration.getProteinStart(), new HashSet<Alteration>());
                locationBasedAlts.get(alteration.getProteinStart()).add(alteration);
            } else {
                specialAlts.add(alteration);
            }
        }

        for (Map.Entry entry : locationBasedAlts.entrySet()) {
            alterationNames.add(alleleNamesStr((Set<Alteration>) entry.getValue()));
        }

        for (Alteration alteration : specialAlts) {
            alterationNames.add(alleleNamesStr(Collections.singleton(alteration)));
        }

        return MainUtils.listToString(alterationNames);
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

    private static Boolean appendThe(String queryAlteration) {
        Boolean appendThe = true;

        if (queryAlteration.toLowerCase().contains("deletion")
            || queryAlteration.toLowerCase().contains("amplification")
            || queryAlteration.toLowerCase().matches("gain")
            || queryAlteration.toLowerCase().matches("loss")
            || queryAlteration.toLowerCase().contains("fusions")) {
            appendThe = false;
        }
        return appendThe;
    }

    private static Map<String, Object> getTumorTypeSummaryFromEvidences(List<Evidence> evidences) {
        Map<String, Object> summary = null;

        if (evidences != null && evidences.size() > 0) {
            evidences = EvidenceUtils.sortTumorTypeEvidenceBasedNumOfAlts(evidences, false);

            Evidence ev = evidences.get(0);
            String tumorTypeSummary = ev.getDescription();
            if (tumorTypeSummary != null) {
                summary = newTumorTypeSummary();
                summary.put("summary", tumorTypeSummary.trim());
                summary.put("lastEdit", ev.getLastEdit());
            }
        }
        return summary;
    }

    public static String getGeneMutationNameInVariantSummary(Gene gene, String queryAlteration) {
        StringBuilder sb = new StringBuilder();
        if (queryAlteration == null) {
            return "";
        } else {
            queryAlteration = queryAlteration.trim();
        }
        Alteration alteration = AlterationUtils.findAlteration(gene, queryAlteration);
        if (alteration == null) {
            alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), queryAlteration, null, null, null, null);
            AlterationUtils.annotateAlteration(alteration, queryAlteration);
        }
        if (AlterationUtils.isGeneralAlterations(queryAlteration, true)) {
            sb.append(gene.getHugoSymbol() + " " + queryAlteration.toLowerCase());
        } else if (StringUtils.equalsIgnoreCase(queryAlteration, "gain")) {
            queryAlteration = "amplification (gain)";
            sb.append(gene.getHugoSymbol() + " " + queryAlteration);
        } else if (StringUtils.equalsIgnoreCase(queryAlteration, "loss")) {
            queryAlteration = "deletion (loss)";
            sb.append(gene.getHugoSymbol() + " " + queryAlteration);
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
            || StringUtils.containsIgnoreCase(queryAlteration, "splice")
            || MainUtils.isEGFRTruncatingVariants(queryAlteration)
            ) {
            if (NamingUtils.hasAbbreviation(queryAlteration)) {
                sb.append(gene.getHugoSymbol() + " " + NamingUtils.getFullName(queryAlteration) + " (" + queryAlteration + ")");
            } else {
                sb.append(gene.getHugoSymbol() + " " + queryAlteration);
            }
            if (!queryAlteration.endsWith("alteration")) {
                sb.append(" alteration");
            }
        } else {
            if (queryAlteration.contains(gene.getHugoSymbol())) {
                sb.append(queryAlteration);
            } else if (NamingUtils.hasAbbreviation(queryAlteration)) {
                sb.append(gene.getHugoSymbol() + " " + NamingUtils.getFullName(queryAlteration) + " (" + queryAlteration + ") alteration");
            } else {
                sb.append(gene.getHugoSymbol() + " " + queryAlteration);
            }
            String finalStr = sb.toString();
            if (!finalStr.endsWith("mutation")
                && !finalStr.endsWith("mutations")
                && !finalStr.endsWith("alteration")
                && !finalStr.endsWith("fusion")
                && !finalStr.endsWith("deletion")
                && !finalStr.endsWith("amplification")
                ) {
                sb.append(" mutation");
            }
        }
        return sb.toString();
    }

    public static String getGeneMutationNameInTumorTypeSummary(Gene gene, String queryAlteration) {
        StringBuilder sb = new StringBuilder();
        if (queryAlteration == null) {
            return "";
        } else {
            queryAlteration = queryAlteration.trim();
        }
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
        } else if (StringUtils.equalsIgnoreCase(queryAlteration, "gain")
            || StringUtils.equalsIgnoreCase(queryAlteration, "amplification")) {
            queryAlteration = gene.getHugoSymbol() + "-amplified";
            sb.append(queryAlteration);
        } else {
            if (!queryAlteration.contains(gene.getHugoSymbol())) {
                sb.append(gene.getHugoSymbol() + " ");
            }
            if (AlterationUtils.isGeneralAlterations(queryAlteration, true)) {
                sb.append(queryAlteration.toLowerCase());
            } else if (StringUtils.equalsIgnoreCase(queryAlteration, "loss")) {
                queryAlteration = "deletion";
                sb.append(queryAlteration);
            } else if (AlterationUtils.isGeneralAlterations(queryAlteration, false)
                || (alteration.getConsequence() != null
                && (alteration.getConsequence().getTerm().equals("inframe_deletion")
                || alteration.getConsequence().getTerm().equals("inframe_insertion")))
                || StringUtils.containsIgnoreCase(queryAlteration, "indel")
                || StringUtils.containsIgnoreCase(queryAlteration, "dup")
                || StringUtils.containsIgnoreCase(queryAlteration, "del")
                || StringUtils.containsIgnoreCase(queryAlteration, "ins")
                || StringUtils.containsIgnoreCase(queryAlteration, "splice")
                || NamingUtils.hasAbbreviation(queryAlteration)
                || MainUtils.isEGFRTruncatingVariants(queryAlteration)
                ) {
                sb.append(queryAlteration + " altered");
            } else if (!queryAlteration.endsWith("mutation")) {
                sb.append(queryAlteration + " mutant");
            }
        }
        return sb.toString();
    }

    private static String replaceSpecialCharacterInTumorTypeSummary(String summary, Gene gene, String queryAlteration, String queryTumorType) {
        String altName = getGeneMutationNameInTumorTypeSummary(gene, queryAlteration);
        String alterationName = getGeneMutationNameInVariantSummary(gene, queryAlteration);
        String variantStr = altName + " " + queryTumorType;
        if (queryAlteration.contains("deletion")) {
            variantStr = queryTumorType + " harboring a " + altName;
        }
        summary = summary.replace("[[variant]]", variantStr);
        summary = summary.replace("[[gene]] [[mutation]] [[[mutation]]]", alterationName);

        // In case of miss typed
        summary = summary.replace("[[gene]] [[mutation]] [[mutation]]", alterationName);
        summary = summary.replace("[[gene]] [[mutation]] [[mutant]]", altName);
        summary = summary.replace("[[gene]] [[mutation]] [[[mutant]]]", altName);
        summary = summary.replace("[[gene]]", gene.getHugoSymbol());

        // Improve false tolerance. Curators often use hugoSymbol directly instead of [[gene]]
        String specialLocationAlt = gene.getHugoSymbol() + " [[mutation]] [[[mutation]]]";
        if (summary.contains(specialLocationAlt)) {
            summary = summary.replace(specialLocationAlt, alterationName);
        }
        specialLocationAlt = gene.getHugoSymbol() + " [[mutation]] [[mutation]]";
        if (summary.contains(specialLocationAlt)) {
            summary = summary.replace(specialLocationAlt, alterationName);
        }
        specialLocationAlt = gene.getHugoSymbol() + " [[mutation]] [[mutant]]";
        if (summary.contains(specialLocationAlt)) {
            summary = summary.replace(specialLocationAlt, altName);
        }

        summary = summary.replace("[[mutation]] [[mutant]]", altName);
        summary = summary.replace("[[mutation]] [[[mutation]]]", alterationName);
        // In case of miss typed
        summary = summary.replace("[[mutation]] [[mutation]]", queryAlteration);
        summary = summary.replace("[[mutation]]", queryAlteration);
        summary = summary.replace("[[tumorType]]", queryTumorType);
        summary = summary.replace("[[tumor type]]", queryTumorType);
        summary = summary.replace("[[fusion name]]", altName);
        summary = summary.replace("[[fusion name]]", altName);
        return summary;
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

    private static Map<String, Object> newTumorTypeSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("summary", "");
        return summary;
    }

    private static String getGeneArticle(Gene gene) {
        String[] vowels = {"A", "E", "I", "O", "U"};
        boolean isVowel = false;
        if (gene != null && gene.getHugoSymbol() != null) {
            for (int i = 0; i < vowels.length; i++) {
                if (gene.getHugoSymbol().startsWith(vowels[i])) {
                    isVowel = true;
                    break;
                }
            }
        }
        return isVowel ? "an" : "a";
    }
}
