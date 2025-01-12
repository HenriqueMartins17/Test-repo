package cn.vika.keycloak.provider;

import cn.vika.keycloak.constant.CommonAuthConstants;
import cn.vika.keycloak.util.VerifyCodeUtil;
import cn.vika.keycloak.util.VikaResponse;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;

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
 * 邮箱rest相关接口
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/30 11:34
 */
@JBossLog
public class EmailCodeResource {
    private final KeycloakSession session;

    public EmailCodeResource(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 发送邮箱验证码
     *
     * @param formData 请求参数
     * @return
     */
    @POST
    @Path("/code")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendCode(final MultivaluedMap<String, String> formData) {
        String hostUrl = session.getContext().getRealm().getAttribute(CommonAuthConstants.VIKA_REST_API_HOST);
        log.info("sendEmailCode host:" + hostUrl);
        VikaResponse response = VerifyCodeUtil.sendEmailCode(formData.getFirst(CommonAuthConstants.EMAIL), hostUrl);
        if (Objects.nonNull(response) && HttpURLConnection.HTTP_OK != response.getCode()) {
            return Response.status(Response.Status.OK).entity(response).build();
        }
        return Response.status(Response.Status.OK).entity(VikaResponse.success()).build();
    }
}
