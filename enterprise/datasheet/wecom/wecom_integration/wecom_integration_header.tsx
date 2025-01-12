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
import { Space, ThemeName } from '@apitable/components';
import { IUserInfo, Strings, t } from '@apitable/core';
import { Avatar, AvatarSize, Logo } from 'pc/components/common';
import { getEnvVariables } from 'pc/utils/env';
import styles from './styles.module.less';

export const WecomIntegrationHeader: React.FC<{userInfo: IUserInfo}> = (props) => {
  const { userInfo: user } = props;
  const renderAvatar = (style?: React.CSSProperties) => {
    if (!user) {
      return;
    }
    return (
      <Space>
        <Avatar
          id={user.memberId}
          src={user.avatar}
          title={user.nickName}
          size={AvatarSize.Size24}
          className={styles.avatorImg}
          style={style}
        />
        <div>{user.nickName}</div>
      </Space>
    );
  };

  return (
    <div className={styles.wecomIntegrationHeader}>
      <div className={styles.headerContainer}>
        <div className={styles.headerLeft}>
          <Logo className={styles.logo} theme={ThemeName.Dark} />
          <div className={styles.desc}>{t(Strings.space_manage_menu_wecom)}</div>
        </div>
        <div className={styles.headerRight}>
          <a
            target="_blank"
            className={styles.helpItem}
            href={getEnvVariables().JOIN_CHATGROUP_PAGE_URL} rel="noreferrer"
          >{t(Strings.help_center)}</a>
          {renderAvatar()}
        </div>
      </div>
    </div>
  );
};
