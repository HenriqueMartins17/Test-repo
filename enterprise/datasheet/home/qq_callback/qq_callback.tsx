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

import { Spin } from 'antd';
import { FC, useEffect } from 'react';
import { Api, ConfigConstant, Navigation } from '@apitable/core';
import { ScreenSize } from 'pc/components/common/component_display';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useResponsive } from 'pc/hooks';
import { isLocalSite } from 'pc/utils';
import { setStorage, StorageName } from 'pc/utils/storage/storage';
import styles from './style.module.less';

export const QqCallback: FC = () => {
  const query = useQuery();
  const accessToken = query.get('access_token') || '';
  const expiresIn = query.get('expires_in') || '';
  const code = query.get('code') || '';
  const type = Number(localStorage.getItem('vika_account_manager_operation_type')) || ConfigConstant.ScanQrType.Login;
  const shareReference = localStorage.getItem('share_login_reference');
  const reference = localStorage.getItem('reference');
  const inviteLinkData = localStorage.getItem('invite_link_data');
  const inviteCode = localStorage.getItem('invite_code');
  localStorage.removeItem('vika_account_manager_operation_type');
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.md);
  const bindOrSignup = (token: string) => {
    const parsedUrl = new URL('/user/improving_info', window.location.origin);
    const searchParams = new URLSearchParams('');
    searchParams.append('token', token);
    if (inviteLinkData) {
      const { linkToken, inviteCode } = JSON.parse(inviteLinkData);
      searchParams.append('inviteLinkToken', linkToken);
      searchParams.append('inviteCode', inviteCode);
    }
    if (inviteCode) {
      searchParams.append('inviteCode', inviteCode);
    }
    parsedUrl.search = searchParams.toString();

    if (isMobile) {
      window.opener && window.opener.close();
      window.location.href = parsedUrl.href;
    } else {
      window.opener.location.href = parsedUrl.href;
      window.close();
    }
  };
  const loginSuccess = () => {
    if (inviteLinkData && isMobile) {
      window.opener && window.opener.close();
      const { linkToken } = JSON.parse(inviteLinkData);
      const url = new URL('/invite/link', window.location.origin);
      const searchParams = new URLSearchParams('');
      searchParams.append('inviteLinkToken', linkToken);
      url.search = searchParams.toString();
      window.opener.location.href = url.href;
      return;
    }
    if (isMobile) {
      window.opener && window.opener.close();
      Router.push(Navigation.HOME);
      return;
    }
    if (shareReference) {
      setStorage(StorageName.ShareLoginFailed, false);
      window.location.href = shareReference;
      return;
    }
    if (reference && isLocalSite(window.location.href, reference)) {
      localStorage.removeItem('reference');
      window.location.href = reference;
      return;
    }
    window.opener.location.reload();
    setTimeout(() => {
      window.close();
    }, 300);
  };
  useEffect(() => {
    Api.qqLoginCallback(code, accessToken, expiresIn, type).then(res => {
      const { data, success, code } = res.data;
      if (success) {
        if (type === ConfigConstant.ScanQrType.Login) {
          if (data) {
            bindOrSignup(data);
          } else {
            loginSuccess();
          }
        } else {
          localStorage.setItem('binding_qq_status', code);
          window.close();
        }
      } else {
        if (type === ConfigConstant.ScanQrType.Login) {
          if (shareReference) {
            setStorage(StorageName.ShareLoginFailed, true);
            window.location.href = shareReference;
            return;
          }
          localStorage.setItem('qq_login_failed', 'true');
          window.close();
        } else {
          localStorage.setItem('binding_qq_status', code);
          window.close();
        }
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return (
    <div className={styles.container}>
      <Spin />
    </div>
  );
};
