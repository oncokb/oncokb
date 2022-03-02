package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.VariantConsequence;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mskcc.cbio.oncokb.Constants.*;

/**
 * Created by Hongxin on 8/10/15.
 */
public class VariantConsequenceUtils {

    private static final String VARIANT_CONSEQUENCE_FILE_PATH = "/data/variant-consequences.txt";
    private static Map<String, VariantConsequence> VariantConsequencesMap = null;
    private static final Map<String, String> TCGAMapping = new HashMap<String, String>() {{
        put("3'flank", "downstream_gene_variant");
        put("3'utr", "3_prime_UTR_variant");
        put("5'flank", "upstream_gene_variant");
        put("5'utr", "5_prime_UTR_variant");
        put("damaging", "feature_truncation");
        put("essential_splice_site", "feature_truncation");
        put("exon skipping", IN_FRAME_DELETION);
        put("frame_shift", "frameshift_variant");
        put("frame_shift_del", "frameshift_variant");
        put("frame_shift_dnp", "frameshift_variant");
        put("frame_shift_ins", "frameshift_variant");
        put("frameshift deletion", "frameshift_variant");
        put("frameshift insertion", "frameshift_variant");
        put("frameshift_coding", "frameshift_variant");
        put("frameshift", "frameshift_variant");
        put("frameshift_deletion", "frameshift_variant");
        put("frameshift_insertion", "frameshift_variant");
        put("fusion", "fusion");
        put("igr", "intergenic_variant");
        put("in_frame_del", IN_FRAME_DELETION);
        put("in_frame_ins", IN_FRAME_INSERTION);
        put("indel", "inframe_indel");
        put("intron", "intron_variant");
        put("missense", MISSENSE_VARIANT);
        put("missense_mutation", MISSENSE_VARIANT);
        put("nonsense", "stop_gained");
        put("nonsense_mutation", "stop_gained");
        put("nonstop_mutation", "stop_lost");
        put("nonsynonymous_snv", "any");
        put("silent", "synonymous_variant");
        put("splice", "splice_region_variant");
        put("splice_region", "splice_region_variant");
        put("splice_site", "splice_region_variant");
        put("splice_site_del", "splice_region_variant");
        put("splice_site_snp", "splice_region_variant");
        put("splicing", "splice_region_variant");
        put("stopgain_snv", "stop_gained");
        put("translation_start_site", "start_lost");
        put("upstream", "5_prime_UTR_variant");
    }};

    private static final Map<String, String> GNMapping = new HashMap<String, String>() {{
        put(MISSENSE_VARIANT, "Missense_Mutation");
        put(IN_FRAME_INSERTION, "In_Frame_Ins");
        put(IN_FRAME_DELETION, "In_Frame_Del");
        put("splice_region_variant", "Splice_Region");
    }};

    private static void cacheVariantConsequencesMap() {
        if (VariantConsequencesMap == null) {
            try {
                List<String> lines = null;
                VariantConsequencesMap = new HashMap<String, VariantConsequence>();
                lines = FileUtils.readTrimedLinesStream(
                    VariantConsequenceUtils.class.getResourceAsStream(VARIANT_CONSEQUENCE_FILE_PATH));

                int nLines = lines.size();
                System.out.println("importing...");
                for (int i = 0; i < nLines; i++) {
                    String line = lines.get(i);
                    if (line.startsWith("#")) continue;

                    String[] parts = line.split("\t");

                    String term = parts[0];
                    String isGenerallyTruncating = parts[1];
                    String desc = parts[2];

                    VariantConsequence variantConsequence = new VariantConsequence(term, desc, isGenerallyTruncating.equalsIgnoreCase("yes"));
                    VariantConsequencesMap.put(term, variantConsequence);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static VariantConsequence findVariantConsequenceByTerm(String searchTerm) {
        VariantConsequence variantConsequence = findVariantConsequenceBySoTerm(searchTerm);
        if (variantConsequence == null) {
            variantConsequence = findVariantConsequenceByTCGATerm(searchTerm);
        }
        return variantConsequence;
    }


    private static VariantConsequence findVariantConsequenceBySoTerm(String term) {
        cacheVariantConsequencesMap();
        VariantConsequence variantConsequence = VariantConsequencesMap.get(term);
        return variantConsequence;
    }

    private static VariantConsequence findVariantConsequenceByTCGATerm(String term) {
        if (term == null) {
            return null;
        } else {
            String matchStr = "";
            term = term.toLowerCase();
            if (TCGAMapping.containsKey(term)) {
                matchStr = TCGAMapping.get(term);
            }
            return findVariantConsequenceBySoTerm(matchStr);
        }
    }

    public static String toGNMutationType(VariantConsequence consequence) {
        if (consequence != null && GNMapping.containsKey(consequence.getTerm())) {
            return GNMapping.get(consequence.getTerm());
        }
        return "";
    }
}
