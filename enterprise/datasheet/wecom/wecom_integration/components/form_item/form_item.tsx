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

import classnames from 'classnames';
import * as React from 'react';
import { TextInput } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import styles from './styles.module.less';

export interface IFormItem {
  label: string;
  key: string;
  placeholder?: string;
  readonly?: boolean;
  required?: boolean;
  suffix?: JSX.Element;
}

export interface IFormItemProps {
  formItem: IFormItem;
  formData: {[key: string]: any};
  error?: boolean | undefined;
  onChange?: (e: any, formItem: IFormItem) => void;
}

export const FormItem: React.FC<IFormItemProps> = (props) => {

  const {
    formItem,
    formData,
    error,
    onChange
  } = props;
  const defaultPlaceholder= t(Strings.placeholder_enter_here);

  return (
    <div className={styles.formItem}>
      <div className={classnames(
        styles.formItemLabel,
        formItem?.required && styles.formItemLabelRequired
      )}>{formItem.label}</div>
      <div className={classnames(
        styles.formItemValue,
        formItem?.suffix && styles.formItemValueSuffix
      )}>
        <TextInput
          className={classnames(
            formItem.readonly && styles.formItemInputReadOnly
          )}
          value={formData[formItem.key]}
          placeholder={formItem.placeholder || defaultPlaceholder}
          readOnly={formItem.readonly}
          onChange={(e) => onChange && onChange(e, formItem)}
          error={error}
          block
        />
        {formItem?.suffix && <div className={styles.formItemSuffix}>{formItem.suffix}</div>}
      </div>
      <div className={styles.formItemError}>{error}</div>
    </div>
  );
};
