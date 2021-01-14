package org.mskcc.cbio.oncokb.util;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.api.client.json.Json;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.model.Gene;
import org.springframework.util.CollectionUtils;

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
        if (StringUtils.isNumeric(symbol) && Integer.parseInt(symbol) <= 0) {
            return null;
        }
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
        if (StringUtils.isNumeric(symbol) && Integer.parseInt(symbol) <= 0) {
            return null;
        }
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

    public static List<Gene> findGenesFromMyGeneInfo(List<Integer> entrezGeneIds) {
        // There is a limit of using mygene.info. The post can only allow no more than 1000 genes
        int pageSize = 500;
        List<Gene> genes = new ArrayList<>();
        List<Integer> page = new ArrayList<>();
        for (int i = 0; i < entrezGeneIds.size(); i++) {
            if (i % pageSize == 0) {
                if (page.size() > 0) {
                    genes.addAll(getGenesFromMyGeneInfo(page));
                }
                page = new ArrayList<>();
            }
            page.add(entrezGeneIds.get(i));
            if (i == entrezGeneIds.size() - 1) {
                genes.addAll(getGenesFromMyGeneInfo(page));
            }
        }
        return genes;
    }

    private static List<Gene> getGenesFromMyGeneInfo(List<Integer> entrezGeneIds) {
        List<Gene> genes = new ArrayList<>();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ids", entrezGeneIds.stream().map(id -> id.toString()).collect(Collectors.joining(",")));
        try {
            String response = HttpUtils.postRequest(URL_MY_GENE_INFO_3 + "gene?fields=entrezgene,symbol,alias", jsonObject.toString());
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                genes.add(parseGeneFromMyGeneInfo(jsonArray.getJSONObject(i)));
            }
            return genes;
        } catch (IOException e) {
            e.printStackTrace();
            return genes;
        }
    }

    private static Gene parseGeneFromMyGeneInfo(JSONObject object) {
        Gene gene = new Gene();
        if (object.has("symbol")) {
            gene.setHugoSymbol(object.getString("symbol"));
        }
        if (object.has("entrezgene")) {
            gene.setEntrezGeneId(object.getInt("entrezgene"));
        }
        if (object.has("alias")) {
            JSONArray aliases = object.optJSONArray("alias");
            if (aliases == null) {
                String alias = object.getString("alias");
                gene.getGeneAliases().add(alias);
            } else {
                for (int i = 0; i < aliases.length(); i++) {
                    gene.getGeneAliases().add(aliases.getString(i));
                }
            }
        }
        return gene;
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
        if (gene.getEntrezGeneId() == null || gene.getEntrezGeneId() <= 0) {
            return;
        }
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
