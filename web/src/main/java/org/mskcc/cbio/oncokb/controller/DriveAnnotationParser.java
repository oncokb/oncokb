/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.*;
import org.mskcc.cbio.oncokb.importer.ClinicalTrialsImporter;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author jgao
 * 
 * 1. Additional Inforamtion is not parsed.
 */
@Controller
public class DriveAnnotationParser {
    private static final String DO_NOT_IMPORT = "DO NOT IMPORT";

    @RequestMapping(value = "/legacy-api/driveAnnotation", method = POST)
    public
    @ResponseBody
    void getEvidence(
        @RequestParam(value = "gene", required = true) String gene,
        @RequestParam(value = "vus", required = false) String vus
    ) throws IOException {

        if (gene == null) {
            System.out.println("#No gene info available.");
        } else {
            JSONObject jsonObj = new JSONObject(gene);
            JSONArray jsonArray = null;
            if (vus != null) {
                jsonArray = new JSONArray(vus);
            }
            parseGene(jsonObj, jsonArray);
        }
    }

    private static void parseVUS(Gene gene, JSONArray vus) {
        System.out.println("##    Variants of unknown significance");
        if (gene != null && vus != null) {
            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            AlterationType type = AlterationType.MUTATION; //TODO: cna and fusion

            for (int i = 0; i < vus.length(); i++) {
                JSONObject variant = vus.getJSONObject(i);
                String mutationStr = variant.has("name") ? variant.getString("name") : null;
                String lastEdit = variant.has("lastEdit") ? variant.getString("lastEdit") : null;
                JSONArray nameComments = variant.has("nameComments") ? variant.getJSONArray("nameComments") : null;
                if (mutationStr != null) {
                    Map<String, String> mutations = parseMutationString(mutationStr);
                    Set<Alteration> alterations = new HashSet<Alteration>();

                    for (Map.Entry<String, String> mutation : mutations.entrySet()) {
                        String proteinChange = mutation.getKey();
                        String displayName = mutation.getValue();
                        Alteration alteration = alterationBo.findAlteration(gene, type, proteinChange);
                        if (alteration == null) {
                            alteration = new Alteration();
                            alteration.setGene(gene);
                            alteration.setAlterationType(type);
                            alteration.setAlteration(proteinChange);
                            alteration.setName(displayName);
                            AlterationUtils.annotateAlteration(alteration, proteinChange);
                            alterationBo.save(alteration);
                        }
                        alterations.add(alteration);
                    }

                    Evidence evidence = new Evidence();
                    evidence.setEvidenceType(EvidenceType.VUS);
                    evidence.setGene(gene);
                    evidence.setAlterations(alterations);
                    if (lastEdit != null) {
                        Date date = new Date(Long.valueOf(lastEdit).longValue());
                        evidence.setLastEdit(date);
                    }
                    if (nameComments != null) {
                        for (int j = 0; j < nameComments.length(); j++) {
                            JSONObject item = nameComments.getJSONObject(j);
                            if (item != null && item.has("content") && item.getString("content") != null) {
                                setDocuments(item.getString("content"), evidence);
                            }
                        }
                    }
                    evidenceBo.save(evidence);
                }
            }
        } else {
            if (gene == null) {
                System.out.println("###      No gene available.");
            }
            if (vus == null) {
                System.out.println("###      No VUS available.");
            }
        }
    }

