package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.*;

import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;

/**
 * Created by Hongxin on 3/8/17.
 */
public class EvidenceUtilsTest extends TestCase {
    public void testSortTumorTypeEvidenceBasedNumOfAlts() throws Exception {
        Evidence e1 = new Evidence();
        e1.setId(1);
        Evidence e2 = new Evidence();
        e2.setId(2);
        Alteration a1 = new Alteration();
        a1.setAlteration("a");
        Alteration a2 = new Alteration();
        a1.setAlteration("b");

        Set<Alteration> alts = new HashSet<>();
        alts.add(a1);
        alts.add(a2);

        List<Evidence> evidenceList = new ArrayList<>();

        e1.setAlterations(Collections.singleton(a1));
        evidenceList.add(e1);

        e2.setAlterations(alts);
        evidenceList.add(e2);

        evidenceList = EvidenceUtils.sortTumorTypeEvidenceBasedNumOfAlts(evidenceList, false);
        assertEquals(1, evidenceList.get(0).getAlterations().size());
        assertEquals(2, evidenceList.get(1).getAlterations().size());


        evidenceList = EvidenceUtils.sortTumorTypeEvidenceBasedNumOfAlts(evidenceList, true);
        assertEquals(2, evidenceList.get(0).getAlterations().size());
        assertEquals(1, evidenceList.get(1).getAlterations().size());


        // If one of the alterations is null
        evidenceList = new ArrayList<>();
        e1.setAlterations(null);
        evidenceList.add(e1);

        e2.setAlterations(alts);
        evidenceList.add(e2);

        evidenceList = EvidenceUtils.sortTumorTypeEvidenceBasedNumOfAlts(evidenceList, false);
        assertEquals(2, evidenceList.get(0).getId().intValue());
        assertEquals(1, evidenceList.get(1).getId().intValue());

        evidenceList = EvidenceUtils.sortTumorTypeEvidenceBasedNumOfAlts(evidenceList, true);
        assertEquals(2, evidenceList.get(0).getId().intValue());
        assertEquals(1, evidenceList.get(1).getId().intValue());


        // if evidences has same number of alterations, the original order should be kept.
        Evidence e3 = new Evidence();
        e3.setId(3);

        evidenceList = new ArrayList<>();
        e3.setAlterations(alts);
        evidenceList.add(e3);

        e1.setAlterations(Collections.singleton(a1));
        evidenceList.add(e1);

        e2.setAlterations(alts);
        evidenceList.add(e2);

        evidenceList = EvidenceUtils.sortTumorTypeEvidenceBasedNumOfAlts(evidenceList, true);
        assertEquals(3, evidenceList.get(0).getId().intValue());
        assertEquals(2, evidenceList.get(1).getId().intValue());
        assertEquals(1, evidenceList.get(2).getId().intValue());

        evidenceList = EvidenceUtils.sortTumorTypeEvidenceBasedNumOfAlts(evidenceList, false);
        assertEquals(1, evidenceList.get(0).getId().intValue());
        assertEquals(3, evidenceList.get(1).getId().intValue());
        assertEquals(2, evidenceList.get(2).getId().intValue());

        evidenceList = new ArrayList<>();
        evidenceList.add(e1);
        evidenceList.add(e2);
        evidenceList.add(e3);

        evidenceList = EvidenceUtils.sortTumorTypeEvidenceBasedNumOfAlts(evidenceList, true);
        assertEquals(2, evidenceList.get(0).getId().intValue());
        assertEquals(3, evidenceList.get(1).getId().intValue());
        assertEquals(1, evidenceList.get(2).getId().intValue());

        evidenceList = EvidenceUtils.sortTumorTypeEvidenceBasedNumOfAlts(evidenceList, false);
        assertEquals(1, evidenceList.get(0).getId().intValue());
        assertEquals(2, evidenceList.get(1).getId().intValue());
        assertEquals(3, evidenceList.get(2).getId().intValue());

        //Test is one of the evidence has null alterations
        evidenceList = new ArrayList<>();
        e3.setAlterations(null);
        evidenceList.add(e3);

        e1.setAlterations(Collections.singleton(a1));
        evidenceList.add(e1);

        e2.setAlterations(alts);
        evidenceList.add(e2);

        evidenceList = EvidenceUtils.sortTumorTypeEvidenceBasedNumOfAlts(evidenceList, true);
        assertEquals(2, evidenceList.get(0).getId().intValue());
        assertEquals(1, evidenceList.get(1).getId().intValue());
        assertEquals(3, evidenceList.get(2).getId().intValue());

        evidenceList = EvidenceUtils.sortTumorTypeEvidenceBasedNumOfAlts(evidenceList, false);
        assertEquals(1, evidenceList.get(0).getId().intValue());
        assertEquals(2, evidenceList.get(1).getId().intValue());
        assertEquals(3, evidenceList.get(2).getId().intValue());

    }


