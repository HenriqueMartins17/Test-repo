/**
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

import { Url } from '@apitable/core';
import { getEnvVariables } from 'pc/utils/env';

enum ThirdParty {
  Dingding = 'dingding',
  Qq = 'qq',
  Feishu = 'feishu',
  Wechat = 'wechat',
  Wecom = 'wecom',
  WecomShop = 'wecomShop',
}

const getThirdPartyAuthConfigs = (thirdParty: ThirdParty): any => {
  if (!process.env.SSR) {
    const envVariables = getEnvVariables();
    switch(thirdParty) {
      case ThirdParty.Dingding:
        return {
          appId: envVariables.DINGTALK_LOGIN_APPID,
          callbackUrl: window.location.origin + '/user/dingtalk/callback',
        };
      case ThirdParty.Qq:
        return {
          appId: envVariables.QQ_CONNECT_WEB_APPID,
          callbackUrl: window.location.origin + '/user/qq_connect/callback',
        };
      case ThirdParty.Feishu:
        return {
          appId: envVariables.integration_feishu_login_appid,
          pathname: Url.BASE_URL + Url.FEISHU_LOGIN_CALLBACK,
        };
      case ThirdParty.Wechat:
        return {
          appId: envVariables.WECHAT_MP_APPID,
          callbackUrl: window.location.origin + '/user/wechat/callback',
        };
      case ThirdParty.Wecom:
        return {
          callbackUrl: window.location.origin + '/user/wecom/callback',
        };
      case ThirdParty.WecomShop:
        return {
          suiteId: envVariables.WECOM_SHOP_SUITEID,
          corpId: envVariables.WECOM_SHOP_CORPID,
          callbackUrl: window.location.origin + '/user/wecom_shop/callback',
        };
      default:
        return null;
    }
  }
  return null;
};

export const getDingdingConfig = () => {
  return getThirdPartyAuthConfigs(ThirdParty.Dingding);
};

export const getQQConfig = () => {
  return getThirdPartyAuthConfigs(ThirdParty.Qq);
};

export const getFeishuConfig = () => {
  return getThirdPartyAuthConfigs(ThirdParty.Feishu);
};

export const getWechatConfig = () => {
  return getThirdPartyAuthConfigs(ThirdParty.Wechat);
};

export const getWecomConfig = () => {
  return getThirdPartyAuthConfigs(ThirdParty.Wecom);
};

export const getWecomShopConfig = () => {
  return getThirdPartyAuthConfigs(ThirdParty.WecomShop);
};
