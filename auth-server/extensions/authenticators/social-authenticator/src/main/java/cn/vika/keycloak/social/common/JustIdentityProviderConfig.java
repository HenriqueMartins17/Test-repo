package cn.vika.keycloak.social.common;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * 配置扩展字段
 *
 * @author Leo Zhao
 * @date 2021/10/26 14:02
 */
public class JustIdentityProviderConfig extends OAuth2IdentityProviderConfig {
    /**
     * 企业微信AgentId
     */
    private static final String AGENT_ID_KEY = "weworkAgentId";
    /**
     * 阿里PublicKey
     */
    private static final String ALIPAY_PUBLIC_KEY = "alipayPublicKey";
    private static final String CODING_GROUP_NAME = "codingGroupName";
    /**
     * Redis 连接地址
     */
    private static final String REDIS_HOST = "redisHost";
    /**
     * redis 端口
     */
    private static final String REDIS_PORT = "redisPort";
    /**
     * redis 授权密码
     */
    private static final String REDIS_PASSWORD = "redisPassword";
    /**
     * 授权给开发者的appId
     */
    private static final String AUTH_APP_ID = "authAppId";
    /**
     * 开发者验证token
     */
    private static final String COMPONENT_TOKEN = "componentToken";
    /**
     * 开发者AES加密Key
     */
    private static final String COMPONENT_AES_KEY = "componentAesKey";
    /**
     * 模板消息备注
     */
    private static final String MESSAGE_REMARK = "messageRemark";
    /**
     * 微信消息模板ID
     */
    private static final String MESSAGE_TEMPLATE_ID = "messageTemplateId";
    /**
     * 没有关联账号时注册地址
     */
    private static final String LOGIN_URL = "loginUrl";

    private JustAuthKey justAuthKey;

    public JustIdentityProviderConfig(IdentityProviderModel model, JustAuthKey justAuthKey) {
        super(model);
        this.justAuthKey = justAuthKey;
    }

    public JustIdentityProviderConfig(JustAuthKey justAuthKey) {
        this.justAuthKey = justAuthKey;
    }


    public JustAuthKey getJustAuthKey() {
        return this.justAuthKey;
    }

    public String getAgentId() {
        return getConfig().get(AGENT_ID_KEY);
    }

    public void setAgentId(String agentId) {
        getConfig().put(AGENT_ID_KEY, agentId);
    }

    public String getAlipayPublicKey() {
        return getConfig().get(ALIPAY_PUBLIC_KEY);
    }

    public void setAlipayPublicKey(String alipayPublicKey) {

        getConfig().put(ALIPAY_PUBLIC_KEY, alipayPublicKey);
    }

    public String getCodingGroupName() {
        return getConfig().get(CODING_GROUP_NAME);
    }

    public void setCodingGroupName(String codingGroupName) {
        getConfig().put(CODING_GROUP_NAME, codingGroupName);

    }

    public String getRedisHost() {
        return getConfig().get(REDIS_HOST);
    }

    public void setRedisHost(String redisHost) {
        getConfig().put(REDIS_HOST, redisHost);
    }

    public String getRedisPort() {
        return getConfig().get(REDIS_PORT);
    }

    public void setRedisPort(String redisPort) {
        getConfig().put(REDIS_PORT, redisPort);
    }

    public String getRedisPassword() {
        return getConfig().get(REDIS_PASSWORD);
    }

    public void setRedisPassword(String redisPassword) {
        getConfig().put(REDIS_PASSWORD, redisPassword);
    }

    public String getAuthAppId() {
        return getConfig().get(AUTH_APP_ID);
    }

    public void setAuthAppId(String authAppId) {
        getConfig().put(AUTH_APP_ID, authAppId);
    }

    public String getComponentToken() {
        return getConfig().get(COMPONENT_TOKEN);
    }

    public void setComponentToken(String componentToken) {
        getConfig().put(COMPONENT_TOKEN, componentToken);
    }

    public String getComponentAesKey() {
        return getConfig().get(COMPONENT_AES_KEY);
    }

    public void setComponentAesKey(String componentAesKey) {
        getConfig().put(COMPONENT_AES_KEY, componentAesKey);
    }

    public String getMessageRemark() {
        return getConfig().get(MESSAGE_REMARK);
    }

    public void setMessageRemark(String messageRemark) {
        getConfig().put(MESSAGE_REMARK, messageRemark);
    }

    public String getMessageTemplateId() {
        return getConfig().get(MESSAGE_TEMPLATE_ID);
    }

    public void setMessageTemplateId(String messageTemplateId) {
        getConfig().put(MESSAGE_TEMPLATE_ID, messageTemplateId);
    }

    public String getLoginUrl() {
        return getConfig().get(LOGIN_URL);
    }

    public void setLoginUrl(String loginUrl) {
        getConfig().put(LOGIN_URL, loginUrl);
    }
}
