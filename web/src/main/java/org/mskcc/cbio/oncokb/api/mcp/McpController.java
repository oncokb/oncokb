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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Model Context Protocol (MCP) endpoint exposing the OncoKB public API to LLM clients.
 * <p>
 * Implements the stateless form of the Streamable HTTP transport: each POST carries one JSON-RPC
 * message and gets a single JSON response. There are no sessions and no server-initiated SSE
 * stream, so GET and DELETE return 405. It is hand-written because the official MCP Java SDK needs
 * Java 17+, while this app targets Java 8.
 * <p>
 * Tools come from {@link McpToolRegistry} and resources from {@link McpResourceRegistry}. Both call
 * the same services as the REST controllers, so results match the REST API.
 */
@Controller
public class McpController {
    static final List<String> SUPPORTED_PROTOCOL_VERSIONS = Arrays.asList("2025-06-18", "2025-03-26", "2024-11-05");

    private static final Logger LOG = LoggerFactory.getLogger(McpController.class);
    private static final String INSTRUCTIONS = "OncoKB is a precision oncology knowledge base. "
        + "Use the annotate_* tools to get the oncogenicity, mutation effect, levels of evidence, and treatments for a somatic or germline alteration in a tumor type. "
        + "Tumor types use OncoTree codes or names. "
        + "Use the levels* tools to learn what each level of evidence means, and info for the data version. "
        + "Answer only from what OncoKB returns, and cite the dataVersion. "
        + "Annotation results include an oncokbLinks object with oncokb.org page links for the gene, alteration, and cancer type; include them in your answer.";

    // Built the same way as Spring MVC's Jackson converter, so tool output matches REST responses.
    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    @Autowired
    McpToolRegistry toolRegistry;

    @Autowired
    McpResourceRegistry resourceRegistry;

    @RequestMapping(value = "/mcp", method = RequestMethod.POST)
    public ResponseEntity<byte[]> handle(@RequestBody(required = false) byte[] body) throws IOException {
        JsonNode request;
        try {
            request = body == null || body.length == 0 ? null : mapper.readTree(body);
        } catch (JsonProcessingException e) {
            return json(HttpStatus.BAD_REQUEST, JsonRpc.error(mapper, null, JsonRpc.PARSE_ERROR, "Parse error: " + e.getOriginalMessage()));
        }
        if (request == null || !request.isObject()) {
            // Also covers JSON-RPC batches (arrays), which MCP 2025-06-18 removed.
            return json(HttpStatus.BAD_REQUEST, JsonRpc.error(mapper, null, JsonRpc.INVALID_REQUEST, "Invalid Request: expected a single JSON-RPC message object."));
        }

        JsonNode id = request.get("id");
        String method = request.path("method").isTextual() ? request.get("method").asText() : null;
        if (!"2.0".equals(request.path("jsonrpc").asText())) {
            return json(HttpStatus.BAD_REQUEST, JsonRpc.error(mapper, id, JsonRpc.INVALID_REQUEST, "Invalid Request: jsonrpc must be \"2.0\"."));
        }
        if (id == null || method == null) {
            // Notifications (no id) and responses to server requests need no reply. We never send
            // server requests, so any response is simply acknowledged.
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }

        JsonNode params = request.path("params");
        ObjectNode response;
        switch (method) {
            case "initialize":
                response = JsonRpc.result(mapper, id, initialize(params));
                break;
            case "ping":
                response = JsonRpc.result(mapper, id, mapper.createObjectNode());
                break;
            case "tools/list":
                response = JsonRpc.result(mapper, id, listTools());
                break;
            case "tools/call":
                response = callTool(id, params);
                break;
            case "resources/list":
                response = JsonRpc.result(mapper, id, listResources());
                break;
            case "resources/templates/list":
                ObjectNode templates = mapper.createObjectNode();
                templates.putArray("resourceTemplates");
                response = JsonRpc.result(mapper, id, templates);
                break;
            case "resources/read":
                response = readResource(id, params);
                break;
            default:
                response = JsonRpc.error(mapper, id, JsonRpc.METHOD_NOT_FOUND, "Method not found: " + method);
        }
        return json(HttpStatus.OK, response);
    }

    @RequestMapping(value = "/mcp", method = {RequestMethod.GET, RequestMethod.DELETE})
    public ResponseEntity<Void> unsupported() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAllow(Collections.singleton(HttpMethod.POST));
        return new ResponseEntity<>(headers, HttpStatus.METHOD_NOT_ALLOWED);
    }

    private ObjectNode initialize(JsonNode params) {
        String requested = params.path("protocolVersion").asText(null);
        ObjectNode result = mapper.createObjectNode();
        result.put("protocolVersion", SUPPORTED_PROTOCOL_VERSIONS.contains(requested) ? requested : SUPPORTED_PROTOCOL_VERSIONS.get(0));

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
        return result;
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
        String name = params.path("name").asText(null);
        McpTool tool = toolRegistry.getTool(name);
        if (tool == null) {
            return JsonRpc.error(mapper, id, JsonRpc.INVALID_PARAMS, "Unknown tool: " + name);
        }

        long start = System.currentTimeMillis();
        ObjectNode result = mapper.createObjectNode();
        ArrayNode content = result.putArray("content");
        try {
            McpArguments arguments = new McpArguments(params.get("arguments"), McpSchema.propertyNames(tool.inputSchema));
            JsonNode body = mapper.valueToTree(tool.handler.call(arguments));
            content.addObject().put("type", "text").put("text", mapper.writeValueAsString(body));
            if (body != null && body.isObject()) {
                result.set("structuredContent", body);
            }
            result.put("isError", false);
            LOG.info("MCP tools/call {} succeeded in {} ms", tool.name, System.currentTimeMillis() - start);
        } catch (McpToolException | ApiHttpErrorException e) {
            // Bad arguments, or a 4xx-style validation error from the shared REST service code.
            content.addObject().put("type", "text").put("text", e.getMessage());
            result.put("isError", true);
            LOG.warn("MCP tools/call {} returned a tool error in {} ms: {}", tool.name, System.currentTimeMillis() - start, e.getMessage());
        } catch (Exception e) {
            content.addObject().put("type", "text").put("text", "OncoKB failed to process this request. Please try again later.");
            result.put("isError", true);
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
        String uri = params.path("uri").asText(null);
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

    private ResponseEntity<byte[]> json(HttpStatus status, JsonNode node) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        // writeValueAsBytes always encodes UTF-8, so non-ASCII text in descriptions survives.
        return new ResponseEntity<>(mapper.writeValueAsBytes(node), headers, status);
    }
}
