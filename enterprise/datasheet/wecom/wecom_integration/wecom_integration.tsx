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

import { useRouter } from 'next/router';
import * as React from 'react';
import { PropsWithChildren, useRef, useState } from 'react';
import { Loading } from '@apitable/components';
import { Api, IUserInfo, Strings, t } from '@apitable/core';
import { useRequest } from 'pc/hooks';
import { WecomIntegrationContext } from './wecom_integration_context';
import { WecomIntegrationHeader } from './wecom_integration_header';
import styles from './styles.module.less';

export const WecomIntegration: React.FC<PropsWithChildren<any>> = ({ children }) => {
  const router = useRouter();
  const { loading: isLoginStatusGetting } = useRequest(() => Api.getUserMe().then(res => {
    const { data, success } = res.data;
    if (success) {
      setUserInfo(data);
    }
  }));
  const wecomIntegrationRef = useRef<HTMLDivElement>(null);
  const [userInfo, setUserInfo] = useState<IUserInfo | null>(null);

  const scrollTo = (x?: number, y?: number) => {
    const defaultScrollX = wecomIntegrationRef?.current?.scrollTop || 0;
    const defaultScrollY = wecomIntegrationRef?.current?.scrollLeft || 0;
    wecomIntegrationRef?.current?.scrollTo(x || defaultScrollX, y || defaultScrollY);
  };

  if (isLoginStatusGetting) {
    return <Loading />;
  }

  return (
    userInfo?.isAdmin ?
      <WecomIntegrationContext.Provider
        value={{
          scrollTo
        }}
      >
        <div className={styles.wecomIntegrationWrap}>
          <div className={styles.wecomIntegration} ref={wecomIntegrationRef}>
            <WecomIntegrationHeader userInfo={userInfo} />
            <div className={styles.container}>
              {children}
            </div>
            <div className={styles.statementFooter}>
              <div className={styles.statementText}>copyright © 2019-2021 深圳维格智数科技有限公司.All rights reserved.</div>
              <a href="/help/how-contact-service" target="_blank">{t(Strings.connect_us)}</a>
            </div>
          </div>
        </div>
      </WecomIntegrationContext.Provider> : <>
        {
          router.replace('/')
        }
      </>

  );
};