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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author zhangh2
 */
public final class GoogleAuth {
    /** Email of the Service Account */
    private static String SERVICE_ACCOUNT_EMAIL;

    /** Path to the Service Account's Private Key file */
    private static Drive DRIVE_SERVICE;
    private static SpreadsheetService SPREADSHEET_SERVICE;
    private static Gmail GMAIL_SERVICE;
    private static GoogleClientSecrets CLIENT_SECRET;
    private static File CLIENT_SECRET_FILE;
    private static File SERVICE_ACCOUNT_PKCS12_FILE;
    private static Properties PROPERTIES;
    private static String USERNAME;
    private static String PASSWORD;

    private static GoogleCredential CREDENTIAL;

    private GoogleAuth() {

    }

    public static Drive getDriveService() throws GeneralSecurityException, IOException, URISyntaxException {
        if(DRIVE_SERVICE != null){
            return DRIVE_SERVICE;
        }

        createGoogleCredential();
        createDriveService();

        return DRIVE_SERVICE;
    }
    
    public static SpreadsheetService getSpreadSheetService() throws GeneralSecurityException, IOException, ServiceException {
        if(SPREADSHEET_SERVICE != null){
            return SPREADSHEET_SERVICE;
        }

        createGoogleCredential();
        createSpreadSheetService();
        
        return SPREADSHEET_SERVICE;
    }

    /**
     * Send email by using users' gmail address. It's been replaced by javax.mail.Transport
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws ServiceException
     */
    public static Gmail getGmailService() throws GeneralSecurityException, IOException, ServiceException {
        if(GMAIL_SERVICE != null){
            return GMAIL_SERVICE;
        }

        createGoogleCredential();
        createGmailService();
        
        return GMAIL_SERVICE;
    }

    private static void getProperties() throws IOException {
        if(PROPERTIES == null) {
            String propFileName = "properties/config.properties";
            PROPERTIES = new Properties();
            InputStream inputStream = GoogleAuth.class.getClassLoader().getResourceAsStream(propFileName);

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
    }

    private static void openFile() {
        String SERVICE_ACCOUNT_PKCS12_FILE_PATH = PROPERTIES.getProperty("google.p_twelve");
        String CLIENT_SECRET_PATH = PROPERTIES.getProperty("google.json");
        URL CLIENT_SECRET_PATH_URL= GoogleAuth.class.getClassLoader().getResource(CLIENT_SECRET_PATH);
        URL SERVICE_ACCOUNT_PKCS12_URL= GoogleAuth.class.getClassLoader().getResource(SERVICE_ACCOUNT_PKCS12_FILE_PATH);
        SERVICE_ACCOUNT_PKCS12_FILE = new java.io.File(SERVICE_ACCOUNT_PKCS12_URL.getPath());
        CLIENT_SECRET_FILE = new java.io.File(CLIENT_SECRET_PATH_URL.getPath());
    }
    
    private static void createDriveService() throws GeneralSecurityException,
        IOException, URISyntaxException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        DRIVE_SERVICE = new Drive.Builder(httpTransport, jsonFactory, null)
                .setApplicationName("Oncoreport")
                .setHttpRequestInitializer(CREDENTIAL).build();
    }

    private static void createGoogleCredential() throws GeneralSecurityException, IOException {

        if(PROPERTIES == null){
            getProperties();
        }
        if(CLIENT_SECRET_FILE == null) {
            openFile();
        }

        if(CREDENTIAL == null){
            HttpTransport httpTransport = new NetHttpTransport();
            JacksonFactory jsonFactory = new JacksonFactory();

            String [] SCOPESArray= {"https://spreadsheets.google.com/feeds", "https://www.googleapis.com/auth/gmail.compose"};
            CREDENTIAL = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
                    .setServiceAccountScopes(getScopes())
                    .setServiceAccountPrivateKeyFromP12File(SERVICE_ACCOUNT_PKCS12_FILE)
                    .build();
        }else{
            refreshToken();
        }
    }

    private static void refreshToken() throws IOException {
        long timestamp = System.currentTimeMillis() + 10 * 60 * 1000; // If current token expires in 10 minutes, refresh token
        if(CREDENTIAL.getExpiresInSeconds().compareTo(timestamp) > 0){
            CREDENTIAL.refreshToken();
        }
    }

    private static List<String> getScopes() {
        List<String> scopes = new ArrayList<String>();

        scopes.add("https://spreadsheets.google.com/feeds");
        scopes.add("https://www.googleapis.com/auth/gmail.compose");

        scopes.addAll(DriveScopes.all());
        return  scopes;
    }

    /**
     * It was used by gmail service when creating email by using users' email address
     * @throws IOException
     */
    private static void createGoogleClientSecrets() throws IOException {
        JacksonFactory jsonFactory = new JacksonFactory();
        CLIENT_SECRET = GoogleClientSecrets.load(jsonFactory, new FileReader(CLIENT_SECRET_FILE.getAbsolutePath()));
    }
    
    private static void createSpreadSheetService() throws GeneralSecurityException, IOException, ServiceException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();

        SPREADSHEET_SERVICE = new SpreadsheetService("data");
        SPREADSHEET_SERVICE.setOAuth2Credentials(CREDENTIAL);
    }
    
    private static void createGmailService() throws GeneralSecurityException, IOException, ServiceException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
            
        // Create a new authorized Gmail API client
        GMAIL_SERVICE = new Gmail.Builder(httpTransport, jsonFactory, CREDENTIAL).setApplicationName("OncoKB curation comments").build();
        System.out.println();
    }
}
