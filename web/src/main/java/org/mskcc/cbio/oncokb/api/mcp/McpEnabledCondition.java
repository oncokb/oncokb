package org.mskcc.cbio.oncokb.api.mcp;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Registers the MCP beans only when {@value McpSettings#ENABLED} is {@code true}, so the endpoint
 * is off unless a deployment opts in.
 */
public class McpEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return McpSettings.isEnabled();
    }
}
