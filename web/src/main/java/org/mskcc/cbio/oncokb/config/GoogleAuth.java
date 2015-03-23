/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.gmail.Gmail;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.util.ServiceException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private static Drive driveService;
    private static SpreadsheetService spreadsheetService;
    private static Gmail gmailService;
    private static GoogleClientSecrets clientSecrets;
    private static File CLIENT_SECRET_FILE;
    private static File SERVICE_ACCOUNT_PKCS12_FILE;
    
    
    public static Drive getDriveService() throws GeneralSecurityException, IOException, URISyntaxException {
        if(CLIENT_SECRET_FILE == null) {
            openFile();
        }
        
        if(GoogleAuth.driveService == null){
            createDriveService();
        }
        
        return GoogleAuth.driveService;
    }
    
    public static SpreadsheetService getSpreadSheetService() throws GeneralSecurityException, IOException, ServiceException {
        if(CLIENT_SECRET_FILE == null) {
            openFile();
        }
        
        if(GoogleAuth.spreadsheetService == null){
            createSpreadSheetService();
        }
        
        return GoogleAuth.spreadsheetService;
    }
    
    public static Gmail getGmailService() throws GeneralSecurityException, IOException, ServiceException {
        if(CLIENT_SECRET_FILE == null) {
            openFile();
        }
        
        if(GoogleAuth.gmailService == null){
            createGmailService();
        }
        
        return GoogleAuth.gmailService;
    }
    
    private static void openFile() {
        String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "/OncoKB-Report-7c732673acd9.p12";
        String CLIENT_SECRET_PATH = "/OncoKB-Report-ba65ac545ecb.json";
        URL CLIENT_SECRET_PATH_URL= GoogleAuth.class.getResource(CLIENT_SECRET_PATH);
        URL SERVICE_ACCOUNT_PKCS12_URL= GoogleAuth.class.getResource(SERVICE_ACCOUNT_PKCS12_FILE_PATH);
        SERVICE_ACCOUNT_PKCS12_FILE = new java.io.File(SERVICE_ACCOUNT_PKCS12_URL.getPath());
        CLIENT_SECRET_FILE = new java.io.File(CLIENT_SECRET_PATH_URL.getPath());
    }
    
    private static void createDriveService() throws GeneralSecurityException,
        IOException, URISyntaxException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(jsonFactory)
            .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
            .setServiceAccountScopes(Collections.singleton(DriveScopes.DRIVE))
            .setServiceAccountPrivateKeyFromP12File(SERVICE_ACCOUNT_PKCS12_FILE)
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
          .setServiceAccountPrivateKeyFromP12File(SERVICE_ACCOUNT_PKCS12_FILE)
          .build();
      
        GoogleAuth.spreadsheetService = new SpreadsheetService("data");
        GoogleAuth.spreadsheetService.setOAuth2Credentials(credential);
        GoogleAuth.spreadsheetService.setUserCredentials(username, password);
    }
    
    private static void createGmailService() throws GeneralSecurityException, IOException, ServiceException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        
        String [] SCOPESArray= {"https://www.googleapis.com/auth/gmail.compose"};
        String username = "jackson.zhang.828@gmail.com";
        String password = "gmail_privatespace";
        final List SCOPES = Arrays.asList(SCOPESArray); 
        clientSecrets = GoogleClientSecrets.load(jsonFactory,  new FileReader(CLIENT_SECRET_FILE.getAbsolutePath()));

        // Allow user to authorize via url.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientSecrets, SCOPES)
            .setAccessType("online")
            .setApprovalPrompt("auto").build();

        String url = flow.newAuthorizationUrl().setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI)
            .build();
        System.out.println("Please open the following URL in your browser then type"
                           + " the authorization code:\n" + url);

        // Read code entered by user.
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String code = br.readLine();

        // Generate Credential using retrieved code.
        GoogleTokenResponse response = flow.newTokenRequest(code)
            .setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).execute();
        GoogleCredential credential = new GoogleCredential()
            .setFromTokenResponse(response);
            
        // Create a new authorized Gmail API client
        GoogleAuth.gmailService = new Gmail.Builder(httpTransport, jsonFactory, credential).setApplicationName("OncoKB curation comments").build();
        System.out.println();
    }
}
