package org.mskcc.cbio.oncokb.api.websocket;

import com.mysql.jdbc.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.OncokbTranscriptService;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ValidationUtils;
import org.oncokb.oncokb_transcript.ApiException;
import org.oncokb.oncokb_transcript.client.Sequence;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;

import static org.mskcc.cbio.oncokb.api.websocket.ValidationCategory.*;

/**
 * Created by Hongxin on 12/12/16.
 */

//@ServerEndpoint(value = "/api/websocket/curation/validation")
public class CurationValidationApiController {
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        // Get session and WebSocket connection
        this.session = session;

        validateHugoSymbols();

        validateGeneInfo();

        validateEmptyClinicalVariants();

        validateEmptyBiologicalVariants();

        validateEvidenceDescriptionInfo();

        validateAlterationName();

        validateMismatchedRefAA();

        compareActionableGenes();

        try {
            this.session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // Handle new messages
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        session.close();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

    private void sendText(String text) {
        try {
            this.session.getBasicRemote().sendText(text);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                this.session.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void validateEmptyClinicalVariants() {
        sendText(generateInfo(MISSING_TREATMENT_INFO, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = ValidationUtils.getMissingTreatmentInfoData();
        if (data.length() == 0) {
            sendText(generateInfo(MISSING_TREATMENT_INFO, ValidationStatus.IS_COMPLETE, new JSONArray()));
        } else {
            sendText(generateInfo(MISSING_TREATMENT_INFO, ValidationStatus.IS_ERROR, data));
        }
    }

    private void validateEmptyBiologicalVariants() {
        sendText(generateInfo(MISSING_BIOLOGICAL_ALTERATION_INFO, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = ValidationUtils.getEmptyBiologicalVariants();
        if (data.length() == 0) {
            sendText(generateInfo(MISSING_BIOLOGICAL_ALTERATION_INFO, ValidationStatus.IS_COMPLETE, new JSONArray()));
        } else {
            sendText(generateInfo(MISSING_BIOLOGICAL_ALTERATION_INFO, ValidationStatus.IS_ERROR, data));
        }
    }

    private void validateGeneInfo() {
        sendText(generateInfo(MISSING_GENE_INFO, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = ValidationUtils.checkGeneSummaryBackground();
        if (data.length() == 0) {
            sendText(generateInfo(MISSING_GENE_INFO, ValidationStatus.IS_COMPLETE, new JSONArray()));
        } else {
            sendText(generateInfo(MISSING_GENE_INFO, ValidationStatus.IS_ERROR, data));
        }
    }

    private void validateEvidenceDescriptionInfo() {
        sendText(generateInfo(INCORRECT_EVIDENCE_DESCRIPTION_FORMAT, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = ValidationUtils.checkEvidenceDescriptionReferenceFormat();
        if (data.length() == 0) {
            sendText(generateInfo(INCORRECT_EVIDENCE_DESCRIPTION_FORMAT, ValidationStatus.IS_COMPLETE, new JSONArray()));
        } else {
            sendText(generateInfo(INCORRECT_EVIDENCE_DESCRIPTION_FORMAT, ValidationStatus.IS_ERROR, data));
        }
    }

    private void validateAlterationName() {
        sendText(generateInfo(INCORRECT_ALTERATION_NAME_FORMAT, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = ValidationUtils.checkAlterationNameFormat();
        if (data.length() == 0) {
            sendText(generateInfo(INCORRECT_ALTERATION_NAME_FORMAT, ValidationStatus.IS_COMPLETE, new JSONArray()));
        } else {
            sendText(generateInfo(INCORRECT_ALTERATION_NAME_FORMAT, ValidationStatus.IS_ERROR, data));
        }
    }

    private void validateMismatchedRefAA() {
        sendText(generateInfo(MISMATCH_REF_AA, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = null;
        try {
            data = getMismatchRefAAData();
            if (data.length() == 0) {
                sendText(generateInfo(MISMATCH_REF_AA, ValidationStatus.IS_COMPLETE, new JSONArray()));
            } else {
                sendText(generateInfo(MISMATCH_REF_AA, ValidationStatus.IS_ERROR, data));
            }
        } catch (ApiException e) {
            data = new JSONArray();
            data.put(ValidationUtils.getErrorMessage("API ERROR", e.getMessage()));
            sendText(generateInfo(MISMATCH_REF_AA, ValidationStatus.IS_ERROR, null));
        }
    }

    private void validateHugoSymbols() {
        sendText(generateInfo(OUTDATED_HUGO_SYMBOLS, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = ValidationUtils.validateHugoSymbols();
        if (data.length() == 0) {
            sendText(generateInfo(OUTDATED_HUGO_SYMBOLS, ValidationStatus.IS_COMPLETE, new JSONArray()));
        } else {
            sendText(generateInfo(OUTDATED_HUGO_SYMBOLS, ValidationStatus.IS_ERROR, data));
        }
    }

    private void compareActionableGenes() {
        sendText(generateInfo(ACTIONABLE_INFO, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = new JSONArray();
        try {
            data = ValidationUtils.compareActionableGene();
            if (data.length() == 0) {
                sendText(generateInfo(ACTIONABLE_INFO, ValidationStatus.IS_COMPLETE, new JSONArray()));
            } else {
                sendText(generateInfo(ACTIONABLE_INFO, ValidationStatus.IS_ERROR, data));
            }
        } catch (IOException e) {
            sendText(generateInfo(ACTIONABLE_INFO, ValidationStatus.IS_ERROR, data));
        }
    }

    private static final String KEY = "key";
    private static final String TYPE = "type";
    private static final String STATUS_KEY = "status";
    private static final String DATA_KEY = "data";

    private static String generateInfo(ValidationCategory test, ValidationStatus status, JSONArray data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TYPE, test.getType());
        jsonObject.put(KEY, test.getName());
        jsonObject.put(STATUS_KEY, status);
        jsonObject.put(DATA_KEY, data);
        return jsonObject.toString();
    }

    public JSONArray getMismatchRefAAData() throws ApiException {
        JSONArray data = new JSONArray();
        OncokbTranscriptService oncokbTranscriptService = new OncokbTranscriptService();

        List<org.oncokb.oncokb_transcript.client.Sequence> allGrch37Sequences = oncokbTranscriptService.getAllProteinSequences(ReferenceGenome.GRCh37);
        List<org.oncokb.oncokb_transcript.client.Sequence> allGrch38Sequences = oncokbTranscriptService.getAllProteinSequences(ReferenceGenome.GRCh38);

        for (Alteration alteration : AlterationUtils.getAllAlterations()) {
            if (alteration.getGene().getEntrezGeneId() > 0 && alteration.getProteinStart() >= 0 && alteration.getReferenceGenomes() != null && alteration.getRefResidues() != null) {
                String sequence = "";
                ReferenceGenome referenceGenome = null;
                for (ReferenceGenome ref : alteration.getReferenceGenomes()) {
                    if (ref.equals(ReferenceGenome.GRCh37)) {
                        sequence = getGeneSequenceFromPool(allGrch37Sequences, alteration.getGene().getHugoSymbol());
                    } else if (ref.equals(ReferenceGenome.GRCh38)) {
                        sequence = getGeneSequenceFromPool(allGrch38Sequences, alteration.getGene().getHugoSymbol());
                    }
                    if (!StringUtils.isNullOrEmpty(sequence)) {
                        referenceGenome = ref;
                        break;
                    }
                }
                if (StringUtils.isNullOrEmpty(sequence)) {
                    data.put(ValidationUtils.getErrorMessage(ValidationUtils.getTarget(alteration.getGene().getHugoSymbol(), alteration.getName()), "No sequence available for " + alteration.getGene().getHugoSymbol()));
                } else if (referenceGenome != null) {
                    if (sequence.length() < alteration.getProteinStart()) {
                        data.put(ValidationUtils.getErrorMessage(ValidationUtils.getTarget(alteration.getGene().getHugoSymbol(), alteration.getName()), "The gene only has " + sequence.length() + " AAs. But the variant protein start is " + alteration.getProteinStart()));
                    } else if (sequence.length() < alteration.getProteinEnd()) {
                        data.put(ValidationUtils.getErrorMessage(ValidationUtils.getTarget(alteration.getGene().getHugoSymbol(), alteration.getName()), "The gene only has " + sequence.length() + " AAs. But the variant protein end is " + alteration.getProteinEnd()));
                    } else {
                        String referenceAA = sequence.substring(alteration.getProteinStart() - 1, alteration.getProteinStart() + alteration.getRefResidues().length() - 1);
                        ;
                        if (!referenceAA.equals(alteration.getRefResidues())) {
                            data.put(ValidationUtils.getErrorMessage(ValidationUtils.getTarget(alteration.getGene().getHugoSymbol(), alteration.getName()), "The reference amino acid does not match with the curated variant. The expected AA is " + referenceAA));
                        }
                    }
                }
            }
        }
        return data;
    }

    private String getGeneSequenceFromPool(List<Sequence> allSequences, String hugoSymbol) {
        Sequence matchedSeq = allSequences.stream().filter(sequence -> sequence.getTranscript().getHugoSymbol().equals(hugoSymbol)).findAny().orElse(null);
        return matchedSeq == null ? null : matchedSeq.getSequence();
    }
}
