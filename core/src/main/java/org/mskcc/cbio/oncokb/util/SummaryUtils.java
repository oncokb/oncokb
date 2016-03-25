package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.Observer.CacheSubject;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;
import org.mskcc.cbio.oncokb.bo.GeneBo;

/**
 * Created by Hongxin on 8/10/15.
 */
public class SummaryUtils {

    public static long lastUpdateVariantSummaries = new Date().getTime();

    public static String variantSummary(Set<Gene> genes, List<Alteration> alterations, String queryAlteration, Set<TumorType> relevantTumorTypes, String queryTumorType) {
        String geneId = Integer.toString(genes.iterator().next().getEntrezGeneId());
        String key = geneId + "&&" + queryAlteration + "&&" + queryTumorType;

        if(CacheUtils.summaryCacheObserver.containSummary(geneId, key)) {
            return CacheUtils.summaryCacheObserver.getSummary(geneId, key);
        }

        StringBuilder sb = new StringBuilder();
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();

        queryTumorType = queryTumorType == null? null : (StringUtils.isAllUpperCase(queryTumorType)?queryTumorType:queryTumorType.toLowerCase());

        Boolean appendThe = true;
        Boolean isPlural = false;

        if (queryAlteration.toLowerCase().contains("deletion") || queryAlteration.toLowerCase().contains("amplification") || queryAlteration.toLowerCase().contains("fusion")) {
            appendThe = false;
        }

        if (queryAlteration.toLowerCase().contains("fusions")) {
            isPlural = true;
        }

        if (genes.isEmpty() || alterations==null || alterations.isEmpty()) {
            sb.append("The oncogenic activity of this variant is unknown. ");
        } else {
            int oncogenic = -1;
            for (Alteration a : alterations) {
                List<Evidence> oncogenicEvidences = evidenceBo.findEvidencesByAlteration(Collections.singleton(a), Collections.singleton(EvidenceType.ONCOGENIC));
                if (oncogenicEvidences.size() > 0 && Integer.parseInt(oncogenicEvidences.get(0).getKnownEffect()) > 0) {
                    oncogenic = Integer.parseInt(oncogenicEvidences.get(0).getKnownEffect());
                    break;
                }
            }

            //Mutation summary
            List<Evidence> mutationSummaryEvs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_SUMMARY));
            if (!mutationSummaryEvs.isEmpty()) {
                Evidence ev = mutationSummaryEvs.get(0);
                String mutationSummary = ev.getShortDescription();

                if(mutationSummary == null) {
                    mutationSummary = ev.getDescription();
                }
                if(mutationSummary != null) {
                    mutationSummary = StringEscapeUtils.escapeXml(mutationSummary).trim();
                    sb.append(mutationSummary)
                            .append(" ");
                }
            }else{
                if (oncogenic > 0) {
                    if (appendThe) {
                        sb.append("The ");
                    }
                    sb.append(queryAlteration);

                    if (isPlural) {
                        sb.append(" are");
                    } else {
                        sb.append(" is");
                    }

                    if (oncogenic == 2) {
                        sb.append(" likely");
                    } else if (oncogenic == 1) {
                        sb.append(" known to be");
                    }

                    sb.append(" oncogenic. ");
                } else {
                    sb.append("It is unknown whether ");
                    if (appendThe) {
                        sb.append("the ");
                    }

                    sb.append(queryAlteration);

                    if (isPlural) {
                        sb.append(" are");
                    } else {
                        sb.append(" is");
                    }
                    sb.append(" oncogenic. ");
                }
            }

