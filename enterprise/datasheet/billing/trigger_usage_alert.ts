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
import { isMobile } from 'react-device-detect';
import { ISubscription, Strings, t } from '@apitable/core';
import { Message } from 'pc/components/common/message';
import { usageWarnModal } from '../subscribe_system/usage_warn_modal/usage_warn_modal';
import { IExtra } from './interface';
import { subscribeUsageCheck } from './subscribe_usage_check';
// @ts-ignore
import { showVikaby } from 'enterprise/vikaby';

export enum SubscribeUsageTipType {
  Vikaby,
  Alert,
}

export const triggerUsageAlert = (functionName: keyof ISubscription, extra?: IExtra, tipType?: SubscribeUsageTipType): boolean => {
  const result = subscribeUsageCheck.triggerVikabyAlert(functionName, extra);

  if (!result) {
    return false;
  }

  const { title, content } = result;

  if (tipType === SubscribeUsageTipType.Alert) {
    if (isMobile) {
      Message.warning({
        content: t(Strings.mobile_usage_over_limit_tip),
      });
      return true;
    }
    usageWarnModal({ alertContent: content, reload: extra?.reload });
    return true;
  }

  showVikaby({
    defaultExpandDialog: true,
    dialogConfig: {
      title,
      content,
      dialogClx: 'billingNotify',
    },
  });
  return true;
};

export const triggerUsageAlertUniversal = (content: string) => {
  if (isMobile) {
    Message.warning({
      content: t(Strings.mobile_usage_over_limit_tip),
    });
    return;
  }
  // usageWarnModal({ alertContent: t(Strings.subscribe_seats_usage_over_limit) });
  usageWarnModal({ alertContent: content });
};

export const triggerUsageAlertForDatasheet = (content: string) => {
  if (isMobile) {
    Message.warning({
      content: t(Strings.mobile_usage_over_limit_tip),
    });
    return;
  }
  usageWarnModal({ alertContent: content, reload: true });
};
