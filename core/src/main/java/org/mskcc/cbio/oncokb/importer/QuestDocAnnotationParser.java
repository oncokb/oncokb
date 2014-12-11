
package org.mskcc.cbio.oncokb.importer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.ArticleBo;
import org.mskcc.cbio.oncokb.bo.ClinicalTrialBo;
import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.NccnGuidelineBo;
import org.mskcc.cbio.oncokb.bo.TreatmentBo;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Article;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.NccnGuideline;
import org.mskcc.cbio.oncokb.model.Treatment;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.mskcc.cbio.oncokb.util.GeneAnnotatorMyGeneInfo2;
import org.mskcc.cbio.oncokb.util.NcbiEUtils;

/**
 *
 * @author jgao
 */
public final class QuestDocAnnotationParser {
    private static final String GENE_P = "Gene: ?(.+)";
    private static final String SUMMARY_P = "Summary:? *";
    private static final String GENE_BACKGROUND_P = "Background:? *";
        
    private static final String MUTATION_P = "Mutations?: ?(.+)";
    private static final String MUTATION_ONCOGENIC_P = "Oncogenic?: ?([^ ]+) *";
    private static final String MUTATION_EFFECT_P = "Mutation effect: ?([^\\(]+)(\\(PMIDs?:.+\\))?";
    private static final String MUTATION_EFFECT_DESCRIPTION_P = "^Description of mutation effect:? *";
    
    private static final String TUMOR_TYPE_P = "Tumor type: ?(.+)";
    
    private static final String PREVALENCE_P = "Prevalence:? *";
    
    private static final String PROGNOSTIC_IMPLICATIONS_P = "Prognostic implications:? *";
    
    private static final String STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P = "Standard therapeutic implications for drug sensitivity:? ?";
    private static final String STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P = "Standard therapeutic implications for drug resistance:? ?";
    private static final String INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P = "Investigational therapeutic implications for drug sensitivity:?";
    private static final String INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P = "Investigational therapeutic implications for drug resistance:?";
    private static final String SENSITIVE_TO_P = "Sensitive to: ?(.+)";
    private static final String APPROVED_INDICATION_P = "Approved indications: ?(.*)";
    private static final String RESISTANT_TO_P = "Resistant to: ?(.+)";
    private static final String HIGHEST_LEVEL_OF_EVIDENCE = "Highest level of evidence: ?(.+)";
    private static final String DESCRIPTION_OF_EVIDENCE_P = "Description of evidence:? ?(.*)";
    
    
    private static final String NCCN_GUIDELINES_P = "NCCN guidelines";
    private static final String NCCN_DISEASE_P = "Disease: ?(.+)";
    private static final String NCCN_VERSION_P = "Version: ?(.+)";
    private static final String NCCN_PAGES_P = "Pages: ?(.+)";
    private static final String NCCN_PAGES_RECOMMENDATION_CATEGORY = "Recommendation category: ?(.+)";
    private static final String NCCN_EVIDENCE_P = "Description of evidence:? ?";
    
    private static final String ONGOING_CLINICAL_TRIALS_P = "Ongoing clinical trials:?";
    private static final String CLINICAL_TRIALS_P = "(NCT[0-9]+)";
    
    private static final String INVESTIGATIONAL_INTERACTING_GENE_ALTERATIONS_P = "Interacting gene alterations";
    
    private static final String DO_NOT_IMPORT = "DO NOT IMPORT";
    
    private static final String[] CANCER_HEADERS_P = new String[] {
        PREVALENCE_P,
        PROGNOSTIC_IMPLICATIONS_P,
        STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P,
        STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P,
        NCCN_GUIDELINES_P,
        INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P,
        INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P,
        ONGOING_CLINICAL_TRIALS_P,
        INVESTIGATIONAL_INTERACTING_GENE_ALTERATIONS_P,
        "DO NOT IMPORT.*"
    };
    
    private QuestDocAnnotationParser() {
        throw new AssertionError();
    }
    
    private static final String QUEST_CURATION_FOLDER = "/Users/jgao/projects/oncokb-data/quest-annotation";
    private static final String QUEST_CURATION_FILE = "/data/quest-curations.txt";
    
