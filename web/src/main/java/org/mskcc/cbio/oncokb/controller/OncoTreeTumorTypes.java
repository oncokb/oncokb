/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ArrayList;
import org.mskcc.cbio.oncokb.config.GoogleAuth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.ResponseBody;
import org.mskcc.cbio.oncokb.model.OncoTreeTumorType;

/**
 *
 * @author zhangh2
 */
@Controller
public class OncoTreeTumorTypes {
    
    private static final String USER_SPREADSHEET = "0AhyhUieStXV-dFdXbGZ6dGFIRE0tWTJ5RGpYV2E5Y2c";
    private static final Integer ENTRY = 1;
    
    @RequestMapping(value="/oncoTreeTumorTypes.json", method = GET)
    public @ResponseBody List<OncoTreeTumorType> OncoTreeTumorTypes() throws MalformedURLException, ServiceException{
        
        try {
            return geMainTypes();
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
        
        return null;
    }
    
    public ListFeed getListFeed() throws MalformedURLException, IOException, ServiceException, GeneralSecurityException {
        URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + USER_SPREADSHEET);
        SpreadsheetService service = GoogleAuth.getSpreadSheetService();
        SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);
        
        WorksheetFeed worksheetFeed = service.getFeed(
        spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
        List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
        
        //Entry 0: admin, Entry 1: curators
        WorksheetEntry entry = worksheets.get(ENTRY);
        
        // Fetch the list feed of the worksheet.
        URL url = entry.getListFeedUrl();
        ListFeed list = service.getFeed(url, ListFeed.class);
        
        return list;
    }
    
    public List<OncoTreeTumorType> getOncoTreeTumorTypes() throws IOException, MalformedURLException, ServiceException, GeneralSecurityException {
        ListFeed list = getListFeed();
        // Create a local representation of the new row.
        List<OncoTreeTumorType> tumorTypes = new ArrayList<OncoTreeTumorType>(); 
        // Iterate through each row, printing its cell values.
        for (ListEntry row : list.getEntries()) {
            OncoTreeTumorType tumorType = new OncoTreeTumorType();
            tumorType.setPrimary(row.getCustomElements().getValue("primary"));
            tumorType.setSecondary(row.getCustomElements().getValue("secondary"));
            tumorType.setTertiary(row.getCustomElements().getValue("tertiary"));
            tumorType.setQuaternary(row.getCustomElements().getValue("quaternary"));
            tumorTypes.add(tumorType);
        }
        return tumorTypes;
    }
    
    public List<OncoTreeTumorType> geMainTypes() throws IOException, MalformedURLException, ServiceException, GeneralSecurityException {
        ListFeed list = getListFeed();
        List tumorTypes = new ArrayList(); 
        // Iterate through each row, printing its cell values.
        for (ListEntry row : list.getEntries()) {
            Object type = row.getCustomElements().getValue("metamaintype");
            if(!tumorTypes.contains(type)){
                tumorTypes.add(type);
            }
        }
        return tumorTypes;
    }
}