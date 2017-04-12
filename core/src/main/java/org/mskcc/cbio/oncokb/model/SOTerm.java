package org.mskcc.cbio.oncokb.model;

/**
 * Created by Hongxin on 4/12/17.
 */
public enum SOTerm {
    three_prime_utr_variant("3_prime_UTR_variant"),
    five_prime_utr_variant("5_prime_UTR_variant"),
    any("any"),
    coding_sequence_variant("coding_sequence_variant"),
    downstream_gene_variant("downstream_gene_variant"),
    feature_elongation("feature_elongation"),
    feature_truncation("feature_truncation"),
    frameshift_variant("frameshift_variant"),
    incomplete_terminal_codon_variant("incomplete_terminal_codon_variant"),
    inframe_deletion("inframe_deletion"),
    inframe_insertion("inframe_insertion"),
    initiator_codon_variant("initiator_codon_variant"),
    intergenic_variant("intergenic_variant"),
    intron_variant("intron_variant"),
    mature_mirna_variant("mature_miRNA_variant"),
    missense_variant("missense_variant"),
    na("NA"),
    nc_transcript_variant("nc_transcript_variant"),
    nmd_transcript_variant("NMD_transcript_variant"),
    non_coding_exon_variant("non_coding_exon_variant"),
    regulatory_region_ablation("regulatory_region_ablation"),
    regulatory_region_amplification("regulatory_region_amplification"),
    regulatory_region_variant("regulatory_region_variant"),
    splice_acceptor_variant("splice_acceptor_variant"),
    splice_donor_variant("splice_donor_variant"),
    splice_region_variant("splice_region_variant"),
    stop_gained("stop_gained"),
    stop_lost("stop_lost"),
    stop_retained_variant("stop_retained_variant"),
    synonymous_variant("synonymous_variant"),
    tfbs_ablation("TFBS_ablation"),
    tfbs_amplification("TFBS_amplification"),
    tf_binding_site_variant("TF_binding_site_variant"),
    transcript_ablation("transcript_ablation"),
    transcript_amplification("transcript_amplification"),
    upstream_gene_variant("upstream_gene_variant");

    private String val;

    SOTerm(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }
    }
