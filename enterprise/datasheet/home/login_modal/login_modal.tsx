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
import { ConfigConstant } from '@apitable/core';
import { BaseModal, Logo } from 'pc/components/common';
import { Login } from '../login';
import styles from './style.module.less';

export interface ILoginModalProps {
  afterLogin?: (data: string, loginMode: ConfigConstant.LoginMode) => void;
  onCancel: () => void;
}

export const LoginModal: FC<ILoginModalProps> = props => {
  const { afterLogin } = props;

  const onCancel = () => {
    props.onCancel();
  };

  return (
    <BaseModal
      onCancel={onCancel}
      showButton={false}
    >
      <div className={styles.loginModal}>
        <div className={styles.logo}>
          <Logo size="large" />
        </div>
        <Login afterLogin={afterLogin} />
      </div>
    </BaseModal>
  );
};
