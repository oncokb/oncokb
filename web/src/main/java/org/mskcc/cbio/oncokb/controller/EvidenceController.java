/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import com.mysql.jdbc.StringUtils;
import io.swagger.annotations.ApiParam;
import org.json.JSONArray;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

/**
 * @author jgao
 */
@Controller
public class EvidenceController {
    @RequestMapping(value = "/legacy-api/evidence.json", method = RequestMethod.GET)
    public
    @ResponseBody
    List<List<Evidence>> getEvidence(
        HttpMethod method,
        @RequestParam(value = "entrezGeneId", required = false) String entrezGeneId,
        @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol,
        @RequestParam(value = "alteration", required = false) String alteration,
        @RequestParam(value = "tumorType", required = false) String tumorType,
        @RequestParam(value = "evidenceType", required = false) String evidenceType,
        @RequestParam(value = "consequence", required = false) String consequence,
        @RequestParam(value = "proteinStart", required = false) String proteinStart,
        @RequestParam(value = "proteinEnd", required = false) String proteinEnd,
        @RequestParam(value = "geneStatus", required = false) String geneStatus,
        @RequestParam(value = "source", required = false) String source,
        @RequestParam(value = "levels", required = false) String levels,
        @RequestParam(value = "highestLevelOnly", required = false) Boolean highestLevelOnly) {

        List<List<Evidence>> evidences = new ArrayList<>();

        Map<String, Object> requestQueries = MainUtils.GetRequestQueries(entrezGeneId, hugoSymbol, alteration,
            tumorType, evidenceType, consequence, proteinStart, proteinEnd, geneStatus, source, levels);

        if (requestQueries == null) {
            return new ArrayList<>();
        }

        List<EvidenceQueryRes> evidenceQueries = EvidenceUtils.processRequest(
            (List<Query>) requestQueries.get("queries"),
            new HashSet<>((List<EvidenceType>) requestQueries.get("evidenceTypes")),
            geneStatus, source, new HashSet<>((List<LevelOfEvidence>) requestQueries.get("levels")), highestLevelOnly);

        if (evidenceQueries != null) {
            for (EvidenceQueryRes query : evidenceQueries) {
                evidences.add(query.getEvidences());
            }
        }

        return evidences;
    }

    @RequestMapping(value = "/legacy-api/evidence.json", method = RequestMethod.POST)
    public
    @ResponseBody
    List<EvidenceQueryRes> getEvidence(
        @RequestBody EvidenceQueries body) {

        List<EvidenceQueryRes> result = new ArrayList<>();
        if (body.getQueries().size() > 0) {
            List<Query> requestQueries = body.getQueries();
            Set<EvidenceType> evidenceTypes = new HashSet<>();

            if (body.getEvidenceTypes() != null) {
                for (String type : body.getEvidenceTypes().split("\\s*,\\s*")) {
                    EvidenceType et = EvidenceType.valueOf(type);
                    evidenceTypes.add(et);
                }
            } else if (body.getEvidenceTypes().isEmpty()) {
                // If the evidenceTypes has been defined but is empty, no result should be returned.
                return result;
            } else {
                evidenceTypes.add(EvidenceType.GENE_SUMMARY);
                evidenceTypes.add(EvidenceType.GENE_BACKGROUND);
            }

            result = EvidenceUtils.processRequest(requestQueries, evidenceTypes, null, body.getSource(),
                body.getLevels(), body.getHighestLevelOnly());
        }

        return result;
    }

