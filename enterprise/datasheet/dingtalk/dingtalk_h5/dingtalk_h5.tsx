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
import React from 'react';
import { Api, Navigation, Strings, t } from '@apitable/core';
import { Loading, Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useRequest } from 'pc/hooks';

export const DingTalkH5 = () => {
  const query = useQuery();
  const agentId = query.get('agentId');
  const reference = query.get('reference') || '';

  const { run } = useRequest(agentId => Api.getDingtalkH5BindSpaceId(agentId), {
    manual: true,
    onSuccess: res => {
      const { data, success } = res.data;
      if (!success) {
        Router.push(Navigation.DINGTALK, {
          params: { dingtalkPath: 'login' },
          query: { reference }
        });
        return;
      }
      if (!data.bindSpaceId) {
        Message.error({ content: t(Strings.error) });
        return;
      }
      // window.location.href = `/workbench?reference=${reference}&spaceId=${data.bindSpaceId}`;
      Router.push(Navigation.WORKBENCH, {
        params: { spaceId: data.bindSpaceId },
        query: { reference, spaceId: data.bindSpaceId }
      });
      return;
    },
    onError: () => {
      Message.error({ content: t(Strings.error) });
    }
  });
  useMount(() => {
    if (agentId) {
      run(agentId);
    }
  });
  return <Loading />;
};