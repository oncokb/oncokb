/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.util;

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
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangh2
 */
public final class GoogleAuth {
    /**
     * Email of the Service Account
     */
    private static String SERVICE_ACCOUNT_EMAIL;

    /**
     * Path to the Service Account's Private Key file
     */
    private static Drive DRIVE_SERVICE;
    private static SpreadsheetService SPREADSHEET_SERVICE;
    private static File SERVICE_ACCOUNT_PKCS12_FILE;

    private static GoogleCredential CREDENTIAL;

    private GoogleAuth() {

    }

    public static Drive getDriveService() throws GeneralSecurityException, IOException, URISyntaxException {
        if (DRIVE_SERVICE != null) {
            return DRIVE_SERVICE;
        }

        createGoogleCredential();
        createDriveService();

        return DRIVE_SERVICE;
    }

    public static SpreadsheetService getSpreadSheetService() throws GeneralSecurityException, IOException, ServiceException {
        if (SPREADSHEET_SERVICE != null) {
            return SPREADSHEET_SERVICE;
        }

        createGoogleCredential();
        createSpreadSheetService();

        return SPREADSHEET_SERVICE;
    }

    private static void openFile() throws IOException {
        String SERVICE_ACCOUNT_PKCS12_FILE_PATH = PropertiesUtils.getProperties("google.p_twelve");
        URL SERVICE_ACCOUNT_PKCS12_URL = GoogleAuth.class.getClassLoader().getResource(SERVICE_ACCOUNT_PKCS12_FILE_PATH);
        SERVICE_ACCOUNT_PKCS12_FILE = new java.io.File(SERVICE_ACCOUNT_PKCS12_URL.getPath());
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

        if (SERVICE_ACCOUNT_EMAIL == null) {
            SERVICE_ACCOUNT_EMAIL = PropertiesUtils.getProperties("google.service_account_email");
        }

        if (SERVICE_ACCOUNT_PKCS12_FILE == null) {
            openFile();
        }

        if (CREDENTIAL == null) {
            HttpTransport httpTransport = new NetHttpTransport();
            JacksonFactory jsonFactory = new JacksonFactory();

            String[] SCOPESArray = {"https://spreadsheets.google.com/feeds", "https://www.googleapis.com/auth/gmail.compose"};
            CREDENTIAL = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
                .setServiceAccountScopes(getScopes())
                .setServiceAccountPrivateKeyFromP12File(SERVICE_ACCOUNT_PKCS12_FILE)
                .build();
        } else {
            refreshToken();
        }
    }

    private static void refreshToken() throws IOException {
        long timestamp = System.currentTimeMillis() + 10 * 60 * 1000; // If current token expires in 10 minutes, refresh token
        if (CREDENTIAL.getExpiresInSeconds().compareTo(timestamp) > 0) {
            CREDENTIAL.refreshToken();
        }
    }

    private static List<String> getScopes() {
        List<String> scopes = new ArrayList<String>();

        scopes.add("https://spreadsheets.google.com/feeds");
        scopes.add("https://www.googleapis.com/auth/gmail.compose");

        scopes.addAll(DriveScopes.all());
        return scopes;
    }

    private static void createSpreadSheetService() throws GeneralSecurityException, IOException, ServiceException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();

        SPREADSHEET_SERVICE = new SpreadsheetService("data");
        SPREADSHEET_SERVICE.setOAuth2Credentials(CREDENTIAL);
    }
}
