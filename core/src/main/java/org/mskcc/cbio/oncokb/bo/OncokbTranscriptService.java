package org.mskcc.cbio.oncokb.bo;

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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Hongxin Zhang on 2/24/21.
 */
public class OncokbTranscriptService {

    private static final String ONCOKB_TRANSCRIPT_URL = "https://transcript.oncokb.org";

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
            cacheAllGenes();
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
        Sequence sequence = sequenceResourceApi.findCanonicalSequenceUsingGET(referenceGenome.name(), gene.getEntrezGeneId(), SEQUENCE_TYPE);
        return sequence == null ? null : sequence.getSequence();
    }

    public List<Sequence> getAllProteinSequences(ReferenceGenome referenceGenome) throws ApiException {
        SequenceControllerApi sequenceResourceApi = new SequenceControllerApi();
        return sequenceResourceApi.findCanonicalSequencesUsingPOST(referenceGenome.name(), SEQUENCE_TYPE, CacheUtils.getAllGenes().stream().map(Gene::getEntrezGeneId).collect(Collectors.toList()));
    }

    public String getAminoAcid(ReferenceGenome referenceGenome, Gene gene, int positionStart, int length) throws ApiException {
        String sequence = getProteinSequence(referenceGenome, gene);

        if (sequence.length() >= (positionStart + length - 1)) {
            return sequence.substring(positionStart - 1, positionStart + length - 1);
        }
        return "";
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
