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
import React, { useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';
import { Api, IReduxState } from '@apitable/core';
import { useRequest } from 'pc/hooks';
import { isSocialWecom } from '../../home';
import { getWecomAgentConfig } from './get_wecom_agent_config';

export const WecomContactWrapper = (props: { children: any; }) => {
  const { children } = props;
  const spaceInfo = useSelector((state: IReduxState) => state.space.curSpaceInfo);
  const spaceId = useSelector((state: IReduxState) => state.space.activeId);
  const _isSocialWecom = isSocialWecom(spaceInfo);
  const urlRef = useRef<string | null>(null);

  const { run: getWecomCommonConfig } = useRequest(() => Api.getWecomCommonConfig(spaceId!, window.location.href), {
    onSuccess: res => {
      const { data } = res.data;
      const { authCorpId, random, signature, timestamp } = data;
      const wx = (window as any).wx;
      try {
        wx.config?.({
          beta: true, // It must be written this way, otherwise the jsapi of the wx.invoke call form will have problems
          debug: false, // Enable debugging mode, you can print the information returned by the api call on the PC
          appId: authCorpId, // Required, corpID for corporate wechat
          timestamp, // Required, timestamp for signature generation
          nonceStr: random, //Required, random string to generate signature
          signature, // Required, signature, see Appendix - JS-SDK usage permission signing algorithm
          jsApiList: ['agentConfig'] // Mandatory, list of JS interfaces to be used, all interfaces to be called need to be passed in
        });
        wx.ready?.(() => {
          getWecomAgentConfig(spaceId);
        });
      } catch (e) {
        console.warn('wecom config error: ', e);
      }
    },
    onError: () => {
    },
    manual: true
  });

  useEffect(() => {
    const url = window.location.href;
    const wx = (window as any).wx;
    if (!spaceId || !_isSocialWecom || urlRef.current === url || !wx || !isObject(wx)) return;
    urlRef.current = url;
    getWecomCommonConfig();
  });

  return (
    <>{children}</>
  );
};
