package org.mskcc.cbio.oncokb.api.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builders for JSON-RPC 2.0 response envelopes used by the MCP endpoint.
 */
final class JsonRpc {
    static final int PARSE_ERROR = -32700;
    static final int INVALID_REQUEST = -32600;
    static final int METHOD_NOT_FOUND = -32601;
    static final int INVALID_PARAMS = -32602;
    static final int INTERNAL_ERROR = -32603;
    // MCP-specific code for resources/read on an unknown URI.
    static final int RESOURCE_NOT_FOUND = -32002;

    private JsonRpc() {
    }

    static ObjectNode result(ObjectMapper mapper, JsonNode id, JsonNode result) {
        ObjectNode response = envelope(mapper, id);
        response.set("result", result);
        return response;
    }

    static ObjectNode error(ObjectMapper mapper, JsonNode id, int code, String message) {
        ObjectNode response = envelope(mapper, id);
        ObjectNode error = response.putObject("error");
        error.put("code", code);
        error.put("message", message);
        return response;
    }

    private static ObjectNode envelope(ObjectMapper mapper, JsonNode id) {
        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        // JSON-RPC requires "id": null when the request id could not be determined.
        response.set("id", id == null ? mapper.nullNode() : id);
        return response;
    }
}
