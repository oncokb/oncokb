/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.oncotree.TumorType;
import org.mskcc.cbio.oncokb.util.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jiaojiao
 */
public class validation {
    static Drive driveService;
    static SpreadsheetService spreadsheetService;

    static List<WorksheetEntry> worksheets;

    private static void initialService() throws GeneralSecurityException, IOException, URISyntaxException, ServiceException {
        driveService = GoogleAuth.getDriveService();
        spreadsheetService = GoogleAuth.getSpreadSheetService();
    }

    private static void getWorksheets() throws IOException, ServiceException {
        String propFileName = "properties/config.properties";
        Properties prop = new Properties();
        ValidationConfig config = new ValidationConfig();
        InputStream inputStream = config.getStram(propFileName);

        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException ex) {
                Logger.getLogger(validation.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }

        String REPORT_PARENT_FOLDER = prop.getProperty("google.report_parent_folder");
        String REPORT_DATA_TEMPLATE = prop.getProperty("google.report_data_template");

        System.out.println("Got drive service");

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();

        String fileName = "Data run " + dateFormat.format(date);
        File file = new File();
        file.setTitle(fileName);
        file.setParents(Arrays.asList(new ParentReference().setId(REPORT_PARENT_FOLDER)));
        file.setDescription("New File created from server");

        System.out.println("Copying file");

        file = driveService.files().copy(REPORT_DATA_TEMPLATE, file).execute();

        System.out.println("Successfully copied file. Start to change file content");

        String fileId = file.getId();
        URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full/" + fileId);

        SpreadsheetEntry spreadSheetEntry = spreadsheetService.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);

