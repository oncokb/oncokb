/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;

/**
 *
 * @author jgao
 */
public class TumorTypeImporter {
    private TumorTypeImporter() {
        throw new AssertionError();
    }
    
    private static final String TUMOR_TYPES_FILE = "/data/tumor-types.txt";
    
    public static void main(String[] args) throws IOException {
        List<String> lines = FileUtils.readTrimedLinesStream(
                TumorTypeImporter.class.getResourceAsStream(TUMOR_TYPES_FILE));
        String[] headers = lines.get(0).split("\t");
	
    	TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        
        int nLines = lines.size();
        System.out.println("importing...");
        for (int i=1; i<nLines; i++) {
            String line = lines.get(i);
            String[] parts = line.split("\t");
            
            String id = parts[0];
            String name = parts[1];
            String tissue = parts[2];
            String clinicalTrialKeywordsStr = parts[3];
            
            TumorType tumorType = new TumorType();
            tumorType.setTumorTypeId(id);
            tumorType.setName(name);
            if (!tissue.isEmpty())
                tumorType.setTissue(tissue);
            if (!clinicalTrialKeywordsStr.isEmpty()) {
                Set<String> keywords = new HashSet<String>(Arrays.asList(clinicalTrialKeywordsStr.split(",")));
                keywords.add(name);
                tumorType.setClinicalTrialKeywords(keywords);
            }
            
            tumorTypeBo.saveOrUpdate(tumorType);
        }
        
    }
}
