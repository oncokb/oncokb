/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.quest;

import org.apache.commons.lang3.StringEscapeUtils;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.mskcc.oncotree.model.TumorType;
import org.springframework.stereotype.Controller;

import java.util.*;

/**
 * @author jgao
 */
@Controller
public final class VariantAnnotationXML {

    public static String annotate(Alteration alt, String tumorType) {
//        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();

        StringBuilder sb = new StringBuilder();

        Gene gene = alt.getGene();

//        Set<Gene> genes = new HashSet<Gene>();
//        if (gene.getEntrezGeneId() > 0) {
//            genes.add(gene);
//        } else {
        // fake gene... could be a fusion gene
//            Set<String> aliases = gene.getGeneAliases();
//            for (String alias : aliases) {
//                Gene g = geneBo.findGeneByHugoSymbol(alias);
//                if (g != null) {
//                    genes.add(g);
//                }
//            }
//        }

        Set<TumorType> relevantTumorTypes = new HashSet<TumorType>(TumorTypeUtils.getMappedOncoTreeTypesBySource(tumorType, "quest"));

        AlterationUtils.annotateAlteration(alt, alt.getAlteration());

        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        List<Alteration> alterations = alterationBo.findRelevantAlterations(alt, null);

        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        // find all drugs
        //List<Drug> drugs = evidenceBo.findDrugsByAlterations(alterations);

        // find tumor types
        Set<String> tumorTypes = new HashSet<>();

        if (alterations != null && alterations.size() > 0) {
            List<Object> tumorTypesEvidence = evidenceBo.findTumorTypesWithEvidencesForAlterations(alterations);
            for (Object evidence : tumorTypesEvidence) {
                if (evidence != null) {
                    Object[] evidences = (Object[]) evidence;
                    if (evidences.length > 0 && evidences[0] != null) {
                        tumorTypes.add((String) evidences[0]);
                    }
                }
            }
        }

//        sortTumorType(tumorTypes, tumorType);
        Set<ClinicalTrial> allTrails = new HashSet<ClinicalTrial>();
        Query query = new Query(alt);
        query.setTumorType(tumorType);
        // summary
        sb.append("<annotation_summary>");
        sb.append(SummaryUtils.fullSummary(gene, alterations.isEmpty() ? Collections.singletonList(alt) : alterations, query, relevantTumorTypes));
        sb.append("</annotation_summary>\n");

        // gene background
        List<Evidence> geneBgEvs = evidenceBo.findEvidencesByGene(Collections.singleton(gene), Collections.singleton(EvidenceType.GENE_BACKGROUND));
        if (!geneBgEvs.isEmpty()) {
            Evidence ev = geneBgEvs.get(0);
            sb.append("<gene_annotation>\n");
            sb.append("    <description>");
            sb.append(StringEscapeUtils.escapeXml(ev.getDescription()).trim());
            sb.append("</description>\n");
            exportRefereces(ev, sb, "    ");
            sb.append("</gene_annotation>\n");
        }

        if (alterations.isEmpty()) {
            sb.append("<!-- There is no information about the function of this variant in the MSKCC OncoKB. -->");
            return sb.toString();
        }

        List<Evidence> mutationEffectEbs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_EFFECT));
        for (Evidence ev : mutationEffectEbs) {
            sb.append("<variant_effect>\n");
            sb.append("    <effect>");
            if (ev != null) {
                sb.append(ev.getKnownEffect());
            }
            sb.append("</effect>\n");
            if (ev.getDescription() != null) {
                sb.append("    <description>");
                sb.append(StringEscapeUtils.escapeXml(ev.getDescription()).trim());
                sb.append("</description>\n");
            }
            if (ev != null) {
                exportRefereces(ev, sb, "    ");
            }

            sb.append("</variant_effect>\n");
        }

        for (String tt : tumorTypes) {
            TumorType oncoTreeType = TumorTypeUtils.getMappedOncoTreeTypesBySource(tt, "quest").get(0);
            boolean isRelevant = relevantTumorTypes.contains(oncoTreeType);

            StringBuilder sbTumorType = new StringBuilder();
            sbTumorType.append("<cancer_type type=\"").append(tt).append("\" relevant_to_patient_disease=\"").append(isRelevant ? "Yes" : "No").append("\">\n");
            int nEmp = sbTumorType.length();

            // find prevalence evidence blob
            Set<Evidence> prevalanceEbs = new HashSet<>(evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.PREVALENCE), Collections.singleton(oncoTreeType)));
            if (!prevalanceEbs.isEmpty()) {
                sbTumorType.append("    <prevalence>\n");
                sbTumorType.append("        <description>\n");
                for (Evidence ev : prevalanceEbs) {
                    String description = ev.getDescription();
                    if (description != null) {
                        sbTumorType.append("        ").append(StringEscapeUtils.escapeXml(description).trim()).append("\n");
                    }
                }

                sbTumorType.append("</description>\n");
                for (Evidence ev : prevalanceEbs) {
                    exportRefereces(ev, sbTumorType, "        ");
                }
                sbTumorType.append("    </prevalence>\n");
            }


            // find prognostic implication evidence blob
            Set<Evidence> prognosticEbs = new HashSet<Evidence>(evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.PROGNOSTIC_IMPLICATION), Collections.singleton(oncoTreeType)));
            if (!prognosticEbs.isEmpty()) {
                sbTumorType.append("    <prognostic_implications>\n");
                sbTumorType.append("        <description>\n");
                for (Evidence ev : prognosticEbs) {
                    String description = ev.getDescription();
                    if (description != null) {
                        sbTumorType.append("        ").append(StringEscapeUtils.escapeXml(description).trim()).append("\n");
                    }
                }
                sbTumorType.append("</description>\n");

                for (Evidence ev : prognosticEbs) {
                    exportRefereces(ev, sbTumorType, "        ");
                }
                sbTumorType.append("    </prognostic_implications>\n");
            }

            // STANDARD_THERAPEUTIC_IMPLICATIONS
            List<Evidence> stdImpEbsSensitivity = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY), Collections.singleton(oncoTreeType));
            List<Evidence> stdImpEbsResisitance = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE), Collections.singleton(oncoTreeType));

            //Remove level_0
            stdImpEbsSensitivity = filterLevelZeroEvidence(stdImpEbsSensitivity);

            //Remove level_R3
            stdImpEbsResisitance = filterResistanceEvidence(stdImpEbsResisitance);

            exportTherapeuticImplications(relevantTumorTypes, stdImpEbsSensitivity, stdImpEbsResisitance, "standard_therapeutic_implications", sbTumorType, "    ", isRelevant);

            // NCCN_GUIDELINES
            List<Evidence> nccnEvs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.NCCN_GUIDELINES), Collections.singleton(oncoTreeType));
            Set<NccnGuideline> nccnGuidelines = new LinkedHashSet<NccnGuideline>();
            for (Evidence ev : nccnEvs) {
                nccnGuidelines.addAll(ev.getNccnGuidelines());
            }

            for (NccnGuideline nccnGuideline : nccnGuidelines) {
                sbTumorType.append("    <nccn_guidelines>\n");
                sbTumorType.append("        <disease>");
                if (nccnGuideline.getDisease() != null) {
                    sbTumorType.append(nccnGuideline.getDisease());
                }
                sbTumorType.append("</disease>\n");
                sbTumorType.append("        <version>");
                if (nccnGuideline.getVersion() != null) {
                    sbTumorType.append(nccnGuideline.getVersion());
                }
                sbTumorType.append("</version>\n");
                sbTumorType.append("        <pages>");
                if (nccnGuideline.getPages() != null) {
                    sbTumorType.append(nccnGuideline.getPages());
                }
                sbTumorType.append("</pages>\n");
                sbTumorType.append("        <recommendation_category>");
                if (nccnGuideline.getCategory() != null) {
                    sbTumorType.append(nccnGuideline.getCategory());
                }
                sbTumorType.append("</recommendation_category>\n");
                sbTumorType.append("        <description>");
                if (nccnGuideline.getDescription() != null) {
                    sbTumorType.append(StringEscapeUtils.escapeXml(nccnGuideline.getDescription()));
                }
                sbTumorType.append("</description>\n");
                sbTumorType.append("    </nccn_guidelines>\n");
            }

            // INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS
            List<Evidence> invImpEbsSensitivity = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY), Collections.singleton(oncoTreeType));
            List<Evidence> invImpEbsResisitance = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE), Collections.singleton(oncoTreeType));

            //Remove level_R3
            invImpEbsResisitance = filterResistanceEvidence(invImpEbsResisitance);

            exportTherapeuticImplications(relevantTumorTypes, invImpEbsSensitivity, invImpEbsResisitance, "investigational_therapeutic_implications", sbTumorType, "    ", isRelevant);

            // CLINICAL_TRIAL
            {
//                Set<Drug> drugs = new HashSet<Drug>();
//                for (Evidence ev : stdImpEbsSensitivity) {
//                    for (Treatment treatment : ev.getTreatments()) {
//                        drugs.addAll(treatment.getDrugs());
//                    }
//                }
//                for (Evidence ev : invImpEbsSensitivity) {
//                    for (Treatment treatment : ev.getTreatments()) {
//                        drugs.addAll(treatment.getDrugs());
//                    }
//                }

                List<String> tumorTypesForTrials;
                if (isRelevant) { // if relevant to pateint disease, find trials that match the tumor type
                    tumorTypesForTrials = Collections.singletonList(tt);
                } else if (relevantTumorTypes.size() == 1) { // if no relevant disease, find trials that match the tumor type
                    tumorTypesForTrials = Collections.singletonList(tt);
                } else { // for irrelevant diseases, find trials that match the relavant tumor types
                    tumorTypesForTrials = null;
                }

                if (tumorTypesForTrials != null) {
                    List<Evidence> clinicalTrialEvidences = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.CLINICAL_TRIAL), Collections.singleton(oncoTreeType));
                    List<ClinicalTrial> clinicalTrials = new LinkedList<ClinicalTrial>();
                    for (Evidence ev : clinicalTrialEvidences) {
                        clinicalTrials.addAll(ev.getClinicalTrials());
                    }
                    exportClinicalTrials(clinicalTrials, sbTumorType, "    ");
                }
            }

            if (sbTumorType.length() > nEmp) {
                sbTumorType.append("</cancer_type>\n");
                sb.append(sbTumorType);
            }
        }

        return sb.toString();
    }

    private static List<Evidence> filterLevelZeroEvidence(List<Evidence> sensitivityEvidences) {
        if (sensitivityEvidences != null) {
            Iterator<Evidence> i = sensitivityEvidences.iterator();
            while (i.hasNext()) {
                Evidence sensitivityEvidence = i.next(); // must be called before you can call i.remove()
                if (sensitivityEvidence.getLevelOfEvidence() != null && sensitivityEvidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_0)) {
                    i.remove();
                }
            }
        }
        return sensitivityEvidences;
    }

    private static List<Evidence> filterResistanceEvidence(List<Evidence> resistanceEvidences) {
        if (resistanceEvidences != null) {
            Iterator<Evidence> i = resistanceEvidences.iterator();
            while (i.hasNext()) {
                Evidence resistanceEvidence = i.next(); // must be called before you can call i.remove()
                if (resistanceEvidence.getLevelOfEvidence() != null && resistanceEvidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_R3)) {
                    i.remove();
                }
            }
        }
        return resistanceEvidences;
    }

    private static void exportTherapeuticImplications(Set<TumorType> relevantTumorTypes, List<Evidence> evSensitivity, List<Evidence> evResisitance, String tagTherapeuticImp, StringBuilder sb, String indent, Boolean sameIndication) {
        if (evSensitivity.isEmpty() && evResisitance.isEmpty()) {
            return;
        }

        sb.append(indent).append("<").append(tagTherapeuticImp).append(">\n");

        List<List<Evidence>> evsSensitivity = seperateGeneralAndSpecificEvidencesForTherapeuticImplications(evSensitivity);
        List<List<Evidence>> evsResisitance = seperateGeneralAndSpecificEvidencesForTherapeuticImplications(evResisitance);

        // general evs
        if (!evsSensitivity.get(0).isEmpty() || !evsResisitance.get(0).isEmpty()) {
            sb.append(indent).append("    <general_statement>\n");
            for (Evidence ev : evsSensitivity.get(0)) {
                sb.append(indent).append("        <sensitivity>\n");
                exportTherapeuticImplications(null, ev, sb, indent + "            ");
                sb.append(indent).append("        </sensitivity>\n");
            }
            for (Evidence ev : evsResisitance.get(0)) {
                sb.append(indent).append("        <resistance>\n");
                exportTherapeuticImplications(null, ev, sb, indent + "            ");
                sb.append(indent).append("        </resistance>\n");
            }
            sb.append(indent).append("    </general_statement>\n");
        }

        // specific evs
        //boolean isInvestigational = tagTherapeuticImp.equals("investigational_therapeutic_implications");
        if (!evsSensitivity.get(1).isEmpty() || !evsResisitance.get(1).isEmpty()) {
            for (Evidence ev : evsSensitivity.get(1)) {
                if(sameIndication || ev.getPropagation() == null || !ev.getPropagation().equals("NO")) {
                    sb.append(indent).append("    <sensitive_to>\n");
                    exportTherapeuticImplications(relevantTumorTypes, ev, sb, indent + "        ");
                    sb.append(indent).append("    </sensitive_to>\n");
                }
            }
            for (Evidence ev : evsResisitance.get(1)) {
                if(sameIndication || ev.getPropagation() == null || !ev.getPropagation().equals("NO")) {
                    sb.append(indent).append("    <resistant_to>\n");
                    exportTherapeuticImplications(relevantTumorTypes, ev, sb, indent + "        ");
                    sb.append(indent).append("    </resistant_to>\n");
                }
            }
        }

        sb.append(indent).append("</").append(tagTherapeuticImp).append(">\n");
    }

    private static List<List<Evidence>> seperateGeneralAndSpecificEvidencesForTherapeuticImplications(List<Evidence> evs) {
        List<List<Evidence>> ret = new ArrayList<List<Evidence>>();
        ret.add(new ArrayList<Evidence>());
        ret.add(new ArrayList<Evidence>());

        for (Evidence ev : evs) {
            if (ev.getTreatments().isEmpty()) {
                ret.get(0).add(ev);
            } else {
                ret.get(1).add(ev);
            }
        }

        return ret;
    }

    private static void exportClinicalTrials(List<ClinicalTrial> clinicalTrials, StringBuilder sb, String indent) {
        Collections.sort(clinicalTrials, new Comparator<ClinicalTrial>() {
            public int compare(ClinicalTrial trial1, ClinicalTrial trial2) {
                return phase2int(trial2.getPhase()) - phase2int(trial1.getPhase());
            }

            private int phase2int(String phase) {
                if (phase.matches("Phase [0-4]")) {
                    return 2 * Integer.parseInt(phase.substring(6));
                }
                if (phase.matches("Phase [0-4]/Phase [0-4]")) {
                    return Integer.parseInt(phase.substring(6, 7)) + Integer.parseInt(phase.substring(14));
                }
                return -1;
            }
        });

        for (ClinicalTrial clinicalTrial : clinicalTrials) {
            if (filterClinicalTrials(clinicalTrial)) {
                exportClinicalTrial(clinicalTrial, sb, indent);
            }
        }
    }

    private static boolean filterClinicalTrials(ClinicalTrial clinicalTrial) {
//        if (!clinicalTrial.isInUSA()) {
//            return false;
//        }
//
//        if (!clinicalTrial.isOpen()) {
//            return false;
//        }
//
//        String phase = clinicalTrial.getPhase().toLowerCase();
//        return phase.contains("phase 1") ||
//                phase.contains("phase 2") ||
//                phase.contains("phase 3") ||
//                phase.contains("phase 4") ||
//                phase.contains("phase 5");

        return true;
    }

    private static void exportClinicalTrial(ClinicalTrial trial, StringBuilder sb, String indent) {
        sb.append(indent).append("<clinical_trial>\n");

        sb.append(indent).append("    <trial_id>");
        if (trial.getNctId() != null) {
            sb.append(trial.getNctId());
        }
        sb.append("</trial_id>\n");

        sb.append(indent).append("    <title>");
        if (trial.getTitle() != null) {
            sb.append(StringEscapeUtils.escapeXml(trial.getTitle()));
        }
        sb.append("</title>\n");

        sb.append(indent).append("    <purpose>");
        if (trial.getPurpose() != null) {
            sb.append(StringEscapeUtils.escapeXml(trial.getPurpose()));
        }
        sb.append("</purpose>\n");

        sb.append(indent).append("    <recruiting_status>");
        if (trial.getRecruitingStatus() != null) {
            sb.append(StringEscapeUtils.escapeXml(trial.getRecruitingStatus()));
        }
        sb.append("</recruiting_status>\n");

        sb.append(indent).append("    <eligibility_criteria>");
        if (trial.getEligibilityCriteria() != null) {
            sb.append(StringEscapeUtils.escapeXml(trial.getEligibilityCriteria()));
        }
        sb.append("</eligibility_criteria>\n");

        sb.append(indent).append("    <phase>");
        if (trial.getPhase() != null) {
            sb.append(StringEscapeUtils.escapeXml(trial.getPhase()));
        }
        sb.append("</phase>\n");

        for (Drug drug : trial.getDrugs()) {
            sb.append(indent).append("    <intervention>");
            sb.append(StringEscapeUtils.escapeXml(drug.getDrugName()));
            sb.append("</intervention>\n");
        }

        sb.append(indent).append("</clinical_trial>");
    }

    private static void exportTherapeuticImplications(Set<TumorType> relevantTumorTypes, Evidence evidence, StringBuilder sb, String indent) {
        LevelOfEvidence levelOfEvidence = evidence.getLevelOfEvidence();

        for (Treatment treatment : evidence.getTreatments()) {
            sb.append(indent).append("<treatment>\n");
            exportTreatment(treatment, sb, indent + "    ", levelOfEvidence);
            sb.append(indent).append("</treatment>\n");
        }

        if (levelOfEvidence != null) {
            levelOfEvidence = LevelUtils.updateOrKeepLevelByIndication(levelOfEvidence, evidence.getPropagation(), relevantTumorTypes.contains(evidence.getOncoTreeType()));
            sb.append(indent).append("<level_of_evidence_for_patient_indication>\n");
            sb.append(indent).append("    <level>");
            sb.append(levelOfEvidence.getLevel());
            sb.append("</level>\n");
            sb.append(indent).append("    <description>");
            sb.append(StringEscapeUtils.escapeXml(levelOfEvidence.getDescription()).trim());
            sb.append("</description>\n");
            if (levelOfEvidence == LevelOfEvidence.LEVEL_1 ||
                levelOfEvidence == LevelOfEvidence.LEVEL_2A ||
                levelOfEvidence == LevelOfEvidence.LEVEL_2B) {
                sb.append(indent).append("<approved_indication>");
                sb.append("</approved_indication>\n");
            }
            sb.append(indent).append("</level_of_evidence_for_patient_indication>\n");
        }

        sb.append(indent).append("<description>");
        if (evidence.getDescription() != null) {
            sb.append(StringEscapeUtils.escapeXml(evidence.getDescription()).trim());
        }
        sb.append("</description>\n");

        exportRefereces(evidence, sb, indent);
    }

    private static void exportTreatment(Treatment treatment, StringBuilder sb, String indent, LevelOfEvidence levelOfEvidence) {
        Set<Drug> drugs = treatment.getDrugs();
        for (Drug drug : drugs) {
            sb.append(indent).append("<drug>\n");

            sb.append(indent).append("    <name>");
            String name = drug.getDrugName();
            if (name != null) {
                sb.append(StringEscapeUtils.escapeXml(name));
            }
            sb.append("</name>\n");

            Set<String> synonyms = drug.getSynonyms();
            for (String synonym : synonyms) {
                sb.append(indent).append("    <synonym>");
                sb.append(synonym);
                sb.append("</synonym>\n");
            }

            sb.append(indent).append("    <fda_approved>");

            //FDA approved info based on evidence level. Temporaty solution. The info should be pulled up from database
            //by using PI-helper
//            Boolean fdaApproved = drug.isFdaApproved();
//            if (fdaApproved!=null) {
//                sb.append(fdaApproved ? "Yes" : "No");
//            }

            Boolean fdaApproved = levelOfEvidence == LevelOfEvidence.LEVEL_1 || levelOfEvidence == LevelOfEvidence.LEVEL_2A;
            sb.append(fdaApproved ? "Yes" : "No");

            sb.append("</fda_approved>\n");

//            sb.append(indent).append("    <description>");
//            String desc = drug.getDescription();
//            if (desc != null) {
//                sb.append(StringEscapeUtils.escapeXml(desc));
//            }
//            sb.append("</description>\n");

            sb.append(indent).append("</drug>\n");

        }
    }

    private static void exportRefereces(Evidence evidence, StringBuilder sb, String indent) {
        Set<Article> articles = evidence.getArticles();
        for (Article article : articles) {
            sb.append(indent).append("<reference>\n");
            sb.append(indent).append("    <pmid>");
            String pmid = article.getPmid();
            if (pmid != null) {
                sb.append(pmid);
            }
            sb.append("</pmid>\n");

            sb.append(indent).append("    <authors>");
            if (article.getAuthors() != null) {
                sb.append(article.getAuthors());
            }
            sb.append("</authors>\n");

            sb.append(indent).append("    <title>");
            if (article.getTitle() != null) {
                sb.append(article.getTitle());
            }
            sb.append("</title>\n");

            sb.append(indent).append("    <journal>");
            if (article.getJournal() != null) {
                sb.append(article.getJournal());
            }
            sb.append("</journal>\n");

            sb.append(indent).append("    <pub_date>");
            if (article.getPubDate() != null) {
                sb.append(article.getPubDate());
            }
            sb.append("</pub_date>\n");

            sb.append(indent).append("    <volume>");
            if (article.getVolume() != null) {
                sb.append(article.getVolume());
            }
            sb.append("</volume>\n");

            sb.append(indent).append("    <issue>");
            if (article.getIssue() != null) {
                sb.append(article.getIssue());
            }
            sb.append("</issue>\n");

            sb.append(indent).append("    <pages>");
            if (article.getPages() != null) {
                sb.append(article.getPages());
            }
            sb.append("</pages>\n");

            sb.append(indent).append("    <elocation_id>");
            if (article.getElocationId() != null) {
                sb.append(article.getElocationId());
            }
            sb.append("</elocation_id>\n");

            sb.append(indent).append("</reference>\n");
        }
    }

    /**
     *
     * @param tumorTypes
     * @param patientTumorType
     * @return the number of relevant tumor types
     */
//    private static void sortTumorType(List<String> tumorTypes, String patientTumorType) {
//        List<TumorType> relevantTumorTypes = TumorTypeUtils.getTumorTypes(patientTumorType, "quest");
////        relevantTumorTypes.retainAll(tumorTypes); // only tumor type with evidence
//        tumorTypes.removeAll(relevantTumorTypes); // other tumor types
//        tumorTypes.addAll(0, relevantTumorTypes);
//    }

//    private static Set<Drug> allTargetedDrugs = new HashSet<Drug>();
//    static {
//        TreatmentBo treatmentBo = ApplicationContextSingleton.getTreatmentBo();
//        for (Treatment treatment : treatmentBo.findAll()) {
//            allTargetedDrugs.addAll(treatment.getDrugs());
//        }
//    }
}
