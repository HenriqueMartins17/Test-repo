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

interface IMobileModalProps {
  onClose?: (...args: any) => void,
}

export const MobileModal: FC<PropsWithChildren<IMobileModalProps>> = (props) => {
  const colors = useThemeColors();
  const { children, onClose } = props;
  return (
    <div className={styles.mobileModal}>
      <div className={styles.mask} />
      <div className={styles.content}>
        <div className={styles.body}>
          { children }
        </div>
        <div className={styles.closeBtn} onClick={() => { onClose && onClose(); }}>
          <CloseOutlined size={16} color={colors.textStaticPrimary} />
        </div>
      </div>
    </div>
  );
};
