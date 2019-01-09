
package org.mskcc.cbio.oncokb.importer;

import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.ArticleBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;

import java.io.IOException;
import java.util.*;

/**
 * @author jgao
 */
public class AlterationActivityEvidenceImporter {
    private AlterationActivityEvidenceImporter() {
        throw new AssertionError();
    }

    private static final String ALTERATION_ACTIVITY_EVIDENCE_FILE = "/data/alteration-activity-evidence.txt";

    public static void main(String[] args) throws IOException {
        VariantConsequenceImporter.main(args);
        List<String> lines = FileUtils.readTrimedLinesStream(
            AlterationActivityEvidenceImporter.class.getResourceAsStream(ALTERATION_ACTIVITY_EVIDENCE_FILE));
        String[] headers = lines.get(0).split("\t");

        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        ArticleBo articleBo = ApplicationContextSingleton.getArticleBo();
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        int nLines = lines.size();
        System.out.println("importing...");
        for (int i = 1; i < nLines; i++) {
            String line = lines.get(i);
            String[] parts = line.split("\t");

            EvidenceType evidenceType = EvidenceType.MUTATION_EFFECT;
            String hugo = parts[0];
            String alt = parts[1];
            String effect = parts[2];
            String desc = parts[3];
            String context = parts[4];
            String comments = parts[5];
            Set<String> pmids = new HashSet<String>(Arrays.asList(parts[6].split(", *")));
//            String tumorType = "";//parts[7];
            AlterationType type = AlterationType.valueOf(parts[8]);

            Gene gene = geneBo.findGeneByHugoSymbol(hugo);
            if (gene == null) {
                System.out.println("Could not find gene " + hugo + ". Loading from MyGene.Info...");
                gene = GeneAnnotatorMyGeneInfo2.readByHugoSymbol(hugo);
                if (gene == null) {
                    System.out.println("Could not find gene " + hugo + " either. skip.");
                    continue;
                }
                geneBo.save(gene);
            }

            Alteration alteration = alterationBo.findAlteration(gene, type, alt);
            if (alteration == null) {
                alteration = new Alteration();
                alteration.setGene(gene);
                alteration.setAlterationType(type);
                alteration.setAlteration(alt);
                AlterationUtils.annotateAlteration(alteration, alt);

                alterationBo.save(alteration);
            }

            Set<Article> docs = new HashSet<Article>(pmids.size());
            Set<String> docsToBeAdded = new HashSet<>();
            for (String pmid : pmids) {
                Article doc = articleBo.findArticleByPmid(pmid);
                if (doc == null) {
                    docsToBeAdded.add(pmid);
                } else {
                    docs.add(doc);
                }
            }

            if (!docsToBeAdded.isEmpty()) {
                for (Article article : NcbiEUtils.readPubmedArticles(docsToBeAdded)) {
                    articleBo.save(article);
                    docs.add(article);
                }
            }

            Evidence evidence = new Evidence();
            evidence.setDescription(desc);
            evidence.setEvidenceType(evidenceType);
            evidence.setGene(gene);
            evidence.setAlterations(Collections.singleton(alteration));
            evidence.setCancerType(null);
            evidence.setSubtype(null);
            evidence.setKnownEffect(effect);
            evidence.setArticles(docs);

            evidenceBo.save(evidence);
        }

    }
}
