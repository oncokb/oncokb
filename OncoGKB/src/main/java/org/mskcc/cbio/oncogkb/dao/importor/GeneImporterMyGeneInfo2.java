/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.dao.importor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncogkb.model.Gene;
import org.mskcc.cbio.oncogkb.util.FileUtils;
import org.mskcc.cbio.oncogkb.util.JsonUtils;

/**
 *
 * @author jgao
 */
public final class GeneImporterMyGeneInfo2 {
    private GeneImporterMyGeneInfo2() {
        throw new AssertionError();
    }
    
    private static final String urlMyGeneInfo2 = "http://mygene.info/v2/";
    
    public static Gene readByEntrezId(int entrezId) throws IOException {
        String url = urlMyGeneInfo2 + "query?species=human&q=entrezgene:" + entrezId;
        String json = FileUtils.readRemote(url);
        
        List<Gene> genes = parseGenes(json);
        
        if (genes.isEmpty()) {
            return null;
        }
        
        if (genes.size()>1) {
            System.out.println("More than one hits:\n"+url);
        }
        
        Gene gene = genes.get(0);
        annotateGene(gene);
        
        return gene;
    }
    
    public static List<Gene> readBySymbol(String symbol) throws IOException {
        String url = urlMyGeneInfo2 + "query?species=human&q=" + symbol;
        String json = FileUtils.readRemote(url);
        
        List<Gene> genes = parseGenes(json);
        for (Gene gene : genes) {
            annotateGene(gene);
        }
        
        return genes;
    }
    
    private static List<Gene> parseGenes(String json) throws IOException {
        Map<String,Object> map = JsonUtils.jsonToMap(json);
        Object objHits = map.get("hits");
        if (objHits==null) {
            return Collections.emptyList();
        }
        
        List<Map<String,Object>> hits = List.class.cast(objHits);
        
        List<Gene> genes = new ArrayList<Gene>();
        for (Map<String,Object> hit : hits) {
            int entrez = Integer.class.cast(hit.get("entrezgene")).intValue();
            String symbol = String.class.cast(hit.get("symbol"));
            String name = String.class.cast(hit.get("name"));
            
            Gene gene = new Gene(entrez, symbol, name);
            genes.add(gene);
        }
        
        return genes;
    }
    
    private static void annotateGene(Gene gene) throws IOException {
        String url = urlMyGeneInfo2 + "gene/" + gene.getEntrezGeneId();
        String json = FileUtils.readRemote(url);
        
        Map<String,Object> map = JsonUtils.jsonToMap(json);
        Object objSummary = map.get("summary");
        if (objSummary!=null) {
            gene.setSummary(String.class.cast(objSummary));
        }
        
        Object objAlias = map.get("alias");
        if (objAlias!=null) {
            if (objAlias instanceof String) {
                gene.setAliases(String.class.cast(objAlias));
            } else if (objAlias instanceof List) {
                List<String> aliases = List.class.cast(objAlias);
                gene.setAliases(StringUtils.join(aliases, ", "));
            }
        }
    }
}
