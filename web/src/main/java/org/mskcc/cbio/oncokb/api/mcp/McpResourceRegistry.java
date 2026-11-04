package org.mskcc.cbio.oncokb.api.mcp;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Static reference data exposed as MCP resources. Each resource is read through its matching
 * parameterless tool, so a resource and its tool always return the same data. They are also kept
 * as tools because client support for resources varies.
 */
@Component
public class McpResourceRegistry {
    static final String MIME_TYPE = "application/json";

    static final class Resource {
        final String uri;
        final String name;
        final String description;
        final String toolName;

        Resource(String uri, String name, String description, String toolName) {
            this.uri = uri;
            this.name = name;
            this.description = description;
            this.toolName = toolName;
        }
    }

    private static final List<Resource> RESOURCES = Collections.unmodifiableList(Arrays.asList(
        new Resource("oncokb://info", "OncoKB info", "OncoKB data version, app version, OncoTree version, and other release information.", "info"),
        new Resource("oncokb://levels", "OncoKB levels of evidence", "All public OncoKB levels of evidence with their descriptions.", "levels"),
        new Resource("oncokb://levels/sensitive", "OncoKB sensitive levels", "OncoKB therapeutic levels of evidence for sensitivity.", "levels_sensitive"),
        new Resource("oncokb://levels/resistance", "OncoKB resistance levels", "OncoKB therapeutic levels of evidence for resistance.", "levels_resistance"),
        new Resource("oncokb://levels/prognostic", "OncoKB prognostic levels", "OncoKB prognostic levels of evidence.", "levels_prognostic"),
        new Resource("oncokb://levels/diagnostic", "OncoKB diagnostic levels", "OncoKB diagnostic levels of evidence.", "levels_diagnostic"),
        new Resource("oncokb://cancer-gene-list", "OncoKB cancer gene list", "The OncoKB cancer gene list for the latest data version.", "utils_cancerGeneList")
    ));

    List<Resource> getResources() {
        return RESOURCES;
    }

    Resource getResource(String uri) {
        for (Resource resource : RESOURCES) {
            if (resource.uri.equals(uri)) {
                return resource;
            }
        }
        return null;
    }
}
