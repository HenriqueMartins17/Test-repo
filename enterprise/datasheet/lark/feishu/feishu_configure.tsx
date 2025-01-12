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
import { useEffect, useState } from 'react';
import { Api, Navigation, Strings, t } from '@apitable/core';
import { Loading, Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useRequest } from 'pc/hooks';
import { FeishuErrType } from './feishu_err';

const FeiShuConfigure = () => {
  const query = useQuery();
  const [err, setErr] = useState('');
  const appId = query.get('app_id') || query.get('appId') || undefined;
  const accessToken = query.get('access_token') || query.get('accessToken') || undefined;
  const { run: check } = useRequest(token => Api.socialFeiShuUserAuth(token), {
    onSuccess: async (res) => {
      const { success, data } = res.data;
      if (!success) {
        Router.push(Navigation.FEISHU, {
          params: { feiShuPath: 'err' },
          query: {
            key: FeishuErrType.SELECT_VALID,
          },
        });
        return;
      }
      const { openId, tenantKey } = data;
      const adminCheckRes = await Api.socialFeiShuCheckAdmin(openId, tenantKey);
      const { success: checkAdminSuccess, data: checkAdminInfo, message: checkAdminMsg } = adminCheckRes.data;
      if (!checkAdminSuccess) {
        setErr(checkAdminMsg);
        return;
      }
      if (!checkAdminInfo.isAdmin) {
        Router.push(Navigation.FEISHU, {
          params: { feiShuPath: 'err' },
          query: {
            key: FeishuErrType.IDENTITY,
          },
        });
        return;
      }

      const tenantBindRes = await Api.socialFeiShuCheckTenantBind(tenantKey);
      const { success: tenantBindSuccess, data: tenantBindData, message: tenantBindMsg } = tenantBindRes.data;
      if (tenantBindSuccess && tenantBindData.hasBind) {
        Router.push(Navigation.FEISHU, {
          clearQuery: true,
          params: { feiShuPath: 'err' },
          query: {
            key: FeishuErrType.BOUND,
            appId,
          },
        });
        return;
      }
      if (tenantBindSuccess && !tenantBindData.hasBind) {
        Router.push(Navigation.FEISHU, {
          params: { feiShuPath: 'admin_login' },
          query: { openId, tenantKey },
        });
        return;
      }
      if (!tenantBindSuccess) {
        setErr(tenantBindMsg);
        return;
      }
    },
    onError: () => {
      Message.error({ content: t(Strings.error) });
    },
    manual: true,
  });
  useMount(() => {
    if (!accessToken) return;
    check(accessToken);
  });

  useEffect(() => {
    if (err) {
      Router.push(Navigation.FEISHU, {
        params: { feiShuPath: 'err' },
        query: {
          msg: err,
        },
      });
    }
  }, [err]);

  return <Loading />;
};

export default FeiShuConfigure;
