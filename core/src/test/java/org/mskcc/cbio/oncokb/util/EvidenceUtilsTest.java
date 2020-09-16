package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.tumor_type.MainType;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;

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
        MainType mainType = new MainType();
        mainType.setName("Melanoma");
        tumorType.setName("Melanoma");

        e1.setOncoTreeType(tumorType);
        e2.setOncoTreeType(tumorType);
        e3.setOncoTreeType(tumorType);
        e4.setOncoTreeType(tumorType);
        e5.setOncoTreeType(tumorType);

        Drug d1 = new Drug("Vemurafinib");
        Drug d2 = new Drug("Dabrafinib");

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
}
