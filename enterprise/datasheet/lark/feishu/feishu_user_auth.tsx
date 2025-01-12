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
import { Api, Navigation, StatusCode, Strings, t } from '@apitable/core';
import { Loading, Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useRequest } from 'pc/hooks';
import { FeishuErrType } from './feishu_err';

const FeiShuUserAuth = () => {
  const query = useQuery();
  const [err, setErr] = useState('');
  const accessToken =
    query.get('access_token') || query.get('accessToken') || undefined;
  const { run: check } = useRequest(
    (token) => Api.socialFeiShuUserAuth(token),
    {
      onSuccess: async (res) => {
        const { success, data, message } = res.data;
        if (!success) {
          setErr(message);
          return;
        }
        const { openId, tenantKey } = data;
        const bindDataRes = await Api.socialFeiShuCheckTenantBind(tenantKey);
        const { success: bindDataSuccess, data: bindData } = bindDataRes.data;
        if (!bindDataSuccess) {
          Router.push(Navigation.FEISHU, {
            params: { feiShuPath: 'err' },
            query: {
              msg: FeishuErrType.IDENTITY,
            },
          });
          return;
        }
        if (bindDataSuccess && !bindData.hasBind) {
          const adminRes = await Api.socialFeiShuCheckAdmin(openId, tenantKey);
          const { success, data: adminInfo } = adminRes.data;
          if (success && adminInfo.isAdmin) {
            Router.push(Navigation.FEISHU, {
              params: { feiShuPath: 'admin_login' },
              query: { openId, tenantKey },
            });
            return;
          } else if (success && !adminInfo.isAdmin) {
            Router.push(Navigation.FEISHU, {
              params: { feiShuPath: 'err' },
              query: {
                key: FeishuErrType.CONFIGURING,
              },
            });
            return;
          }
          return;
        }
        if (bindDataSuccess && bindData.hasBind) {
          const loginRes = await Api.feishuUserLogin(openId, tenantKey);
          const {
            success: loginSuccess,
            code,
            message: loginMsg,
          } = loginRes.data;
          if (loginSuccess) {
            const bindDetailRes = await Api.feishuTenantBindDetail(tenantKey);
            const { success, data } = bindDetailRes.data;
            if (success) {
              const list = data.bindInfoList;
              if (list && typeof list === 'object') {
                Router.redirect(Navigation.WORKBENCH, {
                  params: { spaceId: list[0].spaceId },
                });
              } else {
                Router.redirect(Navigation.HOME);
              }
              return;
            }
            return;
          }
          if (code === StatusCode.FEISHU_ACCOUNT_NOT_BOUND) {
            Router.push(Navigation.FEISHU, {
              params: { feiShuPath: 'bind_user' },
              query: {
                openId,
                tenantKey,
              },
            });
          } else {
            setErr(loginMsg);
            return;
          }
        }
      },
      onError: () => {
        Message.error({ content: t(Strings.error) });
      },
      manual: true,
    }
  );
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

export default FeiShuUserAuth;
