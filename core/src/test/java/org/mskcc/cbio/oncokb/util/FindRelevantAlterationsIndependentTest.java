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
                .findRelevantAlterations(ReferenceGenome.GRCh37, query, Collections.singleton(alt1), true);

        assertEquals(1, relevantAlterations.size());

        relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(ReferenceGenome.GRCh38, query, Collections.singleton(alt1), true);

        assertEquals(0, relevantAlterations.size());


        Alteration bothRG = new Alteration();
        bothRG.setGene(braf);
        bothRG.setAlteration("V600E");
        Set<ReferenceGenome> referenceGenomes = new HashSet<>();
        referenceGenomes.add(ReferenceGenome.GRCh37);
        referenceGenomes.add(ReferenceGenome.GRCh38);
        bothRG.setReferenceGenomes(referenceGenomes);
        AlterationUtils.annotateAlteration(bothRG, bothRG.getAlteration());

        Set<Alteration> allAlterations = new HashSet<>();
        allAlterations.add(alt1);
        allAlterations.add(bothRG);

        relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(ReferenceGenome.GRCh37, query, allAlterations, true);

        assertEquals(2, relevantAlterations.size());

        relevantAlterations =
            ApplicationContextSingleton.getAlterationBo()
                .findRelevantAlterations(ReferenceGenome.GRCh38, query, allAlterations, true);

        assertEquals(1, relevantAlterations.size());
    }

}
