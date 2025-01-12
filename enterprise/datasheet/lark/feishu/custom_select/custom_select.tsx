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

import { Select } from 'antd';
import { SelectValue, SelectProps } from 'antd/lib/select';
import classNames from 'classnames';
import { useCallback } from 'react';
import * as React from 'react';
import { useThemeColors } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import { ChevronDownOutlined } from '@apitable/icons';
import styles from './style.module.less';
const { Option } = Select;

interface ICustomSelect extends SelectProps<any> {
  optionData?: any[];
  children?: React.ReactNode;
}

const CustomSelectBase: React.FC<ICustomSelect> = ({ optionData, className, ...rest }) => {
  const colors = useThemeColors();
  const onChange = useCallback(
    (value: SelectValue, options?: any) => {
      rest.onChange && rest.onChange(value, options);
    },
    [rest],
  );

  return (
    <Select
      className={classNames(styles.customSelect, className)}
      onChange={onChange}
      dropdownClassName={styles.dropdown}
      notFoundContent={t(Strings.no_option)}
      suffixIcon={<ChevronDownOutlined size={16} color={colors.fourthLevelText} />}
      {...rest}
    >
      {rest.children ? rest.children : optionData && optionData.map(item => {
        return (
          <Option value={item.value} key={item.value}>{item.label}</Option>
        );
      })}
    </Select>
  );
};

export const CustomSelect = React.memo(CustomSelectBase);
