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

import { Spin } from 'antd';
import * as React from 'react';
import { useEffect } from 'react';
import { Message } from '@apitable/components';
import { Api, Navigation, Strings, t } from '@apitable/core';
import { Router } from 'pc/components/route_manager/router';
import { useQuery } from 'pc/hooks';
import styles from './styles.module.less';

export const WecomCallback: React.FC = () => {
  const query = useQuery();
  const code = query.get('code') || '';
  const agentId = query.get('agentId') || '';
  const corpId = query.get('corpId') || '';
  const reference = query.get('reference') || '';

  useEffect(() => {
    Api.wecomLoginCallback(code, agentId, corpId).then(res => {
      const { success, data, code } = res.data;
      if (success) {
        if (reference) {
          window.location.href = reference;
        } else if (data && data.bindSpaceId) {
          Router.redirect(Navigation.WORKBENCH, { clearQuery: true });
        } else {
          Router.redirect(Navigation.HOME);
        }
      } else if (code === 1109) {
        switch (code) {
          case 1109:
            Message.error({ content: t(Strings.wecom_logo_unauthorized_error) });
            break;
          default:
            Message.error({ content: t(Strings.login_failed) });
        }
        Router.push(Navigation.LOGIN);
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return (
    <div className={styles.wecomCallback}>
      <Spin />
    </div>
  );
};
