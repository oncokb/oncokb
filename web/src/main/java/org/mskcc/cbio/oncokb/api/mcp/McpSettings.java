package org.mskcc.cbio.oncokb.api.mcp;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configuration of the MCP endpoint, read from {@code config.properties} or a JVM system property
 * of the same name (see {@link PropertiesUtils}).
 * <ul>
 *     <li>{@value #ENABLED}: {@code true} registers the {@code /api/v1/mcp} endpoint. Default
 *     {@code false}. Only the gateway-fronted enterprise deployment should turn it on; the
 *     no-token public build ({@code -P public-api}) never scans the MCP package at all.</li>
 *     <li>{@value #ALLOWED_ORIGINS}: comma-separated browser origins (e.g.
 *     {@code https://www.oncokb.org}) allowed to call the endpoint. Requests without an
 *     {@code Origin} header (server-to-server MCP clients, the gateway) are always allowed;
 *     requests with any other {@code Origin} get 403. Default: none.</li>
 *     <li>{@value #MAX_REQUEST_BYTES}: largest accepted request body. Default
 *     {@value #DEFAULT_MAX_REQUEST_BYTES}.</li>
 * </ul>
 */
public final class McpSettings {
    public static final String ENABLED = "mcp.enabled";
    public static final String ALLOWED_ORIGINS = "mcp.allowed_origins";
    public static final String MAX_REQUEST_BYTES = "mcp.max_request_bytes";
    static final int DEFAULT_MAX_REQUEST_BYTES = 64 * 1024;

    private McpSettings() {
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(StringUtils.trimToEmpty(PropertiesUtils.getProperties(ENABLED)));
    }

    public static Set<String> allowedOrigins() {
        return parseOrigins(PropertiesUtils.getProperties(ALLOWED_ORIGINS));
    }

    public static int maxRequestBytes() {
        String value = StringUtils.trimToNull(PropertiesUtils.getProperties(MAX_REQUEST_BYTES));
        if (value == null) {
            return DEFAULT_MAX_REQUEST_BYTES;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : DEFAULT_MAX_REQUEST_BYTES;
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_REQUEST_BYTES;
        }
    }

    static Set<String> parseOrigins(String value) {
        Set<String> origins = new LinkedHashSet<>();
        if (value != null) {
            for (String origin : value.split(",")) {
                String trimmed = StringUtils.removeEnd(origin.trim(), "/");
                if (!trimmed.isEmpty()) {
                    origins.add(trimmed.toLowerCase());
                }
            }
        }
        return Collections.unmodifiableSet(origins);
    }
}
