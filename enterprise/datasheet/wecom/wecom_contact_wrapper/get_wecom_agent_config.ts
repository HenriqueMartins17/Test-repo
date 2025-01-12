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

import { isObject } from 'lodash';
import { Api } from '@apitable/core';

declare const WWOpenData: any;

export const getWecomAgentConfig = async (spaceId: string | null, cb?: () => void) => {
  const wx = (window as any).wx;
  const rlt = await Api.getWecomAgentConfig(spaceId!, window.location.href);
  if (rlt.data.message === 'SUCCESS') {
    const { authCorpId, agentId, random, signature, timestamp } = rlt.data.data;
    try {
      wx.agentConfig?.({
        corpid: authCorpId,
        agentid: agentId,
        timestamp,
        nonceStr: random,
        signature,
        jsApiList: ['selectExternalContact', 'selectPrivilegedContact'],
        success: function (res: any) {
          console.log('success', res);
          if (cb) {
            cb();
          }
          if (isObject(WWOpenData)) {
            (WWOpenData as any).bindAll(document.querySelectorAll('ww-open-data'));
          }
        },
        fail: function (res: { errMsg: string | string[]; }) {
          console.log('fail', res);
          if(res.errMsg.indexOf('function not exist') > -1) {
            console.warn('Please upgrade if the version is too low');
          }
        }
      });
    } catch (e) {
      console.warn('wecom agentConfig error: ', e);
    }
  }
};