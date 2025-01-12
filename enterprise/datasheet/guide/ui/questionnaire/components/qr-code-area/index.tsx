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

import { useMount } from 'ahooks';
import classNames from 'classnames';
import QRCode from 'qrcode';
import { FC } from 'react';
import { Message } from '@apitable/components';
import styles from './style.module.less';

type IQrCodeAreaProps = {
  className?: string
  img?: string
  url?: string
};

export const QrCodeArea: FC<IQrCodeAreaProps> = (props) => {
  useMount(() => {
    if (!props.url) return;
    QRCode.toCanvas(props.url,
      {
        errorCorrectionLevel: 'H',
        margin: 1,
        width: 200,
      },
      (err, canvas) => {
        if (err) {
          Message.error({ content: 'Generate QrCode failed' });
        }
        const container = document.getElementById('shareQrCode');
        container?.appendChild(canvas);
      });
  });

  return (
    <div className={classNames(styles.box, props.className)}>
      <div className={styles.corner} />
      <div className={styles.corner} />
      <div className={styles.corner} />
      <div className={styles.corner} />
      {props.img && <img src={props.img} alt="" />}
      <div id="shareQrCode" />
    </div>
  );
};
