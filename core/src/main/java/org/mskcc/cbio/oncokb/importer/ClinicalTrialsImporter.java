/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.importer;

import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.ClinicalTrialBo;
import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jgao
 */
public class ClinicalTrialsImporter {

    private static Map<String, ClinicalTrial> allTrials = null;

    private static void setAllTrials() {
        if (allTrials == null) {
            ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();
            allTrials = new HashMap<String, ClinicalTrial>();
            for (ClinicalTrial ct : clinicalTrialBo.findAll()) {
                allTrials.put(ct.getNctId(), ct);
            }
        }
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

        Set<String> nctIds = getListOfCancerTrialsFromClinicalTrialsGov();//getListOfCancerTrialsFromCancerGov();
        List<ClinicalTrial> trials = importTrials(nctIds);
    }

    public static List<ClinicalTrial> importTrials(Collection<String> nctIds) throws ParserConfigurationException {
//        setAllTrials();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        List<ClinicalTrial> trials = new ArrayList<ClinicalTrial>();
        int n = nctIds.size();
        int i = 1;
        System.out.println("Found " + n + " trials");
        for (String nctId : nctIds) {
            System.out.print("Process... " + (i++) + "/" + n + " " + nctId);
            ClinicalTrial trial = null;
            try {
                trial = parseClinicalTrialsGov(nctId, db);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (trial != null) {
                trials.add(trial);
                ApplicationContextSingleton.getClinicalTrialBo().saveOrUpdate(trial);
            }
            System.out.println();
        }

        return trials;
    }

    private static ClinicalTrial parseClinicalTrialsGov(String nctId, DocumentBuilder db) throws SAXException, IOException {
        ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();

        String strUrl = "https://clinicaltrials.gov/show/" + nctId + "?displayxml=true";

        Document doc = db.parse(strUrl);
        Element docEle = doc.getDocumentElement();

        String lastChangedDate = getText(docEle, "lastchanged_date");
        ClinicalTrial trial = clinicalTrialBo.findClinicalTrialByNctId(nctId);
        if (trial == null) {
            System.out.print(" new");

            trial = new ClinicalTrial();
            trial.setNctId(nctId);
        } else if (!lastChangedDate.equalsIgnoreCase(trial.getLastChangedDate())) {
            // updated
            System.out.print(" updated");
            trial.setLastChangedDate(lastChangedDate);
            clinicalTrialBo.saveOrUpdate(trial);
        } else {
            // if no update
            return trial;
        }

        String briefTitle = getText(docEle, "brief_title").trim();
//        String officialTitle = getText(docEle, "official_title").trim();
        String briefSummary = getText(docEle, "brief_summary/textblock").trim();
//        String detailedDesc = getText(docEle, "detailed_description/textblock").trim();
        String status = getText(docEle, "overall_status").trim();
        String phase = getText(docEle, "phase").trim();
        String condition = getText(docEle, "condition").trim();
        String eligibility = getText(docEle, "eligibility/criteria/textblock").trim();
        eligibility = replaceUTFcode(eligibility);

        Set<String> countries = new HashSet<String>(getTexts(docEle, "location_countries/country"));

        trial.setLastChangedDate(lastChangedDate);
        trial.setTitle(briefTitle);
        trial.setPhase(phase);
        trial.setPurpose(briefSummary);
        trial.setRecruitingStatus(status);
        trial.setDiseaseCondition(condition);
        trial.setEligibilityCriteria(eligibility);
        trial.setCountries(countries);
        trial.setDrugs(parseDrugs(docEle));
//        trial.setAlterations(parseAlterations(docEle));

        clinicalTrialBo.saveOrUpdate(trial);

        return trial;
    }

    private static Set<Drug> parseDrugs(Element docEle) {
        Set<Drug> drugs = new HashSet<Drug>();

        //from intervetion
        List<Element> elements = getElements(docEle, "intervention");
        for (Element el : elements) {
            String type = getText(el, "intervention_type");
            if (!"Drug".equals(type)) {
                continue;
            }

            String name = getText(el, "intervention_name");
            if (name == null) {
                continue;
            }

            List<String> otherNames = getTexts(el, "other_name");

            Map<String, Set<String>> mapNameOtherNames = splitDrugNames(name, otherNames);
            for (Map.Entry<String, Set<String>> entry : mapNameOtherNames.entrySet()) {
                drugs.add(getDrug(entry.getKey(), entry.getValue()));
            }
        }

        // from mesh_term
        List<String> drugNames = getTexts(docEle, "intervention_browse/mesh_term");

        Set<String> synonyms = Collections.emptySet();
        for (String drugName : drugNames) {
            drugs.add(getDrug(drugName, synonyms));
        }

        return drugs;
    }

    private static Drug getDrug(String drugName, Set<String> otherNames) {
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        Drug drug = drugBo.guessUnambiguousDrug(drugName);
        if (drug != null) {
            return drug;
        }

        for (String otherName : otherNames) {
            drug = drugBo.guessUnambiguousDrug(otherName);
            if (drug != null) {
                return drug;
            }
        }

        drug = new Drug();
        drug.setDrugName(drugName);
//        drug.setSynonyms(otherNames);
        drugBo.save(drug);
        return drug;
    }

    private static Map<String, Set<String>> splitDrugNames(String drugName, Collection<String> otherNames) {
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        String[] names = drugName.split(" \\+ ");
        for (String name : names) {
            map.put(name, new HashSet<String>());
        }

        if (names.length == 1) {
            map.get(names[0]).addAll(otherNames);
            return map;
        }

        for (String otherName : otherNames) {
            String[] onames = otherName.split(" \\+ ");
            if (onames.length != names.length) {
                continue;
            }

            for (int i = 0; i < onames.length; i++) {
                map.get(names[i]).add(onames[i]);
            }
        }

        return map;
    }

    private static List<Alteration> allAlterations = null;

    private static Set<Alteration> parseAlterations(Element docEle) {
        if (allAlterations == null) {
            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
            allAlterations = alterationBo.findAll();
        }

        Set<Alteration> ret = new HashSet<Alteration>();
        List<String> keywords = getTexts(docEle, "keyword");
        for (Alteration alteration : allAlterations) {
            for (String keyword : keywords) {
                if (keyword.toUpperCase().contains(alteration.getAlteration().toUpperCase())) {
                    ret.add(alteration);
                }
            }
        }

        return ret;
    }

//    private static boolean isLocationUsa(Element docEle) {
//        List<String> locs = getTexts(docEle, "location_countries/country");
//        for (String loc : locs) {
//            if (loc.equalsIgnoreCase("United States")) {
//                return true;
//            }
//        }
//        return false;
//    }

    private static Element getElement(Element el, String path) {
        List<Element> e = getElements(el, path);
        return e.isEmpty() ? null : e.get(0);
    }

    private static List<Element> getElements(Element el, String path) {
        LinkedList<Element> els = new LinkedList<Element>();
        els.add(el);
        String[] tags = path.split("/");
        for (String tag : tags) {
            int n = els.size();
            for (int i = 0; i < n; i++) {
                Element e = els.poll();
                NodeList nl = e.getElementsByTagName(tag);
                for (int j = 0; j < nl.getLength(); j++) {
                    els.add((Element) nl.item(j));
                }
            }
        }
        return els;
    }

    private static String getText(Element el, String path) {
        List<String> text = getTexts(el, path);
        return text.isEmpty() ? null : text.get(0);
    }

    private static List<String> getTexts(Element el, String path) {
        List<Element> els = getElements(el, path);

        List<String> ret = new ArrayList<String>();
        for (Element e : els) {
            ret.add(e.getChildNodes().item(0).getNodeValue());
        }

        return ret;
    }

    private static String replaceUTFcode(String str) {
        return str.replaceAll("\\u2265", ">=").replaceAll("\\u2264", ">=");
    }

    private static Set<String> getListOfCancerTrialsFromClinicalTrialsGov() throws SAXException, ParserConfigurationException, IOException {
        List<String> urls = getUrlsConditions();
        Set<String> nctIds = new HashSet<String>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();

        for (String url : urls) {
            int pg = 1;
            while (true) {
                String urlCts = "https://clinicaltrials.gov" + url + "&displayxml=true&pg=" + pg;
                System.out.println(urlCts);
                Document doc = null;
                for (int iTry = 0; iTry < 10 && doc == null; iTry++) {
                    try {
                        doc = db.parse(urlCts);
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
                if (doc == null) {
                    doc = db.parse(urlCts);
                }
                Element docEle = doc.getDocumentElement();
                List<Element> els = getElements(docEle, "clinical_study");
                if (els.isEmpty()) {
                    break;
                }
                for (Element el : els) {
                    String nctId = getText(el, "nct_id");
                    if (nctId == null) {
                        continue;
                    }

                    String lastChangedDate = getText(el, "last_changed");
                    ClinicalTrial trial = clinicalTrialBo.findClinicalTrialByNctId(nctId);
                    if (trial == null) {
//                        Element e = getElement(el, "status");
//                        if (e!=null && "Y".equals(e.getAttribute("open"))) {
                        nctIds.add(nctId);
//                        }
                    } else if (!lastChangedDate.equalsIgnoreCase(trial.getLastChangedDate())) {
                        nctIds.add(nctId);
                    }

                }
                pg++;
            }
        }

        return nctIds;
    }

    private static List<String> getUrlsConditions() throws IOException {
        List<String> urlConditions = new ArrayList<String>();

        String urlConditionList = "https://clinicaltrials.gov/ct2/search/browse?brwse=cond_cat_BC04&brwse-force=true";
        String[] lines = FileUtils.readRemote(urlConditionList).split("\n");
        Pattern p = Pattern.compile("/ct2/results\\?[^\"']+");
        for (String line : lines) {
            Matcher m = p.matcher(line);
            if (m.find()) {
                urlConditions.add(m.group());
            }
        }

        return urlConditions;
    }

    private static Set<String> getListOfCancerTrialsFromCancerGov() throws ParserConfigurationException, SAXException, IOException {
        String nciClinicalTrialFolder = "/Users/jgao/projects/oncokb-data/CTGovProtocol";
        List<String> files = FileUtils.getFilesInFolder(nciClinicalTrialFolder, "xml");
        int n = files.size();
        Set<String> nctIds = new HashSet<String>();
        for (int i = 0; i < n; i++) {
            System.out.println("Process... " + (i++) + "/" + n);
            String file = files.get(i);
            String nctId = parseNci(file);
            if (nctId != null) {
                nctIds.add(nctId);
            }
        }
        return nctIds;
    }

    private static String parseNci(String file) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(file);
        Element docEle = doc.getDocumentElement();
        String cdrId = docEle.getAttribute("id");

        String nctId = getText(docEle, "IDInfo/NCTID");

        return nctId;
    }

}
