package org.mskcc.cbio.oncokb.api.websocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.util.ValidationUtils;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.mskcc.cbio.oncokb.api.websocket.ValidationTest.MISSING_CLINICAL_INFO_VARIANTS;

/**
 * Created by Hongxin on 12/12/16.
 */

@ServerEndpoint(value = "/websocket/curation/validation")
public class CurationValidationApiController {
    private Session session;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Get session and WebSocket connection
        this.session = session;
        sendText("Validation started");

        validateEmptyClinicalVariants();

        sendText("Validation is finished.");
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
        sendText(generateInfo(MISSING_CLINICAL_INFO_VARIANTS, ValidationStatus.IS_PENDING, new JSONArray()));

        JSONArray data = ValidationUtils.getEmptyClinicalVariants();
        if (data.length() == 0) {
            sendText(generateInfo(MISSING_CLINICAL_INFO_VARIANTS, ValidationStatus.IS_COMPLETE, new JSONArray()));
        } else {
            sendText(generateInfo(MISSING_CLINICAL_INFO_VARIANTS, ValidationStatus.IS_ERROR, data));
        }
    }

    private static final String TEST_KEY = "test";
    private static final String STATUS_KEY = "status";
    private static final String DATA_KEY = "data";

    private static String generateInfo(ValidationTest test, ValidationStatus status, JSONArray data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.append(TEST_KEY, test);
        jsonObject.append(STATUS_KEY, status);
        jsonObject.append(DATA_KEY, data);
        return jsonObject.toString();
    }
}
