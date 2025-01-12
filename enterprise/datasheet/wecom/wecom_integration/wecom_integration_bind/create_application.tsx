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
import { useEffect, useState } from 'react';
import * as React from 'react';
import { Button, Message, useThemeColors } from '@apitable/components';
// eslint-disable-next-line no-restricted-imports
import { Strings, t, Api } from '@apitable/core';
import { CopyOutlined } from '@apitable/icons';
// eslint-disable-next-line no-restricted-imports
import { Tooltip } from 'pc/components/common';
import { copy2clipBoard } from 'pc/utils';
import { FormItem, IFormItem } from '../components/form_item';
import styles from './styles.module.less';

export interface IConfigForm {
  corpId: string;
  agentId: string;
  agentSecret: string;
}

interface ICreateApplicationProps {
  nextStep: () => void;
  scrollToTop: () => void;
  setConfig: (corpId: string, agentId: string, configSha: any, domainName: string) => void
}

export const CopyButton = (value?: string) => {
  const colors = useThemeColors();
  return (
    <Tooltip title={t(Strings.copy_link)} placement="top">
      <Button
        className={styles.iconButton}
        color={colors.fc6}
        onClick={() => {
          copy2clipBoard(value!);
        }}
      ><CopyOutlined className={styles.buttonIcon} color={colors.secondLevelText} /></Button>
    </Tooltip>
  );
};

export const CreateApplication: React.FC<ICreateApplicationProps> = (props) => {
  const { nextStep, scrollToTop, setConfig } = props;
  const [isValidForm, setIsValidForm] = useState(false);
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState<IConfigForm>({
    corpId: '',
    agentId: '',
    agentSecret: '',
  });
  const [formError, setFormError] = useState({
    corpId: '',
    agentId: '',
    agentSecret: ''
  });

  const handleChange = (e: any, item: IFormItem) => {
    const value = e.target.value;
    if (value.length > 0) {
      setFormError({ ...formError, [item.key]: '' });
    }
    const form = {
      ...formData,
      [item.key]: e.target.value
    };
    setFormData(form);
  };

  useEffect(() => {
    setIsValidForm(Object.keys(formData).every(key => formData[key]));
  }, [formData]);

  const schema1 = {
    corpId: {
      label: t(Strings.integration_app_wecom_form1_item1_label),
      pattern: /^\S*$/,
      required: true
    },
    agentId: {
      label: t(Strings.integration_app_wecom_form1_item2_label),
      pattern: /^[0-9]*$/,
      required: true
    },
    agentSecret: {
      label: t(Strings.integration_app_wecom_form1_item3_label),
      pattern: /^\S*$/,
      required: true
    }
  };

  const setError = () => {
    const error = {};
    Object.keys(schema1).forEach(key => {
      error[key] = `${t(Strings.please_check)} ${schema1[key].label}`;
    });
    setFormError({ ...formError, ...error });
  };

  const validator = (properties: any) => {
    return Object.keys(properties).every(key => {
      const property = properties[key];
      const value = formData[key];
      if (property.required && !(value && property.pattern.test(value))) {
        setError();
        return false;
      }
      return true;
    });
  };

  const setWecomConfig = async (formData: any) => {
    setLoading(true);
    const { data: { data, success, message, code } } = await Api.socialWecomCheckConfig(formData);
    setLoading(false);
    if (success) {
      nextStep();
      setConfig(formData.corpId, formData.agentId, data?.configSha, data?.domainName);
      return;
    }
    if ([40001, 40013, 301002].includes(code)) {
      const error = {};
      Object.keys(schema1).forEach(key => {
        error[key] = `${t(Strings.please_check)} ${schema1[key].label}`;
      });
      setFormError({ ...formError, ...error });
      return;
    }
    Message.error({ content: message });
  };

  const onClick = () => {
    if (!validator({ ...schema1 })) {
      scrollToTop();
      return;
    }
    setWecomConfig(formData);
  };

  return (
    <div className={classNames(
      styles.createApplication,
      styles.formPage
    )}>
      <div className={styles.formWrap}>
        <div className={styles.form}>
          <div className={styles.formTitle}>{t(Strings.integration_app_wecom_form1_title)}</div>
          <div
            className={styles.formDesc}
            dangerouslySetInnerHTML={{ __html: t(Strings.integration_app_wecom_form1_desc) }}
          />
          <div className={styles.formContent}>
            {
              Object.keys(schema1).map(key => (
                <FormItem key={key} formData={formData} formItem={{ ...schema1[key], key }} error={formError[key]} onChange={handleChange} />
              ))
            }
          </div>
        </div>
      </div>
      <div className={styles.buttonWrap}>
        <Button color="primary" onClick={onClick} disabled={!isValidForm} block loading={loading}>{t(Strings.next_step)}</Button>
      </div>
    </div>
  );
};
