package org.mskcc.cbio.oncokb.api.websocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.util.ValidationUtils;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

import static org.mskcc.cbio.oncokb.api.websocket.ValidationTest.*;

/**
 * Created by Hongxin on 12/12/16.
 */

@ServerEndpoint(value = "/api/websocket/curation/validation")
public class CurationValidationApiController {
    private Session session;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Get session and WebSocket connection
        this.session = session;

        validateGeneInfo();

        validateEmptyClinicalVariants();

        validateEmptyBiologicalVariants();

        this.session.close();
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        // Handle new messages
    }

    @OnClose
    public void onClose(Session session) throws IOException {
//        allAvailableRequests.remove(session);
        session.close();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

    private void sendText(String text) throws IOException {
        this.session.getBasicRemote().sendText(text);
    }

    private void validateEmptyClinicalVariants() throws IOException {
        sendText(generateInfo(MISSING_TREATMENT_INFO, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = ValidationUtils.getMissingTreatmentInfoData();
        if (data.length() == 0) {
            sendText(generateInfo(MISSING_TREATMENT_INFO, ValidationStatus.IS_COMPLETE, new JSONArray()));
        } else {
            sendText(generateInfo(MISSING_TREATMENT_INFO, ValidationStatus.IS_ERROR, data));
        }
    }

    private void validateEmptyBiologicalVariants() throws IOException {
        sendText(generateInfo(MISSING_BIOLOGICAL_ALTERATION_INFO, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = ValidationUtils.getEmptyBiologicalVariants();
        if (data.length() == 0) {
            sendText(generateInfo(MISSING_BIOLOGICAL_ALTERATION_INFO, ValidationStatus.IS_COMPLETE, new JSONArray()));
        } else {
            sendText(generateInfo(MISSING_BIOLOGICAL_ALTERATION_INFO, ValidationStatus.IS_ERROR, data));
        }
    }

    private void validateGeneInfo() throws IOException {
        sendText(generateInfo(MISSING_GENE_INFO, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = ValidationUtils.checkGeneSummaryBackground();
        if (data.length() == 0) {
            sendText(generateInfo(MISSING_GENE_INFO, ValidationStatus.IS_COMPLETE, new JSONArray()));
        } else {
            sendText(generateInfo(MISSING_GENE_INFO, ValidationStatus.IS_ERROR, data));
        }
    }

    private static final String TEST_KEY = "test";
    private static final String STATUS_KEY = "status";
    private static final String DATA_KEY = "data";

    private static String generateInfo(ValidationTest test, ValidationStatus status, JSONArray data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TEST_KEY, test.getName());
        jsonObject.put(STATUS_KEY, status);
        jsonObject.put(DATA_KEY, data);
        return jsonObject.toString();
    }
}
