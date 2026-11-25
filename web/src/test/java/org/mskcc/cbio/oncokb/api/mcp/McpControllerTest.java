package org.mskcc.cbio.oncokb.api.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Protocol tests for the MCP endpoint. Uses stub tools and resources, so no database is needed.
 */
public class McpControllerTest {
    private static final String ALLOWED_ORIGIN = "https://www.oncokb.org";
    private static final int MAX_BYTES = 2048;
    private final ObjectMapper mapper = new ObjectMapper();
    private McpController controller;
    private MockMvc mvc;

    @Before
    public void setUp() {
        controller = new McpController();
        controller.toolRegistry = new StubToolRegistry();
        controller.resourceRegistry = new StubResourceRegistry();
        controller.setMaxRequestBytes(MAX_BYTES);
        mvc = MockMvcBuilders.standaloneSetup(controller)
            .addFilters(new McpOriginFilter(true, McpSettings.parseOrigins(ALLOWED_ORIGIN + "/")))
            .build();
    }

    // ---- initialize / version negotiation ----

    @Test
    public void initializeWithSupportedVersion() throws Exception {
        JsonNode response = rpcOk(request(1, "initialize", "{\"protocolVersion\":\"2025-11-25\",\"capabilities\":{},\"clientInfo\":{\"name\":\"t\",\"version\":\"1\"}}"));
        JsonNode result = response.get("result");
        assertEquals(1, response.get("id").asInt());
        assertEquals("2025-11-25", result.get("protocolVersion").asText());
        assertFalse(result.at("/capabilities/tools/listChanged").asBoolean());
        assertTrue(result.at("/capabilities/resources").isObject());
        assertEquals("oncokb", result.at("/serverInfo/name").asText());
        assertTrue(result.get("instructions").asText().contains("OncoKB"));
    }

    @Test
    public void initializeWithOtherWellFormedVersionAnswersWithTheOnlySupportedOne() throws Exception {
        for (String requested : Arrays.asList("2025-06-18", "2024-11-05", "2026-07-28")) {
            JsonNode response = rpcOk(request("init-" + requested, "initialize", "{\"protocolVersion\":\"" + requested + "\"}"));
            assertEquals(requested, "2025-11-25", response.at("/result/protocolVersion").asText());
        }
    }

    @Test
    public void initializeWithMalformedOrMissingVersionIsRejected() throws Exception {
        for (String params : Arrays.asList("{\"protocolVersion\":\"1.0.0\"}", "{}", "{\"protocolVersion\":20251125}")) {
            JsonNode response = rpcOk(request(7, "initialize", params));
            assertEquals(params, -32602, response.at("/error/code").asInt());
            assertEquals("Unsupported protocol version", response.at("/error/message").asText());
            assertEquals("2025-11-25", response.at("/error/data/supported/0").asText());
            assertEquals(7, response.get("id").asInt());
        }
    }

    @Test
    public void unsupportedProtocolVersionHeaderIsRejectedWith400() throws Exception {
        MockHttpServletResponse response = perform(mcpPost(request(3, "tools/list", null)).header("MCP-Protocol-Version", "2025-06-18"));
        assertEquals(400, response.getStatus());
        JsonNode body = json(response);
        assertEquals(-32600, body.at("/error/code").asInt());
        assertEquals(3, body.get("id").asInt());
        assertEquals("2025-11-25", body.at("/error/data/supported/0").asText());
    }

    @Test
    public void supportedOrAbsentProtocolVersionHeaderIsAccepted() throws Exception {
        assertEquals(200, perform(mcpPost(request(1, "ping", null)).header("MCP-Protocol-Version", "2025-11-25")).getStatus());
        assertEquals(200, perform(mcpPost(request(1, "ping", null))).getStatus());
    }

    @Test
    public void pingReturnsEmptyResult() throws Exception {
        JsonNode response = rpcOk(request("p", "ping", null));
        assertEquals("p", response.get("id").asText());
        assertEquals(0, response.get("result").size());
    }

    // ---- notifications and client responses ----

