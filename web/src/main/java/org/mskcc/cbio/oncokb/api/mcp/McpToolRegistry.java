package org.mskcc.cbio.oncokb.api.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Builds MCP tools from the public REST API rather than hand-copying them, so the MCP endpoint
 * stays in sync with REST as endpoints change.
 * <p>
 * A tool is created for each handler method in {@link #SOURCE_PACKAGE} that is annotated
 * {@link PublicApi} and mapped to GET. Plain-text (.txt) variants are skipped because they duplicate
 * the JSON endpoints. POST batch variants are skipped because a client can call the single-query
 * tool repeatedly.
 * <p>
 * Tools are built lazily on first use rather than at startup. Scanning all {@code @Controller} beans
 * also reaches {@link McpController}, which depends on this registry, so scanning during bean
 * creation would cause a circular reference.
 */
@Component
public class McpToolRegistry implements ApplicationContextAware {
    static final String SOURCE_PACKAGE = "org.mskcc.cbio.oncokb.api.pub.v1";
    static final String REST_PATH_PREFIX = "/api/v1";

    private static final Logger LOG = LoggerFactory.getLogger(McpToolRegistry.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final DefaultConversionService conversionService = new DefaultConversionService();
    private ApplicationContext applicationContext;
    // Keyed by tool name. Sorted so tools/list output is stable.
    private volatile Map<String, McpTool> tools;

    /**
     * A tool call failed in a way the caller can act on, such as a bad argument or a 4xx from the
     * underlying endpoint. The message is returned to the client as a tool error.
     */
    static class McpToolException extends Exception {
        McpToolException(String message) {
            super(message);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    Collection<McpTool> getTools() {
        return toolMap().values();
    }

    McpTool getTool(String name) {
        return name == null ? null : toolMap().get(name);
    }

    /**
     * Binds the tool call arguments to the handler method parameters, invokes the handler, and
     * returns the response body.
     */
    Object invoke(McpTool tool, JsonNode arguments) throws Exception {
        if (arguments != null && !arguments.isNull() && !arguments.isObject()) {
            throw new McpToolException("Tool arguments must be a JSON object.");
        }
        rejectUnknownArguments(tool, arguments);

        Object[] args = new Object[tool.params.size()];
        for (int i = 0; i < tool.params.size(); i++) {
            args[i] = bindArgument(tool.params.get(i), arguments == null ? null : arguments.get(tool.params.get(i).name));
        }

        Object result;
        try {
            result = tool.method.invoke(tool.bean, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ApiHttpErrorException || cause instanceof IllegalArgumentException) {
                throw new McpToolException(cause.getMessage());
            }
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw e;
        }

        if (result instanceof ResponseEntity) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new McpToolException("Request failed with HTTP status " + response.getStatusCode().value() + " " + response.getStatusCode().getReasonPhrase() + ".");
            }
            return response.getBody();
        }
        return result;
    }

    private void rejectUnknownArguments(McpTool tool, JsonNode arguments) throws McpToolException {
        if (arguments == null || arguments.isNull()) {
            return;
        }
        Set<String> known = new LinkedHashSet<>();
        for (McpTool.Param param : tool.params) {
            known.add(param.name);
        }
        Iterator<String> names = arguments.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            if (!known.contains(name)) {
                throw new McpToolException("Unknown argument '" + name + "'. Valid arguments: " + (known.isEmpty() ? "none" : StringUtils.join(known, ", ")) + ".");
            }
        }
    }

    private Object bindArgument(McpTool.Param param, JsonNode value) throws McpToolException {
        String raw;
        if (value == null || value.isNull()) {
            raw = param.defaultValue;
        } else if (value.isValueNode()) {
            raw = value.asText();
        } else if (value.isArray() && isMultiValued(param.type)) {
            // REST takes multi-valued params as a comma-separated string; join so both forms work.
            List<String> items = new ArrayList<>();
            for (JsonNode item : value) {
                if (!item.isValueNode()) {
                    throw new McpToolException("Argument '" + param.name + "' must be an array of strings.");
                }
                items.add(item.asText());
            }
            raw = StringUtils.join(items, ",");
        } else {
            throw new McpToolException("Argument '" + param.name + "' must be a single value, not an object or array.");
        }

        if (raw == null) {
            if (param.required) {
                throw new McpToolException("Missing required argument '" + param.name + "'.");
            }
            return null;
        }
        try {
            return conversionService.convert(raw, TypeDescriptor.valueOf(String.class), param.type);
        } catch (ConversionException e) {
            throw new McpToolException("Invalid value '" + raw + "' for argument '" + param.name + "'.");
        }
    }

    private Map<String, McpTool> toolMap() {
        Map<String, McpTool> result = tools;
        if (result == null) {
            synchronized (this) {
                result = tools;
                if (result == null) {
                    result = Collections.unmodifiableMap(buildTools());
                    tools = result;
                    LOG.info("Registered {} MCP tools: {}", result.size(), result.keySet());
                }
            }
        }
        return result;
    }

    private Map<String, McpTool> buildTools() {
        Map<String, McpTool> result = new TreeMap<>();
        for (Object bean : applicationContext.getBeansWithAnnotation(Controller.class).values()) {
            Class<?> beanClass = AopUtils.getTargetClass(bean);
            if (beanClass.getPackage() == null || !SOURCE_PACKAGE.equals(beanClass.getPackage().getName())) {
                continue;
            }
            for (Method method : ReflectionUtils.getUniqueDeclaredMethods(beanClass)) {
                McpTool tool = buildTool(bean, beanClass, method);
                if (tool == null) {
                    continue;
                }
                if (result.containsKey(tool.name)) {
                    throw new IllegalStateException("Duplicate MCP tool name '" + tool.name + "' for " + tool.path);
                }
                result.put(tool.name, tool);
            }
        }
        return result;
    }

    /**
     * Returns null when the method should not be exposed as a tool.
     */
    private McpTool buildTool(Object bean, Class<?> beanClass, Method method) {
        if (method.isBridge() || method.isSynthetic() || !Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        // findAnnotation also searches interfaces, where the swagger-codegen style *Api
        // interfaces declare the mapping and markers for their *ApiController implementations.
        if (AnnotationUtils.findAnnotation(method, PublicApi.class) == null) {
            return null;
        }
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        if (mapping == null || !Arrays.asList(mapping.method()).contains(RequestMethod.GET) || mapping.path().length == 0) {
            return null;
        }
        String path = mapping.path()[0];
        if (path.endsWith(".txt") || producesPlainText(mapping)) {
            return null;
        }

        List<McpTool.Param> params = new ArrayList<>();
        ObjectNode properties = mapper.createObjectNode();
        ArrayNode required = mapper.createArrayNode();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Annotation[] annotations = parameterAnnotations(method, i);
            McpTool.Param param = toParam(annotations, new TypeDescriptor(new MethodParameter(method, i)));
            if (param == null) {
                LOG.warn("Skipping MCP tool for GET {} because parameter {} of {}.{} is not a @RequestParam or @PathVariable with an explicit name", path, i, beanClass.getSimpleName(), method.getName());
                return null;
            }
            params.add(param);
            properties.set(param.name, propertySchema(param, findAnnotation(annotations, ApiParam.class)));
            if (param.required) {
                required.add(param.name);
            }
        }

        ObjectNode inputSchema = mapper.createObjectNode();
        inputSchema.put("type", "object");
        inputSchema.set("properties", properties);
        if (required.size() > 0) {
            inputSchema.set("required", required);
        }
        inputSchema.put("additionalProperties", false);

        Method invocable = AopUtils.selectInvocableMethod(method, bean.getClass());
        return new McpTool(toolName(path), toolDescription(method, path), path, inputSchema, bean, invocable, params);
    }

    static String toolName(String path) {
        return StringUtils.strip(path.replaceAll("[^A-Za-z0-9]+", "_"), "_");
    }

    private static String toolDescription(Method method, String path) {
        ApiOperation operation = AnnotationUtils.findAnnotation(method, ApiOperation.class);
        String summary = operation == null ? "" : StringUtils.defaultIfBlank(operation.notes(), operation.value());
        String rest = "Same as GET " + REST_PATH_PREFIX + path + " in the OncoKB REST API.";
        return StringUtils.isBlank(summary) ? rest : summary.trim() + " " + rest;
    }

    private static boolean producesPlainText(RequestMapping mapping) {
        for (String produces : mapping.produces()) {
            if (produces.startsWith("text/plain")) {
                return true;
            }
        }
        return false;
    }

    private McpTool.Param toParam(Annotation[] annotations, TypeDescriptor type) {
        RequestParam requestParam = findAnnotation(annotations, RequestParam.class);
        if (requestParam != null) {
            String name = StringUtils.defaultIfEmpty(requestParam.value(), requestParam.name());
            if (name.isEmpty()) {
                return null;
            }
            String defaultValue = ValueConstants.DEFAULT_NONE.equals(requestParam.defaultValue()) ? null : requestParam.defaultValue();
            // Spring MVC treats a param with a default as optional even if required=true.
            return new McpTool.Param(name, type, requestParam.required() && defaultValue == null, defaultValue);
        }
        PathVariable pathVariable = findAnnotation(annotations, PathVariable.class);
        if (pathVariable != null) {
            String name = StringUtils.defaultIfEmpty(pathVariable.value(), pathVariable.name());
            return name.isEmpty() ? null : new McpTool.Param(name, type, true, null);
        }
        return null;
    }

    private ObjectNode propertySchema(McpTool.Param param, ApiParam apiParam) {
        ObjectNode schema = mapper.createObjectNode();
        // For multi-valued params the type and enum constraints apply to each array item.
        boolean multiValued = isMultiValued(param.type);
        TypeDescriptor valueType = multiValued ? param.type.getElementTypeDescriptor() : param.type;
        Class<?> valueClass = valueType == null ? String.class : valueType.getType();
        ObjectNode valueSchema = multiValued ? mapper.createObjectNode() : schema;
        if (multiValued) {
            schema.put("type", "array");
        }
        String jsonType = jsonType(valueClass);
        valueSchema.put("type", jsonType);
        if (apiParam != null && StringUtils.isNotBlank(apiParam.value())) {
            schema.put("description", apiParam.value());
        }
        if (multiValued) {
            schema.set("items", valueSchema);
        }

        List<String> allowed = new ArrayList<>();
        if (valueClass.isEnum()) {
            for (Object constant : valueClass.getEnumConstants()) {
                allowed.add(((Enum<?>) constant).name());
            }
        } else if (apiParam != null && StringUtils.isNotBlank(apiParam.allowableValues()) && !apiParam.allowableValues().startsWith("range")) {
            for (String value : apiParam.allowableValues().split(",")) {
                if (StringUtils.isNotBlank(value)) {
                    allowed.add(value.trim());
                }
            }
        }
        if (!allowed.isEmpty()) {
            ArrayNode values = valueSchema.putArray("enum");
            for (String value : allowed) {
                values.add(value);
            }
        }

        if (param.defaultValue != null && !multiValued) {
            if ("boolean".equals(jsonType)) {
                schema.put("default", Boolean.parseBoolean(param.defaultValue));
            } else if ("integer".equals(jsonType) && StringUtils.isNumeric(param.defaultValue)) {
                schema.put("default", Long.parseLong(param.defaultValue));
            } else {
                schema.put("default", param.defaultValue);
            }
        }
        return schema;
    }

    private static boolean isMultiValued(TypeDescriptor type) {
        return type.isCollection() || type.isArray();
    }

    private static String jsonType(Class<?> type) {
        Class<?> boxed = ClassUtils.resolvePrimitiveIfNecessary(type);
        if (boxed == Boolean.class) {
            return "boolean";
        }
        if (boxed == Integer.class || boxed == Long.class || boxed == Short.class) {
            return "integer";
        }
        if (Number.class.isAssignableFrom(boxed)) {
            return "number";
        }
        return "string";
    }

    /**
     * Returns the annotations of parameter {@code index}. If the implementation does not carry
     * {@code @RequestParam}/{@code @PathVariable}, falls back to the interface method, mirroring
     * where swagger-codegen interfaces place them. Spring 4.3 does not do this lookup itself.
     */
    private static Annotation[] parameterAnnotations(Method method, int index) {
        Annotation[] own = method.getParameterAnnotations()[index];
        if (isBindable(own)) {
            return own;
        }
        for (Class<?> iface : ClassUtils.getAllInterfacesForClassAsSet(method.getDeclaringClass())) {
            try {
                Annotation[] inherited = iface.getMethod(method.getName(), method.getParameterTypes()).getParameterAnnotations()[index];
                if (isBindable(inherited)) {
                    return inherited;
                }
            } catch (NoSuchMethodException ignored) {
                // This interface does not declare the method; try the next one.
            }
        }
        return own;
    }

    private static boolean isBindable(Annotation[] annotations) {
        return findAnnotation(annotations, RequestParam.class) != null || findAnnotation(annotations, PathVariable.class) != null;
    }

    private static <A extends Annotation> A findAnnotation(Annotation[] annotations, Class<A> type) {
        for (Annotation annotation : annotations) {
            if (type.isInstance(annotation)) {
                return type.cast(annotation);
            }
        }
        return null;
    }
}
