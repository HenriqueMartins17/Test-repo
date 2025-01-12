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
import { Api } from '@apitable/core';
import { useRequest } from 'pc/hooks';
import { isSocialPlatformEnabled } from '../home';
import { AppType, IStoreApp, IStoreAppInstance } from './interface';

export const useMarketing = (spaceId: string, refresh: boolean) => {
  const [appInstances, setAppInstances] = useState<IStoreApp[]>([]);
  const [apps, setApps] = useState<IStoreApp[]>([]);
  const socialApps = [AppType.DingTalk, AppType.Lark, AppType.Wecom, AppType.LarkStore, AppType.WecomStore, AppType.DingtalkStore];
  // const isSocial = spaceInfo && isSocialPlatformEnabled(spaceInfo);

  const { loading } = useRequest(() => Promise.all([
    Api.getAppstoreApps(),
    Api.getAppInstances(spaceId),
    Api.spaceInfo(spaceId),
  ]).then((res) => {
    const [appsResult, instancesResult, spaceResult] = res;
    const { data: appsData, success: appsSuccess } = appsResult.data;
    const { data: instancesData, success: instanceSuccess } = instancesResult.data;
    const { data: spaceData, success: spaceSuccess } = spaceResult.data;

    let isSocial = false;
    if (spaceSuccess) {
      isSocial = isSocialPlatformEnabled(spaceData);
    }

    let appRecords: IStoreApp[] = [];
    if (appsSuccess) {
      appRecords = appsData.records;
    }
    const finalInstanceRecords: IStoreApp[] = [];
    let type: AppType;
    if (instanceSuccess) {
      const instanceRecords: IStoreAppInstance[] = instancesData.records;
      const finalAppRecords: IStoreApp[] = [];
      appRecords.forEach((v) => {
        // Find Example Items
        const instanceItem = instanceRecords.filter((ele) => ele.appId === v.appId)[0];
        if (instanceItem) {
          if (!type && socialApps.includes(instanceItem.type)) {
            type = instanceItem.type; // Logging of opened instances
          }
          finalInstanceRecords.push({ ...v, instance: instanceItem });
        } else {
          finalAppRecords.push(v);
        }
      });
      appRecords = finalAppRecords.filter((v) => (
        !type || (type && !socialApps.includes(v.type))
      ));
      // Filtering without the store app turned on
      if (!isSocial) {
        appRecords = appRecords.filter((v) => v.type === 'OFFICE_PREVIEW' || !v.type.includes('STORE'));
      }
    }
    setApps(appRecords);
    setAppInstances(finalInstanceRecords);
  }), {
    refreshDeps: [refresh]
  });

  return { loading, appInstances, apps };
};