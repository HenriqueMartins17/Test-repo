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
import { useThemeColors } from '@apitable/components';
import { CheckOutlined } from '@apitable/icons';
import styles from './styles.module.less';

export interface IStepsProps {
  current: number;
  steps: IStepItem[];
  onChange: (current: number) => void;
}

export interface IStepItem {
  title: string;
  onClick?: (item: IStepItem, index: number) => void;
}

export const Steps: React.FC<IStepsProps> = ({ current, steps }) => {
  const colors = useThemeColors();
  const stepItem = (item: IStepItem, index: number) => {
    const isFinish = current > index;
    return (
      <div key={item.title} className={classnames(
        styles.stepItem,
        current === index && styles.stepItemActive,
        isFinish && styles.stepItemFinish
      )}>
        <div className={styles.stepItemIcon} onClick={() => item?.onClick?.(item, index)}>
          {isFinish ? <CheckOutlined color={colors.white} size={24}/> : index + 1}
        </div>
        <div className={styles.stepItemContent}>
          <div className={styles.stepItemTitle}>{item.title}</div>
        </div>
      </div>
    );
  };

  return (
    <div className={styles.steps}>
      {steps.map((v, index) => (
        stepItem(v, index)
      ))}
    </div>
  );
};
