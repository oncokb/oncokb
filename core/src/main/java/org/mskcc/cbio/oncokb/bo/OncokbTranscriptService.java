package org.mskcc.cbio.oncokb.bo;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.TranscriptUpdateValidationVM;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.oncokb.oncokb_transcript.ApiClient;
import org.oncokb.oncokb_transcript.ApiException;
import org.oncokb.oncokb_transcript.Configuration;
import org.oncokb.oncokb_transcript.auth.OAuth;
import org.oncokb.oncokb_transcript.client.*;

import java.util.List;

/**
 * Created by Hongxin Zhang on 2/24/21.
 */
public class OncokbTranscriptService {
    private ApiClient client;

    public OncokbTranscriptService() {
        this.client = Configuration.getDefaultApiClient();

        // Configure API key authorization: authorization
        OAuth authorization = (OAuth) this.client.getAuthentication("Authorization");
        String oncokbTranscriptToken = PropertiesUtils.getProperties("oncokb_transcript.token");
        authorization.setAccessToken(oncokbTranscriptToken);
    }

    public void updateTranscriptUsage(Gene gene, String grch37EnsemblTranscriptId, String grch38EnsemblTranscriptId) throws ApiException {
        TranscriptControllerApi controllerApi = new TranscriptControllerApi();

        if(StringUtils.isNotEmpty(grch37EnsemblTranscriptId)) {
            controllerApi.updateTranscriptUsageUsingPOST(
                "ONCOKB",
                gene.getHugoSymbol(),
                gene.getEntrezGeneId(),
                ReferenceGenome.GRCh37.toString(),
                grch37EnsemblTranscriptId
            );
        }
        if (StringUtils.isNotEmpty(grch38EnsemblTranscriptId)) {
            controllerApi.updateTranscriptUsageUsingPOST(
                "ONCOKB",
                gene.getHugoSymbol(),
                gene.getEntrezGeneId(),
                ReferenceGenome.GRCh38.toString(),
                grch38EnsemblTranscriptId
            );
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

    private TranscriptComparisonResultVM compareTranscript(TranscriptPairVM.ReferenceGenomeEnum referenceGenome, Gene gene, String ensemblTranscriptId)  throws ApiException{
        SequenceResourceApi sequenceResourceApi = new SequenceResourceApi();
        TranscriptControllerApi controllerApi = new TranscriptControllerApi();

        List<Sequence> sequences = null;
        try {
            sequences = sequenceResourceApi.getAllSequencesUsingGET1(referenceGenome.toString(), "ONCOKB", gene.getHugoSymbol());
        } catch (ApiException e) {
            throw e;
        }
        Sequence pickedSequence = sequences.iterator().next();
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

            return controllerApi.compareTranscriptUsingPOST1(gene.getHugoSymbol(), vm);
        }
    }

    public String getProteinSequence(ReferenceGenome referenceGenome, Gene gene) throws ApiException {
        SequenceResourceApi sequenceResourceApi = new SequenceResourceApi();
        List<Sequence> sequenceList = sequenceResourceApi.getAllSequencesUsingGET1(referenceGenome.name(), "ONCOKB", gene.getHugoSymbol());
        if (sequenceList.isEmpty()) {
            return null;
        } else {
            return sequenceList.iterator().next().getSequence();
        }
    }

    public List<Sequence> getAllProteinSequences(ReferenceGenome referenceGenome) throws ApiException {
        SequenceResourceApi sequenceResourceApi = new SequenceResourceApi();
        return sequenceResourceApi.getAllSequencesUsingGET1(referenceGenome.name(), "ONCOKB", null);
    }

    public String getAminoAcid(ReferenceGenome referenceGenome, Gene gene, int positionStart, int length) throws ApiException {
        String sequence = getProteinSequence(referenceGenome, gene);

        if (sequence.length() >= (positionStart + length - 1)) {
            return sequence.substring(positionStart - 1, positionStart + length - 1);
        }
        return "";
    }

    public List<Drug> findDrugs(String query) throws ApiException {
        DrugResourceApi drugResourceApi = new DrugResourceApi();
        return drugResourceApi.findDrugsUsingGET(query);
    }

    public Drug findDrugByNcitCode(String code) throws ApiException {
        DrugResourceApi drugResourceApi = new DrugResourceApi();
        return drugResourceApi.findDrugByCodeUsingGET(code);
    }
}
