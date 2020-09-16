package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.apache.commons.lang3.AnnotationUtils;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;

/**
 * Created by Hongxin Zhang on 6/20/18.
 */
public class AlterationUtilsTest extends TestCase

{
    public void testAnnotateAlteration() throws Exception {
    }

    public void testIsFusion() throws Exception {
    }

    public void testGetRevertFusions() throws Exception {
        Alteration alteration = createBRAFAlteration("BRAF-MKRN1 fusion");

        // Check when alteration is not available
        Set<Alteration> fullAlterations = new HashSet<>();
        Alteration result = AlterationUtils.getRevertFusions(DEFAULT_REFERENCE_GENOME, alteration, fullAlterations);
        assertEquals("The result should be null", null, result);

        fullAlterations = new HashSet<>();
        fullAlterations.add(createBRAFAlteration("BRAF-MKRN1 fusion"));
        result = AlterationUtils.getRevertFusions(DEFAULT_REFERENCE_GENOME, alteration, fullAlterations);
        assertEquals("The result should be null", null, result);

        fullAlterations = new HashSet<>();
        fullAlterations.add(createBRAFAlteration("MKRN1-BRAF fusion"));
        result = AlterationUtils.getRevertFusions(DEFAULT_REFERENCE_GENOME, alteration, fullAlterations);
        assertTrue("The result should not be null", result != null);

    }

