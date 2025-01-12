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
import { useState } from 'react';
import { Typography, useThemeColors } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import { SelectMarkFilled, WechatpayFilled, AlipayFilled, BankFilled } from '@apitable/icons';
import { ILevelInfo } from '../config';
import { payIdList } from '../order_info/order_info';
import styles from '../styles.module.less';

interface ISubscribePayMethodProps {
  setPay: React.Dispatch<React.SetStateAction<number>>;
  levelInfo: ILevelInfo;
  pay: number;
}

export const SubscribePayMethod: React.FC<ISubscribePayMethodProps> = (props) => {
  const { pay, setPay, levelInfo } = props;
  const [hoverPay, setHoverPay] = useState<undefined | number>();
  const colors = useThemeColors();

  const payMethodConfig = {
    new_wx_pub_qr: {
      payName: t(Strings.wechat_payment),
      payIcon: <WechatpayFilled color={'#09BB07'} size={24} />,
    },
    new_alipay_pc_direct: {
      payName: t(Strings.alipay),
      payIcon: <AlipayFilled color={'#1677FF'} size={24} />,
    },
    bank: {
      payName: t(Strings.transfer_to_public),
      payIcon: <BankFilled color={colors.rainbowTeal5} size={24} />,
    },
  };

  return <nav className={styles.tabs}>
    {
      payIdList.map((payId, index) => {
        const { payName, payIcon } = payMethodConfig[payId];
        const active = pay === index;
        return <div
          key={payId}
          className={styles.tab} onClick={() => { setPay(index); }}
          onMouseOver={() => { setHoverPay(index); }}
          onMouseOut={() => { setHoverPay(undefined); }}
          style={{
            borderColor: (active || hoverPay === index) ? levelInfo.activeColor : '',
            backgroundColor: active ? levelInfo.cardSelectBg : '',
          }}
        >
          {payIcon}
          <Typography
            variant={'body1'}
            className={classnames({ [styles.active]: active })}
            style={{
              marginLeft: 8,
              fontWeight: active ? 'bolder' : 'normal',
              color: active ? levelInfo.activeColor : ''
            }}
          >
            {payName}
          </Typography>
          {
            active && <SelectMarkFilled className={styles.checked} color={levelInfo.activeColor} />
          }
        </div>;
      })
    }
  </nav>;
};