            //Tumor type summary
            List<Evidence> tumorTypeSummaryEvs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), relevantTumorTypes);
            if (!tumorTypeSummaryEvs.isEmpty()) {
                Evidence ev = tumorTypeSummaryEvs.get(0);
                String tumorTypeSummary = ev.getShortDescription();

                if(tumorTypeSummary == null){
                    tumorTypeSummary = ev.getDescription();
                }
                if(tumorTypeSummary != null) {
                    tumorTypeSummary = StringEscapeUtils.escapeXml(tumorTypeSummary).trim();
                    sb.append(tumorTypeSummary)
                            .append(" ");
                }
            }else{

                Set<EvidenceType> sensitivityEvidenceTypes =
                        EnumSet.of(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY,
                                EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
                Map<LevelOfEvidence, List<Evidence>> evidencesByLevel = groupEvidencesByLevel(
                        evidenceBo.findEvidencesByAlteration(alterations, sensitivityEvidenceTypes, relevantTumorTypes)
                );
                List<Evidence> evidences = new ArrayList<>();
//                if (!evidencesByLevel.get(LevelOfEvidence.LEVEL_0).isEmpty()) {
//                    evidences.addAll(evidencesByLevel.get(LevelOfEvidence.LEVEL_0));
//                }
                if (!evidencesByLevel.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                    // if there are FDA approved drugs in the patient tumor type with the variant
                    evidences.addAll(evidencesByLevel.get(LevelOfEvidence.LEVEL_1));
                    sb.append(treatmentsToStringByTumorType(evidences, queryAlteration, queryTumorType, true, true, false, false))
                            .append(". ");
                } else if (!evidencesByLevel.get(LevelOfEvidence.LEVEL_2A).isEmpty()) {
                    // if there are NCCN guidelines in the patient tumor type with the variant
//                Map<LevelOfEvidence, List<Evidence>> otherEvidencesByLevel = groupEvidencesByLevel(
//                        evidenceBo.findEvidencesByAlteration(alterations, sensitivityEvidenceTypes)
//                );
//                if (!otherEvidencesByLevel.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
//                    // FDA approved drugs in other tumor type with the variant
//                    sb.append("There are FDA approved drugs ")
//                        .append(treatmentsToStringbyTumorType(otherEvidencesByLevel.get(LevelOfEvidence.LEVEL_1), queryAlteration))
//                        .append(". ");
//                }
                    evidences.addAll(evidencesByLevel.get(LevelOfEvidence.LEVEL_2A));
                    sb.append(treatmentsToStringByTumorType(evidences, queryAlteration, queryTumorType, true, false, true, false))
                            .append(". ");
                } else {
                    // no FDA or NCCN in the patient tumor type with the variant
                    Map<LevelOfEvidence, List<Evidence>> evidencesByLevelOtherTumorType = groupEvidencesByLevel(
                            evidenceBo.findEvidencesByAlteration(alterations, sensitivityEvidenceTypes)
                    );
                    evidences.clear();
//                    if (!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_0).isEmpty()) {
//                        evidences.addAll(evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_0));
//                    }

                    if (!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                        // if there are FDA approved drugs in other tumor types with the variant
                        evidences.addAll(evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_1));
                        sb.append("While ")
                                .append(treatmentsToStringByTumorType(evidences, queryAlteration, queryTumorType, false, true, false, true))
                                .append(", the clinical utility for patients with ")
                                .append(queryTumorType == null ? "tumors" : queryTumorType)
                                .append(" harboring the " + queryAlteration)
                                .append(" is unknown. ");
                    } else if (!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_2A).isEmpty()) {
                        // if there are NCCN drugs in other tumor types with the variant
                        evidences.addAll(evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_2A));
                        sb.append(treatmentsToStringByTumorType(evidences, queryAlteration, queryTumorType, true, false, true, true))
                                .append(", the clinical utility for patients with ")
                                .append(queryTumorType == null ? "tumors" : queryTumorType)
                                .append(" harboring the " + queryAlteration)
                                .append(" is unknown. ");
                    } else {
                        // no FDA or NCCN drugs for the variant in any tumor type -- remove wild type evidence
                        List<Evidence> evs = evidenceBo.findEvidencesByGene(genes, sensitivityEvidenceTypes);
                        for (Gene gene : genes) {
                            Alteration alt = alterationBo.findAlteration(gene, AlterationType.MUTATION, "wildtype");
                            EvidenceUtils.removeByAlterations(evs , Collections.singleton(alt));
                        }
                        Map<LevelOfEvidence, List<Evidence>> evidencesByLevelGene = groupEvidencesByLevel(evs);

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
                                    .append(" harboring the " + queryAlteration)
                                    .append(" is unknown. ");
                        } else if (!evidencesByLevelGene.get(LevelOfEvidence.LEVEL_2A).isEmpty()) {
                            // if there are NCCN drugs for different variants in the same gene (either same tumor type or different ones) .. e.g. BRAF K601E
                            evidences.addAll(evidencesByLevelGene.get(LevelOfEvidence.LEVEL_2A));
                            sb.append(treatmentsToStringByTumorType(evidences, null, queryTumorType, true, false, true, true))
                                    .append(", the clinical utility for patients with ")
                                    .append(queryTumorType == null ? "tumors" : queryTumorType)
                                    .append(" harboring the " + queryAlteration)
                                    .append(" is unknown. ");
                        } else {
                            // if there is no FDA or NCCN drugs for the gene at all
                            sb.append("There are no FDA-approved or NCCN-compendium listed treatments specifically for patients with ")
                                    .append(queryTumorType == null ? "tumors" : queryTumorType)
                                    .append(" harboring ");
                            if (appendThe) {
                                sb.append("the ");
                            }
                            sb.append(queryAlteration)
                                    .append(". ");
                        }
                    }

