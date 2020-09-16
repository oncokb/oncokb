package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.genome_nexus.ApiClient;
import org.genome_nexus.ApiException;
import org.genome_nexus.client.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.apiModels.TranscriptMatchResult;
import org.mskcc.cbio.oncokb.apiModels.TranscriptPair;
import org.mskcc.cbio.oncokb.apiModels.ensembl.Sequence;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.genomenexus.VEPDetailedEnrichmentService;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Hongxin on 6/26/17.
 */
public class GenomeNexusUtils {

    private static final String MSK_ISOFORM_OVERRIDE ="mskcc";

    private static final String ENSEMBL_37_API_URL = "https://grch37.rest.ensembl.org";
    private static final String ENSEMBL_38_API_URL = "https://rest.ensembl.org";

    private static final String GN_37_URL = "https://www.genomenexus.org";
    private static final String GN_38_URL = "https://grch38.genomenexus.org";
    private static final int GN_READ_TIMEOUT_OVERRIDE = 5000;

    public static String getEnsemblSequencePOSTUrl(ReferenceGenome referenceGenome) {
        return getEnsemblAPIUrl(referenceGenome) + "/sequence/id";
    }

    public static TranscriptMatchResult matchTranscript(TranscriptPair transcript, ReferenceGenome referenceGenome, String hugoSymbol) throws ApiException {
        // Find whether both transcript length are the same
        Optional<EnsemblTranscript> _ensemblTranscript = getEnsemblTranscript(hugoSymbol, transcript);
        TranscriptMatchResult transcriptMatchResult = new TranscriptMatchResult();

        if (_ensemblTranscript.isPresent()) {
            transcriptMatchResult.setOriginalEnsemblTranscript(_ensemblTranscript.get());
            Optional<Sequence> _sequence = getProteinSequence(transcript.getReferenceGenome(), _ensemblTranscript.get().getProteinId());
            if (_sequence.isPresent()) {
                List<EnsemblTranscript> targetEnsemblTranscripts = getEnsemblTranscriptList(hugoSymbol, referenceGenome);
                if (targetEnsemblTranscripts.size() == 0) {
                    transcriptMatchResult.setNote("The target reference genome does not have any ensembl transcripts.");
                } else {
                    try {
                        pickEnsemblTranscript(transcriptMatchResult, referenceGenome, targetEnsemblTranscripts, _sequence.get());
                    } catch (Exception exception) {
                        transcriptMatchResult.setNote(exception.getMessage());
                    }
                }
            } else {
                transcriptMatchResult.setNote("The transcript is invalid");
            }
        } else {
            transcriptMatchResult.setNote("The transcript is invalid");
        }
        return transcriptMatchResult;
    }

    private static ApiClient getGNApiClient(String url) {
        ApiClient client = new ApiClient();
        client.setReadTimeout(GN_READ_TIMEOUT_OVERRIDE);
        client.setBasePath(url);
        return client;
    }

    private static EnsemblControllerApi getGNEnsemblControllerApi(String url) {
        return new EnsemblControllerApi(getGNApiClient(url));
    }

    private static AnnotationControllerApi getGNAnnotationControllerApi(String url) {
        return new AnnotationControllerApi(getGNApiClient(url));
    }

    private static EnsemblControllerApi getEnsemblControllerApi(ReferenceGenome referenceGenome) {
        switch (referenceGenome) {
            case GRCh37:
                return getGNEnsemblControllerApi(GN_37_URL);
            case GRCh38:
                return getGNEnsemblControllerApi(GN_38_URL);
            default:
                return new EnsemblControllerApi();
        }
    }

    private static AnnotationControllerApi getAnnotationControllerApi(ReferenceGenome referenceGenome) {
        switch (referenceGenome) {
            case GRCh37:
                return getGNAnnotationControllerApi(GN_37_URL);
            case GRCh38:
                return getGNAnnotationControllerApi(GN_38_URL);
            default:
                return new AnnotationControllerApi();
        }
    }

    private static String getEnsemblSequenceGETUrl(ReferenceGenome referenceGenome, String transcript) {
        return getEnsemblAPIUrl(referenceGenome) + "/sequence/id/" + transcript;
    }

