package org.mskcc.cbio.oncokb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class TestUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    static public BufferedReader getTestFileBufferedReader(String filePath) throws FileNotFoundException {
        if (filePath == null) {
            LOGGER.info("Please specify the testing file path");
            return null;
        }

        File file = new File(filePath);
        FileReader reader = new FileReader(file);
        return new BufferedReader(reader);
    }
}