//                sb.append("Please refer to the clinical trials section. ");
                }
            }
        }

        CacheUtils.summaryCacheObserver.setSummary(geneId, key, sb.toString().trim());
        return sb.toString().trim();
    }

    public static String fullSummary(Set<Gene> genes, List<Alteration> alterations, String queryAlteration, Set<TumorType> relevantTumorTypes, String queryTumorType) {
        StringBuilder sb = new StringBuilder();
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        queryTumorType = queryTumorType!=null?StringUtils.isAllUpperCase(queryTumorType)?queryTumorType:queryTumorType.toLowerCase():null;

        List<Evidence> geneSummaryEvs = evidenceBo.findEvidencesByGene(genes, Collections.singleton(EvidenceType.GENE_SUMMARY));
        if (!geneSummaryEvs.isEmpty()) {
            Evidence ev = geneSummaryEvs.get(0);
            String geneSummary = ev.getShortDescription();

            if(geneSummary == null){
                geneSummary = ev.getDescription();
            }

            if(geneSummary != null) {
                geneSummary = StringEscapeUtils.escapeXml(geneSummary).trim();
                sb.append(geneSummary)
                        .append(" ");
            }
        }

        sb.append(SummaryUtils.variantSummary(genes, alterations, queryAlteration, relevantTumorTypes, queryTumorType));

        return sb.toString();
    }

    private static Map<LevelOfEvidence, List<Evidence>> groupEvidencesByLevel(List<Evidence> evidences) {
        Map<LevelOfEvidence, List<Evidence>> map = new EnumMap<LevelOfEvidence, List<Evidence>>(LevelOfEvidence.class);
        for (LevelOfEvidence level : LevelOfEvidence.values()) {
            map.put(level, new ArrayList<Evidence>());
        }
        for (Evidence ev : evidences) {
            if (ev.getLevelOfEvidence()==null || ev.getTreatments().isEmpty()) continue;
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
        Set<String> drugs= new HashSet<>();
        Map<String, Set<String>> levelZeroDrugs= new HashMap<>();
        List<String> list = new ArrayList<String>();

        for (Evidence ev : evidences) {
            String tt = ev.getTumorType().getName().toLowerCase();
            Map<String, Map<String, Object>> ttMap = map.get(tt);
            if (ttMap == null && !ev.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_0)) {
                ttMap = new TreeMap<String, Map<String, Object>>();
                map.put(tt, ttMap);
            }

            for (Treatment t : ev.getTreatments()) {
                for (Drug drug : t.getDrugs()) {
                    String drugName = drug.getDrugName().toLowerCase();
                    if(ev.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_0)){
                        if(!levelZeroDrugs.containsKey(drugName)){
                            levelZeroDrugs.put(drugName, new HashSet<String>());
                        }
                        if(!levelZeroDrugs.get(drugName).contains(tt)){
                            levelZeroDrugs.get(drugName).add(tt);
                        }
                    }else{
                        Map<String, Object> drugMap = ttMap.get(drugName);
                        if(!drugs.contains(drugName)){
                            drugs.add(drugName);
                        }
                        if(drugMap == null){
                            drugMap = new TreeMap<>();
                            ttMap.put(drugName, drugMap);
                            drugMap.put("approvedIndications", t.getApprovedIndications());
                            drugMap.put("level", ev.getLevelOfEvidence());
                            drugMap.put("alteration", ev.getAlterations());
                        }
                    }
                }
            }
        }

        if(map.size() > 2){
            list.add(treatmentsToStringAboveLimit(drugs, capFirstLetter, fda, nccn, null));
        }else{
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
        return listToString(list, " and ");
    }

    private static String treatmentsToStringLevelZero(Map<String, Set<String>> drugs, Boolean capFirstLetter){
        StringBuilder sb = new StringBuilder();
        Set<String> tumorTypes = new HashSet<>();
        boolean sameDrugs = true;

        for(String drugName : drugs.keySet()){
            if(tumorTypes.isEmpty()){
                tumorTypes = drugs.get(drugName);
            }else{
                if(tumorTypes.size() != drugs.get(drugName).size()){
                    sameDrugs = false;
                    break;
                }
                for(String tt : drugs.get(drugName)){
                    if(!tumorTypes.contains(tt)){
                        sameDrugs = false;
                        break;
                    }
                }
            }
        }

        if(sameDrugs) {
            sb.append(drugStr(drugs.keySet(), capFirstLetter, true, false, null));
        }else{
            sb.append(capFirstLetter ? "T" : "t")
                    .append("here are multiple FDA-approved agents");
        }
        sb.append(" for treatment of patients with ");
        sb.append(tumorTypes.size()>2?"different tumor types":listToString(new ArrayList<String>(tumorTypes), " and "))
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
        Set<String> approvedIndications = (Set<String>)drugMap.get("approvedIndications");
        String aiStr = null;

        for(String ai : approvedIndications){
            if(ai !=null && !ai.isEmpty()){
                aiStr = ai;
                break;
            }
        }

        sb.append(drugStr(drugs, capFirstLetter, fda, nccn, aiStr))
                .append(" for treatment of patients ")
                .append(tumorType == null ? "" : ("with " + tumorType + " "))
                .append("harboring ");

        if (alteration!=null) {
            sb.append("the ").append(alteration);
        } else if ((Boolean)drugAltMap.get("isSame")){
            Set<Alteration> alterations = (Set<Alteration>)drugAltMap.get("alterations");

            if (alterations.size() <= 2){
                sb.append("the ").append(alterationsToString(alterations));
            }else {
                sb.append("specific mutations");
            }

        } else{
            sb.append("specific mutations");
        }
        return sb.toString();
    }

    private static String drugStr(Set<String> drugs, boolean capFirstLetter, boolean fda, boolean nccn, String approvedIndication) {
        int drugLimit = 3;

        StringBuilder sb = new StringBuilder();

        if(drugs.size() > drugLimit){
            sb.append(capFirstLetter?"T":"t").append("here");
        }else{
            sb.append(capFirstLetter?"T":"t").append("he drug");
            if (drugs.size()>1) {
                sb.append("s");
            }
            sb.append(" ");
            sb.append(listToString(new ArrayList<String>(drugs), " and "));
        }
        if (fda || nccn) {
            sb.append(" ");
            if (drugs.size()>1) {
                sb.append("are");
            } else {
                sb.append("is");
            }
        }

        if (fda) {
            sb.append(" FDA-approved");
        } else if (nccn){
            if(approvedIndication != null){
                sb.append(" FDA-approved for the treatment of ")
                        .append(approvedIndication)
                        .append(" and");
            }

            if (drugs.size() > drugLimit || approvedIndication != null) {
                sb.append(" NCCN-compendium listed");
            }else if(drugs.size() <= drugLimit) {
                sb.append(" listed by NCCN-compendium");
            }
        }

        if (drugs.size() > drugLimit) {
            sb.append(" drugs");
        }

        return sb.toString();
    }

    private static Map<String, Object> drugsAreSameByAlteration(Map<String, Map<String, Object>> drugs){
        Set<Alteration> alterations = new HashSet<>();
        Map<String, Object> map = new HashMap<>();

        map.put("isSame", true);
        map.put("alterations", alterations);

        for (String drugName : drugs.keySet()) {
            Map<String, Object> drug = drugs.get(drugName);
            Set<Alteration> alts = (Set<Alteration>)drug.get("alteration");
            if(alterations.isEmpty()){
                alterations = alts;
            }else{
                if(alterations.size() != alts.size()) {
                    map.put("isSame", false);
                    return map;
                }

                for(Alteration alt : alts){
                    if(!alterations.contains(alt)){
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
        Map<String,Set<String>> mapGeneVariants = new TreeMap<String,Set<String>>();
        for (Alteration alteration : alterations) {
            String gene = alteration.getGene().getHugoSymbol();
            Set<String> variants = mapGeneVariants.get(gene);
            if (variants==null) {
                variants = new TreeSet<String>();
                mapGeneVariants.put(gene, variants);
            }
            variants.add(alteration.getName());
        }

        List<String> list = new ArrayList<String>();
        for (Map.Entry<String,Set<String>> entry : mapGeneVariants.entrySet()) {
            list.add(entry.getKey()+" "+listToString(new ArrayList<String>(entry.getValue()), " and "));
        }

        String gene = alterations.iterator().next().getGene().getHugoSymbol();

        String ret = listToString(list, " or ");

        if(!ret.startsWith(gene)) {
            ret =  gene + " " + ret;
        }

        String retLow = ret.toLowerCase();
        if (retLow.endsWith("mutation")||retLow.endsWith("mutations")) {
            return ret;
        }

        return ret + " mutation" + (alterations.size()>1?"s":"");
    }

    private static String specialMutation(String mutationStr) {
        String[] specialMutations = {"amplification", "deletion", "fusion", "fusions"};
        mutationStr = mutationStr.toString();

        if(stringContainsItemFromList(mutationStr, specialMutations)) {
            if(itemFromListAtEndString(mutationStr, specialMutations)) {
                return mutationStr;
            }else{

            }
        }else {
            return mutationStr;
        }

        return mutationStr;
    }

    public static boolean stringContainsItemFromList(String inputString, String[] items) {
        for(int i =0; i < items.length; i++)
        {
            if(inputString.contains(items[i]))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean itemFromListAtEndString(String inputString, String[] items) {
        for(int i =0; i < items.length; i++)
        {
            if(inputString.endsWith(items[i]))
            {
                return true;
            }
        }
        return false;
    }

    private static String listToString(List<String> list, String separator) {
        if (list.isEmpty()) {
            return "";
        }

        int n = list.size();
        StringBuilder sb = new StringBuilder();
        sb.append(list.get(0));
        if (n==1) {
            return sb.toString();
        }

        for (int i=1; i<n-1; i++) {
            sb.append(", ").append(list.get(i));
        }

        sb.append(separator).append(list.get(n-1));

        return sb.toString();
    }

}
