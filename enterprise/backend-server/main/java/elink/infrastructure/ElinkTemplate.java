/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.apitable.enterprise.elink.infrastructure;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.elink.infrastructure.core.ApiBinding;
import com.apitable.enterprise.elink.infrastructure.model.TokenResponse;
import com.apitable.enterprise.elink.infrastructure.model.UserInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;

public class ElinkTemplate extends ApiBinding implements ElinkConnector {


    private static final Logger LOGGER = LoggerFactory.getLogger(ElinkTemplate.class);

    private final String corpId;

    private final String baseUrl;

    private final List<AgentApp> agentAppList;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private final static String ELINK_TOKEN_KEY = "vikadata:connector:elink:{}:token";


    public ElinkTemplate(String corpId, List<AgentApp> agentAppList, String host) {
        this.corpId = corpId;
        this.baseUrl = host;
        this.agentAppList = agentAppList;
    }

    public static class AgentApp implements Serializable {

        private static final long serialVersionUID = -6968365532035131390L;

        /**
         * Agent Id
         */
        private String agentId;

        /**
         * Agent Secret
         */
        private  String agentSecret;

        /**
         * Callback domain name, different agentId configuration domain name is not the same
         */
        private String callbackDomain;

        private String qrDomain;

        public String getAgentId() {
            return agentId;
        }

        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        public String getCallbackDomain() {
            return callbackDomain;
        }

        public void setCallbackDomain(String callbackDomain) {
            this.callbackDomain = callbackDomain;
        }

        public String getAgentSecret() {
            return agentSecret;
        }

        public void setAgentSecret(String agentSecret) {
            this.agentSecret = agentSecret;
        }

        public String getQrDomain() {
            return qrDomain;
        }

        public void setQrDomain(String qrDomain) {
            this.qrDomain = qrDomain;
        }
    }

    /**
     * The access token is the token for calling the elink interface, and the validity period is 7200 seconds.
     * * Developers must cache the access token globally in their own services
     * @return
     * @throws Exception
     */
    private String getAccessToken(AgentApp agentApp) throws Exception {
        String tokenKey = CharSequenceUtil.format(ELINK_TOKEN_KEY, agentApp.agentId);
        if (stringRedisTemplate != null) {
            String token = stringRedisTemplate.opsForValue().get(tokenKey);
            if (token != null) {
                return token;
            }
        }
        String url =  String.format("%s/cgi-bin/gettoken?corpid=%s&corpsecret=%s",
                this.baseUrl, this.corpId, agentApp.getAgentSecret());
        Map<String, Object> map = new HashMap<>(1);
        HttpHeaders headers = new HttpHeaders();
        try {
            TokenResponse tokenResponse = getRestTemplate().postForObject(url, new HttpEntity<>(map, headers), TokenResponse.class);
            LOGGER.info("getAccessToken url:{},token:{}", url, tokenResponse);
            assert tokenResponse != null;
            if (tokenResponse.getErrcode() == 0) {
                //Cache access_token
                stringRedisTemplate.opsForValue().set(tokenKey, tokenResponse.getAccess_token(),
                        tokenResponse.getExpires_in(), TimeUnit.SECONDS);
                return tokenResponse.getAccess_token();
            }
        } catch (RestClientException e) {
            throw new RestClientException("gettoken Authentication request error. msg: " + e.getMessage());
        }
        return "";
    }

    /**
     * Get user info by code.
     * @param code Authentication Code
     * @param agentApp
     * @return UserId
     * @throws Exception
     */
    public String getUserIdByCode(AgentApp agentApp,String code) {
        try {
            String accessToken = this.getAccessToken(agentApp);
            String url = String.format("%s/cgi-bin/user/getuserinfo?access_token=%s&code=%s",this.baseUrl,accessToken,code);
            HttpHeaders headers =  new HttpHeaders();
            Map<String, Object> map = new HashMap<>(1);
            String resp = getRestTemplate().postForObject(url, new HttpEntity<>(map, headers), String.class);
            LOGGER.info("getAccessToken,url:{},token:{},agentId:{},resp:{}", url, accessToken, agentApp.agentId, resp);
            UserInfoResponse userInfoResponse = JSONUtil.parse(resp).toBean(UserInfoResponse.class);
            LOGGER.info("getUserInfo url:{},token:{}", url, userInfoResponse);
            assert userInfoResponse != null;
            if (userInfoResponse.getErrcode() == 0 ){
                return userInfoResponse.getUserId();
            }
        }catch (RestClientException e) {
            throw new RestClientException("getUserIdByCode fail. msg: " + e.getMessage());
        }catch (Exception e){
            LOGGER.info("getUserIdByCode fail，code:{} ,err:{}", code, e.getMessage());
        }
        return "";
    }

    @Override
    public String buildRedirectUrl(AgentApp agentApp,String callbackUri) throws UnsupportedEncodingException {
        //Get the associated application through host
        String redirectUri = URLEncoder.encode(agentApp.getCallbackDomain() + callbackUri, StandardCharsets.UTF_8.toString());
        return String.format(
                "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_base&agentid=%s&state=STATE#wechat_redirect",
                this.corpId,
                redirectUri,
                agentApp.getAgentId()
        );
    }

    @Override
    public String buildQRRedirectUrl(AgentApp agentApp, String callbackUri) throws UnsupportedEncodingException {
        String redirectUri = URLEncoder.encode(agentApp.getCallbackDomain() + callbackUri, StandardCharsets.UTF_8.toString());
        return String.format("%s/wwopen/sso/qrConnect?appid=%s&=&agentid=%s&redirect_uri=%s&state=web_login@gyoss9",
                agentApp.getQrDomain(),
                this.corpId, agentApp.getAgentId(),
                redirectUri );
    }


    @Override
    public AgentApp getAgentAppByHost(String host){
        for(AgentApp agentApp : this.agentAppList){
            //Return App by host
            if (agentApp.getCallbackDomain().contains(host)){
                return agentApp;
            }
        }
        return agentAppList.get(0);
    }

}
