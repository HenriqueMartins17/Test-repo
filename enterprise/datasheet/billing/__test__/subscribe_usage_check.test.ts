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

import { ISubscription, StoreActions } from '@apitable/core';
import { store } from 'pc/store';
import { subscribeUsageCheck } from '../subscribe_usage_check';

const defaultSubscribe: ISubscription = {
  addonPlans: [],
  maxRowsPerSheet: 1000,
  maxRowsInSpace: 11,
  maxSheetNums: 2,
  maxCapacitySizeInBytes: 1000000000,
  maxSeats: 10,
  maxGalleryViewsInSpace: 2,
  maxKanbanViewsInSpace: 100,
  maxFormViewsInSpace: 100,
  maxMirrorNums: 10,
  deadline: '2019-01-01T00:00:00.000+00:00',
  maxAdminNums: 10,
  maxRemainTrashDays: 10,
  plan: 'bronze_no_billing_period',
  product: 'Bronze',
  version: 'V1',
  maxGanttViewsInSpace: 100,
  maxCalendarViewsInSpace: 100,
  maxApiCall: 120000, // Original key-value pairs："maxApiUsages": 10,
  fieldPermissionNums: 1,
  maxRemainTimeMachineDays: 90,
  rainbowLabel: false,
  integrationDingtalk: false,
  integrationFeishu: false,
  integrationWeCom: false,
  watermark: false,
  integrationOfficePreview: false,
  nodePermissionNums: 100,
  productName: '-',
  billingPeriod: '-',
  productColor: '-',
  subscriptionType: '-',
  maxRemainRecordActivityDays: 14,
  blackSpace: true,
  securitySettingInviteMember: true,
  securitySettingApplyJoinSpace: true,
  securitySettingShare: true,
  securitySettingExport: true,
  securitySettingCatalogManagement: true,
  securitySettingDownloadFile: true,
  securitySettingCopyCellData: true,
  securitySettingMobile: true,
  securitySettingAddressListIsolation: true,
  subscriptionCapacity: 0,
  unExpireGiftCapacity: 0,
  maxAuditQueryDays: 0,
  recurringInterval: 'monthly',
  onTrial: true,
  expireAt: 11111111111,
  maxMessageCredits: 100
};

describe('test subscribeUsageChecker', () => {
  beforeEach(() => {
    // Inject the benefits corresponding to the default subscription level
    store.dispatch(StoreActions.updateSubscription(defaultSubscribe));

    // Injecting the user's global configuration
    store.dispatch(StoreActions.updateUserInfo({ sendSubscriptionNotify: true } as any));
  });

  it('maxRowsInSpace usage over limit', () => {
    const result = subscribeUsageCheck.triggerVikabyAlert('maxRowsInSpace', { usage: 200 });
    expect(result).not.toBe(undefined);
  });

  it('maxGanttViewsInSpace count  under limit', () => {
    const result = subscribeUsageCheck.triggerVikabyAlert('maxGanttViewsInSpace', { usage: 1 });
    expect(result).toBe(undefined);
  });

  it('maxGanttViewsInSpace count over limit', () => {
    const result = subscribeUsageCheck.triggerVikabyAlert('maxGanttViewsInSpace', { usage: 3000 });
    expect(result).not.toBe(undefined);
  });

  it('maxRowsPerSheet count over limit', () => {
    const result = subscribeUsageCheck.triggerVikabyAlert('maxRowsPerSheet', { usage: 3000 });
    expect(result).not.toBe(undefined);
  });

  it('sendSubscriptionNotify set false', () => {
    const result = subscribeUsageCheck.triggerVikabyAlert('maxRowsPerSheet', { usage: 3000 });
    expect(result).toBe(undefined);
  });

  it('allow click rainbowLabel', () => {
    const result = subscribeUsageCheck.triggerVikabyAlert('rainbowLabel');
    expect(result).not.toBe(undefined);
  });

  it('not allow click rainbowLabel', () => {
    store.dispatch(StoreActions.updateSubscription({ ...defaultSubscribe, rainbowLabel: true }));
    const result = subscribeUsageCheck.triggerVikabyAlert('rainbowLabel');
    expect(result).toBe(undefined);
  });
});
