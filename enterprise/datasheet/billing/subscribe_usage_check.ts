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

import { underscore } from 'naming-style';
import store, { StoreAPI } from 'store2';
import {
  Api, byteMG, isPrivateDeployment, ISubscription, Strings, SubscribeUsageCheck, t,
  // @ts-ignore
  BillingConfig
} from '@apitable/core';
// @ts-ignore
import { store as storeState } from 'pc/store';
import { isMobileApp } from 'pc/utils/env';
// @ts-ignore
import { IExtra } from 'enterprise/billing/interface';

const SUBSCRIBE_USAGE = 'SUBSCRIBE_USAGE';

class SubscribeUsageCheckEnhance extends SubscribeUsageCheck {
  private storage: StoreAPI;

  constructor(props: any) {
    super(props);
    this.storage = store.namespace(SUBSCRIBE_USAGE);
  }

  // Record data in localstorage, set expiry time
  // Read data from localstorage
  triggerVikabyAlert(functionName: keyof ISubscription, extra?: IExtra) {
    if (document.querySelector('.VIKABY_SUB_POPOVER_CONTENT')) {
      return;
    }

    // Check if it has been triggered in the current time period
    if (!this.shouldAlertToUser(functionName, extra?.usage, extra?.alwaysAlert)) {
      return;
    }

    const state = storeState.getState();
    const subscription = state.billing.subscription!;
    const title = typeof subscription[functionName] !== 'number' ? t(Strings.billing_subscription_warning) : t(Strings.billing_usage_warning);
    const content =
      extra?.message ||
      t(Strings[underscore(functionName)], {
        usage: functionName === 'maxCapacitySizeInBytes' ? byteMG(Number(extra?.usage)) : extra?.usage,
        specification: functionName === 'maxCapacitySizeInBytes' ? byteMG(subscription[functionName]) : subscription[functionName],
        grade: extra?.grade || '',
      });

    this.triggerNotifyAdmin(functionName, extra);

    return {
      content,
      title,
      spaceId: state.space.activeId,
    };
  }

  /**
   * @description Depending on business requirements, the same usage warning will only appear once in a day
   * The data is stored in localstorage, before each prompt, check if the prompt appears in local, and pop up the prompt only if it does not.
   * In addition, warnings for usage alerts need to be sent to the administrator, but they are only sent once a day,
   * so it means that only the first triggered warning will be notified
   */
  shouldAlertToUser(functionName: keyof ISubscription, usage?: any, readonly?: boolean) {
    const state = storeState.getState();
    const userInfo = state.user.info;

    if (!userInfo?.sendSubscriptionNotify) {
      // If the global switch is turned off, user notification is disabled
      return false;
    }

    if (isMobileApp()) {
      // No prompt on app
      return false;
    }

    if (isPrivateDeployment()) {
      // Public cloud environments are also not prompted
      return false;
    }

    if (super.underUsageLimit(functionName, usage)) {
      // Check that the functional dosage criteria are met
      return false;
    }

    const spaceId = state.space.activeId;
    const result = this.storage.get(spaceId);

    if (readonly) {
      return true;
    }

    if (!result || result.expireDate < Date.now()) {
      this.storage.set(spaceId, {
        // Reminder expiry time adjusted to 2 days
        expireDate: new Date().setHours(24, 0, 0, 0) + 86400000,
        alertFunctionName: [functionName],
      });
      return true;
    }

    if (!result.alertFunctionName.includes(functionName)) {
      result.alertFunctionName.push(functionName);
      this.storage.set(spaceId, result);
      return true;
    }

    return false;
  }

  private triggerNotifyAdmin(functionName: keyof ISubscription, extra?: IExtra) {
    const state = storeState.getState();
    const spaceId = state.space.activeId!;
    const subscription = state.billing.subscription!;
    const idMap = BillingConfig.billing.notify;

    Api.createNotification([{
      spaceId,
      templateId: idMap?.[underscore(functionName)]?.link_notification_id?.[0] || '',
      body: {
        extras: {
          usage: functionName === 'maxCapacitySizeInBytes' ? this.splitStringToNumber(byteMG(Number(extra?.usage))) : extra?.usage,
          specification: functionName === 'maxCapacitySizeInBytes' ? this.splitStringToNumber(byteMG(Number(subscription[functionName]))) :
            Number(subscription[functionName]),
        }
      }
    }]);
  }

  private splitStringToNumber(str: string): number {
    return Number(str.split(' ')[0]);
  }
}

export const subscribeUsageCheck = new SubscribeUsageCheckEnhance(storeState);
