/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.Link;
import com.google.gdata.data.batch.*;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mskcc.cbio.oncokb.config.GoogleAuth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author jgao
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
        public final String idString;

        /**
         * Constructs a CellAddress representing the specified {@code row} and
         * {@code col}.  The idString will be set in 'RnCn' notation.
         */
        public CellAddress(int row, int col) {
          this.row = row;
          this.col = col;
          this.idString = String.format("R%sC%s", row, col);
        }
    }
    
    @RequestMapping(value="/generateGoogleDoc")
    public void getTumorType(
            @RequestParam(value="tumorTypeId", required=false) List<String> tumorTypeIds) throws IOException, ServiceException, GeneralSecurityException {
        
        String key = "1pSrPQ9RaBGKfAR44wPbHPJ-LzzSEYH5RFCeOo6ASvtM";
        /*[
            ['fileName', 'fileId', 'targetFolder', 'templateId'],
            ['patientName', 'specimen', 'clientNum', 'overallInterpretation'],
            ['diagnosis', 'tumorTissueType', 'specimenSource', 'blockId', 'stage', 'grade'],
            ['geneName', 'mutation', 'alterType', 'mutationFreq', 'tumorTypeDrugs', 'nonTumorTypeDrugs', 'hasClinicalTrial'],
            ['treatment', 'fdaApprovedInTumor', 'fdaApprovedInOtherTumor', 'clinicalTrials', 'background', 'companionDiagnostics']
          ]*/
        
        int numOfRow = 5;
        int[] numOfCol = {5,4,6,7,6};
        
//        URL SPREADSHEET_FEED_URL = FeedURLFactory.getDefault().getWorksheetFeedUrl(key, "private", "full");          
        URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + key);
        SpreadsheetService service = GoogleAuth.createSpreedSheetService();
        SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);
        
        WorksheetFeed worksheetFeed = service.getFeed(
        spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
        List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
        WorksheetEntry worksheet = worksheets.get(0);
        URL cellFeedUrl = worksheet.getCellFeedUrl();
        CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
//        System.out.println(cellFeedUrl);
//        cellFeed.getEntries().get(0).changeInputValueLocal("serves");
//        cellFeed.getEntries().get(0).update();
        // Build list of cell addresses to be filled in
        List<CellAddress> cellAddrs = new ArrayList<CellAddress>();
        for (int row = 1; row <= numOfRow; ++row) {
          for (int col = 1; col <= numOfCol[row-1]; ++col) {
            cellAddrs.add(new CellAddress(row, col));
          }
        }

        // Prepare the update
        // getCellEntryMap is what makes the update fast.
        Map<String, CellEntry> cellEntries = getCellEntryMap(service, cellFeedUrl, cellAddrs);

        CellFeed batchRequest = new CellFeed();
        for (CellAddress cellAddr : cellAddrs) {
            CellEntry batchEntry = new CellEntry(cellEntries.get(cellAddr.idString));
            if(cellAddr.row == 1 && cellAddr.col == 2){
                StringBuffer sb = new StringBuffer();  
                for (int x = 0; x < 20; x++)  
                {  
                  sb.append((char)((int)(Math.random()*26)+97));  
                }

                batchEntry.changeInputValueLocal(sb.toString());
            }else {
                batchEntry.changeInputValueLocal(batchEntry.getCell().getValue());
            }
          BatchUtils.setBatchId(batchEntry, cellAddr.idString);
          BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.UPDATE);
          batchRequest.getEntries().add(batchEntry);
        }

        // Submit the update
        Link batchLink = cellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
        CellFeed batchResponse = service.batch(new URL(batchLink.getHref()), batchRequest);

        // Check the results
        boolean isSuccess = true;
        for (CellEntry entry : batchResponse.getEntries()) {
          String batchId = BatchUtils.getBatchId(entry);
          if (!BatchUtils.isSuccess(entry)) {
            isSuccess = false;
            BatchStatus status = BatchUtils.getBatchStatus(entry);
            System.out.printf("%s failed (%s) %s", batchId, status.getReason(), status.getContent());
          }
        }
    }
    
            
    /**
    * Connects to the specified {@link SpreadsheetService} and uses a batch
    * request to retrieve a {@link CellEntry} for each cell enumerated in {@code
    * cellAddrs}. Each cell entry is placed into a map keyed by its RnCn
    * identifier.
    *
    * @param ssSvc the spreadsheet service to use.
    * @param cellFeedUrl url of the cell feed.
    * @param cellAddrs list of cell addresses to be retrieved.
    * @return a map consisting of one {@link CellEntry} for each address in {@code
    *         cellAddrs}
    */
    public static Map<String, CellEntry> getCellEntryMap(
        SpreadsheetService ssSvc, URL cellFeedUrl, List<CellAddress> cellAddrs)
        throws IOException, ServiceException {
      CellFeed batchRequest = new CellFeed();
      for (CellAddress cellId : cellAddrs) {
        CellEntry batchEntry = new CellEntry(cellId.row, cellId.col, cellId.idString);
        batchEntry.setId(String.format("%s/%s", cellFeedUrl.toString(), cellId.idString));
        BatchUtils.setBatchId(batchEntry, cellId.idString);
        BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.QUERY);
        batchRequest.getEntries().add(batchEntry);
      }

      CellFeed cellFeed = ssSvc.getFeed(cellFeedUrl, CellFeed.class);
      CellFeed queryBatchResponse =
        ssSvc.batch(new URL(cellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM).getHref()),
                    batchRequest);

      Map<String, CellEntry> cellEntryMap = new HashMap<String, CellEntry>(cellAddrs.size());
      for (CellEntry entry : queryBatchResponse.getEntries()) {
        cellEntryMap.put(BatchUtils.getBatchId(entry), entry);
        System.out.printf("batch %s {CellEntry: id=%s editLink=%s inputValue=%s\n",
            BatchUtils.getBatchId(entry), entry.getId(), entry.getEditLink().getHref(),
            entry.getCell().getInputValue());
      }

      return cellEntryMap;
    }
}