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
import { Loading, Message } from '@apitable/components';
import { Api, Navigation, Strings, t } from '@apitable/core';
import { Router } from 'pc/components/route_manager/router';
import { useQuery } from 'pc/hooks';
import styles from './styles.module.less';

export const WecomToBind: React.FC = () => {
  const query = useQuery();
  const code = query.get('code') || '';
  const domainName = query.get('domainName') || '';
  const configSha = query.get('configSha') || '';
  const spaceId = query.get('spaceId') || '';

  const toErrorPage = (errorCode?: string) => {
    Router.push(Navigation.WECOM, {
      params: {
        wecomPath: 'error'
      }, clearQuery: true, query: { errorCode }
    });
  };

  useEffect(() => {
    if (!configSha || !code || !spaceId) {
      toErrorPage();
      return;
    }
    Api.socialWecomBindConfig(configSha, code, spaceId).then(res => {
      const { success, code } = res.data;
      if (success) {
        Message.success({ content: t(Strings.binding_success) });
        Router.push(Navigation.WECOM, {
          params: { wecomPath: 'integration/bind_success' }, query: {
            domainName
          }, clearQuery: true
        });
        return;
      }
      toErrorPage(code);
    }).catch(() => {
      toErrorPage();
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className={styles.loadingWrap}>
      <Loading />
      <div className={styles.loadingText}>{t(Strings.integration_app_wecom_bind_loading_text)}</div>
    </div>
  );
};
