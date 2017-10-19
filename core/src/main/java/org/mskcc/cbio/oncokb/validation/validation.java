/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.validation;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.mskcc.cbio.oncokb.util.*;
import org.mskcc.oncotree.model.TumorType;

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

    public static void main(String[] args) throws IOException, GeneralSecurityException, URISyntaxException, ServiceException {
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
        Drive driveService = GoogleAuth.getDriveService();
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

        SpreadsheetService service = GoogleAuth.getSpreadSheetService();
        SpreadsheetEntry spreadSheetEntry = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);

        WorksheetFeed worksheetFeed = service.getFeed(
            spreadSheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
        List<WorksheetEntry> worksheets = worksheetFeed.getEntries();

        WorksheetEntry worksheet3 = worksheets.get(1),
            worksheet5 = worksheets.get(2);

        // Fetch the list feed of the worksheets.
        URL listFeedUrl3 = worksheet3.getListFeedUrl(),
            listFeedUrl5 = worksheet5.getListFeedUrl();


        Map<Gene, Set<Evidence>> allGeneBasedEvidences = EvidenceUtils.getAllGeneBasedEvidences();
        Set<Gene> genes = GeneUtils.getAllGenes();
        Integer count = 0;
        List<String> specialAlterations = AlterationUtils.getGeneralAlterations();

        List<LevelOfEvidence> levels = new ArrayList<>();
        levels.add(LevelOfEvidence.LEVEL_1);
        levels.add(LevelOfEvidence.LEVEL_2A);
        levels.add(LevelOfEvidence.LEVEL_3A);
        levels.add(LevelOfEvidence.LEVEL_4);

        System.out.println("Prepare actionable genes for published version and latest version...");
        for (LevelOfEvidence levelOfEvidence : levels) {
            System.out.println("\tOn level " + levelOfEvidence.getLevel());

            //Get published actionable genes
            printEvidences(getPublishedEvidencesByLevel(levelOfEvidence), service, worksheets.get(6));

            //Get latest actionable genes
            printEvidences(getEvidencesByLevel(levelOfEvidence), service, worksheets.get(5));
        }
        System.out.println("Done updating actionable genes.\n");

        System.out.println("Now... validating data based on genes...");
        for (Gene gene : genes) {
            //Check for empty clinical data
            getEmptyClinicalVariants(gene, service, worksheets.get(3));

            //Check for empty biological data
            getEmptyBiologicalVariants(gene, service, worksheets.get(4));

            // Get Tumor Type Summary for this gene
            printTumorTypeSummary(EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY)), service, worksheets.get(7));

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
                    relevantAlterationsMapping.put(alterationItem, new ArrayList<Alteration>(AlterationUtils.getRelevantAlterations(alterationItem)));
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
                    if (referencesMapping.containsKey(alterationItem)) {
                        Set<String> oldPMIDs = referencesMapping.get(alterationItem);
                        Set<String> newPMIDs = EvidenceUtils.getPmids(new HashSet<Evidence>(Arrays.asList(evidenceItem)));
                        newPMIDs.addAll(oldPMIDs);
                        referencesMapping.put(alterationItem, newPMIDs);
                    } else {
                        referencesMapping.put(alterationItem, EvidenceUtils.getPmids(new HashSet<Evidence>(Arrays.asList(evidenceItem))));
                    }
                    if (!altsWithDescriptions.contains(alterationItem)) {
                        if (evidenceItem.getDescription() != null && !evidenceItem.getDescription().isEmpty()) {
                            altsWithDescriptions.add(alterationItem);
                        }
                    }
                }
            }
            for (Alteration alt : allAlts) {
                ArrayList<Alteration> relevantAlts = relevantAlterationsMapping.get(alt);
//                for (Alteration relevantAlt : relevantAlts) {
//                    if (oncogenicityMapping.containsKey(alt)
//                        && oncogenicityMapping.get(alt) != null
//                        && oncogenicityMapping.get(relevantAlt) != null
//                        && oncogenicityMapping.containsKey(relevantAlt)
//                        && !oncogenicityMapping.get(alt).equals(oncogenicityMapping.get(relevantAlt))) {
//                        ListEntry row = new ListEntry();
//                        setValue(row, "Gene", alt.getGene().getHugoSymbol());
//                        setValue(row, "Alteration", alt.getAlteration());
//                        setValue(row, "Oncogenicty", Oncogenicity.getByEffect(oncogenicityMapping.get(alt)).getOncogenic());
//                        setValue(row, "RelevantAlteration", relevantAlt.getAlteration());
//                        setValue(row, "RelevantAlterationOncogenicty", Oncogenicity.getByEffect(oncogenicityMapping.get(relevantAlt)).getOncogenic());
//                        service.insert(listFeedUrl1, row);
//                        break;
//                    }
//                }
//                for (Alteration relevantAlt : relevantAlts) {
//                    if (mutationEffectMapping.containsKey(alt) && mutationEffectMapping.containsKey(relevantAlt) &&
//                        mutationEffectMapping.get(alt) != null && mutationEffectMapping.get(relevantAlt) != null &&
//                        !mutationEffectMapping.get(alt).equals(mutationEffectMapping.get(relevantAlt))) {
//                        ListEntry row = new ListEntry();
//                        setValue(row, "Gene", alt.getGene().getHugoSymbol());
//                        setValue(row, "Alteration", alt.getAlteration());
//                        setValue(row, "MutationEffect", mutationEffectMapping.get(alt));
//                        setValue(row, "RelevantAlteration", relevantAlt.getAlteration());
//                        setValue(row, "RelevantAlterationMutationEffect", mutationEffectMapping.get(relevantAlt));
//                        service.insert(listFeedUrl2, row);
//                        break;
//                    }
//                }
//                if (oncogenicityMapping.containsKey(alt)
//                    && oncogenicityMapping.get(alt) != null
//                    && oncogenicityMapping.get(alt).equals(Oncogenicity.INCONCLUSIVE.getOncogenic())
//                    && mutationEffectMapping.containsKey(alt)
//                    && mutationEffectMapping.get(alt) != null
//                    && mutationEffectMapping.get(alt).equals(MutationEffect.INCONCLUSIVE.getMutationEffect())) {
//                    Integer relevantsSize = relevantAlts.size();
//                    Integer relevantCount = 0;
//                    for (Alteration relevantAlt : relevantAlts) {
//                        relevantCount++;
//                        if (relevantCount == relevantsSize - 1 && oncogenicityMapping.containsKey(alt) && oncogenicityMapping.get(relevantAlt) != null && oncogenicityMapping.get(relevantAlt).equals(Oncogenicity.INCONCLUSIVE.getOncogenic())
//                            && mutationEffectMapping.containsKey(alt) && mutationEffectMapping.get(relevantAlt).equals(MutationEffect.INCONCLUSIVE.getMutationEffect())) {
//                            ListEntry row = new ListEntry();
//                            setValue(row, "Gene", relevantAlt.getGene().getHugoSymbol());
//                            setValue(row, "Alteration", relevantAlt.getAlteration());
//                            service.insert(listFeedUrl3, row);
//
//                        }
//                    }
//                }
//                if (!oncogenicityMapping.containsKey(alt) && !mutationEffectMapping.containsKey(alt) && !VUSAlterations.contains(alt) && !specialAlterations.contains(alt.getAlteration()) && !altsWithDescriptions.contains(alt)) {
//                    ListEntry row = new ListEntry();
//                    setValue(row, "Gene", alt.getGene().getHugoSymbol());
//                    setValue(row, "Alteration", alt.getAlteration());
//                    service.insert(listFeedUrl4, row);
//
//                }
//                if (oncogenicityMapping.containsKey(alt) && mutationEffectMapping.containsKey(alt) && referencesMapping.get(alt).size() == 0) {
//                    ListEntry row = new ListEntry();
//                    setValue(row, "Gene", alt.getGene().getHugoSymbol());
//                    setValue(row, "Alteration", alt.getAlteration());
//                    service.insert(listFeedUrl5, row);
//
//                }
//                if (multipleMutationEffects.containsKey(alt) && multipleMutationEffects.get(alt).size() > 1) {
//                    ListEntry row = new ListEntry();
//                    setValue(row, "Gene", alt.getGene().getHugoSymbol());
//                    setValue(row, "Alteration", alt.getAlteration());
//                    service.insert(listFeedUrl6, row);
//
//                }

            }
            count++;
            System.out.println("\tProcessing " + gene.getHugoSymbol() + "  " + 100 * count / genes.size() + "% finished");
        }

        System.out.println("");
    }

    private static void getEmptyClinicalVariants(Gene gene, SpreadsheetService service, WorksheetEntry entry) throws IOException, ServiceException {
        if (gene != null && service != null && entry != null) {
            URL feedUrl = entry.getListFeedUrl();
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
                    service.insert(feedUrl, row);
                }
            }
        }
    }

    private static void getEmptyBiologicalVariants(Gene gene, SpreadsheetService service, WorksheetEntry entry) throws IOException, ServiceException {
        if (gene != null && service != null && entry != null) {
            URL feedUrl = entry.getListFeedUrl();
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
                    service.insert(feedUrl, row);
                }
            }
        }
    }

    private static Set<Evidence> getEvidencesByLevel(LevelOfEvidence levelOfEvidence) {
        if (levelOfEvidence == null)
            return new HashSet<>();
        return EvidenceUtils.getEvidenceByEvidenceTypesAndLevels(MainUtils.getTreatmentEvidenceTypes(), Collections.singleton(levelOfEvidence));
    }

    private static void printEvidences(Set<Evidence> evidences, SpreadsheetService service, WorksheetEntry entry) throws IOException, ServiceException {
        URL feedUrl = entry.getListFeedUrl();
        if (evidences != null && service != null && entry != null) {
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

                Set<ArticleAbstract> articleAbstracts = EvidenceUtils.getAbstracts(Collections.singleton(evidence));
                Set<String> abstractContent = new HashSet<>();
                for (ArticleAbstract aa : articleAbstracts) {
                    abstractContent.add(aa.getAbstractContent());
                }
                setValue(row, "Abstracts", org.apache.commons.lang3.StringUtils.join(abstractContent, ", "));

                service.insert(feedUrl, row);
            }
        }
    }

    private static void printTumorTypeSummary(Set<Evidence> evidences, SpreadsheetService service, WorksheetEntry entry) throws IOException, ServiceException {
        URL feedUrl = entry.getListFeedUrl();
        if (evidences != null && service != null && entry != null) {
            for (Evidence evidence : evidences) {
                ListEntry row = new ListEntry();
                setValue(row, "Gene", evidence.getGene().getHugoSymbol());

                List<String> alterationNames = getAlterationNameByEvidence(evidence);

                setValue(row, "Variants", MainUtils.listToString(alterationNames, ", "));

                setValue(row, "CancerType", getCancerType(evidence.getOncoTreeType()));
                setValue(row, "Summary", evidence.getDescription());
                service.insert(feedUrl, row);
            }
        }
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
        String json = FileUtils.readRemote("http://oncokb.org/legacy-api/evidence.json?levels=" + levelOfEvidence.name());

        ObjectMapper mapper = new ObjectMapper();
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
}
