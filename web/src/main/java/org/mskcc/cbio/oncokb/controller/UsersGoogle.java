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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.mskcc.cbio.oncokb.model.GoogleUser;

/**
 *
 * @author zhangh2
 */
@Controller
public class UsersGoogle {
    
    private static final String USER_SPREADSHEET = "1VPFB4KuE3tF07KVIbJzU8V4amjuR8f9az0Rvp6wdKzI";
        
    @RequestMapping(value="/users", method = GET)
    public @ResponseBody List<GoogleUser> UsersGoogle(
            @RequestParam(value="email", required=false) String email) throws MalformedURLException, ServiceException{
        
        try {
            return getUserInfo();
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
    
    public static List<GoogleUser> getUserInfo() throws MalformedURLException, GeneralSecurityException, IOException, ServiceException {
        URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + USER_SPREADSHEET);
        GoogleAuth google = new GoogleAuth();
        SpreadsheetService service = GoogleAuth.createSpreedSheetService();
        SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);
        
        WorksheetFeed worksheetFeed = service.getFeed(
        spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
        List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
        
        //Entry 0: admin, Entry 1: curators
        WorksheetEntry userEntry = worksheets.get(0);
        
        // Fetch the list feed of the worksheet.
        URL userUrl = userEntry.getListFeedUrl();
        ListFeed userList = service.getFeed(userUrl, ListFeed.class);
        // Create a local representation of the new row.
        List<GoogleUser> users = new ArrayList<GoogleUser>(); 
        // Iterate through each row, printing its cell values.
        for (ListEntry row : userList.getEntries()) {
            GoogleUser user = new GoogleUser();
            user.setName(row.getCustomElements().getValue("user"));
            user.setEmail(row.getCustomElements().getValue("email"));
            user.setPermission(Integer.parseInt(row.getCustomElements().getValue("permission")));
            user.setGenes(row.getCustomElements().getValue("genes"));
            users.add(user);
        }
        return users;
    }
}