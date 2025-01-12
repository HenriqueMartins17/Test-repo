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
import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { Skeleton, Tooltip, useThemeColors } from '@apitable/components';
import { getLanguage, Strings, t } from '@apitable/core';
import { QuestionCircleOutlined, SelectMarkFilled } from '@apitable/icons';
import { WrapperTooltip } from 'pc/components/widget/widget_panel/widget_panel_header';
import { ILevelInfo, paySystemConfig, SubscribePageType } from '../config';
import styles from '../styles.module.less';

interface ISubscribeSeatProps {
  seatList: number[];
  seat: number | undefined;
  setSeat: React.Dispatch<React.SetStateAction<number | undefined>>;
  levelInfo: ILevelInfo;
  loading: boolean;
  pageType: SubscribePageType;
}

export const SubscribeSeat: React.FC<ISubscribeSeatProps> = (props) => {
  const { seatList, seat, setSeat, levelInfo, loading, pageType } = props;
  const [hoverSeatIndex, setHoverSeatIndex] = useState<undefined | number>();
  const subscription = useSelector(state => state.billing.subscription);
  const isUpgrade = pageType === SubscribePageType.Upgrade;
  const colors = useThemeColors();

  if (pageType === SubscribePageType.Subscribe && levelInfo.level !== paySystemConfig.SILVER.level) {
    return <p className={styles.maxSeat}>
      {t(Strings.subscribe_new_choose_member, { member_num: seatList[0] })}
      <Tooltip content={t(Strings.subscribe_new_choose_member_tips, { member_num: seatList[0] })}>
        <span>
          <QuestionCircleOutlined color={colors.textCommonTertiary} />
        </span>
      </Tooltip>
    </p>;
  }

  if (isUpgrade && levelInfo.level !== paySystemConfig.SILVER.level) {
    return <p className={styles.maxSeat}>
      {t(Strings.subscribe_upgrade_choose_member, {
        old_member_num: Number(subscription?.maxSeats),
        new_member_num: seatList[0],
      })}
      <Tooltip
        content={t(Strings.subscribe_upgrade_choose_member_tips, { old_member_num: Number(subscription?.maxSeats), new_member_num: seatList[0] })}
      >
        <span>
          <QuestionCircleOutlined color={colors.textCommonTertiary} />
        </span>
      </Tooltip>
    </p>;
  }

  /**
   * The following part of the code no longer works and is purely a backup
   */
  return <div className={styles.checkSeatsNum}>
    {loading ? (
      <div style={{ width: '100%' }}>
        <Skeleton width='38%' />
        <Skeleton count={2} />
        <Skeleton width='61%' />
      </div>
    ) : seatList.map((item, index) => {
      const active = seat === item;
      const isDisabled = isUpgrade && item <= Number(subscription?.maxSeats);
      return <WrapperTooltip key={index} wrapper={isDisabled} tip={t(Strings.subscribe_disabled_seat)}>
        <div
          className={classnames({
            [styles.seatCard]: true,
            [styles.active]: active,
            [styles.disabledSeatCard]: isDisabled,
          })}
          onClick={() => {!isDisabled && setSeat(item); }}
          onMouseOver={() => {!isDisabled && setHoverSeatIndex(index); }}
          onMouseOut={() => {!isDisabled && setHoverSeatIndex(undefined); }}
          style={{
            fontSize: 16,
            borderColor: (active || hoverSeatIndex === index) ? levelInfo.activeColor : '',
            backgroundColor: active ? levelInfo.cardSelectBg : '',
            color: active ? levelInfo.activeColor : '',
            fontWeight: active ? 'bolder' : 'normal',
          }}
        >
          {item} {getLanguage() === 'zh-CN' && t(Strings.people)}
          {levelInfo.level === paySystemConfig.SILVER.level && index == 0 && <span>(买一送一)</span>}
          {
            active &&
            <SelectMarkFilled size={24} className={styles.checked} color={levelInfo.activeColor} />
          }
        </div>
      </WrapperTooltip>;
    })}
  </div>;
};
