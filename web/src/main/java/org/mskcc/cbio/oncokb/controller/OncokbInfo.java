/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import org.mskcc.cbio.oncokb.model.GoogleUser;
import org.mskcc.cbio.oncokb.util.GoogleAuth;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author zhangh2
 */
@Controller
public class OncokbInfo {
    @RequestMapping(value = "/legacy-api/oncokbInfo.json", method = GET)
    public
    @ResponseBody
    Map OncokbInfo() throws MalformedURLException, ServiceException {
        try {
            String USER_SPREADSHEET = PropertiesUtils.getProperties("google.oncokb.user");
            URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + USER_SPREADSHEET);
            SpreadsheetService service = GoogleAuth.getSpreadSheetService();
            SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);

            WorksheetFeed worksheetFeed = service.getFeed(
                spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
            List<WorksheetEntry> worksheets = worksheetFeed.getEntries();

            Map<String, List> oncokbInfo = new HashMap<String, List>();
            oncokbInfo.put("users", getUsersInfo(worksheets.get(0), service));

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
}