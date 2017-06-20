package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.CancerGene;

import java.io.IOException;
import java.util.Iterator;
import java.util.*;
import org.apache.commons.lang3.math.NumberUtils;
/**
 * Created by jiaojiao on 6/9/17.
 */
public class CancerGeneUtils {
    private static final String CANCER_GENE_FILE_PATH = "/data/cancer-gene.txt";
    public static List<CancerGene> getCancerGeneList() {
        List<CancerGene> CancerGeneList = new ArrayList<CancerGene>();
        try {
            List<String> lines = FileUtils.readTrimedLinesStream(
                    CancerGeneUtils.class.getResourceAsStream(CANCER_GENE_FILE_PATH));
            Iterator itr = lines.iterator();
            // skip the header
            itr.next();
            while(itr.hasNext()) {
                String[] line = itr.next().toString().split("\t");
                if (line.length != 10) continue;
                CancerGene item = new CancerGene();
                item.setHugoSymbol(line[0]);
                item.setEntrezGeneId(line[1]);
                item.setOncokbAnnotated(line[2].trim().equals("1"));
                item.setOccurrenceCount(NumberUtils.isNumber(line[3].trim()) ? Integer.parseInt(line[3].trim()) : 0);
                item.setmSKImpact(line[4].trim().equals("1"));
                item.setmSKHeme(line[5].trim().equals("1"));
                item.setFoundation(line[6].trim().equals("1"));
                item.setFoundationHeme(line[7].trim().equals("1"));
                item.setVogelstein(line[8].trim().equals("1"));
                item.setSangerCGC(line[9].trim().equals("1"));
                CancerGeneList.add(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CancerGeneList;
    }
}
