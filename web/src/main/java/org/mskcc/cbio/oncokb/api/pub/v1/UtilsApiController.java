package org.mskcc.cbio.oncokb.api.pub.v1;

import org.mskcc.cbio.oncokb.apiModels.ActionableGene;
import org.mskcc.cbio.oncokb.apiModels.AnnotatedVariant;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.oncotree.TumorType;
import org.mskcc.cbio.oncokb.util.CancerGeneUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.*;

/**
 * Created by Hongxin on 10/28/16.
 */
@Controller
public class UtilsApiController implements UtilsApi {

    @Override
    public ResponseEntity<List<AnnotatedVariant>> utilsAllAnnotatedVariantsGet() {
        return new ResponseEntity<>(getAllAnnotatedVariants(), HttpStatus.OK);

    }

    @Override
    public ResponseEntity<String> utilsAllAnnotatedVariantsTxtGet() {
        String separator = "\t";
        String newLine = "\n";

        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        header.add("Isoform");
        header.add("RefSeq");
        header.add("Entrez Gene ID");
        header.add("Gene");
        header.add("Alteration");
        header.add("Protein Change");
        header.add("Oncogenicity");
        header.add("Mutation Effect");
        header.add("PMIDs for Mutation Effect");
        header.add("Abstracts for Mutation Effect");
        sb.append(MainUtils.listToString(header, separator));
        sb.append(newLine);

        for (AnnotatedVariant annotatedVariant : getAllAnnotatedVariants()) {
            List<String> row = new ArrayList<>();
            row.add(annotatedVariant.getIsoform());
            row.add(annotatedVariant.getRefSeq());
            row.add(String.valueOf(annotatedVariant.getEntrezGeneId()));
            row.add(annotatedVariant.getGene());
            row.add(annotatedVariant.getVariant());
            row.add(annotatedVariant.getProteinChange());
            row.add(annotatedVariant.getOncogenicity());
            row.add(annotatedVariant.getMutationEffect());
            row.add(annotatedVariant.getMutationEffectPmids());
            row.add(annotatedVariant.getMutationEffectAbstracts());
            sb.append(MainUtils.listToString(row, separator));
            sb.append(newLine);
        }
        return new ResponseEntity<>(sb.toString(), HttpStatus.OK);
    }

    private List<AnnotatedVariant> getAllAnnotatedVariants() {
        List<AnnotatedVariant> annotatedVariantList = new ArrayList<>();
        Set<Gene> genes = GeneUtils.getAllGenes();
        Map<Gene, Set<BiologicalVariant>> map = new HashMap<>();

        for (Gene gene : genes) {
            map.put(gene, MainUtils.getBiologicalVariants(gene));
        }

        Set<AnnotatedVariant> annotatedVariants = new HashSet<>();
        for (Map.Entry<Gene, Set<BiologicalVariant>> entry : map.entrySet()) {
            Gene gene = entry.getKey();
            for (BiologicalVariant biologicalVariant : entry.getValue()) {
                Set<ArticleAbstract> articleAbstracts = biologicalVariant.getMutationEffectAbstracts();
                List<String> abstracts = new ArrayList<>();
                for (ArticleAbstract articleAbstract : articleAbstracts) {
                    abstracts.add(articleAbstract.getAbstractContent() + " " + articleAbstract.getLink());
                }
                annotatedVariants.add(new AnnotatedVariant(
                    gene.getCuratedIsoform(), gene.getCuratedRefSeq(), gene.getEntrezGeneId(),
                    gene.getHugoSymbol(), biologicalVariant.getVariant().getName(),
                    biologicalVariant.getVariant().getAlteration(),
                    biologicalVariant.getOncogenic(),
                    biologicalVariant.getMutationEffect(),
                    MainUtils.listToString(new ArrayList<>(biologicalVariant.getMutationEffectPmids()), ", "),
                    MainUtils.listToString(abstracts, "; ")));
            }
        }

        annotatedVariantList.addAll(annotatedVariants);
        MainUtils.sortAnnotatedVariants(annotatedVariantList);
        return annotatedVariantList;
    }

