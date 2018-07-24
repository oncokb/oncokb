package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.Alteration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hongxin Zhang on 6/20/18.
 */
public class AlterationUtilsTest extends TestCase
{
    public void testSortAlterationsByTheRange() throws Exception {
        Integer[] starts = {0, 8, 8, null, 8};
        Integer[] ends = {10, 9, 8, 8, null};
        List<Alteration> alterationList = new ArrayList<>();
        for(int i = 0 ; i < starts.length;i++) {
            Alteration alt = new Alteration();
            alt.setProteinStart(starts[i]);
            alt.setProteinEnd(ends[i]);
            alt.setName(Integer.toString(i));
            alterationList.add(alt);
        }
        AlterationUtils.sortAlterationsByTheRange(alterationList);
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
        assertTrue("This variant should be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17", null, null, null, null);
        assertTrue("This variant should be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17*", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "EE17*", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "EE17", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "17", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17A", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17AA", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "EE17AA", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));
    }

}
