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

import React, { useEffect, useState, FC } from 'react';
import { useSelector } from 'react-redux';
import { Loading } from '@apitable/components';
import { integrateCdnHost, Settings, t, Strings } from '@apitable/core';
import { useQuery } from 'pc/hooks';
import { getWecomConfig } from '../../login/utils';
import { Script } from '../wecom_integration/components/srcipt';
import styles from './styles.module.less';

export const WecomQrCode: FC = () => {
  const [isLoadingScript, setIsLoadingScript] = useState(true);
  const query = useQuery();

  const { callbackUrl } = getWecomConfig();

  const { agentId, corpId } = useSelector(state => state.space.envs?.weComEnv || { agentId: query.get('agentId'), corpId: query.get('corpId') });

  useEffect(() => {
    if (!isLoadingScript && corpId && agentId) {
      new WwLogin({
        id : 'wecomLoginQrCode',
        appid : corpId,
        agentid : agentId,
        redirect_uri: encodeURIComponent(
          // eslint-disable-next-line max-len
          `${callbackUrl}?agentId=${agentId}&corpId=${corpId}`
        ),
        href: integrateCdnHost(Settings.integration_wecom_qrcode_css.value)
      });
    }
  }, [isLoadingScript, corpId, agentId, callbackUrl]);

  return (
    <div className={styles.wecomQrCode}>
      <div id="wecomLoginQrCode" className={styles.wecomQrCodeWrap}>
        { isLoadingScript && <Loading />}
      </div>
      <div className={styles.wecomQrCodeTips}>{t(Strings.wecom_login_btn_text)}</div>
      <Script
        src="https://wwcdn.weixin.qq.com/node/wework/wwopen/js/wwLogin-1.2.4.js"
        onload={() => {
          setIsLoadingScript(false);
        }}
      />
    </div>
  );
};
