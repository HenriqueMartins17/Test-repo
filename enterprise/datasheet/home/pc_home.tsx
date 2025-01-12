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

import { Space } from 'antd';
import Image from 'next/image';
import React, { FC } from 'react';
import { useSelector } from 'react-redux';
import { ButtonGroup, lightColors, LinkButton } from '@apitable/components';
import { integrateCdnHost, AutoTestID, isIdassPrivateDeployment, isPrivateDeployment, Navigation, Settings, Strings, t } from '@apitable/core';
import { PlanetOutlined } from '@apitable/icons';
import { Logo } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { isRenderServer } from 'pc/utils';
import { getEnvVariables, isMobileApp } from 'pc/utils/env';
import ConsultImg from 'static/icon/signin/signin_img_homepage.png';
import BackgroundLeft from 'static/icon/signin/signin_img_homepage_bj_left.png';
import BackgroundRight from 'static/icon/signin/signin_img_homepage_bj_right.png';
import { LoginToggle, isDingtalkFunc, isLarkFunc, isQQFunc, isWechatFunc, isWecomFunc } from '../home';
import { IntroductionVideo } from './introduction_video';
import { Login } from './login';
import { PcSsoIdaasHome } from './pc_sso_idaas_home';
import styles from './style.module.less';

const PcHome: FC = () => {
  const isWecom = useSelector(state => state.space.envs?.weComEnv?.enabled || isWecomFunc());
  const { LOGIN_OFFICIAL_WEBSITE_URL, IS_SELFHOST, IS_APITABLE } = getEnvVariables();
  const env = getEnvVariables();
  const qrcodeVisible = !(env.IS_SELFHOST || env.IS_APITABLE);
  const jumpOfficialWebsite = () => {
    if (LOGIN_OFFICIAL_WEBSITE_URL) {
      window.open(LOGIN_OFFICIAL_WEBSITE_URL, '__blank');
      return;
    }
    Router.newTab(Navigation.HOME, { query: { home: 1 } });
  };

  return (
    <>
      {isIdassPrivateDeployment() ? <PcSsoIdaasHome /> :
        <>
          <span className={styles.backgroundLeft}>
            <Image src={BackgroundLeft} alt='background left' layout={'fill'} objectFit={'contain'} />
          </span>
          <div className={styles.homeLeft}>
            <div>
              <div className={styles.logo} onClick={jumpOfficialWebsite}>
                {isPrivateDeployment() ? (
                  <span
                    className={styles.logoPng}
                    onClick={jumpOfficialWebsite}
                    style={{
                      height: 36,
                    }}
                  >
                    <Image
                      src={`${env.NEXT_PUBLIC_PUBLIC_URL || ''}/common_img_logo.png`}
                      alt='vika_logo'
                      width={32}
                      height={32}
                    />
                  </span>

                ) :
                  (
                    <span
                      className={styles.logoPng}>
                      <Logo size='large' />
                    </span>
                  )
                }
                <div className={styles.logoSlogan}>{t(Strings.login_logo_slogan)}</div>
              </div>
              <div className={styles.placeholder} />
              {!(IS_SELFHOST || IS_APITABLE) &&
                <div className={styles.video}>
                  <IntroductionVideo />
                </div>
              }
            </div>
            { qrcodeVisible &&
              <div className={styles.bottom}>
                <span className={styles.consultImg}>
                  <Image src={ConsultImg} alt='consult' layout={'responsive'} />
                </span>
                <Space size={55} align='start' className={styles.qrCodeGroup}>
                  <div className={styles.qrCode}>
                    <Image
                      src={integrateCdnHost(getEnvVariables().LOGIN_WECHAT_GROUP_QR_CODE!)} alt='CommunicationGroup Code'
                      width={80}
                      height={80}
                    />
                    <div className={styles.caption}>{t(Strings.communication_group_qrcode)}</div>
                  </div>
                  <div className={styles.qrCode}>
                    <Image
                      src={integrateCdnHost(getEnvVariables().LOGIN_VIKA_QR_CODE!)} alt='OfficialAccount Code'
                      width={80}
                      height={80}
                    />
                    {!isPrivateDeployment() && <div className={styles.caption}>{t(Strings.official_account_qrcode)}</div>}
                  </div>
                </Space>
              </div>
            }
          </div>
          <div className={styles.homeRight}>
            <span className={styles.backgroundRight}>
              <Image src={BackgroundRight} alt='background right' layout={'fill'} objectFit={'contain'} />
            </span>

            <div className={styles.nav}>
              {
                !isRenderServer() && <ButtonGroup withSeparate>
                  {!isPrivateDeployment() && (
                    <>
                      {
                        env.PRIVATE_DEVELOPMENT_FORM && <LinkButton
                          component='button'
                          underline={false}
                          style={{ color: lightColors.black[500] }}
                        >
                          <a href={env.PRIVATE_DEVELOPMENT_FORM} target='_blank' style={{ color: 'inherit' }} rel='noreferrer'>
                            {t(Strings.self_hosting)}
                          </a>
                        </LinkButton>
                      }
                      <LinkButton
                        component='button'
                        underline={false}
                        style={{ color: lightColors.black[500] }}
                      >
                        <a href={'/chatgroup/'} target='_blank' style={{ color: 'inherit' }} rel='noreferrer'>{t(Strings.feedback)}</a>
                      </LinkButton>
                    </>
                  )}
                  <LinkButton
                    component='button'
                    underline={false}
                    style={{ color: lightColors.black[500] }}
                  >
                    <a href='/help/' target='_blank' style={{ color: 'inherit' }}>{t(Strings.support)}</a>
                  </LinkButton>
                  <LinkButton
                    component='button'
                    underline={false}
                    onClick={jumpOfficialWebsite}
                    style={{ color: lightColors.black[500] }}
                  >
                    {t(Strings.enter_official_website)}
                  </LinkButton>
                </ButtonGroup>
              }
            </div>
            <div className={styles.homeContent}>
              <div className={styles.loginContainer} id={AutoTestID.LOGIN}>
                {isWecom || isWechatFunc() || isDingtalkFunc() || isQQFunc() || isLarkFunc() || isMobileApp() || isPrivateDeployment()
                  ? <Login />
                  : <LoginToggle />
                }
              </div>
              <div className={styles.templateBtn} onClick={() => Router.newTab(Navigation.TEMPLATE)}>
                <PlanetOutlined />{t(Strings.massive_template)}
              </div>
            </div>
            {
              !isRenderServer() && <div className={styles.icp}>
                <LinkButton className={styles.icpBtn} href={Settings['login_icp1_url'].value} underline={false} color={lightColors.black[500]}
                  target='_blank'>
                  {t(Strings.icp1)}
                </LinkButton>
                <div className={styles.line} />
                <LinkButton className={styles.icpBtn} href={Settings['login_icp2_url'].value} underline={false} color={lightColors.black[500]}
                  target='_blank'>
                  {t(Strings.icp2)}
                </LinkButton>
              </div>
            }
          </div>
        </>
      }
    </>

  );
};

export default PcHome;
