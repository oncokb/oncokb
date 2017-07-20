package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.VariantSearchQuery;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
        , @ApiParam(value = "") @RequestParam(value = "proteinStart", required = false) Integer proteinStart
        , @ApiParam(value = "") @RequestParam(value = "proteinEnd", required = false) Integer proteinEnd
    ) {
        VariantSearchQuery query = new VariantSearchQuery(entrezGeneId, hugoSymbol, variant, variantType, consequence, proteinStart, proteinEnd);
        return new ResponseEntity<>(getVariants(query), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<List<Alteration>>> variantsLookupPost(@ApiParam(value = "List of queries.", required = true) @RequestBody(required = true) List<VariantSearchQuery> body) {
        List<List<Alteration>> result = new ArrayList<>();
        if (body != null) {
            for (VariantSearchQuery query : body) {
                result.add(getVariants(query));
            }
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private List<Alteration> getVariants(VariantSearchQuery query) {
        LinkedHashSet<Alteration> alterationSet = new LinkedHashSet<>();
        List<Alteration> alterationList = new ArrayList<>();
        if (query != null) {
            if (query.getHugoSymbol() != null || query.getEntrezGeneId() != null) {
                Gene gene = GeneUtils.getGene(query.getEntrezGeneId(), query.getHugoSymbol());
                if (gene != null) {
                    if (AlterationUtils.isInferredAlterations(query.getVariant())) {
                        alterationSet.addAll(AlterationUtils.getAlterationsByKnownEffectInGene(gene, AlterationUtils.getInferredAlterationsKnownEffect(query.getVariant()), true));
                    } else {
                        Set<Alteration> allAlterations = AlterationUtils.getAllAlterations(gene);
                        if (query.getVariant() == null && query.getProteinStart() == null && query.getProteinEnd() == null) {
                            alterationSet.addAll(allAlterations);
                        } else {
                            AlterationBo alterationBo = new ApplicationContextSingleton().getAlterationBo();
                            List<Alteration> alterations = AlterationUtils.lookupVariant(query.getVariant(), true, allAlterations);

                            // If this variant is not annotated
                            if (alterations == null || alterations.isEmpty()) {
                                Alteration alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getVariant(), query.getVariantType(), query.getConsequence(), query.getProteinStart(), query.getProteinEnd());
                                if (alteration != null) {
                                    alterations.add(alteration);
                                }
                            }
                            for (Alteration alteration : alterations) {
                                alterationSet.addAll(alterationBo.findRelevantAlterations(alteration, new ArrayList<Alteration>(allAlterations)));
                            }
                        }
                    }
                }
            } else if (query.getVariant() != null) {
                if (AlterationUtils.isInferredAlterations(query.getVariant())) {
                    for (Gene gene : GeneUtils.getAllGenes()) {
                        alterationSet.addAll(AlterationUtils.getAlterationsByKnownEffectInGene(gene, AlterationUtils.getInferredAlterationsKnownEffect(query.getVariant()), true));
                    }
                } else {
                    alterationList = AlterationUtils.lookupVariant(query.getVariant(), false, AlterationUtils.getAllAlterations());
                }
            }
        }

        alterationList.addAll(alterationSet);
        return alterationList;
    }

}
