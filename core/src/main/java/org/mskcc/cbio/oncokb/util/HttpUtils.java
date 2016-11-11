package org.mskcc.cbio.oncokb.util;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Hongxin on 11/03/16.
 */
public class HttpUtils {
    
    public static String postRequest(String url, String postBody) {
        if (url != null) {

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                //add reuqest header
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

            } catch (SocketTimeoutException e) {
                System.out.println("Requesting " + url + " TIMEOUT.");
                return "TIMEOUT";
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}