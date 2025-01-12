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

import { Navigation, Settings } from '@apitable/core';
import { IDingTalkModalType, showModalInDingTalk, showModalInFeiShu, showModalInWecom } from 'pc/components/economy/upgrade_modal';
import { navigationToUrl } from 'pc/components/route_manager/navigation_to_url';
import { Router } from 'pc/components/route_manager/router';
import { expandUpgradeSpace } from 'pc/components/space_manage/upgrade_space/expand_upgrade_space';
import { store } from 'pc/store';
import { getEnvVariables } from 'pc/utils/env';
import { showOrderContactUs } from './order_modal/pay_order_success';

export const goToUpgrade = () => {
  const state = store.getState();
  const spaceInfo = state.space.curSpaceInfo;
  const spaceId = state.space.activeId;
  const subscription = state.billing.subscription;
  const social = spaceInfo?.social;
  const user = state.user.info;

  if (subscription?.product?.includes('appsumo')) {
    window.open('https://appsumo.com/account/products/', '_blank', 'noopener,noreferrer');
    return;
  }

  // apitable env
  if (getEnvVariables().HIDDEN_BUSINESS_SUPPORT_PROGRAM_MODAL) {
    if (user && user.isAdmin) {
      Router.redirect(Navigation.SPACE_MANAGE, { params: { pathInSpace: 'upgrade' }, clearQuery: true });
    } else {
      expandUpgradeSpace();
    }
    return;
  }

  if (social?.appType == null && subscription?.product !== 'Bronze') {
    window.open(`/space/${spaceId}/upgrade?pageType=2`, '_blank', 'noopener,noreferrer');
    return;
  }

  // Self-built applications - Contact Customer Service
  if (social?.appType === 1) {
    return showOrderContactUs();
  }

  // Third-party applications
  if (social?.appType === 2 && social.enabled) {
    // Wecom
    if (social.platform === 1) {
      return showModalInWecom();
    }
    // Dingtalk
    if (social.platform === 2) {
      showModalInDingTalk(IDingTalkModalType.Upgrade);
      return;
    }
    // Feishu
    if (social.platform === 3) {
      if (user?.isAdmin) {
        return navigationToUrl(Settings.integration_feishu_upgrade_url.value);
      }
      return showModalInFeiShu();
    }
  }
  window.open(`/space/${spaceId}/upgrade`, '_blank', 'noopener,noreferrer');
};
