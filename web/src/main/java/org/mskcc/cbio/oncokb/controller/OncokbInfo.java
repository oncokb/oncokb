/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mskcc.cbio.oncokb.config.GoogleAuth;
import org.mskcc.cbio.oncokb.model.CurationSuggestion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.ResponseBody;
import org.mskcc.cbio.oncokb.model.GoogleUser;
import org.mskcc.cbio.oncokb.model.PubMed;

/**
 *
 * @author zhangh2
 */
@Controller
public class OncokbInfo {
    
    private static final String USER_SPREADSHEET = "1VPFB4KuE3tF07KVIbJzU8V4amjuR8f9az0Rvp6wdKzI";
        
    @RequestMapping(value="/oncokbInfo.json", method = GET)
    public @ResponseBody Map OncokbInfo() throws MalformedURLException, ServiceException{
        try {
            URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + USER_SPREADSHEET);
            GoogleAuth auth = new GoogleAuth();
            SpreadsheetService service = auth.getSpreadSheetService();
            SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);
            
            WorksheetFeed worksheetFeed = service.getFeed(
                    spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
            List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
            
            Map<String, List> oncokbInfo = new HashMap<String, List>();
            oncokbInfo.put("users", getUsersInfo(worksheets.get(0), service));
            oncokbInfo.put("suggestions", getSuggestionsInfo(worksheets.get(1), service));
            oncokbInfo.put("pubMed", getPubMedInfo(worksheets.get(2), service));
            
            return oncokbInfo;
        } catch (MalformedURLException ex) {
            Logger.getLogger(OncokbInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(OncokbInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OncokbInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(OncokbInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static List<GoogleUser> getUsersInfo(WorksheetEntry entry, SpreadsheetService service) throws IOException, ServiceException {
        URL userUrl = entry.getListFeedUrl();
        ListFeed userList = service.getFeed(userUrl, ListFeed.class);
        // Create a local representation of the new row.
        List<GoogleUser> users = new ArrayList<GoogleUser>();
        // Iterate through each row, printing its cell values.
        for (ListEntry row : userList.getEntries()) {
            GoogleUser user = new GoogleUser();
            user.setName(row.getCustomElements().getValue("user"));
            user.setEmail(row.getCustomElements().getValue("email"));
            user.setMskccEmail(row.getCustomElements().getValue("mskccemail"));
            user.setRole(Integer.parseInt(row.getCustomElements().getValue("role")));
            user.setGenes(row.getCustomElements().getValue("genes"));
            user.setPhases(row.getCustomElements().getValue("phases"));
            users.add(user);
        }
        return users;
    }
    
    private static List<CurationSuggestion> getSuggestionsInfo(WorksheetEntry entry, SpreadsheetService service) throws IOException, ServiceException {
        // Fetch the list feed of the worksheet.
        URL url = entry.getListFeedUrl();
        ListFeed list = service.getFeed(url, ListFeed.class);
        List<CurationSuggestion> suggestions = new ArrayList<CurationSuggestion>(); 
        // Iterate through each row, printing its cell values.
        for (ListEntry row : list.getEntries()) {
            CurationSuggestion suggestion = new CurationSuggestion();
            suggestion.setGene(row.getCustomElements().getValue("gene"));
            suggestion.setMutations(row.getCustomElements().getValue("mutations"));
            suggestions.add(suggestion);
        }
        return suggestions;
    }
    
    private static List<PubMed> getPubMedInfo(WorksheetEntry entry, SpreadsheetService service) throws IOException, ServiceException {
        // Fetch the list feed of the worksheet.
        URL url = entry.getListFeedUrl();
        ListFeed list = service.getFeed(url, ListFeed.class);
        List<PubMed> links = new ArrayList<PubMed>(); 
        // Iterate through each row, printing its cell values.
        for (ListEntry row : list.getEntries()) {
            PubMed link = new PubMed();
            link.setGene(row.getCustomElements().getValue("gene"));
            link.setLinks(row.getCustomElements().getValue("links"));
            link.setMutationLinks(row.getCustomElements().getValue("mutationlinks"));
            links.add(link);
        }
        return links;
    }
}