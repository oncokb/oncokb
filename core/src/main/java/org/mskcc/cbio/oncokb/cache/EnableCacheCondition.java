package org.mskcc.cbio.oncokb.cache;

import jodd.util.StringUtil;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class EnableCacheCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final String REDIS_ENABLE = "redis.enable";
        if (StringUtil.isEmpty(PropertiesUtils.getProperties(REDIS_ENABLE))) {
            return false;
        } else {
            return PropertiesUtils.getProperties(REDIS_ENABLE).equalsIgnoreCase("true");
        }
    }
}