    public static String getEnsemblAPIUrl(ReferenceGenome referenceGenome) {
        switch (referenceGenome) {
            case GRCh37:
                return ENSEMBL_37_API_URL;
            case GRCh38:
                return ENSEMBL_38_API_URL;
            default:
                return "";
        }
    }

    public static TranscriptConsequence getTranscriptConsequence(GNVariantAnnotationType type, String query, ReferenceGenome referenceGenome) {
        VariantAnnotation annotation = getVariantAnnotation(type, query, referenceGenome);
        return getConsequence(annotation, referenceGenome);
    }

    private static String getIsoform(Gene gene, ReferenceGenome referenceGenome) {
        if (gene == null) {
            return null;
        }
        switch (referenceGenome) {
            case GRCh37:
                return gene.getGrch37Isoform();
            case GRCh38:
                return gene.getGrch38Isoform();
            default:
                return "";
        }
    }

    private static VariantAnnotation getVariantAnnotation(GNVariantAnnotationType type, String query, ReferenceGenome referenceGenome) {
        VariantAnnotation variantAnnotation = null;
        if (query != null && type != null) {
            try {
                if (type.equals(GNVariantAnnotationType.HGVS_G)) {
                    variantAnnotation = getAnnotationControllerApi(referenceGenome).fetchVariantAnnotationGET(query, MSK_ISOFORM_OVERRIDE, null, null);
                } else {
                    variantAnnotation = getAnnotationControllerApi(referenceGenome).fetchVariantAnnotationByGenomicLocationGET(query, MSK_ISOFORM_OVERRIDE, null, null);
                }


                if (variantAnnotation != null) {
                    VEPDetailedEnrichmentService service = new VEPDetailedEnrichmentService();
                    variantAnnotation = service.enrich(variantAnnotation);
                }
            } catch (ApiException e) {
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

    private static List<EnsemblTranscript> getEnsemblTranscriptList(String hugoSymbol, ReferenceGenome referenceGenome) throws ApiException {
        EnsemblControllerApi controllerApi = GenomeNexusUtils.getEnsemblControllerApi(referenceGenome);
        Set<EnsemblTranscript> transcripts = new LinkedHashSet<>();
        transcripts.add(getCanonicalEnsemblTranscript(hugoSymbol, referenceGenome));
        transcripts.addAll(controllerApi.fetchEnsemblTranscriptsGET(null, null, hugoSymbol));
        return new ArrayList<>(transcripts);
    }

    public static EnsemblTranscript getCanonicalEnsemblTranscript(String hugoSymbol, ReferenceGenome referenceGenome) throws ApiException {
        EnsemblControllerApi controllerApi = GenomeNexusUtils.getEnsemblControllerApi(referenceGenome);
        return controllerApi.fetchCanonicalEnsemblTranscriptByHugoSymbolGET(hugoSymbol, MSK_ISOFORM_OVERRIDE);
    }

    private static Optional<EnsemblTranscript> getEnsemblTranscript(String hugoSymbol, TranscriptPair transcriptPair) throws ApiException {
        return getEnsemblTranscriptList(hugoSymbol, transcriptPair.getReferenceGenome()).stream().filter(ensemblTranscript -> !StringUtils.isEmpty(ensemblTranscript.getTranscriptId()) && ensemblTranscript.getTranscriptId().equalsIgnoreCase(transcriptPair.getTranscript())).findFirst();
    }

    private static Optional<Sequence> getProteinSequence(ReferenceGenome referenceGenome, String transcript) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Sequence> response = restTemplate.exchange(
            GenomeNexusUtils.getEnsemblSequenceGETUrl(referenceGenome, transcript), HttpMethod.GET, entity, Sequence.class);
        return Optional.of(response.getBody());
    }

    private static List<Sequence> getProteinSequences(ReferenceGenome referenceGenome, List<String> transcripts) {
        if (transcripts.size() == 0) {
            return new ArrayList<>();
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        transcripts.stream().forEach(transcript -> jsonArray.put(transcript));
        try {
            jsonObject.put("ids", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Sequence[]> response = restTemplate.postForEntity(
            GenomeNexusUtils.getEnsemblSequencePOSTUrl(referenceGenome), entity, Sequence[].class);
        return Arrays.asList(response.getBody());
    }

    private static TranscriptMatchResult pickEnsemblTranscript(TranscriptMatchResult transcriptMatchResult, ReferenceGenome referenceGenome, List<EnsemblTranscript> availableTranscripts, Sequence sequence) {
        List<EnsemblTranscript> sameLengthList = availableTranscripts.stream().filter(ensemblTranscript -> ensemblTranscript.getProteinLength() != null && ensemblTranscript.getProteinLength().equals(sequence.getSeq().length())).collect(Collectors.toList());

        List<Sequence> sequences = getProteinSequences(referenceGenome, sameLengthList.stream().map(EnsemblTranscript::getProteinId).collect(Collectors.toList())).stream().filter(filteredSequence -> filteredSequence.getSeq().length() == sequence.getSeq().length()).collect(Collectors.toList());
        Optional<Sequence> sequenceSame = sequences.stream().filter(matchedSequence -> matchedSequence.getSeq().equals(sequence.getSeq())).findAny();


        if (sequenceSame.isPresent()) {
            Optional<EnsemblTranscript> ensemblTranscript = getEnsemblTranscriptBySequence(sameLengthList, sequenceSame.get());
            transcriptMatchResult.setTargetEnsemblTranscript(ensemblTranscript.get());
            transcriptMatchResult.setNote("Same sequence");
        } else if (sequences.size() > 0) {
            // We should make some comparison with the original sequence for the same length
            sequences.sort(Comparator.comparingInt(s -> getNumOfMismatchSameLengthSequences(sequence.getSeq(), s.getSeq())));
            Sequence pickedSequence = sequences.iterator().next();

            Optional<EnsemblTranscript> ensemblTranscript = getEnsemblTranscriptBySequence(availableTranscripts, pickedSequence);
            transcriptMatchResult.setTargetEnsemblTranscript(ensemblTranscript.get());
            transcriptMatchResult.setNote("Same length, but mismatch: " + getNumOfMismatchSameLengthSequences(sequence.getSeq(), pickedSequence.getSeq()));
        } else {
            // we want to see whether there is any transcript includes the original sequence
            List<EnsemblTranscript> longerOnes = availableTranscripts.stream().filter(ensemblTranscript -> ensemblTranscript.getProteinLength() != null && ensemblTranscript.getProteinLength() > sequence.getSeq().length()).collect(Collectors.toList());

            List<Sequence> longerSequences = getProteinSequences(referenceGenome, longerOnes.stream().map(EnsemblTranscript::getProteinId).collect(Collectors.toList()));
            List<Sequence> sequencesContains = longerSequences.stream().filter(matchedSequence -> matchedSequence.getSeq().contains(sequence.getSeq())).collect(Collectors.toList());
            sequencesContains.sort((s1, s2) -> s2.getSeq().length() - s1.getSeq().length());

            if (sequencesContains.size() > 0) {
                Sequence pickedSequence = sequencesContains.iterator().next();
                Optional<EnsemblTranscript> ensemblTranscript = getEnsemblTranscriptBySequence(longerOnes, pickedSequence);
                transcriptMatchResult.setTargetEnsemblTranscript(ensemblTranscript.get());
                transcriptMatchResult.setNote("Longer one found, length: " + ensemblTranscript.get().getProteinLength());

            } else {
                transcriptMatchResult.setNote("No matched sequence found");
            }
        }
        return transcriptMatchResult;
    }

    private static Optional<EnsemblTranscript> getEnsemblTranscriptBySequence(List<EnsemblTranscript> availableEnsemblTranscripts, Sequence sequence) {
        return availableEnsemblTranscripts.stream().filter(ensemblTranscript -> {
            if (ensemblTranscript.getProteinId() != null && ensemblTranscript.getProteinId().equals(sequence.getId())) {
                return true;
            } else {
                return false;
            }
        }).findAny();
    }

    private static int getNumOfMismatchSameLengthSequences(String reference, String newSequence) {
        int mismatch = 0;
        for (int i = 0; i < reference.length(); i++) {
            char r = reference.charAt(i);
            char n = newSequence.charAt(i);
            if (r != n) {
                mismatch++;
            }
        }
        return mismatch;
    }
}
