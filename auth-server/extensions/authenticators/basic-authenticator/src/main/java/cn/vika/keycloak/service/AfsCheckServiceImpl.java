package cn.vika.keycloak.service;

import cn.hutool.core.util.StrUtil;
import cn.vika.keycloak.constant.AfsCheckMessage;
import cn.vika.keycloak.util.AfsChecker;
import cn.vika.keycloak.util.AliyunAfsChecker;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

/**
 * <p>
 * 阿里云盾人机验证接口实现类
 * </p>
 *
 * @author Chambers
 * @date 2020/2/6
 */
public class AfsCheckServiceImpl implements AfsCheckService {
    private static final Logger LOGGER = Logger.getLogger(AfsCheckServiceImpl.class);

    private AfsChecker afsChecker;

    public AfsCheckServiceImpl(String regionId, String accessKeyId, String accessKeySecret) {
        afsChecker = new AliyunAfsChecker(regionId, accessKeyId, accessKeySecret);
    }

    @Override
    public AfsCheckMessage noTraceCheck(String data) {
        if (afsChecker == null) {
            System.out.println();
            LOGGER.info("人机认证未开启");
            return null;
        }
        if (StrUtil.isBlank(data)) {
            return AfsCheckMessage.MAN_MACHINE_VERIFICATION_FAILED;
        } else if (data.equals("FutureIsComing")) {
            return null;
        }
        // 根据需求填写
        String scoreJsonStr = "{\"200\":\"PASS\",\"400\":\"NC\",\"600\":\"NC\",\"700\":\"NC\",\"800\":\"BLOCK\"}";
        String result = afsChecker.noTraceCheck(data, scoreJsonStr);
        LOGGER.info("人机验证结果:" + result);
        if (StringUtils.isBlank(result)) {
            return AfsCheckMessage.SECONDARY_VERIFICATION;
        }
        switch (result) {
            case "100":
            case "200":
                //直接通过
                return null;
            case "400":
            case "600":
            case "700":
                //唤起滑块验证码
                return AfsCheckMessage.SECONDARY_VERIFICATION;
            case "800":
                //验证失败，直接拦截
                return AfsCheckMessage.MAN_MACHINE_VERIFICATION_FAILED;
            case "900":
                //滑块验证继续被风控识别为非法之后，启用短信验证码验证
                return AfsCheckMessage.ENABLE_SMS_VERIFICATION;
            default:
                return null;
        }
    }
}
