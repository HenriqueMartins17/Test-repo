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

import { useUpdateEffect } from 'ahooks';
import { Tabs } from 'antd';
import { FC, useEffect, useRef, useState } from 'react';
import * as React from 'react';
import { TextInput, Typography } from '@apitable/components';
import { AutoTestID, ConfigConstant, Strings, t } from '@apitable/core';
import { EmailFilled } from '@apitable/icons';
import {
  IdentifyingCodeInput,
  IPhoneInputRefProps,
  PhoneInput,
  WithTipWrapper,
} from 'pc/components/common/input';
import { useFocusEffect } from 'pc/components/editors/hooks/use_focus_effect';
import { useSetState } from 'pc/hooks';
import { getEnvVariables } from 'pc/utils/env';
import styles from './style.module.less';

const { TabPane } = Tabs;

export type IdentifyingCodeModes = ConfigConstant.LoginMode.PHONE
  | ConfigConstant.LoginMode.MAIL;

export interface IIdentifyingCodeData {
  areaCode: string;
  account: string;
  credential: string;
}

export type IIdentifyingCodeConfig = {
  [key in IdentifyingCodeModes]?: {
    defaultValue?: string,
    disabled?: boolean,
  }
};

export interface IIdentifyingCodeModesProps {
  config?: IIdentifyingCodeConfig;
  // Only a certain mode is displayed
  mode?: IdentifyingCodeModes;
  // Type of text message
  smsType: ConfigConstant.SmsTypes;
  // Type of email sent
  emailType?: ConfigConstant.EmailCodeType;
  // Default captcha method (default is mobile captcha login)
  defaultIdentifyingCodeMode?: IdentifyingCodeModes;
  // Error reporting from outside interfaces (related to account or authentication)
  error?: { accountErrMsg: string; identifyingCodeErrMsg: string };
  // Triggered when captcha mode changes
  onModeChange?: (mode: IdentifyingCodeModes) => void;
  // Calibrate mobile or email
  checkAccount?: () => boolean;
  // Triggered when the data in the input box changes
  onChange?: (data: IIdentifyingCodeData) => void;
}

