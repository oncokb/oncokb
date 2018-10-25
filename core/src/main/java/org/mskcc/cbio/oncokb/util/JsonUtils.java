/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jgao
 */
public final class JsonUtils {
    private JsonUtils() {
        throw new AssertionError();
    }

    private static ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
    private static TypeReference<HashMap<String, Object>> typeRefMap
        = new TypeReference<HashMap<String, Object>>() {
    };

    public static Map<String, Object> jsonToMap(String json) throws IOException {
        return objectMapper.readValue(json, typeRefMap);
    }

    public static <T> List<T> jsonToArray(String json) throws IOException {
        return objectMapper.readValue(json, new TypeReference<List<T>>() {
        });
    }
}
