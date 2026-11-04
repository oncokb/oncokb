package org.mskcc.cbio.oncokb.api.mcp;

/**
 * A tool call failed in a way the caller can act on, such as a missing or invalid argument. The
 * message is returned to the client as a tool error.
 */
class McpToolException extends Exception {
    McpToolException(String message) {
        super(message);
    }
}
