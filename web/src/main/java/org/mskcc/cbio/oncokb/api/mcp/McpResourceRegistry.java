package org.mskcc.cbio.oncokb.api.mcp;

import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.service.CancerGeneService;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mskcc.cbio.oncokb.api.mcp.McpToolRegistry.body;
import static org.mskcc.cbio.oncokb.api.mcp.McpToolRegistry.levelDescriptions;

/**
 * Static reference data exposed as MCP resources. The same data is also available as tools,
 * because client support for resources varies.
 */
@Component
public class McpResourceRegistry {
    static final String MIME_TYPE = "application/json";

    /**
     * Produces the resource's content, serialized to JSON by the caller.
     */
    interface Reader {
        Object read() throws Exception;
    }

    static final class Resource {
        final String uri;
        final String name;
        final String description;
        final Reader reader;

        Resource(String uri, String name, String description, Reader reader) {
            this.uri = uri;
            this.name = name;
            this.description = description;
            this.reader = reader;
        }
    }

    @Autowired
    CacheFetcher cacheFetcher;

    @Autowired
    CancerGeneService cancerGeneService;

    private List<Resource> resources;

    @PostConstruct
    void registerResources() {
        List<Resource> registered = new ArrayList<>();
        registered.add(new Resource("oncokb://info", "OncoKB info",
            "OncoKB data version, app version, OncoTree version, and other release information.",
            () -> cacheFetcher.getOncoKBInfo()));
        registered.add(new Resource("oncokb://levels", "OncoKB levels of evidence",
            "All public OncoKB levels of evidence with their descriptions.",
            () -> levelDescriptions(LevelUtils.getPublicLevels())));
        registered.add(new Resource("oncokb://levels/sensitive", "OncoKB sensitive levels",
            "OncoKB therapeutic levels of evidence for sensitivity.",
            () -> levelDescriptions(LevelUtils.getSensitiveLevels())));
        registered.add(new Resource("oncokb://levels/resistance", "OncoKB resistance levels",
            "OncoKB therapeutic levels of evidence for resistance.",
            () -> levelDescriptions(LevelUtils.getResistanceLevels())));
        registered.add(new Resource("oncokb://levels/prognostic", "OncoKB prognostic levels",
            "OncoKB prognostic levels of evidence.",
            () -> levelDescriptions(LevelUtils.getPrognosticLevels())));
        registered.add(new Resource("oncokb://levels/diagnostic", "OncoKB diagnostic levels",
            "OncoKB diagnostic levels of evidence.",
            () -> levelDescriptions(LevelUtils.getDiagnosticLevels())));
        registered.add(new Resource("oncokb://cancer-gene-list", "OncoKB cancer gene list",
            "The OncoKB cancer gene list for the latest data version.",
            () -> body(cancerGeneService.getCancerGeneList(null))));
        resources = Collections.unmodifiableList(registered);
    }

    List<Resource> getResources() {
        return resources;
    }

    Resource getResource(String uri) {
        for (Resource resource : resources) {
            if (resource.uri.equals(uri)) {
                return resource;
            }
        }
        return null;
    }
}
