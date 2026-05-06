package org.mskcc.cbio.oncokb.util.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class ProteinChangeParserTest {

    @Test
    public void testParseGeneralWithValidInput() {
        ParseAlterationResult result = ProteinChangeParser.parseGeneral("V600E");
        assertTrue(result.isParsed);
        assertEquals("V", result.ref);
        assertEquals(600, result.start.intValue());
        assertEquals("E", result.var);
        assertEquals("missense_variant", result.consequence);
    }

    @Test
    public void testParseGeneralWithLargeNumber() {
        ParseAlterationResult result = ProteinChangeParser.parseGeneral("V2147483647E");
        assertTrue(result.isParsed);
        assertEquals(2147483647, result.start.intValue());
    }

    @Test
    public void testParseGeneralWithTooBigNumber() {
        ParseAlterationResult result = ProteinChangeParser.parseGeneral("V600000000000000000000E");
        assertFalse(result.isParsed);
    }
}
