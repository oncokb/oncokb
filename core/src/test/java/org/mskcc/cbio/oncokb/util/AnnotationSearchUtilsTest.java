package org.mskcc.cbio.oncokb.util;

import static org.mskcc.cbio.oncokb.util.AnnotationSearchUtils.annotationSearch;
import static org.mskcc.cbio.oncokb.util.AnnotationSearchUtils.searchNonHgvsAnnotation;

import java.util.Set;
import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.AnnotationSearchQueryType;
import org.mskcc.cbio.oncokb.model.AnnotationSearchResult;
import org.mskcc.cbio.oncokb.model.TypeaheadQueryType;
import org.mskcc.cbio.oncokb.model.TypeaheadSearchResp;

public class AnnotationSearchUtilsTest extends TestCase {

    public void testSearchNonHgvsAnnotation() {
        // test gene query
        Set<TypeaheadSearchResp> respSet = searchNonHgvsAnnotation("BRAF");
        // This is a gene search, there should be a gene search result
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.GENE.equals(resp.getQueryType())).count() > 0);
        // Since BRAF is mentioned in multiple alterations, variant search result should be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // BRAF is not part of any cancer type, the cancer type search result should not be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() == 0);

        // test variant query
        respSet = searchNonHgvsAnnotation("V600E");
        // This is a variant search, there should not be any gene query search result
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.GENE.equals(resp.getQueryType())).count() == 0);
        // This is a variant search, variant search result should be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // This is a variant search, the cancer type search result should not be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() == 0);

        // test fusion query
        respSet = searchNonHgvsAnnotation("BCR-ABL1");
        // This query contains gene names
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.GENE.equals(resp.getQueryType())).count() > 0);
        // This query is included in fusion name that we curated
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // This query is included in cancer type
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() > 0);

        // test cancer type query
        respSet = searchNonHgvsAnnotation("NSCLC");
        // This is a cancer type search, there should not be any gene query search result
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.GENE.equals(resp.getQueryType())).count() == 0);
        // This is a cancer type search, there should not be any variant query search result
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType())).count() == 0);
        // This is a cancer type search, the cancer type search result should be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() > 0);

        // test gene+variant query
        respSet = searchNonHgvsAnnotation("BRAF V600E");
        // This is a variant search, there should not be any gene query search result
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.GENE.equals(resp.getQueryType())).count() == 0);
        // This is a variant search, variant search result should be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.VARIANT.equals(resp.getQueryType())).count() > 0);
        // This is a variant search, the cancer type search result should not be available
        assertTrue(respSet.stream().filter(resp -> TypeaheadQueryType.CANCER_TYPE.equals(resp.getQueryType())).count() == 0);
    }

    public void testAnnotationSearch() {
        // test gene query
        Set<AnnotationSearchResult> respSet = annotationSearch("BRAF");
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
