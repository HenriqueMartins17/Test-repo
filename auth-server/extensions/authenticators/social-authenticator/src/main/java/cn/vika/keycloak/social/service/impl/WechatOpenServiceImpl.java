package cn.vika.keycloak.social.service.impl;

import cn.hutool.json.JSONUtil;
import cn.vika.keycloak.social.common.ClientOriginInfo;
import cn.vika.keycloak.social.common.JustIdentityProviderConfig;
import cn.vika.keycloak.social.service.WechatOpenService;
import lombok.extern.jbosslog.JBossLog;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.open.api.WxOpenMpService;
import me.chanjar.weixin.open.api.WxOpenService;
import me.chanjar.weixin.open.api.impl.WxOpenInRedisConfigStorage;
import me.chanjar.weixin.open.api.impl.WxOpenServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 微信开放平台接口
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/10/05 00:02
 */
@JBossLog
public class WechatOpenServiceImpl implements WechatOpenService {
    /**
     * 小程序码/二维码有效时间（单位：秒）
     */
    public static final int TIMEOUT = 10 * 60;
    /**
     * 未关注公众号 扫描二维码事件 KEY 值 唯一标识前缀
     */
    public static final String QR_SCENE_PRE = "qrscene_";
    /**
     * PC 登录、帐号绑定二维码 唯一标识前缀
     */
    public static final String MARK_PRE = "mark_";
    /**
     * 微信开放平台接口
     */
    private WxOpenService wxOpenService;
    /**
     * 微信redis缓存接口
     */
    private WxRedisOps redisOps;
    /**
     * 微信redis缓存接口
     */
    private StringRedisTemplate redisTemplate;
    /**
     * Redis缓存前缀
     */
    private final String redisPrefix = "keycloak";
    /**
     * 微信扫码key规则，示例：keycloak:qrScan:LoKb1qgQ1pQB
     */
    private final String qrScanKey = redisPrefix + ":qrScan:%s";
    /**
     * 微信扫码客户端信息key规则，示例：keycloak:qrScan:info:LoKb1qgQ1pQB
     */
    private final String qrScanInfoKey = redisPrefix + ":qrScan:info:%s";

    public WechatOpenServiceImpl(JustIdentityProviderConfig config) {
        this.wxOpenService = new WxOpenServiceImpl();

        redisTemplate = getStringRedisTemplate(config.getRedisHost(), Integer.valueOf(config.getRedisPort()), config.getRedisPassword());
        redisOps = new RedisTemplateWxRedisOps(redisTemplate);

        WxOpenInRedisConfigStorage configStorage = new WxOpenInRedisConfigStorage(redisOps, redisPrefix);
        configStorage.setWxOpenInfo(config.getClientId(), config.getClientSecret(), config.getComponentToken(), config.getComponentAesKey());
        wxOpenService.setWxOpenConfigStorage(configStorage);
    }

    private StringRedisTemplate getStringRedisTemplate(String redisHost, Integer redisPort, String redisPassword) {
        RedisStandaloneConfiguration rsc = new RedisStandaloneConfiguration();
        rsc.setPort(redisPort);
        rsc.setPassword(RedisPassword.of(redisPassword));
        rsc.setHostName(redisHost);

        JedisClientConfiguration.JedisPoolingClientConfigurationBuilder builder = (JedisClientConfiguration.JedisPoolingClientConfigurationBuilder) JedisClientConfiguration.builder();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        builder.poolConfig(jedisPoolConfig);
        JedisClientConfiguration jedisClientConfiguration = builder.build();
        JedisConnectionFactory fac = new JedisConnectionFactory(rsc, jedisClientConfiguration);
        fac.afterPropertiesSet();

        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(fac);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    /**
     * 获取微信二维码ticket
     *
     * @param mark  唯一标识，种在二维码里
     * @param appId 对应公众号的appId
     * @return
     */
    public WxMpQrCodeTicket getWxMpQrCodeTicket(String mark, String appId) {
        try {
            // 生成二维码
            WxOpenMpService wxOpenMpService = wxOpenService.getWxOpenComponentService().getWxMpServiceByAppid(appId);
            return wxOpenMpService.getQrcodeService().qrCodeCreateTmpTicket(MARK_PRE + mark, TIMEOUT);
        } catch (WxErrorException e) {
            log.error("getWxMpQrCodeTicket error", e);
            return null;
        }
    }

    /**
     * 缓存微信扫码结果到缓存
     *
     * @param mark   唯一标识，种在二维码里
     * @param openid 微信开放id
     */
    @Override
    public void cacheScanMark(String mark, String openid) {
        redisOps.setValue(String.format(qrScanKey, mark), openid, TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * 获取微信扫码缓存数据
     *
     * @param mark 唯一标识，种在二维码里
     * @return
     */
    @Override
    public String getScanMarkCache(String mark) {
        return redisOps.getValue(String.format(qrScanKey, mark));
    }

    /**
     * 删除微信扫码缓存数据
     *
     * @param mark
     */
    @Override
    public void deleteScanMarkCache(String mark) {
        redisTemplate.delete(String.format(qrScanKey, mark));
    }

    /**
     * 缓存用户扫码客户端信息
     *
     * @param info 客户端数据
     * @param mark 唯一标识，种在二维码里
     */
    @Override
    public void cacheScanInfo(ClientOriginInfo info, String mark) {
        redisOps.setValue(String.format(qrScanInfoKey, mark), JSONUtil.toJsonStr(info), TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * 从缓存获取扫码用户客户端信息
     *
     * @param mark
     * @return
     */
    @Override
    public ClientOriginInfo getScanInfo(String mark) {
        String infoStr = redisOps.getValue(String.format(qrScanInfoKey, mark));
        if (StringUtils.isBlank(infoStr)) {
            return null;
        }
        ClientOriginInfo info = JSONUtil.toBean(infoStr, ClientOriginInfo.class);
        return info;
    }

    /**
     * 获取微信开放平台服务
     *
     * @return
     */
    @Override
    public WxOpenService getWxOpenService() {
        return wxOpenService;
    }
}
