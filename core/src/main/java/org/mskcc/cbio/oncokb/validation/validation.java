/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.validation;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ParentReference;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;
import org.mskcc.cbio.oncokb.util.GoogleAuth;

/**
 *
 * @author jiaojiao
 */
public class validation {
    private static final String REPORT_PARENT_FOLDER = "0BzBfo69g8fP6fkNjY0RSQlNtRUgyRHVNeWJ5ZzFkU2twVGtYR19Bb1dld0JZY0VPd3hCTms";
    private static final String REPORT_DATA_TEMPLATE = "1yjcti-clYq61RKAPcXD33lutcjCW82FLwk8KaPntO7A";
    public static void main(String[] args) throws IOException, GeneralSecurityException, URISyntaxException, ServiceException {
        Drive driveService = GoogleAuth.getDriveService();
        System.out.println("Got drive service");
        String fileName = "Validator Report";            
        com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
        file.setTitle(fileName);
        file.setParents(Arrays.asList(new ParentReference().setId(REPORT_PARENT_FOLDER)));
        file.setDescription("New File created from server");

        System.out.println("Copying file");

        file = driveService.files().copy(REPORT_DATA_TEMPLATE, file).execute();

        System.out.println("Successfully copied file. Start to change file content");
        
        String fileId = file.getId();
        URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + fileId);

