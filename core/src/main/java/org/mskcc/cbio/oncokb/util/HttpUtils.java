package org.mskcc.cbio.oncokb.util;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Hongxin on 11/03/16.
 */
public class HttpUtils {

    public static String postRequest(String url, String postBody, Boolean retry) {
        if (retry == null) {
            retry = false;
        }
        if (url != null) {
            url = url.replaceAll(" ", "%20");
            try {
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

            } catch (SocketTimeoutException e) {
                System.out.println("Requesting " + url + " TIMEOUT.");
                return "TIMEOUT";
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();

                // If retry needed, but only retry once
                if (retry) {
                    return postRequest(url, postBody, false);
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public static String getRequest(String url, Boolean retry) {
        if (retry == null) {
            retry = false;
        }
        if (url != null) {
            try {
                System.out.println("Sending request: " + url);
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // Set timeout to 10 seconds
                con.setReadTimeout(10000);
                con.connect();

                int responseCode = con.getResponseCode();
                System.out.println("Response Code : " + responseCode);

                return FileUtils.readStream(con.getInputStream());

            } catch (SocketTimeoutException e) {
                System.out.println("Requesting " + url + " TIMEOUT.");
                // If retry needed, but only retry once
                if (retry) {
                    return getRequest(url, false);
                } else {
                    return null;
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
                // If retry needed, but only retry once
                if (retry) {
                    return getRequest(url, false);
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }
}
