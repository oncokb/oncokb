package org.mskcc.cbio.oncokb.util;

import static org.junit.Assert.assertEquals;
import static org.mskcc.cbio.oncokb.util.CplUtils.annotate;
import static org.mskcc.cbio.oncokb.util.CplUtils.annotateGene;

import org.junit.Test;
import org.mskcc.cbio.oncokb.model.Gene;

public class CplUtilsTest {

    @Test
    public void testAnnotate() {
        assertEquals(
            "BRAF",
            annotate(
                "[[gene]]",
                "BRAF",
                null,
                null,
                null,
                null,
                null
            )
        );
        assertEquals(
            "braf",
            annotate(
                "[[gene]]",
                "braf",
                null,
                null,
                null,
                null,
                null
            )
        );

        // even when Gene is specified, we still use the query hugoSymbol
        Gene gene = new Gene();
        gene.setHugoSymbol("BRAF");
        assertEquals(
            "braf",
            annotate(
                "[[gene]]",
                "braf",
                null,
                null,
                null,
                gene,
                null
            )
        );

        assertEquals(
            "BRAF V600E",
            annotate(
                "[[gene]] [[mutation]]",
                "BRAF",
                "V600E",
                null,
                null,
                null,
                null
            )
        );

        assertEquals(
            "BRAF V600E mutation",
            annotate(
                "[[gene]] [[mutation]] [[[mutation]]]",
                "BRAF",
                "V600E",
                null,
                null,
                null,
                null
            )
        );

        assertEquals(
            "BRAF V600E mutant",
            annotate(
                "[[gene]] [[mutation]] [[[mutant]]]",
                "BRAF",
                "V600E",
                null,
                null,
                null,
                null
            )
        );

        assertEquals(
            "melanoma",
            annotate(
                "[[tumor type]]",
                "BRAF",
                "V600E",
                "Melanoma",
                null,
                null,
                null
            )
        );

        assertEquals(
            "BRAF V600E mutant melanoma",
            annotate(
                "[[variant]]",
                "BRAF",
                "V600E",
                "Melanoma",
                null,
                null,
                null
            )
        );
    }

    @Test
    public void testAnnotateGene() {
        assertEquals("BRAF", annotateGene("[[gene]]", "BRAF"));
        assertEquals("bRaf", annotateGene("[[gene]]", "bRaf"));
        assertEquals(" BRAF", annotateGene(" [[gene]]", "BRAF"));
        assertEquals("BRAF ", annotateGene("[[gene]] ", "BRAF"));
        assertEquals(" BRAF ", annotateGene(" [[gene]] ", "BRAF"));
        assertEquals(" ", annotateGene(" ", "BRAF"));
        assertEquals("  ", annotateGene("  ", "BRAF"));
        assertEquals("", annotateGene(null, "BRAF"));
    }
}
