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
import org.mskcc.cbio.oncokb.model.CurationSuggestion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author zhangh2
 */
@Controller
public class SuggestedMutations {
    
    private static final String USER_SPREADSHEET = "1VPFB4KuE3tF07KVIbJzU8V4amjuR8f9az0Rvp6wdKzI";
    private static final Integer ENTRY = 1;
        
    @RequestMapping(value="/curationSuggestions.json", method = GET)
    public @ResponseBody List<CurationSuggestion> SuggestedMutations() throws MalformedURLException, ServiceException, GeneralSecurityException{
        
        try {
            URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + USER_SPREADSHEET);
            SpreadsheetService service = GoogleAuth.getSpreadSheetService();
            SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);

            WorksheetFeed worksheetFeed = service.getFeed(
            spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
            List<WorksheetEntry> worksheets = worksheetFeed.getEntries();

            WorksheetEntry userEntry = worksheets.get(ENTRY);

            // Fetch the list feed of the worksheet.
            URL userUrl = userEntry.getListFeedUrl();
            ListFeed userList = service.getFeed(userUrl, ListFeed.class);
            // Create a local representation of the new row.
            List<CurationSuggestion> suggestions = new ArrayList<CurationSuggestion>(); 
            // Iterate through each row, printing its cell values.
            for (ListEntry row : userList.getEntries()) {
                CurationSuggestion suggestion = new CurationSuggestion();
                suggestion.setGene(row.getCustomElements().getValue("gene"));
                suggestion.setMutations(row.getCustomElements().getValue("mutations"));
                suggestions.add(suggestion);
            }
            return suggestions;
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
        }
        
        return null;
    }
}