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

import * as React from 'react';
import { useEffect } from 'react';
import { Message } from '@apitable/components';
import { Api, Navigation } from '@apitable/core';
import { Router } from 'pc/components/route_manager/router';
import { useQuery } from 'pc/hooks';

export const WoaCallback: React.FC = () => {
  const query = useQuery();
  const code = query.get('code') || '';
  const appid = query.get('state') || '';

  useEffect(() => {
    Api.woaLoginCallback(code, appid).then(res => {
      const { success, data, message } = res.data;
      if (success) {
        if (data && data.bindSpaceId) {
          Router.redirect(Navigation.WORKBENCH, { query: { spaceId: data.bindSpaceId }, clearQuery: true });
          return;
        }
        Router.redirect(Navigation.HOME);
        return;
      }
      Message.error({ content: message });
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return <div />;
};
