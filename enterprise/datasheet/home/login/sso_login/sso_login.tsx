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
import { Form } from 'antd';
import { FC, useRef } from 'react';
import * as React from 'react';
import { Typography, Button, TextInput } from '@apitable/components';
import { Strings, t, StatusCode, ConfigConstant } from '@apitable/core';
import { UserOutlined } from '@apitable/icons';
import { PasswordInput, WithTipWrapper } from 'pc/components/common';
import { useRequest } from 'pc/hooks';
import { ISubmitRequestParam } from '../identifying_code_login';
import { IIdentifyingCodeConfig } from '../identifying_code_login/identifying_code_modes';
import styles from './style.module.less';
export interface IPasswordData {
  areaCode: string;
  account: string;
  credential: string;
}
const defaultErrMsg = { accountErrMsg: '', passwordErrMsg: '' };
export type IPasswordLoginConfig = IIdentifyingCodeConfig;
export interface IPasswordLoginModesProps {
  submitRequest: (data: ISubmitRequestParam) => Promise<any>;
  onChange?: (data: IPasswordData) => void;
  config?: IPasswordLoginConfig;
}

const defaultState = {
  areaCode: '',
  account: '',
  credential: '',
};

export const SSOLogin: FC<IPasswordLoginModesProps> = ({
  submitRequest,
  onChange,
  config
}) => {
  const [state, setState] = useSetState<IPasswordData>(defaultState);
  const [errMsg, setErrMsg] = useSetState(defaultErrMsg);

  const accountInputRef = useRef<any>(null);
  const { run: login, loading } = useRequest(submitRequest, { manual: true });

  useUpdateEffect(() => {
    onChange && onChange(state);
  }, [state]);

  const handleAccountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (errMsg.accountErrMsg || errMsg.passwordErrMsg) {
      setErrMsg(defaultErrMsg);
    }
    const value = e.target.value.trim();
    setState({ account: value });
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (errMsg.accountErrMsg || errMsg.passwordErrMsg) {
      setErrMsg(defaultErrMsg);
    }
    const value = e.target.value.trim();
    setState({ credential: value });
  };

  const handleSubmit = async () => {
    const loginData = {
      areaCode: '',
      account: state.account,
      credential: state.credential,
      type: ConfigConstant.LoginTypes.SSO_AUTH,
    };
    const result = await login(loginData);
    if (!result) {
      return;
    }
    const { code, message, success } = result;
    if (success) {
      return;
    }

    switch (code) {
      case StatusCode.NAME_AND_PWD_ERR:
        setErrMsg({ accountErrMsg: message, passwordErrMsg: message });
        break;
      case StatusCode.PASSWORD_ERR:
        setErrMsg({ passwordErrMsg: message });
        break;
      default:
        setErrMsg({ accountErrMsg: message });
    }
  };
  const accountErrTip = errMsg?.accountErrMsg || '';
  const passwordErrTip = errMsg?.passwordErrMsg || '';
  const btnDisabled = Boolean(loading || errMsg.accountErrMsg || errMsg.passwordErrMsg || !state.account || !state.credential);
  return (
    <div className={styles.ssoLogin}>
      <Form onFinish={handleSubmit}>
        <Typography variant="body2" className={styles.label}>{t(Strings.sso_account)}</Typography>
        <WithTipWrapper tip={accountErrTip}>
          <TextInput
            ref={accountInputRef}
            value={state.account}
            prefix={<UserOutlined />}
            disabled={config?.phone?.disabled}
            placeholder={t(Strings.placeholder_input_sso_account)}
            onChange={handleAccountChange}
            error={Boolean(errMsg?.accountErrMsg)}
            block
          />
        </WithTipWrapper>
        <Typography variant="body2" className={styles.label}>{t(Strings.sso_password)}</Typography>
        <WithTipWrapper tip={passwordErrTip}>
          <PasswordInput
            placeholder={t(Strings.placeholder_input_password)}
            onChange={handlePasswordChange}
            error={Boolean(passwordErrTip)}
            block
          />
        </WithTipWrapper>
        <Button
          type="submit"
          className={styles.loginButton}
          color="primary"
          size="large"
          loading={loading}
          disabled={btnDisabled}
          block
        >
          {t(Strings.login)}
        </Button>
      </Form>
      
    </div>
  );
};