    @Override
    public ResponseEntity<List<ActionableGene>> utilsAllActionableVariantsGet() {
        return new ResponseEntity<>(getAllActionableVariants(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> utilsAllActionableVariantsTxtGet() {
        String separator = "\t";
        String newLine = "\n";
        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        header.add("Isoform");
        header.add("RefSeq");
        header.add("Entrez Gene ID");
        header.add("Gene");
        header.add("Alteration");
        header.add("Protein Change");
        header.add("Cancer Type");
        header.add("Level");
        header.add("Drugs(s)");
        header.add("PMIDs for drug");
        header.add("Abstracts for drug");
        sb.append(MainUtils.listToString(header, separator));
        sb.append(newLine);

        for (ActionableGene actionableGene : getAllActionableVariants()) {
            List<String> row = new ArrayList<>();
            row.add(actionableGene.getIsoform());
            row.add(actionableGene.getRefSeq());
            row.add(String.valueOf(actionableGene.getEntrezGeneId()));
            row.add(actionableGene.getGene());
            row.add(actionableGene.getVariant());
            row.add(actionableGene.getProteinChange());
            row.add(actionableGene.getCancerType());
            row.add(actionableGene.getLevel());
            row.add(actionableGene.getDrugs());
            row.add(actionableGene.getPmids());
            row.add(actionableGene.getAbstracts());
            sb.append(MainUtils.listToString(row, separator));
            sb.append(newLine);
        }
        return new ResponseEntity<>(sb.toString(), HttpStatus.OK);
    }

    private List<ActionableGene> getAllActionableVariants() {
        List<ActionableGene> actionableGeneList = new ArrayList<>();
        Set<Gene> genes = GeneUtils.getAllGenes();
        Map<Gene, Set<ClinicalVariant>> map = new HashMap<>();

        for (Gene gene : genes) {
            map.put(gene, MainUtils.getClinicalVariants(gene));
        }

        Set<ActionableGene> actionableGenes = new HashSet<>();
        for (Map.Entry<Gene, Set<ClinicalVariant>> entry : map.entrySet()) {
            Gene gene = entry.getKey();
            for (ClinicalVariant clinicalVariant : entry.getValue()) {
                Set<ArticleAbstract> articleAbstracts = clinicalVariant.getDrugAbstracts();
                List<String> abstracts = new ArrayList<>();
                for (ArticleAbstract articleAbstract : articleAbstracts) {
                    abstracts.add(articleAbstract.getAbstractContent() + " " + articleAbstract.getLink());
                }

                actionableGenes.add(new ActionableGene(
                    gene.getCuratedIsoform(), gene.getCuratedRefSeq(), gene.getEntrezGeneId(),
                    gene.getHugoSymbol(),
                    clinicalVariant.getVariant().getName(),
                    clinicalVariant.getVariant().getAlteration(),
                    getCancerType(clinicalVariant.getOncoTreeType()),
                    clinicalVariant.getLevel(),
                    MainUtils.listToString(new ArrayList<>(clinicalVariant.getDrug()), ", "),
                    MainUtils.listToString(new ArrayList<>(clinicalVariant.getDrugPmids()), ", "),
                    MainUtils.listToString(abstracts, "; ")));
            }
        }

        actionableGeneList.addAll(actionableGenes);
        MainUtils.sortActionableVariants(actionableGeneList);
        return actionableGeneList;
    }

    private String getCancerType(TumorType oncoTreeType) {
        return oncoTreeType == null ? null : (
            oncoTreeType.getName() == null ?
                (oncoTreeType.getMainType() == null ? null : oncoTreeType.getMainType().getName()) :
                oncoTreeType.getName());
    }

    @Override
    public ResponseEntity<List<CancerGene>> utilsCancerGeneListGet() {
        List<CancerGene> result = CancerGeneUtils.getCancerGeneList();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> utilsCancerGeneListTxtGet() {
        String separator = "\t";
        String newLine = "\n";
        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        header.add("Hugo Symbol");
        header.add("Entrez Gene ID");
        header.add("# of occurrence within resources (Column D-J)");
        header.add("OncoKB Annotated");
        header.add("MSK-IMPACT");
        header.add("MSK-HEME");
        header.add("FOUNDATION ONE");
        header.add("FOUNDATION ONE HEME");
        header.add("Vogelstein");
        header.add("SANGER CGC(05/30/2017)");
        sb.append(MainUtils.listToString(header, separator));
        sb.append(newLine);

        for (CancerGene cancerGene : CancerGeneUtils.getCancerGeneList()) {
            List<String> row = new ArrayList<>();
            row.add(cancerGene.getHugoSymbol());
            row.add(cancerGene.getEntrezGeneId());
            row.add(String.valueOf(cancerGene.getOccurrenceCount()));
            row.add(getStringByBoolean(cancerGene.getOncokbAnnotated()));
            row.add(getStringByBoolean(cancerGene.getmSKImpact()));
            row.add(getStringByBoolean(cancerGene.getmSKHeme()));
            row.add(getStringByBoolean(cancerGene.getFoundation()));
            row.add(getStringByBoolean(cancerGene.getFoundationHeme()));
            row.add(getStringByBoolean(cancerGene.getVogelstein()));
            row.add(getStringByBoolean(cancerGene.getSangerCGC()));
            sb.append(MainUtils.listToString(row, separator));
            sb.append(newLine);
        }
        return new ResponseEntity<>(sb.toString(), HttpStatus.OK);
    }

    private String getStringByBoolean(Boolean val) {
        return val ? "Yes" : "No";
    }
}
