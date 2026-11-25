package org.mskcc.cbio.oncokb.api.mcp;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.GeneticType;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.TumorTypeUtils;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;

/**
 * Builds links to the oncokb.org gene, alteration, and cancer type pages for an annotation.
 * <p>
 * URLs follow the oncokb-public routes (getGenePageLink / getAlterationPageLink in UrlUtils.tsx):
 * <pre>
 *   /gene/{hugoSymbol}/{somatic|germline}
 *   /gene/{hugoSymbol}/{somatic|germline}/{alteration}
 *   /gene/{hugoSymbol}/{somatic|germline}/{alteration}/{cancerTypeName}
 * </pre>
 * with {@code ?refGenome=GRCh38} on alteration and cancer type pages when the query was not on the
 * default reference genome. For example:
 * https://www.oncokb.org/gene/ABL1/somatic/T315I/B-Lymphoblastic%20Leukemia%2FLymphoma
 */
final class OncoKBPageLinks {
    static final String BASE_URL = "https://www.oncokb.org";

    private OncoKBPageLinks() {
    }

    /**
     * Returns the links that apply to the annotated query, keyed by "gene", "alteration" and
     * "cancerType". A link is left out when the annotation lacks what that page needs.
     * <ul>
     *   <li>gene: only when OncoKB recognizes the gene</li>
     *   <li>alteration: also needs an alteration. Not built for structural variants, whose
     *       normalized query does not carry the fusion's page name</li>
     *   <li>cancerType: also needs a tumor type that matches an OncoTree code, subtype, or main type</li>
     * </ul>
     */
    static Map<String, String> forAnnotation(Query query, Boolean geneExist, GeneticType geneticType) {
        Map<String, String> links = new LinkedHashMap<>();
        if (query == null || !Boolean.TRUE.equals(geneExist)) {
            return links;
        }
        Gene gene = GeneUtils.getGene(query.getEntrezGeneId(), query.getHugoSymbol());
        if (gene == null || StringUtils.isBlank(gene.getHugoSymbol())) {
            return links;
        }

        String genePage = BASE_URL + "/gene/" + segment(gene.getHugoSymbol()) + "/" + geneticType.toLowerCase();
        links.put("gene", genePage);

        boolean structuralVariant = AlterationType.STRUCTURAL_VARIANT.name().equalsIgnoreCase(query.getAlterationType());
        if (structuralVariant || StringUtils.isBlank(query.getAlteration())) {
            return links;
        }
        String alterationPage = genePage + "/" + segment(query.getAlteration().trim());
        String refGenomeQuery = refGenomeQuery(query.getReferenceGenome());
        links.put("alteration", alterationPage + refGenomeQuery);

        String cancerTypeName = cancerTypeName(query.getTumorType());
        if (StringUtils.isNotBlank(cancerTypeName)) {
            links.put("cancerType", alterationPage + "/" + segment(cancerTypeName) + refGenomeQuery);
        }
        return links;
    }

    /**
     * Resolves the queried tumor type to the name the website uses in cancer type URLs. Matches an
     * OncoTree code first, then a subtype name, then a main type name, the same order
     * {@link TumorTypeUtils#findRelevantTumorTypes(String)} uses to interpret the query.
     */
    private static String cancerTypeName(String tumorType) {
        if (StringUtils.isBlank(tumorType)) {
            return null;
        }
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        String trimmed = tumorType.trim();
        TumorType matched = tumorTypeBo.getByCode(trimmed);
        if (matched == null) {
            matched = tumorTypeBo.getBySubtype(trimmed);
        }
        if (matched == null) {
            matched = tumorTypeBo.getByMainType(trimmed);
        }
        return matched == null ? null : TumorTypeUtils.getTumorTypeName(matched);
    }

    private static String refGenomeQuery(ReferenceGenome referenceGenome) {
        return referenceGenome == null || referenceGenome == DEFAULT_REFERENCE_GENOME ? "" : "?refGenome=" + referenceGenome.name();
    }

    /**
     * Percent-encodes one path segment. A "/" inside a name such as "B-Lymphoblastic
     * Leukemia/Lymphoma" becomes %2F, matching the website's encodeSlash.
     */
    private static String segment(String value) {
        try {
            return UriUtils.encodePathSegment(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 is always supported", e);
        }
    }
}
