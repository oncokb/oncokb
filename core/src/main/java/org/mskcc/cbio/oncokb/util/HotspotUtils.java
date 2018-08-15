package org.mskcc.cbio.oncokb.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.cmo.cancerhotspots.model.SingleResidueHotspotMutation;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.VariantConsequence;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Hongxin on 11/03/16.
 */
public class HotspotUtils {
    private static Map<Gene, List<SingleResidueHotspotMutation>> hotspotMutations = new HashMap<>();
    private static boolean hotspotMutationsInitialized = false;

    public static void getHotspotsFromRemote() {
        List<SingleResidueHotspotMutation> hotspots = new ArrayList<>();
        try {
            String cancerHotspotsUrl = PropertiesUtils.getProperties("cancerhotspots.single");
            String response = HttpUtils.postRequest(cancerHotspotsUrl, "");
            hotspots = new ObjectMapper().readValue(response, new TypeReference<List<SingleResidueHotspotMutation>>() {
            });
        } catch (Exception e) {
            System.out.println("Fail to reach CancerHotspot endpoint. Fetch local file.");
            Gson gson = new GsonBuilder().create();
            SingleResidueHotspotMutation[] mutations = gson.fromJson(new BufferedReader(new InputStreamReader(HotspotUtils.class.getResourceAsStream("/data/cancer-hotspots-public-v2.json"))), SingleResidueHotspotMutation[].class);
            hotspots = new ArrayList<>(Arrays.asList(mutations));
        }
        parseData(hotspots);
        hotspotMutationsInitialized = true;
    }

    private static void parseData(List<SingleResidueHotspotMutation> hotspots) {
        if (hotspots != null) {
            for (SingleResidueHotspotMutation hotspotMutation : hotspots) {
                Gene gene = GeneUtils.getGeneByHugoSymbol(hotspotMutation.getHugoSymbol());
                if (gene != null) {
                    if (!hotspotMutations.containsKey(gene)) {
                        hotspotMutations.put(gene, new ArrayList<SingleResidueHotspotMutation>());
                    }
                    hotspotMutations.get(gene).add(hotspotMutation);
                }
            }
        }
    }

    public static Map<Gene, List<SingleResidueHotspotMutation>> getHotspots() {
        if (!hotspotMutationsInitialized) {
            getHotspotsFromRemote();
        }
        return hotspotMutations;
    }

    public static Boolean isHotspot(Alteration alteration) {
        Boolean isHotspot = false;
        if (alteration != null && alteration.getGene() != null) {
            if (alteration.getConsequence() == null) {
                AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());
            }

            Integer proteinStart = alteration.getProteinStart();
            Integer proteinEnd = alteration.getProteinEnd();
            VariantConsequence missense = VariantConsequenceUtils.findVariantConsequenceByTerm("missense_variant");
            VariantConsequence insertion = VariantConsequenceUtils.findVariantConsequenceByTerm("inframe_insertion");
            VariantConsequence deletion = VariantConsequenceUtils.findVariantConsequenceByTerm("inframe_deletion");

            if (proteinStart != null &&
                alteration.getConsequence().equals(missense)) {
                if (proteinEnd == null) {
                    proteinEnd = proteinStart;
                }

                if (hotspotMutations.containsKey(alteration.getGene())) {
                    for (SingleResidueHotspotMutation hotspotMutation : hotspotMutations.get(alteration.getGene())) {
                        if (hotspotMutation.getType().equals("single residue")
                            && hotspotMutation.getAminoAcidPosition() != null
                            && proteinStart >= hotspotMutation.getAminoAcidPosition().getStart()
                            && proteinEnd <= hotspotMutation.getAminoAcidPosition().getEnd()) {
                            isHotspot = true;
                            break;
                        }
                    }
                }
            } else if (alteration.getConsequence().equals(insertion) || alteration.getConsequence().equals(deletion)) {
                if (hotspotMutations.containsKey(alteration.getGene())) {
                    for (SingleResidueHotspotMutation hotspotMutation : hotspotMutations.get(alteration.getGene())) {
                        if (hotspotMutation.getType().equals("in-frame indel")
                            && hotspotMutation.getAminoAcidPosition() != null
                            && proteinEnd >= hotspotMutation.getAminoAcidPosition().getStart()
                            && proteinStart <= hotspotMutation.getAminoAcidPosition().getEnd()) {
                            isHotspot = true;
                            break;
                        }
                    }
                }
            }
        }
        return isHotspot;
    }
}
