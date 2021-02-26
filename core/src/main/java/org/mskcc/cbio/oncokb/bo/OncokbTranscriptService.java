package org.mskcc.cbio.oncokb.bo;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.oncokb.oncokb_transcript.ApiClient;
import org.oncokb.oncokb_transcript.ApiException;
import org.oncokb.oncokb_transcript.Configuration;
import org.oncokb.oncokb_transcript.auth.OAuth;
import org.oncokb.oncokb_transcript.client.Sequence;
import org.oncokb.oncokb_transcript.client.SequenceResourceApi;
import org.oncokb.oncokb_transcript.client.TranscriptControllerApi;

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

    public String getProteinSequence(ReferenceGenome referenceGenome, Gene gene) throws ApiException {
        SequenceResourceApi sequenceResourceApi = new SequenceResourceApi();
        List<Sequence> sequenceList = sequenceResourceApi.getAllSequencesUsingGET1(referenceGenome.name(), "ONCOKB", gene.getHugoSymbol());
        if (sequenceList.isEmpty()) {
            return null;
        } else {
            return sequenceList.iterator().next().getSequence();
        }
    }

    public String getAminoAcid(ReferenceGenome referenceGenome, Gene gene, int positionStart, int length) throws ApiException {
        String sequence = getProteinSequence(referenceGenome, gene);

        if (sequence.length() >= (positionStart + length - 1)) {
            return sequence.substring(positionStart - 1, positionStart + length - 1);
        }
        return "";
    }
}
