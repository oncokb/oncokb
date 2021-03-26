package org.mskcc.cbio.oncokb.util;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Coordinates;

/**
 * Created by Yifu Yao on 3/4/2021
 */

public class OpenStreetMapUtils {

    public static final Logger log = Logger.getLogger("OpenStreeMapUtils");

    private static OpenStreetMapUtils instance = null;

    public static OpenStreetMapUtils getInstance() {
        if (instance == null) {
            instance = new OpenStreetMapUtils();
        }
        return instance;
    }

    public Coordinates getCoordinates(String address) {
        Coordinates res = new Coordinates();
        StringBuffer query;
        String[] split = address.split(" ");
        String queryResult = null;
        query = new StringBuffer();

        query.append("https://nominatim.openstreetmap.org/search?q=");

        if (split.length == 0) {
            return res;
        }

        for (int i = 0; i < split.length; i++) {
            query.append(split[i]);
            if (i < (split.length - 1)) {
                query.append("+");
            }
        }
        query.append("&format=json&addressdetails=1");

        try {
            queryResult = HttpUtils.getRequest(query.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (queryResult == null) {
            return null;
        }

        Object obj = JSONValue.parse(queryResult);
        log.debug("obj=" + obj);

        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            if (array.size() > 0) {
                JSONObject jsonObject = (JSONObject) array.get(0);

                String lon = (String) jsonObject.get("lon");
                String lat = (String) jsonObject.get("lat");
                res.setLat(Double.parseDouble(lat));
                res.setLon(Double.parseDouble(lon));
            }
        }

        return res;
    }

    // Accroding to: https://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
    public double calculateDistance(Coordinates ori, Coordinates des) {
        double lat1 = ori.getLat();
        double lat2 = des.getLat();
        double lon1 = ori.getLon();
        double lon2 = des.getLon();

        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);
        double a =
            Math.sin(dLat / 2) *
            Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) *
            Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) *
            Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        return dist / 1000;
    }
}
