package org.mskcc.cbio.oncokb;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mskcc.cbio.oncokb.util.GzipUtils;

/**
 * Created by Yifu Yao on 3/16/2021
 */
public class GzipUtilsTest {

    private String testDir =
        System.getProperty("user.dir").replaceAll("\\\\", "/") +
        "/src/test/resources";

    @Test
    public void fileTest() throws IOException {
        String orginal = testDir + "/test_gzip_file.json";
        String compress = testDir + "/test_gzip_file.json.gz";
        String decompress = testDir + "/test_gzip_file_1.json";
        File orginalFile = new File(orginal);
        File compressFile = new File(compress);
        File decompressFile = new File(decompress);
        if (!compressFile.exists()) {
            compressFile.createNewFile();
        }
        if (!decompressFile.exists()) {
            decompressFile.createNewFile();
        }
        GzipUtils.compress(orginalFile, compressFile);
        GzipUtils.deCompress(compressFile, decompressFile);
        assertTrue(
            "File test filed",
            FileUtils.contentEquals(orginalFile, decompressFile)
        );
    }

    @Test
    public void stringTest() throws IOException {
        String orginal = testDir + "/test_gzip_file.json";
        String compress = testDir + "/test_gzip_file.json.gz";
        String decompress = testDir + "/test_gzip_file_1.json";

        GzipUtils.compress(orginal, compress);
        GzipUtils.deCompress(compress, decompress);

        File orginalFile = new File(orginal);
        File decompressFile = new File(decompress);
        assertTrue(
            "File name test failed",
            FileUtils.contentEquals(orginalFile, decompressFile)
        );
    }

    @Test
    public void byteTest() throws IOException {
        String test =
            "oncokb @ mskcc.org oncokb @ mskcc.org oncokb @ mskcc.org oncokb @ mskcc.org oncokb @ mskcc.org";

        System.out.println("compress before:" + test.getBytes().length);
        byte[] bytes1 = GzipUtils.compress(test.getBytes());
        System.out.println("compress after:" + bytes1.length);
        byte[] bytes2 = GzipUtils.deCompress(bytes1);
        System.out.println("decompress after:" + bytes2.length);
        assertTrue(
            "Byte test failed",
            test.equals(new String(bytes2, StandardCharsets.UTF_8))
        );
    }
}
