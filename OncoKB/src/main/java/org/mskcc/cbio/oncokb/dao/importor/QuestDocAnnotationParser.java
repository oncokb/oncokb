
package org.mskcc.cbio.oncokb.dao.importor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.DocumentBo;
import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBlobBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Document;
import org.mskcc.cbio.oncokb.model.DocumentType;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceBlob;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.KnownEffectOfEvidence;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.mskcc.cbio.oncokb.util.GeneAnnotatorMyGeneInfo2;

/**
 *
 * @author jgao
 */
public final class QuestDocAnnotationParser {
    private static final String GENE_P = "^Gene: ?(.+)";
    private static final String GENE_BACKGROUND_P = "^Background";
    private static final String MUTATIONS_P = "^Mutations:?.*";
    private static final String MUTATION_P = "^Mutation: ?(.+)";
    private static final String MUTATION_EFFECT_P = "^Mutation effect: ?([^\\\\(]+)(\\\\(PMIDs?:.+\\\\))?";
    private static final String MUTATION_EFFECT_DESCRIPTION_P = "^Description of mutation effect: ?.*";
    private static final String TUMOR_TYPE_P = "^Tumor type: ?(.+)";
    private static final String PREVALENCE_P = "^Prevalence$";
    private static final String PROGNOSTIC_IMPLICATIONS_P = "^Prognostic implications$";
    private static final String STANDARD_THERAPEUTIC_IMPLICATIONS_P = "^Standard therapeutic implications$";
    private static final String STANDARD_THERAPEUTIC_IMPLICATIONS_SENSITIVE_TO_P = "^Sensitive to: ?(.*)";
    private static final String STANDARD_THERAPEUTIC_IMPLICATIONS_INSENSITIVE_TO_P = "^Insensitive to: ?(.*)";
    private static final String STANDARD_THERAPEUTIC_IMPLICATIONS_EVIDENCE_P = "^Description of evidence: ?(.*)";
    private static final String NCCN_GUIDELINES_P = "^NCCN guidelines$";
    private static final String NCCN_DISEASE_P = "^Disease: ?(.*)";
    private static final String NCCN_VERSION_P = "^Version: ?(.*)";
    private static final String NCCN_PAGES_P = "^Pages: ?(.*)";
    private static final String INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_P = "^Investigational therapeutic implications$";
    private static final String INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_SENSITIVE_TO_P = "^Sensitive to (Highest level of evidence): ?(.*)";
    private static final String INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_INSENSITIVE_TO_P = "^Insensitive to: ?(.*)";
    private static final String INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_EVIDENCE_P = "^Description of evidence: ?(.*)";
    private static final String ONGOING_CLINICAL_TRIALS_P = "^Ongoing clinical trials:?$";
    private static final String INTERACTNG_GENE_ALTERATIONS_P = "^Interacting gene alterations$";
    private static final String INTERACTNG_GENE_ALTERATIONS_GENE_P = "^Gene: ?(.*)";
    private static final String INTERACTNG_GENE_ALTERATIONS_MUTATION_P = "^Mutation (mutation effect): ?(.*)";
    private static final String INTERACTNG_GENE_ALTERATIONS_SENSITIVE_TO_P = "^Sensitive to: ?(.*)";
    private static final String INTERACTNG_GENE_ALTERATIONS_INSENSITIVE_TO_P = "^Insensitive to: ?(.*)";
    private static final String INTERACTNG_GENE_ALTERATIONS_EVIDENCE_P = "^Description of evidence: ?(.*)";
    
    private QuestDocAnnotationParser() {
        throw new AssertionError();
    }
    
    private static final String QUEST_CURATION_FILE = "/data/quest-curations.txt";
    
    public static void main(String[] args) throws IOException {
        parse(QUEST_CURATION_FILE);
    }
    
    private static void parse(String file) throws IOException {
        List<String> lines = FileUtils.readLinesStream(
                GeneLabelImporter.class.getResourceAsStream(file));
        List<int[]> geneLines = extractLines(lines, 0, lines.size(), GENE_P, GENE_P, -1);
        for (int[] ix : geneLines) {
            parseGene(lines, ix[0], ix[1]);
        }
    }
    
