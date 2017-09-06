/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.*;
import org.mskcc.cbio.oncokb.importer.ClinicalTrialsImporter;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.mskcc.oncotree.model.TumorType;
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
 */
@Controller
public class DriveAnnotationParser {
    @RequestMapping(value = "/legacy-api/driveAnnotation", method = POST)
    public
    @ResponseBody
    void getEvidence(
        @RequestParam(value = "gene") String gene,
        @RequestParam(value = "vus", required = false) String vus
    ) throws IOException, JSONException {

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

    public static void parseVUS(Gene gene, JSONArray vus, Integer nestLevel) throws JSONException {
        System.out.println(spaceStrByNestLevel(nestLevel) + "Variants of unknown significance");
        if (vus != null) {
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
                    Set<Alteration> alterations = new HashSet<>();
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
                    if (evidence.getLastEdit() == null) {
                        System.out.println(spaceStrByNestLevel(nestLevel + 1) + "WARNING: " + mutationStr + " do not have last update.");
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
            if (vus == null) {
                System.out.println(spaceStrByNestLevel(nestLevel) + "No VUS available.");
            }
        }
    }

    private static void parseGene(JSONObject geneInfo, JSONArray vus) throws IOException, JSONException {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Integer nestLevel = 1;
        if (geneInfo.has("name") && !geneInfo.getString("name").trim().isEmpty()) {
            String hugo = geneInfo.has("name") ? geneInfo.getString("name").trim() : null;

            if (hugo != null) {
                Gene gene = geneBo.findGeneByHugoSymbol(hugo);

                if (gene == null) {
                    System.out.println(spaceStrByNestLevel(nestLevel) + "Don't save gene not in 476 gene list.");
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
                    System.out.println(spaceStrByNestLevel(nestLevel) + "Gene: " + gene.getHugoSymbol());
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
                    parseSummary(gene, geneInfo.has("summary") ? geneInfo.getString("summary").trim() : null, geneInfo.has("summary_uuid") ? geneInfo.getString("summary_uuid") : null, (geneInfo.has("summary_review") ? getUpdateTime(geneInfo.get("summary_review")) : null), nestLevel + 1);

                    // background
                    parseGeneBackground(gene, geneInfo.has("background") ? geneInfo.getString("background").trim() : null, geneInfo.has("background_uuid") ? geneInfo.getString("background_uuid") : null, (geneInfo.has("background_review") ? getUpdateTime(geneInfo.get("background_review")) : null), nestLevel + 1);

                    // mutations
                    parseMutations(gene, geneInfo.has("mutations") ? geneInfo.getJSONArray("mutations") : null, nestLevel + 1);

                    // Variants of unknown significance
                    parseVUS(gene, vus, nestLevel + 1);

                    CacheUtils.updateGene(gene.getEntrezGeneId());
                } else {
                    System.out.print(spaceStrByNestLevel(nestLevel) + "No info about " + hugo);
                }
            } else {
                System.out.println(spaceStrByNestLevel(nestLevel) + "No hugoSymbol available");
            }
        }
    }

    private static Date getUpdateTime(Object obj) throws JSONException {
        if (obj == null) return null;
        JSONObject reviewObj = new JSONObject(obj.toString());
        if (reviewObj.has("updateTime") && StringUtils.isNumeric(reviewObj.get("updateTime").toString())) {
            return new Date((long) reviewObj.get("updateTime"));
        }
        return null;
    }

    private static void parseSummary(Gene gene, String geneSummary, String uuid, Date lastEdit, Integer nestLevel) {
        System.out.println(spaceStrByNestLevel(nestLevel) + "Summary");
        // gene summary
        if (geneSummary != null && !geneSummary.isEmpty()) {
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.GENE_SUMMARY);
            evidence.setGene(gene);
            evidence.setDescription(geneSummary);
            evidence.setUuid(uuid);
            evidence.setLastEdit(lastEdit);
            if (lastEdit != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 1) +
                    "Last update on: " + MainUtils.getTimeByDate(lastEdit));
            }
            setDocuments(geneSummary, evidence);
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            evidenceBo.save(evidence);
            System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Has description");
        }
    }

    private static void parseGeneBackground(Gene gene, String bg, String uuid, Date lastEdit, Integer nestLevel) {
        System.out.println(spaceStrByNestLevel(nestLevel) + "Background");

        if (bg != null && !bg.isEmpty()) {
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.GENE_BACKGROUND);
            evidence.setGene(gene);
            evidence.setDescription(bg);
            evidence.setUuid(uuid);
            evidence.setLastEdit(lastEdit);
            if (lastEdit != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 1) +
                    "Last update on: " + MainUtils.getTimeByDate(lastEdit));
            }
            setDocuments(bg, evidence);
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            evidenceBo.save(evidence);
            System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Has description");
        }
    }

    private static void parseMutations(Gene gene, JSONArray mutations, Integer nestLevel) throws JSONException {
        if (mutations != null) {
            for (int i = 0; i < mutations.length(); i++) {
                parseMutation(gene, mutations.getJSONObject(i), nestLevel + 1);
            }
        }
    }

    private static void parseMutation(Gene gene, JSONObject mutationObj, Integer nestLevel) throws JSONException {
        String mutationStr = mutationObj.has("name") ? mutationObj.getString("name").trim() : null;

        if (mutationStr != null && !mutationStr.isEmpty() && !mutationStr.contains("?")) {
            System.out.println(spaceStrByNestLevel(nestLevel) + "Mutation: " + mutationStr);

            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
            AlterationType type = AlterationType.MUTATION; //TODO: cna and fusion

            Set<Alteration> alterations = new HashSet<>();

            Oncogenicity oncogenic = getOncogenicity(mutationObj);
            String oncogenic_uuid = mutationObj.has("oncogenic_uuid") ? mutationObj.getString("oncogenic_uuid") : "";
            Date oncogenic_lastEdit = mutationObj.has("oncogenic_review") ? getUpdateTime(mutationObj.get("oncogenic_review")) : null;

            Set<Date> lastEditDatesEffect = new HashSet<>();

            String effect = getMutationEffect(mutationObj);
            addDateToSetFromObject(lastEditDatesEffect, mutationObj, "effect_review");
            String effect_uuid = mutationObj.has("effect_uuid") ? mutationObj.getString("effect_uuid") : "";
            // If both mutation effect and oncogenicity both unknown, ignore variant.
            if (oncogenic != null && oncogenic.equals(Oncogenicity.INCONCLUSIVE)
                && effect != null && effect.toLowerCase().equals("inconclusive")
                && gene.getHugoSymbol().equals("EGFR")) {
                return;
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
                }
                alterations.add(alteration);
                setOncogenic(gene, alteration, oncogenic, oncogenic_uuid, oncogenic_lastEdit);
            }

            // mutation effect
            String effectDesc = mutationObj.has("description") ?
                (mutationObj.getString("description").trim().isEmpty() ? null :
                    mutationObj.getString("description").trim())
                : null;
            addDateToSetFromObject(lastEditDatesEffect, mutationObj, "description_review");
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
                evidence.setUuid(effect_uuid);

                Date effect_lastEdit = getMostRecentDate(lastEditDatesEffect);
                evidence.setLastEdit(effect_lastEdit);
                EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
                evidenceBo.save(evidence);
            }

            // cancers
            if (mutationObj.has("tumors")) {
                JSONArray cancers = mutationObj.getJSONArray("tumors");
                if(cancers != null && cancers.length() > 0) {
                    System.out.println(spaceStrByNestLevel(nestLevel) + "Tumor Types");
                }
                for (int i = 0; i < cancers.length(); i++) {
                    JSONArray subTumorTypes = cancers.getJSONObject(i).getJSONArray("cancerTypes");
                    for (int j = 0; j < subTumorTypes.length(); j++) {
                        JSONObject subTT = subTumorTypes.getJSONObject(j);
                        parseCancer(gene, alterations, cancers.getJSONObject(i),
                            subTT.has("cancerType") ? subTT.getString("cancerType") : null,
                            (subTT.has("oncoTreeCode") && !subTT.getString("oncoTreeCode").equals("")) ? subTT.getString("oncoTreeCode") : null, nestLevel + 1);
                    }
                }
            }
        } else {
            System.out.println(spaceStrByNestLevel(nestLevel) + "Mutation does not have name skip...");
        }
    }

    private static String getMutationEffect(JSONObject mutationObj) throws JSONException {
        return mutationObj.has("effect") ?
            (mutationObj.getJSONObject("effect").has("value") ?
                (mutationObj.getJSONObject("effect").getString("value").trim().isEmpty() ? null :
                    mutationObj.getJSONObject("effect").getString("value").trim())
                : null)
            : null;
    }

    protected static Oncogenicity getOncogenicityByString(String oncogenicStr) {
        Oncogenicity oncogenic = null;
        if (oncogenicStr != null) {
            oncogenicStr = oncogenicStr.toLowerCase();
            switch (oncogenicStr) {
                case "yes":
                    oncogenic = Oncogenicity.YES;
                    break;
                case "likely":
                    oncogenic = Oncogenicity.LIKELY;
                    break;
                case "likely neutral":
                    oncogenic = Oncogenicity.LIKELY_NEUTRAL;
                    break;
                case "inconclusive":
                    oncogenic = Oncogenicity.INCONCLUSIVE;
                    break;
                default:
                    break;
            }
        }
        return oncogenic;
    }

    private static Oncogenicity getOncogenicity(JSONObject mutationObj) throws JSONException {
        Oncogenicity oncogenic = null;
        if (mutationObj.has("oncogenic") && !mutationObj.getString("oncogenic").isEmpty()) {
            oncogenic = getOncogenicityByString(mutationObj.getString("oncogenic"));
        }
        return oncogenic;
    }

    private static void setOncogenic(Gene gene, Alteration alteration, Oncogenicity oncogenic, String uuid, Date lastEdit) {
        if (alteration != null && gene != null && oncogenic != null) {
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            List<Evidence> evidences = evidenceBo.findEvidencesByAlteration(Collections.singleton(alteration), Collections.singleton(EvidenceType.ONCOGENIC));
            if (evidences.isEmpty()) {
                Evidence evidence = new Evidence();
                evidence.setGene(gene);
                evidence.setAlterations(Collections.singleton(alteration));
                evidence.setEvidenceType(EvidenceType.ONCOGENIC);
                evidence.setKnownEffect(oncogenic.getOncogenic());
                evidence.setUuid(uuid);
                evidence.setLastEdit(lastEdit);
                evidenceBo.save(evidence);
            } else if (Oncogenicity.compare(oncogenic, Oncogenicity.getByEvidence(evidences.get(0))) > 0) {
                evidences.get(0).setKnownEffect(oncogenic.getOncogenic());
                evidences.get(0).setLastEdit(lastEdit);
                evidenceBo.update(evidences.get(0));
            }
        }
    }

    private static Map<String, String> parseMutationString(String mutationStr) {
        Map<String, String> ret = new HashMap<>();

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

    private static void parseCancer(Gene gene, Set<Alteration> alterations, JSONObject cancerObj, String cancerType, String code, Integer nestLevel) throws JSONException {
        if (cancerType == null || cancerType.equals("")) {
            return;
        }

        TumorType oncoTreeType;

        if (code != null && !code.equals("")) {
            oncoTreeType = TumorTypeUtils.getOncoTreeSubtypeByCode(code);
        } else {
            oncoTreeType = TumorTypeUtils.getOncoTreeCancerType(cancerType);
        }

        if (oncoTreeType == null) {
            System.out.println(spaceStrByNestLevel(nestLevel) + "WARNING: No mapped TumorType for " + cancerType + " " + code);
            return;
        }

        System.out.println(spaceStrByNestLevel(nestLevel) + "Cancer type: " + cancerType);
        System.out.println(spaceStrByNestLevel(nestLevel) + "Subtype code: " + code);

        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        // cancer type summary
        if (cancerObj.has("summary") && !cancerObj.getString("summary").isEmpty()) {
            System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Summary");
            Date lastEdit = cancerObj.has("summary_review") ? getUpdateTime(cancerObj.get("summary_review")) : null;
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.TUMOR_TYPE_SUMMARY);
            evidence.setGene(gene);
            evidence.setDescription(cancerObj.getString("summary"));
            evidence.setUuid(cancerObj.has("summary_uuid") ? cancerObj.getString("summary_uuid") : "");
            evidence.setAlterations(alterations);
            evidence.setLastEdit(lastEdit);
            if (lastEdit != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                    "Last update on: " + MainUtils.getTimeByDate(lastEdit));
            }
            if (oncoTreeType.getMainType() != null) {
                evidence.setCancerType(oncoTreeType.getMainType().getName());
            }
            evidence.setSubtype(oncoTreeType.getCode());
            setDocuments(cancerObj.getString("summary"), evidence);
            System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                "Has description.");
            evidenceBo.save(evidence);
        }

        // Prevalence
        if ((cancerObj.has("prevalence") && !cancerObj.getString("prevalence").trim().isEmpty()) || (cancerObj.has("shortPrevalence") && !cancerObj.getString("shortPrevalence").trim().isEmpty())) {
            System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Prevalence: " + alterations.toString());
            Date lastEdit = cancerObj.has("prevalence_review") ? getUpdateTime(cancerObj.get("prevalence_review")) : null;
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.PREVALENCE);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            if (oncoTreeType.getMainType() != null) {
                evidence.setCancerType(oncoTreeType.getMainType().getName());
            }
            evidence.setSubtype(oncoTreeType.getCode());
            evidence.setUuid(cancerObj.has("prevalence_uuid") ? cancerObj.getString("prevalence_uuid") : "");
            evidence.setLastEdit(lastEdit);
            if (lastEdit != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                    "Last update on: " + MainUtils.getTimeByDate(lastEdit));
            }
