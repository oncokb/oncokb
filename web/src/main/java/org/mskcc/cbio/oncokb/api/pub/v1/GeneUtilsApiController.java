package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
import org.mskcc.cbio.oncokb.apiModels.ActionableGene;
import org.mskcc.cbio.oncokb.apiModels.ActionableGeneEvidence;
import org.mskcc.cbio.oncokb.apiModels.CuratedGene;
import org.mskcc.cbio.oncokb.apiModels.download.FileName;
import org.mskcc.cbio.oncokb.apiModels.download.FileExtension;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.oncokb.oncokb_transcript.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.mskcc.cbio.oncokb.api.pub.v1.Constants.INCLUDE_EVIDENCE;
import static org.mskcc.cbio.oncokb.api.pub.v1.Constants.VERSION;
import static org.mskcc.cbio.oncokb.util.HttpUtils.getDataDownloadResponseEntity;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;

@Api(tags = "Cancer Genes", description = "OncoKB gene relevant info. Cancer genes, curated genes, actionable genes.")
@Controller
public class GeneUtilsApiController {
    @Autowired
    CacheFetcher cacheFetcher;

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Find actionable gene evidences.", response = ActionableGeneEvidence.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ActionableGeneEvidence.class, responseContainer = "List"),
    })
    @RequestMapping(value = "/utils/actionableGeneEvidences", produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<List<ActionableGeneEvidence>> utilsActionableGenesGet(
        @ApiParam(value = "Gene entrez gene id or hugo symbol. Example: BRAF") @RequestParam(value = "gene", required = false) String gene
        , @ApiParam(value = "Level of evidence, see allowed levels at https://www.oncokb.org/api/v1/info. Example: LEVEL_1") @RequestParam(value = "level", required = false) String level
        , @ApiParam(value = "OncoTree(https://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = "Exact drug name. Example: Dabrafenib") @RequestParam(value = "drug", required = false) String drug
    ) throws Exception {
        List<Evidence> filteredEvidences = new ArrayList<>();
        Map<LevelOfEvidence, Set<Evidence>> levelEvidences = EvidenceUtils.getEvidencesByLevels();
        if (StringUtils.isEmpty(level)) {
            for (Map.Entry<LevelOfEvidence, Set<Evidence>> entry : levelEvidences.entrySet()) {
                filteredEvidences.addAll(entry.getValue());
            }
        } else {
            LevelOfEvidence levelOfEvidence = LevelOfEvidence.getByName(level);
            if (levelOfEvidence == null) {
                throw new Exception("Level is not supported");
            } else {
                if (levelEvidences.containsKey(levelOfEvidence)) {
                    filteredEvidences = new ArrayList<>(levelEvidences.get(levelOfEvidence));
                } else {
                    filteredEvidences = new ArrayList<>();
                }
            }
        }
        if (!StringUtils.isEmpty(gene)) {
            LinkedHashSet<Gene> genes = GeneUtils.searchGene(gene, true);
            if (genes.size() == 0) {
                filteredEvidences = new ArrayList<>();
            } else {
                filteredEvidences = filteredEvidences.stream().filter(evidence -> genes.iterator().next().equals(evidence.getGene())).collect(Collectors.toList());
            }
        }

        if (!StringUtils.isEmpty(tumorType)) {
            TumorType matchedTumorType = ApplicationContextSingleton.getTumorTypeBo().getByName(tumorType);
            if (matchedTumorType == null) {
                throw new Exception("Cannot find matched tumor type");
            }
            filteredEvidences = filteredEvidences.stream().filter(evidence -> evidence.getRelevantCancerTypes().contains(matchedTumorType)).collect(Collectors.toList());
        }

        if (!StringUtils.isEmpty(drug)) {
            Set<Drug> drugs = DrugUtils.getDrugsByNames(Collections.singleton(drug), true);
            if (drugs.size() > 1) {
                throw new Exception("Multi entries for the drug name");
            } else if (drugs.size() == 0) {
                filteredEvidences = new ArrayList<>();
            } else {
                Drug matchedDrug = drugs.iterator().next();
                filteredEvidences = filteredEvidences.stream().filter(evidence -> EvidenceUtils.getDrugs(Collections.singleton(evidence)).contains(matchedDrug)).collect(Collectors.toList());
            }
        }
        filteredEvidences.stream().forEach(evidence -> {
            evidence.setId(null);
            evidence.setUuid(null);
            evidence.setRelevantCancerTypes(new HashSet<>());
        });
        return new ResponseEntity<>(filteredEvidences.stream().map(evidence -> {
            ActionableGeneEvidence actionableGeneEvidence = new ActionableGeneEvidence();
            actionableGeneEvidence.setGene(evidence.getGene());
            actionableGeneEvidence.setAlterations(evidence.getAlterations());
            actionableGeneEvidence.setLevelOfEvidence(evidence.getLevelOfEvidence());
            actionableGeneEvidence.setFdaLevel(evidence.getFdaLevel());
            actionableGeneEvidence.setCancerTypes(evidence.getCancerTypes());
            actionableGeneEvidence.setExcludedCancerTypes(evidence.getExcludedCancerTypes());
            actionableGeneEvidence.setTreatments(evidence.getTreatments());
            actionableGeneEvidence.setCitations(MainUtils.getCitationsByEvidence(evidence));
            return actionableGeneEvidence;
        }).collect(Collectors.toList()), HttpStatus.OK);
    }

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get all actionable genes.", response = ActionableGene.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ActionableGene.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allActionableGenes", produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<List<ActionableGene>> utilsAllActionableGenesGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.ALL_ACTIONABLE_GENES, FileExtension.JSON);
        }
        return new ResponseEntity<>(getAllActionableGenes(), HttpStatus.OK);
    }

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get all actionable genes in text file.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allActionableGenes.txt",
        produces = TEXT_PLAIN_VALUE,
        method = RequestMethod.GET)
    public ResponseEntity<String> utilsAllActionableGenesTxtGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.ALL_ACTIONABLE_GENES, FileExtension.TEXT);
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

        for (ActionableGene actionableGene : getAllActionableGenes()) {
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

    private List<ActionableGene> getAllActionableGenes() {
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

                if (clinicalVariant.getExcludedCancerTypes().size() > 0) {
                    // for any clinical variant that has cancer type excluded, we no longer list the cancer types separately
                    actionableGenes.add(new ActionableGene(
                        gene.getGrch37Isoform(), gene.getGrch37RefSeq(),
                        gene.getGrch38Isoform(), gene.getGrch38RefSeq(),
                        gene.getEntrezGeneId(),
                        gene.getHugoSymbol(),
                        clinicalVariant.getVariant().getReferenceGenomes().stream().map(referenceGenome -> referenceGenome.name()).collect(Collectors.joining(", ")),
                        clinicalVariant.getVariant().getName(),
                        clinicalVariant.getVariant().getAlteration(),
                        TumorTypeUtils.getTumorTypesNameWithExclusion(clinicalVariant.getCancerTypes(), clinicalVariant.getExcludedCancerTypes()),
                        clinicalVariant.getLevel(),
                        MainUtils.listToString(new ArrayList<>(clinicalVariant.getDrug()), ", ", true),
                        MainUtils.listToString(new ArrayList<>(clinicalVariant.getDrugPmids()), ", ", true),
                        MainUtils.listToString(abstracts, "; ", true))
                    );
                } else {
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
                            MainUtils.listToString(abstracts, "; ", true))
                        );
                    }
                }
            }
        }

        actionableGeneList.addAll(actionableGenes);
        MainUtils.sortActionableGenes(actionableGeneList);
        return actionableGeneList;
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get cancer gene list")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/cancerGeneList",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<List<CancerGene>> utilsCancerGeneListGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) throws ApiException, IOException {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.CANCER_GENE_LIST, FileExtension.JSON);
        }
        List<CancerGene> result = this.cacheFetcher.getCancerGenes();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get cancer gene list in text file.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/cancerGeneList.txt",
        produces = TEXT_PLAIN_VALUE,
        method = RequestMethod.GET)
    public ResponseEntity<String> utilsCancerGeneListTxtGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) throws ApiException, IOException {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.CANCER_GENE_LIST, FileExtension.TEXT);
        }
        return new ResponseEntity<>(this.cacheFetcher.getCancerGenesTxt(), HttpStatus.OK);
    }


    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get list of genes OncoKB curated")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allCuratedGenes",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<List<CuratedGene>> utilsAllCuratedGenesGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
        , @ApiParam(value = INCLUDE_EVIDENCE, defaultValue = "TRUE") @RequestParam(value = "includeEvidence", required = false, defaultValue = "TRUE") Boolean includeEvidence
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.ALL_CURATED_GENES, FileExtension.JSON);
        }
        return new ResponseEntity<>(this.cacheFetcher.getCuratedGenes(includeEvidence), HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get list of genes OncoKB curated in text file.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allCuratedGenes.txt",
        produces = TEXT_PLAIN_VALUE,
        method = RequestMethod.GET)
    public ResponseEntity<String> utilsAllCuratedGenesTxtGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
        , @ApiParam(value = INCLUDE_EVIDENCE, defaultValue = "TRUE") @RequestParam(value = "includeEvidence", required = false, defaultValue = "TRUE") Boolean includeEvidence
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.ALL_CURATED_GENES, FileExtension.TEXT);
        }
        return new ResponseEntity<>(this.cacheFetcher.getCuratedGenesTxt(includeEvidence), HttpStatus.OK);
    }

}
