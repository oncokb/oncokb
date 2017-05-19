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
import org.mskcc.oncotree.model.TumorType;

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
//        List<Gene> genes = ApplicationContextSingleton.getGeneBo().findGenesByHugoSymbol(Collections.singleton("ALK"));
        List<Map<String, Object>> allLevels = getRecords();
        List<Map<String, Object>> newList = new ArrayList<>();

        System.out.println("Gene\tAlteration\tTumor Type\tLevel\tTreatment\tPMIDs\tExist");
        for (Gene gene : genes) {
            List<Alteration> alterations = ApplicationContextSingleton.getAlterationBo().findAlterationsByGene(Collections.singleton(gene));
            List<Alteration> alterationsWithoutVUS = AlterationUtils.excludeVUS(alterations);
            for (Alteration alteration : alterationsWithoutVUS) {
                LinkedHashSet<Alteration> relevantAlts = ApplicationContextSingleton.getAlterationBo().findRelevantAlterations(alteration, alterations);
                List<Evidence> relevantEvidences = ApplicationContextSingleton.getEvidenceBo().findEvidencesByAlteration(relevantAlts);
//                LevelOfEvidence levelOfEvidence = LevelUtils.getHighestLevelFromEvidence(new HashSet<>(relevantEvidences));

                for (Evidence evidence : relevantEvidences) {
                    LevelOfEvidence level = evidence.getLevelOfEvidence();
                    TumorType oncoTreeType = evidence.getOncoTreeType();
                    String levelStr = "";

                    if (level != null && level.getLevel() != null) {
                        levelStr = level.getLevel().toUpperCase();
                    }

                    if (oncoTreeType != null && (levelStr.equals("1") || levelStr.equals("2A") || levelStr.equals("3A"))) {
                        String cancerTypeStr = oncoTreeType.getMainType() == null ? null : oncoTreeType.getMainType().getName();
                        String subtypeStr = oncoTreeType.getName();
                        Set<Treatment> treatments = evidence.getTreatments();
                        List<String> treatmentNames = new ArrayList<>();
                        Set<String> pmids = new HashSet<>();

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

                        for(Article article : evidence.getArticles()) {
                            pmids.add(article.getPmid());
                        }

                        String treatmentStr = StringUtils.join(treatmentNames, ", ");

                        if (treatmentStr != null && !treatmentStr.equals("")) {
                            Map<String, Object> query = new HashMap<>();
                            query.put("gene", gene.getHugoSymbol());
                            query.put("alteration", alteration.getAlteration());
                            query.put("level", levelStr);
                            query.put("tumorType", subtypeStr == null ? cancerTypeStr : subtypeStr);
                            query.put("treatment", treatmentStr);
                            query.put("pmids", pmids);
                            newList.add(query);
                            System.out.println(gene.getHugoSymbol() + "\t" + alteration.getAlteration() + "\t" + (subtypeStr == null ? cancerTypeStr : subtypeStr) + "\t" + levelStr + "\t" + treatmentStr + "\t" + sortPMIDs(StringUtils.join(pmids, ", ")) + "\t" + hasMatch(allLevels, query));
                        }
                    }
                }
            }
        }

        System.out.println("Comparing with old list to identify anything not in the new list");

        //Don't compare pmids
        for(Map<String, Object> query : newList) {
            query.remove("pmids");
        }
        for(Map<String, Object> record : allLevels) {
            record.remove("pmids");
            if(!newList.contains(record)) {
                System.out.println(record.get("gene") + "\t" + record.get("alteration") + "\t" + record.get("level") + "\t" + record.get("tumorType") + "\t" + record.get("treatment"));
            }
        }
    }

    public static Boolean hasMatch(List<Map<String, Object>> records, Map<String, Object> query) {
        Boolean hasMatch = false;


        for(Map<String, Object> record : records) {
            if(sameRecord(record, query)){
                hasMatch = true;
                break;
            }
        }
        return hasMatch;
    }

    private static Boolean sameRecord(Map<String, Object>record, Map<String, Object>query) {
        Boolean hasMatch = true;
        if(!record.get("gene").equals(query.get("gene"))) {
            hasMatch = false;
        }
        if(!record.get("alteration").equals(query.get("alteration"))) {
            hasMatch = false;
        }
        if(!record.get("level").equals(query.get("level"))) {
            hasMatch = false;
        }
        if(!record.get("tumorType").equals(query.get("tumorType"))) {
            hasMatch = false;
        }
        if(!record.get("treatment").equals(query.get("treatment"))) {
            hasMatch = false;
        }
        Set<String> recordpmids = (Set<String>) record.get("pmids");
        Set<String> querypmids = (Set<String>) query.get("pmids");
        if(!querypmids.containsAll(recordpmids)) {
            hasMatch = false;
        }
        return hasMatch;
    }
    public static List<Map<String, Object>> getRecords() {
        try {
            String allLevels = PropertiesUtils.getProperties("google.all_levels_final");
            URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + allLevels);
            SpreadsheetService service = GoogleAuth.getSpreadSheetService();
            SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);

            WorksheetFeed worksheetFeed = service.getFeed(
                spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
            List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
            URL url = worksheets.get(4).getListFeedUrl();
            ListFeed list = service.getFeed(url, ListFeed.class);
            List<Map<String, Object>> records = new ArrayList<>();

            for (ListEntry row : list.getEntries()) {
                Map<String, Object> record = new HashMap<>();
                record.put("gene", row.getCustomElements().getValue("gene"));
                record.put("alteration", row.getCustomElements().getValue("alteration"));
                record.put("level", row.getCustomElements().getValue("level"));
                record.put("tumorType", row.getCustomElements().getValue("tumortype"));
                record.put("treatment", row.getCustomElements().getValue("treatment"));
                record.put("pmids", convertToSetPmids(row.getCustomElements().getValue("pmids")));
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

    private static Set<String> convertToSetPmids(String pmids) {
        Set<String> pmidList = new HashSet<>();
        String[] list = StringUtils.split(pmids,", ");

        for(String pmid : list) {
            pmidList.add(StringUtils.trim(pmid));
        }
        return pmidList;
    }
    private static String sortPMIDs(String pmids) {
        Set<String> pmidList = convertToSetPmids(pmids);

        String[] newList = pmidList.toArray(new String[pmidList.size()]);
        Arrays.sort(newList);

        return StringUtils.join(newList, ", ");
    }
}
