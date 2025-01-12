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

import { useRequest } from 'ahooks';
import * as React from 'react';
import { useMemo } from 'react';
import { Typography } from '@apitable/components';
import { Api, Strings, t } from '@apitable/core';
import { TimeOutlined } from '@apitable/icons';
import styles from '../styles.module.less';
import { SubscribeLevelTab } from '../subscribe_header/subscribe_header';

interface ISubscribeOfferProps {
  levelTab: SubscribeLevelTab;
}

export const SubscribeOffer: React.FC<ISubscribeOfferProps> = props => {
  const { levelTab } = props;
  const { data: eventData } = useRequest(Api.getSubscribeActiveEvents);

  const endDate = useMemo(() => {
    if (!eventData) return null;
    // eslint-disable-next-line no-unsafe-optional-chaining
    const { data, success } = eventData?.data;
    if (!success) {
      return null;
    }
    return data.endDate;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [eventData?.data]);

  if (!endDate) {
    return null;
  }

  return (
    <div className={styles.limitedTimeOffer} style={{ opacity: levelTab !== 'ENTERPRISE' ? 1 : 0 }}>
      <TimeOutlined color={'#fff'} />
      <Typography variant="h9" color={'#fff'}>
        {t(Strings.limited_time_offer)}
      </Typography>
      <span>|</span>
      <Typography variant="body4" color={'#fff'}>
        {t(Strings.discount_price_deadline)}
      </Typography>
      &nbsp;
      <Typography variant="h9" color={'#fff'}>
        {endDate}
      </Typography>
    </div>
  );
};