//            if (cancerObj.has("shortPrevalence") && !cancerObj.getString("shortPrevalence").trim().isEmpty()) {
//                System.out.println("###         Short prevalence: " + alterations.toString());
//                String additionalInfo = cancerObj.getString("shortPrevalence").trim();
//                evidence.setAdditionalInfo(additionalInfo);
//            }

            if (cancerObj.has("prevalence") && !cancerObj.getString("prevalence").trim().isEmpty()) {
                System.out.println(spaceStrByNestLevel(nestLevel + 2) + "Has description.");
                String desc = cancerObj.getString("prevalence").trim();
                evidence.setDescription(desc);
                setDocuments(desc, evidence);
            }
            evidenceBo.save(evidence);
        }

        // Prognostic implications
        parseImplication(gene, alterations, oncoTreeType,
            cancerObj.has("prognostic") ? cancerObj.getJSONObject("prognostic") : null,
            cancerObj.has("prognostic_uuid") ? cancerObj.getString("prognostic_uuid") : "",
            EvidenceType.PROGNOSTIC_IMPLICATION, nestLevel + 1);

        // Diagnostic implications
        parseImplication(gene, alterations, oncoTreeType,
            cancerObj.has("diagnostic") ? cancerObj.getJSONObject("diagnostic") : null,
            cancerObj.has("diagnostic_uuid") ? cancerObj.getString("diagnostic_uuid") : "",
            EvidenceType.DIAGNOSTIC_IMPLICATION, nestLevel + 1);

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
                    parseTherapeuticImplications(gene, alterations, oncoTreeType, implication, evidenceType, type, nestLevel + 1);
                }
            }
        }

        // NCCN
        if (cancerObj.has("nccn") && cancerObj.getJSONObject("nccn").has("disease") && !cancerObj.getJSONObject("nccn").getString("disease").isEmpty()) {
            System.out.println(spaceStrByNestLevel(nestLevel + 1) + "NCCN for " + alterations.toString());
            parseNCCN(gene, alterations, oncoTreeType, cancerObj.getJSONObject("nccn"), cancerObj.has("nccn_uuid") ? cancerObj.getString("nccn_uuid") : "", nestLevel + 2);
        }

        if (cancerObj.has("trials") && cancerObj.getJSONArray("trials").length() > 0) {
            System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Clinical trials for " + alterations.toString());
            parseClinicalTrials(gene, alterations, oncoTreeType, cancerObj.getJSONArray("trials"), cancerObj.has("trials_uuid") ? cancerObj.getString("trials_uuid") : "", (cancerObj.has("trials_review") ? getUpdateTime(cancerObj.get("trials_review")) : null), nestLevel + 2);
        }
    }

    private static void parseClinicalTrials(Gene gene, Set<Alteration> alterations, TumorType oncoTreeType, JSONArray trialsArray, String uuid, Date lastEdit, Integer nestLevel) throws JSONException {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        Set<String> nctIds = new HashSet<>();
        for (int i = 0; i < trialsArray.length(); i++) {
            String nctId = trialsArray.getString(i).trim();
            if (!nctId.isEmpty()) {
                nctIds.add(nctId);
            }
        }
        if (nctIds.isEmpty())
            return;

        System.out.println(spaceStrByNestLevel(nestLevel) + nctIds.size() + " trails");
        // Could not find out the reason why designed to remove all previous trials.
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
            if (oncoTreeType.getMainType() != null) {
                evidence.setCancerType(oncoTreeType.getMainType().getName());
            }
            evidence.setSubtype(oncoTreeType.getCode());
            evidence.setClinicalTrials(new HashSet<>(trials));
            evidence.setUuid(uuid);
            evidence.setLastEdit(lastEdit);
            if (lastEdit != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 1) +
                    "Last update on: " + MainUtils.getTimeByDate(lastEdit));
            }
            evidenceBo.save(evidence);
            if (trials.size() != nctIds.size()) {
                System.out.println(spaceStrByNestLevel(nestLevel + 1) + "WARNING: mismatch(s)!!!");
                System.out.println(spaceStrByNestLevel(nestLevel + 1) + "NCT Ids: " + StringUtils.join(nctIds, ", "));
                List<String> trialIds = new ArrayList<>();
                for (ClinicalTrial trial : trials) {
                    trialIds.add(trial.getNctId());
                }
                System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Matched NCT Ids: " + StringUtils.join(trialIds, ", "));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseTherapeuticImplications(Gene gene, Set<Alteration> alterations, TumorType oncoTreeType, JSONObject implicationObj,
                                                     EvidenceType evidenceType, String knownEffectOfEvidence, Integer nestLevel) throws JSONException {
        System.out.println(spaceStrByNestLevel(nestLevel) + evidenceType);

        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        if (implicationObj.has("description") && !implicationObj.getString("description").trim().isEmpty()) {
            // general description
            System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Has General Description.");
            Date lastEdit = implicationObj.has("description_review") ? getUpdateTime(implicationObj.get("description_review")) : null;
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(evidenceType);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            if (oncoTreeType.getMainType() != null) {
                evidence.setCancerType(oncoTreeType.getMainType().getName());
            }
            evidence.setSubtype(oncoTreeType.getCode());
            evidence.setKnownEffect(knownEffectOfEvidence);
            evidence.setUuid(implicationObj.has("description_uuid") ? implicationObj.getString("description_uuid") : "");
            evidence.setLastEdit(lastEdit);
            if (lastEdit != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 1) +
                    "Last update on: " + MainUtils.getTimeByDate(lastEdit));
            }
            String desc = implicationObj.getString("description");
            evidence.setDescription(desc);
            setDocuments(desc, evidence);
            evidenceBo.save(evidence);
        }

        // specific evidence
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        TreatmentBo treatmentBo = ApplicationContextSingleton.getTreatmentBo();
        JSONArray drugsArray = implicationObj.getJSONArray("treatments");

        for (int i = 0; i < drugsArray.length(); i++) {
            JSONObject drugObj = drugsArray.getJSONObject(i);
            if (!drugObj.has("name") || drugObj.getString("name").trim().isEmpty()) {
                System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Drug does not have name, skip... " + drugObj.toString());
                continue;
            }

            String drugNameStr = drugObj.getString("name").trim();
            System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Drug(s): " + drugNameStr);

            Set<Date> lastEditDates = new HashSet<>();
            addDateToSetFromObject(lastEditDates, drugObj, "name_review");

            Evidence evidence = new Evidence();
            evidence.setEvidenceType(evidenceType);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            if (oncoTreeType.getMainType() != null) {
                evidence.setCancerType(oncoTreeType.getMainType().getName());
            }
            evidence.setSubtype(oncoTreeType.getCode());
            evidence.setKnownEffect(knownEffectOfEvidence);
            evidence.setUuid(drugObj.has("name_uuid") ? drugObj.getString("name_uuid") : "");

            // approved indications
            Set<String> approvedIndications = new HashSet<>();
            if (drugObj.has("indication") && !drugObj.getString("indication").trim().isEmpty()) {
                approvedIndications = new HashSet<>(Arrays.asList(drugObj.getString("indication").split(";")));
                addDateToSetFromObject(lastEditDates, drugObj, "indication_review");
            }

            String[] drugTxts = drugNameStr.replaceAll("(\\([^\\)]*\\))|(\\[[^\\]]*\\])", "").split(",");

            Set<Treatment> treatments = new HashSet<>();
            for (String drugTxt : drugTxts) {
                String[] drugNames = drugTxt.split(" ?\\+ ?");

                Set<Drug> drugs = new HashSet<>();
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
                treatment.setApprovedIndications(approvedIndications);

                treatmentBo.save(treatment);

                treatments.add(treatment);
            }
            evidence.setTreatments(treatments);

            // highest level of evidence
            if (!drugObj.has("level") || drugObj.getString("level").trim().isEmpty()) {
                System.err.println(spaceStrByNestLevel(nestLevel + 2) + "Error: no level of evidence");
                // TODO:
                //throw new RuntimeException("no level of evidence");
            } else {
                String level = drugObj.getString("level").trim();
                addDateToSetFromObject(lastEditDates, drugObj, "level_review");
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
                    System.err.println(spaceStrByNestLevel(nestLevel + 2) + "Error: wrong level of evidence: " + level);
                    // TODO:
                    //throw new RuntimeException("wrong level of evidence: "+level);
                } else {
                    System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                        "Level: " + levelOfEvidence.getLevel());
                }
                evidence.setLevelOfEvidence(levelOfEvidence);

                List<LevelOfEvidence> acceptablePropagationList = new ArrayList<>();
                acceptablePropagationList.add(LevelOfEvidence.LEVEL_2B);
                acceptablePropagationList.add(LevelOfEvidence.LEVEL_3B);
                acceptablePropagationList.add(LevelOfEvidence.LEVEL_4);

                if (drugObj.has("propagation")) {
                    String definedPropagation = drugObj.getString("propagation");
                    if (definedPropagation.equals("no")) {
                        evidence.setPropagation("NO");
                    }
                    LevelOfEvidence definedLevel = LevelOfEvidence.getByLevel(definedPropagation);

                    // Validate level
                    if (definedLevel != null && acceptablePropagationList.contains(definedLevel)) {
                        evidence.setPropagation(definedLevel.name());
                    }
                    if (evidence.getPropagation() != null) {
                        System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                            "Manual propagation level: " + evidence.getPropagation());
                    }
                }

                // If there is no propagation info predefined, use the default settings.
                if (evidence.getPropagation() == null && evidence.getLevelOfEvidence() != null) {
                    if (evidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_1) ||
                        evidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_2A)) {
                        evidence.setPropagation(LevelOfEvidence.LEVEL_2B.name());
                    } else if (evidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_3A)) {
                        evidence.setPropagation(LevelOfEvidence.LEVEL_3B.name());
                    }
                }
            }

            // description
