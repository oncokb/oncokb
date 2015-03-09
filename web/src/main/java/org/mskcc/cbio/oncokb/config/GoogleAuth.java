/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.util.ServiceException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author zhangh2
 */
public class GoogleAuth {
    /** Email of the Service Account */
    private static final String SERVICE_ACCOUNT_EMAIL = "184231070809-f51uf3d89ljd78ssvss5eoidhtca4fhp@developer.gserviceaccount.com";

    /** Path to the Service Account's Private Key file */
    private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "/OncoKB-Report-7c732673acd9.p12";
    private static final URL url= GoogleAuth.class.getResource(SERVICE_ACCOUNT_PKCS12_FILE_PATH);
    private static final File file = new java.io.File(url.getPath());
    private static Drive driveService;
    private static SpreadsheetService spreadsheetService;
    
    public static Drive getDriveService() throws GeneralSecurityException, IOException, URISyntaxException {
        if(GoogleAuth.driveService == null){
            createDriveService();
        }
        
        return GoogleAuth.driveService;
    }
    
    public static SpreadsheetService getSpreadSheetService() throws GeneralSecurityException, IOException, ServiceException {
        if(GoogleAuth.spreadsheetService == null){
            createSpreadSheetService();
        }
        
        return GoogleAuth.spreadsheetService;
    }
    
    private static void createDriveService() throws GeneralSecurityException,
        IOException, URISyntaxException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(jsonFactory)
            .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
            .setServiceAccountScopes(Collections.singleton(DriveScopes.DRIVE_FILE))
            .setServiceAccountPrivateKeyFromP12File(file)
          .build();
        credential.refreshToken();
        GoogleAuth.driveService = new Drive.Builder(httpTransport, jsonFactory, null)
                .setApplicationName("Oncoreport")
                .setHttpRequestInitializer(credential).build();
    }
    
    private static void createSpreadSheetService() throws GeneralSecurityException, IOException, ServiceException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        String [] SCOPESArray= {"https://spreadsheets.google.com/feeds", "https://docs.google.com/feeds"};
        String username = "jackson.zhang.828@gmail.com";
        String password = "gmail_privatespace";
        final List SCOPES = Arrays.asList(SCOPESArray); 
        GoogleCredential credential = new GoogleCredential.Builder()
          .setTransport(httpTransport)
          .setJsonFactory(jsonFactory)
          .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
          .setServiceAccountScopes(SCOPES)
          .setServiceAccountPrivateKeyFromP12File(file)
          .build();
      
        GoogleAuth.spreadsheetService = new SpreadsheetService("data");
        GoogleAuth.spreadsheetService.setOAuth2Credentials(credential);
        GoogleAuth.spreadsheetService.setUserCredentials(username, password);
    }
}