    private static void parseGene(List<String> lines, int start, int end) throws IOException {
        if (!lines.get(start).startsWith("Gene: ")) {
            System.err.println("Gene line should start with Gene: ");
        }
        
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        
        String hugo = lines.get(start).substring(6);
        Gene gene = geneBo.findGeneByHugoSymbol(hugo);
        if (gene == null) {
            System.out.println("Could not find gene "+hugo+". Loading from MyGene.Info...");
            gene = GeneAnnotatorMyGeneInfo2.readByHugoSymbol(hugo);
            if (gene == null) {
                System.err.println("Could not find gene "+hugo+" either.");
            }
            geneBo.save(gene);
        }
        
        // background
        int[] geneBgLines = parseGeneBackground(gene, lines, start, end);
        
        // mutations
        parseMutations(gene, lines, geneBgLines[1], end);
    }
    
    private static int[] parseGeneBackground(Gene gene, List<String> lines, int start, int end) {
        List<int[]> backgroundLines = extractLines(lines, start, end, GENE_BACKGROUND_P, MUTATIONS_P, 1);
        if (backgroundLines.size()!=1) {
            System.err.println("There should be one background section for a gene");
        }
        
        int s = backgroundLines.get(0)[0];
        int e = backgroundLines.get(0)[1];
        StringBuilder sb = new StringBuilder();
        for (int i=s+1; i<e; i++) {
            if (!lines.get(i).startsWith("cBioPortal link:") &&
                    lines.get(i).startsWith("COSMIC link:")) {
                sb.append(lines.get(i)).append("\n");
            }
        }
        String bg = sb.toString();
        
        EvidenceBlob eb = new EvidenceBlob();
        eb.setEvidenceType(EvidenceType.GENE_BACKGROUND);
        eb.setGene(gene);
        eb.setDescription(bg);
        
        Set<Document> docs = extractDocuments(bg);
        Evidence evidence = new Evidence();
        evidence.setDocuments(docs);
        evidence.setEvidenceBlob(eb);
        eb.setEvidences(Collections.singleton(evidence));
        
        EvidenceBlobBo evidenceBlobBo = ApplicationContextSingleton.getEvidenceBlobBo();
        evidenceBlobBo.save(eb);
        
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
        Pattern p = Pattern.compile(MUTATION_P);
        Matcher m = p.matcher(lines.get(start));
        if (!m.matches()) {
            System.err.println("wrong format of mutation line: "+lines.get(0));
        }
        
        String mutation = m.group(1).trim();
        
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        AlterationType type = AlterationType.MUTATION; //TODO: cna and fution
        Alteration alteration = alterationBo.findAlteration(gene, type, mutation);
        if (alteration==null) {
            alteration = new Alteration(gene, mutation, type);
            alterationBo.save(alteration);
        }
        
        // mutation effect
        List<int[]> mutationEffectLine = extractLines(lines, start, end, MUTATION_EFFECT_P, MUTATION_EFFECT_DESCRIPTION_P, 1);
        if (mutationEffectLine.size()!=1) {
            System.err.println("There should be one mutation effect line per gene");
        }
        String mutationEffectStr = lines.get(mutationEffectLine.get(0)[0]);
        Set<Document> docs = extractDocuments(mutationEffectStr);
        
        p = Pattern.compile(MUTATION_EFFECT_P);
        m = p.matcher(mutationEffectStr);
        if (!m.matches()) {
            System.err.println("wrong format of mutation effect line: "+mutationEffectStr);
        }
        KnownEffectOfEvidence effect = KnownEffectOfEvidence.valueOf(m.group(1).trim());
        
        // Description of mutation effect
        List<int[]> mutationEffectDescLine = extractLines(lines, mutationEffectLine.get(0)[1], end, MUTATION_EFFECT_DESCRIPTION_P, TUMOR_TYPE_P, 1);
        if (mutationEffectLine.size()!=1) {
            System.err.println("There should be one mutation effect line per gene");
        }
        
        p = Pattern.compile(MUTATION_EFFECT_DESCRIPTION_P);
        m = p.matcher(joinLines(lines, mutationEffectDescLine.get(0)[0], mutationEffectDescLine.get(0)[1]));
        if (!m.matches()) {
            System.err.println("wrong format of mutation effect description lines: "+mutationEffectStr);
        }
        String mutationEffect = m.group(1).trim();
        
        EvidenceBlob eb = new EvidenceBlob();
        eb.setEvidenceType(EvidenceType.MUTATION_EFFECT);
        eb.setAlteration(alteration);
        eb.setGene(gene);
        eb.setDescription(mutationEffect);
        
        Evidence evidence = new Evidence();
        evidence.setKnownEffect(effect);
        evidence.setDocuments(docs);
        evidence.setEvidenceBlob(eb);
        eb.setEvidences(Collections.singleton(evidence));
        
        EvidenceBlobBo evidenceBlobBo = ApplicationContextSingleton.getEvidenceBlobBo();
        evidenceBlobBo.save(eb);
        
        // cancers
        List<int[]> cancerLines = extractLines(lines, mutationEffectDescLine.get(0)[1], end, TUMOR_TYPE_P, TUMOR_TYPE_P, -1);
        for (int[] ixcancerLines : cancerLines) {
            int startCancer = ixcancerLines[0];
            int endCancer = ixcancerLines[1];
            parseCancer(alteration, lines, startCancer, endCancer);
        }
    }
    
