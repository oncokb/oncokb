package org.mskcc.cbio.oncokb.api.websocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.util.ValidationUtils;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

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

        JSONArray data = ValidationUtils.getMismatchRefAAData();
        if (data.length() == 0) {
            sendText(generateInfo(MISMATCH_REF_AA, ValidationStatus.IS_COMPLETE, new JSONArray()));
        } else {
            sendText(generateInfo(MISMATCH_REF_AA, ValidationStatus.IS_ERROR, data));
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
}
