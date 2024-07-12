package org.mskcc.cbio.oncokb.api.websocket;

/**
 * Created by Hongxin Zhang on 3/6/20.
 */
public enum ValidationCategory {
    ACTIONABLE_INFO(ValidationType.INFO, "The actionable genes comparison between public and latest"),
    MISSING_TREATMENT_INFO(ValidationType.TEST, "Whether treatment missing information"),
    MISSING_BIOLOGICAL_ALTERATION_INFO(ValidationType.TEST, "Whether biological alteration missing information"),
    MISSING_GENE_INFO(ValidationType.TEST, "Whether gene missing summary or background"),
    INCORRECT_EVIDENCE_DESCRIPTION_FORMAT(ValidationType.TEST, "Whether evidence description has wrong format content"),
    OUTDATED_INFO_EVIDENCE_DESCRIPTION(ValidationType.TEST, "Whether evidence description has outdated content"),
    INCORRECT_ALTERATION_NAME_FORMAT(ValidationType.TEST, "Whether alteration is named appropriately"),
    OUTDATED_HUGO_SYMBOLS(ValidationType.TEST, "Whether all genes are using the latest hugo symbol"),
    MISMATCH_REF_AA(ValidationType.TEST, "Whether all variants have matched reference amino acid on the position curated"),
    DUP_ALTERATION(ValidationType.TEST, "Whether the same variant exists in both mutation list and VUS list"),
    TRUNCATING_MUTATIONS_NOT_UNDER_TSG(ValidationType.TEST, "Whether all genes are tumor suppressor genes if Truncating Mutations curated under the gene"),
    VARIANT_ACTIONABILITY_AND_ONCOGENICITY(ValidationType.TEST, "Whether any actionable variants are not oncogenic, likely oncogenic or resistant")
    ;

    ValidationCategory(ValidationType type, String name) {
        this.type = type;
        this.name = name;
    }

    String name;
    ValidationType type;

    public String getName() {
        return name;
    }

    public ValidationType getType() {
        return type;
    }
}
