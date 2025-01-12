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

import { useSetState, useUpdateEffect } from 'ahooks';
import { Tabs } from 'antd';
import * as React from 'react';
import { FC, useRef, useState } from 'react';
import { TextInput, Typography } from '@apitable/components';
import { AutoTestID, ConfigConstant, Strings, t } from '@apitable/core';
import { EmailFilled } from '@apitable/icons';
import { IPhoneInputRefProps, PasswordInput, PhoneInput, WithTipWrapper } from 'pc/components/common';
import { useFocusEffect } from 'pc/components/editors/hooks/use_focus_effect';
import { getEnvVariables } from 'pc/utils/env';
import { IdentifyingCodeModes, IIdentifyingCodeConfig } from '../../identifying_code_login/identifying_code_modes';
import styles from './style.module.less';

const { TabPane } = Tabs;

export interface IPasswordData {
  areaCode: string;
  account: string;
  credential: string;
}

export type IPasswordModes = ConfigConstant.LoginMode.PHONE | ConfigConstant.LoginMode.MAIL;
export type IPasswordLoginConfig = IIdentifyingCodeConfig;

export interface IPasswordLoginModesProps {
  defaultPasswordMode?: IPasswordModes;
  mode?: IdentifyingCodeModes;
  error?: { accountErrMsg: string, passwordErrMsg: string };
  onModeChange?: (mode: IPasswordModes) => void;
  onChange?: (data: IPasswordData) => void;
  config?: IPasswordLoginConfig;
}

const defaultState = {
  areaCode: '',
  account: '',
  credential: '',
};

export const PasswordLoginModes: FC<IPasswordLoginModesProps> = (
  {
    defaultPasswordMode = ConfigConstant.LoginMode.PHONE,
    onModeChange,
    onChange,
    error,
    config,
    mode,
  }
) => {
  const [defaultMode, setDefaultMode] = useState(defaultPasswordMode);
  const [state, setState] = useSetState<IPasswordData>(defaultState);
  const phoneInputRef = useRef<IPhoneInputRefProps>(null);
  const mailInputRef = useRef<any>(null);
  const { LOGIN_DEFAULT_ACCOUNT_TYPE } = getEnvVariables();
  useUpdateEffect(() => {
    onChange && onChange(state);
  }, [state]);
  useFocusEffect(() => {
    if (defaultMode === ConfigConstant.LoginMode.PHONE) {
      phoneInputRef.current?.focus();
    } else {
      mailInputRef.current?.focus();
    }
    // default mode change clear password input
    setState({ credential: '' });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [defaultMode]);

  const handleModeChange = (key: any) => {
    const defaultV = (config && config[key] && (config[key]?.defaultValue) || '');
    setState({ ...state, account: defaultV });
    setDefaultMode(key);
    localStorage.setItem('vika-preference-login-mode', key);
    onModeChange && onModeChange(key);
  };

  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/\s/g, '');
    setState({ account: value });
  };

  const handleEmailKeyPress = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === ' ') {
      event.preventDefault();
    }
  };

  const handlePhoneChange = (areaCode: string, phone: string) => {
    setState({ areaCode, account: phone });
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.trim();
    setState({ credential: value });
  };

  const accountErrTip = error?.accountErrMsg || '';
  const passwordErrTip = error?.passwordErrMsg || '';

  const LoginWithPhone = (
    <WithTipWrapper tip={accountErrTip}>
      <PhoneInput
        id={AutoTestID.LOGIN_PHONE_INPUT}
        ref={phoneInputRef}
        value={state.account}
        disabled={config?.phone?.disabled}
        placeholder={t(Strings.placeholder_input_mobile)}
        onChange={handlePhoneChange}
        error={Boolean(error?.accountErrMsg)}
        block
      />
    </WithTipWrapper>
  );

  const LoginWithEmail = (
    <WithTipWrapper tip={accountErrTip}>
      <TextInput
        id={AutoTestID.LOGIN_MAIL_INPUT}
        ref={mailInputRef}
        value={state.account}
        disabled={config?.mail?.disabled}
        prefix={<EmailFilled />}
        placeholder={t(Strings.email_placeholder)}
        onChange={handleEmailChange}
        onKeyPress={handleEmailKeyPress}
        error={Boolean(accountErrTip)}
        block
      />
    </WithTipWrapper>
  );

  const TabsContent = (
    <Tabs id={AutoTestID.LOGIN_CHANGE_MODE_TAB} className={styles.tabs} activeKey={defaultMode} onChange={handleModeChange}>
      {
        (!LOGIN_DEFAULT_ACCOUNT_TYPE || LOGIN_DEFAULT_ACCOUNT_TYPE.includes('phone')) &&
        <TabPane tab={t(Strings.phone_number)} key={ConfigConstant.LoginMode.PHONE}>
          {LoginWithPhone}
        </TabPane>
      }
      {
        (!LOGIN_DEFAULT_ACCOUNT_TYPE || LOGIN_DEFAULT_ACCOUNT_TYPE.includes('phone')) &&
        <TabPane tab={t(Strings.mail)} key={ConfigConstant.LoginMode.MAIL}>
          {LoginWithEmail}
        </TabPane>
      }
    </Tabs>
  );

  return (
    <div className={styles.passwordLoginModes}>
      {mode && !mode.includes(',') ? (mode === ConfigConstant.LoginMode.PHONE ? (
        LoginWithPhone
      ) : (
        LoginWithEmail
      )) : (
        TabsContent
      )}
      <Typography variant='body2' className={styles.label}>{t(Strings.password)}</Typography>
      <WithTipWrapper tip={passwordErrTip}>
        <PasswordInput
          id={AutoTestID.LOGIN_PASSWORD_INPUT}
          placeholder={t(Strings.placeholder_input_password)}
          onChange={handlePasswordChange}
          error={Boolean(passwordErrTip)}
          value={state.credential}
          block
        />
      </WithTipWrapper>
    </div>
  );
};
