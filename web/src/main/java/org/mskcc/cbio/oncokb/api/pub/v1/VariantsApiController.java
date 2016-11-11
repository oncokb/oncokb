package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.mskcc.cbio.oncokb.apiModels.Meta;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.MetaUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Controller
public class VariantsApiController implements VariantsApi {

    public ResponseEntity<ApiListResp> variantsGet() {
        ApiListResp apiListResp = new ApiListResp();
        Meta meta = MetaUtils.getOKMeta();

        apiListResp.setData(new ArrayList(AlterationUtils.getAllAlterations()));
        apiListResp.setMeta(meta);

        return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.OK);
    }

    public ResponseEntity<ApiListResp> variantsLookupGet(
        @ApiParam(value = "The entrez gene ID. entrezGeneId is prioritize than hugoSymbol if both parameters have been defined") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "The gene symbol used in Human Genome Organisation.") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "variant name.") @RequestParam(value = "variant", required = false) String variant
        , @ApiParam(value = "") @RequestParam(value = "variantType", required = false) String variantType
        , @ApiParam(value = "") @RequestParam(value = "consequence", required = false) String consequence
//        , @ApiParam(value = "") @RequestParam(value = "refResidues", required = false) String refResidues
        , @ApiParam(value = "") @RequestParam(value = "proteinStart", required = false) Integer proteinStart
        , @ApiParam(value = "") @RequestParam(value = "proteinEnd", required = false) Integer proteinEnd
//        , @ApiParam(value = "") @RequestParam(value = "variantResidues", required = false) String variantResidues
    ) {
        ApiListResp apiListResp = new ApiListResp();
        Meta meta = MetaUtils.getOKMeta();

        if (hugoSymbol != null || entrezGeneId != null) {
            Gene gene = null;
            if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
                meta = MetaUtils.getBadRequestMeta("Entrez Gene ID and Hugo Symbol are not pointing to same gene.");
            } else {
                if (entrezGeneId != null) {
                    gene = GeneUtils.getGeneByEntrezId(entrezGeneId);
                }

                if (hugoSymbol != null && gene == null) {
                    gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
                }

                if (gene != null) {
                    List<Alteration> alterationList = new ArrayList<>();
                    Set<Alteration> allAlterations = AlterationUtils.getAllAlterations(gene);
                    if (variant == null && proteinStart == null && proteinEnd == null) {
                        alterationList.addAll(allAlterations);
                    } else {
                        AlterationBo alterationBo = new ApplicationContextSingleton().getAlterationBo();
                        Alteration alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), variant, variantType, consequence, proteinStart, proteinEnd);
                        alterationList = alterationBo.findRelevantAlterations(alteration, new ArrayList<Alteration>(AlterationUtils.getAllAlterations(gene)));
                    }
                    apiListResp.setData(alterationList);
                }
            }
        } else {
            meta = MetaUtils.getBadRequestMeta("Please specify entrezGeneId or hugoSymbol");
        }

        apiListResp.setMeta(meta);
        return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.OK);
    }

//    public ResponseEntity<ApiListResp> variantsVariantIdEvidencesGet(
//        @ApiParam(value = "Variant unique identifier, maintained by OncoKB. The ID may be changed.", required = true) @PathVariable("variantId") Integer variantId
//        , @ApiParam(value = "Separate by comma. Evidence type includes MUTATION_SUMMARY, ONCOGENIC, MUTATION_EFFECT, VUS") @RequestParam(value = "evidenceTypes", required = false) String evidenceTypes
//    ) {
//        // do some magic!
//        return new ResponseEntity<ApiListResp>(HttpStatus.OK);
//    }
//
//    public ResponseEntity<ApiObjectResp> variantsVariantIdGet(
//        @ApiParam(value = "Variant unique identifier, maintained by OncoKB. The ID may be changed.", required = true) @PathVariable("variantId") Integer variantId
//    ) {
//        // do some magic!
//        return new ResponseEntity<ApiObjectResp>(HttpStatus.OK);
//    }
//
//    public ResponseEntity<ApiObjectResp> variantsVariantIdTreatmentsGet(
//        @ApiParam(value = "Variant unique identifier, maintained by OncoKB. The ID may be changed.", required = true) @PathVariable("variantId") Integer variantId
//    ) {
//        // do some magic!
//        return new ResponseEntity<ApiObjectResp>(HttpStatus.OK);
//    }
//
//    public ResponseEntity<ApiListResp> variantsVariantIdTumorTypesGet(
//        @ApiParam(value = "Variant unique identifier, maintained by OncoKB. The ID may be changed.", required = true) @PathVariable("variantId") Integer variantId
//    ) {
//        // do some magic!
//        return new ResponseEntity<ApiListResp>(HttpStatus.OK);
//    }
//
//    public ResponseEntity<ApiListResp> variantsVariantIdTumorTypesOncoTreeCodeTreatmentsGet(
//        @ApiParam(value = "Variant unique identifier, maintained by OncoKB. The ID may be changed.", required = true) @PathVariable("variantId") Integer variantId
//        , @ApiParam(value = "OncoTree tumor types unique code.", required = true) @PathVariable("oncoTreeCode") String oncoTreeCode
//    ) {
//        // do some magic!
//        return new ResponseEntity<ApiListResp>(HttpStatus.OK);
//    }

}
