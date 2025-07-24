/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.*;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.mskcc.cbio.oncokb.util.ArticleUtils.getAbstractFromText;
import static org.mskcc.cbio.oncokb.util.ArticleUtils.getPmidsFromText;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * @author jgao
 */
@Controller
public class DriveAnnotationParser {
    private static final Logger LOGGER = LogManager.getLogger();
    OncokbTranscriptService oncokbTranscriptService = new OncokbTranscriptService();

    @RequestMapping(value = "/legacy-api/driveAnnotation", method = POST)
    public
    @ResponseBody
    synchronized void getEvidence(
        @RequestParam(value = "gene") String gene,
        @RequestParam(value = "releaseGene", defaultValue = "FALSE") Boolean releaseGene,
        @RequestParam(value = "vus", required = false) String vus
    ) throws Exception {

        if (gene == null) {
            LOGGER.info("#No gene info available.");
        } else {
            JSONObject jsonObj = new JSONObject(gene);
            JSONArray jsonArray = null;
            if (vus != null) {
                jsonArray = new JSONArray(vus);
            }
            parseGene(jsonObj, releaseGene, jsonArray);
        }
    }

    private static final String LAST_EDIT_EXTENSION = "_review";
    private static final String UUID_EXTENSION = "_uuid";
    private static final String SOLID_PROPAGATION_KEY = "propagation";
    private static final String LIQUID_PROPAGATION_KEY = "propagationLiquid";
    private static final String FDA_LEVEL_KEY = "fdaLevel";
    private static final String EXCLUDED_RCTS_KEY = "excludedRCTs";

    public void parseVUS(Gene gene, JSONArray vus, Integer nestLevel) throws JSONException {
        LOGGER.info("{} Variants of unknown significance", spaceStrByNestLevel(nestLevel));
        if (vus != null) {
            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            AlterationType type = AlterationType.MUTATION; //TODO: cna and fusion

            LOGGER.info("{} VUSs", vus.length());
            for (int i = 0; i < vus.length(); i++) {
                JSONObject variant = vus.getJSONObject(i);
                String mutationStr = variant.has("name") ? variant.getString("name") : null;
                JSONObject time = variant.has("time") ? variant.getJSONObject("time") : null;
                Long lastEdit = null;
                if (time != null) {
                    lastEdit = time.has("value") ? time.getLong("value") : null;
                }
//                JSONArray nameComments = variant.has("nameComments") ? variant.getJSONArray("nameComments") : null;
                if (mutationStr != null) {
                    List<Alteration> mutations = AlterationUtils.parseMutationString(mutationStr, ",");
                    Set<Alteration> alterations = new HashSet<>();
                    for (Alteration mutation : mutations) {
                        Alteration alteration = alterationBo.findAlteration(gene, type, mutation.getAlteration());
                        if (alteration == null) {
                            alteration = new Alteration();
                            alteration.setGene(gene);
                            alteration.setAlterationType(type);
                            alteration.setAlteration(mutation.getAlteration());
                            alteration.setName(mutation.getName());
                            alteration.setReferenceGenomes(mutation.getReferenceGenomes());
                            AlterationUtils.annotateAlteration(alteration, mutation.getAlteration());
                            alterationBo.save(alteration);
                        } else if (!alteration.getReferenceGenomes().equals(mutation.getReferenceGenomes())) {
                            alteration.setReferenceGenomes(mutation.getReferenceGenomes());
                            alterationBo.save(alteration);
                        }
                        alterations.add(alteration);
                    }

                    Evidence evidence = new Evidence();
                    evidence.setEvidenceType(EvidenceType.VUS);
                    evidence.setGene(gene);
                    evidence.setAlterations(alterations);
                    if (lastEdit != null) {
                        Date date = new Date(lastEdit);
                        evidence.setLastEdit(date);
//                        evidence.setLastReview(date);
                    }
                    if (evidence.getLastEdit() == null) {
                        LOGGER.warn("{} WARNING: {} do not have last update.", spaceStrByNestLevel(nestLevel + 1));
                    }
                    evidenceBo.save(evidence);
                }
                if (i % 10 == 9)
                    LOGGER.info("Imported {}", i + 1);
            }
        } else {
            if (vus == null) {
                LOGGER.info("{} No VUS available.", spaceStrByNestLevel(nestLevel));
            }
        }
    }

    private void updateGeneInfo(JSONObject geneInfo, Gene gene) {
        JSONObject geneType = geneInfo.has("type") ? geneInfo.getJSONObject("type") : null;
        String oncogene = geneType == null ? null : (geneType.has("ocg") ? geneType.getString("ocg").trim() : null);
        String tsg = geneType == null ? null : (geneType.has("tsg") ? geneType.getString("tsg").trim() : null);

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

        String grch37Isoform = geneInfo.has("isoform_override") ? geneInfo.getString("isoform_override") : null;
        String grch37RefSeq = geneInfo.has("dmp_refseq_id") ? geneInfo.getString("dmp_refseq_id") : null;
        String grch38Isoform = geneInfo.has("isoform_override_grch38") ? geneInfo.getString("isoform_override_grch38") : null;
        String grch38RefSeq = geneInfo.has("dmp_refseq_id_grch38") ? geneInfo.getString("dmp_refseq_id_grch38") : null;

        if (grch37Isoform != null) {
            gene.setGrch37Isoform(grch37Isoform);
        }
        if (grch37RefSeq != null) {
            gene.setGrch37RefSeq(grch37RefSeq);
        }
        if (grch38Isoform != null) {
            gene.setGrch38Isoform(grch38Isoform);
        }
        if (grch38RefSeq != null) {
            gene.setGrch38RefSeq(grch38RefSeq);
        }
    }

