package cn.vika.keycloak.util;

import cn.hutool.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 请求参数对象
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/31 18:23
 */
public class RequestParams {

    private Map<String, Object> data;

    public RequestParams() {
        data = new HashMap<>();
    }

    public void setAttribute(String key, Object value) {
        this.data.put(key, value);
    }

    public Map<String, Object> toMap() {
        return this.data;
    }

    public String toJSON() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> entry : toMap().entrySet()) {
            json.putOpt(entry.getKey(), entry.getValue());
        }
        return json.toString();
    }
}
