/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

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
public class CreateGoogleFolder {
    private static final String REPORT_PARENT_FOLDER = "0BzBfo69g8fP6fjZSQzlXYVdXaUJVcjllNS1CRnp5eW1LcnlLSDlXRDFFT0w0WkNBek1FTlE";
    
//    @RequestMapping(value="/legacy-api/createGoogleFolder", method = POST)
    public @ResponseBody String CreateGoogleFolder(
            @RequestParam(value="folderName", required=true) String folderName) throws MalformedURLException, ServiceException, GeneralSecurityException, URISyntaxException{
        try {
            if(folderName != null && !folderName.equals("")) {
                Drive driveService = GoogleAuth.getDriveService();
                System.out.println("Got drive service");
                
                File folder = new File();
                folder.setTitle(folderName);
                folder.setMimeType("application/vnd.google-apps.folder");
                folder.setParents(Arrays.asList(new ParentReference().setId(REPORT_PARENT_FOLDER)));
                folder = driveService.files().insert(folder).execute();
                System.out.println("New folder created.");
                return folder.getId();
            }else {
                throw new Error("Invalid folder name.");
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
        }
        throw new Error("Error.");
    }
}
