package org.mskcc.cbio.oncokb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class TestUtils {
    static public BufferedReader getTestFileBufferedReader(String filePath) throws FileNotFoundException {
        if (filePath == null) {
            System.out.println("Please specify the testing file path");
            return null;
        }

        File file = new File(filePath);
        FileReader reader = new FileReader(file);
        return new BufferedReader(reader);
    }
}
