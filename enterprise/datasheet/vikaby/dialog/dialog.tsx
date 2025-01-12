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
import parser from 'html-react-parser';
import React, { FC } from 'react';
import { Button } from '@apitable/components';
import { CloseOutlined, Star2Filled } from '@apitable/icons';
import { ButtonPlus } from 'pc/components/common/button_plus/button_plus';
import styles from './style.module.less';

export interface IDialog {
  onClose?: (event?: React.MouseEvent<HTMLElement, MouseEvent>) => void;
  title?: string;
  content?: string;
  btnText?: string;
  onBtnClick?: () => void;
  dialogClx?: string;
}

export const Dialog: FC<IDialog> = (props) => {
  const { onBtnClick, onClose, title, content, btnText, dialogClx } = props;

  return (
    <div
      className={classNames({
        [styles.billingNotify]: dialogClx === 'billingNotify',
      })}
    >
      <ButtonPlus.Icon icon={<CloseOutlined />} size="x-small" className={styles.close} onClick={onClose} />
      <div className={styles.title}>{title}</div>
      <div className={styles.desc}>{content && parser(content)}</div>
      {btnText && (
        <div className={styles.btnWrap}>
          <Button size="middle" color="warning" onClick={onBtnClick} prefixIcon={<Star2Filled color="#FFEB3A" />}>
            {btnText}
          </Button>
        </div>
      )}
    </div>
  );
};
