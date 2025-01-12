package cn.vika.keycloak.util;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import cn.vika.keycloak.constant.CommonAuthConstants;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.MediaType;

/**
 * <p>
 * 维格短信相关接口
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/30 11:34
 */
@JBossLog
public class VerifyCodeUtil {
    /**
     * 发送短信接口url
     */
    public static final String SMS_SEND_PATH = "/api/v1/base/action/sms/code";
    /**
     * 校验短信接口url
     */
    public static final String SMS_VERIFY_PATH = "/api/v1/base/action/sms/code/validate";
    /**
     * 发送邮箱验证码URL
     */
    public static final String MAIL_SEND_PATH = "/api/v1/base/action/mail/code";

    /**
     * 邮箱验证码校验URL
     */
    public static final String MAIL_VERIFICATION_PATH = "/api/v1/base/action/email/code/validate";

    /**
     * 发送短信
     *
     * @param phone    手机号
     * @param areaCode 地区
     * @param hostUrl  域名
     * @return
     */
    public static VikaResponse sendSMS(String phone, String areaCode, String hostUrl) {
        RequestParams data = new RequestParams();
        data.setAttribute(CommonAuthConstants.PHONE, phone);
        // 地区
        data.setAttribute(CommonAuthConstants.AREA_CODE, areaCode);
        // 人机验证数据 上层已经做过
        data.setAttribute(CommonAuthConstants.DATA, "FutureIsComing");
        data.setAttribute(CommonAuthConstants.TYPE, 2);

        return requestApi(data, hostUrl + SMS_SEND_PATH);
    }

    /**
     * 校验短信验证码
     *
     * @param phone    手机号
     * @param code     验证码
     * @param areaCode 地区
     * @param hostUrl  域名
     * @return
     */
    public static VikaResponse verifySMS(String phone, String code, String areaCode, String hostUrl) {
        RequestParams data = new RequestParams();
        data.setAttribute(CommonAuthConstants.PHONE, phone);
        data.setAttribute(CommonAuthConstants.AREA_CODE, areaCode);
        data.setAttribute(CommonAuthConstants.VERIFY_CODE, code);

        return requestApi(data, hostUrl + SMS_VERIFY_PATH);
    }

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱
     * @param hostUrl 域名
     * @return
     */
    public static VikaResponse sendEmailCode(String email, String hostUrl) {
        RequestParams data = new RequestParams();
        data.setAttribute(CommonAuthConstants.EMAIL, email);
        data.setAttribute(CommonAuthConstants.TYPE, 2);

        return requestApi(data, hostUrl + MAIL_SEND_PATH);
    }

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱
     * @param code 验证码
     * @param hostUrl 域名
     * @return
     */
    public static VikaResponse verifyEmailCode(String email, String code, String hostUrl) {
        RequestParams data = new RequestParams();
        data.setAttribute(CommonAuthConstants.EMAIL, email);
        data.setAttribute(CommonAuthConstants.VERIFY_CODE, code);
        return requestApi(data, hostUrl + MAIL_VERIFICATION_PATH);
    }

    /**
     * 发送http请求
     *
     * @param data 参数
     * @param url  http请求URL
     * @return
     */
    private static VikaResponse requestApi(RequestParams data, String url) {
        String body = HttpRequest.post(url)
                .header(Header.CONTENT_TYPE.getValue(), MediaType.APPLICATION_JSON)
                .body(data.toJSON())
                .execute().body();
        log.info("requestApi response: " + body);

        if (StringUtils.isNotBlank(body)) {
            VikaResponse response = JSONUtil.toBean(body, VikaResponse.class);
            return response;
        }

        return null;
    }
}
