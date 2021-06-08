package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.apiModels.ActionableGene;
import org.mskcc.cbio.oncokb.apiModels.AnnotatedVariant;
import org.mskcc.cbio.oncokb.apiModels.CuratedGene;
import org.mskcc.cbio.oncokb.apiModels.download.FileName;
import org.mskcc.cbio.oncokb.apiModels.download.FileExtension;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

import static org.mskcc.cbio.oncokb.api.pub.v1.Constants.INCLUDE_EVIDENCE;
import static org.mskcc.cbio.oncokb.api.pub.v1.Constants.VERSION;
import static org.mskcc.cbio.oncokb.util.HttpUtils.getDataDownloadResponseEntity;

/**
 * Created by Hongxin on 10/28/16.
 */
@Controller
public class UtilsApiController implements UtilsApi {
    @Autowired
    CacheFetcher cacheFetcher;

    @Override
    public ResponseEntity<List<AnnotatedVariant>> utilsAllAnnotatedVariantsGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.ALL_ANNOTATED_VARIANTS, FileExtension.JSON);
        }
        return new ResponseEntity<>(getAllAnnotatedVariants(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> utilsAllAnnotatedVariantsTxtGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.ALL_ANNOTATED_VARIANTS, FileExtension.TEXT);
        }
        String separator = "\t";
        String newLine = "\n";

        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        header.add("GRCh37 Isoform");
        header.add("GRCh37 RefSeq");
        header.add("GRCh38 Isoform");
        header.add("GRCh38 RefSeq");
        header.add("Entrez Gene ID");
        header.add("Hugo Symbol");
        header.add("Reference Genome");
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
            row.add(annotatedVariant.getGrch37Isoform());
            row.add(annotatedVariant.getGrch37RefSeq());
            row.add(annotatedVariant.getGrch38Isoform());
            row.add(annotatedVariant.getGrch38RefSeq());
            row.add(String.valueOf(annotatedVariant.getEntrezGeneId()));
            row.add(annotatedVariant.getGene());
            row.add(annotatedVariant.getReferenceGenome());
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
        Set<Gene> genes = CacheUtils.getAllGenes();
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
                    gene.getGrch37Isoform(),
                    gene.getGrch37RefSeq(),
                    gene.getGrch38Isoform(),
                    gene.getGrch38RefSeq(),
                    gene.getEntrezGeneId(),
                    gene.getHugoSymbol(),
                    biologicalVariant.getVariant().getReferenceGenomes().stream().map(referenceGenome -> referenceGenome.name()).collect(Collectors.joining(", ")),
                    biologicalVariant.getVariant().getName(),
                    biologicalVariant.getVariant().getAlteration(),
                    biologicalVariant.getOncogenic(),
                    biologicalVariant.getMutationEffect(),
                    MainUtils.listToString(new ArrayList<>(biologicalVariant.getMutationEffectPmids()), ", ", true),
                    MainUtils.listToString(abstracts, "; ", true)));
            }
        }

        annotatedVariantList.addAll(annotatedVariants);
        MainUtils.sortAnnotatedVariants(annotatedVariantList);
        return annotatedVariantList;
    }

    @Override
    public ResponseEntity<List<ActionableGene>> utilsAllActionableVariantsGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.ALL_ACTIONABLE_VARIANTS, FileExtension.JSON);
        }
        return new ResponseEntity<>(getAllActionableVariants(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> utilsAllActionableVariantsTxtGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.ALL_ACTIONABLE_VARIANTS, FileExtension.TEXT);
        }
        String separator = "\t";
        String newLine = "\n";
        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        header.add("GRCh37 Isoform");
        header.add("GRCh37 RefSeq");
        header.add("GRCh38 Isoform");
        header.add("GRCh38 RefSeq");
        header.add("Entrez Gene ID");
        header.add("Hugo Symbol");
        header.add("Reference Genome");
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
            row.add(actionableGene.getGrch37Isoform());
            row.add(actionableGene.getGrch37RefSeq());
            row.add(actionableGene.getGrch38Isoform());
            row.add(actionableGene.getGrch38RefSeq());
            row.add(String.valueOf(actionableGene.getEntrezGeneId()));
            row.add(actionableGene.getGene());
            row.add(actionableGene.getReferenceGenome());
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
        Set<Gene> genes = CacheUtils.getAllGenes();
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

                for (TumorType tumorType : clinicalVariant.getCancerTypes()) {
                    actionableGenes.add(new ActionableGene(
                        gene.getGrch37Isoform(), gene.getGrch37RefSeq(),
                        gene.getGrch38Isoform(), gene.getGrch38RefSeq(),
                        gene.getEntrezGeneId(),
                        gene.getHugoSymbol(),
                        clinicalVariant.getVariant().getReferenceGenomes().stream().map(referenceGenome -> referenceGenome.name()).collect(Collectors.joining(", ")),
                        clinicalVariant.getVariant().getName(),
                        clinicalVariant.getVariant().getAlteration(),
                        TumorTypeUtils.getTumorTypeName(tumorType),
                        clinicalVariant.getLevel(),
                        MainUtils.listToString(new ArrayList<>(clinicalVariant.getDrug()), ", ", true),
                        MainUtils.listToString(new ArrayList<>(clinicalVariant.getDrugPmids()), ", ", true),
                        MainUtils.listToString(abstracts, "; ", true)));

                }
            }
        }

        actionableGeneList.addAll(actionableGenes);
        MainUtils.sortActionableVariants(actionableGeneList);
        return actionableGeneList;
    }

    @Override
    public ResponseEntity<List<CancerGene>> utilsCancerGeneListGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.CANCER_GENE_LIST, FileExtension.JSON);
        }
        List<CancerGene> result = CancerGeneUtils.getCancerGeneList();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> utilsCancerGeneListTxtGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.CANCER_GENE_LIST, FileExtension.TEXT);
        }
        String separator = "\t";
        String newLine = "\n";
        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        header.add("Hugo Symbol");
        header.add("Entrez Gene ID");
        header.add("GRCh37 Isoform");
        header.add("GRCh37 RefSeq");
        header.add("GRCh38 Isoform");
        header.add("GRCh38 RefSeq");
        header.add("# of occurrence within resources (Column D-J)");
        header.add("OncoKB Annotated");
        header.add("Is Oncogene");
        header.add("Is Tumor Suppressor Gene");
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
            row.add(cancerGene.getEntrezGeneId().toString());
            row.add(cancerGene.getGrch37Isoform());
            row.add(cancerGene.getGrch37RefSeq());
            row.add(cancerGene.getGrch38Isoform());
            row.add(cancerGene.getGrch37RefSeq());
            row.add(String.valueOf(cancerGene.getOccurrenceCount()));
            row.add(getStringByBoolean(cancerGene.getOncokbAnnotated()));
            row.add(getStringByBoolean(cancerGene.getOncogene()));
            row.add(getStringByBoolean(cancerGene.getTSG()));
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


    @Override
    public ResponseEntity<List<CuratedGene>> utilsAllCuratedGenesGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
        , @ApiParam(value = INCLUDE_EVIDENCE, defaultValue = "TRUE") @RequestParam(value = "includeEvidence", required = false, defaultValue = "TRUE") Boolean includeEvidence
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.ALL_CURATED_GENES, FileExtension.JSON);
        }
        return new ResponseEntity<>(getCuratedGenes(includeEvidence == Boolean.TRUE), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> utilsAllCuratedGenesTxtGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
        , @ApiParam(value = INCLUDE_EVIDENCE, defaultValue = "TRUE") @RequestParam(value = "includeEvidence", required = false, defaultValue = "TRUE") Boolean includeEvidence
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.ALL_CURATED_GENES, FileExtension.TEXT);
        }
        String separator = "\t";
        String newLine = "\n";
        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        header.add("GRCh37 Isoform");
        header.add("GRCh37 RefSeq");
        header.add("GRCh38 Isoform");
        header.add("GRCh38 RefSeq");
        header.add("Entrez Gene ID");
        header.add("Hugo Symbol");
        header.add("Is Oncogene");
        header.add("Is Tumor Suppressor Gene");
        header.add("Highest Level of Evidence(sensitivity)");
        header.add("Highest Level of Evidence(resistance)");
        if (includeEvidence == Boolean.TRUE) {
            header.add("Summary");
            header.add("Background");
        }
        sb.append(MainUtils.listToString(header, separator));
        sb.append(newLine);

        List<CuratedGene> genes = getCuratedGenes(includeEvidence == Boolean.TRUE);
        for (CuratedGene gene : genes) {
            List<String> row = new ArrayList<>();
            row.add(gene.getGrch37Isoform());
            row.add(gene.getGrch37RefSeq());
            row.add(gene.getGrch38Isoform());
            row.add(gene.getGrch38RefSeq());
            row.add(String.valueOf(gene.getEntrezGeneId()));
            row.add(gene.getHugoSymbol());
            row.add(getStringByBoolean(gene.getOncogene()));
            row.add(getStringByBoolean(gene.getTSG()));
            row.add(gene.getHighestSensitiveLevel());
            row.add(gene.getHighestResistancLevel());
            if (includeEvidence == Boolean.TRUE) {
                row.add(gene.getSummary());
                row.add(gene.getBackground());
            }
            sb.append(MainUtils.listToString(row, separator));
            sb.append(newLine);
        }

        return new ResponseEntity<>(sb.toString(), HttpStatus.OK);
    }

    private static List<CuratedGene> getCuratedGenes(boolean includeEvidence) {
        List<CuratedGene> genes = new ArrayList<>();
        for (Gene gene : CacheUtils.getAllGenes()) {
            // Skip all genes without entrez gene id
            if (gene.getEntrezGeneId() == null) {
                continue;
            }

            String highestSensitiveLevel = "";
            String highestResistanceLevel = "";
            Set<Evidence> therapeuticEvidences = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, EvidenceTypeUtils.getTreatmentEvidenceTypes());
            Set<Evidence> highestSensitiveLevelEvidences = EvidenceUtils.getOnlyHighestLevelEvidences(EvidenceUtils.getSensitiveEvidences(therapeuticEvidences), null, null);
            Set<Evidence> highestResistanceLevelEvidences = EvidenceUtils.getOnlyHighestLevelEvidences(EvidenceUtils.getResistanceEvidences(therapeuticEvidences), null, null);
            if (!highestSensitiveLevelEvidences.isEmpty()) {
                highestSensitiveLevel = highestSensitiveLevelEvidences.iterator().next().getLevelOfEvidence().getLevel();
            }
            if (!highestResistanceLevelEvidences.isEmpty()) {
                highestResistanceLevel = highestResistanceLevelEvidences.iterator().next().getLevelOfEvidence().getLevel();
            }

            genes.add(
                new CuratedGene(
                    gene.getGrch37Isoform(), gene.getGrch37RefSeq(),
                    gene.getGrch38Isoform(), gene.getGrch38RefSeq(),
                    gene.getEntrezGeneId(), gene.getHugoSymbol(),
                    gene.getTSG(), gene.getOncogene(),
                    highestSensitiveLevel, highestResistanceLevel,
                    includeEvidence ? SummaryUtils.geneSummary(gene, gene.getHugoSymbol()) : "",
                    includeEvidence ? SummaryUtils.geneBackground(gene, gene.getHugoSymbol()) : ""
                )
            );
        }
        MainUtils.sortCuratedGenes(genes);
        return genes;
    }

    private String getStringByBoolean(Boolean val) {
        return val ? "Yes" : "No";
    }
}
