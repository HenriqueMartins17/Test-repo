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

import { isObject } from 'lodash';
import { Message } from '@apitable/components';
import { Api, ISpaceBasicInfo, ISpaceInfo, Strings, t } from '@apitable/core';
import { Modal } from 'pc/components/common/modal/modal/modal';
import { store } from 'pc/store';
import { isSocialWecom, isWecomFunc, isSocialPlatformEnabled, SocialPlatformMap } from '../../home';
import { getWecomAgentConfig } from '../../wecom/wecom_contact_wrapper';

export const checkSocialInvite = (spaceInfo?: ISpaceInfo | ISpaceBasicInfo | null) => {
  const state = store.getState();
  // Enterprise, authMode 2 means "Member Authorization", only member authorization mode can call Enterprise address book
  if (isSocialWecom?.(spaceInfo) && spaceInfo?.social.authMode === 2) {
    // Not a built-in browser for Enterprise Micro
    if (!isWecomFunc()) {
      Modal.warning({
        title: t(Strings.invite_member),
        content: t(Strings.wecom_invite_member_browser_tips),
      });
      return true;
    }
    const wx = (window as any).wx;
    const spaceId = state.space.activeId;
    if (isObject(wx) && spaceId) {
      // https://developer.work.weixin.qq.com/document/path/94516
      getWecomAgentConfig?.(spaceId, () => {
        (wx as any).invoke(
          'selectPrivilegedContact',
          {
            fromDepartmentId: -1,
            mode: 'multi', // Mandatory, select mode, single means single, multi means multiple
            selectedContextContact: 0,
          },
          function (res: any) {
            if (res.err_msg == 'selectPrivilegedContact:ok') {
              const selectedTicket = res.result.selectedTicket;
              Api.postWecomUnauthMemberInvite(spaceId, [selectedTicket]).then(rlt => {
                if (rlt.data.message === 'SUCCESS') {
                  Message.success({ content: t(Strings.success) });
                } else {
                  Message.error({ content: t(Strings.error) });
                }
              });
            }
          },
        );
      });
    }
    return true;
  }

  if (spaceInfo && isSocialPlatformEnabled(spaceInfo)) {
    SocialPlatformMap[spaceInfo.social.platform].org_manage_reject_default_modal();
    return true;
  }

  return false;
};