    private Gene parseGene(JSONObject geneInfo, Boolean releaseGene, JSONArray vus) throws Exception {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Integer nestLevel = 1;
        if (geneInfo.has("name") && !geneInfo.getString("name").trim().isEmpty()) {
            String hugo = geneInfo.has("name") ? geneInfo.getString("name").trim() : null;

            if (hugo != null) {
                Gene gene = geneBo.findGeneByHugoSymbol(hugo);

                if (gene == null) {
                    LOGGER.info("{} Gene {} is not in the released list.", spaceStrByNestLevel(nestLevel), hugo);
                    if (releaseGene) {
                        OncokbTranscriptService oncokbTranscriptService = new OncokbTranscriptService();
                        gene = oncokbTranscriptService.findGeneBySymbol(hugo);
                        if (gene == null) {
                            LOGGER.error("!!!!!!!!!Could not find gene {} either.", hugo);
                            throw new IOException("!!!!!!!!!Could not find gene " + hugo + ".");
                        } else {
                            updateGeneInfo(geneInfo, gene);
                            geneBo.saveOrUpdate(gene);
                        }
                    } else {
                        return null;
                    }
                }

                if (gene != null) {
                    LOGGER.info("{} Gene: {}", spaceStrByNestLevel(nestLevel), gene.getHugoSymbol());
                    updateGeneInfo(geneInfo, gene);
                    geneBo.update(gene);

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

                    CacheUtils.updateGene(Collections.singleton(gene.getEntrezGeneId()), false);

                    // summary
                    parseSummary(gene, geneInfo.has("summary") ? geneInfo.getString("summary").trim() : null, getUUID(geneInfo, "summary"), getLastEdit(geneInfo, "summary"), nestLevel + 1);

                    // background
                    parseGeneBackground(gene, geneInfo.has("background") ? geneInfo.getString("background").trim() : null, getUUID(geneInfo, "background"), getLastEdit(geneInfo, "background"), nestLevel + 1);

                    // mutations
                    parseMutations(gene, geneInfo.has("mutations") ? geneInfo.getJSONArray("mutations") : null, nestLevel + 1);

                    // Variants of unknown significance
                    parseVUS(gene, vus, nestLevel + 1);

                    CacheUtils.updateGene(Collections.singleton(gene.getEntrezGeneId()), true);
                } else {
                    LOGGER.info("{} No info about {}", spaceStrByNestLevel(nestLevel), hugo);
                }
                return gene;
            } else {
                LOGGER.info("{} No hugoSymbol available", spaceStrByNestLevel(nestLevel));
            }
        }
        return null;
    }

    private Date getUpdateTime(Object obj) throws JSONException {
        if (obj == null) return null;
        JSONObject reviewObj = new JSONObject(obj.toString());
        if (reviewObj.has("updateTime") && StringUtils.isNumeric(reviewObj.get("updateTime").toString())) {
            return new Date(reviewObj.getLong("updateTime"));
        }
        return null;
    }

