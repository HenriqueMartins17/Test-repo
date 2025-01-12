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

import { Api, ConfigConstant, Navigation, Settings, StatusCode, Strings, t } from '@apitable/core';
import { Message, Modal } from 'pc/components/common';
import { navigationToUrl } from 'pc/components/route_manager/navigation_to_url';
import { Router } from 'pc/components/route_manager/router';
import { useQuery } from 'pc/hooks';
import { ISubmitRequestParam } from '../../home';
import { FeiShuLogin } from './feishu_login';

const FeiShuBindUser = () => {
  const query = useQuery();
  const openId = query.get('openId');
  const tenantKey = query.get('tenantKey');

  const mobileModSubmit = async (data: ISubmitRequestParam) => {
    if (!openId || !tenantKey) {
      Router.push(Navigation.FEISHU, {
        params: { feiShuPath: 'err' },
        query: {
          msg: t(Strings.wrong_url),
        },
      });
      return;
    }

    const { areaCode, account, credential } = data;
    const {
      data: { success, code, message },
    } = await Api.socialFeiShuBindUser(
      areaCode,
      account,
      credential,
      openId!,
      tenantKey!
    );

    try {
      if (!success) {
        if (code === StatusCode.AccountErr.CommonErr) {
          Modal.confirm({
            type: 'warning',
            title: t(Strings.kindly_reminder),
            content: t(Strings.feishu_admin_login_err_message),
            okText: t(Strings.feishu_admin_login_err_to_register),
            onOk: () => {
              navigationToUrl(Settings.integration_feisu_register_now_url.value);
            },
          });
          return;
        }
        Message.error({ content: message });
        return;
      }

      const { data } = await Api.feishuTenantBindDetail(tenantKey);
      if (data.success) {
        const list = data.data.bindInfoList;
        if (Array.isArray(list) && list.length) {
          Router.push(Navigation.WORKBENCH, {
            params: { spaceId: list[0].spaceId },
          });
        } else {
          Router.push(Navigation.HOME);
        }
      } else {
        Message.error({ content: data.message });
      }
    } catch (error) {
      Message.error({ content: t(Strings.error) });
    }
  };

  return (
    <FeiShuLogin
      submitRequest={mobileModSubmit}
      mobileCodeType={ConfigConstant.SmsTypes.BIND_SOCIAL_USER}
    />
  );
};

export default FeiShuBindUser;
