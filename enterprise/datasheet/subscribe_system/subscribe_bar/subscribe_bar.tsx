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

import { useSelector } from 'react-redux';
import { Avatar, LinkButton, Typography, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { Logo } from 'pc/components/common';
import { showOrderContactUs } from '../order_modal/pay_order_success';
import styles from '../styles.module.less';

export const SubscribeBar = () => {
  const colors = useThemeColors();
  const userInfo = useSelector(state => state.user.info);

  return <div className={styles.navBar}>
    <div style={{ margin: '0 auto', width: 976 }} className={styles.navBarInner}>
      <div className={styles.logo} onClick={() => location.href = '/workbench'}>
        {/* The color values here are fixed according to the requirements of the design, regardless of theme switching */}
        <Logo size="small" />
      </div>
      <div style={{ flex: 1 }} />
      <Typography variant={'body2'} style={{ marginRight: 32 }}>
        <LinkButton underline={false} onClick={showOrderContactUs} color={colors.fc1}>
          {t(Strings.contact_us)}
        </LinkButton>
      </Typography>
      <Avatar src={userInfo?.avatar || undefined} size={'xs'}>
        {userInfo?.nickName[0]}
      </Avatar>
      <Typography variant={'body2'} style={{ marginLeft: 8 }}>
        {userInfo?.memberName}
      </Typography>
    </div>
  </div>;
};
