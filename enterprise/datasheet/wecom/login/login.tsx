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

import { useMount } from 'ahooks';
import React, { useState } from 'react';
import { ConfigConstant } from '@apitable/core';
import { Loading } from 'pc/components/common';
import { useQuery } from 'pc/hooks';
import { ContactSyncing } from '../../dingtalk/dingtalk/contact_syncing';
// import { Navigation } from '@apitable/core';
import { getWecomShopConfig } from '../../login/utils';
// import { useNavigation } from 'pc/components/route_manager/use_navigation';

const navigationToAuth = (env: any, suiteId: string, wecomShopLoginType: string) => {
  localStorage.removeItem('wecomShopLoginCache');
  const url = 'https://open.weixin.qq.com/connect/oauth2/authorize';
  const redirectUrl = env.callbackUrl;
  localStorage.setItem('wecomShopLoginType', wecomShopLoginType);
  window.location.href =
    `${url}?appid=${suiteId}&redirect_uri=${redirectUrl}&state=${suiteId}&response_type=code&scope=snsapi_base#wechat_redirect`;
};

export const WecomLogin = () => {
  const query = useQuery();
  // const navigationTo = useNavigation();
  const env = getWecomShopConfig();
  // const cache = localStorage.getItem('wecomShopLoginCache');
  // const cacheInfo = cache ? JSON.parse(cache) : {};
  // const corpId = cacheInfo?.authCorpId || '';
  const suiteId = query.get('suiteid') || '';
  const reference = query.get('reference') || '';
  const [isSyncing/* , setSyncing */] = useState(false);

  useMount(() => {
  
    localStorage.setItem('wecomShopLoginToReference', reference);
    navigationToAuth(env, suiteId, ConfigConstant.AuthReference.APPLICATION);
  });

  return (
    <>
      {
        isSyncing ?
          <ContactSyncing /> :
          <Loading />
      }
    </>
  );
};