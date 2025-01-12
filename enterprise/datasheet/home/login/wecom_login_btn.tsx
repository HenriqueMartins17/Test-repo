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

import { FC } from 'react';
import { useSelector } from 'react-redux';
import { Strings, t } from '@apitable/core';
import { WecomFilled } from '@apitable/icons';
import { useQuery } from 'pc/hooks';
// @ts-ignore
import { wecomLogin, wecomQuickLogin } from 'enterprise/login/other_login/other_login';
import styles from './style.module.less';

export const WecomLoginBtn: FC = () => {
  const isWecomDomain = useSelector(state => state.space.envs?.weComEnv?.enabled);
  const query = useQuery();
  return (
    <button className={styles.wecomLoginBtn} onClick={() => {
      isWecomDomain ? wecomLogin?.() : wecomQuickLogin?.('snsapi_base', query.get('reference'));
    }}>
      <WecomFilled size={20}/>
      <span>{isWecomDomain ? t(Strings.wecom_login_btn_text) : t(Strings.wecom_login)}</span>
    </button>
  );
};