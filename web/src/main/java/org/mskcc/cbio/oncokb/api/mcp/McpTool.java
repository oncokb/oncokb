package org.mskcc.cbio.oncokb.api.mcp;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * An MCP tool: its advertised definition plus the handler that runs it.
 */
final class McpTool {
    /**
     * Runs the tool and returns the value to serialize as the result. An
     * {@link org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException} or
     * {@link McpToolException} is reported to the client as a tool error.
     */
    interface Handler {
        Object call(McpArguments arguments) throws Exception;
    }

    final String name;
    final String description;
    final ObjectNode inputSchema;
    final Handler handler;

    McpTool(String name, String description, ObjectNode inputSchema, Handler handler) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
        this.handler = handler;
    }
}
