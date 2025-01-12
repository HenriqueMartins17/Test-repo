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

import { Api, Strings, t, StoreActions, IMember, ITeam, UnitItem, IUserInfo } from '@apitable/core';
import { Message, Modal } from 'pc/components/common';
import { store } from 'pc/store';

export const freshDingtalkOrg = () => {
  return Api.freshDingtalkOrg().then(res => {
    const { success, message } = res.data;
    if (!success) {
      Message.error({ content: message || t(Strings.error) });
      return;
    } 
    Message.success({ content: t(Strings.reload_page_later_msg) });
    setTimeout(() => {
      window.location.reload();
    }, 5000);
    return;
  });
};

export const freshWecomOrg = () => {
  return Api.freshWecomOrg().then(res => {
    const { success, message, code } = res.data;
    if (!success) {
      switch(code) {
        case 1109: Modal.error({
          title: t(Strings.operate_fail),
          content: t(Strings.wecom_sync_address_error),
          okText: t(Strings.submit)
        }); break;
        default: Message.error({ content: message || t(Strings.error) });
      }
      return;
    } 
    Message.success({ content: t(Strings.reload_page_later_msg) });
    setTimeout(() => {
      window.location.reload();
    }, 5000);
    return;
  });
};

export const freshWoaContact= () => {
  return Api.woaRefreshContact().then(res => {
    const { success, message } = res.data;
    if (!success) {
      Message.error({ content: message || t(Strings.error) });
      return;
    }
    Message.success({ content: t(Strings.reload_page_later_msg) });
    setTimeout(() => {
      window.location.reload();
    }, 5000);
    return;
  });
};

export const freshIdaasOrg = () => {
  return Api.idaasContactSync().then(res => {
    const { success, message } = res.data;
    if (!success) { 
      Message.error({ content: message || t(Strings.error) });
      return;
    }
    Message.success({ content: t(Strings.reload_page_later_msg) });
    setTimeout(() => {
      window.location.reload();
    }, 5000);
    return;
  });
};

export const syncOrgMember = ({
  values,
  linkId,
  userInfo,
}: {
  values: UnitItem[];
  linkId: string;
  userInfo: IUserInfo | null;
}) => {
  const data = values.reduce(
    (prev: any, cur: any) => {
      if ((cur as IMember).memberId) {
        const { syncingTeamId, memberId, memberName } = cur as IMember & { syncingTeamId: string };
        prev.members.push({ teamId: syncingTeamId, memberId, memberName });
      } else if ((cur as ITeam).teamId) {
        prev.teamIds.push((cur as ITeam).teamId);
      }
      return prev;
    },
    { members: [], linkId, teamIds: [] },
  );

  Message.loading({ content: t(Strings.syncing) });
  Api.syncOrgMembers(data).then(res => {
    const { success, message } = res.data;
    if (!success) {
      Message.error({ content: message });
    } else {
      store.dispatch(StoreActions.getTeamListData(userInfo!));
      Message.success({ content: t(Strings.sync_success) });
    }
  });
};