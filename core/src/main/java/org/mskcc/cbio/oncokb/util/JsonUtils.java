/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 *
 * @author jgao
 */
public final class JsonUtils {
    private JsonUtils() {
        throw new AssertionError();
    }
    
    private static ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
    private static TypeReference<HashMap<String,Object>> typeRefMap
                = new TypeReference<HashMap<String,Object>>() {}; 
    
    public static Map<String, Object> jsonToMap(String json) throws IOException {
        return objectMapper.readValue(json, typeRefMap);
    }
}
