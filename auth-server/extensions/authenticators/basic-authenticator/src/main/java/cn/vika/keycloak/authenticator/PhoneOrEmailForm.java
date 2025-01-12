package cn.vika.keycloak.authenticator;

import cn.vika.keycloak.bean.VikaLoginBean;
import cn.vika.keycloak.constant.CommonAuthConstants;
import cn.vika.keycloak.constant.PatternConstants;
import cn.vika.keycloak.constant.VerifyCodeType;
import cn.vika.keycloak.util.AuthenticatorConfigUtil;
import cn.vika.keycloak.util.VerifyCodeUtil;
import cn.vika.keycloak.util.VikaResponse;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * <p>
 * 维格自定义校验器
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/30 11:34
 */
@JBossLog
public class PhoneOrEmailForm extends UsernamePasswordForm {
    private Pattern pattern = Pattern.compile(PatternConstants.EMAIL, Pattern.CASE_INSENSITIVE);

    /**
     * 登录事件
     *
     * @param context
     */
    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        // 设置 页面使用URL
        setFormAttribute(context.form(), context.getAuthenticatorConfig(), formData);

        if (!validateForm(context, formData)) {
            Response challengeResponse = challenge(context, formData);
            context.challenge(challengeResponse);
            return;
        }

        context.success();
    }

    /**
     * 校验密码
     *
     * @param context
     * @param user
     * @param inputData
     * @param clearUser
     * @return
     */
    @Override
    public boolean validatePassword(AuthenticationFlowContext context, UserModel user, MultivaluedMap<String, String> inputData, boolean clearUser) {
        String password = inputData.getFirst(CredentialRepresentation.PASSWORD);
        if (StringUtils.isEmpty(password)) {
            return badPasswordHandler(context, user, clearUser, true);
        }

        if (isDisabledByBruteForce(context, user)) {
            return false;
        }

        if (context.getSession().userCredentialManager().isValid(context.getRealm(), user, UserCredentialModel.password(password))) {
            return true;
        } else {
            return badPasswordHandler(context, user, clearUser, false);
        }
    }

    /**
     * 查询用户
     *
     * @param context
     * @param inputData
     * @return
     */
    private UserModel getUser(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        String phoneNumber = inputData.getFirst(CommonAuthConstants.PHONE_NUMBER);
        String email = inputData.getFirst(Details.EMAIL);

        if (StringUtils.isBlank(phoneNumber) && StringUtils.isBlank(email)) {
            // 用户查询参数为空
            Response challengeResponse = this.challenge(context, this.getDefaultChallengeMessage(context));
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return null;
        } else {
            UserModel user = null;
            try {
                if (StringUtils.isNotBlank(phoneNumber)) {
                    user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), phoneNumber);
                }
                if (StringUtils.isNotBlank(email)) {
                    user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), email);
                }
            } catch (Exception e) {
                log.error("find user error", e);
                return null;
            }

            if (Objects.isNull(user)) {
                Response challengeResponse = challenge(context, Messages.INVALID_USER, CommonAuthConstants.ACCOUNT_PASSWORD);
                context.challenge(challengeResponse);
            }

            this.testInvalidUser(context, user);
            return user;
        }
    }

    /**
     * 校验用户是否被禁用
     *
     * @param context
     * @param user
     * @return
     */
    private boolean validateUser(AuthenticationFlowContext context, UserModel user) {
        if (!this.enabledUser(context, user)) {
            return false;
        } else {
            context.setUser(user);
            return true;
        }
    }

    /**
     * 校验验证码
     *
     * @param context
     * @param formData
     * @param verifyCodeType
     * @return
     */
    public boolean validateVerifyCode(AuthenticationFlowContext context, MultivaluedMap<String, String> formData, VerifyCodeType verifyCodeType) {
        context.clearUser();
        String account = null;
        if (verifyCodeType == VerifyCodeType.PHONE) {
            account = formData.getFirst(CommonAuthConstants.PHONE_NUMBER);
        } else if (verifyCodeType == VerifyCodeType.EMAIL) {
            account = formData.getFirst(CommonAuthConstants.EMAIL);
        }

        String code = formData.getFirst(CommonAuthConstants.VERIFY_CODE);
        // 参数缺失
        if (StringUtils.isBlank(account) || StringUtils.isBlank(code)) {
            return Boolean.FALSE;
        }

        // 用户不存在
        UserModel user = this.getUser(context, formData);
        if (Objects.isNull(user)) {
            return Boolean.FALSE;
        }

        String hostUrl = context.getRealm().getAttribute(CommonAuthConstants.VIKA_REST_API_HOST);
        String areaCode = "+86";
        if (verifyCodeType == VerifyCodeType.PHONE) {
            String areaCodeTemp = formData.getFirst(CommonAuthConstants.AREA_CODE);
            if (StringUtils.isNotBlank(areaCodeTemp)) {
                areaCode = areaCodeTemp;
            }
        }

        log.info("validateVerifyCode:" + hostUrl);
        VikaResponse response = null;
        if (verifyCodeType == VerifyCodeType.PHONE) {
            response = VerifyCodeUtil.verifySMS(account, code, areaCode, hostUrl);
        } else if (verifyCodeType == VerifyCodeType.EMAIL) {
            response = VerifyCodeUtil.verifyEmailCode(account, code, hostUrl);
        }
        if (Objects.isNull(response)) {
            return Boolean.FALSE;
        }

        if (HttpURLConnection.HTTP_OK != response.getCode()) {
            Response challengeResponse = null;
            if (CommonAuthConstants.ERROR_CODE_MESSAGE.equals(response.getMessage())) {
                challengeResponse = challenge(context, "errorCodeMessage", CommonAuthConstants.VERIFY_CODE);
            } else if (CommonAuthConstants.CODE_NOT_FOUND_MESSAGE.equals(response.getMessage())) {
                challengeResponse = challenge(context, "codeNotFoundMessage", CommonAuthConstants.VERIFY_CODE);
            }
            if (Objects.nonNull(challengeResponse)) {
                context.challenge(challengeResponse);
            }
            return Boolean.FALSE;
        }
        return validateUser(context, user);
    }

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        boolean result = false;
        try {
            String email = formData.getFirst(Details.EMAIL);
            String phoneNumber = formData.getFirst(CommonAuthConstants.PHONE_NUMBER);
            String password = formData.getFirst(CommonAuthConstants.PASSWORD);
            // 手机号密码登录，默认取的是username参数
            if (StringUtils.isNotBlank(phoneNumber)) {
                formData.put(Details.USERNAME, Collections.singletonList(phoneNumber));
            }
            if (StringUtils.isNotBlank(email)) {
                if (!pattern.matcher(email).matches()) {
                    Response challengeResponse = challenge(context, Messages.INVALID_EMAIL, CommonAuthConstants.EMAIL);
                    context.challenge(challengeResponse);
                    return false;
                }
                formData.put(Details.USERNAME, Collections.singletonList(email));
            }
            // 邮箱，手机号密码登录
            if (StringUtils.isNotEmpty(password) && (StringUtils.isNotBlank(phoneNumber) || StringUtils.isNotBlank(email))) {
                result = validateUserAndPassword(context, formData);
                if (!result) {
                    Response challengeResponse = challenge(context, Messages.INVALID_USER, CommonAuthConstants.ACCOUNT_PASSWORD);
                    context.challenge(challengeResponse);
                }
            }
            formData.remove(Details.USERNAME);

        } catch (Exception e) {
            log.error(e);
        }
        return result || validateVerifyCode(context, formData, VerifyCodeType.PHONE) || validateVerifyCode(context, formData, VerifyCodeType.EMAIL);
    }

    /**
     * 初始化页面请求
     *
     * @param context
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        setFormAttribute(context.form(), context.getAuthenticatorConfig(), formData);
        Response challengeResponse = challenge(context, formData);
        context.challenge(challengeResponse);
    }

    /**
     * 默认登录页面
     *
     * @param context
     * @param formData
     * @return
     */
    @Override
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();
        if (formData.size() > 0) {
            forms.setFormData(formData);
        }

        // 配置信息设置到realm
        AuthenticatorConfigUtil.setAuthenticatorConfig(context);

        return forms.setAttribute("login", new VikaLoginBean(formData))
                .createForm("login.ftl");
    }

    /**
     * 密码错误处理
     *
     * @param context
     * @param user
     * @param clearUser
     * @param isEmptyPassword
     * @return
     */
    private boolean badPasswordHandler(AuthenticationFlowContext context,
                                       UserModel user, boolean clearUser,
                                       boolean isEmptyPassword) {
        context.getEvent().user(user);
        context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);

        if (isEmptyPassword) {
            Response challengeResponse = challenge(context, Messages.INVALID_USER);
            context.forceChallenge(challengeResponse);
        } else {
            Response challengeResponse = challenge(context, Messages.INVALID_USER);
            context.challenge(challengeResponse);
        }

        if (clearUser) {
            context.clearUser();
        }

        return false;
    }

    /**
     * 设置首页忘记密码、注册账号等url
     *
     * @param forms
     * @param configModel
     */
    private void setFormAttribute(LoginFormsProvider forms, AuthenticatorConfigModel configModel, MultivaluedMap<String, String> formData) {
        String restApiHost = configModel.getConfig().get(CommonAuthConstants.VIKA_REST_API_HOST);
        forms.setAttribute(CommonAuthConstants.RESET_PASSWORD_URL_KEY, restApiHost + CommonAuthConstants.RESET_PASSWORD_URL);
        forms.setAttribute(CommonAuthConstants.REGISTER_URL_KEY, restApiHost + CommonAuthConstants.REGISTER_URL);
        forms.setAttribute("login", new VikaLoginBean(formData));
    }
}
