package org.mskcc.cbio.oncokb.cache.keygenerator;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

import static org.mskcc.cbio.oncokb.cache.Constants.REDIS_KEY_SEPARATOR;

public class ConcatGenerator implements KeyGenerator {

    public Object generate(Object target, Method method, Object... params) {
        return StringUtils.arrayToDelimitedString(params, REDIS_KEY_SEPARATOR);
    }
}