    public void testKeepHighestLevelForSameTreatments() throws Exception {
        Evidence e1 = new Evidence();
        Evidence e2 = new Evidence();
        Evidence e3 = new Evidence();
        Evidence e4 = new Evidence();
        Evidence e5 = new Evidence();

        Alteration alteration = AlterationUtils.findAlteration(GeneUtils.getGeneByHugoSymbol("BRAF"), DEFAULT_REFERENCE_GENOME, "V600E");

        e1.setAlterations(Collections.singleton(alteration));
        e2.setAlterations(Collections.singleton(alteration));
        e3.setAlterations(Collections.singleton(alteration));
        e4.setAlterations(Collections.singleton(alteration));
        e5.setAlterations(Collections.singleton(alteration));

        e1.setId(1);
        e2.setId(2);
        e3.setId(3);
        e4.setId(4);
        e5.setId(5);

        e1.setLevelOfEvidence(LevelOfEvidence.LEVEL_1);
        e2.setLevelOfEvidence(LevelOfEvidence.LEVEL_1);
        e3.setLevelOfEvidence(LevelOfEvidence.LEVEL_1);
        e4.setLevelOfEvidence(LevelOfEvidence.LEVEL_2);
        e5.setLevelOfEvidence(LevelOfEvidence.LEVEL_R1);

        TumorType tumorType = new TumorType();
        tumorType.setMainType("Melanoma");
        tumorType.setSubtype("Melanoma");

        e1.setCancerTypes(Collections.singleton(tumorType));
        e2.setCancerTypes(Collections.singleton(tumorType));
        e3.setCancerTypes(Collections.singleton(tumorType));
        e4.setCancerTypes(Collections.singleton(tumorType));
        e5.setCancerTypes(Collections.singleton(tumorType));

        Drug d1 = new Drug("Vemurafenib");
        Drug d2 = new Drug("Dabrafenib");

        List<Drug> dc1 = new ArrayList<>();
        List<Drug> dc2 = new ArrayList<>();
        List<Drug> dc3 = new ArrayList<>();

        dc1.add(d1);
        dc2.add(d2);

        dc3.add(d1);
        dc3.add(d2);

        Treatment t1 = new Treatment();
        Treatment t2 = new Treatment();
        Treatment t3 = new Treatment();

        t1.setDrugs(dc1);
        t2.setDrugs(dc2);
        t3.setDrugs(dc3);

        List<Treatment> tc1 = new ArrayList<>();
        List<Treatment> tc2 = new ArrayList<>();
        List<Treatment> tc3 = new ArrayList<>();
        List<Treatment> tc4 = new ArrayList<>();

        // d1
        tc1.add(t1);

        // d2
        tc2.add(t2);

        // d1+d2
        tc3.add(t3);

        // d1, d2
        tc4.add(t1);
        tc4.add(t2);

        e1.setTreatments(tc1);
        e2.setTreatments(tc2);
        e3.setTreatments(tc3);
        e4.setTreatments(tc4);
        e5.setTreatments(tc1);

        Set<Evidence> sets = new HashSet<>();
        sets.add(e1);
        sets.add(e2);

        Set<Evidence> filtered = EvidenceUtils.keepHighestLevelForSameTreatments(sets, DEFAULT_REFERENCE_GENOME, alteration);
        assertEquals("1,2", getIds(filtered));

        sets.add(e3);
        filtered = EvidenceUtils.keepHighestLevelForSameTreatments(sets, DEFAULT_REFERENCE_GENOME, alteration);
        assertEquals("1,2,3", getIds(filtered));

        sets = new HashSet<>();
        sets.add(e1);
        sets.add(e4);
        filtered = EvidenceUtils.keepHighestLevelForSameTreatments(sets, DEFAULT_REFERENCE_GENOME, alteration);
        assertEquals("1,4", getIds(filtered));

        e1.setLevelOfEvidence(LevelOfEvidence.LEVEL_3A);
        filtered = EvidenceUtils.keepHighestLevelForSameTreatments(sets, DEFAULT_REFERENCE_GENOME, alteration);
        assertEquals("4", getIds(filtered));

        sets = new HashSet<>();
        sets.add(e1);
        sets.add(e5);

        e1.setLevelOfEvidence(LevelOfEvidence.LEVEL_1);
        e5.setLevelOfEvidence(LevelOfEvidence.LEVEL_R1);
        filtered = EvidenceUtils.keepHighestLevelForSameTreatments(sets, DEFAULT_REFERENCE_GENOME, alteration);
        assertEquals("5", getIds(filtered));

        e1.setLevelOfEvidence(LevelOfEvidence.LEVEL_2);
        e5.setLevelOfEvidence(LevelOfEvidence.LEVEL_R1);
        filtered = EvidenceUtils.keepHighestLevelForSameTreatments(sets, DEFAULT_REFERENCE_GENOME, alteration);
        assertEquals("5", getIds(filtered));

        e1.setLevelOfEvidence(LevelOfEvidence.LEVEL_2);
        e5.setLevelOfEvidence(LevelOfEvidence.LEVEL_R2);
        filtered = EvidenceUtils.keepHighestLevelForSameTreatments(sets, DEFAULT_REFERENCE_GENOME, alteration);
        assertEquals("1,5", getIds(filtered));

        e1.setLevelOfEvidence(LevelOfEvidence.LEVEL_4);
        e5.setLevelOfEvidence(LevelOfEvidence.LEVEL_R2);
        filtered = EvidenceUtils.keepHighestLevelForSameTreatments(sets, DEFAULT_REFERENCE_GENOME, alteration);
        assertEquals("1,5", getIds(filtered));
    }