    private static void parseGene(JSONObject geneInfo, JSONArray vus) throws IOException {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        if (geneInfo.has("name") && !geneInfo.getString("name").trim().isEmpty()) {
            String hugo = geneInfo.has("name") ? geneInfo.getString("name").trim() : null;

            if (hugo != null) {
                Gene gene = geneBo.findGeneByHugoSymbol(hugo);

                if (gene == null) {
                    System.out.println("Don't save gene not in 418 gene list.");
                    return;
//                    System.out.println("Could not find gene " + hugo + ". Loading from MyGene.Info...");
//                    gene = GeneAnnotatorMyGeneInfo2.readByHugoSymbol(hugo);
//                    if (gene == null) {
////                    throw new RuntimeException("Could not find gene "+hugo+" either.");
//                        System.out.println("!!!!!!!!!Could not find gene " + hugo + " either.");
//                    } else {
//                        geneBo.save(gene);
//                    }
                }

                if (gene != null) {
                    // Get gene type info
                    JSONObject geneType = geneInfo.has("type") ? geneInfo.getJSONObject("type") : null;
                    String oncogene = geneType.has("OCG") ? geneType.getString("OCG").trim() : null;
                    String tsg = geneType.has("TSG") ? geneType.getString("TSG").trim() : null;

                    // Get isoform info
                    JSONArray transcripts = geneInfo.has("transcripts") ? geneInfo.getJSONArray("transcripts") : null;

                    if (oncogene != null) {
                        if (oncogene.equals("Oncogene")) {
                            gene.setOncogene(true);
                        } else {
                            gene.setOncogene(false);
                        }
                    }
                    if (tsg != null) {
                        if (tsg.equals("Tumor Suppressor")) {
                            gene.setTSG(true);
                        } else {
                            gene.setTSG(false);
                        }
                    }

                    if (transcripts != null && transcripts.length() > 0) {
                        JSONObject transcript = transcripts.getJSONObject(0);
                        String isoform = transcript.has("isoform_override") ? transcript.getString("isoform_override") : null;
                        String refSeq = transcript.has("dmp_refseq_id") ? transcript.getString("dmp_refseq_id") : null;

                        if (isoform != null) {
                            gene.setCuratedIsoform(isoform);
                        }
                        if (refSeq != null) {
                            gene.setCuratedRefSeq(refSeq);
                        }
                    }
                    geneBo.saveOrUpdate(gene);

                    EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
                    AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
                    List<Evidence> evidences = evidenceBo.findEvidencesByGene(Collections.singleton(gene));
                    List<Alteration> alterations = alterationBo.findAlterationsByGene(Collections.singleton(gene));

                    for (Evidence evidence : evidences) {
                        evidenceBo.delete(evidence);
                    }

                    for (Alteration alteration : alterations) {
                        alterationBo.delete(alteration);
                    }

                    // summary
                    parseSummary(gene, geneInfo.has("summary") ? geneInfo.getString("summary").trim() : null);

                    // background
                    parseGeneBackground(gene, geneInfo.has("background") ? geneInfo.getString("background").trim() : null);

                    // mutations
                    parseMutations(gene, geneInfo.has("mutations") ? geneInfo.getJSONArray("mutations") : null);

                    // Variants of unknown significance
                    parseVUS(gene, vus);

//                    CacheUtils.updateGene(gene.getEntrezGeneId());
                } else {
                    System.out.print("No gene name available");
                }
            } else {
                System.out.println("No hugoSymbol available");
            }
        }
    }

