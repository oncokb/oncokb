package org.mskcc.cbio.oncokb.api.mcp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * An MCP tool backed by a public REST handler method. Calling the tool invokes {@link #method}
 * on {@link #bean} directly, so the tool shares validation and behavior with the REST endpoint.
 */
final class McpTool {
    /**
     * One handler method argument, bound from a tool argument with the same name as the
     * {@code @RequestParam}.
     */
    static final class Param {
        final String name;
        // Full generic type, so List<SomeEnum> parameters convert element-wise like Spring MVC.
        final TypeDescriptor type;
        final boolean required;
        // Raw @RequestParam defaultValue, or null when the annotation declares none.
        final String defaultValue;

        Param(String name, TypeDescriptor type, boolean required, String defaultValue) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.defaultValue = defaultValue;
        }
    }

    final String name;
    final String description;
    final String path;
    final ObjectNode inputSchema;
    final Object bean;
    final Method method;
    // Ordered to match the handler method's parameter list.
    final List<Param> params;

    McpTool(String name, String description, String path, ObjectNode inputSchema, Object bean, Method method, List<Param> params) {
        this.name = name;
        this.description = description;
        this.path = path;
        this.inputSchema = inputSchema;
        this.bean = bean;
        this.method = method;
        this.params = params;
    }
}
