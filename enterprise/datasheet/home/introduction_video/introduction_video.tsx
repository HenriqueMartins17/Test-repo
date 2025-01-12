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
import { FC } from 'react';
import { integrateCdnHost, Strings, t } from '@apitable/core';
import { getEnvVariables } from 'pc/utils/env';
import ApiImg from 'static/icon/signin/signin_img_api.png';
import FormImg from 'static/icon/signin/signin_img_form.png';
import styles from './style.module.less';

export const IntroductionVideo: FC = () => {
  const env = getEnvVariables();
  const videoSrc = integrateCdnHost(env.LOGIN_INTRODUCTION_VIDEO || '');
  if (!videoSrc) {
    return null;
  }
  return (
    <div className={styles.introductionVideo}>
      <div className={styles.titleBar}>
        <Space className={styles.dots} size={10} align='center'>
          <div className={styles.whiteDot} />
          <div className={styles.transparentDot} />
          <div className={styles.whiteDot} />
        </Space>
      </div>
      <div className={styles.videoWrapper}>
        <video controls>
          <source src={videoSrc} type='video/mp4' />
          {t(Strings.nonsupport_video)}
        </video>
      </div>
      <span className={styles.apiImg}>
        <Image width={40} height={40} src={ApiImg} alt='api' />
      </span>
      <span className={styles.formImg}>
        <Image width={40} height={40} src={FormImg} alt='form' />
      </span>
    </div>
  );
};
