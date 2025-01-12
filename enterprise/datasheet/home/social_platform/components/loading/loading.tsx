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

import { Spin } from 'antd';
import { CSSProperties, FC } from 'react';
import { LoadingOutlined } from '@apitable/icons';
import styles from './style.module.less';

export interface ILoadingProps {
  tip?: string;
  style?: CSSProperties;
}

export const Loading: FC<ILoadingProps> = props => {
  const {
    tip,
    style,
  } = props;

  return (
    <div style={style} className={styles.loading}>
      <Spin
        tip={tip}
        indicator={<LoadingOutlined size={24} className="circle-loading" />}
      />
    </div>
  );
};
