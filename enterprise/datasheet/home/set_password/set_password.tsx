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

import { Form } from 'antd';
import { FC, useState } from 'react';
import * as React from 'react';
import { Button } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import { CheckCircleFilled, UncheckedOutlined } from '@apitable/icons';
import { PasswordInput } from 'pc/components/common';
import styles from './style.module.less';

interface ISetPassword {
  apiCb(
    // e: React.FormEvent<HTMLFormElement>,
    password: string,
    setLoading: React.Dispatch<React.SetStateAction<boolean>>,
    setPwdErr: React.Dispatch<React.SetStateAction<string>>,
  ): void;
}

export const SetPassword: FC<ISetPassword> = props => {
  const [form, setForm] = useState({
    password: '',
    firstPwd: '',
    isCheck: false,
  });
  const [isCheck, setIsCheck] = useState(false);
  const [loading, setLoading] = useState(false);
  const [, setPwdErr] = useState('');

  const handleSubmit = () => {
    if (form.firstPwd !== form.password) {
      setPwdErr(t(Strings.password_not_identical_err));
      return;
    }
    props.apiCb(form.password, setLoading, setPwdErr);
    setLoading(true);
  };
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>, property: 'firstPwd' | 'password') => {
    setForm({
      ...form,
      [property]: e.target.value,
    });
  };
  return (
    <div className={styles.setPasswordWrapper}>
      <h2>{t(Strings.label_set_password)}</h2>
      <Form>
        <PasswordInput
          value={form.firstPwd}
          onChange={e => { handleChange(e, 'firstPwd'); }}
          placeholder={t(Strings.placeholder_set_password)}
          autoComplete="Off"
        />
        <PasswordInput
          value={form.password}
          onChange={e => { handleChange(e, 'password'); }}
          placeholder={t(Strings.placeholder_input_password_again)}
          autoComplete="Off"
        // err={pwdErr}
        />
        <div className={styles.protocol}>
          <div
            className={styles.checkBox}
            onClick={() => setIsCheck(!isCheck)}
          >
            {isCheck ? <CheckCircleFilled className={styles.checkBoxSelect} /> : <UncheckedOutlined />}
          </div>
          <div className={styles.protocolText}>
            {t(Strings.read_agree_agreement, {
              Agreement1: <a href="/agreement/register">{t(Strings.registration_service_agreement)}</a>,
              Agreement2: <a href="/agreement/private">{t(Strings.privacy_protection)}</a>,
            })}
          </div>
        </div>
        <Button
          color="primary"
          htmlType="submit"
          disabled={!isCheck || form.firstPwd === '' || form.password === ''}
          loading={loading}
          onClick={() => {
            handleSubmit();
          }}
          block
        >
          确认加入
        </Button>
      </Form>
    </div>
  );
};
