package cn.vika.keycloak.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.ws.rs.core.Response;

/**
 * <p>
 * 维格接口响应对象
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/09/15 17:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VikaResponse {
    private Integer code;
    private String message;

    public static VikaResponse success() {
        return new VikaResponse(Response.Status.OK.getStatusCode(), Response.Status.OK.getReasonPhrase());
    }

    public static VikaResponse error(String message) {
        return new VikaResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), message);
    }

    public static VikaResponse error(Integer code, String message) {
        return new VikaResponse(code, message);
    }
}
