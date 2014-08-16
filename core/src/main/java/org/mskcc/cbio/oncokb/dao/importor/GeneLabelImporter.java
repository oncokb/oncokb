/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.importor;

import org.mskcc.cbio.oncokb.util.GeneAnnotatorMyGeneInfo2;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author jgao
 */
public final class GeneLabelImporter {
    private GeneLabelImporter() {
        throw new AssertionError();
    }
    
    private static String GENE_LABEL_FILE = "/data/gene-label.txt";
    
    public static void main(String[] args) throws IOException {
        List<String> lines = FileUtils.readTrimedLinesStream(
                GeneLabelImporter.class.getResourceAsStream(GENE_LABEL_FILE));
        String[] headers = lines.get(0).split("\t");
	
    	GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        
        int nLines = lines.size();
        System.out.println("importing...");
        for (int i=1; i<nLines; i++) {
            String line = lines.get(i);
            String[] parts = line.split("\t");
            
            System.out.println(i+"/"+nLines+": "+parts[0]+" ("+parts[1]+")");
            
            int entrez = Integer.parseInt(parts[0]);
            
            Gene gene = GeneAnnotatorMyGeneInfo2.readByEntrezId(entrez);
            if (gene==null) {
                System.err.println("No gene information for entrez: " + entrez
                        + "(" + parts[1] + ") from gene.info");
                continue;
            }

            if (!gene.getHugoSymbol().equals(parts[1])) {
                System.out.println("HUGO symbol changed from " + parts[1]
                        + " to " + gene.getHugoSymbol());
            }
            
            Set<String> labels = new HashSet<String>();
            for (int j=2; j<parts.length; j++) {
                if (parts[j].equals("1")) {
                    labels.add(headers[j]);
                }
            }
            gene.setGeneLabels(labels);
            
            geneBo.saveOrUpdate(gene);
        }
        
    }
}
