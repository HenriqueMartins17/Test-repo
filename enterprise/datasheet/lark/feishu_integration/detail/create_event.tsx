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

import classNames from 'classnames';
import * as React from 'react';
import { useCallback, useEffect, useState } from 'react';
import { Button, Message } from '@apitable/components';
import { Strings, t, Api } from '@apitable/core';
import { IFeishuConfigParams } from '../interface';
// @ts-ignore
import { FormItem as WecomFormItem, IFormItem as IWecomFormItem } from 'enterprise/wecom/wecom_integration/components/form_item/form_item';
import styles from './styles.module.less';

export interface IConfigForm {
  encryptKey: string;
  verificationToken: string;
}

interface ICreateEvent {
  nextStep: () => void;
  appInstanceId: string;
  onSetConfig: (result: any) => void;
  config: IFeishuConfigParams;
}

const schema1 = {
  encryptKey: {
    label: t(Strings.lark_integration_step4_encryptkey),
  },
  verificationToken: {
    label: t(Strings.lark_integration_step4_verificationtoken),
    required: true,
  },
};

export const CreateEvent: React.FC<ICreateEvent> = props => {
  const { nextStep, onSetConfig, appInstanceId, config } = props;
  const [isValidForm, setIsValidForm] = useState(false);
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState<IConfigForm>({
    encryptKey: config.encryptKey,
    verificationToken: config.verificationToken,
  });
  const [formError, setFormError] = useState({
    encryptKey: '',
    verificationToken: '',
  });

  const setError = () => {
    const error = {};
    Object.keys(schema1).forEach(key => {
      if (schema1[key].required) {
        error[key] = `${t(Strings.please_check)} ${schema1[key].label}`;
      }
    });
    setFormError(val => ({ ...val, ...error }));
  };

  const validator = useCallback((properties: any) => {
    return Object.keys(schema1).every(key => {
      const property = schema1[key];
      const value = properties[key];
      if (property.required && !value) {
        setError();
        return false;
      }
      return true;
    });
  }, []);

  const handleChange = useCallback(
    (e: any, item: IWecomFormItem) => {
      const value = e.target.value;
      if (value.length > 0) {
        setFormError(val => ({ ...val, [item.key]: '' }));
      }
      const form = { ...formData, [item.key]: e.target.value };
      setFormData(form);
      setIsValidForm(validator(form));
    },
    [formData, validator],
  );

  const setLarkConfig = useCallback(
    async (formData: IConfigForm) => {
      setLoading(true);
      const { data: result } = await Api.updateLarkEventConfig(
        appInstanceId,
        formData.encryptKey ? formData.encryptKey.trim() : '',
        formData.verificationToken.trim(),
      );
      const { data, success, message } = result;
      setLoading(false);
      if (success) {
        nextStep();
        onSetConfig(data.config.profile);
        return;
      }
      const error = {};
      Object.keys(schema1).forEach(key => {
        error[key] = `${t(Strings.please_check)} ${schema1[key].label}`;
      });
      setFormError(val => ({ ...val, ...error }));
      Message.error({ content: message });
    },
    [setFormError, appInstanceId, nextStep, onSetConfig],
  );

  const onClick = useCallback(() => {
    if (!validator(formData)) {
      return;
    }
    setLarkConfig(formData);
  }, [formData, setLarkConfig, validator]);

  useEffect(() => {
    if (config.verificationToken) {
      setIsValidForm(validator(config));
    }
  }, [config, validator]);

  return (
    <div className={classNames(styles.createApplication, styles.formPage)}>
      <div className={styles.formWrap}>
        <div className={styles.form}>
          <div className={styles.formTitle}>{t(Strings.lark_integration_step4_title)}</div>
          <div className={styles.formDesc} dangerouslySetInnerHTML={{ __html: t(Strings.lark_integration_step4_content) }} />
          <div className={styles.formContent}>
            {Object.keys(schema1).map(key => (
              <WecomFormItem key={key} formData={formData} formItem={{ ...schema1[key], key }} error={formError[key]} onChange={handleChange} />
            ))}
          </div>
        </div>
      </div>
      <div className={styles.buttonWrap}>
        <Button color="primary" onClick={onClick} disabled={!isValidForm} block loading={loading}>
          {t(Strings.lark_integration_step4_next)}
        </Button>
      </div>
    </div>
  );
};
