package org.mskcc.cbio.oncokb.api.pub.v1;

import org.mskcc.cbio.oncokb.apiModels.ActionableGene;
import org.mskcc.cbio.oncokb.apiModels.AnnotatedVariant;
import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.mskcc.cbio.oncokb.apiModels.Meta;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.mskcc.cbio.oncokb.util.MetaUtils;
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
    public ResponseEntity<ApiListResp> utilsAllAnnotatedVariantsGet() {
        ApiListResp apiListResp = new ApiListResp();
        Meta meta = MetaUtils.getOKMeta();
        HttpStatus status = HttpStatus.OK;

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
                    gene.getHugoSymbol(), biologicalVariant.getVariant().getName(), biologicalVariant.getOncogenic(),
                    biologicalVariant.getMutationEffect(),
                    MainUtils.listToString(new ArrayList<>(biologicalVariant.getMutationEffectPmids()), ", "),
                    MainUtils.listToString(abstracts, "; ")));
            }
        }

        apiListResp.setData(new ArrayList(annotatedVariants));
        apiListResp.setMeta(meta);
        return new ResponseEntity<>(apiListResp, status);

    }

    @Override
    public ResponseEntity<String> utilsAllAnnotatedVariantsTxtGet() {
        Set<Gene> genes = GeneUtils.getAllGenes();
        Map<Gene, Set<BiologicalVariant>> map = new HashMap<>();

        for (Gene gene : genes) {
            map.put(gene, MainUtils.getBiologicalVariants(gene));
        }

        String separator = "\t";
        String newLine = "\n";

        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        header.add("Gene");
        header.add("Alteration");
        header.add("Oncogenicity");
        header.add("Mutation Effect");
        header.add("PMIDs for Mutation Effect");
        header.add("Abstracts for Mutation Effect");
        sb.append(MainUtils.listToString(header, separator));
        sb.append(newLine);

        for (Map.Entry<Gene, Set<BiologicalVariant>> entry : map.entrySet()) {
            Gene gene = entry.getKey();
            for (BiologicalVariant biologicalVariant : entry.getValue()) {
                List<String> row = new ArrayList<>();
                row.add(gene.getHugoSymbol());
                row.add(biologicalVariant.getVariant().getName());
                row.add(biologicalVariant.getOncogenic());
                row.add(biologicalVariant.getMutationEffect());
                row.add(MainUtils.listToString(new ArrayList<>(biologicalVariant.getMutationEffectPmids()), ", "));
                Set<ArticleAbstract> articleAbstracts = biologicalVariant.getMutationEffectAbstracts();
                List<String> abstracts = new ArrayList<>();
                for (ArticleAbstract articleAbstract : articleAbstracts) {
                    abstracts.add(articleAbstract.getAbstractContent() + " " + articleAbstract.getLink());
                }
                row.add(MainUtils.listToString(abstracts, "; "));
                sb.append(MainUtils.listToString(row, separator));
                sb.append(newLine);
            }
        }
        return new ResponseEntity<>(sb.toString(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiListResp> utilsAllActionableVariantsGet() {
        ApiListResp apiListResp = new ApiListResp();
        Meta meta = MetaUtils.getOKMeta();
        HttpStatus status = HttpStatus.OK;

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
                    gene.getHugoSymbol(), clinicalVariant.getVariant().getName(),
                    getCancerType(clinicalVariant.getOncoTreeType()),
                    clinicalVariant.getLevel(),
                    MainUtils.listToString(new ArrayList<>(clinicalVariant.getDrug()), ", "),
                    MainUtils.listToString(new ArrayList<>(clinicalVariant.getDrugPmids()), ", "),
                    MainUtils.listToString(abstracts, "; ")));
            }
        }

        apiListResp.setData(new ArrayList(actionableGenes));
        apiListResp.setMeta(meta);
        return new ResponseEntity<>(apiListResp, status);
    }

    @Override
    public ResponseEntity<String> utilsAllActionableVariantsTxtGet() {
        Set<Gene> genes = GeneUtils.getAllGenes();
        Map<Gene, Set<ClinicalVariant>> map = new HashMap<>();

        for (Gene gene : genes) {
            map.put(gene, MainUtils.getClinicalVariants(gene));
        }

        String separator = "\t";
        String newLine = "\n";
        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        header.add("Gene");
        header.add("Alteration");
        header.add("Cancer Type");
        header.add("Level");
        header.add("Drugs(s)");
        header.add("PMIDs for drug");
        header.add("Abstracts for drug");
        sb.append(MainUtils.listToString(header, separator));
        sb.append(newLine);

        for (Map.Entry<Gene, Set<ClinicalVariant>> entry : map.entrySet()) {
            Gene gene = entry.getKey();
            for (ClinicalVariant clinicalVariant : entry.getValue()) {
                List<String> row = new ArrayList<>();
                row.add(gene.getHugoSymbol());
                row.add(clinicalVariant.getVariant().getName());
                row.add(getCancerType(clinicalVariant.getOncoTreeType()));
                row.add(clinicalVariant.getLevel());
                row.add(MainUtils.listToString(new ArrayList<>(clinicalVariant.getDrug()), ", "));
                row.add(MainUtils.listToString(new ArrayList<>(clinicalVariant.getDrugPmids()), ", "));
                Set<ArticleAbstract> articleAbstracts = clinicalVariant.getDrugAbstracts();
                List<String> abstracts = new ArrayList<>();
                for (ArticleAbstract articleAbstract : articleAbstracts) {
                    abstracts.add(articleAbstract.getAbstractContent() + " " + articleAbstract.getLink());
                }
                row.add(MainUtils.listToString(abstracts, "; "));
                sb.append(MainUtils.listToString(row, separator));
                sb.append(newLine);
            }
        }
        return new ResponseEntity<>(sb.toString(), HttpStatus.OK);
    }

    private String getCancerType(OncoTreeType oncoTreeType) {
        return oncoTreeType == null ? null : (
            oncoTreeType.getSubtype() == null ?
                oncoTreeType.getCancerType() :
                oncoTreeType.getSubtype());
    }
}
