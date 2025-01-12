package com.apitable.enterprise.ai.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * pure json.
 */
public class PureJson extends HashMap<String, Object> {

    /**
     * extract data field.
     *
     * @return Map
     */
    public Map<String, Object> extractData() {
        Object data = this.get("data");
        if (data == null) {
            return new HashMap<>();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> typeRef
            = new TypeReference<>() {
        };
        return objectMapper.convertValue(data, typeRef);
    }
}
