package cn.vika.keycloak.social.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.ws.rs.core.Response;
import java.io.Serializable;

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
public class VikaResponse<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;

    public static VikaResponse success() {
        return new VikaResponse(Response.Status.OK.getStatusCode(), Response.Status.OK.getReasonPhrase(), null);
    }

    public static VikaResponse error(String message) {
        return new VikaResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), message, null);
    }

    public static VikaResponse error(Integer code, String message) {
        return new VikaResponse(code, message, null);
    }
}