    public static void main(String[] args) throws Exception {
        VariantConsequenceImporter.main(args);
        TumorTypeImporter.main(args);
        PiHelperDrugImporter.main(args);
        List<String> files = FileUtils.getFilesInFolder(QUEST_CURATION_FOLDER, "docx");
        for (String file : files) {
            parse(new FileInputStream(file));
        }
        
//        parse(new FileInputStream(QUEST_CURATION_FOLDER+"/GNA11.docx"));
    }
    
    private static void parse(InputStream is) throws IOException {
        List<String> lines = FileUtils.readLinesDocStream(is, true);
        List<int[]> geneLines = extractLines(lines, 0, lines.size(), GENE_P, 1);
        for (int[] ix : geneLines) {
            parseGene(lines, ix[0], ix[1]);
        }
    }
    
    private static void parseGene(List<String> lines, int start, int end) throws IOException {
        if (!lines.get(start).startsWith("Gene: ")) {
            throw new RuntimeException("Gene line should start with Gene: ");
        }
        
        System.out.println("##"+lines.get(start));
        
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        
        Pattern p = Pattern.compile(GENE_P, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(lines.get(start));
        m.matches();
        String hugo = m.group(1);
        Gene gene = geneBo.findGeneByHugoSymbol(hugo);
        if (gene == null) {
            System.out.println("Could not find gene "+hugo+". Loading from MyGene.Info...");
            gene = GeneAnnotatorMyGeneInfo2.readByHugoSymbol(hugo);
            if (gene == null) {
                throw new RuntimeException("Could not find gene "+hugo+" either.");
            }
            geneBo.save(gene);
        }
        
        // summary
        int[] summaryBgLines = parseSummary(gene, lines, start, end);
        
        // background
        int[] geneBgLines = parseGeneBackground(gene, lines, start, end);
        
        // mutations
        parseMutations(gene, lines, geneBgLines[1], end);
    }
    
    private static int[] parseSummary(Gene gene, List<String> lines, int start, int end) {
        List<int[]> summaryLines = extractLines(lines, start, end, SUMMARY_P, GENE_BACKGROUND_P, 1);
        if (summaryLines.size()!=1) {
            throw new RuntimeException("No summary for "+gene.getHugoSymbol());
        }
        
        System.out.println("##  Summary");
        
        int sSummary = summaryLines.get(0)[0];
        int eSummary = summaryLines.get(0)[1];
        
        // gene summary
        List<int[]> geneSummaryLines = extractLines(lines, sSummary, eSummary, SUMMARY_P, 1);
        String geneSummary = joinLines(lines, geneSummaryLines.get(0)[0]+1, geneSummaryLines.get(0)[1]);
        Evidence evidence = new Evidence();
        evidence.setEvidenceType(EvidenceType.GENE_SUMMARY);
        evidence.setGene(gene);
        evidence.setDescription(geneSummary);
        setDocuments(geneSummary, evidence);
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        evidenceBo.save(evidence);
        
//        // tumor types
//        List<int[]> cancerSummaryLines = extractLines(lines, geneSummaryLines.get(0)[1], eSummary, TUMOR_TYPE_P, TUMOR_TYPE_P, -1);
//        for (int[] cancerLine : cancerSummaryLines) {
//            String line = lines.get(cancerLine[0]);
//            Pattern p = Pattern.compile(TUMOR_TYPE_P, Pattern.CASE_INSENSITIVE);
//            Matcher m = p.matcher(line);
//            if (!m.matches()) {
//                throw new RuntimeException("wrong format of type type line: "+line);
//            }
//            String cancer = m.group(1).trim();
//
//            System.out.println("##    Cancer type: " + cancer);
//
//            TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
//            TumorType tumorType = tumorTypeBo.findTumorTypeByName(cancer);
//            if (tumorType==null) {
//                tumorType = new TumorType();
//                tumorType.setTumorTypeId(cancer);
//                tumorType.setName(cancer);
//                tumorType.setClinicalTrialKeywords(Collections.singleton(cancer));
//                tumorTypeBo.save(tumorType);
//            }
//
//            String cancerSummary = joinLines(lines, cancerLine[0]+1, cancerLine[1]);
//            evidence = new Evidence();
//            evidence.setEvidenceType(EvidenceType.GENE_TUMOR_TYPE_SUMMARY);
//            evidence.setGene(gene);
//            evidence.setTumorType(tumorType);
//            evidence.setDescription(cancerSummary);
//            setDocuments(cancerSummary, evidence);
//            evidenceBo.save(evidence);
//        }
        
        return summaryLines.get(0);
    }
    
    private static int[] parseGeneBackground(Gene gene, List<String> lines, int start, int end) {
        List<int[]> backgroundLines = extractLines(lines, start, end, GENE_BACKGROUND_P, MUTATION_P, 1);
        if (backgroundLines.size()!=1) {
            throw new RuntimeException("There should be one background section for gene: "+gene.getHugoSymbol());
        }
        
        System.out.println("##  Background");
        
        int s = backgroundLines.get(0)[0];
        int e = backgroundLines.get(0)[1];
        StringBuilder sb = new StringBuilder();
        for (int i=s+1; i<e; i++) {
            if (!lines.get(i).startsWith("cBioPortal link:") &&
                    !lines.get(i).startsWith("COSMIC link:") &&
                    !lines.get(i).startsWith("Mutations")) {
                sb.append(lines.get(i)).append("\n");
            }
        }
        String bg = sb.toString();
        
        Evidence evidence = new Evidence();
        evidence.setEvidenceType(EvidenceType.GENE_BACKGROUND);
        evidence.setGene(gene);
        evidence.setDescription(bg);
        setDocuments(bg, evidence);
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        evidenceBo.save(evidence);
        
        return backgroundLines.get(0);
    }
    
    private static void parseMutations(Gene gene, List<String> lines, int start, int end) {
        List<int[]> mutationLines = extractLines(lines, start, end, MUTATION_P, MUTATION_P, -1);
        for (int[] ixMutationLines : mutationLines) {
            int startMutation = ixMutationLines[0];
            int endMutation = ixMutationLines[1];
            parseMutation(gene, lines, startMutation, endMutation);
        }
        
    }
    
    private static void parseMutation(Gene gene, List<String> lines, int start, int end) {
        // mutation
        Pattern p = Pattern.compile(MUTATION_P, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(lines.get(start));
        if (!m.matches()) {
            throw new RuntimeException("wrong format of mutation line: "+lines.get(0));
        }
        
        String mutationStr = m.group(1).trim();
        
        System.out.println("##  Mutation: "+mutationStr);
        
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        AlterationType type = AlterationType.MUTATION; //TODO: cna and fusion
        
        // oncogenic
        List<int[]> line = extractLines(lines, start, end, MUTATION_ONCOGENIC_P, TUMOR_TYPE_P, 1);
        Boolean oncogenic = Boolean.FALSE;
        if (line.isEmpty()) {
            System.err.println("Warning: no oncogenic line");
        } else {
            String oncogenicStr = lines.get(line.get(0)[0]);
            p = Pattern.compile(MUTATION_ONCOGENIC_P, Pattern.CASE_INSENSITIVE);
            m = p.matcher(oncogenicStr);
            if (!m.matches()) {
                System.err.println("Error: wrong format of oncogenic line: "+oncogenicStr);
            } else {
                oncogenic = m.group(1).equalsIgnoreCase("YES");
            }
        }
        
        Set<Alteration> alterations = new HashSet<Alteration>();
        Map<String,String> mutations = parseMutationString(mutationStr);
        for (Map.Entry<String,String> mutation : mutations.entrySet()) {
            String proteinChange = mutation.getKey();
            String displayName = mutation.getValue();
            Alteration alteration = alterationBo.findAlteration(gene, type, proteinChange);
            if (alteration==null) {
                alteration = new Alteration();
                alteration.setGene(gene);
                alteration.setAlterationType(type);
                alteration.setAlteration(proteinChange);
                alteration.setName(displayName);
                alteration.setOncogenic(oncogenic);
                AlterationUtils.annotateAlteration(alteration, proteinChange);
                alterationBo.save(alteration);
            }
            
            if (oncogenic && !alteration.getOncogenic()) {
                alterationBo.update(alteration);
            }
            alterations.add(alteration);
        }
        
        // mutation effect
        line = extractLines(lines, start, end, MUTATION_EFFECT_P, TUMOR_TYPE_P, 1);
        String effect = null;
        if (line.isEmpty()) {
            System.err.println("Warning: no mutation effect line");
        } else {
            String mutationEffectStr = lines.get(line.get(0)[0]);        
            p = Pattern.compile(MUTATION_EFFECT_P, Pattern.CASE_INSENSITIVE);
            m = p.matcher(mutationEffectStr);
            if (!m.matches()) {
                System.err.println("Error: wrong format of mutation effect line: "+mutationEffectStr);
            } else {
                effect = m.group(1);
                if (effect==null || effect.isEmpty()) {
                    effect = "Unknown";
                }
                System.out.println("##    Effect: "+alterations.toString());

                effect = effect.trim();
            }
        }
        
        // description
        line = extractLines(lines, start, end, MUTATION_EFFECT_DESCRIPTION_P, TUMOR_TYPE_P, 1);
        String desc = null;
        if (line.isEmpty()) {
            System.err.println("Warning: no mutation desc line");
        } else {
            desc = joinLines(lines, line.get(0)[0]+1, line.get(0)[1]);
        }
        
        // save
        if (effect!=null || desc!=null) {
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.MUTATION_EFFECT);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setDescription(desc);
            evidence.setKnownEffect(effect);
            setDocuments(desc, evidence);

            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            evidenceBo.save(evidence);
        }
        
        // cancers
        List<int[]> cancerLines = extractLines(lines, start+1, end, TUMOR_TYPE_P, TUMOR_TYPE_P, -1);
        for (int[] ixcancerLines : cancerLines) {
            int startCancer = ixcancerLines[0];
            int endCancer = ixcancerLines[1];
            parseCancer(gene, alterations, lines, startCancer, endCancer);
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
                displayName = part.substring(l+1, r).trim();
            } else {
                proteinChange = part;
                displayName = part;
            }
            
            Matcher m = p.matcher(proteinChange);
            if (m.find()) {
                String ref = m.group(1);
                for (String var : m.group(2).split("/")) {
                    ret.put(ref+var, ref+var);
                }
            } else {
                ret.put(proteinChange, displayName);
            }
        }
        return ret;
    }
    
