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

import { store } from 'pc/store';

export const isSaaSApp = () => {
  const state = store.getState();
  const spaceInfo = state.space.curSpaceInfo;
  const subscription = state.billing.subscription;
  const social = spaceInfo?.social;
  const user = state.user.info;

  if (social?.appType == null && subscription?.product !== 'Bronze') {
    return true;
  }

  // Self-built applications - Contact Support
  if (social?.appType === 1) {
    return false;
  }

  // Third-party applications
  if (social?.appType === 2 && social.enabled) {
    // Wecom
    if (social.platform === 1) {
      return false;
    }
    // Dingtalk
    if (social.platform === 2) {
      return false;
    }
    // Feishu
    if (social.platform === 3) {
      if (user?.isAdmin) {
        return false;
      }
      return false;
    }
  }
  return true;
};
