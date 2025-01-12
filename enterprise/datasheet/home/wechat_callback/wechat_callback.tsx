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
import { FC, useEffect } from 'react';
import { Api, Navigation, Strings, t } from '@apitable/core';
import { Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery } from 'pc/hooks';
import styles from './style.module.less';

export const WechatCallback: FC = () => {
  const query = useQuery();
  const code = query.get('code') || '';
  const state = query.get('state') || '';
  const reference = localStorage.getItem('reference');

  useEffect(() => {
    Api.wechatLoginCallback(code, state).then(res => {
      const { success, data } = res.data;
      if (success) {
        if (data) {
          Router.push(Navigation.IMPROVING_INFO, { query: { token: data } });
        } else {
          if (reference) {
            localStorage.removeItem('reference');
            window.location.href = reference;
            return;
          }
          Router.redirect(Navigation.HOME,);
        }
      } else {
        Message.error({ content: t(Strings.login_failed) });
        Router.push(Navigation.LOGIN);
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className={styles.container}>
      <Spin />
    </div>
  );
};