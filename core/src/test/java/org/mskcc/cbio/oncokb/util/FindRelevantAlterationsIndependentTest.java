package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;


/**
 * Created by Hongxin on 3/8/17.
 */
public class FindRelevantAlterationsIndependentTest extends TestCase {
    public void testFindRelevantAlterationsWithReferenceGenome() throws Exception {
        Gene braf = new Gene();
        braf.setHugoSymbol("BRAF");
        braf.setEntrezGeneId(673);

        Alteration alt1 = new Alteration();
        alt1.setGene(braf);
        alt1.setAlteration("V600E");
        alt1.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh37));
        AlterationUtils.annotateAlteration(alt1, alt1.getAlteration());


        Alteration query = new Alteration();
        query.setGene(braf);
        query.setAlteration("V600E");
        AlterationUtils.annotateAlteration(query, query.getAlteration());

        LinkedHashSet<Alteration> relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(ReferenceGenome.GRCh37, query, Collections.singletonList(alt1), true);

        assertEquals(1, relevantAlterations.size());

        relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(ReferenceGenome.GRCh38, query, Collections.singletonList(alt1), true);

        assertEquals(0, relevantAlterations.size());


        Alteration bothRG = new Alteration();
        bothRG.setGene(braf);
        bothRG.setAlteration("V600E");
        Set<ReferenceGenome> referenceGenomes = new HashSet<>();
        referenceGenomes.add(ReferenceGenome.GRCh37);
        referenceGenomes.add(ReferenceGenome.GRCh38);
        bothRG.setReferenceGenomes(referenceGenomes);
        AlterationUtils.annotateAlteration(bothRG, bothRG.getAlteration());

        List<Alteration> allAlterations = new ArrayList<>();
        allAlterations.add(alt1);
        allAlterations.add(bothRG);

        relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(ReferenceGenome.GRCh38, query, allAlterations, true);

        assertEquals(1, relevantAlterations.size());
    }

    public void testFindRelevantAlterationsWithConsequence() {
        Gene gene = new Gene();
        gene.setHugoSymbol("MET");
        gene.setEntrezGeneId(4233);

        Alteration alt1 = new Alteration();
        alt1.setGene(gene);
        alt1.setAlteration("X1010_splice");
        alt1.setConsequence(VariantConsequenceUtils.findVariantConsequenceByTerm("splice_region_variant"));
        alt1.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh37));
        AlterationUtils.annotateAlteration(alt1, alt1.getAlteration());


        // alt should be matched when the consequence is the same
        Alteration query = new Alteration();
        query.setGene(gene);
        query.setAlteration("X1010_splice");
        query.setConsequence(VariantConsequenceUtils.findVariantConsequenceByTerm("splice_region_variant"));
        AlterationUtils.annotateAlteration(query, query.getAlteration());

        LinkedHashSet<Alteration> relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(ReferenceGenome.GRCh37, query, Collections.singletonList(alt1), true);

        assertEquals(1, relevantAlterations.size());


        // alt should be matched when the consequence is relevant
        query = new Alteration();
        query.setGene(gene);
        query.setAlteration("X1010_splice");
        query.setConsequence(VariantConsequenceUtils.findVariantConsequenceByTerm("splice_donor_variant"));
        AlterationUtils.annotateAlteration(query, query.getAlteration());

        relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(ReferenceGenome.GRCh37, query, Collections.singletonList(alt1), true);

        assertEquals(1, relevantAlterations.size());


        // alt should not be matched when the consequence is irrelevant
        query = new Alteration();
        query.setGene(gene);
        query.setAlteration("X1010_splice");
        query.setConsequence(VariantConsequenceUtils.findVariantConsequenceByTerm("missense_mutation"));
        AlterationUtils.annotateAlteration(query, query.getAlteration());

        relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(ReferenceGenome.GRCh37, query, Collections.singletonList(alt1), true);

        assertEquals(0, relevantAlterations.size());

    }

    public void testFindRelevantAlterationsForInframeDeletionWithTrailing() {
        Gene gene = new Gene();
        gene.setHugoSymbol("PIK3CA");
        gene.setEntrezGeneId(5290);

        String inframeDelAltTrailing = "V105_R108delVGNR";
        String inframeDelAlt = "V105_R108del";

        // Check inframe deletion without trailing can be matched to with trailing
        Alteration alt1 = new Alteration();
        alt1.setGene(gene);
        alt1.setAlteration(inframeDelAltTrailing);
        AlterationUtils.annotateAlteration(alt1, alt1.getAlteration());


        // alt should be matched when the consequence is the same
        Alteration query = new Alteration();
        query.setGene(gene);
        query.setAlteration(inframeDelAlt);
        AlterationUtils.annotateAlteration(query, query.getAlteration());

        LinkedHashSet<Alteration> relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(ReferenceGenome.GRCh37, query, Collections.singletonList(alt1), true);

        assertEquals(1, relevantAlterations.size());
        assertTrue(relevantAlterations.iterator().next().getAlteration().equals(inframeDelAltTrailing));


        // Check inframe deletion with trailing can be matched to without trailing
        alt1 = new Alteration();
        alt1.setGene(gene);
        alt1.setAlteration(inframeDelAlt);
        AlterationUtils.annotateAlteration(alt1, alt1.getAlteration());


        // alt should be matched when the consequence is the same
        query = new Alteration();
        query.setGene(gene);
        query.setAlteration(inframeDelAltTrailing);
        AlterationUtils.annotateAlteration(query, query.getAlteration());

        relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(ReferenceGenome.GRCh37, query, Collections.singletonList(alt1), true);

        assertEquals(1, relevantAlterations.size());
        assertTrue(relevantAlterations.iterator().next().getAlteration().equals(inframeDelAlt));
    }

}
