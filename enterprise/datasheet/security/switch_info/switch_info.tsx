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

import { Radio, Tooltip } from 'antd';
import classNames from 'classnames';
import React, { FC } from 'react';
import { ISwitchProps, Switch } from '@apitable/components';
import { IPermissionInfo, PermissionType } from '../security';
// @ts-ignore
import { SubscribeGrade, SubscribeLabel } from 'enterprise/subscribe_system/subscribe_label/subscribe_label';
import styles from './style.module.less';

interface ISwitchInfoProps extends ISwitchProps {
  switchText: string;
  tipContent: string;
  loading?: boolean;
  grade?: SubscribeGrade;
  permissionType?: PermissionType;
  permissionList?: IPermissionInfo[];
  onClick?: (value: any) => void;
}

export const SwitchInfo: FC<ISwitchInfoProps> = props => {
  const { tipContent, switchText, loading = false, style, grade, permissionType = PermissionType.Readable, permissionList = [], ...rest } = props;
  const arr = tipContent.split('；');
  const checked = props.checked;
  return (
    <div className={styles.switchInfo} style={style}>
      <div className={styles.switchInfoTop}>
        <Switch
          {...rest}
          size="small"
          loading={loading}
        />
        <span className={styles.switchText}>{switchText}</span>
        {
          grade && <SubscribeLabel grade={grade} />
        }
      </div>
      <div>
        <ul>
          {arr.map(item => <li key={item}>{item}</li>)}
        </ul>
      </div>
      {
        Boolean(permissionList.length) &&
        <div className={classNames(styles.radioGroup, !checked && styles.radioGroupDisabled)}>
          <Radio.Group
            name="inline"
            onChange={(e) => props.onClick?.(e.target.value)}
            value={String(permissionType)}
            disabled={!checked}
          >
            {
              permissionList.map(item => {
                const { name, value, disableTip } = item;
                const isCurrent = String(value) === String(permissionType);
                const radioComponent = (
                  <Radio key={value} value={String(value)}>
                    <span
                      className={classNames({
                        [styles.radioText]: checked,
                        [styles.radioTextSelected]: checked && isCurrent
                      })}
                    >
                      {name}
                    </span>
                  </Radio>
                );
                return (
                  checked && disableTip ?
                    <Tooltip title={disableTip} key={value}>
                      <span className={styles.radioWrapper}>
                        {radioComponent}
                      </span>
                    </Tooltip> :
                    radioComponent
                );
              })
            }
          </Radio.Group>
        </div>
      }
    </div>
  );
};