    private static void parseCancer(Gene gene, Set<Alteration> alterations, List<String> lines, int start, int end) {
        String line = lines.get(start);
        Pattern p = Pattern.compile(TUMOR_TYPE_P, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(line);
        if (!m.matches()) {
            throw new RuntimeException("wrong format of tumor type line: "+line);
        }
        String cancer = m.group(1).trim();
        
        if (cancer.endsWith(DO_NOT_IMPORT)) {
            System.out.println("##    Cancer type: " + cancer+ " -- skip");
            return;
        }
        
        System.out.println("##    Cancer type: " + cancer);
        
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        TumorType tumorType = tumorTypeBo.findTumorTypeByName(cancer);
        if (tumorType==null) {
            tumorType = new TumorType();
            tumorType.setTumorTypeId(cancer);
            tumorType.setName(cancer);
            tumorType.setClinicalTrialKeywords(Collections.singleton(cancer));
            tumorTypeBo.save(tumorType);
        }
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        
        // Prevalance
        List<int[]> prevalenceLines = extractLines(lines, start+1, end, PREVALENCE_P, CANCER_HEADERS_P, 1);
        if (!prevalenceLines.isEmpty()) {
            System.out.println("##      Prevalance: " + alterations.toString());
            String prevalenceTxt = joinLines(lines, prevalenceLines.get(0)[0]+1, prevalenceLines.get(0)[1]).trim();
            if (!prevalenceTxt.isEmpty()) {
                Evidence evidence = new Evidence();
                evidence.setEvidenceType(EvidenceType.PREVALENCE);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);
                evidence.setTumorType(tumorType);
                evidence.setDescription(prevalenceTxt);
                setDocuments(prevalenceTxt, evidence);

                evidenceBo.save(evidence);
            }
        } else {
            System.out.println("##      No Prevalance for " + alterations.toString());
        }
        
        // Prognostic implications
        List<int[]> prognosticLines = extractLines(lines, start+1, end, PROGNOSTIC_IMPLICATIONS_P, CANCER_HEADERS_P, 1);
        if (!prognosticLines.isEmpty()) {
            System.out.println("##      Proganostic implications:" + alterations.toString());
            String prognosticTxt = joinLines(lines, prognosticLines.get(0)[0]+1, prognosticLines.get(0)[1]).trim();
            if (!prognosticTxt.isEmpty()) {

                Evidence evidence = new Evidence();
                evidence.setEvidenceType(EvidenceType.PROGNOSTIC_IMPLICATION);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);
                evidence.setTumorType(tumorType);
                evidence.setDescription(prognosticTxt);
                setDocuments(prognosticTxt, evidence);

                evidenceBo.save(evidence);
            }
        } else {
            System.out.println("##      No Proganostic implications "+alterations.toString());
        }
        
