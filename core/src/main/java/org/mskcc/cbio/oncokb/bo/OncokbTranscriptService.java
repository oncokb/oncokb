package org.mskcc.cbio.oncokb.bo;

import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.TranscriptUpdateValidationVM;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.oncokb.oncokb_transcript.ApiClient;
import org.oncokb.oncokb_transcript.ApiException;
import org.oncokb.oncokb_transcript.Configuration;
import org.oncokb.oncokb_transcript.auth.HttpBearerAuth;
import org.oncokb.oncokb_transcript.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * Created by Hongxin Zhang on 2/24/21.
 */
public class OncokbTranscriptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OncokbTranscriptService.class);
    private static final String ONCOKB_TRANSCRIPT_URL = "https://transcript.oncokb.org";
    private static final int SEQUENCE_BATCH_COUNT = 3;

    private ApiClient client;
    private final int DEFAULT_TIMEOUT = 30000;
    private final String SEQUENCE_TYPE = "PROTEIN";
    private Boolean enabled = false;

    private static List<org.oncokb.oncokb_transcript.client.Gene> transcriptGenes = new ArrayList<>();
    private static Map<String, org.oncokb.oncokb_transcript.client.Gene> transcriptGeneByKeywords = new HashMap<>();

    public OncokbTranscriptService() {
        this.client = Configuration.getDefaultApiClient();
        String timeoutProperty = PropertiesUtils.getProperties("oncokb_transcript.api_timeout");
        Integer timeout = StringUtils.isNumeric(timeoutProperty) ? Integer.parseInt(timeoutProperty) : DEFAULT_TIMEOUT;
        this.client.setConnectTimeout(timeout);
        this.client.setReadTimeout(timeout);
        this.client.setBasePath(getOncokbTranscriptUrl());

        String oncokbTranscriptToken = PropertiesUtils.getProperties("oncokb_transcript.token");
        HttpBearerAuth Authorization = (HttpBearerAuth) this.client.getAuthentication("Authorization");
        Authorization.setBearerToken(oncokbTranscriptToken);
        if (StringUtils.isNotEmpty(oncokbTranscriptToken)) {
            enabled = true;
            // The gene cache is static and shared by every instance, so constructing another service has
            // nothing to add once it is populated. Skipping it matters because instances are created per
            // request in several controllers and each fetch pulls the full gene list (~8MB).
            // Call cacheAllGenes() directly to refresh it on purpose.
            if (transcriptGeneByKeywords.isEmpty()) {
                cacheAllGenes();
            }
        }
    }

    private static String getOncokbTranscriptUrl() {
        String oncokbTranscriptUrlProperty = PropertiesUtils.getProperties("oncokb_transcript.url");
        return StringUtils.isEmpty(oncokbTranscriptUrlProperty) ? ONCOKB_TRANSCRIPT_URL : oncokbTranscriptUrlProperty;
    }

    public void updateTranscriptUsage(Gene gene, String grch37EnsemblTranscriptId, String grch38EnsemblTranscriptId) throws ApiException {
        TranscriptControllerApi controllerApi = new TranscriptControllerApi();

        if (StringUtils.isNotEmpty(grch37EnsemblTranscriptId)) {
            AddTranscriptBody addTranscriptBody = new AddTranscriptBody();
            addTranscriptBody.setEntrezGeneId(gene.getEntrezGeneId());
            addTranscriptBody.setReferenceGenome(ReferenceGenome.GRCh37.toString());
            addTranscriptBody.setEnsemblTranscriptId(grch37EnsemblTranscriptId);
            addTranscriptBody.setCanonical(true);
            controllerApi.addTranscriptUsingPOST(addTranscriptBody);
        }
        if (StringUtils.isNotEmpty(grch38EnsemblTranscriptId)) {
            AddTranscriptBody addTranscriptBody = new AddTranscriptBody();
            addTranscriptBody.setEntrezGeneId(gene.getEntrezGeneId());
            addTranscriptBody.setReferenceGenome(ReferenceGenome.GRCh38.toString());
            addTranscriptBody.setEnsemblTranscriptId(grch38EnsemblTranscriptId);
            addTranscriptBody.setCanonical(true);
            controllerApi.addTranscriptUsingPOST(addTranscriptBody);
        }
    }

    public TranscriptUpdateValidationVM validateTranscriptUpdate(Gene gene, String grch37EnsemblTranscriptId, String grch38EnsemblTranscriptId) throws ApiException {
        TranscriptUpdateValidationVM transcriptUpdateValidationVM = new TranscriptUpdateValidationVM();
        if (StringUtils.isNotEmpty(grch37EnsemblTranscriptId)) {
            transcriptUpdateValidationVM.setGrch37(this.compareTranscript(TranscriptPairVM.ReferenceGenomeEnum.GRCH37, gene, grch37EnsemblTranscriptId));
        }
        if (StringUtils.isNotEmpty(grch38EnsemblTranscriptId)) {
            transcriptUpdateValidationVM.setGrch38(this.compareTranscript(TranscriptPairVM.ReferenceGenomeEnum.GRCH38, gene, grch38EnsemblTranscriptId));
        }
        return transcriptUpdateValidationVM;
    }

    private TranscriptComparisonResultVM compareTranscript(TranscriptPairVM.ReferenceGenomeEnum referenceGenome, Gene gene, String ensemblTranscriptId) throws ApiException {
        SequenceControllerApi sequenceControllerApi = new SequenceControllerApi();
        TranscriptControllerApi controllerApi = new TranscriptControllerApi();

        Sequence pickedSequence = null;
        pickedSequence = sequenceControllerApi.findCanonicalSequenceUsingGET(referenceGenome.toString(), gene.getEntrezGeneId(), SEQUENCE_TYPE);
        if (pickedSequence == null) {
            return null;
        } else {

            TranscriptComparisonVM vm = new TranscriptComparisonVM();
            vm.setAlign(true);

            // Pair A is the old transcript
            TranscriptPairVM pairA = new TranscriptPairVM();
            pairA.setReferenceGenome(referenceGenome);
            pairA.setTranscript(pickedSequence.getTranscript().getEnsemblTranscriptId());

            // Pair B is the new transcript
            TranscriptPairVM pairB = new TranscriptPairVM();
            pairB.setReferenceGenome(referenceGenome);
            pairB.setTranscript(ensemblTranscriptId);

            vm.setTranscriptA(pairA);
            vm.setTranscriptB(pairB);

            return controllerApi.compareTranscriptUsingPOST(gene.getHugoSymbol(), vm);
        }
    }

    public String getProteinSequence(ReferenceGenome referenceGenome, Gene gene) throws ApiException {
        SequenceControllerApi sequenceResourceApi = new SequenceControllerApi();
        // The sequence differs between reference genomes for some genes, so an unspecified genome has to
        // fall back to the same default the API advertises rather than to whichever one the service picks.
        ReferenceGenome rg = referenceGenome == null ? DEFAULT_REFERENCE_GENOME : referenceGenome;
        Sequence sequence = sequenceResourceApi.findCanonicalSequenceUsingGET(rg.name(), gene.getEntrezGeneId(), SEQUENCE_TYPE);
        return sequence == null ? null : sequence.getSequence();
    }

    public List<Sequence> getAllProteinSequences(ReferenceGenome referenceGenome) throws ApiException {
        List<Integer> allGeneIds = CacheUtils.getAllGenes().stream().map(Gene::getEntrezGeneId).collect(Collectors.toList());

        int chunkSize = (allGeneIds.size() + SEQUENCE_BATCH_COUNT - 1) / SEQUENCE_BATCH_COUNT;
        List<CompletableFuture<List<Sequence>>> futures = new ArrayList<>();

        for (int i = 0; i < allGeneIds.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, allGeneIds.size());
            List<Integer> chunk = new ArrayList<>(allGeneIds.subList(i, end));
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return fetchProteinSequences(referenceGenome, chunk);
                } catch (ApiException e) {
                    throw new CompletionException(e);
                }
            }));
        }

        List<Sequence> combined = new ArrayList<>();
        for (CompletableFuture<List<Sequence>> future : futures) {
            try {
                combined.addAll(future.join());
            } catch (CompletionException e) {
                if (e.getCause() instanceof ApiException) {
                    throw (ApiException) e.getCause();
                }
                throw e;
            }
        }
        return combined;
    }

    private List<Sequence> fetchProteinSequences(ReferenceGenome referenceGenome, List<Integer> geneIds) throws ApiException {
        SequenceControllerApi sequenceResourceApi = new SequenceControllerApi();
        return sequenceResourceApi.findCanonicalSequencesUsingPOST(referenceGenome.name(), SEQUENCE_TYPE, geneIds);
    }

    /**
     * Fetches the canonical protein sequence for every given gene in one request, keyed by entrez gene id.
     *
     * <p>Uses /api/find-canonical-protein-sequences, which resolves any number of genes with a single joined
     * statement and returns the entrez gene id alongside each sequence. That is what makes one request for
     * the whole gene list viable, and it also removes the need to map results back onto genes through the
     * curated isoform.
     *
     * <p>Genes the service has no sequence for are simply absent from the returned map — that is a normal
     * outcome (not every gene has a transcript in every reference genome), so callers should treat a missing
     * key as "known to have none" rather than as a failure.
     */
    public Map<Integer, String> getCanonicalProteinSequences(ReferenceGenome referenceGenome, Collection<Gene> genes) throws ApiException {
        Map<Integer, String> sequences = new HashMap<>();
        if (!this.enabled || genes == null || genes.isEmpty()) {
            return sequences;
        }
        ReferenceGenome rg = referenceGenome == null ? DEFAULT_REFERENCE_GENOME : referenceGenome;

        List<Integer> entrezGeneIds = genes
            .stream()
            .filter(gene -> gene != null && gene.getEntrezGeneId() != null)
            .map(Gene::getEntrezGeneId)
            .collect(Collectors.toList());
        if (entrezGeneIds.isEmpty()) {
            return sequences;
        }

        List<CanonicalProteinSequenceVM> response = new SequenceControllerApi()
            .findCanonicalProteinSequencesUsingPOST(rg.name(), entrezGeneIds);

        for (CanonicalProteinSequenceVM proteinSequence : response == null ? Collections.<CanonicalProteinSequenceVM>emptyList() : response) {
            if (proteinSequence == null || proteinSequence.getEntrezGeneId() == null || StringUtils.isEmpty(proteinSequence.getSequence())) {
                continue;
            }
            sequences.put(proteinSequence.getEntrezGeneId(), proteinSequence.getSequence());
        }
        return sequences;
    }

    public String getAminoAcid(ReferenceGenome referenceGenome, Gene gene, int positionStart, int length) throws ApiException {
        String sequence = getProteinSequence(referenceGenome, gene);
        int end = positionStart + length - 1;
        if (sequence == null || sequence.length() < end) {
            return "";
        }
        return sequence.substring(positionStart - 1, end);
    }

    /**
     * Returns the OncoKB canonical protein sequence for the gene, or {@code null} when it cannot be
     * determined (service disabled, or missing entrez id/sequence).
     */
    public String getCanonicalProteinSequence(ReferenceGenome referenceGenome, Gene gene) throws ApiException {
        if (!this.enabled || gene == null || gene.getEntrezGeneId() == null) {
            return null;
        }
        String sequence = getProteinSequence(referenceGenome, gene);
        return StringUtils.isEmpty(sequence) ? null : sequence;
    }

    public List<Drug> findDrugs(String query) throws ApiException {
        if (!this.enabled) {
            return new ArrayList<>();
        }
        DrugControllerApi drugControllerApi = new DrugControllerApi();
        return drugControllerApi.findDrugsUsingGET(query);
    }

    public Drug findDrugByNcitCode(String code) throws ApiException {
        if (!this.enabled) {
            return null;
        }
        DrugControllerApi drugControllerApi = new DrugControllerApi();
        return drugControllerApi.findDrugByCodeUsingGET(code);
    }

    public void cacheAllGenes() {
        GeneResourceApi geneResourceApi = new GeneResourceApi();
        try {
            transcriptGenes = geneResourceApi.getAllGenesUsingGET();
            transcriptGenes.forEach(gene -> {
                transcriptGeneByKeywords.put(gene.getHugoSymbol().toLowerCase(), gene);
                transcriptGeneByKeywords.put(gene.getEntrezGeneId().toString(), gene);
                gene.getGeneAliases().forEach(ea -> {
                    transcriptGeneByKeywords.put(ea.getName().toLowerCase(), gene);
                });
            });
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public org.oncokb.oncokb_transcript.client.Gene findTranscriptGeneBySymbol(String symbol) {
        if(StringUtils.isEmpty(symbol)){
            return null;
        }
        return transcriptGeneByKeywords.get(symbol.toLowerCase());
    }

    public Gene findGeneBySymbol(String symbol) {
        Gene gene = GeneUtils.getGene(symbol);
        if (gene != null) {
            return gene;
        }
        if (!this.enabled) {
            return null;
        }
        org.oncokb.oncokb_transcript.client.Gene transcriptGene = findTranscriptGeneBySymbol(symbol);
        return transcriptGene == null ? null : transcriptGeneMap(transcriptGene);
    }

    public List<TranscriptDTO> findEnsemblTranscriptsByIds(List<String> ensemblTranscriptIds, ReferenceGenome referenceGenome) throws ApiException {
        if (!this.enabled) {
            return new ArrayList<>();
        }
        TranscriptControllerApi transcriptControllerApi = new TranscriptControllerApi();
        return transcriptControllerApi.findTranscriptsByEnsemblIdsUsingPOST(referenceGenome.name(), ensemblTranscriptIds);
    }

    public List<Gene> findGenesBySymbols(List<String> symbols) throws ApiException {
        List<String> unknownGenes = new ArrayList<>();
        List<Gene> genes = new ArrayList<>();
        for (String symbol : symbols) {
            Gene gene = GeneUtils.getGene(symbol);
            if (gene == null) {
                unknownGenes.add(symbol);
            } else {
                genes.add(gene);
            }
        }
        if (this.enabled && unknownGenes.size() > 0) {
            unknownGenes.forEach(ug -> {
                Gene gene = this.findGeneBySymbol(ug);
                if (gene != null) {
                    genes.add(gene);
                }
            });
        }
        return genes;
    }

    public Set<org.oncokb.oncokb_transcript.client.Gene> findTranscriptGenesBySymbols(List<String> symbols) {
        if (!this.enabled || symbols == null) {
            return new HashSet<>();
        }
        return symbols.stream().map(symbol -> findTranscriptGeneBySymbol(symbol)).filter(g -> g != null).collect(Collectors.toSet());
    }

    private Gene transcriptGeneMap(org.oncokb.oncokb_transcript.client.Gene transcriptGene) {
        if (transcriptGene != null) {
            Gene gene = new Gene();
            gene.setHugoSymbol(transcriptGene.getHugoSymbol());
            gene.setEntrezGeneId(transcriptGene.getEntrezGeneId());
            gene.setGeneAliases(transcriptGene.getGeneAliases().stream().map(geneAlias -> geneAlias.getName()).collect(Collectors.toSet()));
            return gene;
        }
        return new Gene();
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
