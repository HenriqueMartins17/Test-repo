package cn.vika.keycloak.bean;

import lombok.Data;

import javax.ws.rs.core.MultivaluedMap;

/**
 * <p>
 * 维格用户登录对象
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/09/22 14:06
 */
@Data
public class VikaLoginBean {
    private String phoneNumber;

    private String email;

    private String password;

    private String passwordToken;

    private String rememberMe;

    private String code;

    public VikaLoginBean(MultivaluedMap<String, String> formData) {
        if (formData != null) {
            this.phoneNumber = formData.getFirst("phoneNumber");
            this.email = formData.getFirst("email");
            this.password = formData.getFirst("password");
            this.code = formData.getFirst("code");
        }
    }
}
