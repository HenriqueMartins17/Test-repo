package cn.vika.keycloak.constant;

/**
 * <p>
 * 维格自定义校验器
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/30 11:34
 */
public class CommonAuthConstants {
	public static final String AFS_REGION_ID = "afs_region_id";
	public static final String AFS_ACCESS_KEY_ID = "afs_access_key_id";
	public static final String AFS_ACCESS_KEY_SECRET = "afs_access_key_secret";

	public static final String RESET_PASSWORD_URL_KEY = "loginResetCredentialsUrl";
	public static final String RESET_PASSWORD_URL = "/user/reset_password";
	public static final String REGISTER_URL_KEY = "registerUrl";
	public static final String REGISTER_URL = "/login";

	public static final String PHONE_NUMBER = "phoneNumber";
	public static final String RECAPTCHA_DATA = "recaptchaData";
	public static final String PASSWORD = "password";
	public static final String ACCOUNT_PASSWORD = "accountPassword";

	// api常量
	public static final String VIKA_REST_API_HOST = "vika_rest_api_host";
	public static final String PHONE = "phone";
	public static final String AREA_CODE = "areaCode";
	public static final String TYPE = "type";
	public static final String DATA = "data";
	public static final String VERIFY_CODE = "code";
	public static final String EMAIL = "email";

	public static final String ERROR_CODE_MESSAGE = "验证码错误，请重新输入";
	public static final String CODE_NOT_FOUND_MESSAGE = "未获取验证码或已过期，请重新获取";
}
