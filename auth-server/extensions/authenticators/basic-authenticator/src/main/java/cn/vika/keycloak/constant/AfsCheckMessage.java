package cn.vika.keycloak.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * 阿里云盾校验结果消息
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/09/01 15:26
 */
@Getter
@AllArgsConstructor
public enum AfsCheckMessage {
    MAN_MACHINE_VERIFICATION_FAILED(250, "人机验证失败"),

    /**
     * 二次验证
     */
    SECONDARY_VERIFICATION(251, "二次验证"),

    /**
     * 启用短信验证
     */
    ENABLE_SMS_VERIFICATION(252, "当前环境存在风险，请重新验证");

    private final Integer code;

    private final String message;
}
