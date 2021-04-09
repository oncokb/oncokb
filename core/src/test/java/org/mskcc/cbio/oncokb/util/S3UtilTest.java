package org.mskcc.cbio.oncokb.util;

import org.junit.Test;

public class S3UtilTest {

    @Test
    public void testS3Utils() {
        System.out.println(S3Utils.getInstance().isPropertiesConfigured());
    }
}
