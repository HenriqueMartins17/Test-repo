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

import { FC, useEffect, useRef } from 'react';
import { Strings, t, isPrivateDeployment, getCustomConfig } from '@apitable/core';
import { isMobileApp } from 'pc/utils/env';
import { OtherLogin } from '../../../../login';
import { Login } from '../../../login';
import styles from './style.module.less';

export const MobileAndAccountLogin: FC = () => {
  const secondPageRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const initHeight = () => {
      const mobileHomeTarget = document.querySelector(`.${styles.mobileHome}`) as HTMLDivElement;
      if (!mobileHomeTarget) { return; }
      return mobileHomeTarget.clientHeight;
    };
    const onResize = () => {
      const secondPageTarget = (secondPageRef.current as HTMLDivElement);
      const mobileHomeTarget = document.querySelector(`.${styles.mobileHome}`) as HTMLDivElement;
      if (!secondPageTarget || !mobileHomeTarget) { return; }
      mobileHomeTarget.style.height = `${initHeight}px`;
      if (secondPageTarget.style.display === 'none') {
        secondPageTarget.style.display = 'block';
        return;
      }
      secondPageTarget.style.display = 'none';
    };
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  });

  const { slogan } = getCustomConfig();

  return (
    <div className={styles.mobileAndAccountLogin}>
      <div>
        <div className={styles.slogan}>{slogan || t(Strings.login_slogan)}</div>
        <Login />
      </div>
      {!isPrivateDeployment() && !isMobileApp() && <OtherLogin />}
    </div>
  );
};

