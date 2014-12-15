/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.Link;
import com.google.gdata.data.batch.*;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import org.codehaus.jackson.map.ObjectMapper;
import org.mskcc.cbio.oncokb.config.GoogleAuth;
import org.json.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author zhangh2
 */
@Controller
public class GenerateGoogleDoc {
    /**
        * A basic struct to store cell row/column information and the associated RnCn
        * identifier.
        */
    private static class CellAddress {
        public final int row;
        public final int col;
        public final String attr;
        public final String idString;

        /**
         * Constructs a CellAddress representing the specified {@code row} and
         * {@code col}.  The idString will be set in 'RnCn' notation.
         */
        public CellAddress(int row, int col, String attr) {
          this.row = row;
          this.col = col;
          this.attr = attr;
          this.idString = String.format("R%sC%s", row, col);
        }
    }
    
    private static final String REPORT_PARENT_FOLDER = "0BzBfo69g8fP6eXoydVRrdHJBbE0";
    private static final String  REPORT_DATA_TEMPLATE = "1fCv8J8fZ2ZziZFJMqRRpNexxqGT5tewDEdbVT64wjjM";
    private static final String REPORTS_INFO_SHEET_ID = "1fsixOxg-o-_UwZvInU99791ITyYhs4nz8s35_qJmw8o";
        
    @RequestMapping(value="/generateGoogleDoc", method = POST)
    public @ResponseBody Boolean generateGoogleDoc(
            @RequestParam(value="reportContent", required=false) String reportContent) throws MalformedURLException, ServiceException{
        Boolean responseFlag = false;
        try {
            GoogleAuth google = new GoogleAuth();
            JSONObject jsonObj = new JSONObject(reportContent);
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM-dd-yyyy z");
            Date date = new Date();
            String dateString = dateFormat.format(date);
            Drive driveService = GoogleAuth.getDriveService();
            System.out.println("Got drive service");
            String fileName = jsonObj.getString("geneName") + "_" + jsonObj.getString("mutation") + "_" + jsonObj.getString("diagnosis");
            File file = new File();
            file.setTitle(fileName);
            file.setParents(Arrays.asList(new ParentReference().setId(REPORT_PARENT_FOLDER)));
            file.setDescription("New File created from server");
            System.out.println("Copying file");
            file = driveService.files().copy(REPORT_DATA_TEMPLATE, file).execute();
            System.out.println("Successfully copied file. Start to change file content");
            changeFileContent(file.getId(), file.getTitle(), jsonObj);
            System.out.println("Successfully changed file content. Start to add new record");
            addNewRecord(file.getId(), file.getTitle(), "zhx", dateString, jsonObj.get("email").toString(),jsonObj.has("folderId")?jsonObj.get("folderId").toString():null);
            System.out.println("Successfully added new record");
            responseFlag = true;
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();

            System.err.println("Error code: " + error.getCode());
            System.err.println("Error message: " + error.getMessage());
            // More error information can be retrieved with error.getErrors().
        } catch (HttpResponseException e) {
            // No Json body was returned by the API.
            System.err.println("HTTP Status code: " + e.getStatusCode());
            System.err.println("HTTP Reason:" + e.getMessage());
        } catch (IOException e) {
            // Other errors (e.g connection timeout, etc.).
            System.out.println("An error occurred: " + e);
        } catch (GeneralSecurityException e) {
            System.out.println("An GeneralSecurityException occurred: " + e);
        } catch (URISyntaxException e) {
            System.out.println("An URISyntaxException occurred: " + e);
        }
        return responseFlag;
    }
    
    public static void addNewRecord(String reportDataFileId, String reportName, String user, String date, String email, String folderId) throws MalformedURLException, GeneralSecurityException, IOException, ServiceException {
        URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + REPORTS_INFO_SHEET_ID);
        
        SpreadsheetService service = GoogleAuth.createSpreedSheetService();
        SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);
        
        WorksheetFeed worksheetFeed = service.getFeed(
        spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
        List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
        WorksheetEntry worksheet = worksheets.get(0);
        
        // Fetch the list feed of the worksheet.
        URL listFeedUrl = worksheet.getListFeedUrl();
        ListFeed listEntry = service.getFeed(listFeedUrl, ListFeed.class);
        // Create a local representation of the new row.
        ListEntry row = new ListEntry();
        row.getCustomElements().setValueLocal("reportdatafileid", reportDataFileId);
        row.getCustomElements().setValueLocal("reportname", reportName);
        row.getCustomElements().setValueLocal("user", user);
        row.getCustomElements().setValueLocal("requestdate", date);
        row.getCustomElements().setValueLocal("email", email);
        row.getCustomElements().setValueLocal("sendreminder", "No");
        if(folderId != null) {
            row.getCustomElements().setValueLocal("folderid", folderId);
        }

        // Send the new row to the API for insertion.
        service.insert(listFeedUrl, row);
    }
    
    public static void changeFileContent(String fileId, String fileName, JSONObject content) throws MalformedURLException, GeneralSecurityException, IOException, ServiceException {
        URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + fileId);
        
        SpreadsheetService service = GoogleAuth.createSpreedSheetService();
        SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);

        WorksheetFeed worksheetFeed = service.getFeed(
        spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
        List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
        WorksheetEntry worksheet = worksheets.get(0);

        // Fetch the list feed of the worksheet.
        URL listFeedUrl = worksheet.getListFeedUrl();
        ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);
        ListEntry listEntry  = listFeed.getEntries().get(0);
        
        Iterator<String> keys = content.keys();
        System.out.println("Successfully get all entries. Start to change content");
        while(keys.hasNext()){
            String key = keys.next();
            if(!content.get(key).toString().equals("")) {
                listEntry.getCustomElements().setValueLocal(key, content.get(key).toString());
            }
        }
        System.out.print("Updating...");
        listEntry.update();
        System.out.println("Done.");
    }
}