    private String getIds(Set<Evidence> evidences) {
        List<String> ids = new ArrayList<>();
        for (Evidence evidence : evidences) {
            ids.add(evidence.getId().toString());
        }

        Collections.sort(ids);
        return StringUtils.join(ids, ",");
    }

    public void testProcessRequest() {
        // Test with levels only
        Query query = new Query();
        List<EvidenceQueryRes> responses = EvidenceUtils.processRequest(Collections.singletonList(query), null, Collections.singleton(LevelOfEvidence.LEVEL_1), true, false);
        processRequestSuite(responses);
        assertTrue("There should be evidence returned", responses.get(0).getEvidences().stream().filter(evidence -> evidence.getLevelOfEvidence() != null && evidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_1)).findAny().isPresent());
        assertTrue("The response should only return the level 1 evidences", responses.get(0).getEvidences().stream().filter(evidence -> evidence.getLevelOfEvidence() != null && evidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_1)).count() == responses.get(0).getEvidences().size());

        // Test with gene evidence only
        query = new Query();
        responses = EvidenceUtils.processRequest(Collections.singletonList(query), Collections.singleton(EvidenceType.GENE_SUMMARY), null, true, false);
        processRequestSuite(responses);
        assertTrue("The response should only return the level 1 evidences", responses.get(0).getEvidences().stream().filter(evidence -> evidence.getEvidenceType() != null && evidence.getEvidenceType().equals(EvidenceType.GENE_SUMMARY)).count() == responses.get(0).getEvidences().size());

        // Test with mutation evidence only
        query = new Query();
        responses = EvidenceUtils.processRequest(Collections.singletonList(query), Collections.singleton(EvidenceType.MUTATION_EFFECT), null, true, false);
        processRequestSuite(responses);
        assertTrue("The response should only return the level 1 evidences", responses.get(0).getEvidences().stream().filter(evidence -> evidence.getEvidenceType() != null && evidence.getEvidenceType().equals(EvidenceType.MUTATION_EFFECT)).count() == responses.get(0).getEvidences().size());

        // Test with tumor type evidence only
        query = new Query();
        responses = EvidenceUtils.processRequest(Collections.singletonList(query), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), null, true, false);
        processRequestSuite(responses);
        assertTrue("The response should only return the level 1 evidences", responses.get(0).getEvidences().stream().filter(evidence -> evidence.getEvidenceType() != null && evidence.getEvidenceType().equals(EvidenceType.TUMOR_TYPE_SUMMARY)).count() == responses.get(0).getEvidences().size());

        // Test with Tx evidence only
        query = new Query();
        responses = EvidenceUtils.processRequest(Collections.singletonList(query), EvidenceTypeUtils.getSensitiveTreatmentEvidenceTypes(), null, true, false);
        processRequestSuite(responses);
        assertTrue("The response should only return the level 1 evidences", responses.get(0).getEvidences().stream().filter(evidence -> evidence.getEvidenceType() != null && EvidenceTypeUtils.getSensitiveTreatmentEvidenceTypes().contains(evidence.getEvidenceType())).count() == responses.get(0).getEvidences().size());

        // Test with gene only
        query = new Query();
        query.setHugoSymbol("BRAF");
        responses = EvidenceUtils.processRequest(Collections.singletonList(query), null, null, true, false);
        processRequestSuite(responses);
        assertTrue("The response should only contains BRAF evidences", responses.get(0).getEvidences().stream().filter(evidence -> evidence.getGene().getHugoSymbol().equals("BRAF")).count() == responses.get(0).getEvidences().size());

        // Test with variant only
        query = new Query();
        query.setHugoSymbol("BRAF");
        query.setAlteration("V600E");
        responses = EvidenceUtils.processRequest(Collections.singletonList(query), null, null, true, false);
        processRequestSuite(responses);

        // Test with tumor type only
        query = new Query();
        query.setTumorType("MEL");
        List<TumorType> upward = TumorTypeUtils.findRelevantTumorTypes("MEL", false, RelevantTumorTypeDirection.UPWARD);
        List<TumorType> downward = TumorTypeUtils.findRelevantTumorTypes("MEL", false, RelevantTumorTypeDirection.DOWNWARD);
        responses = EvidenceUtils.processRequest(Collections.singletonList(query), null, null, true, false);
        assertTrue("The response should only tumor type relevant evidences", responses.get(0).getEvidences().stream().filter(evidence -> {
            if (evidence.getLevelOfEvidence() != null && evidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_Dx1)) {
                return !Collections.disjoint(downward, evidence.getCancerTypes());
            } else if (evidence.getLevelOfEvidence() != null) {
                return !Collections.disjoint(upward, evidence.getCancerTypes());
            } else {
                return true;
            }
        }).count() == responses.get(0).getEvidences().size());
        processRequestSuite(responses);

    }

    private void processRequestSuite(List<EvidenceQueryRes> responses) {
        assertTrue("There should only be one query response", responses.size() == 1);
        assertTrue("There should be evidences associated", responses.get(0).getEvidences().size() > 0);
    }
}
