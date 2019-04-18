package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;

import java.util.*;

import static org.junit.Assert.assertEquals;

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

                // Check Fusions
                {"BRAF", "PAPSS1-BRAF Fusion", null, "PAPSS1-BRAF Fusion, Fusions, Oncogenic Mutations"},

                // The revert fusion should get picked
                {"ABL1", "ABL1-BCR fusion", null, "BCR-ABL1 Fusion, Fusions"},
                {"ABL1", "BCR-ABL1 fusion", null, "BCR-ABL1 Fusion, Fusions"},

                // Tumor suppressor should be mapped with Truncating Mutations. (The code does not check whether gene
                // is tumor suppressor, just check whether Fusions is curated, is not, link Truncating Mutations)
                {"PIK3R1", "KCTD16-PIK3R1 fusion", null, "KCTD16-PIK3R1 fusion, Truncating Mutations"},

                // General truncating consequence should be associated with Truncating Mutations
                // Check splice
                // TP53 Oncogenic Mutations does not have any information we are ready to relase
                {"TP53", "X33_splice", null, "X33_splice, Truncating Mutations"},
                {"MET", "X1010_splice", null, "X1010_splice, 963_D1010splice, 981_1028splice, 963_1028splice, Oncogenic Mutations"},
                {"MET", "X1010splice", null, "X1010_splice, 963_D1010splice, 981_1028splice, 963_1028splice, Oncogenic Mutations"},

                // Check stop_gained
                {"MAP2K4", "R304*", null, "R304*, Truncating Mutations"},

                // EGFR exon deletion
                {"EGFR", "vIII", null, "vIII, Oncogenic Mutations"},
                {"EGFR", "CTD", null, "C-terminal domain, Oncogenic Mutations"},
                {"EGFR", "vIV", null, "C-terminal domain, Oncogenic Mutations"},
                {"EGFR", "vIVa", null, "C-terminal domain, Oncogenic Mutations"},
                {"EGFR", "vIVb", null, "C-terminal domain, Oncogenic Mutations"},
                {"EGFR", "vIVc", null, "C-terminal domain, Oncogenic Mutations"},

                // Check range
                {"MED12", "G44S", null, "G44S, G44A, G44C, G44D, G44V, 34_68mut"},
                {"MED12", "G44D", null, "G44D, G44A, G44C, G44S, G44V, 34_68mut"},
                {"NOTCH1", "Q2405Rfs*17", null, "Q2405Rfs*17, T2375_K2555trunc, Truncating Mutations"},

                // VUS should get mapped to hotspot VUS, but should not get Oncogenic Mutations from the hotspot VUS.
                // In this case VUS N109_R113del is covered by VUS I99_R113del, and I99_R113del is a hotpot.
                // No longer applicable
//                {"MAP2K1", "N109_R113del", null, "N109_R113del, I99_R113del"},

                // Range missense variant
                {"PDGFRA", "D842I", null, "D842I, D842H, D842Y, D842_I843delinsIM, Oncogenic Mutations"},

                // D842V should not be mapped as alternative allele
                {"PDGFRA", "D842I", null, "D842I, D842H, D842Y, D842_I843delinsIM, Oncogenic Mutations"},
                {"PDGFRA", "D842V", null, "D842V, D842H, D842I, D842Y, D842_I843delinsIM, Oncogenic Mutations"},

                // Check whether the overlapped variants(with the same consequence) will be mapped
                {"MAP2K1", "E41_F53del", null, "E41_F53del, E41_L54del, E51_Q58del, F53_Q58del, F53_Q58delinsL, Oncogenic Mutations"},

                // Truncating Mutations in the Oncogene should not be mapped to any range mutation unless the consequence is truncating
                {"KIT", "K509Nfs*2", null, ""},
                {"MED12", "Q1836Lfs*57", null, "Truncating Mutations"},
                {"PIK3CA", "*1069Ffs*5", null, ""},

                // For oncogene, we do not map mut range to truncating mutations.
                // But we do map if gene is oncogene and TSG. TSG here is a Oncogene+TSG
                {"MED12", "A34*", null, "34_68mut, Truncating Mutations"},

                {"NOTCH1", "Q2405Rfs*17", null, "Q2405Rfs*17, T2375_K2555trunc, Truncating Mutations"},

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

                // Do not map few special KIT variants as alternative alleles, "K642E", "V654A", "T670I"
                {"KIT", "K652G", null, ""},

                // 654 is a hotspot position
                {"KIT", "V654G", null, "Oncogenic Mutations"},
                {"KIT", "T670A", null, "IT669MI"},


                // Do not mapping Oncogenic Mutations to Amplification
                {"KIT", "Amplification", null, "Amplification"},
            });
    }

    private String toString(LinkedHashSet<Alteration> relevantAlterations) {
        List<String> names = new ArrayList<>();
        for (Alteration alteration : relevantAlterations) {
            names.add(alteration.getAlteration());
        }
        return MainUtils.listToString(names, ", ");
    }

    @Test
    public void testAnnotateAlteration() throws Exception {
        // Particularly test consequence
        Alteration alt = AlterationUtils.getAlteration(hugoSymbol, alteration, AlterationType.getByName(alterationType), null, null, null);
        AlterationUtils.annotateAlteration(alt, alteration);

        LinkedHashSet<Alteration> relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(alt, AlterationUtils.getAllAlterations(alt.getGene()), true);
        String relevantAltsName = toString(relevantAlterations);

        assertEquals("Relevant alterations are not matched on case " +
            hugoSymbol + " " + alteration + " " + alterationType + " ", expectedRelevantAlterations, relevantAltsName);
    }

}
