package cn.vika.keycloak.social.service;

import cn.vika.keycloak.social.common.ClientOriginInfo;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.open.api.WxOpenService;

/**
 * <p>
 * 微信开放平台服务
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/10/05 00:01
 */
public interface WechatOpenService {

    /**
     * 获取微信二维码ticket
     *
     * @param mark  唯一标识，种在二维码里
     * @param appId 对应公众号的appId
     * @return
     */
    WxMpQrCodeTicket getWxMpQrCodeTicket(String mark, String appId);

    /**
     * 缓存微信扫码结果到缓存
     *
     * @param mark   唯一标识，种在二维码里
     * @param openid 微信开放id
     */
    void cacheScanMark(String mark, String openid);

    /**
     * 获取微信扫码缓存数据
     *
     * @param mark 唯一标识，种在二维码里
     * @return
     */
    String getScanMarkCache(String mark);

    /**
     * 删除微信扫码缓存数据
     *
     * @param mark 唯一标识，种在二维码里
     */
    void deleteScanMarkCache(String mark);

    /**
     * 缓存用户扫码客户端信息
     *
     * @param info 客户端数据
     * @param mark 唯一标识，种在二维码里
     */
    void cacheScanInfo(ClientOriginInfo info, String mark);

    /**
     * 从缓存获取扫码用户客户端信息
     *
     * @param mark 唯一标识，种在二维码里
     * @return
     */
    ClientOriginInfo getScanInfo(String mark);

    /**
     * 获取微信开放平台服务
     *
     * @return
     */
    WxOpenService getWxOpenService();
}