    private static void parseSummary(Gene gene, String geneSummary) {
        System.out.println("##  Summary");

        // gene summary
        if (geneSummary != null && !geneSummary.isEmpty()) {
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.GENE_SUMMARY);
            evidence.setGene(gene);
            evidence.setDescription(geneSummary);
            setDocuments(geneSummary, evidence);
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            evidenceBo.save(evidence);
        } else {
            System.out.println("    No info...");
        }
    }

    private static void parseGeneBackground(Gene gene, String bg) {
        System.out.println("##  Background");

        if (bg != null && !bg.isEmpty()) {
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.GENE_BACKGROUND);
            evidence.setGene(gene);
            evidence.setDescription(bg);
            setDocuments(bg, evidence);

            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            evidenceBo.save(evidence);
        } else {
            System.out.println("    No info...");
        }
    }

    private static void parseMutations(Gene gene, JSONArray mutations) {
        System.out.println("##  Mutations");
        if (mutations != null) {
            for (int i = 0; i < mutations.length(); i++) {
                parseMutation(gene, mutations.getJSONObject(i));
            }
        } else {
            System.out.println("    no mutation available.");
        }
    }

    private static void parseMutation(Gene gene, JSONObject mutationObj) {
        String mutationStr = mutationObj.has("name") ? mutationObj.getString("name").trim() : null;

        if (mutationStr != null && !mutationStr.isEmpty() && !mutationStr.contains("?")) {
            System.out.println("##  Mutation: " + mutationStr);

            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
            AlterationType type = AlterationType.MUTATION; //TODO: cna and fusion

            Set<Alteration> alterations = new HashSet<Alteration>();

            String oncogenic = getOncogenicity(mutationObj);
            String doe = null;
            String variantSummary = null;
            String effect = getMutationEffect(mutationObj);

            // If both mutation effect and oncogenicity both unknown, ignore variant.
            if (oncogenic != null && oncogenic.equals("-1")
                && effect != null && effect.toLowerCase().equals("inconclusive")
                && gene.getHugoSymbol().equals("EGFR")) {
                return;
            }

            System.out.println("##    Oncogenicity Summary");
            if (mutationObj.has("shortSummary") && !mutationObj.getString("shortSummary").isEmpty()) {
                variantSummary = mutationObj.getString("shortSummary");
            }

            if (mutationObj.has("summary") && !mutationObj.getString("summary").isEmpty()) {
                doe = mutationObj.getString("summary");
            }

            Map<String, String> mutations = parseMutationString(mutationStr);
            for (Map.Entry<String, String> mutation : mutations.entrySet()) {
                String proteinChange = mutation.getKey();
                String displayName = mutation.getValue();
                Alteration alteration = alterationBo.findAlteration(gene, type, proteinChange);
                if (alteration == null) {
                    alteration = new Alteration();
                    alteration.setGene(gene);
                    alteration.setAlterationType(type);
                    alteration.setAlteration(proteinChange);
                    alteration.setName(displayName);
                    AlterationUtils.annotateAlteration(alteration, proteinChange);
                    alterationBo.save(alteration);
                } else {
                    alterationBo.update(alteration);
                }
                alterations.add(alteration);
                setOncogenic(gene, alteration, oncogenic, doe);
            }

            if (variantSummary != null) {
                Evidence evidence = new Evidence();
                evidence.setGene(gene);
                evidence.setAlterations(alterations);
                evidence.setEvidenceType(EvidenceType.MUTATION_SUMMARY);
                evidence.setDescription(variantSummary);
            }

            // mutation effect
            String effectDesc = mutationObj.has("description") ?
                (mutationObj.getString("description").trim().isEmpty() ? null :
                    mutationObj.getString("description").trim())
                : null;
//            String additionalME = mutationObj.has("short") ?
//                (mutationObj.getString("short").trim().isEmpty() ? null : mutationObj.getString("short").trim())
//                : null;

            if (effect != null || effectDesc != null) {
                // save
                Evidence evidence = new Evidence();
                evidence.setEvidenceType(EvidenceType.MUTATION_EFFECT);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);

                if ((effectDesc != null && !effectDesc.trim().isEmpty())) {
                    evidence.setDescription(effectDesc);
                    setDocuments(effectDesc, evidence);
                }

//                if ((additionalME != null && !additionalME.trim().isEmpty())) {
//                    evidence.setAdditionalInfo(additionalME);
//                }

                evidence.setKnownEffect(effect);

                EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
                evidenceBo.save(evidence);
            }

            // cancers
            if (mutationObj.has("tumors")) {
                JSONArray cancers = mutationObj.getJSONArray("tumors");
                for (int i = 0; i < cancers.length(); i++) {
                    JSONArray subTumorTypes = cancers.getJSONObject(i).getJSONArray("cancerTypes");
                    for (int j = 0; j < subTumorTypes.length(); j++) {
                        parseCancer(gene, alterations, cancers.getJSONObject(i), subTumorTypes.getJSONObject(j).getString("cancerType"), subTumorTypes.getJSONObject(j).getString("oncoTreeCode") != "" ? subTumorTypes.getJSONObject(j).getString("oncoTreeCode") : null);
                    }
                }
            } else {
                System.out.println("    No tumor available.");
            }
        } else {
            System.out.println("##  Mutation does not have name skip...");
        }
    }

    private static String getMutationEffect(JSONObject mutationObj) {
        return mutationObj.has("effect") ?
            (mutationObj.getJSONObject("effect").has("value") ?
                (mutationObj.getJSONObject("effect").getString("value").trim().isEmpty() ? null :
                    mutationObj.getJSONObject("effect").getString("value").trim())
                : null)
            : null;
    }

    private static String getOncogenicity(JSONObject mutationObj) {
        String oncogenic = null;
        if (mutationObj.has("oncogenic") && !mutationObj.getString("oncogenic").isEmpty()) {
            String oncogenicStr = mutationObj.getString("oncogenic").toLowerCase();

            switch (oncogenicStr) {
                case "yes":
                    oncogenic = "1";
                    break;
                case "likely":
                    oncogenic = "2";
                    break;
                case "likely neutral":
                    oncogenic = "0";
                    break;
                case "inconclusive":
                    oncogenic = "-1";
                    break;
                default:
                    break;
            }
        }
        return oncogenic;
    }

    private static void setOncogenic(Gene gene, Alteration alteration, String oncogenic, String doe) {
        if (alteration != null && gene != null && (oncogenic != null || doe != null)) {
            List<Evidence> evidences = new ArrayList<>();
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            evidences = evidenceBo.findEvidencesByAlteration(Collections.singleton(alteration), Collections.singleton(EvidenceType.ONCOGENIC));
            if (evidences.size() == 0) {
                Evidence evidence = new Evidence();
                evidence.setGene(gene);
                evidence.setAlterations(Collections.singleton(alteration));
                evidence.setEvidenceType(EvidenceType.ONCOGENIC);
                evidence.setKnownEffect(oncogenic);
                evidence.setDescription(doe);
                setDocuments(doe, evidence);
                evidenceBo.save(evidence);
            } else if (oncogenic.equals("1") || oncogenic.equals("2")) {
                evidences.get(0).setKnownEffect(oncogenic);
                evidences.get(0).setDescription(doe);
                setDocuments(doe, evidences.get(0));
                evidenceBo.update(evidences.get(0));
            }
        }
    }

    private static Map<String, String> parseMutationString(String mutationStr) {
        Map<String, String> ret = new HashMap<String, String>();

        mutationStr = mutationStr.replaceAll("\\([^\\)]+\\)", ""); // remove comments first

        String[] parts = mutationStr.split(", *");

        Pattern p = Pattern.compile("([A-Z][0-9]+)([^0-9/]+/.+)", Pattern.CASE_INSENSITIVE);
        for (String part : parts) {
            String proteinChange, displayName;
            part = part.trim();
            if (part.contains("[")) {
                int l = part.indexOf("[");
                int r = part.indexOf("]");
                proteinChange = part.substring(0, l).trim();
                displayName = part.substring(l + 1, r).trim();
            } else {
                proteinChange = part;
                displayName = part;
            }

            Matcher m = p.matcher(proteinChange);
            if (m.find()) {
                String ref = m.group(1);
                for (String var : m.group(2).split("/")) {
                    ret.put(ref + var, ref + var);
                }
            } else {
                ret.put(proteinChange, displayName);
            }
        }
        return ret;
    }

    private static void parseCancer(Gene gene, Set<Alteration> alterations, JSONObject cancerObj, String cancerType, String code) {
        if (cancerType == null || cancerType.equals("")) {
            return;
        }

        System.out.println("##    Cancer type: " + cancerType);
        System.out.println("##    Subtype code: " + code);

        OncoTreeType oncoTreeType;

        if (code != null && !code.equals("")) {
            oncoTreeType = TumorTypeUtils.getOncoTreeSubtypeByCode(code);
        } else {
            oncoTreeType = TumorTypeUtils.getOncoTreeCancerType(cancerType);
        }

        if (oncoTreeType == null) {
            System.out.println("##      No mapped OncoTreeType.");
            return;
        }

        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        // cancer type summary
        System.out.println("##      Summary");

        if (cancerObj.has("summary") && !cancerObj.getString("summary").isEmpty()) {
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.TUMOR_TYPE_SUMMARY);
            evidence.setGene(gene);
            evidence.setDescription(cancerObj.getString("summary"));
            evidence.setAlterations(alterations);
            evidence.setCancerType(oncoTreeType.getCancerType());
            evidence.setSubtype(oncoTreeType.getCode());
            setDocuments(cancerObj.getString("summary"), evidence);
            evidenceBo.save(evidence);
        } else {
            System.out.println("    No info...");
        }

        // Prevalence
        if ((cancerObj.has("prevalence") && !cancerObj.getString("prevalence").trim().isEmpty()) || (cancerObj.has("shortPrevalence") && !cancerObj.getString("shortPrevalence").trim().isEmpty())) {
            System.out.println("##      Prevalence: " + alterations.toString());

            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.PREVALENCE);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setCancerType(oncoTreeType.getCancerType());
            evidence.setSubtype(oncoTreeType.getCode());

