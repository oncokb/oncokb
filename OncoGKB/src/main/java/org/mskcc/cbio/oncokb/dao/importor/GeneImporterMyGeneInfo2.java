/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.importor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.impl.GeneImpl;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.mskcc.cbio.oncokb.util.JsonUtils;

/**
 *
 * @author jgao
 */
public final class GeneImporterMyGeneInfo2 {
    private GeneImporterMyGeneInfo2() {
        throw new AssertionError();
    }
    
    private static final String URL_MY_GENE_INFO_2 = "http://mygene.info/v2/";
    
    public static Gene readByEntrezId(int entrezId) throws IOException {
        String url = URL_MY_GENE_INFO_2 + "query?species=human&q=entrezgene:" + entrezId;
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
        String url = URL_MY_GENE_INFO_2 + "query?species=human&q=" + symbol;
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
            
            Gene gene = new GeneImpl(entrez, symbol, name);
            genes.add(gene);
        }
        
        return genes;
    }
    
    private static void annotateGene(Gene gene) throws IOException {
        String url = URL_MY_GENE_INFO_2 + "gene/" + gene.getEntrezGeneId()
                + "?fields=summary,alias";
        String json = FileUtils.readRemote(url);
        
        Map<String,Object> map = JsonUtils.jsonToMap(json);
        Object objSummary = map.get("summary");
        if (objSummary!=null) {
            gene.setSummary(String.class.cast(objSummary));
        }
        
        Object objAlias = map.get("alias");
        if (objAlias!=null) {
            if (objAlias instanceof String) {
                String alias = String.class.cast(objAlias);
                HashSet<String> aliases = new HashSet<String>(1);
                aliases.add(alias);
                gene.setGeneAliases(aliases);
            } else if (objAlias instanceof List) {
                List<String> aliases = List.class.cast(objAlias);
                gene.setGeneAliases(new HashSet<String>(aliases));
            }
        }
    }
}
