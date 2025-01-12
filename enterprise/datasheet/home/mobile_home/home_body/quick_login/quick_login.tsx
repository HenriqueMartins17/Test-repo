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
import classnames from 'classnames';
import Image from 'next/image';
import React, { FC } from 'react';
import { useSelector } from 'react-redux';
import { getCustomConfig, HomeId, Navigation, Strings, t } from '@apitable/core';
import { DingdingFilled, FeishuFilled, QqFilled, WechatFilled, WecomColorFilled, MobilephoneOutlined, PlanetOutlined } from '@apitable/icons';
import { Router } from 'pc/components/route_manager/router';
import { useQuery } from 'pc/hooks';
import WelcomePng from 'static/icon/datasheet/datasheet_img_welcome.png';
import { dingdingLogin, feishuLogin, qqLogin, wechatLogin, wecomLogin, wecomQuickLogin } from '../../../../login/other_login';
import { isDingtalkFunc, isLarkFunc, isQQFunc, isWechatFunc, isWecomFunc } from '../../../social_platform/utils';
import styles from './style.module.less';

export const QuickLogin: FC = () => {
  const query = useQuery();
  const isWecomDomain = useSelector((state) => state.space.envs?.weComEnv?.enabled);
  const changeMobileMode = () => {
    const searchParams = new URLSearchParams(window.location.search);
    const queryObj = {
      quickLogin: 'off',
    };
    for (const pair of searchParams.entries()) {
      queryObj[pair[0]] = pair[1];
    }
    Router.push(Navigation.LOGIN, { query: queryObj });
  };

  const renderQuickLoginBtn = () => {
    if (isWecomFunc()) {
      return (
        <div
          className={classnames(styles.btn, styles.wecom)}
          onClick={() => (isWecomDomain ? wecomLogin() : wecomQuickLogin('snsapi_base', query.get('reference')))}
        >
          <WecomColorFilled />
          <span className={styles.text}>{t(Strings.wecom_login)}</span>
        </div>
      );
    } else if (isWechatFunc()) {
      return (
        <div className={classnames(styles.btn, styles.wechat)} onClick={() => wechatLogin()} id={HomeId.LOGIN_TYPE_WECHAT}>
          <WechatFilled />
          <span className={styles.text}>{t(Strings.wechat_login)}</span>
        </div>
      );
    } else if (isDingtalkFunc()) {
      return (
        <div className={classnames(styles.btn, styles.dingding)} onClick={() => dingdingLogin()} id={HomeId.LOGIN_TYPE_DINGDING}>
          <DingdingFilled />
          <span className={styles.text}>{t(Strings.dingding_login)}</span>
        </div>
      );
    } else if (isQQFunc()) {
      return (
        <div className={classnames(styles.btn, styles.qq)} onClick={() => qqLogin()} id={HomeId.LOGIN_TYPE_QQ}>
          <QqFilled />
          <span className={styles.text}>{t(Strings.qq)}</span>
        </div>
      );
    } else if (isLarkFunc()) {
      return (
        <div className={classnames(styles.btn, styles.lark)} onClick={() => feishuLogin()} id={HomeId.LOGIN_TYPE_LARK}>
          <FeishuFilled />
          <span className={styles.text}>{t(Strings.lark_login)}</span>
        </div>
      );
    }
    return (
      <div className={classnames(styles.btn, styles.wechat)} onClick={() => wechatLogin()} id={HomeId.LOGIN_TYPE_WECHAT}>
        <WechatFilled />
        <span className={styles.text}>{t(Strings.wechat_login)}</span>
      </div>
    );
  };

  const { slogan } = getCustomConfig();

  return (
    <div className={styles.quickLogin}>
      <div className={styles.main}>
        <div className={styles.slogan}>{slogan || t(Strings.login_slogan)}</div>
        <div className={styles.subSlogan}>{t(Strings.login_sub_slogan)}</div>
        <div className={styles.welcomePng}>
          <Image src={WelcomePng} alt="Welcome" width="320" height="240" />
        </div>
        <Space className={styles.btnGroup} direction="vertical" size={16}>
          {renderQuickLoginBtn()}
          <div className={classnames(styles.btn, styles.mobileAndAccount)} onClick={changeMobileMode}>
            <MobilephoneOutlined />
            <span className={styles.text}>{t(Strings.more_login_mode)}</span>
          </div>
        </Space>
      </div>
      <span className={styles.templateBtn} onClick={() => Router.push(Navigation.TEMPLATE)}>
        <PlanetOutlined />
        {t(Strings.massive_template)}
      </span>
    </div>
  );
};
