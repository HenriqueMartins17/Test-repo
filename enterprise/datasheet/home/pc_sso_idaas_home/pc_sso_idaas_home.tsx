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
import { Space } from 'antd';
import Image from 'next/image';
import React, { FC, useState } from 'react';
import { Button, ButtonGroup, colors, LinkButton } from '@apitable/components';
import { Api, ApiInterface, Navigation, Settings, Strings, t, integrateCdnHost } from '@apitable/core';
import { Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useUserRequest } from 'pc/hooks';
import { getEnvVariables } from 'pc/utils/env';
import { ISubmitRequestParam } from '../login/identifying_code_login';
import { PasswordLogin } from '../login/password_login';
import styles from './style.module.less';

export const PcSsoIdaasHome: FC = () => {

  return (
    <>
      <div className={styles.homeLeft}>
        <div
          style={{ backgroundImage: `url(${process.env.PUBLIC_URL}/idaas_login.png)` }}
          className={styles.leftBg}
        />
        <div className={styles.bottom}>
          <Space size={55} align='start' className={styles.qrCodeGroup}>
            <div className={styles.qrCode}>
              <Image
                src={integrateCdnHost(getEnvVariables().LOGIN_WECHAT_GROUP_QR_CODE!)} alt='CommunicationGroup Code' layout={'fill'}
              />
              <div className={styles.caption}>{t(Strings.communication_group_qrcode)}</div>
            </div>
            <div className={styles.qrCode}>
              <Image src={integrateCdnHost(getEnvVariables().LOGIN_VIKA_QR_CODE!)} alt='OfficialAccount Code' layout={'fill'} />
              <div className={styles.caption}>{t(Strings.signin_idaas_official_account)}</div>
            </div>
          </Space>
        </div>
      </div>
      <div className={styles.homeRight}>
        <SsoNav />
        <LoginContent />
      </div>

    </>
  );
};

export const SsoNav: FC = () => {
  const { LOGIN_OFFICIAL_WEBSITE_URL } = getEnvVariables();
  const jumpOfficialWebsite = () => {
    if (LOGIN_OFFICIAL_WEBSITE_URL) {
      window.open(LOGIN_OFFICIAL_WEBSITE_URL, '__blank');
      return;
    }
    Router.newTab(Navigation.HOME, { query: { home: 1 } });
  };
  return (
    <div className={styles.nav}>
      <ButtonGroup withSeparate>
        <LinkButton
          component='button'
          underline={false}
          style={{ color: colors.thirdLevelText }}
          onClick={() => Router.push(Navigation.TEMPLATE)}
        >
          模板中心
        </LinkButton>
        <LinkButton
          component='button'
          underline={false}
          style={{ color: colors.thirdLevelText }}
        >
          <a href={`${window.location.origin}/chatgroup/`} target='_blank' style={{ color: 'inherit' }} rel='noreferrer'>{t(Strings.feedback)}</a>
        </LinkButton>
        <LinkButton
          component='button'
          underline={false}
          style={{ color: colors.thirdLevelText }}
        >
          <a href='/help/' target='_blank' style={{ color: 'inherit' }}>{t(Strings.support)}</a>
        </LinkButton>
        <LinkButton
          component='button'
          underline={false}
          onClick={jumpOfficialWebsite}
          style={{ color: colors.thirdLevelText }}
        >
          {t(Strings.enter_official_website)}
        </LinkButton>
      </ButtonGroup>
    </div>
  );
};

export const LoginContent: FC = () => {
  const [isPasswordLogin, setIsPasswordLogin] = useState<Boolean>(false);
  const { loginOrRegisterReq } = useUserRequest();

  const submitRequest = React.useCallback((data: ISubmitRequestParam) => {
    const loginData: ApiInterface.ISignIn = {
      areaCode: data.areaCode,
      username: data.account,
      type: data.type,
      credential: data.credential,
      data: data.nvcVal,
      mode: data.mode,
    };
    return loginOrRegisterReq(loginData);
  }, [loginOrRegisterReq]);

  return (
    <div className={styles.rightContent}>
      <div className={styles.loginContainer}>
        {isPasswordLogin ?
          <div className={styles.passwordLogin}>
            <div className={styles.topBlock} />
            <div className={styles.header}>
              <Image src={`${process.env.PUBLIC_URL}/logo.png`} alt='vika_logo' layout={'fill'} />
              <p>维格专有云版</p>
            </div>
            <p className={styles.username}>账号</p>
            <PasswordLogin
              submitRequest={submitRequest}
            />
          </div> :
          <div className={styles.loginContent}>
            <div className={styles.topBlock} />
            <Image src={`${process.env.PUBLIC_URL}/logo.png`} alt='vika_logo' layout={'fill'} />
            <p>维格专有云版</p>
            <div className={styles.loginButton}>
              <LoginButton />
            </div>
          </div>
        }
        <p className={styles.swtichLogin} onClick={() => setIsPasswordLogin(!isPasswordLogin)}>{isPasswordLogin ? '切换快速登录' : '切换账号登录'}</p>
      </div>
      <div className={styles.icp}>
        <LinkButton className={styles.icpBtn} href={Settings['login_icp1_url'].value} underline={false} color={colors.fc3} target='_blank'>
          {t(Strings.icp1)}
        </LinkButton>
        <div className={styles.line} />
        <LinkButton className={styles.icpBtn} href={Settings['login_icp2_url'].value} underline={false} color={colors.fc3} target='_blank'>
          {t(Strings.icp2)}
        </LinkButton>
      </div>
    </div>
  );
};

export const LoginButton: FC = () => {
  const query = useQuery();

  const getIdaasLoginUrl = async (clientId: string) => {
    const res = await Api.getIDassLoginUrl(clientId);
    return res.data;
  };

  const [idaasLoginUrl, setIdaasLoginUrl] = useState<string>('');
  const clientId = query.get('client_id');
  const reference = query.get('reference') || '';
  const spaceId = query.get('spaceId') || localStorage.getItem('spaceId');

  useMount(async () => {
    localStorage.setItem('reference', reference);
    let clientIdGet = '';
    if (spaceId) {
      const res = await Api.spaceBindIdaasInfo(spaceId);
      const { data, success } = res.data;
      if (success && data.enabled) {
        clientIdGet = data.clientId;
      }
    }
    const param = clientId || clientIdGet;
    if (!param) {
      return;
    }
    const res = await getIdaasLoginUrl(param);
    const { data, success, message } = res;
    if (success) {
      setIdaasLoginUrl(data.loginUrl);
    } else {
      Message.error({ content: message || t(Strings.login_failed) });
    }

  });

  const gotoIdaasLogin = () => {
    //encodeURIComponent
    window.location.href = idaasLoginUrl;
  };
  return (
    <Button color='primary' block onClick={gotoIdaasLogin} disabled={!idaasLoginUrl}>{idaasLoginUrl !== '' ? 'SSO 登录' : '当前网址不合法'}</Button>
  );
};