    private static void parseCancer(Alteration alteration, List<String> lines, int start, int end) {
        String line = lines.get(start);
        String cancer = line.substring(line.indexOf(" in ")+4);
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        TumorType tumorType = tumorTypeBo.findTumorTypeByName(cancer);
        if (tumorType==null) {
            tumorType = new TumorType(cancer, cancer, cancer);
            tumorTypeBo.save(tumorType);
        }
        
        EvidenceBlobBo evidenceBlobBo = ApplicationContextSingleton.getEvidenceBlobBo();
        
        // Prevalance
        List<int[]> prevalenceLines = extractLines(lines, start+1, end, PREVALENCE_P, PROGNOSTIC_IMPLICATIONS_P, 1);
        String prevalenceTxt = joinLines(lines, prevalenceLines.get(0)[0]+1, prevalenceLines.get(0)[1]).trim();
        if (!prevalenceTxt.isEmpty()) {
            Set<Document> prevalenceDocs = extractDocuments(prevalenceTxt);
            
            EvidenceBlob eb = new EvidenceBlob();
            eb.setEvidenceType(EvidenceType.PREVALENCE);
            eb.setAlteration(alteration);
            eb.setGene(alteration.getGene());
            eb.setTumorType(tumorType);
            eb.setDescription(prevalenceTxt);
            
            Evidence evidence = new Evidence();
            evidence.setDocuments(prevalenceDocs);
            evidence.setEvidenceBlob(eb);
            eb.setEvidences(Collections.singleton(evidence));

            evidenceBlobBo.save(eb);
        }
        
        // Prognostic implications
        List<int[]> prognosticLines = extractLines(lines, prevalenceLines.get(0)[1], end, PROGNOSTIC_IMPLICATIONS_P, STANDARD_THERAPEUTIC_IMPLICATIONS_P, 1);
        String prognosticTxt = joinLines(lines, prognosticLines.get(0)[0]+1, prognosticLines.get(0)[1]).trim();
        if (!prognosticTxt.isEmpty()) {
            Set<Document> prognosticDocs = extractDocuments(prognosticTxt);
            
            EvidenceBlob eb = new EvidenceBlob();
            eb.setEvidenceType(EvidenceType.PROGNOSTIC_IMPLICATION);
            eb.setAlteration(alteration);
            eb.setGene(alteration.getGene());
            eb.setTumorType(tumorType);
            eb.setDescription(prognosticTxt);
            
            Evidence evidence = new Evidence();
            evidence.setDocuments(prognosticDocs);
            evidence.setEvidenceBlob(eb);
            eb.setEvidences(Collections.singleton(evidence));

            evidenceBlobBo.save(eb);
        }
        
        // standard therapeutic implications
        List<int[]> standardLines = extractLines(lines, prognosticLines.get(0)[1], end, STANDARD_THERAPEUTIC_IMPLICATIONS_P, NCCN_GUIDELINES_P, 1);
        parseTherapeuticImplcations(alteration, tumorType, lines, standardLines.get(0)[0], standardLines.get(0)[1],
                EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS, STANDARD_THERAPEUTIC_IMPLICATIONS_SENSITIVE_TO_P,
                STANDARD_THERAPEUTIC_IMPLICATIONS_INSENSITIVE_TO_P, STANDARD_THERAPEUTIC_IMPLICATIONS_EVIDENCE_P);
        
        // NCCN
        List<int[]> nccnLines = extractLines(lines, standardLines.get(0)[1], end, NCCN_GUIDELINES_P, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_P, 1);
        parseNCCN(alteration, tumorType, lines, nccnLines.get(0)[0], nccnLines.get(0)[1]);
        
        // Investigational therapeutic implications
        List<int[]> investigationalLines = extractLines(lines, nccnLines.get(0)[1], end, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_P, ONGOING_CLINICAL_TRIALS_P, 1);
        parseTherapeuticImplcations(alteration, tumorType, lines, investigationalLines.get(0)[0], investigationalLines.get(0)[1],
                EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_SENSITIVE_TO_P,
                INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_INSENSITIVE_TO_P, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_EVIDENCE_P);
    }
    
