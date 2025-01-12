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
import { Api, Navigation, Settings, StatusCode, Strings, t } from '@apitable/core';
import { Loading, Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useRequest } from 'pc/hooks';
import { getStorage, setStorage, StorageName } from 'pc/utils/storage';
// @ts-ignore
import { AdminLayout, IAdminData } from 'enterprise/dingtalk/dingtalk/admin_layout/admin_layout';

const config = {
  adminTitle: t(Strings.wecom_admin_title),
  adminDesc: t(Strings.wecom_admin_desc),
  helpLink: Settings.integration_wecom_shop_cms.value,
};

export const WecomAdmin = () => {
  const query = useQuery();
  const code = query.get('code') || query.get('auth_code') || '';
  const suiteId = query.get('suiteid') || '';
  const [data, setData] = useState<IAdminData | null>(null);
  const [corpId, setCorpId] = useState<string>(() => getStorage(StorageName.SocialPlatformMap)?.socialWecom?.corpId || '');
  const [cpUserId, setCpUserId] = useState<string>(() => getStorage(StorageName.SocialPlatformMap)?.socialWecom?.cpUserId || '');
  const { run: changeAdmin } = useRequest((spaceId, memberId) => Api.postWecomChangeAdmin(
    corpId,
    memberId,
    spaceId,
    suiteId,
    cpUserId,
  ), {
    onError: () => {
      Message.error({ content: t(Strings.error) });
    },
    onSuccess: res => {
      const { success, code } = res.data;
      if (!success) {
        if (code === StatusCode.WECOM_NOT_ADMIN) {
          Router.push(Navigation.WECOM, {
            params: { wecomPath: 'error' },
            clearQuery: true,
            query: { errorCode: String(code) }
          });
        }
        return;
      }
      Message.success({ content: t(Strings.success) });
      return getAdminDetail();
    },
    manual: true,
  });

  // Get information about the space to which the user is bound
  const { run: getAdminDetail } = useRequest(() => Api.getWecomBindSpacesInfo(corpId, suiteId, cpUserId), {
    onSuccess: res => {
      const { data, success, code } = res.data;

      if (!success) {
        if (code === StatusCode.WECOM_NOT_ADMIN) {
          Router.push(Navigation.WECOM, {
            params: { wecomPath: 'error' },
            clearQuery: true,
            query: { errorCode: String(code) }
          });
        }
        return;
      }
      return setData(data);
    },
    onError: () => {
      Message.error({ content: t(Strings.error) });
    },
    manual: true
  });

  const { run: adminLogin } = useRequest(() => Api.postWecomLoginAdmin(code, suiteId), {
    onSuccess: (res) => {
      const { data, success, code } = res.data;

      if (!success || !data.spaceId) {
        if (code === StatusCode.WECOM_NOT_ADMIN) {
          Router.push(Navigation.WECOM, {
            params: { wecomPath: 'error' },
            clearQuery: true,
            query: { errorCode: String(code) }
          });
        }
        return;
      }

      const { authCorpId: corpId, cpUserId } = data;
      const socialPlatformMap = getStorage(StorageName.SocialPlatformMap) || {};
      socialPlatformMap.socialWecom = { corpId, cpUserId };
      setStorage(StorageName.SocialPlatformMap, socialPlatformMap);
      setCorpId(corpId);
      setCpUserId(cpUserId);

      const url = new URL(window.location.href);
      const params = url.searchParams;
      params.delete('code');
      params.delete('auth_code');
      window.location.href = url.href;
    },
    onError: () => {
      Message.error({ content: t(Strings.error) });
    },
    manual: true
  });

  useMount(() => {
    if (code) {
      adminLogin();
    } else {
      getAdminDetail();
    }
  });

  return (
    <>
      {
        data ?
          (
            AdminLayout &&
            <AdminLayout
              data={data}
              config={config}
              onChange={changeAdmin}
            />
          ) :
          <Loading />
      }
    </>
  );
};