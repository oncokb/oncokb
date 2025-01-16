package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;

/**
 * Created by Hongxin on 12/23/16.
 */

@RunWith(Parameterized.class)
public class FindRelevantAlterationsTest {
    private String hugoSymbol;
    private String alteration;
    private String alterationType;
    private String expectedRelevantAlterations;

    public FindRelevantAlterationsTest(String hugoSymbol, String alteration, String alterationType, String expectedRelevantAlterations) {
        this.hugoSymbol = hugoSymbol;
        this.alteration = alteration;
        this.alterationType = alterationType;
        this.expectedRelevantAlterations = expectedRelevantAlterations;
    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() {
        return Arrays.asList(
            new String[][]{
                {"MSH2", null, null, ""},
                {"MSH2", "", null, ""},

                // Critical cases
                {"BRAF", "V600E", null, "V600E, V600A, V600D, V600G, V600K, V600L, V600M, V600Q, V600R, VK600EI, V600, Oncogenic Mutations"},
                {"SMARCB1", "R374Q", null, "R374Q, R374W, Oncogenic Mutations"},
                {"EGFR", "S768I", null, "S768I, S768T, Oncogenic Mutations"},
                {"EGFR", "S768_V769delinsIL", null, "S768I, V769L, S768T, V769M, Oncogenic Mutations"},

                // Check resistance mutations, they should be matched with Oncogenic Mutations
                {"ALK", "G1202R", null, "G1202R, Oncogenic Mutations"},
                {"FLT3", "N676D", null, "N676D, N676K, N676S, Oncogenic Mutations"},
                // but the alternative allele should not be resistance, therefore, should not get Oncogenic Mutations matched
                {"ALK", "G1202E", null, "G1202R"},

                // Check Fusions
                {"BRAF", "PAPSS1-BRAF Fusion", null, "PAPSS1-BRAF Fusion, Fusions, Oncogenic Mutations, Oncogenic Mutations {excluding V600}"},

                // Check excluding issue
                {"BRAF", "V600E", null, "V600E, V600A, V600D, V600G, V600K, V600L, V600M, V600Q, V600R, VK600EI, V600, Oncogenic Mutations"},
                {"BRAF", "V600R", null, "V600R, V600A, V600D, V600E, V600G, V600K, V600L, V600M, V600Q, VK600EI, V600, Oncogenic Mutations, V600 {excluding V600E; V600K}"},
                {"BRAF", "L597S", null, "L597S, L597P, L597Q, L597R, L597V, L597, Oncogenic Mutations, Oncogenic Mutations {excluding V600}"},
                {"EGFR", "Y764_D770dup", null, "Y764_D770dup, 762_823ins, A767_V769dup, S768_D770dup, A767_S768insASV, S768_V769insSVD, S768_V769insVAS, V769_D770insASV, V769_D770insGVV, D770delinsGTH, D770delinsGY, A763_Y764insFQEA, D770_N771insD, D770_N771insG, D770_N771insGF, D770_N771insGL, D770_N771insNPG, D770_N771insSVD, D770_N771insVDSVDNP, D770_N771insY, D770_P772dup, Oncogenic Mutations, 762_823ins {excluding A763_Y764insFQEA}"},
                {"EGFR", "A763_Y764insFQEA", null, "A763_Y764insFQEA, 762_823ins, A763insLQEA, Y764_D770dup, Oncogenic Mutations"},

                // The revert fusion should get picked
                {"ABL1", "ABL1-BCR fusion", null, "BCR-ABL1 Fusion, Fusions"},
                {"ABL1", "BCR-ABL1 fusion", null, "BCR-ABL1 Fusion, Fusions"},

                // Tumor suppressor should be mapped with Truncating Mutations. (The code does not check whether gene
                // is tumor suppressor, just check whether Fusions is curated, is not, link Truncating Mutations)
                {"PIK3R1", "KCTD16-PIK3R1 fusion", null, "KCTD16-PIK3R1 fusion, Truncating Mutations"},

                // General truncating consequence should be associated with Truncating Mutations
                // Check splice
                // TP53 Oncogenic Mutations does not have any information we are ready to release
                {"TP53", "X33_splice", null, "X33_splice, Truncating Mutations, Oncogenic Mutations"},
                {"MET", "X1010_splice", null, "X1010_splice, 963_1010splice, 981_1028splice, Oncogenic Mutations"},
                {"MET", "X1010splice", null, "X1010_splice, 963_1010splice, 981_1028splice, Oncogenic Mutations"},

                // Check stop_gained
                {"MAP2K4", "R304*", null, "R304*, Truncating Mutations"},
                {"NF1", "L1340_Q1341delinsF*", null, "1_2771trunc, Oncogenic Mutations"},

                // Check stop_lost, especially it should not be associated with truncating mutation
                {"MLH1", "*757Kext*36", null, "*757Kext*36"},

                // Check inframe-insertion, inframe-deletion
                {"EGFR", "Y764_D770dup", null, "Y764_D770dup, 762_823ins, A767_V769dup, S768_D770dup, A767_S768insASV, S768_V769insSVD, S768_V769insVAS, V769_D770insASV, V769_D770insGVV, D770delinsGTH, D770delinsGY, A763_Y764insFQEA, D770_N771insD, D770_N771insG, D770_N771insGF, D770_N771insGL, D770_N771insNPG, D770_N771insSVD, D770_N771insVDSVDNP, D770_N771insY, D770_P772dup, Oncogenic Mutations, 762_823ins {excluding A763_Y764insFQEA}"},
                {"EGFR", "A763_Y764insFQEA", null, "A763_Y764insFQEA, 762_823ins, A763insLQEA, Y764_D770dup, Oncogenic Mutations"},
                {"EGFR", "E746_A750del", null, "E746_A750del, E746_A750delinsQ, E746_T751delinsA, E746_T751delinsL, E746_T751delinsVA, K745_A750del, E746_S752delinsA, E746_S752delinsI, E746_S752delinsV, 729_761del, L747_A750del, L747_A750delinsP, L747_T751del, L747_T751delinsP, L747_S752del, L747_P753del, L747_P753delinsS, L747_K754delinsATSPE, L747_E749del, A750_E758del, A750_E758delinsP, Oncogenic Mutations"},
                {"EGFR", "E746_A750delinsQ", null, "E746_A750delinsQ, E746_A750del, E746_T751delinsA, E746_T751delinsL, E746_T751delinsVA, K745_A750del, E746_S752delinsA, E746_S752delinsI, E746_S752delinsV, 729_761del, L747_A750del, L747_A750delinsP, L747_T751del, L747_T751delinsP, L747_S752del, L747_P753del, L747_P753delinsS, L747_K754delinsATSPE, L747_E749del, A750_E758del, A750_E758delinsP, Oncogenic Mutations"},

                // Check frame shift
                // G659fs*41 is curated as likely neutral. We do not do further relevant alts match if alt is not oncogenic
                {"RNF43", "G659Vfs*41", null, "G659fs*41"},
                // G700fs*41 is not curated, but we have general Truncating Mutations curated
                {"RNF43", "G700fs*41", null, "Truncating Mutations"},
                // if the frame shift variant happens on stop codon, it's extension rather truncating
                {"RNF43", "*700fs*41", null, ""},
                {"RNF43", "*700fs*?", null, ""},

                // EGFR exon deletion
                {"EGFR", "vIII", null, "vIII, Oncogenic Mutations"},
                {"EGFR", "CTD", null, "C-terminal domain, Oncogenic Mutations"},
                {"EGFR", "vIV", null, "C-terminal domain, Oncogenic Mutations"},
                {"EGFR", "vIVa", null, "C-terminal domain, Oncogenic Mutations"},
                {"EGFR", "vIVb", null, "C-terminal domain, Oncogenic Mutations"},
                {"EGFR", "vIVc", null, "C-terminal domain, Oncogenic Mutations"},

                // Check range
                {"MED12", "G44S", null, "G44S, G44A, G44C, G44D, G44V, 34_68mis"},
                {"MED12", "G44D", null, "G44D, G44A, G44C, G44S, G44V, 34_68mis"},
                {"MED12", "G44*", null, "Truncating Mutations"},
                {"MED12", "38_40del", null, ""},
                {"NOTCH1", "Q2405Rfs*17", null, "Q2405Rfs*17, 2375_2555trunc, Oncogenic Mutations"},
                {"CALR", "K385Nfs*47", null, "309_417trunc, Oncogenic Mutations"},

                // VUS should get mapped to hotspot VUS, but should not get Oncogenic Mutations from the hotspot VUS.
                // In this case VUS N109_R113del is covered by VUS I99_R113del, and I99_R113del is a hotpot.
                // No longer applicable
//                {"MAP2K1", "N109_R113del", null, "N109_R113del, I99_R113del"},

                // Range missense variant
                {"PDGFRA", "D842I", null, "D842I, D842H, D842N, D842Y, D842_I843delinsIM, 814_852mis, Oncogenic Mutations"},

                // D842V should not be mapped as alternative allele
                {"PDGFRA", "D842I", null, "D842I, D842H, D842N, D842Y, D842_I843delinsIM, 814_852mis, Oncogenic Mutations"},
                {"PDGFRA", "D842V", null, "D842V, D842H, D842I, D842N, D842Y, D842_I843delinsIM, 814_852mis, Oncogenic Mutations"},

                // Check whether the overlapped variants(with the same consequence) will be mapped
                {"MAP2K1", "E41_F53del", null, "E41_F53del, E41_L54del, L42_K57del, E51_Q58del, F53_Q58del, F53_Q58delinsL, Oncogenic Mutations"},

                // Truncating Mutations in the Oncogene should not be mapped to any range mutation unless the consequence is truncating
                {"KIT", "K509Nfs*2", null, "K509Nfs*2"},
                {"MED12", "Q1836Lfs*57", null, "Truncating Mutations"},
                {"PIK3CA", "*1069Ffs*5", null, ""},

                // For oncogene, we do not map mut range to truncating mutations.
                // But we do map if gene is oncogene and TSG. TSG here is a Oncogene+TSG
                {"MED12", "A34*", null, "Truncating Mutations"},

                {"NOTCH1", "Q2405Rfs*17", null, "Q2405Rfs*17, 2375_2555trunc, Oncogenic Mutations"},

                // Deletion
                // With specific Deletion curated
                {"BRCA2", "Deletion", null, "Deletion, Oncogenic Mutations"},
                // Without specific Deletion curated
                {"MAP2K4", "Deletion", null, "Truncating Mutations"},

                // Abbreviated alterations
                {"EGFR", "KDD", null, "Kinase Domain Duplication, Oncogenic Mutations"},
                {"EGFR", "Kinase Domain Duplication", null, "Kinase Domain Duplication, Oncogenic Mutations"},
                {"EGFR", "CTD", null, "C-terminal domain, Oncogenic Mutations"},
                {"EGFR", "C-terminal domain", null, "C-terminal domain, Oncogenic Mutations"},
                {"EGFR", "vII", null, "vII, Oncogenic Mutations"},
                {"EGFR", "vIII", null, "vIII, Oncogenic Mutations"},

                // Do not get alternative alleles or positional variant for ABL1 T315I
                {"ABL1", "T315I", null, "T315I"},

                // 654 is a hotspot position
                {"KIT", "V654G", null, "V654A, Oncogenic Mutations"},
                {"KIT", "T670A", null, "T670I, I669_T670delinsMI, Oncogenic Mutations"},


                // Do not map Oncogenic Mutations to Amplification
                {"KIT", "Amplification", null, "Amplification"},

                // Do not map missense alteration with different reference AA as relevant
                {"BRAF", "A600E", null, ""},



                // Check non_truncating_variant
//                {"MED12", "A22*", null, "1_33mut, Truncating Mutations"},
//                {"MED12", "G22D", null, "1_33nontrunc, 1_33mut"},

                // Check Variants of Unknown Significance is associated
                {"BRCA2", "V159L", null, "V159L, V159M, Variants of Unknown Significance"}, // this is curated VUS
                {"BRCA2", "R2659G", null, "R2659G"}, // this is curated inconclusive
                {"BRCA2", "E3002K", null, "E3002K, E3002D, Oncogenic Mutations"}, // this is curated likely oncogenic
                {"BRCA2", "Y3035C", null, "Y3035C"}, // this is curated likely neutral
                {"BRCA2", "E790*", null, "Truncating Mutations, Oncogenic Mutations"}, // this is not curated truncating mutation
                {"BRCA2", "E10000A", null, "Variants of Unknown Significance"}, // this is not curated
            });
    }

    @Test
    public void testAnnotateAlteration() throws Exception {
        // Particularly test consequence
        Alteration alt = AlterationUtils.getAlteration(hugoSymbol, alteration, AlterationType.getByName(alterationType), null, null, null, null);

        LinkedHashSet<Alteration> relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(DEFAULT_REFERENCE_GENOME, alt, AlterationUtils.getAllAlterations(DEFAULT_REFERENCE_GENOME, alt.getGene()), true);
        Set<Alteration> excludings = relevantAlterations.stream().filter(relevantAlt->relevantAlt.getAlteration().contains("exclud")).collect(Collectors.toSet());
        relevantAlterations.removeAll(excludings);
        String relevantAltsName = AlterationUtils.toString(relevantAlterations);
        if(excludings.size() > 0) {
            relevantAltsName += ", " + AlterationUtils.toString(excludings);
        }

        assertEquals("Relevant alterations are not matched on case " +
            hugoSymbol + " " + alteration + " " + alterationType + " ", expectedRelevantAlterations, relevantAltsName);
    }

}
