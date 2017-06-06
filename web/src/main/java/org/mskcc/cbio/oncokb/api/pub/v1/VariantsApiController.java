package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.GeneUtils;
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

    public ResponseEntity<List<Alteration>> variantsGet() {
        List<Alteration> alterations = new ArrayList(AlterationUtils.getAllAlterations());

        return new ResponseEntity<>(alterations, HttpStatus.OK);
    }

    public ResponseEntity<List<Alteration>> variantsLookupGet(
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
        HttpStatus httpStatus = HttpStatus.OK;
        List<Alteration> alterationList = new ArrayList<>();
        if (hugoSymbol != null || entrezGeneId != null) {
            Gene gene = GeneUtils.getGene(entrezGeneId, hugoSymbol);
            if (gene != null) {
                Set<Alteration> allAlterations = AlterationUtils.getAllAlterations(gene);
                if (variant == null && proteinStart == null && proteinEnd == null) {
                    alterationList.addAll(allAlterations);
                } else {
                    AlterationBo alterationBo = new ApplicationContextSingleton().getAlterationBo();
                    Alteration alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), variant, variantType, consequence, proteinStart, proteinEnd);
                    alterationList.addAll(alterationBo.findRelevantAlterations(alteration, new ArrayList<Alteration>(AlterationUtils.getAllAlterations(gene))));
                }
            }
        } else if (variant != null) {
            alterationList = AlterationUtils.lookupVarinat(variant, false, AlterationUtils.getAllAlterations());
        }

        return new ResponseEntity<>(alterationList, HttpStatus.OK);
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
