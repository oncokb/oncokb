package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Coordinates;

public class OpenStreetMapUtilsTest extends TestCase {

    public void testOpenStreetMapUtils() throws Exception {
        String address1 = "65202, USA";
        Coordinates coord1 = OpenStreetMapUtils
            .getInstance()
            .getCoordinates(address1);
        Coordinates exp1 = new Coordinates();
        exp1.setLat(38.99686041183989);
        exp1.setLon(-92.31136395450818);
        assertEquals("Get coordinates by zip code has error", exp1, coord1);

        String address2 = "New York City, NY, United States";
        Coordinates coord2 = OpenStreetMapUtils
            .getInstance()
            .getCoordinates(address2);
        Coordinates exp2 = new Coordinates();
        exp2.setLat(40.7127281);
        exp2.setLon(-74.0060152);
        assertEquals("Get coordinates by address has error", exp2, coord2);

        double exp3 = 1571.2669660632407;
        double dis1 = OpenStreetMapUtils
            .getInstance()
            .calculateDistance(coord1, coord2);
        assertEquals(
            "Get distance between two coordinates has error",
            exp3,
            dis1
        );
    }
}
