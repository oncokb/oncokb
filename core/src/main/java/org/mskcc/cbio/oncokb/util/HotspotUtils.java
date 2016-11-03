package org.mskcc.cbio.oncokb.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mskcc.cbio.oncokb.model.HotspotMutation;

import java.io.IOException;
import java.util.List;

/**
 * Created by Hongxin on 11/03/16.
 */
public class HotspotUtils {
    private static List<HotspotMutation> hotspotMutations = null;
    private static String cancerHotspotsUrl = "http://localhost:8080/cancerhotspots/api/hotspots/single";

    public static void getHotspotsFromRemote() {
        String response = HttpUtils.postRequest(cancerHotspotsUrl, "");
        if (response != null) {
            try {
                hotspotMutations = new ObjectMapper().readValue(response, new TypeReference<List<HotspotMutation>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<HotspotMutation> getHotspots() {
        if (hotspotMutations == null) {
            getHotspotsFromRemote();
        }
        return hotspotMutations;
    }

    public static Boolean isHotspot(String hugoSymbol, Integer proteinStart, Integer proteinEnd) {
        Boolean isHotspot = false;
        if (hugoSymbol != null && proteinStart != null && proteinEnd != null
            && proteinEnd >= proteinStart && proteinStart > 0) {
            for (HotspotMutation hotspotMutation : hotspotMutations) {
                if (hotspotMutation.getHugoSymbol().equals(hugoSymbol)
                    && proteinStart >= hotspotMutation.getAminoAcidPosition().getStart()
                    && proteinEnd <= hotspotMutation.getAminoAcidPosition().getEnd()) {
                    isHotspot = true;
                    break;
                }
            }
        }
        return isHotspot;
    }
}