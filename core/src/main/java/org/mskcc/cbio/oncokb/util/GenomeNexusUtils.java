package org.mskcc.cbio.oncokb.util;

import com.google.gson.Gson;
import org.mskcc.cbio.oncokb.genomenexus.TranscriptConsequence;
import org.mskcc.cbio.oncokb.genomenexus.VEPDetailedEnrichmentService;
import org.mskcc.cbio.oncokb.genomenexus.VariantAnnotation;
import org.mskcc.cbio.oncokb.model.Gene;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hongxin on 6/26/17.
 */
public class GenomeNexusUtils {
    private static final String HGVS_ENDPOINT = "hgvs";

    public static TranscriptConsequence getTranscriptConsequence(String hgvs) {
        VariantAnnotation annotation = hgvsCall(hgvs);
        return getConsequence(annotation);
    }

    private static VariantAnnotation hgvsCall(String hgvs) {
        VariantAnnotation variantAnnotation = null;
        if (hgvs != null) {
            String encodedHgvs = "";
            String genomeNexusApi = "http://genomenexus.org/";
            try {
                genomeNexusApi = PropertiesUtils.getProperties("genomenexus.api");
                encodedHgvs = URLEncoder.encode(hgvs, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            String response = HttpUtils.getRequest(genomeNexusApi + HGVS_ENDPOINT + "/" + encodedHgvs, true);
            VariantAnnotation[] variantAnnotations = new Gson().fromJson(response, VariantAnnotation[].class);
            if (variantAnnotations != null && variantAnnotations.length >= 1) {
                variantAnnotation = variantAnnotations[0];
                VEPDetailedEnrichmentService service = new VEPDetailedEnrichmentService();
                variantAnnotation = service.enrich(variantAnnotation);
            }
        }
        return variantAnnotation;
    }

    private static TranscriptConsequence getConsequence(VariantAnnotation variantAnnotation) {
        List<TranscriptConsequence> transcripts = new ArrayList<>();

        if (variantAnnotation == null) {
            return null;
        }

        if (variantAnnotation.getTranscriptConsequences() != null) {
            for (TranscriptConsequence transcript : variantAnnotation.getTranscriptConsequences()) {
                if (transcript.getGeneSymbol() != null && transcript.getTranscriptId() != null) {
                    Gene gene = GeneUtils.getGeneByHugoSymbol(transcript.getGeneSymbol());
                    if (gene != null && (gene.getCuratedIsoform() == null || gene.getCuratedIsoform().equals(transcript.getTranscriptId()))) {
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
