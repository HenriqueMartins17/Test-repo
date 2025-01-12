package cn.vika.keycloak.util;

import cn.vika.keycloak.constant.CommonAuthConstants;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.RealmModel;

import java.util.Objects;

/**
 * <p>
 * 自定义配置工具类
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/31 18:23
 */
public class AuthenticatorConfigUtil {
    /**
     * 把配置信息设置到验证流上线文的域当中
     *
     * @param context 验证流上线文
     */
    public static void setAuthenticatorConfig(AuthenticationFlowContext context) {
        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
        if (Objects.nonNull(authenticatorConfig) && Objects.nonNull(authenticatorConfig.getConfig())) {
            RealmModel realm = context.getRealm();
            String restApiHost = authenticatorConfig.getConfig().get(CommonAuthConstants.VIKA_REST_API_HOST);
            if (StringUtils.isNotEmpty(restApiHost)) {
                realm.setAttribute(CommonAuthConstants.VIKA_REST_API_HOST, restApiHost);
            } else {
                realm.removeAttribute(CommonAuthConstants.VIKA_REST_API_HOST);
            }

            String afsRegionId = authenticatorConfig.getConfig().get(CommonAuthConstants.AFS_REGION_ID);
            if (StringUtils.isNotEmpty(afsRegionId)) {
                realm.setAttribute(CommonAuthConstants.AFS_REGION_ID, afsRegionId);
            } else {
                realm.removeAttribute(CommonAuthConstants.AFS_REGION_ID);
            }

            String afsAccessKeyId = authenticatorConfig.getConfig().get(CommonAuthConstants.AFS_ACCESS_KEY_ID);
            if (StringUtils.isNotEmpty(afsAccessKeyId)) {
                realm.setAttribute(CommonAuthConstants.AFS_ACCESS_KEY_ID, afsAccessKeyId);
            } else {
                realm.removeAttribute(CommonAuthConstants.AFS_ACCESS_KEY_ID);
            }

            String afsAccessKeySecret = authenticatorConfig.getConfig().get(CommonAuthConstants.AFS_ACCESS_KEY_SECRET);
            if (StringUtils.isNotEmpty(afsAccessKeySecret)) {
                realm.setAttribute(CommonAuthConstants.AFS_ACCESS_KEY_SECRET, afsAccessKeySecret);
            } else {
                realm.removeAttribute(CommonAuthConstants.AFS_ACCESS_KEY_SECRET);
            }
        }
    }
}
