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
import { FC } from 'react';
import * as React from 'react';
import GoldImg from 'static/icon/workbench/workbench_account_gold_icon.png';
import styles from './style.module.less';

interface IProgress {
  percent: number;
  maxCount: number;
  curCount: number;
}
interface IProgressValue {
  style?: React.CSSProperties;
  className?: string;
  count: number;
}
export const Progress: FC<IProgress> = (props) => {
  const { percent = 0, maxCount = 100, curCount } = props;
  const ProgressValue: FC<IProgressValue> = ({ style, className, count }) =>{
    return (
      <div
        className={classNames(styles.progressValue, className, styles.valueWrap)}
        style={{ backgroundImage: `url(${GoldImg})`, ...style }}
      >
        <span className={styles.valueText}>{count}</span>
      </div>
    );
  };
  return (
    <div className={styles.progress}>
      <div className={styles.progressBg}>
        <div className={classNames(styles.minValue, styles.valueWrap)} ><span className={styles.valueText}>0</span></div>
        <div className={styles.progressFinished}
          style={{ width: `${percent * 100}%` }}
        >
          {percent !== 0 && <ProgressValue className={styles.highlight} count={curCount}/>}
        </div>
        <ProgressValue count={maxCount} className={percent === 1 ? styles.highlight: undefined}/>
      </div>
    </div>
  );
};