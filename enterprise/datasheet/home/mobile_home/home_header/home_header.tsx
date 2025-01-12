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

import { FC } from 'react';
import { Navigation, Strings, t } from '@apitable/core';
import { Logo } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import styles from './style.module.less';

export const HomeHeader: FC = () => {
  const jumpOfficialWebsite = () => {
    Router.newTab(Navigation.HOME, { query: { home: 1 } });
  };

  return (
    <div className={styles.headerContainer}>
      <div className={styles.logo}>
        <div className={styles.logoWrap} onClick={jumpOfficialWebsite}>
          <Logo />
        </div>
        <div className={styles.logoSlogan}>{t(Strings.login_logo_slogan_mobile)}</div>
      </div>
    </div>
  );
};
