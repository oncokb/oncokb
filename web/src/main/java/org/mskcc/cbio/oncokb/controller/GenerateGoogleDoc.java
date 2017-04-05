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
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import org.json.*;
import org.mskcc.cbio.oncokb.util.GoogleAuth;
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
    
    private static final String REPORT_PARENT_FOLDER = "0BzBfo69g8fP6fnhBT1hjQkhQV3M3dnRkajdyYmtWR3pxeS1VTkJURVhwRkhlYV8wT0J6ZTA";
    private static final String REPORT_DATA_TEMPLATE = "1740Tw1f06j0IZPKqnNeg0E-7639sd4y50ZiE2ORPCn0";
    private static final String REPORTS_INFO_SHEET_ID = "1dHsXjrk9R5C3MkJ_iEUZ39OQPTSF3UOPYjU_C54zXuA";
        
//    @RequestMapping(value="/legacy-api/generateGoogleDoc", method = POST)
    public @ResponseBody Boolean generateGoogleDoc(
            @RequestParam(value="reportParams", required=true) String reportParams) throws MalformedURLException, ServiceException, URISyntaxException{
        try {
            
            if(reportParams == null) {
                return false;
            }else{
                JSONObject params = new JSONObject(reportParams);
                JSONObject reportContent = params.getJSONObject("reportContent");
                JSONObject requestInfo =  params.getJSONObject("requestInfo");
                
                if(reportContent.isNull("items") || reportContent.getJSONArray("items").length() == 0) {
                    return false;
                }else{
                    JSONArray items = reportContent.getJSONArray("items");
                    
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM-dd-yyyy z");
                    Date date = new Date();
                    String dateString = dateFormat.format(date);

                    Drive driveService = GoogleAuth.getDriveService();
                    System.out.println("Got drive service");
                    
                    String fileName = requestInfo.getString("fileName");
                    
                    File file = new File();
                    file.setTitle(fileName);
                    file.setParents(Arrays.asList(new ParentReference().setId(REPORT_PARENT_FOLDER)));
                    file.setDescription("New File created from server");
                    
                    System.out.println("Copying file");
                    
                    file = driveService.files().copy(REPORT_DATA_TEMPLATE, file).execute();
                    
                    System.out.println("Successfully copied file. Start to change file content");
                    
                    changeFileContent(file.getId(), file.getTitle(), reportContent, items);
                    
                    String userName = "zhx";

                    if(requestInfo.has("userName") && requestInfo.getString("fileName") != ""){
                        userName = requestInfo.getString("fileName");
                    }
                    
                    if(requestInfo.has("userName")) {
                        userName = requestInfo.getString("userName");
                    }
                    System.out.println("Successfully changed file content. Start to add new record");
                    
                    addNewRecord(file.getId(), file.getTitle(), userName, 
                            dateString, requestInfo.get("email").toString(), 
                            requestInfo.has("folderId")?requestInfo.get("folderId").toString():null,
                            requestInfo.has("folderName")?requestInfo.get("folderName").toString():null);
                    
                    System.out.println("Successfully added new record");
                    return true;
                }
            }
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
        }
        return false;
    }
    
    private static String getFileName(JSONObject content, JSONArray items){
        String fileName = "";
        
        if(content.has("items")) {
            if(items.length() > 1) {
                fileName = content.getString("patientName");
            }else{
                fileName = items.getJSONObject(0).getString("geneName") + "_" + items.getJSONObject(0).getString("mutation");
            }
            fileName += "_" + content.getString("diagnosis");
        }
        
        return fileName;
    }
    
    private static void addNewRecord(String reportDataFileId, String reportName, String user, String date, String email, String folderId, String folderName) throws MalformedURLException, GeneralSecurityException, IOException, ServiceException {
        URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + REPORTS_INFO_SHEET_ID);

        SpreadsheetService service = GoogleAuth.getSpreadSheetService();
        SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);
        
        WorksheetFeed worksheetFeed = service.getFeed(
        spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
        List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
        WorksheetEntry worksheet = worksheets.get(0);
        
        // Fetch the list feed of the worksheet.
        URL listFeedUrl = worksheet.getListFeedUrl();
        
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
        if(folderName != null) {
            row.getCustomElements().setValueLocal("foldername", folderName);
        }

        // Send the new row to the API for insertion.
        service.insert(listFeedUrl, row);
    }
    
    private static void changeFileContent(String fileId, String fileName, JSONObject content, JSONArray records) throws MalformedURLException, GeneralSecurityException, IOException, ServiceException {
        URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + fileId);

        SpreadsheetService service = GoogleAuth.getSpreadSheetService();
        SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);

        WorksheetFeed worksheetFeed = service.getFeed(
        spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
        List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
        WorksheetEntry worksheet = worksheets.get(0);

        // Fetch the list feed of the worksheet.
        URL listFeedUrl = worksheet.getListFeedUrl();
        
        System.out.println("Successfully get all entries. Start to add record");
        
        for(int i = 0 ; i < records.length() ; i++) {
            ListEntry row = new ListEntry();
            JSONObject record = records.getJSONObject(i);
            Iterator<String> keys = content.keys();
            while(keys.hasNext()){
                String key = keys.next();
                if(!content.get(key).toString().equals("")) {
                    row.getCustomElements().setValueLocal(key, content.get(key).toString());
                }
            }
            Iterator<String> recordKeys = record.keys();
            while(recordKeys.hasNext()){
                String recordKey = recordKeys.next();
                if(!record.get(recordKey).toString().equals("")) {
                    row.getCustomElements().setValueLocal(recordKey, record.get(recordKey).toString());
                }else{
                    System.out.println(recordKey);
                    System.out.println(content);
                }
            }
            System.out.println("Added one record.");
            service.insert(listFeedUrl, row);
        }
        System.out.println("Done.");
    }
}