        // standard therapeutic implications of drug sensitivity
        List<int[]> standardSensitivityLines = extractLines(lines, start+1, end, STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P, CANCER_HEADERS_P, 1);
        if (!standardSensitivityLines.isEmpty()) {
            parseTherapeuticImplcations(gene, alterations, tumorType, lines, standardSensitivityLines.get(0)[0], standardSensitivityLines.get(0)[1],
                    EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY, "Sensitive", SENSITIVE_TO_P);
        } else {
            System.out.println("##      No "+STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P+" for "+alterations.toString());
        }
        
        // standard therapeutic implications of drug resistance
        List<int[]> standardResistanceLines = extractLines(lines, start+1, end, STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P, CANCER_HEADERS_P, 1);
        if (!standardResistanceLines.isEmpty()) {
            parseTherapeuticImplcations(gene, alterations, tumorType, lines, standardResistanceLines.get(0)[0], standardResistanceLines.get(0)[1],
                    EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE, "Resistant", RESISTANT_TO_P); 
        } else {
            System.out.println("##      No "+STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P+" for "+alterations.toString());
        }
        
        // NCCN
        List<int[]> nccnLines = extractLines(lines, start+1, end, NCCN_GUIDELINES_P, CANCER_HEADERS_P, 1);
        if (!nccnLines.isEmpty()) {
            System.out.println("##      NCCN for "+alterations.toString());
            for (int[] nccnLine: nccnLines) {
                List<int[]> nccnOneDiseaseLines = extractLines(lines, nccnLine[0]+1, nccnLine[1], NCCN_DISEASE_P, NCCN_DISEASE_P, -1);
                for (int[] nccnOneDiseaseLine : nccnOneDiseaseLines) {
                    parseNCCN(gene, alterations, tumorType, lines, nccnOneDiseaseLine[0], nccnOneDiseaseLine[1]);
                }
            }
        } else {
            System.out.println("##      No NCCN for "+alterations.toString());
        }
        
        
        // Investigational therapeutic implications of drug sensitivity
        List<int[]> investigationalSensitivityLines = extractLines(lines, start+1, end, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P, CANCER_HEADERS_P, 1);
        if (!investigationalSensitivityLines.isEmpty()) {
            parseTherapeuticImplcations(gene, alterations, tumorType, lines, investigationalSensitivityLines.get(0)[0], investigationalSensitivityLines.get(0)[1],
                EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY, "Sensitive", SENSITIVE_TO_P);
        } else {
            System.out.println("##      No "+INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P+" for "+alterations.toString());
        }
        
