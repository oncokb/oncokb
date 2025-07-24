package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.apiModels.download.FileExtension;
import org.mskcc.cbio.oncokb.apiModels.download.FileName;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by Hongxin on 11/03/16.
 */
public class HttpUtils {
    private static final Logger LOGGER = LogManager.getLogger();

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
            LOGGER.info("Sending 'POST' request to URL : {}", url);
            LOGGER.info("Response Code : {}", responseCode);

            return FileUtils.readStream(con.getInputStream());
        } else {
            return null;
        }
    }

    public static String postRequestUrlParams(String url, String urlParams) throws IOException {
        if (url != null) {
            url = url.replaceAll(" ", "%20");
            URL obj = new URL(url);

            String urlParameters = urlParams;
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Send post request
            con.setDoOutput(true);

            // Set timeout to 10 seconds
            con.setConnectTimeout(10000);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(postData);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            LOGGER.info("\nSending '{}' request to URL : {}", con.getRequestMethod(), url);
            LOGGER.info("Response Code : " + responseCode);

            return FileUtils.readStream(con.getInputStream());
        } else {
            return null;
        }
    }

    public static String getRequest(String url) throws IOException {
        if (url != null) {
            LOGGER.info("Sending request: {}", url);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Set timeout to 10 seconds
            con.setReadTimeout(10 * 60 * 1000);
            con.connect();

            int responseCode = con.getResponseCode();
            LOGGER.info("Response Code : {}", responseCode);

            return FileUtils.readStream(con.getInputStream());
        } else {
            return null;
        }
    }


    public static <T> ResponseEntity<T> getDataDownloadResponseEntity(String version, FileName fileName, FileExtension fileExtension) {
        return getDataDownloadResponseEntity(version, fileName.getName() + fileExtension.getExtension(), fileExtension);
    }

    public static <T> ResponseEntity<T> getDataDownloadResponseEntity(String version, String fileName, FileExtension fileExtension) {
        try {
            if (fileExtension.equals(FileExtension.JSON)) {
                return new ResponseEntity<>((T) JsonUtils.jsonToArray(GitHubUtils.getOncoKBData(version, fileName)), HttpStatus.OK);
            } else if (fileExtension.equals(FileExtension.GZ)) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Disposition", "attachment; filename=" + fileName);
                return (ResponseEntity<T>) ResponseEntity.ok()
                    .headers(headers)
                    .contentType(new MediaType("application", "gz"))
                    .body(GitHubUtils.getOncoKBDataInBytes(version, fileName));
            } else {
                return new ResponseEntity<>((T) GitHubUtils.getOncoKBData(version, fileName), HttpStatus.OK);
            }
        } catch (HttpClientErrorException exception) {
            return new ResponseEntity<>(null, exception.getStatusCode());
        } catch (IOException exception) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NoPropertyException exception) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