    @Test
    public void notificationsGet202WithNoBody() throws Exception {
        for (String method : Arrays.asList("notifications/initialized", "notifications/cancelled", "notifications/unknown")) {
            MockHttpServletResponse response = perform(mcpPost("{\"jsonrpc\":\"2.0\",\"method\":\"" + method + "\"}"));
            assertEquals(method, 202, response.getStatus());
            assertEquals(method, 0, response.getContentAsByteArray().length);
        }
    }

    @Test
    public void clientResponsesGet202WithNoBody() throws Exception {
        MockHttpServletResponse response = perform(mcpPost("{\"jsonrpc\":\"2.0\",\"id\":5,\"result\":{}}"));
        assertEquals(202, response.getStatus());
        assertEquals(0, response.getContentAsByteArray().length);
    }

    // ---- JSON-RPC errors ----

    @Test
    public void malformedJsonIsParseError() throws Exception {
        MockHttpServletResponse response = perform(mcpPost("{\"jsonrpc\":\"2.0\",\"id\":1,"));
        assertEquals(400, response.getStatus());
        JsonNode body = json(response);
        assertEquals(-32700, body.at("/error/code").asInt());
        assertTrue(body.get("id").isNull());
        assertNoLeak(response);
    }

    @Test
    public void batchesAreRejected() throws Exception {
        MockHttpServletResponse response = perform(mcpPost("[" + request(1, "ping", null) + "," + request(2, "ping", null) + "]"));
        assertEquals(400, response.getStatus());
        JsonNode body = json(response);
        assertEquals(-32600, body.at("/error/code").asInt());
        assertTrue(body.at("/error/message").asText().contains("batch"));
    }