//            if (drugObj.has("short") && !drugObj.getString("short").trim().isEmpty()) {
//                String additionalInfo = drugObj.getString("short").trim();
//                evidence.setAdditionalInfo(additionalInfo);
//            }
            if (drugObj.has("description") && !drugObj.getString("description").trim().isEmpty()) {
                String desc = drugObj.getString("description").trim();
                addDateToSetFromObject(lastEditDates, drugObj, "description_review");
                evidence.setDescription(desc);
                System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                    "Has description.");
                setDocuments(desc, evidence);
            }

            Date lastEdit = getMostRecentDate(lastEditDates);
            if (lastEdit != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                    "Last update on: " + MainUtils.getTimeByDate(lastEdit));
            }
            evidence.setLastEdit(lastEdit);

            evidenceBo.save(evidence);
        }
    }

    private static void parseNCCN(Gene gene, Set<Alteration> alterations, TumorType oncoTreeType, JSONObject nccnObj, String uuid, Integer nestLevel) throws JSONException {
        // disease
        Set<Date> lastEditDates = new HashSet<>();

        String therapy = null;
        if (nccnObj.has("therapy") && !nccnObj.getString("therapy").trim().isEmpty()) {
            therapy = nccnObj.getString("therapy").trim();
            addDateToSetFromObject(lastEditDates, nccnObj, "therapy_review");
            System.out.println(spaceStrByNestLevel(nestLevel) + "Has therapy " + therapy);
        }

        // disease
        String disease = null;
        if (nccnObj.has("disease") && !nccnObj.getString("disease").trim().isEmpty()) {
            disease = nccnObj.getString("disease").trim();
            addDateToSetFromObject(lastEditDates, nccnObj, "disease_review");
            System.out.println(spaceStrByNestLevel(nestLevel) + "Has disease " + disease);
        }

        // version
        String version = null;
        if (nccnObj.has("version") && !nccnObj.getString("version").trim().isEmpty()) {
            version = nccnObj.getString("version").trim();
            addDateToSetFromObject(lastEditDates, nccnObj, "version_review");
            System.out.println(spaceStrByNestLevel(nestLevel) + "Has version " + version);
        }

        // pages
        String pages = null;
        if (nccnObj.has("pages") && !nccnObj.getString("pages").trim().isEmpty()) {
            pages = nccnObj.getString("pages").trim();
            addDateToSetFromObject(lastEditDates, nccnObj, "pages_review");
            System.out.println(spaceStrByNestLevel(nestLevel) + "Has pages " + pages);
        }

        // Recommendation category
        String category = null;
        if (nccnObj.has("category") && !nccnObj.getString("category").trim().isEmpty()) {
            category = nccnObj.getString("category").trim();
            addDateToSetFromObject(lastEditDates, nccnObj, "category_review");
            System.out.println(spaceStrByNestLevel(nestLevel) + "Has category " + category);
        }

        // description
        String nccnDescription = null;
        if (nccnObj.has("description") && !nccnObj.getString("description").trim().isEmpty()) {
            nccnDescription = nccnObj.getString("description").trim();
            addDateToSetFromObject(lastEditDates, nccnObj, "description_review");
            System.out.println(spaceStrByNestLevel(nestLevel) + "Has description.");
        }

        Date lastEdit = getMostRecentDate(lastEditDates);
        Evidence evidence = new Evidence();
        evidence.setEvidenceType(EvidenceType.NCCN_GUIDELINES);
        evidence.setAlterations(alterations);
        evidence.setGene(gene);
        if (oncoTreeType.getMainType() != null) {
            evidence.setCancerType(oncoTreeType.getMainType().getName());
        }
        evidence.setSubtype(oncoTreeType.getCode());
        evidence.setDescription(nccnDescription);
        evidence.setUuid(uuid);
        evidence.setLastEdit(lastEdit);
        if (lastEdit != null) {
            System.out.println(spaceStrByNestLevel(nestLevel) +
                "Last update on: " + MainUtils.getTimeByDate(lastEdit));
        }
        NccnGuidelineBo nccnGuideLineBo = ApplicationContextSingleton.getNccnGuidelineBo();

        NccnGuideline nccnGuideline = new NccnGuideline();
        nccnGuideline.setTherapy(therapy);
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

    private static void parseImplication(Gene gene, Set<Alteration> alterations, TumorType oncoTreeType, JSONObject implication, String uuid, EvidenceType evidenceType, Integer nestLevel) throws JSONException {
        if (evidenceType != null && implication != null &&
            ((implication.has("description") && !implication.getString("description").trim().isEmpty())
                || (implication.has("level") && !implication.getString("level").trim().isEmpty()))) {
            System.out.println(spaceStrByNestLevel(nestLevel) + evidenceType.name() + ":");
            Set<Date> lastEditDates = new HashSet<>();
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            Evidence evidence = new Evidence();

            evidence.setEvidenceType(evidenceType);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setUuid(uuid);

            if (oncoTreeType != null) {
                if (oncoTreeType.getMainType() != null) {
                    evidence.setCancerType(oncoTreeType.getMainType().getName());
                }
                evidence.setSubtype(oncoTreeType.getCode());
            }
            if (implication.has("level") && !implication.getString("level").trim().isEmpty()) {
                LevelOfEvidence level = LevelOfEvidence.getByLevel(implication.getString("level").trim());
                System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Level of the implication: " + level);
                evidence.setLevelOfEvidence(level);
                addDateToSetFromObject(lastEditDates, implication, "level_review");
            }

            if (implication.has("description") && !implication.getString("description").trim().isEmpty()) {
                System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Has description.");
                String desc = implication.getString("description").trim();
                evidence.setDescription(desc);
                addDateToSetFromObject(lastEditDates, implication, "description_review");
                setDocuments(desc, evidence);
            }

            Date lastEdit = getMostRecentDate(lastEditDates);
            evidence.setLastEdit(lastEdit);
            if (lastEdit != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 1) +
                    "Last update on: " + MainUtils.getTimeByDate(lastEdit));
            }
            evidenceBo.save(evidence);
        }
    }

    private static String spaceStrByNestLevel(Integer nestLevel) {
        if (nestLevel == null || nestLevel < 1)
            nestLevel = 1;
        return StringUtils.repeat("    ", nestLevel - 1);
    }

    private static void setDocuments(String str, Evidence evidence) {
        if (str == null) return;
        Set<Article> docs = new HashSet<>();
        Set<ClinicalTrial> clinicalTrials = new HashSet<>();
        ArticleBo articleBo = ApplicationContextSingleton.getArticleBo();
        ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();
        Pattern pmidPattern = Pattern.compile("PMIDs?:\\s*([\\d,\\s*]+)", Pattern.CASE_INSENSITIVE);
        Pattern abstractPattern = Pattern.compile("\\(\\s*Abstract\\s*:([^\\)]*);?\\s*\\)", Pattern.CASE_INSENSITIVE);
        Pattern abItemPattern = Pattern.compile("(.*?)\\.\\s*(http.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = pmidPattern.matcher(str);
        int start = 0;
        while (m.find(start)) {
            String pmids = m.group(1).trim();
            for (String pmid : pmids.split(", *(PMID:)? *")) {
                if (pmid.startsWith("NCT")) {
                    // support NCT numbers
                    Set<String> nctIds = new HashSet<>(Arrays.asList(pmid.split(", *")));
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
                }
                if (doc != null) {
                    articleBo.save(doc);
                    docs.add(doc);
                }
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
                    Article doc = articleBo.findArticleByAbstract(abContent);
                    if (doc == null) {
                        doc = new Article();
                        doc.setAbstractContent(abContent);
                        doc.setLink(abLink);
                        articleBo.save(doc);
                    }
                    docs.add(doc);
                }
                abContent = "";
                abLink = "";

            }
            start = abstractMatch.end();

        }

        evidence.addArticles(docs);
        evidence.addClinicalTrials(clinicalTrials);
    }

    private static void addDateToSetFromObject(Set<Date> set, JSONObject object, String key) throws JSONException {
        if (object.has(key)) {
            Date tmpDate = getUpdateTime(object.get(key));
            if (tmpDate != null) {
                set.add(tmpDate);
            }
        }
    }

    private static Date getMostRecentDate(Set<Date> dates) {
        if (dates == null || dates.size() == 0)
            return null;
        return Collections.max(dates);
    }
}