    private static void parseTherapeuticImplcations(Alteration alteration, TumorType tumorType, List<String> lines, int start, int end,
            EvidenceType evidenceType, String sensitivieToP, String insensitiveToP, String evidenceP) {
        List<int[]> descLines = extractLines(lines, start+1, end, evidenceP, null, 1);
        String desc = joinLines(lines, descLines.get(0)[0]+1, descLines.get(0)[1]).trim();
        if (desc.isEmpty()) {
            return;
        }
        
        EvidenceBlob eb = new EvidenceBlob();
        eb.setEvidenceType(evidenceType);
        eb.setAlteration(alteration);
        eb.setGene(alteration.getGene());
        eb.setTumorType(tumorType);
        eb.setDescription(desc);
        Set<Evidence> evidences = new HashSet<Evidence>();
        eb.setEvidences(evidences);
        
        // sensitive to
        List<int[]> sensitiveLines = extractLines(lines, start+1, end, sensitivieToP, insensitiveToP, 1);
        String sensitiveTxt = joinLines(lines, sensitiveLines.get(0)[0],sensitiveLines.get(0)[1]);
        sensitiveTxt = sensitiveTxt.substring(sensitiveTxt.indexOf(":")+1).trim();
        if (!sensitiveTxt.isEmpty()) {
            String[] drugsTxt = sensitiveTxt.split("\n");
            for (String drugTxt : drugsTxt) {
                Evidence ev = new Evidence();
                ev.setEvidenceBlob(eb);
                ev.setKnownEffect(KnownEffectOfEvidence.SENSITIVE_TO_DRUG);
                parseDrugEvidence(drugTxt, ev);
                evidences.add(ev);
            }
        }
        
        // insensitive to
        List<int[]> insensitiveLines = extractLines(lines, start+1, end, insensitiveToP, evidenceP, 1);
        String insensitiveTxt = joinLines(lines, insensitiveLines.get(0)[0],insensitiveLines.get(0)[1]);
        insensitiveTxt = insensitiveTxt.substring(insensitiveTxt.indexOf(":")+1).trim();
        if (!insensitiveTxt.isEmpty()) {
            String[] drugsTxt = insensitiveTxt.split("\n");
            for (String drugTxt : drugsTxt) {
                Evidence ev = new Evidence();
                ev.setEvidenceBlob(eb);
                ev.setKnownEffect(KnownEffectOfEvidence.INSENSITIVE_TO_DRUG);
                parseDrugEvidence(drugTxt, ev);
                evidences.add(ev);
            }
        }
        
        EvidenceBlobBo evidenceBlobBo = ApplicationContextSingleton.getEvidenceBlobBo();
        evidenceBlobBo.save(eb);
    }
    
