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
import { FC, useRef } from 'react';
import { LinkButton, useThemeColors } from '@apitable/components';
import { integrateCdnHost, Settings, Strings, t } from '@apitable/core';
import { useQuery } from 'pc/hooks';
import { getEnvVariables } from 'pc/utils/env';
// import OfficialAccount from 'static/icon/signin/signin_img_officialaccounts.png';
import videoPoster from 'static/icon/signin/signin_img_phone_cover.png';
import SlidingIcon from 'static/icon/signin/signin_img_phone_sliding.svg';
import { isDingtalkFunc, isLarkFunc, isQQFunc, isWechatFunc, isWecomFunc } from '../social_platform';
import { MobileAndAccountLogin } from './home_body/mobile_and_account_login';
import { QuickLogin } from './home_body/quick_login';
import { HomeFooter } from './home_footer';
import { HomeHeader } from './home_header';
import styles from './style.module.less';

export const MobileHome: FC = () => {
  const env = getEnvVariables();
  const videoSrc = integrateCdnHost(env.LOGIN_INTRODUCTION_VIDEO || '');
  const colors = useThemeColors();
  const query = useQuery();
  const quickLogin = query.get('quickLogin') || 'on';
  const secondPageRef = useRef<HTMLDivElement>(null);
  const isQuickLogin = isWechatFunc() || isDingtalkFunc() || isQQFunc() || isLarkFunc() || isWecomFunc();

  const clickHandler = () => {
    const secondPageElement = secondPageRef.current;
    if (!secondPageElement) {
      return;
    }
    secondPageElement.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <div className={styles.mobileAndAccount}>
      <div className={styles.container}>
        <div className={styles.firstPage}>
          <div className={styles.top}>
            <HomeHeader />
            <div className={styles.loginContainer}>{(isQuickLogin && quickLogin === 'on') ? <QuickLogin /> : <MobileAndAccountLogin />}</div>
            <div className={styles.bottom}>
              {<HomeFooter />}
              <SlidingIcon className={styles.slidingBtn} onClick={clickHandler} />
            </div>
          </div>
        </div>
        <div className={styles.secondPage} ref={secondPageRef}>
          {videoSrc && (
            <video className={styles.video} controls poster={videoPoster as any as string}>
              <source src={videoSrc} type="video/mp4" />
              {t(Strings.nonsupport_video)}
            </video>
          )}
          <div className={styles.qrCodeWrapper}>
            <Space size={48} align="center" className={styles.qrCodeGroup}>
              <div className={styles.qrCode}>
                <Image src={integrateCdnHost(getEnvVariables().LOGIN_WECHAT_GROUP_QR_CODE!)} alt="CommunicationGroup Code" width={88} height={88} />
                <div className={styles.caption}>{t(Strings.communication_group_qrcode)}</div>
              </div>
              <div className={styles.qrCode}>
                <Image src={integrateCdnHost(getEnvVariables().LOGIN_VIKA_QR_CODE!)} alt="OfficialAccount Code" width={88} height={88} />
                <div className={styles.caption}>{t(Strings.official_account_qrcode)}</div>
              </div>
            </Space>
          </div>
          <div className={styles.icp}>
            <LinkButton className={styles.icpBtn} href={Settings['login_icp1_url'].value} underline={false} color={colors.fc3} target="_blank">
              {t(Strings.icp1)}
            </LinkButton>
            <div className={styles.line} />
            <LinkButton className={styles.icpBtn} href={Settings['login_icp2_url'].value} underline={false} color={colors.fc3} target="_blank">
              {t(Strings.icp2)}
            </LinkButton>
          </div>
        </div>
      </div>
    </div>
  );
};
