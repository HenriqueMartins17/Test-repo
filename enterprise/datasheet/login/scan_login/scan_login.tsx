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

import { useMount } from 'ahooks';
import Image from 'next/image';

import * as React from 'react';
import { isMobile } from 'react-device-detect';
import { useSelector } from 'react-redux';
import { ConfigConstant, IReduxState, QrAction, Strings, t } from '@apitable/core';
import { TComponent } from 'pc/components/common/t_component';
import { useQuery } from 'pc/hooks';
import { getEnvVariables } from 'pc/utils/env';
import { isDesktop } from 'pc/utils/os';
import DingdingIcon from 'static/icon/signin/signin_img_dingding.png';
import LarkIcon from 'static/icon/signin/signin_img_feishu.png';
import QQIcon from 'static/icon/signin/signin_img_qq.png';
import WechatIcon from 'static/icon/signin/signin_img_wechat.png';
import WecomIcon from 'static/icon/signin/signin_img_wecom.png';
import { QRCodeBase, isWecomFunc } from '../../home';
import { DingdingQrCode } from '../dingding_qr_code';
import { feishuLogin, qqLogin, wecomLogin, wecomQuickLogin, wecomShopLogin } from '../other_login';

import styles from './style.module.less';

export const ScanLogin = (
  {
    afterLogin
  }: { afterLogin?(data: string, loginMode: ConfigConstant.LoginMode): void }): JSX.Element => {
  const inviteLinkInfo = useSelector((state: IReduxState) => state.invite.inviteLinkInfo);
  const inviteLinkToken = useSelector((state: IReduxState) => state.invite.linkToken);
  const isWecomDomain = useSelector(state => state.space.envs?.weComEnv?.enabled);

  const query = useQuery();
  const reference = query.get('reference');

  const [currentScan, setCurrentScan] = React.useState(() => {
    if (!(getEnvVariables().IS_SELFHOST || getEnvVariables().IS_APITABLE)) {
      return 'wechat';
    }
    if (!(getEnvVariables().IS_SELFHOST || getEnvVariables().IS_APITABLE)) {
      return 'dingding';
    }
    return '';
  });

  const isInvitePage = !process.env.SSR && window.location.pathname.startsWith('/invite');

  const settingReference = () => {
    if (!window.location.pathname.startsWith('/login') && !isInvitePage) {
      localStorage.setItem('reference', window.location.href);
      return;
    }
    if (window.location.pathname.startsWith('/login') && reference) {
      localStorage.setItem('reference', reference);
    }
  };

  const settingInviteData = () => {
    const urlParams = new URLSearchParams(window.location.search);
    const isFromLinkInvite = urlParams.has('inviteLinkToken');
    if (isFromLinkInvite && inviteLinkToken && inviteLinkInfo) {
      const info = {
        inviteLinkInfo,
        linkToken: inviteLinkToken,
        inviteCode: urlParams.get('inviteCode'),
      };
      localStorage.setItem('invite_link_data', JSON.stringify(info));
    }
    const inviteCode = urlParams.get('inviteCode');
    if (inviteCode) {
      localStorage.setItem('invite_code', inviteCode);
    }
  };

  const scanLoginMethods = [{
    id: 'wechat_login_btn',
    img: WechatIcon,
    name: t(Strings.wechat),
    hidden: getEnvVariables().IS_SELFHOST || getEnvVariables().IS_APITABLE,
    onClick: () => {
      settingReference();
      setCurrentScan('wechat');
    },
  }, {
    id: 'dingding_login_btn',
    img: DingdingIcon,
    name: t(Strings.dingtalk),
    hidden: getEnvVariables().IS_SELFHOST || getEnvVariables().IS_APITABLE,
    onClick: () => {
      settingReference();
      settingInviteData();
      setCurrentScan('dingding');
    },
  }, {
    id: 'feishu_login_btn',
    img: LarkIcon,
    name: t(Strings.lark),
    hidden: isInvitePage || !getEnvVariables().INTEGRATIONS_FEISHU_VISIBLE,
    onClick: () => {
      settingReference();
      feishuLogin();
    },
  }, {
    id: 'wecom_login_btn',
    img: WecomIcon,
    name: t(Strings.wecom),
    hidden: !isWecomDomain || !isMobile || getEnvVariables().INTEGRATIONS_WECOM_VISIBLE,
    onClick: () => {
      settingReference();
      wecomLogin();
    }
  }, {
    id: 'wecom_shop_login_btn',
    img: WecomIcon,
    name: t(Strings.wecom),
    hidden: isDesktop(),
    onClick: () => {
      settingReference();
      isWecomFunc() ? wecomQuickLogin('snsapi_privateinfo', reference) : wecomShopLogin();
    }
  }, {
    id: 'qq_login_btn',
    img: QQIcon,
    name: t(Strings.qq),
    hidden: isDesktop() || getEnvVariables().IS_SELFHOST || getEnvVariables().IS_APITABLE,
    onClick: () => {
      settingReference();
      settingInviteData();
      qqLogin();
    },
  }];

  useMount(() => {
    localStorage.removeItem('share_login_reference');
    localStorage.removeItem('invite_link_data');
    localStorage.removeItem('invite_code');
  });

  return (
    <div className={styles.scanLogin}>
      <div className={styles.qrContainer}>
        {currentScan === 'wechat' && (
          <>
            <div className={styles.scanTip}>
              <TComponent
                tkey={t(Strings.scan_to_login_by_method)}
                params={{
                  method: <span className={styles.tipHighlight}>{t(Strings.wechat_qr_code)}</span>,
                }}
              />
            </div>
            <div className={styles.qrWrapper}>
              <QRCodeBase afterLogin={afterLogin} action={QrAction.LOGIN} />
            </div>
          </>
        )}
        {currentScan === 'dingding' && (
          <div className={`${styles.qrWrapper} ${styles.dingding}`}>
            <DingdingQrCode />
          </div>
        )}
      </div>
      <div className={styles.iconRow}>
        {
          scanLoginMethods.map(item => !item.hidden && (
            <div
              key={item.name}
              id={item.id}
              className={styles.icon}
              onClick={item.onClick}
            >
              <Image src={item.img} alt={item.name} width={32} height={32} />
            </div>
          ))
        }
      </div>
    </div>
  );
};
