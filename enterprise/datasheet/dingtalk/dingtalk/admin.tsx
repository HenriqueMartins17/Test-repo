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
import { useState } from 'react';
import { Api, Navigation, Settings, Strings, t } from '@apitable/core';
import { Loading, Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useRequest } from 'pc/hooks';
import { getStorage, setStorage, StorageName } from 'pc/utils/storage';
import { AdminLayout, IAdminData } from './admin_layout';

const config = {
  adminTitle: t(Strings.dingtalk_admin_panel_title),
  adminDesc: t(Strings.dingtalk_admin_panel_message),
  helpLink: Settings.integration_dingtalk_help_url.value,
};

export const DingTalkAdmin = () => {
  const query = useQuery();
  const code = query.get('code') || '';
  const suiteId = query.get('suiteId') || '';
  const [data, setData] = useState<IAdminData | null>(null);
  const [corpId, setCorpId] = useState<string>(() => getStorage(StorageName.SocialPlatformMap)?.socialDingTalk?.[code] || '');

  //  Change Manager
  const { run: changeAdmin } = useRequest((spaceId, memberId) => Api.dingTalkChangeAdmin(suiteId, corpId, spaceId, memberId), {
    onError: () => {
      Message.error({ content: t(Strings.error) });
    },
    onSuccess: res => {
      const { success, message } = res.data;
      if (!success) {
        return Message.error({ content: message });
      }
      Message.success({ content: t(Strings.success) });
      return getAdminDetail(corpId);
    },
    manual: true,
  });

  // Get the bound space information
  const { run: getAdminDetail } = useRequest((_corpId: string) => Api.dingTalkAdminDetail(suiteId, _corpId), {
    onSuccess: res => {
      const { data, success } = res.data;

      if (!success) {
        return Router.push(Navigation.LOGIN);
      }
      return setData(data);
    },
    onError: () => {
      Message.error({ content: t(Strings.error) });
    },
    manual: true
  });

  // Administrator Login
  const { run: adminLogin } = useRequest(() => Api.dingTalkAdminLogin(suiteId, code, corpId), {
    onSuccess: res => {
      const { data, success } = res.data;

      if (!success || !data.bindSpaceId) {
        return Message.error({ content: t(Strings.error) });
      }

      /**
       * The corpId needs to be stored in localStorage.
       * After a browser refresh, the login interface needs to be called again and the corpId
       * needs to be passed back to the backend with the current corporate ID
       */
      const corpId = data.corpId;
      const socialPlatformMap = getStorage(StorageName.SocialPlatformMap) || {};
      socialPlatformMap.socialDingTalk = { [code]: corpId };
      setStorage(StorageName.SocialPlatformMap, socialPlatformMap);
      setCorpId(corpId);
      return getAdminDetail(corpId);
    },
    onError: () => {
      Message.error({ content: t(Strings.error) });
    },
    manual: true
  });

  useMount(() => {
    if (suiteId) {
      adminLogin();
    }
  });

  return (
    <>
      {
        data ?
          <AdminLayout
            data={data}
            config={config}
            onChange={changeAdmin}
          /> :
          <Loading />
      }
    </>
  );
};