    private void parseSummary(Gene gene, String geneSummary, String uuid, Date lastEdit, Integer nestLevel) {
        LOGGER.info("{} Summary", spaceStrByNestLevel(nestLevel));
        // gene summary
        if (geneSummary != null && !geneSummary.isEmpty()) {
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.GENE_SUMMARY);
            evidence.setGene(gene);
            evidence.setDescription(geneSummary);
            evidence.setUuid(uuid);
            evidence.setLastEdit(lastEdit);
            if (lastEdit != null) {
                LOGGER.info("{} Last update on: {}", spaceStrByNestLevel(nestLevel + 1), MainUtils.getTimeByDate(lastEdit));
            }
            setDocuments(geneSummary, evidence);
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            evidenceBo.save(evidence);
            LOGGER.info(spaceStrByNestLevel(nestLevel + 1) + "Has description");
        }
    }

    private void parseGeneBackground(Gene gene, String bg, String uuid, Date lastEdit, Integer nestLevel) {
        LOGGER.info(spaceStrByNestLevel(nestLevel) + "Background");

        if (bg != null && !bg.isEmpty()) {
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.GENE_BACKGROUND);
            evidence.setGene(gene);
            evidence.setDescription(bg);
            evidence.setUuid(uuid);
            evidence.setLastEdit(lastEdit);
            if (lastEdit != null) {
                LOGGER.info("{} Last update on: {}", spaceStrByNestLevel(nestLevel + 1), MainUtils.getTimeByDate(lastEdit));
            }
            setDocuments(bg, evidence);
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            evidenceBo.save(evidence);
            LOGGER.info("{} Has description", spaceStrByNestLevel(nestLevel + 1));
        }
    }

    private void parseMutations(Gene gene, JSONArray mutations, Integer nestLevel) throws Exception {
        if (mutations != null) {
            LOGGER.info("{} {} mutations.", spaceStrByNestLevel(nestLevel), mutations.length());
            for (int i = 0; i < mutations.length(); i++) {
                parseMutation(gene, mutations.getJSONObject(i), nestLevel + 1);
            }
        } else {
            LOGGER.info("{} No mutation.", spaceStrByNestLevel(nestLevel));
        }
    }

    private void parseMutation(Gene gene, JSONObject mutationObj, Integer nestLevel) throws Exception {
        String mutationStr = mutationObj.has("name") ? mutationObj.getString("name").trim() : null;

        if (mutationStr != null && !mutationStr.isEmpty() && !mutationStr.contains("?")) {
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            LOGGER.info("{} Mutation: {}", spaceStrByNestLevel(nestLevel), mutationStr);

            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
            AlterationType type = AlterationType.MUTATION; //TODO: cna and fusion

            Set<Alteration> alterations = new HashSet<>();

            JSONObject mutationEffect = mutationObj.has("mutation_effect") ? mutationObj.getJSONObject("mutation_effect") : null;

            Oncogenicity oncogenic = getOncogenicity(mutationEffect);
            String oncogenic_uuid = getUUID(mutationEffect, "oncogenic");
            Date oncogenic_lastEdit = getLastEdit(mutationEffect, "oncogenic");
//            Date oncogenic_lastReview = getLastReview(mutationEffect, "oncogenic");

            Set<Date> lastEditDatesEffect = new HashSet<>();
            Set<Date> lastReviewDatesEffect = new HashSet<>();

            String effect = mutationEffect.has("effect") ? mutationEffect.getString("effect") : null;
            addDateToLastEditSetFromObject(lastEditDatesEffect, mutationEffect, "effect");
//            addDateToLastReviewSetFromLong(lastReviewDatesEffect, mutationEffect, "effect");
            String effect_uuid = getUUID(mutationEffect, "effect");

            List<Alteration> mutations = AlterationUtils.parseMutationString(mutationStr, ",");
            for (Alteration mutation : mutations) {
                Alteration alteration = alterationBo.findAlteration(gene, type, mutation.getAlteration());
                if (alteration == null) {
                    alteration = new Alteration();
                    alteration.setGene(gene);
                    alteration.setAlterationType(type);
                    alteration.setAlteration(mutation.getAlteration());
                    alteration.setName(mutation.getName());
                    alteration.setReferenceGenomes(mutation.getReferenceGenomes());
                    AlterationUtils.annotateAlteration(alteration, mutation.getAlteration());
                    alterationBo.save(alteration);
                } else if (!alteration.getReferenceGenomes().equals(mutation.getReferenceGenomes())) {
                    alteration.setReferenceGenomes(mutation.getReferenceGenomes());
                    alterationBo.save(alteration);
                }
                alterations.add(alteration);
                setOncogenic(gene, alteration, oncogenic, oncogenic_uuid, oncogenic_lastEdit);
            }

            // mutation effect
            String effectDesc = mutationEffect.has("description") ?
                (mutationEffect.getString("description").trim().isEmpty() ? null :
                    mutationEffect.getString("description").trim())
                : null;
            addDateToLastEditSetFromObject(lastEditDatesEffect, mutationEffect, "description");
//            addDateToLastReviewSetFromLong(lastReviewDatesEffect, mutationEffect, "description");
//            String additionalME = mutationEffect.has("short") ?
//                (mutationEffect.getString("short").trim().isEmpty() ? null : mutationEffect.getString("short").trim())
//                : null;

            if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(effect) || !com.mysql.jdbc.StringUtils.isNullOrEmpty(effectDesc)) {
                // save
                Evidence evidence = new Evidence();
                evidence.setEvidenceType(EvidenceType.MUTATION_EFFECT);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);

                if ((effectDesc != null && !effectDesc.trim().isEmpty())) {
                    evidence.setDescription(effectDesc);
                    setDocuments(effectDesc, evidence);
                }
                evidence.setKnownEffect(effect);
                evidence.setUuid(effect_uuid);

                Date effect_lastEdit = getMostRecentDate(lastEditDatesEffect);
                evidence.setLastEdit(effect_lastEdit);
                evidenceBo.save(evidence);
            }

            // add mutation summary
            String mutationSummary = mutationObj.has("summary") ? mutationObj.getString("summary") : "";
            if (StringUtils.isNotEmpty(mutationSummary)) {
                Evidence evidence = new Evidence();
                evidence.setEvidenceType(EvidenceType.MUTATION_SUMMARY);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);
                evidence.setDescription(mutationSummary);
                setDocuments(mutationSummary, evidence);
                evidence.setUuid(getUUID(mutationObj, "summary"));
                evidence.setLastEdit(getLastEdit(mutationObj, "summary"));
                evidenceBo.save(evidence);
            }

            // cancers
            if (mutationObj.has("tumors")) {
                JSONArray cancers = mutationObj.getJSONArray("tumors");
                if (cancers != null && cancers.length() > 0) {
                    LOGGER.info("{} Tumor Types", spaceStrByNestLevel(nestLevel));
                }
                for (int i = 0; i < cancers.length(); i++) {
                    JSONArray subTumorTypes = cancers.getJSONObject(i).getJSONArray("cancerTypes");
                    List<TumorType> tumorTypes = getTumorTypes(subTumorTypes);

                    List<TumorType> excludedCancerTypes = new ArrayList<>();
                    if (cancers.getJSONObject(i).has("excludedCancerTypes")) {
                        excludedCancerTypes = getTumorTypes(cancers.getJSONObject(i).getJSONArray("excludedCancerTypes"));
                    }

                    List<TumorType> relevantCancerTypes = getRelevantCancerTypesIfExistsFromJsonObject(cancers.getJSONObject(i), tumorTypes, excludedCancerTypes, null);

                    parseCancer(gene, alterations, cancers.getJSONObject(i), tumorTypes, excludedCancerTypes, relevantCancerTypes, nestLevel + 1);
                }
            }
        } else {
            LOGGER.info("{} Mutation does not have name skip...", spaceStrByNestLevel(nestLevel));
        }
    }

    private List<TumorType> getTumorTypes(JSONArray tumorTypeJson) throws Exception {
        List<TumorType> tumorTypes = new ArrayList<>();
        for (int j = 0; j < tumorTypeJson.length(); j++) {
            JSONObject subTT = tumorTypeJson.getJSONObject(j);
            String code = (subTT.has("code") && !subTT.getString("code").equals("")) ? subTT.getString("code") : null;
            String mainType = subTT.has("mainType") ? subTT.getString("mainType") : null;
            if (code != null) {
                TumorType matchedTumorType = ApplicationContextSingleton.getTumorTypeBo().getByCode(code);
                if (matchedTumorType == null) {
                    throw new Exception("The tumor type code does not exist: " + code);
                } else {
                    tumorTypes.add(matchedTumorType);
                }
            } else if (mainType != null) {
                TumorType matchedTumorType = ApplicationContextSingleton.getTumorTypeBo().getByMainType(mainType);
                if (matchedTumorType == null) {
                    throw new Exception("The tumor main type does not exist: " + mainType);
                } else {
                    tumorTypes.add(matchedTumorType);
                }
            } else {
                throw new Exception("The tumor type does not exist. Maintype: " + mainType + ". Subtype: " + code);
            }
        }
        return tumorTypes;
    }

    protected Oncogenicity getOncogenicityByString(String oncogenicStr) {
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
                case "resistance":
                    oncogenic = Oncogenicity.RESISTANCE;
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

    private Oncogenicity getOncogenicity(JSONObject mutationEffect) throws JSONException {
        Oncogenicity oncogenic = null;
        if (mutationEffect.has("oncogenic") && !mutationEffect.getString("oncogenic").isEmpty()) {
            oncogenic = getOncogenicityByString(mutationEffect.getString("oncogenic"));
        }
        return oncogenic;
    }

    private void setOncogenic(Gene gene, Alteration alteration, Oncogenicity oncogenic, String uuid, Date lastEdit) {
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
//                evidence.setLastReview(lastReview);
                evidenceBo.save(evidence);
            } else if (Oncogenicity.compare(oncogenic, Oncogenicity.getByEvidence(evidences.get(0))) > 0) {
                evidences.get(0).setKnownEffect(oncogenic.getOncogenic());
                evidences.get(0).setLastEdit(lastEdit);
                evidenceBo.update(evidences.get(0));
            }
        }
    }

    private void saveDxPxSummaries(JSONObject cancerObj, String summaryKey, Gene gene, Set<Alteration> alterations, List<TumorType> tumorTypes, List<TumorType> excludedCancerTypes, List<TumorType> relevantCancerTypes, EvidenceType evidenceType, Integer nestLevel, LevelOfEvidence level) {
        List<TumorType> rcts = new ArrayList<>(relevantCancerTypes);
        if ((rcts == null || rcts.size() == 0) && LevelOfEvidence.LEVEL_Dx1.equals(level)) {
            rcts.addAll(TumorTypeUtils.getDxOneRelevantCancerTypes(new HashSet<>(tumorTypes)));
        }
        saveTumorLevelSummaries(
            cancerObj,
            summaryKey,
            gene,
            alterations,
            tumorTypes,
            excludedCancerTypes,
            rcts,
            evidenceType,
            nestLevel);
    }

    private void saveTumorLevelSummaries(JSONObject cancerObj, String summaryKey, Gene gene, Set<Alteration> alterations, List<TumorType> tumorTypes, List<TumorType> excludedCancerTypes, List<TumorType> relevantCancerTypes, EvidenceType evidenceType, Integer nestLevel) {
        if (cancerObj.has(summaryKey) && !cancerObj.getString(summaryKey).isEmpty()) {
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            LOGGER.info("{} {}", spaceStrByNestLevel(nestLevel + 1), summaryKey);
            Date lastEdit = getLastEdit(cancerObj, summaryKey);
//            Date lastReview = getLastReview(cancerObj, summaryKey);
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(evidenceType);
            evidence.setGene(gene);
            evidence.setDescription(cancerObj.getString(summaryKey));
            evidence.setUuid(getUUID(cancerObj, summaryKey));
            evidence.setAlterations(alterations);
            evidence.setLastEdit(lastEdit);
            if (excludedCancerTypes != null && !excludedCancerTypes.isEmpty()) {
                evidence.setExcludedCancerTypes(new HashSet<>(excludedCancerTypes));
            }
            if (relevantCancerTypes != null && !relevantCancerTypes.isEmpty()) {
                evidence.setRelevantCancerTypes(new HashSet<>(relevantCancerTypes));
            }
//            evidence.setLastReview(lastReview);
            if (lastEdit != null) {
                LOGGER.info("{} Last update on: {}", spaceStrByNestLevel(nestLevel + 2), MainUtils.getTimeByDate(lastEdit));
            }
            if (!tumorTypes.isEmpty()) {
                evidence.setCancerTypes(new HashSet<>(tumorTypes));
            }
            setDocuments(cancerObj.getString(summaryKey), evidence);
            LOGGER.info("{} Has description.", spaceStrByNestLevel(nestLevel + 2));
            evidenceBo.save(evidence);
        }
    }

    private void parseCancer(Gene gene, Set<Alteration> alterations, JSONObject cancerObj, List<TumorType> tumorTypes, List<TumorType> excludedCancerTypes, List<TumorType> relevantCancerTypes, Integer nestLevel) throws Exception {
        if (tumorTypes.isEmpty()) {
            return;
        }

        LOGGER.info("{} Tumor types: {}", spaceStrByNestLevel(nestLevel), tumorTypes.stream().map(TumorTypeUtils::getTumorTypeName).collect(Collectors.joining(", ")));

        // cancer type summary
        saveTumorLevelSummaries(cancerObj, "summary", gene, alterations, tumorTypes, excludedCancerTypes, relevantCancerTypes, EvidenceType.TUMOR_TYPE_SUMMARY, nestLevel);

        // Prognostic implications
        Evidence prognosticEvidence = parseImplication(gene, alterations, tumorTypes, excludedCancerTypes, relevantCancerTypes,
            cancerObj.has("prognostic") ? cancerObj.getJSONObject("prognostic") : null,
            getUUID(cancerObj, "prognostic"),
            EvidenceType.PROGNOSTIC_IMPLICATION, nestLevel + 1);

        // Diagnostic implications
        Evidence diagnosticEvidence = parseImplication(gene, alterations, tumorTypes, excludedCancerTypes, relevantCancerTypes,
            cancerObj.has("diagnostic") ? cancerObj.getJSONObject("diagnostic") : null,
            getUUID(cancerObj, "diagnostic"),
            EvidenceType.DIAGNOSTIC_IMPLICATION, nestLevel + 1);

        // diagnostic summary
        List<TumorType> diagnosticRCT = getRelevantCancerTypesIfExistsFromJsonObject(cancerObj.getJSONObject("diagnostic"), tumorTypes, excludedCancerTypes, diagnosticEvidence == null ? null : diagnosticEvidence.getLevelOfEvidence());
        saveDxPxSummaries(
            cancerObj,
            "diagnosticSummary",
            gene,
            alterations,
            tumorTypes,
            excludedCancerTypes,
            cancerObj.has("diagnostic") ? diagnosticRCT : relevantCancerTypes,
            EvidenceType.DIAGNOSTIC_SUMMARY,
            nestLevel,
            diagnosticEvidence == null ? null : diagnosticEvidence.getLevelOfEvidence()
        );

        // prognostic summary
        List<TumorType> prognosticRCT = getRelevantCancerTypesIfExistsFromJsonObject(cancerObj.getJSONObject("prognostic"), tumorTypes, excludedCancerTypes, prognosticEvidence == null ? null : prognosticEvidence.getLevelOfEvidence());
        saveDxPxSummaries(cancerObj,
            "prognosticSummary",
            gene,
            alterations,
            tumorTypes,
            excludedCancerTypes,
            cancerObj.has("prognostic") ? prognosticRCT : relevantCancerTypes,
            EvidenceType.PROGNOSTIC_SUMMARY,
            nestLevel,
            prognosticEvidence == null ? null : prognosticEvidence.getLevelOfEvidence()
        );

        JSONArray implications = cancerObj.getJSONArray("TIs");

        for (int i = 0; i < implications.length(); i++) {
            JSONObject implication = implications.getJSONObject(i);
            if ((implication.has("description") && !implication.getString("description").trim().isEmpty()) || (implication.has("treatments") && implication.getJSONArray("treatments").length() > 0)) {
                parseTherapeuticImplications(gene, alterations, tumorTypes, excludedCancerTypes, relevantCancerTypes, implication, nestLevel + 1);
            }
        }
    }

    private void parseTherapeuticImplications(Gene gene, Set<Alteration> alterations, List<TumorType> tumorTypes, List<TumorType> excludedCancerTypes, List<TumorType> relevantCancerTypes, JSONObject implicationObj,
                                              Integer nestLevel) throws Exception {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        // specific evidence
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        JSONArray treatmentsArray = implicationObj.has("treatments") ? implicationObj.getJSONArray("treatments") : new JSONArray();
        int priorityCount = 1;
        for (int i = 0; i < treatmentsArray.length(); i++) {
            JSONObject drugObj = treatmentsArray.getJSONObject(i);
            if (!drugObj.has("name") || drugObj.getJSONArray("name").length() == 0) {
                LOGGER.info("{} Drug does not have name, skip... {}", spaceStrByNestLevel(nestLevel + 1), drugObj);
                continue;
            }

            JSONArray therapiesArray = drugObj.getJSONArray("name");
            LOGGER.info("{} Drug(s): {}", spaceStrByNestLevel(nestLevel + 1), therapiesArray.length());

            Set<Date> lastEditDates = new HashSet<>();
            Set<Date> lastReviewDates = new HashSet<>();
            addDateToLastEditSetFromObject(lastEditDates, drugObj, "name");
//            addDateToLastReviewSetFromLong(lastReviewDates, drugObj, "name");

            ImmutablePair<EvidenceType, String> evidenceTypeAndKnownEffect = getEvidenceTypeAndKnownEffectFromDrugObj(drugObj);
            EvidenceType evidenceType = evidenceTypeAndKnownEffect.getLeft();
            String knownEffect = evidenceTypeAndKnownEffect.getRight();
            if (evidenceType == null) {
                LOGGER.error("{} Could not get evidence type {}", spaceStrByNestLevel(nestLevel + 1), drugObj);
                continue;
            }
            if (knownEffect == null) {
                LOGGER.error("Could not get known effect", spaceStrByNestLevel(nestLevel + 1), drugObj);
            }

            Evidence evidence = new Evidence();
            evidence.setEvidenceType(evidenceType);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setCancerTypes(new HashSet<>(tumorTypes));
            evidence.setKnownEffect(knownEffect);
            evidence.setUuid(getUUID(drugObj, "name"));

            // approved indications
            // TODO: Remove because we do not support indication on new curation platform anymore
            Set<String> approvedIndications = new HashSet<>();
            if (drugObj.has("indication") && !drugObj.getString("indication").trim().isEmpty()) {
                approvedIndications = new HashSet<>(Arrays.asList(drugObj.getString("indication").split(";")));
                addDateToLastEditSetFromObject(lastEditDates, drugObj, "indication");
//                addDateToLastReviewSetFromLong(lastReviewDates, drugObj, "indication");
            }

            List<Treatment> treatments = new ArrayList<>();
            for (int j = 0; j < therapiesArray.length(); j++) {
                JSONArray drugsArray = therapiesArray.getJSONArray(j);

                List<Drug> drugs = new ArrayList<>();
                for (int k = 0; k < drugsArray.length(); k++) {
                    JSONObject drugObject = drugsArray.getJSONObject(k);

                    String ncitCode = drugObject.has("ncitCode") ? drugObject.getString("ncitCode").trim() : null;
                    if (ncitCode != null && ncitCode.isEmpty()) {
                        ncitCode = null;
                    }
                    String drugName = drugObject.has("drugName") ? drugObject.getString("drugName").trim() : null;
                    if (drugName != null && drugName.isEmpty()) {
                        drugName = null;
                    }
                    String drugUuid = drugObject.has("uuid") ? drugObject.getString("uuid").trim() : null;
                    Drug drug = null;
                    if (ncitCode != null) {
                        drug = drugBo.findDrugsByNcitCode(ncitCode);
                    }
                    if (drug == null && drugName != null) {
                        drug = drugBo.findDrugByName(drugName);
                    }
                    if (drug == null) {
                        if (ncitCode != null) {
                            org.oncokb.oncokb_transcript.client.Drug ncitDrug = oncokbTranscriptService.findDrugByNcitCode(ncitCode);
                            if (ncitDrug == null) {
                                LOGGER.error("The NCIT code cannot be found... Code: {}", ncitCode);
                            } else {
                                drug = new Drug();
                                drug.setDrugName(ncitDrug.getName());
                                drug.setSynonyms(ncitDrug.getSynonyms().stream().map(synonym -> synonym.getName()).collect(Collectors.toSet()));
                                drug.setNcitCode(ncitDrug.getCode());

                                if (drugName != null) {
                                    DrugUtils.updateDrugName(drug, drugName);
                                }
                            }
                        }
                        if (drug == null) {
                            drug = new Drug();
                            drug.setNcitCode(ncitCode);
                            drug.setDrugName(drugName);
                        }
                        if (drugUuid != null) {
                            drug.setUuid(drugUuid);
                        }
                        drugBo.save(drug);
                    }
                    drugs.add(drug);
                }

                Treatment treatment = new Treatment();
                treatment.setDrugs(drugs);
                treatment.setPriority(priorityCount);
                treatment.setApprovedIndications(approvedIndications);
                treatment.setEvidence(evidence);

                treatments.add(treatment);
                priorityCount++;
            }
            evidence.setTreatments(treatments);

            // highest level of evidence
            if (!drugObj.has("level") || drugObj.getString("level").trim().isEmpty()) {
                LOGGER.error("{} no level of evidence", spaceStrByNestLevel(nestLevel + 2));
                // TODO:
                //throw new RuntimeException("no level of evidence");
            } else {
                String level = drugObj.getString("level").trim();
                addDateToLastEditSetFromObject(lastEditDates, drugObj, "level");
//                addDateToLastReviewSetFromLong(lastReviewDates, drugObj, "level");

                LevelOfEvidence levelOfEvidence = LevelOfEvidence.getByLevel(level.toUpperCase());
                if (levelOfEvidence == null) {
                    LOGGER.error("{} wrong level of evidence: {}", spaceStrByNestLevel(nestLevel + 2), level);
                    // TODO:
                    //throw new RuntimeException("wrong level of evidence: "+level);
                    continue;
                } else if (LevelUtils.getAllowedCurationLevels().contains(levelOfEvidence)) {
                    LOGGER.info("{} Level: {}", spaceStrByNestLevel(nestLevel + 2), levelOfEvidence.getLevel());
                } else {
                    LOGGER.error("{} Level not allowed: {}", spaceStrByNestLevel(nestLevel + 2), levelOfEvidence.getLevel());
                    continue;
                }
                evidence.setLevelOfEvidence(levelOfEvidence);

                LevelOfEvidence fdaLevel;
                if (drugObj.has(FDA_LEVEL_KEY)) {
                    String fdaLevelStr = drugObj.getString(FDA_LEVEL_KEY);
                    fdaLevel = LevelOfEvidence.getByLevel(fdaLevelStr);
                    LOGGER.info("{} Manual FDA level: {}", spaceStrByNestLevel(nestLevel + 2), fdaLevel);
                } else {
                    fdaLevel = FdaAlterationUtils.convertToFdaLevel(evidence.getLevelOfEvidence());
                    if (fdaLevel != null) {
                        LOGGER.info("{} Default FDA level: {}", spaceStrByNestLevel(nestLevel + 2), fdaLevel);
                    }
                }
                if (fdaLevel != null && LevelUtils.getAllowedFdaLevels().contains(fdaLevel)) {
                    evidence.setFdaLevel(fdaLevel);
                }

                if (drugObj.has(SOLID_PROPAGATION_KEY)) {
                    String definedPropagation = drugObj.getString(SOLID_PROPAGATION_KEY);
                    LevelOfEvidence definedLevel = LevelOfEvidence.getByLevel(definedPropagation.toUpperCase());

                    // Validate level
                    if (definedLevel != null && LevelUtils.getAllowedPropagationLevels().contains(definedLevel)) {
                        evidence.setSolidPropagationLevel(definedLevel);
                    }
                    if (evidence.getSolidPropagationLevel() != null) {
                        LOGGER.info("{} Manual solid propagation level: {}", spaceStrByNestLevel(nestLevel + 2), evidence.getSolidPropagationLevel());
                    }
                } else {
                    evidence.setSolidPropagationLevel(LevelUtils.getDefaultPropagationLevelByTumorForm(evidence, TumorForm.SOLID));
                }


                if (drugObj.has(LIQUID_PROPAGATION_KEY)) {
                    String definedPropagation = drugObj.getString(LIQUID_PROPAGATION_KEY);
                    LevelOfEvidence definedLevel = LevelOfEvidence.getByLevel(definedPropagation.toUpperCase());

                    // Validate level
                    if (definedLevel != null && LevelUtils.getAllowedPropagationLevels().contains(definedLevel)) {
                        evidence.setLiquidPropagationLevel(definedLevel);
                    }
                    if (evidence.getLiquidPropagationLevel() != null) {
                        LOGGER.info("{} Manual liquid propagation level: {}", spaceStrByNestLevel(nestLevel + 2), evidence.getLiquidPropagationLevel());
                    }
                } else {
                    evidence.setLiquidPropagationLevel(LevelUtils.getDefaultPropagationLevelByTumorForm(evidence, TumorForm.LIQUID));
                }
            }

            if (drugObj.has("description") && !drugObj.getString("description").trim().isEmpty()) {
                String desc = drugObj.getString("description").trim();
                addDateToLastEditSetFromObject(lastEditDates, drugObj, "description");
                evidence.setDescription(desc);
                LOGGER.info("{} Has description.", spaceStrByNestLevel(nestLevel + 2));
                setDocuments(desc, evidence);
            }

            Date lastEdit = getMostRecentDate(lastEditDates);
            if (lastEdit != null) {
                LOGGER.info("{} Last update on: {}", spaceStrByNestLevel(nestLevel + 2), MainUtils.getTimeByDate(lastEdit));
            }
            evidence.setLastEdit(lastEdit);

            if (excludedCancerTypes != null) {
                evidence.setExcludedCancerTypes(new HashSet<>(excludedCancerTypes));
            }

            List<TumorType> drugRCT = getRelevantCancerTypesIfExistsFromJsonObject(drugObj, tumorTypes, excludedCancerTypes, evidence.getLevelOfEvidence());
            if (drugRCT.size() > 0) {
                evidence.setRelevantCancerTypes(new HashSet<>(drugRCT));
            } else if (relevantCancerTypes != null) {
                evidence.setRelevantCancerTypes(new HashSet<>(relevantCancerTypes));
            }

            evidenceBo.save(evidence);
        }
    }

    private Evidence parseImplication(Gene gene, Set<Alteration> alterations, List<TumorType> tumorTypes, List<TumorType> excludedCancerTypes, List<TumorType> relevantCancerTypes, JSONObject implication, String uuid, EvidenceType evidenceType, Integer nestLevel) throws Exception {
        if (evidenceType != null && implication != null &&
            ((implication.has("description") && !implication.getString("description").trim().isEmpty())
                || (implication.has("level") && !implication.getString("level").trim().isEmpty()))) {
            LOGGER.info("{} {}", spaceStrByNestLevel(nestLevel), evidenceType.name());
            Set<Date> lastEditDates = new HashSet<>();
            Set<Date> lastReviewDates = new HashSet<>();
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            Evidence evidence = new Evidence();

            evidence.setEvidenceType(evidenceType);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setUuid(uuid);
            evidence.setCancerTypes(new HashSet<>(tumorTypes));

            if (excludedCancerTypes != null) {
                evidence.setExcludedCancerTypes(new HashSet<>(excludedCancerTypes));
            }

            if (implication.has("level") && !implication.getString("level").trim().isEmpty()) {
                LevelOfEvidence level = LevelOfEvidence.getByLevel(implication.getString("level").trim());
                LOGGER.info("{} Level of the implication: {}", spaceStrByNestLevel(nestLevel + 1), level);
                evidence.setLevelOfEvidence(level);
                addDateToLastEditSetFromObject(lastEditDates, implication, "level");
            }

            List<TumorType> implicationRCT = getRelevantCancerTypesIfExistsFromJsonObject(implication, tumorTypes, excludedCancerTypes, evidence.getLevelOfEvidence());
            if (implicationRCT.size() > 0) {
                evidence.setRelevantCancerTypes(new HashSet<>(implicationRCT));
            } else if (relevantCancerTypes != null && relevantCancerTypes.size() > 0) {
                evidence.setRelevantCancerTypes(new HashSet<>(relevantCancerTypes));
            } else if (LevelOfEvidence.LEVEL_Dx1.equals(evidence.getLevelOfEvidence())) {
                evidence.setRelevantCancerTypes(TumorTypeUtils.getDxOneRelevantCancerTypes(new HashSet<>(tumorTypes)));
            }

            if (implication.has("description") && !implication.getString("description").trim().isEmpty()) {
                LOGGER.info("{} Has description.", spaceStrByNestLevel(nestLevel + 1));
                String desc = implication.getString("description").trim();
                evidence.setDescription(desc);
                addDateToLastEditSetFromObject(lastEditDates, implication, "description");
                setDocuments(desc, evidence);
            }

            Date lastEdit = getMostRecentDate(lastEditDates);
            evidence.setLastEdit(lastEdit);
            if (lastEdit != null) {
                LOGGER.info("{} Last update on: {}", spaceStrByNestLevel(nestLevel + 1), MainUtils.getTimeByDate(lastEdit));
            }

            evidenceBo.save(evidence);
            return evidence;
        }
        return null;
    }

    private String spaceStrByNestLevel(Integer nestLevel) {
        if (nestLevel == null || nestLevel < 1)
            nestLevel = 1;
        return StringUtils.repeat("    ", nestLevel - 1);
    }

    private void setDocuments(String str, Evidence evidence) {
        if (str == null) return;
        Set<Article> docs = new HashSet<>();
        ArticleBo articleBo = ApplicationContextSingleton.getArticleBo();

        Set<String> pmidToSearch = new HashSet<>();
        getPmidsFromText(evidence.getDescription()).forEach(pmid -> {
            Article doc = articleBo.findArticleByPmid(pmid);
            if (doc != null) {
                docs.add(doc);
            } else {
                pmidToSearch.add(pmid);
            }
        });

        if (!pmidToSearch.isEmpty()) {
            for (Article article : NcbiEUtils.readPubmedArticles(pmidToSearch)) {
                articleBo.save(article);
                docs.add(article);
            }
        }

        getAbstractFromText(evidence.getDescription()).stream().forEach(article -> {
            Article dbArticle = articleBo.findArticleByAbstract(article.getAbstractContent());
            if (dbArticle == null) {
                articleBo.save(article);
                docs.add(article);
            } else {
                docs.add(dbArticle);
            }
        });

        evidence.addArticles(docs);
    }

    private Date getLastEdit(JSONObject object, String key) {
        return object.has(key + LAST_EDIT_EXTENSION) ? getUpdateTime(object.get(key + LAST_EDIT_EXTENSION)) : null;
    }

    private String getUUID(JSONObject object, String key) {
        return object.has(key + UUID_EXTENSION) ? object.getString(key + UUID_EXTENSION) : "";
    }

    private void addDateToLastEditSetFromObject(Set<Date> set, JSONObject object, String key) throws JSONException {
        if (object.has(key + LAST_EDIT_EXTENSION)) {
            Date tmpDate = getUpdateTime(object.get(key + LAST_EDIT_EXTENSION));
            if (tmpDate != null) {
                set.add(tmpDate);
            }
        }
    }

    private Date getMostRecentDate(Set<Date> dates) {
        if (dates == null || dates.size() == 0)
            return null;
        return Collections.max(dates);
    }

    private ImmutablePair<EvidenceType, String> getEvidenceTypeAndKnownEffectFromDrugObj(JSONObject drugObj) {
        ImmutablePair<EvidenceType, String> emptyPair = new ImmutablePair<EvidenceType, String>(null, null);
        if (!drugObj.has("level") || drugObj.getString("level").trim().isEmpty()) {
            return emptyPair;
        }
        String level = drugObj.getString("level").trim();
        LevelOfEvidence levelOfEvidence = LevelOfEvidence.getByLevel(level.toUpperCase());

        EvidenceType evidenceType = EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY;
        String type = "";
        if (LevelOfEvidence.LEVEL_1.equals(levelOfEvidence) || LevelOfEvidence.LEVEL_2.equals(levelOfEvidence)) {
            evidenceType = EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY;
            type = "Sensitive";
        } else if (LevelOfEvidence.LEVEL_R1.equals(levelOfEvidence)) {
            evidenceType = EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE;
            type = "Resistant";
        } else if (LevelOfEvidence.LEVEL_3A.equals(levelOfEvidence) || LevelOfEvidence.LEVEL_4.equals(levelOfEvidence)) {
            evidenceType = EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY;
            type = "Sensitive";
        } else if (LevelOfEvidence.LEVEL_R2.equals(levelOfEvidence)) {
            evidenceType = EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE;
            type = "Resistant";
        } else {
            return emptyPair;
        }

        return new ImmutablePair<EvidenceType, String>(evidenceType, type);
    }

    private List<TumorType> getRelevantCancerTypes(List<TumorType> tumorTypes, List<TumorType> excludedTumorTypes, LevelOfEvidence level, List<TumorType> excludedRelevantCancerTypes) {
        RelevantTumorTypeDirection direction = level != null && LevelOfEvidence.LEVEL_Dx1.equals(level) ? RelevantTumorTypeDirection.UPWARD : RelevantTumorTypeDirection.DOWNWARD;

        Set<TumorType> queriedTumorTypes = tumorTypes.stream().map(tt -> {
                return TumorTypeUtils.findRelevantTumorTypes(TumorTypeUtils.getTumorTypeName(tt), StringUtils.isEmpty(tt.getSubtype()), direction, false);
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        Set<TumorType> queriedExcludedTumorTypes = excludedTumorTypes.stream().map(ett -> {
                return TumorTypeUtils.findRelevantTumorTypes(TumorTypeUtils.getTumorTypeName(ett), StringUtils.isEmpty(ett.getSubtype()), direction, false);
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        queriedTumorTypes.removeAll(queriedExcludedTumorTypes);
        queriedTumorTypes.removeAll(excludedRelevantCancerTypes);


        return new ArrayList<>(queriedTumorTypes);
    }

    private List<TumorType> getRelevantCancerTypesIfExistsFromJsonObject(JSONObject jsonObject, List<TumorType> tumorTypes, List<TumorType> excludedCancerTypes, LevelOfEvidence level) throws JSONException, Exception {
        List<TumorType> relevantCancerTypes = new ArrayList<>();
        if (jsonObject.has(EXCLUDED_RCTS_KEY)) {
            List<TumorType> excludedRCT = getTumorTypes(jsonObject.getJSONArray(EXCLUDED_RCTS_KEY));
            relevantCancerTypes = getRelevantCancerTypes(tumorTypes, excludedCancerTypes, level, excludedRCT);
        }
        return relevantCancerTypes;
    }
}
