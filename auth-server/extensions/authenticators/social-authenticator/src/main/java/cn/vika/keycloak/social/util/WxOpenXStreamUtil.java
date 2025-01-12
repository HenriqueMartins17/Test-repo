package cn.vika.keycloak.social.util;

import com.thoughtworks.xstream.XStream;
import me.chanjar.weixin.open.bean.message.WxOpenXmlMessage;

/**
 * <p>
 * 微信xml转对象映射
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/09/13 16:58
 */
public class WxOpenXStreamUtil {
    public static void setXStreamAlias(XStream xs) {
        xs.alias("xml", WxOpenXmlMessage.class);
        xs.aliasField("AppId", WxOpenXmlMessage.class, "appId");
        xs.aliasField("CreateTime", WxOpenXmlMessage.class, "createTime");
        xs.aliasField("InfoType", WxOpenXmlMessage.class, "infoType");
        xs.aliasField("ComponentVerifyTicket", WxOpenXmlMessage.class, "componentVerifyTicket");
        xs.aliasField("AuthorizerAppid", WxOpenXmlMessage.class, "authorizerAppid");
        xs.aliasField("AuthorizationCode", WxOpenXmlMessage.class, "authorizationCode");
        xs.aliasField("AuthorizationCodeExpiredTime", WxOpenXmlMessage.class, "authorizationCodeExpiredTime");
        xs.aliasField("PreAuthCode", WxOpenXmlMessage.class, "preAuthCode");
        xs.aliasField("appid", WxOpenXmlMessage.class, "registAppId");
        xs.aliasField("status", WxOpenXmlMessage.class, "status");
        xs.aliasField("auth_code", WxOpenXmlMessage.class, "authCode");
        xs.aliasField("msg", WxOpenXmlMessage.class, "msg");

        xs.alias("info", WxOpenXmlMessage.Info.class);
        xs.aliasField("name", WxOpenXmlMessage.Info.class, "name");
        xs.aliasField("code", WxOpenXmlMessage.Info.class, "code");
        xs.aliasField("code_type", WxOpenXmlMessage.Info.class, "codeType");
        xs.aliasField("legal_persona_wechat", WxOpenXmlMessage.Info.class, "legalPersonaWechat");
        xs.aliasField("legal_persona_name", WxOpenXmlMessage.Info.class, "legalPersonaName");
        xs.aliasField("component_phone", WxOpenXmlMessage.Info.class, "componentPhone");
    }
}
