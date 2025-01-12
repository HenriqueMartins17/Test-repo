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

import { useToggle } from 'ahooks';
import * as React from 'react';
import { useMemo } from 'react';
import { lightColors, LinkButton } from '@apitable/components';
import { isPrivateDeployment, Strings, t, ConfigConstant } from '@apitable/core';
import { QrcodeOutlined, WorkbenchOutlined } from '@apitable/icons';
import { isRenderServer } from 'pc/utils';
import { getEnvVariables } from 'pc/utils/env';
import { ScanLogin } from '../../login';
import { polyfillMode } from '../login';
import { PhoneAndEmailLogin } from '../login/phone_and_email_login';
import styles from './style.module.less';

const initToggle = (): boolean => {
  const preference = !process.env.SSR && localStorage.getItem('vika-login-preference');

  if (!preference) return true;

  return preference === 'scan' && !isPrivateDeployment();
};

const getScanLoginValue = () => {
  const { LOGIN_DEFAULT_ACCOUNT_TYPE } = getEnvVariables();
  if (!LOGIN_DEFAULT_ACCOUNT_TYPE) {
    return false;
  }
  if (LOGIN_DEFAULT_ACCOUNT_TYPE.split(',')[0] === 'qrcode') {
    return true;
  }
  if (!LOGIN_DEFAULT_ACCOUNT_TYPE.includes('qrcode')) {
    return false;
  }
  return initToggle();
};

export const LoginToggle = (): JSX.Element => {
  const { LOGIN_DEFAULT_ACCOUNT_TYPE, LOGIN_SSO_VISIBLE, LOGIN_DEFAULT_VERIFY_TYPE } = getEnvVariables();
  const [isScanLogin, { toggle }] = useToggle(getScanLoginValue());
  const onToggle = () => {
    localStorage.setItem('vika-login-preference', isScanLogin ? 'phone-and-email' : 'scan');

    toggle();
  };

  const lastVerifyMode = polyfillMode(localStorage.getItem('vika_login_mod'));
  const commonDefaultMod = lastVerifyMode && LOGIN_DEFAULT_VERIFY_TYPE?.includes(lastVerifyMode) ?
    lastVerifyMode : LOGIN_DEFAULT_VERIFY_TYPE?.split(',')[0];
  const defaultMod = LOGIN_SSO_VISIBLE ? ConfigConstant.SSO_LOGIN : commonDefaultMod;
  const [mod, setMod] = React.useState(defaultMod);

  const modTitleText = useMemo(() => {
    let title = '';
    switch (mod) {
      case ConfigConstant.IDENTIFY_CODE_LOGIN:
        title = t(Strings.verification_code_login);
        break;
      case ConfigConstant.PASSWORD_LOGIN:
        title = t(Strings.password_login);
        break;
      case ConfigConstant.SSO_LOGIN:
        title = t(Strings.sso_login);
        break;
      default:
        break;
    }
    return title;
  }, [mod]);

  return (
    <>
      <div onClick={() => onToggle()} className={styles.toggler}>
        {
          !isRenderServer() && <div className={styles.toggleTextWrapper}>
            {isScanLogin ? (
              (!LOGIN_DEFAULT_ACCOUNT_TYPE || LOGIN_DEFAULT_ACCOUNT_TYPE.includes('mail') || LOGIN_DEFAULT_ACCOUNT_TYPE.includes('phone')) &&
              <LinkButton
                underline={false}
                component='button'
                prefixIcon={<WorkbenchOutlined color={lightColors.deepPurple[500]} />}
                color={lightColors.deepPurple[500]}
                style={{ padding: 0 }}
              >
                {t(Strings.phone_email_login)}
              </LinkButton>
            ) : (
              (!LOGIN_DEFAULT_ACCOUNT_TYPE || LOGIN_DEFAULT_ACCOUNT_TYPE.includes('qrcode')) && <LinkButton
                underline={false}
                component='button'
                prefixIcon={<QrcodeOutlined color={lightColors.deepPurple[500]} />}
                color={lightColors.deepPurple[500]}
                style={{ padding: 0 }}
              >
                {t(Strings.scan_to_login)}
              </LinkButton>
            )}
          </div>
        }
      </div>
      <div className={styles.toggleTitle}>{!isScanLogin ? modTitleText : t(Strings.scan_to_login)}</div>
      {isScanLogin ? <ScanLogin /> : (
        <div className={styles.phoneAndEmailContainer}>
          <PhoneAndEmailLogin mod={mod} setMod={setMod} />
        </div>
      )}
    </>
  );
};