        // Investigational therapeutic implications of drug resistance
        List<int[]> investigationalResistanceLines = extractLines(lines, start+1, end, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P, CANCER_HEADERS_P, 1);
        if (!investigationalResistanceLines.isEmpty()) {
            parseTherapeuticImplcations(gene, alterations, tumorType, lines, investigationalResistanceLines.get(0)[0], investigationalResistanceLines.get(0)[1],
                EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE, "Resistant", RESISTANT_TO_P);
        } else {
            System.out.println("##      No "+INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P+" for "+alterations.toString());
        }
        
        List<int[]> clinicalTrialsLines = extractLines(lines, start+1, end, ONGOING_CLINICAL_TRIALS_P, CANCER_HEADERS_P, 1);
        if (!clinicalTrialsLines.isEmpty()) {
            System.out.println("##      Clincial trials for "+alterations.toString());
            parseClinicalTrials(gene, alterations, tumorType, lines, clinicalTrialsLines.get(0)[0], clinicalTrialsLines.get(0)[1]);
        } else {
            System.out.println("##      No Clincial trials for "+alterations.toString());
        }
    }
    
    private static void parseClinicalTrials(Gene gene, Set<Alteration> alterations, TumorType tumorType, List<String> lines, int start, int end) {
        ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();
        Pattern p = Pattern.compile(CLINICAL_TRIALS_P, Pattern.CASE_INSENSITIVE);
        Set<String> nctIds = new HashSet<String>();
        for (int i=start; i<end; i++) {
            Matcher m = p.matcher(lines.get(i));
            if (m.find()) {
                String nctId = m.group(1);
                nctIds.add(nctId);
            }
        }
        try {
            List<ClinicalTrial> trials = ClinicalTrialsImporter.importTrials(nctIds);
            for (ClinicalTrial trial : trials) {
                trial.getAlterations().addAll(alterations);
                trial.getTumorTypes().add(tumorType);
                trial.getGenes().add(gene);
                clinicalTrialBo.saveOrUpdate(trial);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void parseTherapeuticImplcations(Gene gene, Set<Alteration> alterations, TumorType tumorType, List<String> lines, int start, int end,
            EvidenceType evidenceType, String knownEffectOfEvidence, String sensitivieP) {
        System.out.println("##      "+evidenceType+" for "+alterations.toString()+" "+tumorType.getName());
        
        List<int[]> drugLines = extractLines(lines, start+1, end, sensitivieP, sensitivieP, -1);
        if (drugLines.isEmpty()) return;
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        
        {
            // general description
            String desc = joinLines(lines, start+1, drugLines.get(0)[0]).trim();
            if (!desc.isEmpty()) {
                Evidence evidence = new Evidence();
                evidence.setEvidenceType(evidenceType);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);
                evidence.setTumorType(tumorType);
                evidence.setKnownEffect(knownEffectOfEvidence);
                evidence.setDescription(desc);
                setDocuments(desc, evidence);
                evidenceBo.save(evidence);
            }
        }
        
        // specific evidence
        Pattern pSensitiveTo = Pattern.compile(sensitivieP);
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        TreatmentBo treatmentBo = ApplicationContextSingleton.getTreatmentBo();
        
        for (int[] drugLine : drugLines) {
            System.out.println("##        drugs: "+lines.get(drugLine[0]));
            
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(evidenceType);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setTumorType(tumorType);
            evidence.setKnownEffect(knownEffectOfEvidence);
            
            // approved indications
            Set<String> appovedIndications = new HashSet<String>();
            Pattern pApprovedFor = Pattern.compile(APPROVED_INDICATION_P, Pattern.CASE_INSENSITIVE);
            List<int[]> approvedForLines = extractLines(lines, drugLine[0], drugLine[1], APPROVED_INDICATION_P, 1);
            if (!approvedForLines.isEmpty()) {
                Matcher m = pApprovedFor.matcher(lines.get(approvedForLines.get(0)[0]));
                if (m.matches())  {
                    appovedIndications = new HashSet<String>(Arrays.asList(m.group(1).trim().split(";")));
                }
            }
            
            // sensitive to
            List<int[]> sensitiveLines = extractLines(lines, drugLine[0], drugLine[1], sensitivieP, 1);
            if (sensitiveLines.isEmpty()) continue;
            
            Matcher m = pSensitiveTo.matcher(lines.get(sensitiveLines.get(0)[0]));
            if (!m.matches()) continue;
            
            String[] drugTxts = m.group(1).trim().replaceAll("(\\([^\\)]*\\))|(\\[[^\\]]*\\])", "").split(",");

            Set<Treatment> treatments = new HashSet<Treatment>();
            for (String drugTxt : drugTxts) {
                String[] drugNames = drugTxt.split(" ?\\+ ?");
                
                Set<Drug> drugs = new HashSet<Drug>();
                for (String drugName : drugNames) {
                    drugName = drugName.trim();
                    Drug drug = drugBo.guessUnambiguousDrug(drugName);
                    if (drug==null) {
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
            Pattern pLevel = Pattern.compile(HIGHEST_LEVEL_OF_EVIDENCE, Pattern.CASE_INSENSITIVE);
            List<int[]> levelLines = extractLines(lines, drugLine[0], drugLine[1], HIGHEST_LEVEL_OF_EVIDENCE, 1);
            if (levelLines.isEmpty()){
                System.err.println("Error: no level of evidence");
                // TODO:
                //throw new RuntimeException("no level of evidence");
            } else {
                m = pLevel.matcher(lines.get(levelLines.get(0)[0]));
                if (!m.matches())  {
                    throw new RuntimeException("wrong format level of evidence");
                }
                
                String level = m.group(1).toLowerCase();
                if (level.equals("2")) {
                    
                    if (evidenceType == EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE
                        || evidenceType == EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY) {
                        level = "2a";
                    } else {
                        level = "2b";
                    }
                }
                
                LevelOfEvidence levelOfEvidence = LevelOfEvidence.getByLevel(level);
                if (levelOfEvidence==null) {
                    System.err.println("Errow: wrong level of evidence: "+level);
                    // TODO:
                    //throw new RuntimeException("wrong level of evidence: "+level);
                }
                evidence.setLevelOfEvidence(levelOfEvidence);
            }
            
            // description
            List<int[]> descLines = extractLines(lines, drugLine[0], drugLine[1], DESCRIPTION_OF_EVIDENCE_P, 1);
            if (!descLines.isEmpty()) {
                String desc = joinLines(lines, descLines.get(0)[0]+1, descLines.get(0)[1]).trim();
                if (!desc.isEmpty()) {
                    evidence.setDescription(desc);
                    setDocuments(desc, evidence);
                }
            }
            
            evidenceBo.save(evidence);
        }
    }
    
    private static void parseNCCN(Gene gene, Set<Alteration> alterations, TumorType tumorType, List<String> lines, int start, int end) {
        // disease
        String disease = null;
        List<int[]> line = extractLines(lines, start, end, NCCN_DISEASE_P, 1);
        if (line.isEmpty()) {
            throw new RuntimeException("Problem with NCCN disease line");
        } else {
            Pattern p = Pattern.compile(NCCN_DISEASE_P, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(lines.get(line.get(0)[0]));
            if (m.matches())  {
                disease = m.group(1);
            }
        }
        
        // version
        String version = null;
        line = extractLines(lines, start, end, NCCN_VERSION_P, 1);
        if (line.isEmpty()) {
            System.err.println("Warning: Problem with NCCN version line");
        } else {
            Pattern p = Pattern.compile(NCCN_VERSION_P, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(lines.get(line.get(0)[0]));
            if (m.matches())  {
                version = m.group(1);
            }
        }
        
        // pages
        String pages = null;
        line = extractLines(lines, start, end, NCCN_PAGES_P, 1);
        if (line.isEmpty()) {
            System.err.println("Warning: Problem with NCCN pages line");
        } else {
            Pattern p = Pattern.compile(NCCN_PAGES_P, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(lines.get(line.get(0)[0]));
            if (m.matches())  {
                pages = m.group(1);
            }
        }
        
        // Recommendation category
        String category = null;
        line = extractLines(lines, start, end, NCCN_PAGES_RECOMMENDATION_CATEGORY, 1);
        if (line.isEmpty()) {
            System.err.println("Warning: Problem with NCCN category line");
        } else {
            Pattern p = Pattern.compile(NCCN_PAGES_RECOMMENDATION_CATEGORY, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(lines.get(line.get(0)[0]));
            if (m.matches())  {
                category = m.group(1);
            }
        }
        
        // description
        String nccnDescription = null;
        line = extractLines(lines, start, end, NCCN_EVIDENCE_P, 1);
        if (line.isEmpty()) {
            System.err.println("Warning: Problem with NCCN description line");
        } else {
            String desc = joinLines(lines, line.get(0)[0]+1, end).trim();
            if (!desc.isEmpty()) {
                nccnDescription = desc;
            }
        }
        
        
        Evidence evidence = new Evidence();
        evidence.setEvidenceType(EvidenceType.NCCN_GUIDELINES);
        evidence.setAlterations(alterations);
        evidence.setGene(gene);
        evidence.setTumorType(tumorType);
        evidence.setDescription(nccnDescription);
        
        NccnGuidelineBo nccnGuideLineBo = ApplicationContextSingleton.getNccnGuidelineBo();
        
        NccnGuideline nccnGuideline = new NccnGuideline();
        nccnGuideline.setDisease(disease);
        nccnGuideline.setVersion(version);
        nccnGuideline.setPages(pages);
        nccnGuideline.setCategory(category);
        nccnGuideline.setDescription(nccnDescription);
        nccnGuideLineBo.save(nccnGuideline);

        evidence.setNccnGuidelines(Collections.singleton(nccnGuideline));
                
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        evidenceBo.save(evidence);
    }
    
    private static List<int[]> extractLines(List<String> lines, int start, int end, String startLinePatten, int limit) {
        return extractLines(lines, start, end, startLinePatten, (String[])null, limit);
    }
    
    private static List<int[]> extractLines(List<String> lines, int start, int end, String startLinePatten, String endLinePattern, int limit) {
        return extractLines(lines, start, end, startLinePatten, new String[]{endLinePattern}, limit);
    }
    
    private static List<int[]> extractLines(List<String> lines, int start, int end, String startLinePatten, String[] endLinePatterns, int limit) {
        List<int[]> indices = new ArrayList<int[]>();

        int s=start, e=start;
        
        while (s<end && e<end) {
            // find start line
            s = e;
            while (s<end && !lines.get(s).toUpperCase().matches(startLinePatten.toUpperCase())) {
                s++;
            }

            // find end line
            e = endLinePatterns==null ? end : (s + 1);
            while (e<end && !matchAnyPattern(lines.get(e), endLinePatterns)) {
                e++;
            }

            if (s<end) {
                indices.add(new int[]{s,e});
            }
            
            if (limit>0 && indices.size()>=limit) {
                break;
            }
        }
        
        return indices;
    }
    
    private static boolean matchAnyPattern(String s, String[] patterns) {
        for (String p : patterns) {
            if (s.toUpperCase().matches(p.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
    
    private static String joinLines(List<String> lines, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i=start; i<end; i++) {
            String line = lines.get(i);
            if (line.startsWith("DO NOT IMPORT")) {
                break;
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
    
    private static void setDocuments(String str, Evidence evidence) {
        if (str==null) return;
        Set<Article> docs = new HashSet<Article>();
        Set<ClinicalTrial> clinicalTrials = new HashSet<ClinicalTrial>();
        ArticleBo articleBo = ApplicationContextSingleton.getArticleBo();
        ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();
        Pattern pmidPattern = Pattern.compile("\\(PMIDs?:([^\\);]+).*\\)", Pattern.CASE_INSENSITIVE);
        Matcher m = pmidPattern.matcher(str);
        int start = 0;
        while (m.find(start)) {
            String pmids = m.group(1).trim();
            for (String pmid : pmids.split(", *(PMID:)? *")) {
                if (pmid.startsWith("NCT")) {
                    // support NCT numbers
                    String[] nctIds = pmid.split(", *");
                    for (String nctId : nctIds) {
                        ClinicalTrial ct = clinicalTrialBo.findClinicalTrialByNctId(nctId);
                        if (ct==null) {
                            ct = new ClinicalTrial();
                            ct.setNctId(nctId);
                            clinicalTrialBo.save(ct);
                        }
                        clinicalTrials.add(ct);
                    }
                }
                
                Article doc = articleBo.findArticleByPmid(pmid);
                if (doc==null) {
                    doc = NcbiEUtils.readPubmedArticle(pmid);
                    articleBo.save(doc);
                }
                docs.add(doc);
            }
            start = m.end();
        }
        
        evidence.setArticles(docs);
        evidence.setClinicalTrials(clinicalTrials);
    }
}