    @Test
    public void invalidRequestsAreRejected() throws Exception {
        String[] invalid = {
            "",
            "42",
            "{\"id\":1,\"method\":\"ping\"}",
            "{\"jsonrpc\":\"1.0\",\"id\":1,\"method\":\"ping\"}",
            "{\"jsonrpc\":\"2.0\",\"id\":null,\"method\":\"ping\"}",
            "{\"jsonrpc\":\"2.0\",\"id\":{\"a\":1},\"method\":\"ping\"}",
            "{\"jsonrpc\":\"2.0\",\"id\":1}",
            "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":5}",
        };
        for (String body : invalid) {
            MockHttpServletResponse response = perform(mcpPost(body));
            assertEquals(body, 400, response.getStatus());
            assertEquals(body, -32600, json(response).at("/error/code").asInt());
        }
    }

    @Test
    public void unknownMethodIsMethodNotFound() throws Exception {
        JsonNode response = rpcOk(request(9, "sampling/createMessage", "{}"));
        assertEquals(-32601, response.at("/error/code").asInt());
        assertEquals(9, response.get("id").asInt());
    }

    @Test
    public void nonObjectParamsAreInvalidParams() throws Exception {
        JsonNode response = rpcOk("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":[1]}");
        assertEquals(-32602, response.at("/error/code").asInt());
    }

    // ---- tools ----

    @Test
    public void toolsListAdvertisesToolsAsReadOnly() throws Exception {
        JsonNode tools = rpcOk(request(1, "tools/list", null)).at("/result/tools");
        assertEquals(5, tools.size());
        JsonNode first = tools.get(0);
        assertEquals("annotate_stub", first.get("name").asText());
        assertEquals("object", first.at("/inputSchema/type").asText());
        assertEquals("hugoSymbol", first.at("/inputSchema/required/0").asText());
        assertTrue(first.at("/annotations/readOnlyHint").asBoolean());
    }

    @Test
    public void toolsCallSendsObjectPayloadOnceAsStructuredContent() throws Exception {
        JsonNode result = rpcOk(request(1, "tools/call", "{\"name\":\"annotate_stub\",\"arguments\":{\"hugoSymbol\":\"BRAF\"}}")).get("result");
        assertFalse(result.get("isError").asBoolean());
        JsonNode structured = result.get("structuredContent");
        assertEquals("Oncogenic", structured.get("oncogenic").asText());
        assertEquals(2, structured.get("treatments").size());

        JsonNode content = result.get("content");
        assertEquals(1, content.size());
        String text = content.get(0).get("text").asText();
        assertEquals("text", content.get(0).get("type").asText());
        assertTrue(text, text.startsWith("BRAF V600E in Melanoma: oncogenic Oncogenic; highest sensitive level LEVEL_1;"));
        assertTrue(text, text.contains("2 treatments"));
        assertTrue(text, text.contains("structuredContent"));
        // The payload is not duplicated into the text content.
        assertFalse(text, text.contains("{"));
        assertFalse(text, text.contains("dabrafenib"));
        assertTrue(text.length() < mapper.writeValueAsString(structured).length());
    }

    @Test
    public void toolsCallSendsNonObjectPayloadOnceAsText() throws Exception {
        JsonNode result = rpcOk(request(1, "tools/call", "{\"name\":\"list_stub\"}")).get("result");
        assertFalse(result.get("isError").asBoolean());
        assertNull(result.get("structuredContent"));
        assertEquals(1, result.get("content").size());
        assertEquals("[\"a\",\"b\"]", result.at("/content/0/text").asText());
    }

    @Test
    public void unknownToolIsInvalidParams() throws Exception {
        JsonNode response = rpcOk(request(1, "tools/call", "{\"name\":\"nope\",\"arguments\":{}}"));
        assertEquals(-32602, response.at("/error/code").asInt());
        assertEquals("Unknown tool: nope", response.at("/error/message").asText());
    }

    @Test
    public void malformedToolsCallParamsAreInvalidParams() throws Exception {
        for (String params : Arrays.asList("{}", "{\"name\":3}", "{\"name\":\"annotate_stub\",\"arguments\":[\"BRAF\"]}")) {
            assertEquals(params, -32602, rpcOk(request(1, "tools/call", params)).at("/error/code").asInt());
        }
    }

    @Test
    public void invalidToolArgumentsAreToolExecutionErrors() throws Exception {
        JsonNode result = rpcOk(request(1, "tools/call", "{\"name\":\"annotate_stub\",\"arguments\":{\"hugo\":\"BRAF\"}}")).get("result");
        assertTrue(result.get("isError").asBoolean());
        assertTrue(result.at("/content/0/text").asText().startsWith("Unknown argument 'hugo'"));

        result = rpcOk(request(1, "tools/call", "{\"name\":\"annotate_stub\",\"arguments\":{}}")).get("result");
        assertTrue(result.get("isError").asBoolean());
        assertEquals("Missing required argument 'hugoSymbol'.", result.at("/content/0/text").asText());
    }

    @Test
    public void serviceValidationErrorsAreToolExecutionErrors() throws Exception {
        JsonNode result = rpcOk(request(1, "tools/call", "{\"name\":\"bad_request_stub\"}")).get("result");
        assertTrue(result.get("isError").asBoolean());
        assertEquals("entrezGeneId and hugoSymbol are not the same gene.", result.at("/content/0/text").asText());
    }

    @Test
    public void unexpectedToolFailuresDoNotLeakDetails() throws Exception {
        MockHttpServletResponse response = perform(mcpPost(request(1, "tools/call", "{\"name\":\"boom_stub\"}")));
        assertEquals(200, response.getStatus());
        JsonNode result = json(response).get("result");
        assertTrue(result.get("isError").asBoolean());
        assertEquals("OncoKB failed to process this request. Please try again later.", result.at("/content/0/text").asText());
        assertNoLeak(response);
    }

    @Test
    public void unexpectedDispatchFailuresAreInternalErrors() throws Exception {
        controller.toolRegistry = new StubToolRegistry() {
            @Override
            Collection<McpTool> getTools() {
                throw new IllegalStateException("secret internal detail");
            }
        };
        MockHttpServletResponse response = perform(mcpPost(request(1, "tools/list", null)));
        assertEquals(200, response.getStatus());
        assertEquals(-32603, json(response).at("/error/code").asInt());
        assertNoLeak(response);
    }

    // ---- resources ----

    @Test
    public void resourcesListAndRead() throws Exception {
        JsonNode resources = rpcOk(request(1, "resources/list", null)).at("/result/resources");
        assertEquals(1, resources.size());
        assertEquals("oncokb://stub", resources.get(0).get("uri").asText());

        JsonNode contents = rpcOk(request(2, "resources/read", "{\"uri\":\"oncokb://stub\"}")).at("/result/contents/0");
        assertEquals("application/json", contents.get("mimeType").asText());
        assertEquals("{\"x\":1}", contents.get("text").asText());

        assertEquals(0, rpcOk(request(3, "resources/templates/list", null)).at("/result/resourceTemplates").size());
    }

    @Test
    public void resourcesReadErrors() throws Exception {
        JsonNode response = rpcOk(request(1, "resources/read", "{\"uri\":\"oncokb://missing\"}"));
        assertEquals(-32002, response.at("/error/code").asInt());
        assertEquals("oncokb://missing", response.at("/error/data/uri").asText());
        assertEquals(-32602, rpcOk(request(2, "resources/read", "{}")).at("/error/code").asInt());
    }

    // ---- Origin validation and CORS ----

    @Test
    public void disallowedOriginIsForbidden() throws Exception {
        for (String origin : Arrays.asList("https://evil.example", "null", "http://www.oncokb.org", "https://www.oncokb.org.evil.example")) {
            MockHttpServletResponse response = perform(mcpPost(request(1, "ping", null)).header("Origin", origin));
            assertEquals(origin, 403, response.getStatus());
            assertEquals(origin, -32600, json(response).at("/error/code").asInt());
            assertNull(origin, response.getHeader("Access-Control-Allow-Origin"));
        }
    }

    @Test
    public void absentOriginIsAllowedWithoutCorsHeaders() throws Exception {
        MockHttpServletResponse response = perform(mcpPost(request(1, "ping", null)));
        assertEquals(200, response.getStatus());
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void allowlistedOriginGetsOriginSpecificCors() throws Exception {
        MockHttpServletResponse response = perform(mcpPost(request(1, "ping", null)).header("Origin", "HTTPS://www.oncokb.org"));
        assertEquals(200, response.getStatus());
        assertEquals("HTTPS://www.oncokb.org", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("Origin", response.getHeader("Vary"));
    }

    @Test
    public void preflightIsAnsweredOnlyForAllowlistedOrigins() throws Exception {
        MockHttpServletResponse allowed = perform(options("/api/v1/mcp").servletPath("/api/v1")
            .header("Origin", ALLOWED_ORIGIN).header("Access-Control-Request-Method", "POST"));
        assertEquals(204, allowed.getStatus());
        assertEquals(ALLOWED_ORIGIN, allowed.getHeader("Access-Control-Allow-Origin"));
        assertEquals("POST", allowed.getHeader("Access-Control-Allow-Methods"));
        assertTrue(allowed.getHeader("Access-Control-Allow-Headers").contains("MCP-Protocol-Version"));

        MockHttpServletResponse denied = perform(options("/api/v1/mcp").servletPath("/api/v1")
            .header("Origin", "https://evil.example").header("Access-Control-Request-Method", "POST"));
        assertEquals(403, denied.getStatus());
        assertNull(denied.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void originIsHiddenFromDownstreamFilters() throws Exception {
        final String[] seen = {"unset"};
        McpOriginFilter filter = new McpOriginFilter(true, McpSettings.parseOrigins(ALLOWED_ORIGIN));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/mcp");
        request.addHeader("Origin", ALLOWED_ORIGIN);
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) ->
            seen[0] = ((javax.servlet.http.HttpServletRequest) req).getHeader("Origin"));
        assertNull("Tomcat's wildcard CorsFilter must not see the Origin", seen[0]);
    }

    @Test
    public void filterIgnoresOtherPathsAndDisabledMcp() throws Exception {
        final int[] calls = {0};
        MockHttpServletRequest rest = new MockHttpServletRequest("GET", "/api/v1/info");
        rest.addHeader("Origin", "https://evil.example");
        new McpOriginFilter(true, McpSettings.parseOrigins(ALLOWED_ORIGIN)).doFilter(rest, new MockHttpServletResponse(), (req, res) -> calls[0]++);

        MockHttpServletRequest mcp = new MockHttpServletRequest("POST", "/api/v1/mcp");
        mcp.addHeader("Origin", "https://evil.example");
        new McpOriginFilter(false, McpSettings.parseOrigins(ALLOWED_ORIGIN)).doFilter(mcp, new MockHttpServletResponse(), (req, res) -> calls[0]++);
        assertEquals(2, calls[0]);
    }

    @Test
    public void filterHonoursContextPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/app/api/v1/mcp");
        request.setContextPath("/app");
        assertTrue(McpOriginFilter.isMcpPath(request));
    }

    // ---- request size and methods ----

    @Test
    public void oversizedBodyIsRejected() throws Exception {
        String big = request(1, "tools/call", "{\"name\":\"annotate_stub\",\"arguments\":{\"hugoSymbol\":\"" + repeat('A', MAX_BYTES) + "\"}}");
        MockHttpServletResponse response = perform(mcpPost(big));
        assertEquals(413, response.getStatus());
        assertEquals(-32600, json(response).at("/error/code").asInt());
    }

    @Test
    public void oversizedBodyWithoutContentLengthIsRejected() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/mcp") {
            @Override
            public int getContentLength() {
                return -1;
            }
        };
        request.setContent(repeat('x', MAX_BYTES + 1).getBytes(StandardCharsets.UTF_8));
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, controller.handle(request).getStatusCode());
    }

    @Test
    public void bodyAtTheLimitIsAccepted() throws Exception {
        String ping = request(1, "ping", null);
        String padded = ping.substring(0, ping.length() - 1) + "," + "\"pad\":\"" + repeat('p', MAX_BYTES - ping.length() - 10) + "\"}";
        assertTrue(padded.length() <= MAX_BYTES);
        assertEquals(200, perform(mcpPost(padded)).getStatus());
    }

    @Test
    public void onlyPostIsServed() throws Exception {
        for (MockHttpServletRequestBuilder builder : Arrays.asList(
            get("/api/v1/mcp").servletPath("/api/v1").accept("text/event-stream"),
            delete("/api/v1/mcp").servletPath("/api/v1"),
            put("/api/v1/mcp").servletPath("/api/v1").content("{}"),
            options("/api/v1/mcp").servletPath("/api/v1"))) {
            MockHttpServletResponse response = perform(builder);
            assertEquals(405, response.getStatus());
            assertEquals("POST", response.getHeader("Allow"));
        }
    }

    @Test
    public void nonCanonicalPathAliasesAreNotServed() throws Exception {
        MockHttpServletResponse response = perform(post("/api/v1/mcp.json").servletPath("/api/v1")
            .contentType(MediaType.APPLICATION_JSON).content(request(1, "ping", null)).header("Origin", "https://evil.example"));
        assertEquals(404, response.getStatus());
    }

    // ---- enablement ----

    @Test
    public void mcpIsDisabledUnlessExplicitlyEnabled() {
        String previous = System.getProperty(McpSettings.ENABLED);
        try {
            System.clearProperty(McpSettings.ENABLED);
            assertFalse(new McpEnabledCondition().matches(null, null));
            System.setProperty(McpSettings.ENABLED, "false");
            assertFalse(new McpEnabledCondition().matches(null, null));
            System.setProperty(McpSettings.ENABLED, "true");
            assertTrue(new McpEnabledCondition().matches(null, null));
        } finally {
            if (previous == null) {
                System.clearProperty(McpSettings.ENABLED);
            } else {
                System.setProperty(McpSettings.ENABLED, previous);
            }
        }
    }

    @Test
    public void settingsParsing() {
        assertEquals(new java.util.LinkedHashSet<>(Arrays.asList("https://a.example", "http://b.example:8080")),
            McpSettings.parseOrigins(" https://A.example/ ,, http://b.example:8080 "));
        assertTrue(McpSettings.parseOrigins(null).isEmpty());
        String previous = System.getProperty(McpSettings.MAX_REQUEST_BYTES);
        try {
            System.setProperty(McpSettings.MAX_REQUEST_BYTES, "not-a-number");
            assertEquals(McpSettings.DEFAULT_MAX_REQUEST_BYTES, McpSettings.maxRequestBytes());
            System.setProperty(McpSettings.MAX_REQUEST_BYTES, "1000");
            assertEquals(1000, McpSettings.maxRequestBytes());
        } finally {
            if (previous == null) {
                System.clearProperty(McpSettings.MAX_REQUEST_BYTES);
            } else {
                System.setProperty(McpSettings.MAX_REQUEST_BYTES, previous);
            }
        }
    }

    // ---- helpers ----

    private String request(Object id, String method, String params) throws Exception {
        return "{\"jsonrpc\":\"2.0\",\"id\":" + mapper.writeValueAsString(id) + ",\"method\":\"" + method + "\""
            + (params == null ? "" : ",\"params\":" + params) + "}";
    }

    private MockHttpServletRequestBuilder mcpPost(String body) {
        return post("/api/v1/mcp").servletPath("/api/v1")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
            .content(body);
    }

    private MockHttpServletResponse perform(MockHttpServletRequestBuilder builder) throws Exception {
        return mvc.perform(builder).andReturn().getResponse();
    }

    private JsonNode rpcOk(String body) throws Exception {
        MockHttpServletResponse response = perform(mcpPost(body));
        assertEquals(response.getContentAsString(), 200, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        JsonNode node = json(response);
        assertEquals("2.0", node.get("jsonrpc").asText());
        return node;
    }

    private JsonNode json(MockHttpServletResponse response) throws Exception {
        return mapper.readTree(response.getContentAsByteArray());
    }

    private static void assertNoLeak(MockHttpServletResponse response) throws Exception {
        String body = response.getContentAsString();
        assertFalse(body, body.contains("secret"));
        assertFalse(body, body.contains("Exception"));
        assertFalse(body, body.contains("\tat "));
    }

    private static String repeat(char c, int n) {
        char[] chars = new char[n];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    private static class StubToolRegistry extends McpToolRegistry {
        private final Map<String, McpTool> tools = new LinkedHashMap<>();

        StubToolRegistry() {
            add("annotate_stub", McpSchema.object().string("hugoSymbol", "gene").required("hugoSymbol"), args -> {
                args.requiredString("hugoSymbol");
                Map<String, Object> query = new LinkedHashMap<>();
                query.put("hugoSymbol", "BRAF");
                query.put("alteration", "V600E");
                query.put("tumorType", "Melanoma");
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("query", query);
                body.put("oncogenic", "Oncogenic");
                body.put("highestSensitiveLevel", "LEVEL_1");
                body.put("hotspot", true);
                body.put("treatments", Arrays.asList(Collections.singletonMap("drug", "dabrafenib"), Collections.singletonMap("drug", "vemurafenib")));
                return body;
            });
            add("list_stub", McpSchema.object(), args -> Arrays.asList("a", "b"));
            add("bad_request_stub", McpSchema.object(), args -> {
                throw new ApiHttpErrorException("entrezGeneId and hugoSymbol are not the same gene.", HttpStatus.BAD_REQUEST);
            });
            add("boom_stub", McpSchema.object(), args -> {
                throw new IllegalStateException("secret internal detail");
            });
            add("empty_stub", McpSchema.object(), args -> Collections.emptyMap());
        }

        private void add(String name, McpSchema schema, McpTool.Handler handler) {
            tools.put(name, new McpTool(name, name + " description", schema.build(), handler));
        }

        @Override
        Collection<McpTool> getTools() {
            return tools.values();
        }

        @Override
        McpTool getTool(String name) {
            return tools.get(name);
        }
    }

    private static class StubResourceRegistry extends McpResourceRegistry {
        private final McpResourceRegistry.Resource resource =
            new McpResourceRegistry.Resource("oncokb://stub", "Stub", "Stub resource", () -> Collections.singletonMap("x", 1));

        @Override
        java.util.List<Resource> getResources() {
            return Collections.singletonList(resource);
        }

        @Override
        Resource getResource(String uri) {
            return resource.uri.equals(uri) ? resource : null;
        }
    }
}
