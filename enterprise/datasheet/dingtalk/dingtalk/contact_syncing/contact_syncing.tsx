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

import parser from 'html-react-parser';
import Image from 'next/image';
import React from 'react';
import { useSelector } from 'react-redux';
import { ThemeName } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { Wrapper } from 'pc/components/common';
import CreateSpaceIconDark from 'static/icon/space/space_add_name_dark.png';
import CreateSpaceIconLight from 'static/icon/space/space_add_name_light.png';
import styles from './style.module.less';

export const ContactSyncing = () => {
  const themeName = useSelector(state => state.theme);
  const WelcomePng = themeName === ThemeName.Light ? CreateSpaceIconLight : CreateSpaceIconDark;
  return (
    <Wrapper>
      <div className={styles.container}>
        <Image
          className={styles.img}
          src={WelcomePng}
          alt="welcome"
        />
        <div className={styles.desc}>
          {parser(t(Strings.dingtalk_member_contact_syncing_tips))}
        </div>
      </div>
    </Wrapper>
  );
};