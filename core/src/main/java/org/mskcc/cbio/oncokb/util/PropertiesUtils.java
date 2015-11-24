/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author jgao
 */
public final class PropertiesUtils {
    private static Properties PROPERTIES;
    private PropertiesUtils() {
        throw new AssertionError();
    }

    public static String getProperties(String name) throws IOException {
        if(name == null) {
            return null;
        }

        if(PROPERTIES == null) {
            String propFileName = "properties/config.properties";
            PROPERTIES = new Properties();
            InputStream inputStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                PROPERTIES.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
            inputStream.close();
        }

        return PROPERTIES.get(name) == null ? null : (String)PROPERTIES.get(name);
    }
}
