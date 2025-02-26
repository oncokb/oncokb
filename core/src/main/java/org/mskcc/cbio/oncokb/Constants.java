package org.mskcc.cbio.oncokb;

import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.VariantConsequence;
import org.mskcc.cbio.oncokb.util.VariantConsequenceUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application constants.
 */
public final class Constants {

    public static final String MISSENSE_VARIANT = "missense_variant";
    public static final String IN_FRAME_DELETION = "inframe_deletion";
    public static final String IN_FRAME_INSERTION = "inframe_insertion";
    public static final String FRAMESHIFT_VARIANT = "frameshift_variant";
    public static final String FIVE_UTR = "5_prime_UTR_variant";
    public static final String UPSTREAM_GENE = "upstream_gene_variant";
    public static final String PROTEIN_ALTERING_VARIANT = "protein_altering_variant";

    public static final String PUBLIC_API_VERSION = "v1.5.0";
    public static final String PRIVATE_API_VERSION = "v1.5.0";

    // Defaults
    public static final String SWAGGER_DEFAULT_DESCRIPTION="OncoKB, a comprehensive and curated precision oncology knowledge base, offers oncologists detailed, evidence-based information about individual somatic mutations and structural alterations present in patient tumors with the goal of supporting optimal treatment decisions.";

    // Config property names
    public static final String IS_PUBLIC_INSTANCE = "is_public_instance";
    public static final String SWAGGER_DESCRIPTION = "swagger_description";
    public static final String SWAGGER_URL = "swagger_url";

    public static final ReferenceGenome DEFAULT_REFERENCE_GENOME = ReferenceGenome.GRCh37;

    public static final List<VariantConsequence> SPLICE_SITE_VARIANTS = Arrays.asList("splice_acceptor_variant", "splice_donor_variant", "splice_region_variant").stream().map(term -> VariantConsequenceUtils.findVariantConsequenceByTerm(term)).collect(Collectors.toList());

    private Constants() {
    }
}
