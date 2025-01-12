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
import { Spin } from 'antd';
import React from 'react';
import { Api, Navigation, Strings, t } from '@apitable/core';
import { Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery } from 'pc/hooks';
import styles from './style.module.less';

export const IdassCallback: React.FC = () => {
  const query = useQuery();
  const code = query.get('code') || '';
  const state = query.get('state') || '';
  const clientId = query.get('client_id') || '';
  const reference = query.get('reference') || localStorage.getItem('reference');

  useMount(() => {
    Api.idaasLoginCallback(clientId, code, state).then(res => {
      const { success, message } = res.data;
      if (success) {
        if (reference) {
          localStorage.removeItem('reference');
          window.location.href = reference;
          return;
        }
        Router.push(Navigation.HOME);
      } else {
        Message.error({ content: message || t(Strings.login_failed) });
        Router.push(Navigation.LOGIN, { query: { client_id: clientId } });
      }

    });
  });

  return (
    <div className={styles.idaasCallback}>
      <Spin />
    </div>
  );
};