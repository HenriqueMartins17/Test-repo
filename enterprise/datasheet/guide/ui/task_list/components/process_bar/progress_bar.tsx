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

import { FC } from 'react';
import { CheckOutlined } from '@apitable/icons';
import styles from './style.module.less';

interface IProps {
  percent?: number,
  strokeWidth?: number, // Line thickness
  strokeColor: string, // Line colour
}

export const ProcessBar: FC<IProps> = (props) => {
  const { percent = 0, strokeWidth = 8, strokeColor } = props;
  return (
    <div className={styles.progressBar}>
      <div
        className={styles.foregroundColor}
        style={{ 
          height: strokeWidth,
          width: percent + '%',
          backgroundColor: strokeColor,
        }}
      />
      { 
        ![0, 100].includes(percent) && 
        <div className={styles.icon} style={{
          left: percent + '%',
          transform: `translate(-${11}px, -${11 - strokeWidth / 2}px)`
        }}>
          <div style={{ transform: 'scale(1.1)' }}>
            <CheckOutlined size={19} />
          </div>
        </div>
      }
    </div>
  );
};
