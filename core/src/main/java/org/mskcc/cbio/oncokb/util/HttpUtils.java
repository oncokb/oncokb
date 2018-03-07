package org.mskcc.cbio.oncokb.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Hongxin on 11/03/16.
 */
public class HttpUtils {

    public static String postRequest(String url, String postBody) throws IOException {
        if (url != null) {
            url = url.replaceAll(" ", "%20");
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");

            // Send post request
            con.setDoOutput(true);

            // Set timeout to 10 seconds
            con.setConnectTimeout(10000);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postBody);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            return FileUtils.readStream(con.getInputStream());
        } else {
            return null;
        }
    }

    public static String getRequest(String url) throws IOException {
        if (url != null) {
            System.out.println("Sending request: " + url);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Set timeout to 10 seconds
            con.setReadTimeout(10000);
            con.connect();

            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            return FileUtils.readStream(con.getInputStream());
        } else {
            return null;
        }
    }
}
