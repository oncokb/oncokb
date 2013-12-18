/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jgao
 */
public class FileUtils {
    private FileUtils() {
        throw new AssertionError();
    }
    
    /**
     * read local files and return content
     * @param pathToFile
     * @return
     * @throws IOException 
     */
    public static String readLocal(String pathToFile) throws IOException {
        return readStream(new FileInputStream(pathToFile));
    }
    
    /**
     * return remote files and return content
     * @param urlToFile
     * @return
     * @throws IOException 
     */
    public static String readRemote(String urlToFile) throws IOException {
        URL url = new URL(urlToFile);
        return readStream(url.openStream());
    }
    
    /**
     * read a stream and return content
     * @param is
     * @return
     * @throws IOException 
     */
    public static String readStream(InputStream is) throws IOException {
        List<String> lines = readLinesStream(is);
        return StringUtils.join(lines, "\n");
    }
    
    /**
     * read a stream and return lines
     * @param is
     * @return
     * @throws IOException 
     */
    public static List<String> readLinesStream(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        List<String> lines = new ArrayList<String>();
        String line;
        while ((line = in.readLine()) != null) {
            lines.add(line);
        }
        in.close();
        
        return lines;
    }
}
