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

public class AllTreatmentsWithPMIDs {
    private AllTreatmentsWithPMIDs() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws IOException {
//        List<Gene> genes = ApplicationContextSingleton.getGeneBo().findGenesByHugoSymbol(Collections.singleton("ACVR1"));
        List<Gene> genes = ApplicationContextSingleton.getGeneBo().findAll();
        List<Map<String, String>> allLevels = getRecords();

        System.out.println("Gene\tAlteration\tLevel\tCancer Type\tSubtype\tTreatment\tPMIDs\t Treatment updated from v1.1\tAlteration newly added");
        for (Gene gene : genes) {
            List<Alteration> alterations = ApplicationContextSingleton.getAlterationBo().findAlterationsByGene(Collections.singleton(gene));
            Set<Alteration> alterationsWithoutVUS = AlterationUtils.excludeVUS(new HashSet<>(alterations));
            for (Alteration alteration : alterationsWithoutVUS) {
                List<Alteration> relevantAlts = ApplicationContextSingleton.getAlterationBo().findRelevantAlterations(alteration, alterations);
                List<Evidence> relevantEvidences = ApplicationContextSingleton.getEvidenceBo().findEvidencesByAlteration(relevantAlts);

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

                    if (oncoTreeType != null) {
                        String cancerTypeStr = oncoTreeType.getCancerType() == null ? "" : oncoTreeType.getCancerType();
                        String subtypeStr = oncoTreeType.getSubtype() == null ? "" : oncoTreeType.getSubtype();
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
                            Set<Integer> PMIDs = new HashSet<>();

                            for (Article article : evidence.getArticles()) {
                                Integer articleRep;
                                try {
                                    articleRep = Integer.parseInt(article.getPmid());
                                } catch (NumberFormatException e) {
                                    articleRep = -article.getArticleId();
                                } catch (NullPointerException e) {
                                    articleRep = -article.getArticleId();
                                }
                                PMIDs.add(articleRep);
                            }

                            List<Integer> PMIDsInt = new ArrayList<>(PMIDs);
                            List<String> PMIDsStr = new ArrayList<>();

                            Collections.sort(PMIDsInt);

                            for (Integer pmid : PMIDsInt) {
                                PMIDsStr.add(pmid.toString());
                            }

                            Map<String, String> query = new HashMap<>();
                            query.put("gene", gene.getHugoSymbol());
                            query.put("alteration", alteration.getAlteration());
                            query.put("level", levelStr);
                            query.put("cancertype", cancerTypeStr);
                            query.put("subtype", subtypeStr);
                            query.put("treatment", treatmentStr);
                            query.put("pmids", StringUtils.join(PMIDsStr, ", "));
//                            System.out.println(query.get("gene") + "\t" + query.get("alteration") + "\t" + levelStr + "\t" + cancerTypeStr + "\t" + subtypeStr + "\t" + treatmentStr + "\t" + query.get("pmids"));
                            Map<String, Boolean> match = hasMatch(allLevels, query);
                            System.out.println(query.get("gene") + "\t" + query.get("alteration") + "\t" + levelStr + "\t" + cancerTypeStr + "\t" + subtypeStr + "\t" + treatmentStr + "\t" + query.get("pmids") + "\t" + !match.get("treatment") + "\t" + !match.get("alt"));
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

    
    public static Map<String, Boolean> hasMatch(List<Map<String, String>> records, Map<String, String> query) {
        Boolean hasSameTreatment = false;
        Boolean hasAlt = false;

        if (records.contains(query)) {
            hasSameTreatment = true;
        }
        
        for(Map<String, String> record : records) {
            if(query.get("gene").equals(record.get("gene")) && query.get("alteration").equals(record.get("alteration"))) {
                hasAlt = true;
                break;
            }
        }

        Map<String, Boolean> match = new HashMap<>();
        match.put("treatment", hasSameTreatment);
        match.put("alt", hasAlt);
        return match;
    }

    public static boolean mapsAreEqual(Map<String, String> mapA, Map<String, String> mapB) {

        try {
            for (String k : mapB.keySet()) {
                if (!mapA.get(k).equals(mapB.get(k))) {
                    return false;
                }
            }
            for (String y : mapA.keySet()) {
                if (!mapB.containsKey(y)) {
                    return false;
                }
            }
        } catch (NullPointerException np) {
            return false;
        }
        return true;
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
            URL url = worksheets.get(0).getListFeedUrl();
            ListFeed list = service.getFeed(url, ListFeed.class);
            List<Map<String, String>> records = new ArrayList<>();

            for (ListEntry row : list.getEntries()) {
                Map<String, String> record = new HashMap<>();
                record.put("gene", row.getCustomElements().getValue("gene") == null ? "" : row.getCustomElements().getValue("gene"));
                record.put("alteration", row.getCustomElements().getValue("alteration") == null ? "" : row.getCustomElements().getValue("alteration"));
                record.put("level", row.getCustomElements().getValue("level") == null ? "" : row.getCustomElements().getValue("level"));
                record.put("cancertype", row.getCustomElements().getValue("cancertype") == null ? "" : row.getCustomElements().getValue("cancertype"));
                record.put("subtype", row.getCustomElements().getValue("subtype") == null ? "" : row.getCustomElements().getValue("subtype"));
                record.put("treatment", row.getCustomElements().getValue("treatment") == null ? "" : row.getCustomElements().getValue("treatment"));
                record.put("pmids", row.getCustomElements().getValue("pmids") == null ? "" : row.getCustomElements().getValue("pmids"));
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