//            if (cancerObj.has("shortPrevalence") && !cancerObj.getString("shortPrevalence").trim().isEmpty()) {
//                System.out.println("###         Short prevalence: " + alterations.toString());
//                String additionalInfo = cancerObj.getString("shortPrevalence").trim();
//                evidence.setAdditionalInfo(additionalInfo);
//            }

            if (cancerObj.has("prevalence") && !cancerObj.getString("prevalence").trim().isEmpty()) {
                System.out.println("###         Full prevalence: " + alterations.toString());
                String desc = cancerObj.getString("prevalence").trim();
                evidence.setDescription(desc);
                setDocuments(desc, evidence);
            }
            evidenceBo.save(evidence);
        } else {
            System.out.println("##      No Prevalance for " + alterations.toString());
        }

        // Prognostic implications
        if ((cancerObj.has("progImp") && !cancerObj.getString("progImp").trim().isEmpty()) || (cancerObj.has("shortProgImp") && !cancerObj.getString("shortProgImp").trim().isEmpty())) {
            System.out.println("##      Prognostic implications:" + alterations.toString());

            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.PROGNOSTIC_IMPLICATION);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setCancerType(oncoTreeType.getCancerType());
            evidence.setSubtype(oncoTreeType.getCode());

//            if (cancerObj.has("shortProgImp") && !cancerObj.getString("shortProgImp").trim().isEmpty()) {
//                System.out.println("###         Short prognostic implications: " + alterations.toString());
//                String additionalInfo = cancerObj.getString("shortProgImp").trim();
//                evidence.setAdditionalInfo(additionalInfo);
//            }

            if (cancerObj.has("progImp") && !cancerObj.getString("progImp").trim().isEmpty()) {
                System.out.println("###         Full prognostic implications: " + alterations.toString());
                String desc = cancerObj.getString("progImp").trim();
                evidence.setDescription(desc);
                setDocuments(desc, evidence);
            }
            evidenceBo.save(evidence);
        } else {
            System.out.println("##      No Proganostic implications " + alterations.toString());
        }

        JSONArray implications = cancerObj.getJSONArray("TI");

        for (int i = 0; i < implications.length(); i++) {
            JSONObject implication = implications.getJSONObject(i);
            if ((implication.has("description") && !implication.getString("description").trim().isEmpty()) || (implication.has("treatments") && implication.getJSONArray("treatments").length() > 0)) {
                EvidenceType evidenceType = EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY;
                String type = "";
                if (implication.has("status") && implication.has("type")) {
                    if (implication.getString("status").equals("1")) {
                        if (implication.getString("type").equals("1")) {
                            evidenceType = EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY;
                            type = "Sensitive";
                        } else if (implication.getString("type").equals("0")) {
                            evidenceType = EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE;
                            type = "Resistant";
                        }
                    } else if (implication.getString("status").equals("0")) {
                        if (implication.getString("type").equals("1")) {
                            evidenceType = EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY;
                            type = "Sensitive";
                        } else if (implication.getString("type").equals("0")) {
                            evidenceType = EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE;
                            type = "Resistant";
                        }
                    }
                    parseTherapeuticImplcations(gene, alterations, oncoTreeType, implication, evidenceType, type);
                }
            }
        }

        // NCCN
        if (cancerObj.has("nccn") && cancerObj.getJSONObject("nccn").has("disease") && !cancerObj.getJSONObject("nccn").getString("disease").isEmpty()) {
            System.out.println("##      NCCN for " + alterations.toString());
            parseNCCN(gene, alterations, oncoTreeType, cancerObj.getJSONObject("nccn"));
        } else {
            System.out.println("##      No NCCN for " + alterations.toString());
        }

        if (cancerObj.has("trials") && cancerObj.getJSONArray("trials").length() > 0) {
            System.out.println("##      Clincial trials for " + alterations.toString());
            parseClinicalTrials(gene, alterations, oncoTreeType, cancerObj.getJSONArray("trials"));
        } else {
            System.out.println("##      No Clincial trials for " + alterations.toString());
        }
    }

    private static void parseClinicalTrials(Gene gene, Set<Alteration> alterations, OncoTreeType oncoTreeType, JSONArray trialsArray) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        Set<String> nctIds = new HashSet<String>();
        for (int i = 0; i < trialsArray.length(); i++) {
            String nctId = trialsArray.getString(i).trim();
            if (!nctId.isEmpty()) {
                nctIds.add(nctId);
            }
        }
        // Couldnot find out the reason why designed to remove all previous trials. 
