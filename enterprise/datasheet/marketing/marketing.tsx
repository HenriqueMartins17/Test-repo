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

import { useState } from 'react';
import * as React from 'react';
import { useSelector } from 'react-redux';
import { Typography, useThemeColors } from '@apitable/components';
import { IReduxState, Strings, t } from '@apitable/core';
import { Loading } from 'pc/components/common';
import { getEnvVariables } from 'pc/utils/env';
import { Trial } from '../log/trial';
import { MarketingContext } from './context';
import { useMarketing } from './hooks';
import { AppStatus, IStoreAppConfig } from './interface';
import { List } from './list';
import style from './style.module.less';

export const OFFICE_APP_ID = 'ina5645957505507647';
export const DINGTALK_APP_ID = 'ina9134969049653777';
export const WEWORK_APP_ID = 'ina5200279359980055';

const MarketingBase: React.FC = () => {
  const colors = useThemeColors();
  const spaceInfo = useSelector((state: IReduxState) => state.space.curSpaceInfo);
  const spaceId = useSelector(state => state.space.activeId)!;
  const [refresh, setRefresh] = React.useState(false);

  const { loading, apps, appInstances } = useMarketing(spaceId, refresh);

  if (loading) {
    return <Loading />;
  }

  const data: IStoreAppConfig[] = [
    { type: AppStatus.Open, data: appInstances },
    { type: AppStatus.Close, data: apps },
  ];

  if (!spaceInfo || !apps) {
    return <Loading />;
  }

  return (
    <div className={style.container}>
      <div style={{ marginBottom: 32 }}>
        <Typography variant='h1'>{t(Strings.space_info_feishu_label)}</Typography>
        <div style={{ height: 8 }} />
        <Typography variant='body2' color={colors.thirdLevelText}>{t(Strings.marketing_sub_title)}</Typography>
      </div>
      <MarketingContext.Provider value={{ onSetRefresh: setRefresh }}>
        {data.map((list) => {
          if (list.data.length === 0) {
            return null;
          }
          return <List type={list.type} data={list.data} key={list.type} />;
        })}
      </MarketingContext.Provider>
    </div>
  );
};

export const Marketing = () => {
  const vars = getEnvVariables();
  const [showTrialModal, setShowTrialModal] = useState<boolean>(vars.CLOUD_DISABLE_USE_APP_STORE);

  if (showTrialModal) {
    return <Trial setShowTrialModal={setShowTrialModal} title={t(Strings.space_info_feishu_label)}/>;
  }

  return <MarketingBase />;
};