export const IdentifyingCodeModes: FC<IIdentifyingCodeModesProps> = (
  {
    config,
    mode,
    smsType,
    emailType = ConfigConstant.EmailCodeType.REGISTER,
    onModeChange,
    defaultIdentifyingCodeMode = ConfigConstant.LoginMode.PHONE,
    error,
    onChange,
    checkAccount,
  }
) => {
  // Current method of obtaining the verification code
  const [identifyingCodeMode, setIdentifyingCodeMode] = useState(mode || defaultIdentifyingCodeMode);
  const { LOGIN_DEFAULT_ACCOUNT_TYPE } = getEnvVariables();
  const defaultPhone = config?.phone?.defaultValue;
  const defaultMail = config?.mail?.defaultValue;
  // Primary data (account number, verification code, password, secondary confirmation password)
  const [state, setState] = useSetState<IIdentifyingCodeData>({
    areaCode: '+86',
    account: (mode === ConfigConstant.LoginMode.PHONE ? defaultPhone : defaultMail) || '',
    credential: ''
  });

  useEffect(() => {
    setState({
      account: (mode === ConfigConstant.LoginMode.PHONE ? defaultPhone : defaultMail) || '',
    });
  }, [defaultPhone, defaultMail, setState, mode]);

  const [errMsg, setErrMsg] = useSetState<{
    accountErrMsg: string;
    identifyingCodeErrMsg: string;
  }>({
    accountErrMsg: '',
    identifyingCodeErrMsg: '',
  });
  const phoneInputRef = useRef<IPhoneInputRefProps>(null);
  const mailInputRef = useRef<any>(null);

  useFocusEffect(() => {
    if (identifyingCodeMode === ConfigConstant.LoginMode.PHONE) {
      phoneInputRef.current?.focus();
      return;
    }
    mailInputRef.current?.focus();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [identifyingCodeMode]);

  useUpdateEffect(() => {
    onChange && onChange(state);
  }, [state]);

  useUpdateEffect(() => {
    if (error) {
      setErrMsg(error);
    }
  }, [error]);

  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.trim();
    setState({ account: value });
  };

  // Change of mobile phone number or area code
  const handlePhoneChange = (areaCode: string, phone: string) => {
    if (errMsg.accountErrMsg) {
      setErrMsg({ accountErrMsg: '' });
    }
    setState({ areaCode, account: phone });
  };

  // Toggle between email verification code login and mobile verification code login
  const handleModeChange = (key: any) => {
    const defaultV = (config && config[key] && (config[key]?.defaultValue) || '');
    setState({ ...state, account: defaultV });
    setIdentifyingCodeMode(key);
    localStorage.setItem('vika-preference-login-mode', key);
    onModeChange && onModeChange(key);
  };

  const handleIdentifyingCodeChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    if (errMsg.identifyingCodeErrMsg) {
      setErrMsg({ identifyingCodeErrMsg: '' });
    }

    const value = e.target.value;
    const newValue = value.replace(/\s/g, '');
    console.log('value--->', newValue);
    setState({ credential: newValue });
  };
  return (
    <div className={styles.identifyingCodeModes}>
      {mode ? (
        mode === ConfigConstant.LoginMode.PHONE ? (
          <>
            <Typography variant='body2' className={styles.label}>
              {t(Strings.phone_number)}
            </Typography>
            <WithTipWrapper tip={errMsg.accountErrMsg}>
              <PhoneInput
                id={AutoTestID.LOGIN_PHONE_INPUT}
                value={state.account}
                disabled={config?.phone?.disabled}
                ref={phoneInputRef}
                onChange={handlePhoneChange}
                error={Boolean(errMsg.accountErrMsg)}
                block
              />
            </WithTipWrapper>
          </>
        ) : (
          <>
            <Typography variant='body2' className={styles.label}>
              {t(Strings.mail)}
            </Typography>
            <WithTipWrapper tip={errMsg.accountErrMsg}>
              <TextInput
                value={state.account}
                disabled={config?.mail?.disabled}
                ref={mailInputRef}
                placeholder={t(Strings.email_placeholder)}
                prefix={<EmailFilled />}
                onChange={handleEmailChange}
                error={Boolean(errMsg.accountErrMsg)}
                block
              />
            </WithTipWrapper>
          </>
        )
      ) :
        <Tabs id={AutoTestID.LOGIN_CHANGE_MODE_TAB} className={styles.tabs} activeKey={identifyingCodeMode} onChange={handleModeChange}>
          {
            (!LOGIN_DEFAULT_ACCOUNT_TYPE || LOGIN_DEFAULT_ACCOUNT_TYPE?.includes('phone')) &&
            <TabPane tab={t(Strings.phone_number)} key={ConfigConstant.LoginMode.PHONE}>
              <WithTipWrapper tip={errMsg.accountErrMsg}>
                <PhoneInput
                  id={AutoTestID.LOGIN_PHONE_INPUT}
                  value={state.account}
                  disabled={config?.phone?.disabled}
                  ref={phoneInputRef}
                  onChange={handlePhoneChange}
                  error={Boolean(errMsg.accountErrMsg)}
                  block
                />
              </WithTipWrapper>
            </TabPane>
          }
          {
            (!LOGIN_DEFAULT_ACCOUNT_TYPE || LOGIN_DEFAULT_ACCOUNT_TYPE?.includes('mail')) &&
            <TabPane tab={t(Strings.mail)} key={ConfigConstant.LoginMode.MAIL}>
              <WithTipWrapper tip={errMsg.accountErrMsg}>
                <TextInput
                  id={AutoTestID.LOGIN_MAIL_INPUT}
                  value={state.account}
                  disabled={config?.mail?.disabled}
                  ref={mailInputRef}
                  placeholder={t(Strings.email_placeholder)}
                  prefix={<EmailFilled />}
                  onChange={handleEmailChange}
                  error={Boolean(errMsg.accountErrMsg)}
                  block
                />
              </WithTipWrapper>
            </TabPane>
          }

        </Tabs>
      }
      <Typography variant='body2' className={styles.label}>
        {t(Strings.verification_code)}
      </Typography>
      <WithTipWrapper tip={errMsg.identifyingCodeErrMsg} captchaVisible>
        <IdentifyingCodeInput
          id={AutoTestID.IDENTIFYING_CODE_INPUT}
          mode={identifyingCodeMode}
          data={{ areaCode: state.areaCode, account: state.account }}
          smsType={smsType}
          emailType={emailType}
          onChange={handleIdentifyingCodeChange}
          setErrMsg={setErrMsg}
          error={Boolean(errMsg.identifyingCodeErrMsg)}
          checkAccount={checkAccount}
          value={state.credential}
        />
      </WithTipWrapper>
    </div>
  );
};
