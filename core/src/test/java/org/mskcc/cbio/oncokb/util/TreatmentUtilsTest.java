package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.Treatment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Hongxin Zhang on 10/24/18.
 */
public class TreatmentUtilsTest extends TestCase {
    public void testGetTreatmentsByGene() throws Exception {
    }

    public void testGetTreatmentsByAlteration() throws Exception {
    }

    public void testGetTreatmentsByLevels() throws Exception {
    }

    public void testGetTreatmentName() throws Exception {
    }

    public void testSortTreatmentsByName() throws Exception {
        Drug d1 = new Drug("D1");
        Drug d2 = new Drug("D2");
        Drug d3 = new Drug("D3");
        Drug d4 = new Drug("D4");
        Treatment t1 = new Treatment();
        Treatment t2 = new Treatment();

        t1.setDrugs(new ArrayList<>(Collections.singleton(d1)));
        t2.setDrugs(new ArrayList<>(Collections.singleton(d2)));

        List<Treatment> treatmentList = new ArrayList<>();
        treatmentList.add(t1);
        treatmentList.add(t2);

        TreatmentUtils.sortTreatmentsByName(treatmentList);

        assertEquals(treatmentList.get(0).getDrugs().get(0).getDrugName(), "D1");
        assertEquals(treatmentList.get(1).getDrugs().get(0).getDrugName(), "D2");


        treatmentList = new ArrayList<>();
        treatmentList.add(t2);
        treatmentList.add(t1);

        TreatmentUtils.sortTreatmentsByName(treatmentList);

        assertEquals(treatmentList.get(0).getDrugs().get(0).getDrugName(), "D1");
        assertEquals(treatmentList.get(1).getDrugs().get(0).getDrugName(), "D2");

    }

    public void testGetTreatments() throws Exception {
    }

    public void testGetTreatmentsByGeneAndLevels() throws Exception {
    }

}