    @RequestMapping(value = "/legacy-api/evidences/update/{uuid}", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity updateEvidence(@ApiParam(value = "uuid", required = true) @PathVariable("uuid") String uuid,
                                  @RequestBody Evidence queryEvidence) throws ParserConfigurationException {

        List<Evidence> updatedEvidences = updateEvidenceBasedOnUuid(uuid, queryEvidence);

        if (updatedEvidences != null) {
            updateCacheBasedOnEvidences(new HashSet<>(updatedEvidences));
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/legacy-api/evidences/update", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity updateEvidence(@RequestBody Map<String, Evidence> queryEvidences) throws ParserConfigurationException {
        Set<Evidence> updatedEvidenceSet = new HashSet<>();

        for (Map.Entry<String, Evidence> entry : queryEvidences.entrySet()) {

            List<Evidence> updatedEvidences = updateEvidenceBasedOnUuid(entry.getKey(), entry.getValue());

            if (updatedEvidences != null) {
                updatedEvidenceSet.addAll(updatedEvidences);
            }
        }
        updateCacheBasedOnEvidences(updatedEvidenceSet);

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/legacy-api/evidences/delete", method = RequestMethod.POST)
    public
    @ResponseBody
    String deleteEvidences(@RequestBody List<String> uuids) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        if (uuids != null) {
            List<Evidence> evidences = evidenceBo.findEvidenceByUUIDs(uuids);
            Set<Gene> genes = deleteEvidencesAndAlts(evidences);
            updateCacheBasedOnGenes(genes);
        }
        return "";
    }

    @RequestMapping(value = "/legacy-api/evidences/delete/{uuid}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    String deleteEvidence(@ApiParam(value = "uuid", required = true) @PathVariable("uuid") String uuid) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> evidences = evidenceBo.findEvidenceByUUIDs(Collections.singletonList(uuid));

        Set<Gene> genes = deleteEvidencesAndAlts(evidences);
        updateCacheBasedOnGenes(genes);
        return "";
    }

    @RequestMapping(value = "/legacy-api/vus/update/{hugoSymbol}", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity updateVUS(@ApiParam(value = "hugoSymbol", required = true) @PathVariable("hugoSymbol") String hugoSymbol,
                             @RequestBody String vus) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (hugoSymbol != null && vus != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            if (gene != null) {
                List<Evidence> evidences = EvidenceUtils.getEvidence(
                    new ArrayList<>(AlterationUtils.getAllAlterations(gene)),
                    Collections.singleton(EvidenceType.VUS), null);
                deleteEvidencesAndAlts(evidences);
                DriveAnnotationParser.parseVUS(gene, new JSONArray(vus));
                updateCacheBasedOnGenes(Collections.singleton(gene));
                status = HttpStatus.OK;
            }
        }

        return new ResponseEntity(status);
    }

    private Set<Gene> deleteEvidencesAndAlts(List<Evidence> evidences) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Alteration> alts = new ArrayList<>();
        List<Alteration> removedAlts = new ArrayList<>();

        // The sample solution for now is updating all gene related evidences and alterations.
        Set<Gene> genes = new HashSet<>();

        for (Evidence evidence : evidences) {
            alts.addAll(evidence.getAlterations());
            genes.add(evidence.getGene());
        }
        evidenceBo.deleteAll(evidences);

        for (Alteration alt : alts) {
            List<Evidence> altEvidences = evidenceBo.findEvidencesByAlteration(Collections.singletonList(alt));
            if (altEvidences == null && altEvidences.isEmpty()) {
                removedAlts.add(alt);
            }
        }
        ApplicationContextSingleton.getAlterationBo().deleteAll(removedAlts);

        return genes;
    }
    private String specialTypeCheck(Evidence queryEvidence) {
        if(queryEvidence.getAlterations() != null)return  "MUTATION_NAME_CHANGE";
        if(queryEvidence.getCancerType() != null)return "TUMOR_NAME_CHANGE";
        if(queryEvidence.getTreatments() != null)return "TREATMENT_NAME_CHANGE";
        return "";
    }
    private Boolean isEmptyEvidence(Evidence queryEvidence) {
        EvidenceType evidenceType = queryEvidence.getEvidenceType();
        String knownEffect = queryEvidence.getKnownEffect();
        String description = queryEvidence.getDescription();
        Set<Treatment> treatments = queryEvidence.getTreatments();
        Set<NccnGuideline> nccnGuidelines = queryEvidence.getNccnGuidelines();
        Set<ClinicalTrial> clinicalTrials = queryEvidence.getClinicalTrials();
        if(description != null) {
            description = description.trim();
        }
        Boolean isEmpty = false;
        if(evidenceType.equals(EvidenceType.ONCOGENIC) || evidenceType.equals(EvidenceType.MUTATION_EFFECT)) {
            if(StringUtils.isNullOrEmpty(knownEffect) && StringUtils.isNullOrEmpty(description)) isEmpty = true;
        } else if(evidenceType.equals(EvidenceType.NCCN_GUIDELINES)) {
            Boolean validNccn = false;
            for(NccnGuideline nccn : nccnGuidelines) {
                if(!nccn.isEmpty()) {
                    validNccn = true;
                }
            }
            isEmpty = !validNccn;
        } else if(evidenceType.equals(EvidenceType.CLINICAL_TRIAL)) {
           if(clinicalTrials == null || clinicalTrials.isEmpty()) isEmpty = true;
        } else if(MainUtils.getTreatmentEvidenceTypes().contains(evidenceType)) {
            if(treatments == null && StringUtils.isNullOrEmpty(description)) isEmpty = true;
        } else if(StringUtils.isNullOrEmpty(description)) {
            isEmpty = true;
        }
        return isEmpty;
    }
    private List<Evidence> updateEvidenceBasedOnUuid(String uuid, Evidence queryEvidence) throws ParserConfigurationException {
        EvidenceUtils.annotateEvidence(queryEvidence);
        Gene gene = queryEvidence.getGene();
        Set<Alteration> alterations = queryEvidence.getAlterations();
        String subType = queryEvidence.getSubtype();
        String cancerType = queryEvidence.getCancerType();
        String knownEffect = queryEvidence.getKnownEffect();
        LevelOfEvidence level = queryEvidence.getLevelOfEvidence();
        String description = queryEvidence.getDescription();
        String additionalInfo = queryEvidence.getAdditionalInfo();
        Date lastEdit = queryEvidence.getLastEdit();
        Set<Treatment> treatments = queryEvidence.getTreatments();
        Set<Article> articles = queryEvidence.getArticles();
        Set<NccnGuideline> nccnGuidelines = queryEvidence.getNccnGuidelines();
        Set<ClinicalTrial> clinicalTrials = queryEvidence.getClinicalTrials();
        String propagation = queryEvidence.getPropagation();

        // if the gene does not exist, return null
        if (gene == null) {
            return new ArrayList<>();
        }

        List<String> cancerTypes = new ArrayList<>();
        List<String> subTypes = new ArrayList<>();
        Boolean isCancerEvidence = true;
        if(cancerType == null) {
            isCancerEvidence = false;
        } else {
            cancerTypes = Arrays.asList(cancerType.split(","));
            if(subType != null) {
                subTypes = Arrays.asList(subType.split(","));
                for(int i = 0;i < subTypes.size();i++) {
                    if(subTypes.get(i).equals("null")) {
                        subTypes.set(i, null);
                    }
                }
            } else {
                for(int i = 0;i < cancerTypes.size();i++) {
                    subTypes.add(null);
                }
            }
        }
        // if cancerTypes and subTypes are not the same length, return null
        if(cancerTypes.size() != subTypes.size()) {
            return new ArrayList<>();
        }
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> evidences = evidenceBo.findEvidenceByUUIDs(Collections.singletonList(uuid));

        EvidenceType evidenceType = queryEvidence.getEvidenceType();
        // Three special evidences update, which are mutation name change, tumor name change and treatment name change
        if(evidenceType == null) {
           // If evidences not exist, return empty list
           if(evidences.isEmpty())return evidences;
           String specialChangeType = specialTypeCheck(queryEvidence);
           if(specialChangeType.equals("MUTATION_NAME_CHANGE")) {
               Evidence oldEvidence = new Evidence(evidences.get(0), evidences.get(0).getId());
               if(oldEvidence.getEvidenceType().equals(EvidenceType.ONCOGENIC)) {
                   evidenceBo.deleteAll(evidences);
                   evidences.clear();
                   for(Alteration alteration : alterations) {
                       Evidence tempEvidence = new Evidence(oldEvidence, null);
                       tempEvidence.setAlterations(Collections.singleton(alteration));
                       evidenceBo.save(tempEvidence);
                       evidences.add(tempEvidence);
                   }
               } else {
                   for(Evidence evidence : evidences) {
                       evidence.setAlterations(alterations);
                       evidenceBo.update(evidence);
                   }
               }
           } else if(specialChangeType.equals("TUMOR_NAME_CHANGE")) {
               Evidence oldEvidence = new Evidence(evidences.get(0), evidences.get(0).getId());
               evidenceBo.deleteAll(evidences);
               evidences.removeAll(evidences);
               for(int i = 0;i < cancerTypes.size();i++) {
                   Evidence tempEvidence = new Evidence(oldEvidence, null);
                   tempEvidence.setCancerType(cancerTypes.get(i));
                   tempEvidence.setSubtype(subTypes.get(i));
                   evidenceBo.save(tempEvidence);
                   evidences.add(tempEvidence);
               }
           } else if(specialChangeType.equals("TREATMENT_NAME_CHANGE")) {
               for(Evidence evidence : evidences) {
                    evidence.setTreatments(treatments);
                    evidenceBo.update(evidence);
                }
           }
           return evidences;
        }
        // if passed in evidence is empty, we delete them in the database and return empty list
        if(isEmptyEvidence(queryEvidence)) {
            evidenceBo.deleteAll(evidences);
            return new ArrayList<Evidence>();
        }
        // common cases for evidence update
        // Use controlled vocabulary to update oncogenic knowneffect
        if(evidenceType.equals(EvidenceType.ONCOGENIC)) {
            Oncogenicity oncogenicity = DriveAnnotationParser.getOncogenicityByString(knownEffect);
            if (oncogenicity != null) {
                knownEffect = oncogenicity.getOncogenic();
            }
        }
        // save newly added evidence
        if (evidences.isEmpty()) {
            if(evidenceType.equals(EvidenceType.ONCOGENIC) && alterations.size() > 1) {
                // save duplicated evidence record for string alteration oncogenic
                for(Alteration alteration : alterations) {
                    Evidence evidence = new Evidence(uuid, evidenceType, null, null, null, gene, Collections.singleton(alteration), description, additionalInfo, treatments, knownEffect, lastEdit, level, propagation, articles, nccnGuidelines, clinicalTrials);
                    evidences.add(evidence);
                    evidenceBo.save(evidence);
                }
            } else if(!isCancerEvidence) {
                Evidence evidence = new Evidence(uuid, evidenceType, null, null, null, gene, alterations, description, additionalInfo, treatments, knownEffect, lastEdit, level, propagation, articles, nccnGuidelines, clinicalTrials);
                evidenceBo.save(evidence);
                evidences.add(evidence);
            } else {
                for(int i = 0;i < cancerTypes.size();i++) {
                    Evidence evidence = new Evidence(uuid, evidenceType, cancerTypes.get(i), subTypes.get(i), null, gene, alterations, description, additionalInfo, treatments, knownEffect, lastEdit, level, propagation, articles, nccnGuidelines, clinicalTrials);
                    evidences.add(evidence);
                    evidenceBo.save(evidence);
                }
            }
        } else if(!isCancerEvidence){
            // For the evidences which tumor type infomation is not involved, update it directly
            for (Evidence evidence : evidences) {
                if(evidence.getAlterations() == null || evidence.getAlterations().isEmpty()) {
                    evidence.setAlterations(alterations);
                }
                evidence.setEvidenceType(evidenceType);
                evidence.setSubtype(subType);
                evidence.setCancerType(cancerType);
                evidence.setKnownEffect(knownEffect);
                evidence.setLevelOfEvidence(level);
                evidence.setDescription(description);
                evidence.setAdditionalInfo(additionalInfo);
                evidence.setLastEdit(lastEdit);
                evidence.setTreatments(treatments);
                evidence.setArticles(articles);
                evidence.setNccnGuidelines(nccnGuidelines);
                evidence.setClinicalTrials(clinicalTrials);
                evidence.setPropagation(propagation);
                evidenceBo.update(evidence);
            }
        } else {
            // remove all old evidences
            evidenceBo.deleteAll(evidences);
            evidences.removeAll(evidences);
            // insert cancer type information and save it
            for(int i = 0;i < cancerTypes.size();i++) {
                // create a new evidence based on input passed in, and gene and alterations information from the current evidences
                Evidence evidence = new Evidence(uuid, evidenceType, cancerTypes.get(i), subTypes.get(i), null, gene, alterations, description, additionalInfo, treatments, knownEffect, lastEdit, level, propagation, articles, nccnGuidelines, clinicalTrials);
                evidenceBo.save(evidence);
                evidences.add(evidence);
            }
        }
        return evidences;
    }

    private void updateCacheBasedOnEvidences(Set<Evidence> evidences) {
        // The sample solution for now is updating all gene related evidences.
        Set<Gene> genes = new HashSet<>();
        for (Evidence evidence : evidences) {
            genes.add(evidence.getGene());
        }
        for (Gene gene : genes) {
            CacheUtils.updateGene(gene.getEntrezGeneId());
        }
    }

    private void updateCacheBasedOnGenes(Set<Gene> genes) {
        // The sample solution for now is updating all gene related evidences.
        for (Gene gene : genes) {
            CacheUtils.updateGene(gene.getEntrezGeneId());
        }
    }
}
