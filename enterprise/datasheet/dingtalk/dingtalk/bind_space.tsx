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
import { Api, Navigation, Strings, t } from '@apitable/core';
import { Loading, Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useRequest } from 'pc/hooks';

export const DingTalkBindSpace = () => {
  const query = useQuery();
  const suiteId = query.get('suiteId') || '';
  const corpId = query.get('corpId') || '';
  const bizAppId = query.get('bizAppId') || '';
  const reference = query.get('reference') || '';

  const { run } = useRequest(() => Api.dingTalkBindSpace(suiteId, corpId), {
    onSuccess: res => {
      const { data, success } = res.data;

      if (!success || !data?.bindSpaceId) {
        return Router.push(Navigation.DINGTALK, {
          params: { dingtalkPath: 'social_login' },
          query: { suiteId, corpId, bizAppId },
          clearQuery: true
        });
      }
      // click notify card jump to reference record url
      if(reference) {
        return window.location.href = reference;
      }
      // return window.location.href = `/workbench/${bizAppId}?spaceId=${data.bindSpaceId}`;
      return Router.push(Navigation.WORKBENCH, {
        params: { spaceId: data.bindSpaceId, nodeId: bizAppId },
        query: { spaceId: data.bindSpaceId },
        clearQuery: true
      });
    },
    onError: () => {
      Message.error({ content: t(Strings.error) });
    },
    manual: true
  });

  useMount(() => {
    if (suiteId) {
      run();
    }
  });

  return <Loading />;
};
