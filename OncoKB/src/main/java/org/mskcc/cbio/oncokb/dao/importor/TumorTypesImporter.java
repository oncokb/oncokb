/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.importor;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author jgao
 */
public class TumorTypesImporter {
    private TumorTypesImporter() {
        throw new AssertionError();
    }
    
    private static String TUMOR_TYPES_FILE = "/data/tumor-types.txt";
    
    public static void main(String[] args) throws IOException {
        List<String> lines = FileUtils.readLinesStream(
                TumorTypesImporter.class.getResourceAsStream(TUMOR_TYPES_FILE));
        String[] headers = lines.get(0).split("\t");
              
        ApplicationContext appContext = 
    		ApplicationContextSingleton.getApplicationContext();
	
    	TumorTypeBo tumorTypeBo = TumorTypeBo.class.cast(appContext.getBean("tumorTypeBo"));
        
        int nLines = lines.size();
        System.out.println("importing...");
        for (int i=1; i<nLines; i++) {
            String line = lines.get(i);
            String[] parts = line.split("\t");
            
            String id = parts[0];
            String name = parts[1];
            String shortName = parts[2];
            String color = parts[3];
            
            TumorType tumorType = new TumorType(id, name, shortName, color);
            
            tumorTypeBo.saveOrUpdate(tumorType);
        }
        
    }
}
