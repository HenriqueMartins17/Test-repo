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

import dd from 'dingtalk-jsapi';
import { Api, Url } from '@apitable/core';

export const getDingtalkConfig = async (spaceId: string) => {
  const res = await Api.getDingtalkConfig(spaceId, window.location.href);
  try {
    const { data, success } = res.data;

    if (!success || !data) return;
    dd.config({
      ...data,
      type: 0,
      jsApiList: ['device.base.getUUID'],
    });

    dd.ready(() => {
      dd.device.base.getUUID({
        onSuccess: (data: any) => {
          console.log(`uuid: ${data.uuid}`);
        },
        onFail: (err: any) => {
          console.warn(`uuid err: ${err}`);
        },
      });
    });

    dd.error(err => {
      console.warn(`dingtalk error: ${err}`);
    });
  } catch (err) {
    console.warn(`getConfig: ${err}`);
  }
};

const isMatch = (staticUrl: string, url: string) => {
  const formatArr = staticUrl.replace(/:[0-9A-Za-z]+/g, '*').split('*');
  return formatArr.every(item => url.includes(item));
};

export const isSocialUrlIgnored = (url: string) => {
  if (
    isMatch(Url.SOCIAL_FEISHU_BIND_SPACE, url) ||
    isMatch(Url.DINGTALK_H5_BIND_SPACE, url) ||
    isMatch(Url.SOCIAL_DINGTALK_BIND_SPACE, url) ||
    isMatch(Url.SOCIAL_DINGTALK_ADMIN_DETAIL, url)
  ) {
    return true;
  }

  if (url.includes(Url.WECOM_AGENT_BINDSPACE) || url.includes('dataPack')) {
    return true;
  }

  return false;
};