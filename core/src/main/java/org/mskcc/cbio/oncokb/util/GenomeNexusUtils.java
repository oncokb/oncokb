package org.mskcc.cbio.oncokb.util;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.genomenexus.TranscriptConsequence;
import org.mskcc.cbio.oncokb.genomenexus.VEPDetailedEnrichmentService;
import org.mskcc.cbio.oncokb.genomenexus.VariantAnnotation;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hongxin on 6/26/17.
 */
public class GenomeNexusUtils {
    private static final String HGVS_ENDPOINT = "annotation";
    private static final String GENOMIC_LOCATION_ENDPOINT = "annotation/genomic";
    private static final String GENOME_NEXUS_GRCH37_API = "https://www.genomenexus.org/";
    private static final String GENOME_NEXUS_GRCH38_API = "https://grch38.genomenexus.org/";

    public static TranscriptConsequence getTranscriptConsequence(GNVariantAnnotationType type, String query, ReferenceGenome referenceGenome) {
        VariantAnnotation annotation = getVariantAnnotation(type, query, referenceGenome);
        return getConsequence(annotation, referenceGenome);
    }

    private static String getGenomeNexusApi(ReferenceGenome referenceGenome) {
        switch (referenceGenome) {
            case GRCH37:
                return GENOME_NEXUS_GRCH37_API;
            case GRCH38:
                return GENOME_NEXUS_GRCH38_API;
            default:
                return "";
        }
    }

    private static String getIsoform(Gene gene, ReferenceGenome referenceGenome) {
        switch (referenceGenome) {
            case GRCH37:
                return gene.getGrch37Isoform();
            case GRCH38:
                return gene.getGrch38Isoform();
            default:
                return "";
        }
    }

    private static VariantAnnotation getVariantAnnotation(GNVariantAnnotationType type, String query, ReferenceGenome referenceGenome) {
        VariantAnnotation variantAnnotation = null;
        if (query != null && type != null) {
            String encodedQuery = "";
            String genomeNexusApi = getGenomeNexusApi(referenceGenome);
            try {
                if (StringUtils.isEmpty(genomeNexusApi)) {
                    return null;
                }
                encodedQuery = URLEncoder.encode(query, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                String response = null;
                String url = null;
                if (type.equals(GNVariantAnnotationType.HGVS_G)) {
                    url = genomeNexusApi + HGVS_ENDPOINT + "/" + encodedQuery;
                } else {
                    url = genomeNexusApi + GENOMIC_LOCATION_ENDPOINT + "/" + encodedQuery;
                }
                response = HttpUtils.getRequest(url);
                variantAnnotation = new Gson().fromJson(response, VariantAnnotation.class);
                if (variantAnnotation != null) {
                    VEPDetailedEnrichmentService service = new VEPDetailedEnrichmentService();
                    variantAnnotation = service.enrich(variantAnnotation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return variantAnnotation;
    }

    private static TranscriptConsequence getConsequence(VariantAnnotation variantAnnotation, ReferenceGenome referenceGenome) {
        List<TranscriptConsequence> transcripts = new ArrayList<>();

        if (variantAnnotation == null) {
            return null;
        }

        if (variantAnnotation.getTranscriptConsequences() != null) {
            for (TranscriptConsequence transcript : variantAnnotation.getTranscriptConsequences()) {
                if (transcript.getGeneSymbol() != null && transcript.getTranscriptId() != null) {
                    Gene gene = GeneUtils.getGeneByHugoSymbol(transcript.getGeneSymbol());
                    String isoform = getIsoform(gene, referenceGenome);
                    if (gene != null && (StringUtils.isEmpty(isoform) || isoform.equals(transcript.getTranscriptId()))) {
                        transcripts.add(transcript);
                    }
                }
            }
        }

        // only one transcript marked as canonical
        if (transcripts.size() == 1) {
            return transcripts.iterator().next();
        } else if (transcripts.size() > 1) {
            return pickTranscript(transcripts, variantAnnotation.getMostSevereConsequence());
        }
        // no transcript marked as canonical (list.size() == 0), use most sever consequence to decide which one to pick among all available
        else {
            return pickTranscript(variantAnnotation.getTranscriptConsequences(), variantAnnotation.getMostSevereConsequence());
        }
    }

    private static TranscriptConsequence pickTranscript(List<TranscriptConsequence> transcripts, String mostSevereConsequence) {
        List<TranscriptConsequence> canonicalTranscripts = new ArrayList<>();

        if (transcripts == null) {
            return null;
        }
        // Find canonical isoforms first
        for (TranscriptConsequence transcript : transcripts) {
            if (transcript.getCanonical() != null && transcript.equals("1")) {
                canonicalTranscripts.add(transcript);
            }
        }

        // Find isoform with most severe consequence
        for (TranscriptConsequence transcript : canonicalTranscripts) {
            List<String> consequenceTerms = transcript.getConsequenceTerms();
            for (String consequenceTerm : consequenceTerms) {
                if (consequenceTerm.trim().equals(mostSevereConsequence)) {
                    return transcript;
                }
            }
        }

        // no match, return the first one
        if (transcripts.size() > 0) {
            return transcripts.get(0);
        }

        return null;
    }
}
