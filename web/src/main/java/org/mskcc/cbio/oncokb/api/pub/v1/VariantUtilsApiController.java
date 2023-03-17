package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
import org.mskcc.cbio.oncokb.apiModels.AnnotatedVariant;
import org.mskcc.cbio.oncokb.apiModels.download.FileExtension;
import org.mskcc.cbio.oncokb.apiModels.download.FileName;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

import static org.mskcc.cbio.oncokb.api.pub.v1.Constants.VERSION;
import static org.mskcc.cbio.oncokb.util.HttpUtils.getDataDownloadResponseEntity;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;

@Api(tags = "Variants", description = "Endpoints related to OncoKB variants")
@Controller
public class VariantUtilsApiController {
    @Autowired
    CacheFetcher cacheFetcher;

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get All Annotated Variants.", response = AnnotatedVariant.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AnnotatedVariant.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allAnnotatedVariants", produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<List<AnnotatedVariant>> utilsAllAnnotatedVariantsGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.ALL_ANNOTATED_VARIANTS, FileExtension.JSON);
        }
        return new ResponseEntity<>(getAllAnnotatedVariants(), HttpStatus.OK);
    }

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get All Annotated Variants in text file.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allAnnotatedVariants.txt",
        produces = TEXT_PLAIN_VALUE,
        method = RequestMethod.GET)
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

}