    private Alteration createBRAFAlteration(String alterationName) {
        Gene gene = GeneUtils.getGeneByHugoSymbol("BRAF");
        Alteration alteration = new Alteration();
        alteration.setGene(gene);
        alteration.setAlteration(alterationName);
        AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());
        return alteration;
    }

    public void testTrimAlterationName() throws Exception {
    }

    public void testGetAlteration() throws Exception {
    }

    public void testGetAlterationByHGVS() throws Exception {
    }

    public void testGetOncogenic() throws Exception {
    }

    public void testGetAllAlterations() throws Exception {
    }

    public void testGetAllAlterations1() throws Exception {
    }

    public void testGetTruncatingMutations() throws Exception {
    }

    public void testFindVUSFromEvidences() throws Exception {
    }

    public void testExcludeVUS() throws Exception {
    }

    public void testExcludeVUS1() throws Exception {
    }

    public void testExcludeInferredAlterations() throws Exception {
    }

    public void testExcludePositionedAlterations() throws Exception {
    }

    public void testIsInferredAlterations() throws Exception {
    }

    public void testIsLikelyInferredAlterations() throws Exception {
    }

    public void testGetAlterationsByKnownEffectInGene() throws Exception {
    }

    public void testGetInferredAlterationsKnownEffect() throws Exception {
    }

    public void testGetAlleleAlterations() throws Exception {
    }

    public void testGetAlleleAlterations1() throws Exception {
    }

    public void testGetPositionedAlterations() throws Exception {
        Alteration alteration = new Alteration();
        alteration.setGene(GeneUtils.getGeneByHugoSymbol("BRAF"));
        alteration.setAlteration("V600E");
        AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());

        List<Alteration> positionedAlterations = AlterationUtils.getPositionedAlterations(DEFAULT_REFERENCE_GENOME, alteration);
        List<String> alterations = new ArrayList<>();
        for (Alteration alt : positionedAlterations) {
            alterations.add(alt.getAlteration());
        }
        assertEquals("V600", MainUtils.listToString(alterations, ","));

        // non missense should not be annotated
        alteration = new Alteration();
        alteration.setGene(GeneUtils.getGeneByHugoSymbol("BRAF"));
        alteration.setAlteration("V600del");
        AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());

        positionedAlterations = AlterationUtils.getPositionedAlterations(DEFAULT_REFERENCE_GENOME, alteration);
        alterations = new ArrayList<>();
        for (Alteration alt : positionedAlterations) {
            alterations.add(alt.getAlteration());
        }
        assertEquals("", MainUtils.listToString(alterations, ","));
    }

    public void testGetPositionedAlterations1() throws Exception {
    }

    public void testGetUniqueAlterations() throws Exception {
    }

    public void testLookupVariant() throws Exception {
    }

    public void testGetAlleleAndRelevantAlterations() throws Exception {
    }

    public void testFindOncogenicAllele() throws Exception {
    }

    public void testGetRelevantAlterations() throws Exception {
        // Check when the consequence is not exactly matched to the alteration in the DB even the alteration is the same
        Gene gene = GeneUtils.getGeneByHugoSymbol("PIK3R1");
        Alteration alteration = new Alteration();
        alteration.setAlteration("W583del");
        alteration.setGene(gene);
        alteration.setConsequence(VariantConsequenceUtils.findVariantConsequenceByTerm("splice_region_variant"));
        AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());

        List<Alteration> alterations = AlterationUtils.getRelevantAlterations(DEFAULT_REFERENCE_GENOME, alteration);
        String relevantAltsName = AlterationUtils.toString(alterations);
        assertEquals("The relevant alterations do not match", "Truncating Mutations", relevantAltsName);


        // if the consequence matches with the alteration in DB
        alteration = new Alteration();
        alteration.setAlteration("W583del");
        alteration.setGene(gene);
        AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());

        alterations = AlterationUtils.getRelevantAlterations(DEFAULT_REFERENCE_GENOME, alteration);
        relevantAltsName = AlterationUtils.toString(alterations);
        assertEquals("The relevant alterations do not match", "W583del", relevantAltsName);
    }

    public void testHasAlleleAlterations() throws Exception {
    }

    public void testFindAlteration() throws Exception {
    }

    public void testIsOncogenicAlteration() throws Exception {
    }

    public void testHasImportantCuratedOncogenicity() throws Exception {
    }

    public void testHasOncogenic() throws Exception {
    }

    public void testGetCuratedOncogenicity() throws Exception {
    }

    public void testGetOncogenicMutations() throws Exception {
    }

    public void testGetGeneralVariants() throws Exception {
    }

    public void testGetInferredMutations() throws Exception {
    }

    public void testGetStructuralAlterations() throws Exception {
    }

    public void testIsPositionedAlteration() throws Exception {
    }

    public void testIsGeneralAlterations() throws Exception {
    }

    public void testIsGeneralAlterations1() throws Exception {
    }

    public void testSortAlterationsByTheRange() throws Exception {
        int start = 8;
        int end = 8;
        Integer[] starts = {0, 8, 8, null, 8};
        Integer[] ends = {10, 9, 8, 8, null};
        List<Alteration> alterationList = new ArrayList<>();
        for (int i = 0; i < starts.length; i++) {
            Alteration alt = new Alteration();
            alt.setProteinStart(starts[i]);
            alt.setProteinEnd(ends[i]);
            alt.setName(Integer.toString(i));
            alterationList.add(alt);
        }
        AlterationUtils.sortAlterationsByTheRange(alterationList, start, end);
        assertEquals(5, alterationList.size());
        assertEquals(alterationList.get(0).getProteinStart().intValue(), 8);
        assertEquals(alterationList.get(0).getProteinEnd().intValue(), 8);
        assertEquals(alterationList.get(1).getProteinStart().intValue(), 8);
        assertEquals(alterationList.get(1).getProteinEnd().intValue(), 9);
        assertEquals(alterationList.get(2).getProteinStart().intValue(), 0);
        assertEquals(alterationList.get(2).getProteinEnd().intValue(), 10);
        assertEquals(alterationList.get(3).getProteinStart(), null);
        assertEquals(alterationList.get(3).getProteinEnd().intValue(), 8);
        assertEquals(alterationList.get(4).getProteinStart().intValue(), 8);
        assertEquals(alterationList.get(4).getProteinEnd(), null);
    }

    public void testIsPositionVariant() throws Exception {
        Alteration alteration = AlterationUtils.getAlteration("AKT1", "E17", null, "NA", null, null);
        assertTrue("This variant should be position variant.", AlterationUtils.isPositionedAlteration(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17", null, null, null, null);
        assertTrue("This variant should be position variant.", AlterationUtils.isPositionedAlteration(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17*", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionedAlteration(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "EE17*", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionedAlteration(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "EE17", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionedAlteration(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "17", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionedAlteration(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionedAlteration(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionedAlteration(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17A", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionedAlteration(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17AA", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionedAlteration(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "EE17AA", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionedAlteration(alteration));

        alteration = AlterationUtils.getAlteration("EGFR", "L747Rfs*13", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionedAlteration(alteration));

    }

    public void testFindOverlapAlteration() {
    }

    public void testGetAlterationFromGenomeNexus() {
    }

    public void testGetEvidencesAlterations() {
    }

    public void testRemoveAlterationsFromList() {
    }

    public void testRemoveAlternativeAllele() {
        // Test regular missense variant
        Alteration v600e = createBRAFAlteration("V600E");

        // Check when alteration is not available
        List<Alteration> relevantAlterations = new ArrayList<>();
        Alteration alteration = createBRAFAlteration("V600G");
        relevantAlterations.add(alteration);
        alteration = createBRAFAlteration("Oncogenic Mutations");
        relevantAlterations.add(alteration);

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME, v600e, relevantAlterations);

        assertEquals(1, relevantAlterations.size());


        // Check when alteration is present in list
        relevantAlterations = new ArrayList<>();
        alteration = createBRAFAlteration("V600G");
        relevantAlterations.add(alteration);
        relevantAlterations.add(v600e);

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME, v600e, relevantAlterations);
        assertEquals(1, relevantAlterations.size());

        // Check when it's a mix
        relevantAlterations = new ArrayList<>();

        alteration = createBRAFAlteration("V600G");
        relevantAlterations.add(alteration);

        alteration = createBRAFAlteration("Oncogenic Mutations");
        relevantAlterations.add(alteration);

        relevantAlterations.add(v600e);

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME, v600e, relevantAlterations);
        assertEquals(2, relevantAlterations.size());

        // Check delins match missense
        relevantAlterations = new ArrayList<>();

        alteration = createBRAFAlteration("V600_V601delinsEK");
        relevantAlterations.add(alteration);

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME, v600e, relevantAlterations);
        assertEquals(1, relevantAlterations.size());

        // Check delins match missense - match
        relevantAlterations = new ArrayList<>();

        alteration = createBRAFAlteration("V599_V600delinsKE");
        relevantAlterations.add(alteration);

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME, v600e, relevantAlterations);
        assertEquals(1, relevantAlterations.size());

        // Check delins match missense - does not match
        relevantAlterations = new ArrayList<>();
        relevantAlterations.add(createBRAFAlteration("V599_V600delinsKK"));

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME, v600e, relevantAlterations);
        assertEquals(0, relevantAlterations.size());

        // Check multi-residues match missense - match
        relevantAlterations = new ArrayList<>();

        alteration = createBRAFAlteration("VK600EI");
        relevantAlterations.add(alteration);

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME, v600e, relevantAlterations);
        assertEquals(1, relevantAlterations.size());

        // Check multi-residues match missense - does not match
        relevantAlterations = new ArrayList<>();

        alteration = createBRAFAlteration("VK600EI");
        relevantAlterations.add(alteration);

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME,createBRAFAlteration("V600K"), relevantAlterations);
        assertEquals(0, relevantAlterations.size());

        // Check missense match delins
        Alteration delins = createBRAFAlteration("V599_V600delinsKE");

        relevantAlterations = new ArrayList<>();
        relevantAlterations.add(v600e);

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME,delins, relevantAlterations);
        assertEquals(1, relevantAlterations.size());

        // Check missense match delins plus mix
        relevantAlterations = new ArrayList<>();
        relevantAlterations.add(v600e);
        relevantAlterations.add(createBRAFAlteration("V600R"));
        relevantAlterations.add(createBRAFAlteration("Oncogenic Mutations"));

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME,delins, relevantAlterations);
        assertEquals(2, relevantAlterations.size());

        // Check missense match delins plus positional
        relevantAlterations = new ArrayList<>();
        relevantAlterations.add(v600e);
        relevantAlterations.add(createBRAFAlteration("V600"));

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME,delins, relevantAlterations);
        assertEquals(2, relevantAlterations.size());


        // Check missense match delins plus positional plus multi-residues missense
        relevantAlterations = new ArrayList<>();
        relevantAlterations.add(v600e);
        relevantAlterations.add(createBRAFAlteration("V600"));
        relevantAlterations.add(createBRAFAlteration("VK600EI"));

        AlterationUtils.removeAlternativeAllele(DEFAULT_REFERENCE_GENOME,delins, relevantAlterations);
        assertEquals(3, relevantAlterations.size());
    }
}