    private static void parseNCCN(Alteration alteration, TumorType tumorType, List<String> lines, int start, int end) {
        
    }
    
    private static void parseDrugEvidence(String txt, Evidence ev) {
        Pattern p = Pattern.compile("([^\\(]+) ?\\((.+)\\)?");
        Matcher m = p.matcher(txt);
        if (!m.matches()) {
            System.err.println("Cannot process drug evidence: "+txt);
            return;
        }
        
        DocumentBo documentBo = ApplicationContextSingleton.getDocumentBo();
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();

        Evidence evidence = new Evidence();
        String drugName = m.group(1);
        Drug drug = drugBo.findDrugByName(drugName);
        if (drug==null) {
            drug = new Drug(drugName);
            drugBo.save(drug);
        }
        evidence.setDrugs(Collections.singleton(drug));
        
        if (m.groupCount()==2) {
            String[] parts = m.group(2).split("; *");
            LevelOfEvidence loe = LevelOfEvidence.getByLevel(parts[0]);
            evidence.setLevelOfEvidence(loe); // note that it could be null
            Set<Document> docs = new HashSet<Document>();
            evidence.setDocuments(docs);
            for (String part : parts) {
                if (part.startsWith("PMID")) {
                    String[] pmids = part.substring(part.indexOf(":")+1).trim().split(", *");
                    for (String pmid : pmids) {
                        Document doc = documentBo.findDocumentByPmid(pmid);
                        if (doc==null) {
                            doc = new Document(DocumentType.JOURNAL_ARTICLE);
                            doc.setPmid(pmid);
                            documentBo.save(doc);
                        }
                        docs.add(doc);
                    }
                } else if (part.startsWith("NCT")) {
                    // support NCT numbers
//                    String[] nctIds = part.split(", *");
//                    for (String nctId : nctIds) {
//                        Document doc = documentBo.
//                    }
                }
            }
        }
    }
    
    
    
    private static List<int[]> extractLines(List<String> lines, int start, int end, String startLinePatten, String endLinePattern, int limit) {
        List<int[]> indices = new ArrayList<int[]>();

        int s=start, e=start;
        
        while (s<end && e<end) {
            // find start line
            s = e;
            while (s<end && !lines.get(s).matches(startLinePatten)) {
                s++;
            }

            // find end line
            e = endLinePattern==null ? end : (s + 1);
            while (e<end && !lines.get(e).matches(endLinePattern)) {
                e++;
            }

            indices.add(new int[]{s,e});
            
            if (limit>0 && indices.size()>=limit) {
                break;
            }
        }
        
        return indices;
    }
    
    private static String joinLines(List<String> lines, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i=start; i<end; i++) {
            sb.append(lines.get(i)).append("\n");
        }
        return sb.toString();
    }
    
    private static Set<Document> extractDocuments(String str) {
        Set<Document> docs = new HashSet<Document>();
        DocumentBo documentBo = ApplicationContextSingleton.getDocumentBo();
        Pattern pmidPattern = Pattern.compile("\\(PMIDs?:([^\\)]+)\\)");
        Matcher m = pmidPattern.matcher(str);
        if (m.matches()) {
            int n = m.groupCount();
            for (int i=1; i<=n; i++) {
                String pmids = m.group(i).trim();
                for (String pmid : pmids.split(", *")) {
                    Document doc = documentBo.findDocumentByPmid(pmid);
                    if (doc==null) {
                        doc = new Document(DocumentType.JOURNAL_ARTICLE);
                        doc.setPmid(pmid);
                        documentBo.save(doc);
                    }
                    docs.add(doc);
                }
            }
        }
        return docs;
    }
}
