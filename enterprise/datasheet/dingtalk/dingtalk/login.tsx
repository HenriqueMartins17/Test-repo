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
import dd from 'dingtalk-jsapi';
import { IRuntimePermissionRequestAuthCodeParams } from 'dingtalk-jsapi/api/runtime/permission/requestAuthCode';
import { useState } from 'react';
import { Api, Navigation, StatusCode, Strings, t } from '@apitable/core';
import { Loading, Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useRequest } from 'pc/hooks';
import { ContactSyncing } from './contact_syncing';

export const DingTalkLogin = () => {
  const query = useQuery();
  const corpId = query.get('corpId') || '';
  const suiteId = query.get('suiteId') || '';
  const bizAppId = query.get('bizAppId') || '';
  const [count, setCount] = useState(0);
  const [isSyncing, setSyncing] = useState(false);

  const { run: login } = useRequest(code => Api.dingTalkUserLogin(suiteId, corpId, code, bizAppId), {
    onSuccess: res => {
      const { data, success, code } = res.data;

      // Retries when internal services are unstable
      if (code === StatusCode.COMMON_ERR && count < 3) {
        setCount(count + 1);
        return dd.runtime.permission.requestAuthCode({
          corpId,
          onSuccess: (res: any) => login(res.code),
          onFail: (err: any) => Message.error({ content: err.errorMessage }),
        } as IRuntimePermissionRequestAuthCodeParams);
      }

      // Situations requiring contact with the administrator
      if ([StatusCode.DINGTALK_NOT_BIND_SPACE, StatusCode.DINGTALK_USER_NOT_EXIST].includes(code)) {
        Message.error({ content: t(Strings.dingtalk_tenant_not_exist_tips) });
        return Router.push(Navigation.LOGIN, {
          clearQuery: true
        });
      }

      if (code === StatusCode.DINGTALK_USER_CONTACT_SYNCING) {
        return setSyncing(true);
      }

      // For the rest, contact customer service
      if (!success || !data.bindSpaceId) {
        Message.error({ content: t(Strings.dingtalk_login_fail_tips) });
        return Router.push(Navigation.LOGIN, {
          clearQuery: true
        });
      }

      const { shouldRename, defaultName } = data;
      if (shouldRename) {
        return Router.push(Navigation.SETTING_NICKNAME, {
          query: { defaultName },
          clearQuery: true
        });
      }

      return Router.push(Navigation.WORKBENCH, {
        params: { spaceId: data.bindSpaceId, nodeId: bizAppId },
        clearQuery: true
      });
    },
    onError: () => {
      Message.error({ content: t(Strings.dingtalk_login_fail_tips) });
    },
    manual: true
  });

  useMount(() => {
    if (suiteId && corpId) {
      dd.runtime.permission.requestAuthCode({
        corpId,
        onSuccess: (res : any) => login(res.code),
        onFail: (err: any) => Message.error({ content: err.errorMessage }),
      } as IRuntimePermissionRequestAuthCodeParams);
    }
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
