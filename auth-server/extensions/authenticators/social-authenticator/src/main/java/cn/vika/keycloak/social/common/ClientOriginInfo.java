package cn.vika.keycloak.social.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 二维码源信息
 * </p>
 *
 * @author Chambers
 * @date 2020/10/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientOriginInfo {
    /**
     * 客户端IP
     */
    private String ip;
    /**
     * 用户引擎
     */
    private String userAgent;
    /**
     * 微信开放id
     */
    private String openid;
}
