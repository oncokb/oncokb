package org.mskcc.cbio.oncokb.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.cbioportal.genome_nexus.model.Hotspot;
import org.cbioportal.genome_nexus.model.ProteinLocation;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import static org.mskcc.cbio.oncokb.util.VariantConsequenceUtils.toGNMutationType;

/**
 * Created by Hongxin on 11/03/16.
 */
public class HotspotUtils {
    private static final String HOTSPOT_FILE_PATH = "/data/cancer-hotspots-gn.json";
    private static Map<Gene, List<Hotspot>> hotspotMutations = new HashMap<>();

    static {
        System.out.println("Cache all hotspots at " + MainUtils.getCurrentTime());
        getHotspotsFromDataFile();
    }

    private static void getHotspotsFromDataFile() {
        List<Hotspot> hotspots = new ArrayList<>();
        System.out.println("Fail to reach CancerHotspot endpoint. Fetch local file.");
        Gson gson = new GsonBuilder().create();
        Hotspot[] mutations = gson.fromJson(new BufferedReader(new InputStreamReader(HotspotUtils.class.getResourceAsStream(HOTSPOT_FILE_PATH))), Hotspot[].class);
        hotspots = new ArrayList<>(Arrays.asList(mutations));
        parseData(hotspots);
    }

    private static void parseData(List<Hotspot> hotspots) {
        if (hotspots != null) {
            for (Hotspot hotspotMutation : hotspots) {
                Gene gene = GeneUtils.getGeneByHugoSymbol(hotspotMutation.getHugoSymbol());
                if (gene != null) {
                    if (!hotspotMutations.containsKey(gene)) {
                        hotspotMutations.put(gene, new ArrayList<Hotspot>());
                    }
                    hotspotMutations.get(gene).add(hotspotMutation);
                }
            }
        }
    }

    public static boolean isHotspot(Alteration alteration) {
        if (alteration == null || alteration.getGene() == null) {
            return false;
        }
        ProteinLocation proteinLocation = new ProteinLocation(alteration.getGene().getCuratedIsoform(), alteration.getProteinStart(), alteration.getProteinEnd(), toGNMutationType(alteration.getConsequence()));
        List<Hotspot> hotspots = new ArrayList<>();

        for(Hotspot hotspot : hotspotMutations.get(alteration.getGene())) {
            if(hotspot.getType()!= "3d") {
                hotspots.add(hotspot);
            }
        }
        return proteinLocationHotspotsFilter(hotspots, proteinLocation).size() > 0;
    }

    // Logic from GN
    private static List<Hotspot> proteinLocationHotspotsFilter(List<Hotspot> hotspots, ProteinLocation proteinLocation) {
        int start = proteinLocation.getStart();
        int end = proteinLocation.getEnd();
        String type = proteinLocation.getMutationType();
        List<Hotspot> result = new ArrayList<>();

        for (Hotspot hotspot: hotspots) {
            boolean validPosition = true;

            // Protein location
            int hotspotStart = proteinLocation.getStart();
            int hotspotStop = proteinLocation.getEnd();
            validPosition &= (start <= hotspotStart && end >= hotspotStop);

            // Mutation type
            boolean validMissense = type.equals("Missense_Mutation") && (hotspot.getType().contains("3d") || hotspot.getType().contains("single residue"));
            boolean validInFrame = type.equals("In_Frame_Ins") || type.equals("In_Frame_Ins") && (hotspot.getType().contains("in-frame"));
            boolean validSplice = type.equals("Splice_Site") || type.equals("Splice_Region") && (hotspot.getType().contains("splice"));

            // Add hotspot
            if (validPosition && (validMissense || validInFrame || validSplice)) {
                result.add(hotspot);
            }
        }

        return result;
    }
}