//        List<Evidence> evidences = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.CLINICAL_TRIAL), Collections.singleton(oncoTreeType));
//
//        for (Evidence eve : evidences) {
//            evidenceBo.delete(eve);
//        }

        try {
            List<ClinicalTrial> trials = ClinicalTrialsImporter.importTrials(nctIds);
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.CLINICAL_TRIAL);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setCancerType(oncoTreeType.getCancerType());
            evidence.setSubtype(oncoTreeType.getCode());
            evidence.setClinicalTrials(new HashSet<ClinicalTrial>(trials));
            evidenceBo.save(evidence);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseTherapeuticImplcations(Gene gene, Set<Alteration> alterations, OncoTreeType oncoTreeType, JSONObject implicationObj,
                                                    EvidenceType evidenceType, String knownEffectOfEvidence) {
        System.out.println("##      " + evidenceType + " for " + alterations.toString() + " CancerType: " + oncoTreeType.getCancerType() + " Subtype: " + oncoTreeType.getCode());

        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        if ((implicationObj.has("description") && !implicationObj.getString("description").trim().isEmpty()) || (implicationObj.has("short") && !implicationObj.getString("short").trim().isEmpty())) {
            // general description
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(evidenceType);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setCancerType(oncoTreeType.getCancerType());
            evidence.setSubtype(oncoTreeType.getCode());
            evidence.setKnownEffect(knownEffectOfEvidence);

            if (implicationObj.has("description") && !implicationObj.getString("description").trim().isEmpty()) {
                String desc = implicationObj.getString("description");
                evidence.setDescription(desc);
                setDocuments(desc, evidence);
            }
            evidenceBo.save(evidence);
        }

        // specific evidence
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        TreatmentBo treatmentBo = ApplicationContextSingleton.getTreatmentBo();
        JSONArray drugsArray = implicationObj.getJSONArray("treatments");

        for (int i = 0; i < drugsArray.length(); i++) {
            JSONObject drugObj = drugsArray.getJSONObject(i);
            if (!drugObj.has("name") || drugObj.getString("name").trim().isEmpty()) {
                System.out.println("##        drug dpes not have name, skip... ");
                continue;
            }

            String drugNameStr = drugObj.getString("name").trim();
            System.out.println("##        drugs: " + drugNameStr);

            Evidence evidence = new Evidence();
            evidence.setEvidenceType(evidenceType);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setCancerType(oncoTreeType.getCancerType());
            evidence.setSubtype(oncoTreeType.getCode());
            evidence.setKnownEffect(knownEffectOfEvidence);

            // approved indications
            Set<String> appovedIndications = new HashSet<String>();
            if (drugObj.has("indication") && !drugObj.getString("indication").trim().isEmpty()) {
                appovedIndications = new HashSet<String>(Arrays.asList(drugObj.getString("indication").split(";")));
            }

            String[] drugTxts = drugNameStr.replaceAll("(\\([^\\)]*\\))|(\\[[^\\]]*\\])", "").split(",");

            Set<Treatment> treatments = new HashSet<Treatment>();
            for (String drugTxt : drugTxts) {
                String[] drugNames = drugTxt.split(" ?\\+ ?");

                Set<Drug> drugs = new HashSet<Drug>();
                for (String drugName : drugNames) {
                    drugName = drugName.trim();
                    Drug drug = drugBo.guessUnambiguousDrug(drugName);
                    if (drug == null) {
                        drug = new Drug(drugName);
                        drugBo.save(drug);
                    }
                    drugs.add(drug);
                }

                Treatment treatment = new Treatment();
                treatment.setDrugs(drugs);
                treatment.setApprovedIndications(appovedIndications);

                treatmentBo.save(treatment);

                treatments.add(treatment);
            }
            evidence.setTreatments(treatments);

            // highest level of evidence
            if (!drugObj.has("level") || drugObj.getString("level").trim().isEmpty()) {
                System.err.println("Error: no level of evidence");
                // TODO:
                //throw new RuntimeException("no level of evidence");
            } else {
                String level = drugObj.getString("level").trim();
                if (level.equals("2")) {

                    if (evidenceType == EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE
                        || evidenceType == EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY) {
                        level = "2A";
                    } else {
                        level = "2B";
                    }
                }

                LevelOfEvidence levelOfEvidence = LevelOfEvidence.getByLevel(level);
                if (levelOfEvidence == null) {
                    System.err.println("Errow: wrong level of evidence: " + level);
                    // TODO:
                    //throw new RuntimeException("wrong level of evidence: "+level);
                }
                evidence.setLevelOfEvidence(levelOfEvidence);
            }

            // description
//            if (drugObj.has("short") && !drugObj.getString("short").trim().isEmpty()) {
//                String additionalInfo = drugObj.getString("short").trim();
//                evidence.setAdditionalInfo(additionalInfo);
//            }
            if (drugObj.has("description") && !drugObj.getString("description").trim().isEmpty()) {
                String desc = drugObj.getString("description").trim();
                evidence.setDescription(desc);
                setDocuments(desc, evidence);
            }

            evidenceBo.save(evidence);
        }
    }

    private static void parseNCCN(Gene gene, Set<Alteration> alterations, OncoTreeType oncoTreeType, JSONObject nccnObj) {
        // disease
        String disease = null;
        if (nccnObj.has("disease") && !nccnObj.getString("disease").trim().isEmpty()) {
            disease = nccnObj.getString("disease").trim();
        }

        // version
        String version = null;
        if (nccnObj.has("version") && !nccnObj.getString("version").trim().isEmpty()) {
            version = nccnObj.getString("version").trim();
        }

        // pages
        String pages = null;
        if (nccnObj.has("pages") && !nccnObj.getString("pages").trim().isEmpty()) {
            pages = nccnObj.getString("pages").trim();
        }

        // Recommendation category
        String category = null;
        if (nccnObj.has("category") && !nccnObj.getString("category").trim().isEmpty()) {
            category = nccnObj.getString("category").trim();
        }

        // description
        String nccnDescription = null;
        if (nccnObj.has("description") && !nccnObj.getString("description").trim().isEmpty()) {
            nccnDescription = nccnObj.getString("description").trim();
        }

        Evidence evidence = new Evidence();
        evidence.setEvidenceType(EvidenceType.NCCN_GUIDELINES);
        evidence.setAlterations(alterations);
        evidence.setGene(gene);
        evidence.setCancerType(oncoTreeType.getCancerType());
        evidence.setSubtype(oncoTreeType.getCode());
        evidence.setDescription(nccnDescription);

        NccnGuidelineBo nccnGuideLineBo = ApplicationContextSingleton.getNccnGuidelineBo();

        NccnGuideline nccnGuideline = new NccnGuideline();
        nccnGuideline.setDisease(disease);
        nccnGuideline.setVersion(version);
        nccnGuideline.setPages(pages);
        nccnGuideline.setCategory(category);
        nccnGuideline.setDescription(nccnDescription);

//        if (nccnObj.has("short") && !nccnObj.getString("short").trim().isEmpty()) {
//            String additionalInfo = nccnObj.getString("short").trim();
//            evidence.setAdditionalInfo(additionalInfo);
//            nccnGuideline.setAdditionalInfo(additionalInfo);
//        }

        nccnGuideLineBo.save(nccnGuideline);

        evidence.setNccnGuidelines(Collections.singleton(nccnGuideline));

        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        evidenceBo.save(evidence);
    }

    private static void setDocuments(String str, Evidence evidence) {
        if (str == null) return;
        Set<Article> docs = new HashSet<Article>();
        Set<ClinicalTrial> clinicalTrials = new HashSet<ClinicalTrial>();
        ArticleBo articleBo = ApplicationContextSingleton.getArticleBo();
        ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();
        Pattern pmidPattern = Pattern.compile("PMIDs?:\\s*([\\d,\\s*]+)", Pattern.CASE_INSENSITIVE);
        Pattern abstractPattern = Pattern.compile("\\(\\s*Abstract\\s*:([^\\)]*);\\s*\\)", Pattern.CASE_INSENSITIVE);
        Pattern abItemPattern = Pattern.compile("(.*?)\\.\\s*(http.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = pmidPattern.matcher(str);
        int start = 0;
        while (m.find(start)) {
            String pmids = m.group(1).trim();
            for (String pmid : pmids.split(", *(PMID:)? *")) {
                if (pmid.startsWith("NCT")) {
                    // support NCT numbers
                    Set<String> nctIds = new HashSet<String>(Arrays.asList(pmid.split(", *")));
                    try {
                        List<ClinicalTrial> trials = ClinicalTrialsImporter.importTrials(nctIds);
                        for (ClinicalTrial trial : trials) {
                            clinicalTrialBo.saveOrUpdate(trial);
                            clinicalTrials.add(trial);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                Article doc = articleBo.findArticleByPmid(pmid);
                if (doc == null) {
                    doc = NcbiEUtils.readPubmedArticle(pmid);
                    articleBo.save(doc);
                }
                docs.add(doc);
            }
            start = m.end();
        }

        Matcher abstractMatch = abstractPattern.matcher(str);
        start = 0;
        String abstracts = "", abContent = "", abLink = "";
        while (abstractMatch.find(start)) {
            abstracts = abstractMatch.group(1).trim();
            for (String abs : abstracts.split(";")) {
                Matcher abItems = abItemPattern.matcher(abs);
                if (abItems.find()) {
                    abContent = abItems.group(1).trim();
                    abLink = abItems.group(2).trim();
                }
                if (!abContent.isEmpty()) {
                    Article doc = new Article();
                    doc.setAbstractContent(abContent);
                    doc.setLink(abLink);
                    if (articleBo.findArticleByAbstract(abContent) == null) {
                        articleBo.save(doc);
                        docs.add(doc);
                    }

                }
                abContent = "";
                abLink = "";

            }
            start = abstractMatch.end();

        }

        evidence.addArticles(docs);
        evidence.addClinicalTrials(clinicalTrials);
    }
}
