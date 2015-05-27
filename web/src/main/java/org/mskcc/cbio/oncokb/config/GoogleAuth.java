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
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.gmail.Gmail;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.util.ServiceException;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author zhangh2
 */
public class GoogleAuth {
    /** Email of the Service Account */
    private String SERVICE_ACCOUNT_EMAIL;

    /** Path to the Service Account's Private Key file */
    private Drive DRIVESERVICE;
    private SpreadsheetService SPREADSHEETSERVICE;
    private Gmail GMAILSERVICE;
    private GoogleClientSecrets clientSecrets;
    private File CLIENT_SECRET_FILE;
    private File SERVICE_ACCOUNT_PKCS12_FILE;
    private Properties PROPERTIES;
    private String USERNAME;
    private String PASSWORD;
    
    
    public Drive getDriveService() throws GeneralSecurityException, IOException, URISyntaxException {
        if(CLIENT_SECRET_FILE == null) {
            openFile();
        }

        if(PROPERTIES == null){
            getProperties();
        }

        if(DRIVESERVICE == null){
            createDriveService();
        }
        
        return DRIVESERVICE;
    }
    
    public SpreadsheetService getSpreadSheetService() throws GeneralSecurityException, IOException, ServiceException {
        if(CLIENT_SECRET_FILE == null) {
            openFile();
        }

        if(PROPERTIES == null){
            getProperties();
        }
        
        if(SPREADSHEETSERVICE == null){
            createSpreadSheetService();
        }
        
        return SPREADSHEETSERVICE;
    }
    
    public Gmail getGmailService() throws GeneralSecurityException, IOException, ServiceException {
        if(CLIENT_SECRET_FILE == null) {
            openFile();
        }

        if(PROPERTIES == null){
            getProperties();
        }
        
        if(GMAILSERVICE == null){
            createGmailService();
        }
        
        return GMAILSERVICE;
    }

    private void getProperties() throws IOException {
        String propFileName = "properties/config.properties";
        PROPERTIES = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            PROPERTIES.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
        inputStream.close();

        SERVICE_ACCOUNT_EMAIL = PROPERTIES.getProperty("google.service_account_email");
        USERNAME = PROPERTIES.getProperty("google.username");
        PASSWORD = PROPERTIES.getProperty("google.password");
    }

    private void openFile() {
        String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "/OncoKB-Report-7c732673acd9.p12";
        String CLIENT_SECRET_PATH = "/OncoKB-Report-ba65ac545ecb.json";
        URL CLIENT_SECRET_PATH_URL= GoogleAuth.class.getResource(CLIENT_SECRET_PATH);
        URL SERVICE_ACCOUNT_PKCS12_URL= GoogleAuth.class.getResource(SERVICE_ACCOUNT_PKCS12_FILE_PATH);
        SERVICE_ACCOUNT_PKCS12_FILE = new java.io.File(SERVICE_ACCOUNT_PKCS12_URL.getPath());
        CLIENT_SECRET_FILE = new java.io.File(CLIENT_SECRET_PATH_URL.getPath());
    }
    
    private void createDriveService() throws GeneralSecurityException,
        IOException, URISyntaxException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(jsonFactory)
            .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
            .setServiceAccountScopes(DriveScopes.all())
            .setServiceAccountPrivateKeyFromP12File(SERVICE_ACCOUNT_PKCS12_FILE)
          .build();
        credential.refreshToken();
        DRIVESERVICE = new Drive.Builder(httpTransport, jsonFactory, null)
                .setApplicationName("Oncoreport")
                .setHttpRequestInitializer(credential).build();
    }
    
    private void createSpreadSheetService() throws GeneralSecurityException, IOException, ServiceException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        String [] SCOPESArray= {"https://spreadsheets.google.com/feeds", "https://docs.google.com/feeds"};
        final List SCOPES = Arrays.asList(SCOPESArray);
        GoogleCredential credential = new GoogleCredential.Builder()
          .setTransport(httpTransport)
          .setJsonFactory(jsonFactory)
          .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
          .setServiceAccountScopes(SCOPES)
          .setServiceAccountPrivateKeyFromP12File(SERVICE_ACCOUNT_PKCS12_FILE)
          .build();
      
        SPREADSHEETSERVICE = new SpreadsheetService("data");
        SPREADSHEETSERVICE.setOAuth2Credentials(credential);
    }
    
    private void createGmailService() throws GeneralSecurityException, IOException, ServiceException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        
        String [] SCOPESArray= {"https://www.googleapis.com/auth/gmail.compose"};
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
        GMAILSERVICE = new Gmail.Builder(httpTransport, jsonFactory, credential).setApplicationName("OncoKB curation comments").build();
        System.out.println();
    }
}