        SpreadsheetService service = GoogleAuth.getSpreadSheetService();
        SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);

        WorksheetFeed worksheetFeed = service.getFeed(
        spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
        List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
        WorksheetEntry worksheet1 = worksheets.get(1), 
                worksheet2 = worksheets.get(2), 
                worksheet3 = worksheets.get(3), 
                worksheet4 = worksheets.get(4), 
                worksheet5 = worksheets.get(5), 
                worksheet6 = worksheets.get(6);
        
        // Fetch the list feed of the worksheets.
        URL listFeedUrl1 = worksheet1.getListFeedUrl(), 
            listFeedUrl2 = worksheet2.getListFeedUrl(), 
            listFeedUrl3 = worksheet3.getListFeedUrl(), 
            listFeedUrl4 = worksheet4.getListFeedUrl(), 
            listFeedUrl5 = worksheet5.getListFeedUrl(), 
            listFeedUrl6 = worksheet6.getListFeedUrl();
        

        Map<Gene, Set<Evidence>> allGeneBasedEvidences = EvidenceUtils.getAllGeneBasedEvidences();
        Set<Gene> genes = GeneUtils.getAllGenes();
        Integer count = 0;
        ArrayList<String> specialAlterations = new ArrayList<>(Arrays.asList("Inactivating Mutations", "Activating Mutations", "Fusions", "Inactivating", "Wildtype", "Amplification", "Fusions"));
        for (Gene gene : genes) {
            Set<Evidence> evidences = allGeneBasedEvidences.get(gene);
            Set<Alteration> VUSAlterations = AlterationUtils.findVUSFromEvidences(evidences);
            Map<Alteration, ArrayList<Alteration>> relevantAlterationsMapping = new HashMap<Alteration, ArrayList<Alteration>>();
            Map<Alteration, String> oncogenicityMapping = new HashMap<Alteration, String>();
            Map<Alteration, String> mutationEffectMapping = new HashMap<Alteration, String>();
            Map<Alteration, Set<Evidence>> multipleMutationEffects = new HashMap<>();
            Map<Alteration, Set<String>> referencesMapping = new HashMap<Alteration, Set<String>>();
            ArrayList<Alteration> altsWithDescriptions = new ArrayList<Alteration>();
            Set<Alteration> allVariants = new HashSet<Alteration>();
            Set<Alteration> allAlts = new HashSet<Alteration>();
            for (Evidence evidenceItem : evidences) {
                allVariants = evidenceItem.getAlterations();
                allAlts.addAll(allVariants);
                for (Alteration alterationItem : allVariants) {
                    relevantAlterationsMapping.put(alterationItem, new ArrayList<Alteration>(AlterationUtils.getRelevantAlterations(gene, alterationItem.getAlteration(), null, null, null)) );
                    if (evidenceItem.getEvidenceType().toString().equals("ONCOGENIC")) {
                        oncogenicityMapping.put(alterationItem, evidenceItem.getKnownEffect());
                    }
                    if (evidenceItem.getEvidenceType().toString().equals("MUTATION_EFFECT")) {
                        mutationEffectMapping.put(alterationItem, evidenceItem.getKnownEffect());
                    }
                    if (evidenceItem.getEvidenceType().toString().equals("MUTATION_EFFECT")) {
                        if (!multipleMutationEffects.containsKey(alterationItem)) {
                            multipleMutationEffects.put(alterationItem, new HashSet<Evidence>());
                        }
                        multipleMutationEffects.get(alterationItem).add(evidenceItem);
                    }
                    if(referencesMapping.containsKey(alterationItem)){
                        Set<String> oldPMIDs = referencesMapping.get(alterationItem);
                        Set<String> newPMIDs = EvidenceUtils.getPmids(new HashSet<Evidence>(Arrays.asList(evidenceItem)));
                        newPMIDs.addAll(oldPMIDs);
                        referencesMapping.put(alterationItem, newPMIDs);
                    }else{
                        referencesMapping.put(alterationItem, EvidenceUtils.getPmids(new HashSet<Evidence>(Arrays.asList(evidenceItem))));
                    }
                    if(!altsWithDescriptions.contains(alterationItem)){    
                        if(evidenceItem.getDescription() != null && !evidenceItem.getDescription().isEmpty() || evidenceItem.getShortDescription() != null && !evidenceItem.getShortDescription().isEmpty()){
                            altsWithDescriptions.add(alterationItem);
                        }
                    }
                }
            }
            for (Alteration alt : allAlts) {
                ArrayList<Alteration> relevantAlts = relevantAlterationsMapping.get(alt);
                for (Alteration relevantAlt : relevantAlts) {
                    if (oncogenicityMapping.containsKey(alt)
                        && oncogenicityMapping.get(alt) != null
                        && oncogenicityMapping.get(relevantAlt) != null
                        && oncogenicityMapping.containsKey(relevantAlt) 
                        && !oncogenicityMapping.get(alt).equals(oncogenicityMapping.get(relevantAlt))) {
                        ListEntry row = new ListEntry();
                        row.getCustomElements().setValueLocal("Gene", alt.getGene().getHugoSymbol());
                        row.getCustomElements().setValueLocal("Alteration", alt.getAlteration());
                        row.getCustomElements().setValueLocal("Oncogenicty", Oncogenicity.getByLevel(oncogenicityMapping.get(alt)).getDescription() );
                        row.getCustomElements().setValueLocal("RelevantAlteration", relevantAlt.getAlteration());
                        row.getCustomElements().setValueLocal("RelevantAlterationOncogenicty", Oncogenicity.getByLevel(oncogenicityMapping.get(relevantAlt)).getDescription());
                        service.insert(listFeedUrl1, row);
                        break;
                    }
                }
                for (Alteration relevantAlt : relevantAlts) {
                    if (mutationEffectMapping.containsKey(alt) && mutationEffectMapping.containsKey(relevantAlt) &&
                        mutationEffectMapping.get(alt) != null && mutationEffectMapping.get(relevantAlt) != null &&
                        !mutationEffectMapping.get(alt).equals(mutationEffectMapping.get(relevantAlt))) {
                        ListEntry row = new ListEntry();
                        row.getCustomElements().setValueLocal("Gene", alt.getGene().getHugoSymbol());
                        row.getCustomElements().setValueLocal("Alteration", alt.getAlteration());
                        row.getCustomElements().setValueLocal("MutationEffect", mutationEffectMapping.get(alt) );
                        row.getCustomElements().setValueLocal("RelevantAlteration", relevantAlt.getAlteration());
                        row.getCustomElements().setValueLocal("RelevantAlterationMutationEffect", mutationEffectMapping.get(relevantAlt));
                        service.insert(listFeedUrl2, row);
                        break;
                    }
                }
                if (oncogenicityMapping.containsKey(alt)
                    && oncogenicityMapping.get(alt) != null
                    && oncogenicityMapping.get(alt).equals(Oncogenicity.UNKNOWN.getOncogenic()) 
                    && mutationEffectMapping.containsKey(alt) 
                    && mutationEffectMapping.get(alt) != null
                    && mutationEffectMapping.get(alt).equals(MutationEffect.UNKNOWN.getMutation_effect())) {
                    Integer relevantsSize = relevantAlts.size();
                    Integer relevantCount = 0;
                    for (Alteration relevantAlt : relevantAlts) {
                        relevantCount++;
                        if (relevantCount == relevantsSize - 1 && oncogenicityMapping.containsKey(alt) && oncogenicityMapping.get(relevantAlt).equals(Oncogenicity.UNKNOWN.getOncogenic()) 
                                && mutationEffectMapping.containsKey(alt) && mutationEffectMapping.get(relevantAlt).equals(MutationEffect.UNKNOWN.getMutation_effect())) {
                            ListEntry row = new ListEntry();
                        row.getCustomElements().setValueLocal("Gene", relevantAlt.getGene().getHugoSymbol());
                        row.getCustomElements().setValueLocal("Alteration", relevantAlt.getAlteration());
                        service.insert(listFeedUrl3, row);

                        }
                    }
                }
                if (!oncogenicityMapping.containsKey(alt) && !mutationEffectMapping.containsKey(alt) && !VUSAlterations.contains(alt) && !specialAlterations.contains(alt.getAlteration()) && !altsWithDescriptions.contains(alt)) {
                    ListEntry row = new ListEntry();
                        row.getCustomElements().setValueLocal("Gene", alt.getGene().getHugoSymbol());
                        row.getCustomElements().setValueLocal("Alteration", alt.getAlteration());
                        service.insert(listFeedUrl4, row);

                }
                if (oncogenicityMapping.containsKey(alt) && mutationEffectMapping.containsKey(alt) && referencesMapping.get(alt).size() == 0) {
                    ListEntry row = new ListEntry();
                        row.getCustomElements().setValueLocal("Gene", alt.getGene().getHugoSymbol());
                        row.getCustomElements().setValueLocal("Alteration", alt.getAlteration());
                        service.insert(listFeedUrl5, row);

                }
                if (multipleMutationEffects.containsKey(alt) && multipleMutationEffects.get(alt).size() > 1) {
                    ListEntry row = new ListEntry();
                        row.getCustomElements().setValueLocal("Gene", alt.getGene().getHugoSymbol());
                        row.getCustomElements().setValueLocal("Alteration", alt.getAlteration());
                        service.insert(listFeedUrl6, row);

                }

            }
            count++;
            System.out.println("Processing " + gene.getHugoSymbol() + "  " + 100 * count / genes.size() + "% finished");
        }
    }
}
