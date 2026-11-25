package org.mskcc.cbio.oncokb.api.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Builds {@code tools/call} results so each payload is sent exactly once.
 * <ul>
 *     <li>An object payload goes in {@code structuredContent}; the text content is only a short
 *     summary pointing at it.</li>
 *     <li>Any other payload (a list, a scalar) cannot be {@code structuredContent}, which must be a
 *     JSON object, so it is sent once as serialized JSON text.</li>
 * </ul>
 */
final class McpToolResult {
    // Top-level annotation fields worth surfacing in the one-line summary, in display order.
    private static final String[][] SUMMARY_FIELDS = {
        {"oncogenic", "oncogenic"},
        {"pathogenic", "pathogenic"},
        {"penetrance", "penetrance"},
        {"highestSensitiveLevel", "highest sensitive level"},
        {"highestResistanceLevel", "highest resistance level"},
        {"highestDiagnosticImplicationLevel", "highest diagnostic level"},
        {"highestPrognosticImplicationLevel", "highest prognostic level"},
        {"highestFdaLevel", "highest FDA level"},
        {"hotspot", "hotspot"},
        {"dataVersion", "data version"},
    };
    private static final int MAX_LISTED_FIELDS = 12;

    private McpToolResult() {
    }

    static ObjectNode success(ObjectMapper mapper, JsonNode payload) throws JsonProcessingException {
        ObjectNode result = mapper.createObjectNode();
        if (payload != null && payload.isObject()) {
            result.putArray("content").addObject().put("type", "text").put("text", summarize(payload));
            result.set("structuredContent", payload);
        } else {
            result.putArray("content").addObject().put("type", "text").put("text", mapper.writeValueAsString(payload));
        }
        result.put("isError", false);
        return result;
    }

    static ObjectNode error(ObjectMapper mapper, String message) {
        ObjectNode result = mapper.createObjectNode();
        result.putArray("content").addObject().put("type", "text").put("text", message);
        result.put("isError", true);
        return result;
    }

    /**
     * A one-paragraph, human-readable summary of an object payload. It never repeats the payload;
     * the full data is in structuredContent.
     */
    static String summarize(JsonNode payload) {
        StringBuilder text = new StringBuilder();
        JsonNode query = payload.path("query");
        if (query.isObject()) {
            String subject = join(textOf(query, "hugoSymbol"), textOf(query, "alteration"));
            if (!subject.isEmpty()) {
                text.append(subject);
                String tumorType = textOf(query, "tumorType");
                if (!tumorType.isEmpty()) {
                    text.append(" in ").append(tumorType);
                }
                text.append(": ");
            }
            List<String> facts = new ArrayList<>();
            for (String[] field : SUMMARY_FIELDS) {
                String value = textOf(payload, field[0]);
                if (!value.isEmpty()) {
                    facts.add(field[1] + " " + value);
                }
            }
            String knownEffect = textOf(payload.path("mutationEffect"), "knownEffect");
            if (!knownEffect.isEmpty()) {
                facts.add("mutation effect " + knownEffect);
            }
            JsonNode treatments = payload.path("treatments");
            if (treatments.isArray()) {
                facts.add(treatments.size() + " treatment" + (treatments.size() == 1 ? "" : "s"));
            }
            text.append(facts.isEmpty() ? "annotation returned" : String.join("; ", facts)).append(". ");
            String summary = textOf(payload, "variantSummary");
            if (!summary.isEmpty()) {
                text.append(summary).append(' ');
            }
        } else {
            List<String> names = new ArrayList<>();
            Iterator<String> fields = payload.fieldNames();
            while (fields.hasNext() && names.size() < MAX_LISTED_FIELDS) {
                names.add(fields.next());
            }
            text.append("Returned ").append(payload.size()).append(" field").append(payload.size() == 1 ? "" : "s");
            if (!names.isEmpty()) {
                text.append(": ").append(String.join(", ", names));
                if (payload.size() > names.size()) {
                    text.append(", ...");
                }
            }
            text.append(". ");
        }
        text.append("The complete result is in structuredContent.");
        return text.toString();
    }

    private static String textOf(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isContainerNode()) {
            return "";
        }
        return value.asText().trim();
    }

    private static String join(String a, String b) {
        return (a + " " + b).trim();
    }
}
