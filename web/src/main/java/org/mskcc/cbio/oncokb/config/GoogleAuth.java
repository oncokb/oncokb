/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author zhangh2
 */
public class GoogleAuth {
    /** Email of the Service Account */
    private static final String SERVICE_ACCOUNT_EMAIL = "184231070809-bs0vrb773cdue7m15e2r93ifrsqr1aav@developer.gserviceaccount.com";

    /** Path to the Service Account's Private Key file */
    private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "/Users/zhangh2/Downloads/OncoKB Report-714d54c4ffed.p12";

    /**
     * Build and returns a Drive service object authorized with the service accounts.
     *
     * @return Drive service object that is ready to make requests.
     * @throws java.security.GeneralSecurityException
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    public static Drive getDriveService() throws GeneralSecurityException,
        IOException, URISyntaxException {
      HttpTransport httpTransport = new NetHttpTransport();
      JacksonFactory jsonFactory = new JacksonFactory();
      GoogleCredential credential = new GoogleCredential.Builder()
          .setTransport(httpTransport)
          .setJsonFactory(jsonFactory)
          .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
          .setServiceAccountScopes(DriveScopes.all())
          .setServiceAccountPrivateKeyFromP12File(
              new java.io.File(SERVICE_ACCOUNT_PKCS12_FILE_PATH))
          .build();
      Drive service = new Drive.Builder(httpTransport, jsonFactory, null)
          .setHttpRequestInitializer(credential).build();
      
      return service;
    }
    
    public static SpreadsheetService createSpreedSheetService() throws GeneralSecurityException, IOException, ServiceException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        String [] SCOPESArray= {"https://spreadsheets.google.com/private/feeds"};
        String username = "jackson.zhang.828@gmail.com";
        String password = "gmail_privatespace";
        final List SCOPES = Arrays.asList(SCOPESArray); 
        Credential credential = new GoogleCredential.Builder()
          .setTransport(httpTransport)
          .setJsonFactory(jsonFactory)
          .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
          .setServiceAccountScopes(SCOPES)
          .setServiceAccountPrivateKeyFromP12File(
              new java.io.File(SERVICE_ACCOUNT_PKCS12_FILE_PATH))
          .build();
      
        SpreadsheetService service =
            new SpreadsheetService("data");
        service.setOAuth2Credentials(credential);
        service.setUserCredentials(username, password);
        
        return service;
    }
    
    public static void sendPost() throws Exception {
 
            String url = "https://script.google.com/macros/s/AKfycbzUugMu9fibfZUTM909RA_wOvDaI4w9uYna42ysRdsYZrv7gA/exec";
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
//            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = "";

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + urlParameters);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
    }
}
