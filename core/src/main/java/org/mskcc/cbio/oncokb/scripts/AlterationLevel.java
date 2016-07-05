/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.scripts;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @author zhangh2
 */

public class AlterationLevel {
    private AlterationLevel() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws IOException {
        List<Gene> genes = ApplicationContextSingleton.getGeneBo().findAll();
        List<Map<String, String>> allLevels = getRecords();

        System.out.println("Gene\tAlteration\tCancer Type\tSubtype\tLevel\tTreatment\tExist");
        for (Gene gene : genes) {
            List<Alteration> alterations = ApplicationContextSingleton.getAlterationBo().findAlterationsByGene(Collections.singleton(gene));
            Set<Alteration> alterationsWithoutVUS = AlterationUtils.excludeVUS(new HashSet<>(alterations));
            for (Alteration alteration : alterationsWithoutVUS) {
                List<Alteration> relevantAlts = ApplicationContextSingleton.getAlterationBo().findRelevantAlterations(alteration, alterations);
                List<Evidence> relevantEvidences = ApplicationContextSingleton.getEvidenceBo().findEvidencesByAlteration(relevantAlts);
//                LevelOfEvidence levelOfEvidence = LevelUtils.getHighestLevelFromEvidence(new HashSet<>(relevantEvidences));

                for (Evidence evidence : relevantEvidences) {
                    LevelOfEvidence level = evidence.getLevelOfEvidence();
                    String tumortype = evidence.getSubtype();
                    OncoTreeType oncoTreeType = null;

                    if (tumortype != null) {
                        oncoTreeType = TumorTypeUtils.getOncoTreeSubtypeByCode(tumortype);
                    } else {
                        tumortype = evidence.getCancerType();
                        if (tumortype != null) {
                            oncoTreeType = TumorTypeUtils.getOncoTreeCancerType(tumortype);
                        }
                    }
                    String levelStr = "";

                    if (level != null && level.getLevel() != null) {
                        levelStr = level.getLevel().toUpperCase();
                    }

                    if (oncoTreeType != null && (levelStr.equals("1") || levelStr.equals("2A"))) {
                        String cancerTypeStr = oncoTreeType.getCancerType();
                        String subtypeStr = oncoTreeType.getSubtype();
                        Set<Treatment> treatments = evidence.getTreatments();
                        List<String> treatmentNames = new ArrayList<>();

                        for (Treatment treatment : treatments) {
                            Set<Drug> drugs = treatment.getDrugs();
                            List<String> drugNames = new ArrayList<>();

                            for (Drug drug : drugs) {
                                if (drug.getDrugName() != null) {
                                    drugNames.add(drug.getDrugName());
                                }
                            }
                            String drugStr = StringUtils.join(drugNames, " + ");

                            treatmentNames.add(drugStr);
                        }

                        String treatmentStr = StringUtils.join(treatmentNames, ", ");

                        if (treatmentStr != null && !treatmentStr.equals("")) {
                            Map<String, String> query = new HashMap<>();
                            query.put("gene", gene.getHugoSymbol());
                            query.put("alteration", alteration.getAlteration());
                            query.put("level", levelStr);
                            query.put("tumorType", subtypeStr == null ? cancerTypeStr : subtypeStr);
                            query.put("treatment", treatmentStr);
                            System.out.println(gene.getHugoSymbol() + "\t" + alteration.getAlteration() + "\t" + cancerTypeStr + "\t" + subtypeStr + "\t" + levelStr + "\t" + treatmentStr + "\t" + hasMatch(allLevels, query));
                        }
                    }
                }

//                String level = "NA";
//
//                if (levelOfEvidence != null && levelOfEvidence.getLevel() != null) {
//                    level = levelOfEvidence.getLevel().toUpperCase();
//                }
            }
        }
    }

    public static Boolean hasMatch(List<Map<String, String>> records, Map<String, String> query) {
        Boolean hasMatch = false;

        if (records.contains(query)) {
            hasMatch = true;
        }
        return hasMatch;
    }

    public static List<Map<String, String>> getRecords() {
        try {
            String allLevels = PropertiesUtils.getProperties("google.all_levels_final");
            URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + allLevels);
            SpreadsheetService service = GoogleAuth.getSpreadSheetService();
            SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);

            WorksheetFeed worksheetFeed = service.getFeed(
                spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
            List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
            URL url = worksheets.get(2).getListFeedUrl();
            ListFeed list = service.getFeed(url, ListFeed.class);
            List<Map<String, String>> records = new ArrayList<>();

            for (ListEntry row : list.getEntries()) {
                Map<String, String> record = new HashMap<>();
                record.put("gene", row.getCustomElements().getValue("gene"));
                record.put("alteration", row.getCustomElements().getValue("alteration"));
                record.put("level", row.getCustomElements().getValue("level"));
                record.put("tumorType", row.getCustomElements().getValue("tumortype"));
                record.put("treatment", row.getCustomElements().getValue("treatment"));
                records.add(record);
            }
            return records;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