        WorksheetFeed worksheetFeed = spreadsheetService.getFeed(
            spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
        worksheets = worksheetFeed.getEntries();
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException, URISyntaxException, ServiceException {
        initialService();
        if (driveService == null || spreadsheetService == null) {
            System.out.println("ERROR: service is not available\n");
            return;
        }

        getWorksheets();

        System.out.println("Updating actionable genes.\n");
        compareActionableGenes();

        System.out.println("Checking for empty clinical data...\n");
        getEmptyClinicalVariants();

        System.out.println("Checking for empty biological data...\n");
        getEmptyBiologicalVariants();

        System.out.println("Getting all tumor type summaries...\n");
        printTumorTypeSummary();

        System.out.println("Checking gene summary and background");
        checkGeneSummaryBackground();

    }

    private static void compareActionableGenes() throws IOException {
        List<LevelOfEvidence> levels = new ArrayList<>();
        levels.add(LevelOfEvidence.LEVEL_1);
        levels.add(LevelOfEvidence.LEVEL_2A);
        levels.add(LevelOfEvidence.LEVEL_3A);
        levels.add(LevelOfEvidence.LEVEL_4);
        levels.add(LevelOfEvidence.LEVEL_R1);
        levels.add(LevelOfEvidence.LEVEL_R2);

        System.out.println("Prepare actionable genes for published version and latest version...");
        for (LevelOfEvidence levelOfEvidence : levels) {
            System.out.println("\tOn level " + levelOfEvidence.getLevel());

            //Get published actionable genes
            printEvidences(getFeedUrl(WorkSheetEntryEnum.PUBLISHED_ACTIONABLE_GENES), getPublishedEvidencesByLevel(levelOfEvidence));

            //Get latest actionable genes
            printEvidences(getFeedUrl(WorkSheetEntryEnum.LATEST_ACTIONABLE_GENES), getEvidencesByLevel(levelOfEvidence));
        }
    }

    private static void getEmptyClinicalVariants() {
        URL feedUrl = getFeedUrl(WorkSheetEntryEnum.EMPTY_CLINICAL);
        if (feedUrl != null) {
            for (Gene gene : GeneUtils.getAllGenes()) {
                Set<ClinicalVariant> variants = MainUtils.getClinicalVariants(gene);
                for (ClinicalVariant variant : variants) {
                    if (variant.getOncoTreeType() == null || StringUtils.isNullOrEmpty(variant.getLevel())
                        || (variant.getDrugAbstracts().isEmpty() && variant.getDrugPmids().isEmpty())
                        || variant.getDrug().isEmpty()) {

                        ListEntry row = new ListEntry();
                        setValue(row, "Gene", gene.getHugoSymbol());
                        setValue(row, "Alteration", variant.getVariant().getAlteration());
                        setValue(row, "CancerType", getCancerType(variant.getOncoTreeType()));
                        setValue(row, "Level", variant.getLevel());
                        setValue(row, "Drug", org.apache.commons.lang3.StringUtils.join(variant.getDrug(), ", "));
                        setValue(row, "Pmids", org.apache.commons.lang3.StringUtils.join(variant.getDrugPmids(), ", "));
                        setValue(row, "Abstracts", org.apache.commons.lang3.StringUtils.join(variant.getDrugAbstracts(), ", "));
                        insertRowToEntry(feedUrl, row);
                    }
                }
            }
        }
    }

    private static void getEmptyBiologicalVariants() {
        URL feedUrl = getFeedUrl(WorkSheetEntryEnum.EMPTY_BIOLOGICAL);
        if (feedUrl != null) {
            for (Gene gene : GeneUtils.getAllGenes()) {
                Set<BiologicalVariant> variants = MainUtils.getBiologicalVariants(gene);
                for (BiologicalVariant variant : variants) {
                    if (variant.getOncogenic() == null || variant.getMutationEffect() == null
                        || (variant.getMutationEffectPmids().isEmpty() && variant.getMutationEffectAbstracts().isEmpty())) {

                        ListEntry row = new ListEntry();
                        setValue(row, "Gene", gene.getHugoSymbol());
                        setValue(row, "Alteration", variant.getVariant().getAlteration());
                        setValue(row, "Oncogenicity", variant.getOncogenic());
                        setValue(row, "MutationEffect", variant.getMutationEffect());
                        setValue(row, "PMIDs", org.apache.commons.lang3.StringUtils.join(variant.getMutationEffectPmids(), ", "));
                        Set<ArticleAbstract> articleAbstracts = variant.getMutationEffectAbstracts();
                        Set<String> abstracts = new HashSet<>();
                        for (ArticleAbstract articleAbstract : articleAbstracts) {
                            abstracts.add(articleAbstract.getAbstractContent());
                        }
                        setValue(row, "Abstracts", org.apache.commons.lang3.StringUtils.join(abstracts, ", "));
                        insertRowToEntry(feedUrl, row);
                    }
                }
            }
        }
    }

    private static Set<Evidence> getEvidencesByLevel(LevelOfEvidence levelOfEvidence) {
        if (levelOfEvidence == null)
            return new HashSet<>();
        return EvidenceUtils.getEvidenceByEvidenceTypesAndLevels(EvidenceTypeUtils.getTreatmentEvidenceTypes(), Collections.singleton(levelOfEvidence));
    }

    private static void printEvidences(URL feedUrl, Set<Evidence> evidences) {
        if (evidences != null && feedUrl != null) {
            for (Evidence evidence : evidences) {
                ListEntry row = new ListEntry();

                setValue(row, "Level", evidence.getLevelOfEvidence().getLevel());
                setValue(row, "Gene", evidence.getGene().getHugoSymbol());

                List<String> alterationNames = getAlterationNameByEvidence(evidence);

                setValue(row, "Variants", MainUtils.listToString(alterationNames, ", "));

                setValue(row, "Disease", getCancerType(evidence.getOncoTreeType()));
                Set<String> drugs = EvidenceUtils.getDrugs(Collections.singleton(evidence));
                List<String> drugList = new ArrayList<>(drugs);
                Collections.sort(drugList);
                setValue(row, "Drugs", org.apache.commons.lang3.StringUtils.join(drugList, ", "));
                Set<String> articles = EvidenceUtils.getPmids(Collections.singleton(evidence));
                List<String> articleList = new ArrayList<>(articles);
                Collections.sort(articleList);
                setValue(row, "PMIDs", org.apache.commons.lang3.StringUtils.join(articleList, ", "));
                setValue(row, "NumberOfPMIDs", Integer.toString(articleList.size()));

                Set<String> abstractContent = getAbstractContentFromEvidence(evidence);
                setValue(row, "Abstracts", org.apache.commons.lang3.StringUtils.join(abstractContent, ", "));
                insertRowToEntry(feedUrl, row);
            }
        }
    }

    private static Set<String> getAbstractContentFromEvidence(Evidence evidence) {
        Set<ArticleAbstract> articleAbstracts = EvidenceUtils.getAbstracts(Collections.singleton(evidence));
        Set<String> abstractContent = new HashSet<>();
        for (ArticleAbstract aa : articleAbstracts) {
            abstractContent.add(aa.getAbstractContent());
        }
        return abstractContent;
    }

    private static void printTumorTypeSummary() {
        URL feedUrl = getFeedUrl(WorkSheetEntryEnum.TUMOR_SUMMARIES);
        if (feedUrl != null) {
            for (Gene gene : GeneUtils.getAllGenes()) {
                Set<Evidence> evidences = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY));
                for (Evidence evidence : evidences) {
                    ListEntry row = new ListEntry();
                    setValue(row, "Gene", evidence.getGene().getHugoSymbol());

                    List<String> alterationNames = getAlterationNameByEvidence(evidence);

                    setValue(row, "Variants", MainUtils.listToString(alterationNames, ", "));

                    setValue(row, "CancerType", getCancerType(evidence.getOncoTreeType()));
                    setValue(row, "Summary", evidence.getDescription());
                    insertRowToEntry(feedUrl, row);
                }
            }
        }
    }

    private static void checkGeneSummaryBackground() {
        URL feedUrl = getFeedUrl(WorkSheetEntryEnum.GENE_SUMMARY_BACKGROUND);
        if (feedUrl != null) {
            for (Gene gene : GeneUtils.getAllGenes()) {
                ListEntry row = new ListEntry();
                setValue(row, "Gene", gene.getHugoSymbol());

                boolean hasIssue = false;

                // Summary
                Set<Evidence> evidences = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.GENE_SUMMARY));
                String issue = getIssue(evidences);
                if (issue != null) {
                    hasIssue = true;
                    setValue(row, "Summary", issue);
                }

                // Background
                evidences = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.GENE_BACKGROUND));
                issue = getIssue(evidences);
                if (issue != null) {
                    hasIssue = true;
                    setValue(row, "Background", issue);
                }

                if (hasIssue && !gene.getEntrezGeneId().equals(-2)) {
                    insertRowToEntry(feedUrl, row);
                } else if (!hasIssue && gene.getEntrezGeneId().equals(-2)) {
                    setValue(row, "Summary", "Has summary, but it should not.");
                    setValue(row, "Background", "Has background, but it should not.");
                    insertRowToEntry(feedUrl, row);
                }
            }
        }
    }

    private static String getIssue(Set<Evidence> evidences) {
        String issue = null;
        if (evidences.size() == 0) {
            issue = "No record";
        }
        if (evidences.size() > 2) {
            issue = "Multiple items detected";
        }
        for (Evidence evidence : evidences) {
            if (StringUtils.isNullOrEmpty(evidence.getDescription())) {
                issue = "No info";
            }
        }
        return issue;
    }

    private static List<String> getAlterationNameByEvidence(Evidence evidence) {
        List<String> alterationNames = new ArrayList<>();
        if (evidence != null) {
            for (Alteration alteration : evidence.getAlterations()) {
                if (StringUtils.isNullOrEmpty(alteration.getName())) {
                    alterationNames.add(alteration.getAlteration());
                } else {
                    alterationNames.add(alteration.getName());
                }
            }
        }
        Collections.sort(alterationNames);
        return alterationNames;
    }

    private static Set<Evidence> getPublishedEvidencesByLevel(LevelOfEvidence levelOfEvidence) throws IOException {
        String json = FileUtils.readRemote("https://oncokb.org/legacy-api/evidence.json?levels=" + levelOfEvidence.name());

        ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<List<Evidence>> list = mapper.readValue(json,
            new TypeReference<ArrayList<ArrayList<Evidence>>>() {
            });
        return new HashSet<>(list.get(0));
    }

    private static void setValue(ListEntry row, String element, String value) {
        if (value == null)
            value = "";
        row.getCustomElements().setValueLocal(element, value);
    }

    private static String getCancerType(TumorType tumorType) {
        String cancerTypeName = "";
        if (tumorType != null) {
            if (StringUtils.isNullOrEmpty(tumorType.getName())) {
                if (tumorType.getMainType() != null) {
                    cancerTypeName = tumorType.getMainType().getName();
                }
            } else {
                cancerTypeName = tumorType.getName();
            }
        }
        return cancerTypeName;
    }

    private static URL getFeedUrl(WorkSheetEntryEnum entryEnum) {
        WorksheetEntry entry = worksheets.get(entryEnum.index());
        return entry.getListFeedUrl();
    }

    private static void insertRowToEntry(URL url, ListEntry row) {
        if (url != null) {
            try {
                spreadsheetService.insert(url, row);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }
}
