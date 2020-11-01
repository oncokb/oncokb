package org.mskcc.cbio.oncokb.util;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 * @author jgao
 */
public final class GeneAnnotator {
    private GeneAnnotator() {
        throw new AssertionError();
    }

    private static final String URL_MY_GENE_INFO_3 = "http://mygene.info/v3/";
    private static final String CBIOPORTAL_GENES_ENDPOINT = "https://www.cbioportal.org/api/genes/";


    public static Gene findGene(String symbol) {
        Gene gene = findGeneFromCBioPortal(symbol);
        if (gene == null) {
            System.out.println("The gene does not exist in cBioPortal, looking in MyGeneInfo");
            try {
                gene = readByHugoSymbol(symbol);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (gene != null) {
            // Swap the hugo symbol with gene alias so that the gene is always using the hugo symbol from input
            if (!gene.getHugoSymbol().equals(symbol) && !StringUtils.isNumeric(symbol)) {
                gene.getGeneAliases().add(gene.getHugoSymbol());
                gene.getGeneAliases().remove(symbol);
                gene.setHugoSymbol(symbol);
            }
        }
        return gene;
    }

    private static Gene findGeneFromCBioPortal(String symbol) {
        try {
            String response = HttpUtils.getRequest(CBIOPORTAL_GENES_ENDPOINT + symbol);
            JSONObject jsonObj = new JSONObject(response);
            if (!jsonObj.has("hugoGeneSymbol") || !jsonObj.has("entrezGeneId")) {
                System.out.println("The gene model is not appropriate from cBioPortal" + response);
                return null;
            } else {
                Gene gene = new Gene();
                gene.setHugoSymbol(jsonObj.getString("hugoGeneSymbol"));
                gene.setEntrezGeneId(jsonObj.getInt("entrezGeneId"));

                includeGeneAlias(gene);

                return gene;
            }
        } catch (IOException e) {
            System.out.println("Something goes wrong while fetching cBioPortal service");
            e.printStackTrace();
            return null;
        }
    }

    private static Gene readByHugoSymbol(String symbol) throws IOException {
        String url = URL_MY_GENE_INFO_3 + "query?species=human&q=" + symbol;
        String json = FileUtils.readRemote(url);

        List<Gene> genes = parseMyGeneResponse(json);

        if (genes.isEmpty()) {
            return null;
        }

        if (genes.size() > 1) {
            System.out.println("More than one hits:\n" + url);
        }

        Gene gene = genes.get(0);
        includeGeneAlias(gene);

        return gene;
    }

    private static List<Gene> parseMyGeneResponse(String json) {
        final String HITS = "hits";
        final String ENTREZ_GENE_KEY = "entrezgene";
        final String HUGO_SYMBOL_KEY = "symbol";

        JSONObject map = new JSONObject(json);
        if (!map.has(HITS)) {
            return Collections.emptyList();
        }
        JSONArray hits = map.getJSONArray(HITS);

        List<Gene> genes = new ArrayList<>();
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i);
            if (hit.has(ENTREZ_GENE_KEY) && hit.has(HUGO_SYMBOL_KEY)) {
                int entrez = hit.getInt(ENTREZ_GENE_KEY);
                String symbol = hit.getString(HUGO_SYMBOL_KEY);

                Gene gene = new Gene(entrez, symbol);
                genes.add(gene);
            }
        }

        return genes;
    }

    private static void includeGeneAlias(Gene gene) throws IOException {
        String url = URL_MY_GENE_INFO_3 + "gene/" + gene.getEntrezGeneId()
            + "?fields=alias";
        String json = FileUtils.readRemote(url);

        Map<String, Object> map = JsonUtils.jsonToMap(json);
        Object objAlias = map.get("alias");
        if (objAlias != null) {
            Set<String> aliases = new HashSet<>();
            if (objAlias instanceof String) {
                String alias = String.class.cast(objAlias);
                aliases.add(alias);
            } else if (objAlias instanceof List) {
                List<String> aliasList = List.class.cast(objAlias);
                aliases.addAll(aliasList);
            }
            gene.setGeneAliases(aliases);
        }
    }
}
