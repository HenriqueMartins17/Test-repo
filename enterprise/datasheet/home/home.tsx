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

import { configResponsive, useResponsive } from 'ahooks';
import { FC, useEffect } from 'react';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import { Api, getCustomConfig, IReduxState, Navigation, StoreActions, Strings, t } from '@apitable/core';
import { Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { getSearchParams } from 'pc/utils';
import MobileHome from './mobile_home';
import PcHome from './pc_home';
import { isSocialDomain } from './social_platform';
import styles from './style.module.less';

configResponsive({
  large: 1023.98,
});

export const Home: FC = () => {
  configResponsive({
    large: 1023.98,
  });
  const responsive = useResponsive();
  const dispatch = useDispatch();
  const urlParams = getSearchParams();
  const reference = urlParams.get('reference') || undefined;

  const { isLogin } = useSelector((state: IReduxState) => (
    { isLogin: state.user.isLogin, user: state.user }), shallowEqual);

  useEffect(() => {
    try {
      localStorage.removeItem('qq_login_failed');
    } catch (e) {}
    const storageChange = (e: StorageEvent) => {
      if (!e.newValue) {
        return;
      }
      if (e.key === 'qq_login_failed') {
        if (e.newValue === 'true') {
          Message.error({ content: t(Strings.login_failed) });
        }
        localStorage.removeItem('qq_login_failed');
      }
    };
    window.addEventListener('storage', storageChange);
    return () => {
      window.removeEventListener('storage', storageChange);
    };
  }, []);

  useEffect(() => {
    if (!isSocialDomain()) {
      return;
    }
    Api.socialTenantEnv().then(res => {
      // eslint-disable-next-line no-unsafe-optional-chaining
      const { success, data } = res?.data;
      if (success) {
        dispatch(StoreActions.setEnvs(data?.envs ?? {}));
      }
    });
  }, [dispatch]);

  const { footer } = getCustomConfig();

  if (isLogin) {
    if (reference) {
      Router.redirect(Navigation.HOME, {
        query: {
          reference,
        }
      });
    } else {
      Router.redirect(Navigation.WORKBENCH);
    }
  }

  return <>
    <div className={styles.homeWrapper}>
      {responsive?.large || process.env.SSR ? <PcHome /> : <MobileHome />}
    </div>
    {footer && (
      <footer className={styles.footer}>
        {footer}
      </footer>
    )}
  </>;
};

