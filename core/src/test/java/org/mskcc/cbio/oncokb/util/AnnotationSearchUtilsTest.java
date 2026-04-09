package org.mskcc.cbio.oncokb.util;

import static org.mskcc.cbio.oncokb.util.AnnotationSearchUtils.annotationSearch;
import static org.mskcc.cbio.oncokb.util.AnnotationSearchUtils.searchCuratedAnnotation;

import java.util.Set;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.GeneticType;
import org.mskcc.cbio.oncokb.model.AnnotationSearchQueryType;
import org.mskcc.cbio.oncokb.model.InferredMutation;
import org.mskcc.cbio.oncokb.model.Pathogenicity;
import org.mskcc.cbio.oncokb.model.SomaticAnnotationSearchResult;
import org.mskcc.cbio.oncokb.model.TypeaheadQueryType;
import org.mskcc.cbio.oncokb.model.TypeaheadSearchResp;

public class AnnotationSearchUtilsTest extends TestCase {

    public void testSearchNonHgvsAnnotation() {
        // test gene query
        Set<TypeaheadSearchResp> respSet = searchCuratedAnnotation("BRAF");
        // This is a gene search, there should be a gene search result
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.GENE.equals(resp.getQueryType())).count() > 0);
        // Since BRAF is mentioned in multiple alterations, variant search result should be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // BRAF is not part of any cancer type, the cancer type search result should not be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() == 0);

        // test variant query
        respSet = searchCuratedAnnotation("V600E");
        // This is a variant search, there should not be any gene query search result
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.GENE.equals(resp.getQueryType())).count() == 0);
        // This is a variant search, variant search result should be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // This is a variant search, the cancer type search result should not be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() == 0);

        // test three-letter amino acid variant query
        respSet = searchCuratedAnnotation("Val600Glu");
        // This is a variant search, there should not be any gene query search result
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.GENE.equals(resp.getQueryType())).count() == 0);
        // This is a variant search, variant search result should be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // This is a variant search, the cancer type search result should not be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() == 0);

        // test fusion query
        respSet = searchCuratedAnnotation("BCR-ABL1");
        // This query contains gene names
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.GENE.equals(resp.getQueryType())).count() > 0);
        // This query is included in fusion name that we curated
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // This query is included in cancer type
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() > 0);

        // test cancer type query
        respSet = searchCuratedAnnotation("NSCLC");
        // This is a cancer type search, there should not be any gene query search result
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.GENE.equals(resp.getQueryType())).count() == 0);
        // This is a cancer type search, there should not be any variant query search result
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType())).count() == 0);
        // This is a cancer type search, the cancer type search result should be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() > 0);

        // test gene+variant query
        respSet = searchCuratedAnnotation("BRAF V600E");
        // This is a variant search, there should not be any gene query search result
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.GENE.equals(resp.getQueryType())).count() == 0);
        // This is a variant search, variant search result should be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // This is a variant search, the cancer type search result should not be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() == 0);

        respSet = searchCuratedAnnotation("BRCA1 patho");
        TypeaheadSearchResp germlinePathogenicVariants = respSet.stream()
            .filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType()))
            .filter(resp -> GeneticType.GERMLINE.equals(resp.getGeneticType()))
            .filter(resp -> resp.getGene() != null && "BRCA1".equals(resp.getGene().getHugoSymbol()))
            .filter(resp -> resp.getVariants() != null
                && resp.getVariants().stream().anyMatch(alt -> InferredMutation.PATHOGENIC_VARIANTS.getVariant().equals(alt.getAlteration())))
            .findFirst()
            .orElse(null);
        assertNotNull("BRCA1 germline Pathogenic Variants should be returned by typeahead.", germlinePathogenicVariants);
        assertTrue("BRCA1 germline Pathogenic Variants should include treatment levels. Actual: " + germlinePathogenicVariants.getHighestSensitiveLevel(),
            StringUtils.isNotEmpty(germlinePathogenicVariants.getHighestSensitiveLevel()));
        assertEquals("BRCA1 germline Pathogenic Variants should expose pathogenicity in typeahead.", Pathogenicity.YES.getPathogenic(), germlinePathogenicVariants.getPathogenicity());
    }

    public void testAnnotationSearch() {
        // test gene query
        Set<SomaticAnnotationSearchResult> respSet = annotationSearch("BRAF");
        // This is a gene search, there should be a gene search result
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.GENE.equals(resp.getQueryType())).count() > 0);
        // Since BRAF is mentioned in multiple alterations, variant search result should be available
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // BRAF is not part of any cancer type, the cancer type search result should not be available
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() == 0);

        // test variant query
        respSet = annotationSearch("V600E");
        // This is a variant search, there should not be any gene query search result
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.GENE.equals(resp.getQueryType())).count() == 0);
        // This is a variant search, variant search result should be available
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // This is a variant search, the cancer type search result should not be available
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() == 0);

        // test three-letter amino acid variant query
        respSet = annotationSearch("Val600Glu");
        // This is a variant search, there should not be any gene query search result
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.GENE.equals(resp.getQueryType())).count() == 0);
        // This is a variant search, variant search result should be available
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // This is a variant search, the cancer type search result should not be available
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() == 0);

        // test fusion query
        respSet = annotationSearch("BCR-ABL1");
        // This query contains gene names
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.GENE.equals(resp.getQueryType())).count() > 0);
        // This query is included in fusion name that we curated
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // This query is included in cancer type
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() > 0);

        // test cancer type query
        respSet = annotationSearch("NSCLC");
        // This is a cancer type search, there should not be any gene query search result
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.GENE.equals(resp.getQueryType())).count() == 0);
        // This is a cancer type search, there should not be any variant query search result
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.VARIANT.equals(resp.getQueryType())).count() == 0);
        // This is a cancer type search, the cancer type search result should be available
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() > 0);

        // test gene+variant query
        respSet = annotationSearch("BRAF V600E");
        // This is a variant search, there should not be any gene query search result
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.GENE.equals(resp.getQueryType())).count() == 0);
        // This is a variant search, variant search result should be available
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // This is a variant search, the cancer type search result should not be available
        assertTrue(respSet.stream().filter(resp -> AnnotationSearchQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() == 0);
    }
}
