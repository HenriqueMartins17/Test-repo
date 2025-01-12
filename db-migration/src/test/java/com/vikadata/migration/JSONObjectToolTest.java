package com.vikadata.migration;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JSONObjectToolTest {

    @Test
    public void testJsonObjectWithNumber() {
        String json = "{ \"anyString\":\"Hello World\", \"aNumber\": 123, \"aDouble\": -895.25 }";
        JSONObject jsonObject = new JSONObject(json);
        Object aDouble = jsonObject.getObj("aDouble");
        Assertions.assertTrue(aDouble instanceof BigDecimal);
    }

    @Test
    public void testJsonWithNull() {
        String json = "{ \"anyString\":\"Hello World\", \"aNumber\": 123, \"aDouble\": -895.25,  \"aNull\": null}";
        JSONObject jsonObject = new JSONObject(json, true, false);
        Object aNull = jsonObject.getObj("aNull");
        Assertions.assertTrue(null == aNull);
    }

    @Test
    public void testJSONArrayDuplicateValue() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("a");
        jsonArray.put("b");
        jsonArray.put("a");
        Set set = new HashSet(jsonArray);
        JSONArray jsonArraySet = new JSONArray(set);
        Assertions.assertTrue(jsonArraySet.size() == 2);
    }

}
