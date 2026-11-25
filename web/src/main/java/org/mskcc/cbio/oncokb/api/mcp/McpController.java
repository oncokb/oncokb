package org.mskcc.cbio.oncokb.api.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mskcc.cbio.oncokb.Constants;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Model Context Protocol (MCP) endpoint exposing the OncoKB public API to LLM clients.
 * <p>
 * Implements MCP protocol version {@value #PROTOCOL_VERSION} over the stateless form of the
 * Streamable HTTP transport: each POST carries one JSON-RPC message and gets a single
 * {@code application/json} response. There are no sessions, no SSE streams and no JSON-RPC
 * batches, so only POST is served. It is hand-written because the official MCP Java SDK needs
 * Java 17+, while this app targets Java 8.
 * <p>
 * The endpoint is only registered when {@value McpSettings#ENABLED} is {@code true}. Origin
 * validation and CORS are handled by {@link McpOriginFilter}. Authentication, rate limiting and usage
 * counting are the gateway's job (oncokb-public), not this server's.
 * <p>
 * Tools come from {@link McpToolRegistry} and resources from {@link McpResourceRegistry}. Both call
 * the same services as the REST controllers, so results match the REST API.
 */
@Controller
@Conditional(McpEnabledCondition.class)
public class McpController {
    /**
     * The only protocol version this server implements. initialize always answers with it, and a
     * request whose MCP-Protocol-Version header names any other version is rejected.
     */
    static final String PROTOCOL_VERSION = "2025-11-25";
    static final String PROTOCOL_VERSION_HEADER = "MCP-Protocol-Version";
    // MCP protocol versions are dates; anything else is not a version we could negotiate down from.
    private static final Pattern VERSION_FORMAT = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    private static final Logger LOG = LoggerFactory.getLogger(McpController.class);
    private static final String INSTRUCTIONS = "OncoKB is a precision oncology knowledge base. "
        + "Use the annotate_* tools to get the oncogenicity, mutation effect, levels of evidence, and treatments for a somatic or germline alteration in a tumor type. "
        + "Tumor types use OncoTree codes or names. "
        + "Use the levels* tools to learn what each level of evidence means, and info for the data version. "
        + "Answer only from what OncoKB returns, and cite the dataVersion. "
        + "Annotation results include an oncokbLinks object with oncokb.org page links for the gene, alteration, and cancer type; include them in your answer.";

    // Built the same way as Spring MVC's Jackson converter, so tool output matches REST responses.
    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    private int maxRequestBytes = McpSettings.maxRequestBytes();

    @Autowired
    McpToolRegistry toolRegistry;

    @Autowired
    McpResourceRegistry resourceRegistry;

    void setMaxRequestBytes(int maxRequestBytes) {
        this.maxRequestBytes = maxRequestBytes;
    }

    @RequestMapping(value = "/mcp", method = RequestMethod.POST)
    public ResponseEntity<byte[]> handle(HttpServletRequest httpRequest) throws IOException {
        if (!McpOriginFilter.isMcpPath(httpRequest)) {
            // Only the canonical path is served, so Origin/CORS handling in McpOriginFilter cannot be
            // side-stepped through suffix (/mcp.json), trailing-slash or other servlet-mapping aliases.
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        int contentLength = httpRequest.getContentLength();
        if (contentLength > maxRequestBytes) {
            return tooLarge();
        }
        byte[] body = readBody(httpRequest.getInputStream());
        if (body == null) {
            return tooLarge();
        }

        JsonNode message;
        try {
            message = body.length == 0 ? null : mapper.readTree(body);
        } catch (JsonProcessingException e) {
            return json(HttpStatus.BAD_REQUEST, JsonRpc.error(mapper, null, JsonRpc.PARSE_ERROR, "Parse error: request body is not valid JSON."));
        }
        if (message != null && message.isArray()) {
            return json(HttpStatus.BAD_REQUEST, JsonRpc.error(mapper, null, JsonRpc.INVALID_REQUEST,
                "Invalid Request: JSON-RPC batches are not supported in MCP " + PROTOCOL_VERSION + "; send one message per request."));
        }
        if (message == null || !message.isObject()) {
            return json(HttpStatus.BAD_REQUEST, JsonRpc.error(mapper, null, JsonRpc.INVALID_REQUEST, "Invalid Request: expected a single JSON-RPC message object."));
        }

        JsonNode id = message.get("id");
        boolean hasId = id != null;
        if (hasId && !(id.isTextual() || id.isIntegralNumber())) {
            return json(HttpStatus.BAD_REQUEST, JsonRpc.error(mapper, null, JsonRpc.INVALID_REQUEST, "Invalid Request: id must be a string or an integer."));
        }
        if (!"2.0".equals(message.path("jsonrpc").asText(null))) {
            return json(HttpStatus.BAD_REQUEST, JsonRpc.error(mapper, id, JsonRpc.INVALID_REQUEST, "Invalid Request: jsonrpc must be \"2.0\"."));
        }

        JsonNode methodNode = message.get("method");
        if (methodNode == null) {
            if (hasId && (message.has("result") || message.has("error"))) {
                // A response to a server-to-client request. This server never sends any, so there is
                // nothing to correlate; acknowledge it as the transport requires.
                return accepted();
            }
            return json(HttpStatus.BAD_REQUEST, JsonRpc.error(mapper, id, JsonRpc.INVALID_REQUEST, "Invalid Request: method is required."));
        }
        if (!methodNode.isTextual()) {
            return json(HttpStatus.BAD_REQUEST, JsonRpc.error(mapper, id, JsonRpc.INVALID_REQUEST, "Invalid Request: method must be a string."));
        }
        String method = methodNode.asText();

        String headerVersion = httpRequest.getHeader(PROTOCOL_VERSION_HEADER);
        if (!"initialize".equals(method) && headerVersion != null && !PROTOCOL_VERSION.equals(headerVersion.trim())) {
            ObjectNode error = JsonRpc.error(mapper, id, JsonRpc.INVALID_REQUEST, "Unsupported " + PROTOCOL_VERSION_HEADER + ": " + headerVersion);
            ObjectNode data = ((ObjectNode) error.get("error")).putObject("data");
            data.putArray("supported").add(PROTOCOL_VERSION);
            data.put("requested", headerVersion);
            return json(HttpStatus.BAD_REQUEST, error);
        }

        if (!hasId) {
            // Notification (notifications/initialized, notifications/cancelled, ...). None need any
            // action from a stateless server, and a notification never gets a response body.
            return accepted();
        }

        JsonNode params = message.get("params");
        if (params != null && !params.isObject()) {
            return json(HttpStatus.OK, JsonRpc.error(mapper, id, JsonRpc.INVALID_PARAMS, "Invalid params: params must be an object."));
        }
        if (params == null) {
            params = mapper.createObjectNode();
        }

        ObjectNode response;
        try {
            response = dispatch(id, method, params);
        } catch (RuntimeException e) {
            LOG.error("MCP {} failed", method, e);
            response = JsonRpc.error(mapper, id, JsonRpc.INTERNAL_ERROR, "Internal error");
        }
        return json(HttpStatus.OK, response);
    }

    @RequestMapping(value = "/mcp", method = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.TRACE})
    public ResponseEntity<Void> unsupported() {
        // GET would open a server-to-client SSE stream and DELETE would end a session; this server
        // offers neither, so the transport requires 405.
        HttpHeaders headers = new HttpHeaders();
        headers.setAllow(Collections.singleton(HttpMethod.POST));
        return new ResponseEntity<>(headers, HttpStatus.METHOD_NOT_ALLOWED);
    }

    private ObjectNode dispatch(JsonNode id, String method, JsonNode params) {
        switch (method) {
            case "initialize":
                return initialize(id, params);
            case "ping":
                return JsonRpc.result(mapper, id, mapper.createObjectNode());
            case "tools/list":
                return JsonRpc.result(mapper, id, listTools());
            case "tools/call":
                return callTool(id, params);
            case "resources/list":
                return JsonRpc.result(mapper, id, listResources());
            case "resources/templates/list":
                ObjectNode templates = mapper.createObjectNode();
                templates.putArray("resourceTemplates");
                return JsonRpc.result(mapper, id, templates);
            case "resources/read":
                return readResource(id, params);
            default:
                return JsonRpc.error(mapper, id, JsonRpc.METHOD_NOT_FOUND, "Method not found: " + method);
        }
    }

    private ObjectNode initialize(JsonNode id, JsonNode params) {
        JsonNode requestedNode = params.get("protocolVersion");
        String requested = requestedNode != null && requestedNode.isTextual() ? requestedNode.asText() : null;
        if (requested == null || !VERSION_FORMAT.matcher(requested).matches()) {
            // A missing or malformed version cannot be negotiated; this mirrors the lifecycle spec's
            // "Unsupported protocol version" error. A well-formed but different version is answered
            // with ours below, as version negotiation requires, and the client decides whether to
            // continue. Later requests must then carry our version in MCP-Protocol-Version.
            ObjectNode error = JsonRpc.error(mapper, id, JsonRpc.INVALID_PARAMS, "Unsupported protocol version");
            ObjectNode data = ((ObjectNode) error.get("error")).putObject("data");
            data.putArray("supported").add(PROTOCOL_VERSION);
            data.set("requested", requestedNode == null ? mapper.nullNode() : requestedNode);
            return error;
        }

        ObjectNode result = mapper.createObjectNode();
        result.put("protocolVersion", PROTOCOL_VERSION);

        ObjectNode capabilities = result.putObject("capabilities");
        capabilities.putObject("tools").put("listChanged", false);
        ObjectNode resources = capabilities.putObject("resources");
        resources.put("subscribe", false);
        resources.put("listChanged", false);

        ObjectNode serverInfo = result.putObject("serverInfo");
        serverInfo.put("name", "oncokb");
        serverInfo.put("title", "OncoKB");
        serverInfo.put("version", Constants.PUBLIC_API_VERSION);

        result.put("instructions", INSTRUCTIONS);
        return JsonRpc.result(mapper, id, result);
    }

    private ObjectNode listTools() {
        ObjectNode result = mapper.createObjectNode();
        ArrayNode tools = result.putArray("tools");
        for (McpTool tool : toolRegistry.getTools()) {
            ObjectNode node = tools.addObject();
            node.put("name", tool.name);
            node.put("description", tool.description);
            node.set("inputSchema", tool.inputSchema);
            ObjectNode annotations = node.putObject("annotations");
            annotations.put("readOnlyHint", true);
            annotations.put("openWorldHint", false);
        }
        return result;
    }

    private ObjectNode callTool(JsonNode id, JsonNode params) {
        JsonNode nameNode = params.get("name");
        if (nameNode == null || !nameNode.isTextual()) {
            return JsonRpc.error(mapper, id, JsonRpc.INVALID_PARAMS, "Invalid params: name must be a string.");
        }
        String name = nameNode.asText();
        McpTool tool = toolRegistry.getTool(name);
        if (tool == null) {
            return JsonRpc.error(mapper, id, JsonRpc.INVALID_PARAMS, "Unknown tool: " + name);
        }
        JsonNode argumentsNode = params.get("arguments");
        if (argumentsNode != null && !argumentsNode.isNull() && !argumentsNode.isObject()) {
            return JsonRpc.error(mapper, id, JsonRpc.INVALID_PARAMS, "Invalid params: arguments must be an object.");
        }

        long start = System.currentTimeMillis();
        ObjectNode result;
        try {
            McpArguments arguments = new McpArguments(argumentsNode, McpSchema.propertyNames(tool.inputSchema));
            result = McpToolResult.success(mapper, mapper.valueToTree(tool.handler.call(arguments)));
            LOG.info("MCP tools/call {} succeeded in {} ms", tool.name, System.currentTimeMillis() - start);
        } catch (McpToolException | ApiHttpErrorException e) {
            // Bad arguments, or a 4xx-style validation error from the shared REST service code. MCP
            // reports input validation failures as tool execution errors so the model can correct
            // itself, not as JSON-RPC protocol errors.
            result = McpToolResult.error(mapper, e.getMessage());
            LOG.warn("MCP tools/call {} returned a tool error in {} ms: {}", tool.name, System.currentTimeMillis() - start, e.getMessage());
        } catch (Exception e) {
            result = McpToolResult.error(mapper, "OncoKB failed to process this request. Please try again later.");
            LOG.error("MCP tools/call {} failed in {} ms", tool.name, System.currentTimeMillis() - start, e);
        }
        return JsonRpc.result(mapper, id, result);
    }

    private ObjectNode listResources() {
        ObjectNode result = mapper.createObjectNode();
        ArrayNode resources = result.putArray("resources");
        for (McpResourceRegistry.Resource resource : resourceRegistry.getResources()) {
            ObjectNode node = resources.addObject();
            node.put("uri", resource.uri);
            node.put("name", resource.name);
            node.put("description", resource.description);
            node.put("mimeType", McpResourceRegistry.MIME_TYPE);
        }
        return result;
    }

    private ObjectNode readResource(JsonNode id, JsonNode params) {
        JsonNode uriNode = params.get("uri");
        if (uriNode == null || !uriNode.isTextual()) {
            return JsonRpc.error(mapper, id, JsonRpc.INVALID_PARAMS, "Invalid params: uri must be a string.");
        }
        String uri = uriNode.asText();
        McpResourceRegistry.Resource resource = resourceRegistry.getResource(uri);
        if (resource == null) {
            ObjectNode error = JsonRpc.error(mapper, id, JsonRpc.RESOURCE_NOT_FOUND, "Resource not found");
            ((ObjectNode) error.get("error")).putObject("data").put("uri", uri);
            return error;
        }
        try {
            String text = mapper.writeValueAsString(resource.reader.read());
            ObjectNode result = mapper.createObjectNode();
            ObjectNode contents = result.putArray("contents").addObject();
            contents.put("uri", resource.uri);
            contents.put("mimeType", McpResourceRegistry.MIME_TYPE);
            contents.put("text", text);
            return JsonRpc.result(mapper, id, result);
        } catch (Exception e) {
            LOG.error("MCP resources/read {} failed", uri, e);
            return JsonRpc.error(mapper, id, JsonRpc.INTERNAL_ERROR, "Failed to read resource " + uri);
        }
    }

    /**
     * Reads at most {@link #maxRequestBytes}; returns null when the body is larger, which also
     * covers chunked requests that declare no Content-Length.
     */
    private byte[] readBody(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (total > maxRequestBytes) {
                return null;
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private ResponseEntity<byte[]> tooLarge() throws IOException {
        return json(HttpStatus.PAYLOAD_TOO_LARGE, JsonRpc.error(mapper, null, JsonRpc.INVALID_REQUEST,
            "Request body exceeds the " + maxRequestBytes + " byte limit."));
    }

    private static ResponseEntity<byte[]> accepted() {
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    private ResponseEntity<byte[]> json(HttpStatus status, JsonNode node) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        // writeValueAsBytes always encodes UTF-8, so non-ASCII text in descriptions survives.
        return new ResponseEntity<>(mapper.writeValueAsBytes(node), headers, status);
    }
}
