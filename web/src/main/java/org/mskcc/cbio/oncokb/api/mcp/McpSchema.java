package org.mskcc.cbio.oncokb.api.mcp;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Builder for a tool's JSON Schema {@code inputSchema}.
 */
final class McpSchema {
    private final ObjectNode schema = JsonNodeFactory.instance.objectNode();
    private final ObjectNode properties = schema.put("type", "object").putObject("properties");
    private final ArrayNode required = JsonNodeFactory.instance.arrayNode();

    static McpSchema object() {
        return new McpSchema();
    }

    McpSchema string(String name, String description) {
        property(name, "string", description);
        return this;
    }

    McpSchema string(String name, String description, String defaultValue) {
        property(name, "string", description).put("default", defaultValue);
        return this;
    }

    McpSchema stringEnum(String name, String description, String... values) {
        ArrayNode allowed = property(name, "string", description).putArray("enum");
        for (String value : values) {
            allowed.add(value);
        }
        return this;
    }

    McpSchema stringEnum(String name, String description, Class<? extends Enum<?>> type) {
        return stringEnum(name, description, names(type));
    }

    McpSchema stringEnumArray(String name, String description, Class<? extends Enum<?>> type) {
        ObjectNode items = property(name, "array", description).putObject("items");
        items.put("type", "string");
        ArrayNode allowed = items.putArray("enum");
        for (String value : names(type)) {
            allowed.add(value);
        }
        return this;
    }

    McpSchema integer(String name, String description) {
        property(name, "integer", description);
        return this;
    }

    McpSchema bool(String name, String description, boolean defaultValue) {
        property(name, "boolean", description).put("default", defaultValue);
        return this;
    }

    McpSchema required(String... names) {
        for (String name : names) {
            if (!properties.has(name)) {
                throw new IllegalArgumentException("Required argument '" + name + "' is not a declared property");
            }
            required.add(name);
        }
        return this;
    }

    ObjectNode build() {
        if (required.size() > 0) {
            schema.set("required", required);
        }
        schema.put("additionalProperties", false);
        return schema;
    }

    /**
     * Argument names declared in a built schema, used to reject unknown arguments.
     */
    static Set<String> propertyNames(ObjectNode schema) {
        Set<String> names = new LinkedHashSet<>();
        Iterator<String> fields = schema.path("properties").fieldNames();
        while (fields.hasNext()) {
            names.add(fields.next());
        }
        return names;
    }

    private ObjectNode property(String name, String type, String description) {
        ObjectNode property = properties.putObject(name);
        property.put("type", type);
        property.put("description", description);
        return property;
    }

    private static String[] names(Class<? extends Enum<?>> type) {
        Enum<?>[] constants = type.getEnumConstants();
        String[] names = new String[constants.length];
        for (int i = 0; i < constants.length; i++) {
            names[i] = constants[i].name();
        }
        return names;
    }
}
