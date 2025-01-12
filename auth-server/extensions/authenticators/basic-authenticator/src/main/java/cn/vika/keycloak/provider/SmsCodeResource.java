package cn.vika.keycloak.provider;

import cn.vika.keycloak.constant.AfsCheckMessage;
import cn.vika.keycloak.constant.CommonAuthConstants;
import cn.vika.keycloak.service.AfsCheckService;
import cn.vika.keycloak.service.AfsCheckServiceImpl;
import cn.vika.keycloak.util.VerifyCodeUtil;
import cn.vika.keycloak.util.VikaResponse;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.Objects;

/**
 * <p>
 * 短信rest相关接口
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/30 11:34
 */
@JBossLog
public class SmsCodeResource {
    private final KeycloakSession session;

    private AfsCheckService afsCheckService;

    public SmsCodeResource(KeycloakSession session) {
        this.session = session;
        RealmModel realm = session.getContext().getRealm();
        this.afsCheckService = new AfsCheckServiceImpl(realm.getAttribute(CommonAuthConstants.AFS_REGION_ID),
                realm.getAttribute(CommonAuthConstants.AFS_ACCESS_KEY_ID),
                realm.getAttribute(CommonAuthConstants.AFS_ACCESS_KEY_SECRET));
    }

    /**
     * 发送短信验证码
     *
     * @param formData 请求参数
     * @return
     */
    @POST
    @Path("/code")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response createVerificationCode(final MultivaluedMap<String, String> formData) {
        AfsCheckMessage afsCheckMessage = afsCheckService.noTraceCheck(formData.getFirst(CommonAuthConstants.RECAPTCHA_DATA));
        if (Objects.nonNull(afsCheckMessage)) {
            VikaResponse response = VikaResponse.error(afsCheckMessage.getCode(), afsCheckMessage.getMessage());
            return Response.status(Response.Status.OK).entity(response).build();
        }

        String smsUrl = session.getContext().getRealm().getAttribute(CommonAuthConstants.VIKA_REST_API_HOST);
        log.info("sendSMS host:" + smsUrl);
        String areaCode = formData.getFirst(CommonAuthConstants.AREA_CODE);
        if (StringUtils.isBlank(areaCode)) {
            areaCode = "+86";
        }
        VikaResponse response = VerifyCodeUtil.sendSMS(formData.getFirst(CommonAuthConstants.PHONE_NUMBER), areaCode, smsUrl);
        if (Objects.nonNull(response) && HttpURLConnection.HTTP_OK != response.getCode()) {
            return Response.status(Response.Status.OK).entity(response).build();
        }

        return Response.status(Response.Status.OK).entity(VikaResponse.success()).build();
    }
}
