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

import { FC, PropsWithChildren } from 'react';
import { useThemeColors } from '@apitable/components';
import { CloseOutlined } from '@apitable/icons';
import styles from './style.module.less';

interface IProps {
  width?: number | string;
  onClick?: () => void;
}

export const Model: FC<PropsWithChildren<IProps>> = (props) => {
  const colors = useThemeColors();
  const { width, children, onClick } = props;
  return (
    <div className={styles.modelRoot}>
      <div className={styles.content} style={{ width }}>
        <div className={styles.closeBtn} onClick={ () => { onClick && onClick(); } }>
          <CloseOutlined size={24} color={colors.defaultBg} />
        </div>
        { children }
      </div>
    </div>
  );
};