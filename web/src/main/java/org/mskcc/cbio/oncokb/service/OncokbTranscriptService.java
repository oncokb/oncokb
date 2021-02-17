package org.mskcc.cbio.oncokb.service;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.oncokb.oncokb_transcript.ApiClient;
import org.oncokb.oncokb_transcript.ApiException;
import org.oncokb.oncokb_transcript.Configuration;
import org.oncokb.oncokb_transcript.auth.OAuth;
import org.oncokb.oncokb_transcript.client.TranscriptControllerApi;

/**
 * Created by Hongxin Zhang on 2/24/21.
 */
public class OncokbTranscriptService {
    public OncokbTranscriptService() {
    }

    public ApiClient getClient() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        // Configure API key authorization: authorization
        OAuth authorization = (OAuth) defaultClient.getAuthentication("Authorization");
        String oncokbTranscriptToken = PropertiesUtils.getProperties("oncokb_transcript.token");
        authorization.setAccessToken(oncokbTranscriptToken);

        return defaultClient;
    }

    public void updateTranscriptUsage(Gene gene, String grch37EnsemblTranscriptId, String grch38EnsemblTranscriptId) throws ApiException {
        getClient();

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
}
