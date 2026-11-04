package org.mskcc.cbio.oncokb.api.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Typed access to the {@code arguments} object of a tools/call request.
 * <p>
 * Values are read the way Spring MVC binds the matching REST query parameters, so a tool accepts
 * what the REST endpoint accepts. Each value may be given as a JSON value of the natural type or as
 * a string. A missing or null argument reads as null unless a default is given.
 */
final class McpArguments {
    private final JsonNode arguments;

    /**
     * @param arguments the tools/call {@code arguments} value; null or JSON null means no arguments
     * @param allowed   argument names declared in the tool's input schema; any other name is rejected
     */
    McpArguments(JsonNode arguments, Set<String> allowed) throws McpToolException {
        if (arguments != null && !arguments.isNull() && !arguments.isObject()) {
            throw new McpToolException("Tool arguments must be a JSON object.");
        }
        this.arguments = arguments == null || arguments.isNull() ? null : arguments;
        if (this.arguments != null) {
            Iterator<String> names = this.arguments.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                if (!allowed.contains(name)) {
                    throw new McpToolException("Unknown argument '" + name + "'. Valid arguments: " + (allowed.isEmpty() ? "none" : StringUtils.join(allowed, ", ")) + ".");
                }
            }
        }
    }

    String string(String name) throws McpToolException {
        return string(name, null);
    }

    String string(String name, String defaultValue) throws McpToolException {
        JsonNode value = value(name);
        if (value == null) {
            return defaultValue;
        }
        if (!value.isValueNode()) {
            throw new McpToolException("Argument '" + name + "' must be a single value, not an object or array.");
        }
        return value.asText();
    }

    String requiredString(String name) throws McpToolException {
        String value = string(name);
        if (value == null) {
            throw missing(name);
        }
        return value;
    }

    Integer integer(String name) throws McpToolException {
        JsonNode value = value(name);
        if (value == null) {
            return null;
        }
        if (value.isIntegralNumber() && value.canConvertToInt()) {
            return value.intValue();
        }
        String text = string(name).trim();
        // Spring MVC binds an empty query parameter to null for Integer.
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            throw invalid(name, text);
        }
    }

    Boolean bool(String name, boolean defaultValue) throws McpToolException {
        JsonNode value = value(name);
        if (value == null) {
            return defaultValue;
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        // Same spellings Spring MVC accepts for a Boolean query parameter.
        String text = string(name).trim().toLowerCase();
        if (Arrays.asList("true", "on", "yes", "1").contains(text)) {
            return true;
        }
        if (Arrays.asList("false", "off", "no", "0").contains(text)) {
            return false;
        }
        throw invalid(name, text);
    }

    <E extends Enum<E>> E requiredEnum(String name, Class<E> type) throws McpToolException {
        String text = string(name);
        if (text == null || text.trim().isEmpty()) {
            throw missing(name);
        }
        return toEnum(name, text.trim(), type);
    }

    /**
     * Reads a list of enum values given as a JSON array or as a comma-separated string, matching
     * how the REST endpoint takes a multi-valued query parameter.
     */
    <E extends Enum<E>> List<E> enumList(String name, Class<E> type) throws McpToolException {
        JsonNode value = value(name);
        if (value == null) {
            return null;
        }
        List<String> items = new ArrayList<>();
        if (value.isArray()) {
            for (JsonNode item : value) {
                if (!item.isValueNode()) {
                    throw new McpToolException("Argument '" + name + "' must be an array of strings.");
                }
                items.add(item.asText());
            }
        } else {
            items.addAll(Arrays.asList(string(name).split(",")));
        }
        List<E> result = new ArrayList<>();
        for (String item : items) {
            if (!item.trim().isEmpty()) {
                result.add(toEnum(name, item.trim(), type));
            }
        }
        return result;
    }

    private <E extends Enum<E>> E toEnum(String name, String text, Class<E> type) throws McpToolException {
        try {
            return Enum.valueOf(type, text);
        } catch (IllegalArgumentException e) {
            throw new McpToolException("Invalid value '" + text + "' for argument '" + name + "'. Allowed values: " + StringUtils.join(type.getEnumConstants(), ", ") + ".");
        }
    }

    private JsonNode value(String name) {
        JsonNode value = arguments == null ? null : arguments.get(name);
        return value == null || value.isNull() ? null : value;
    }

    private static McpToolException missing(String name) {
        return new McpToolException("Missing required argument '" + name + "'.");
    }

    private static McpToolException invalid(String name, String text) {
        return new McpToolException("Invalid value '" + text + "' for argument '" + name + "'.");
    }
}
