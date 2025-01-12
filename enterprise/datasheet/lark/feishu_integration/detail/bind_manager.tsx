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

import classNames from 'classnames';
import { useEffect, useRef, useState } from 'react';
import * as React from 'react';
import { Loading, Message } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { IFeishuConfigParams } from '../interface';
// @ts-ignore
import { IScriptRef, Script } from 'enterprise/wecom/wecom_integration/components/srcipt/script';
import styles from './styles.module.less';

enum STATUS {
  Scan = 1,
  Success = 2,
  Error = 3,
}

interface IBindManage {
  config: IFeishuConfigParams;
  step: number;
}

export const BindManager: React.FC<IBindManage> = props => {
  const { config, step } = props;
  const [status, setStatus] = useState<STATUS>(STATUS.Scan);
  const [isLoadingScript, setIsLoadingScript] = useState(true);
  const scriptRef = useRef<IScriptRef>(null);

  useEffect(() => {
    setStatus(STATUS.Scan);
  }, []);

  useEffect(() => {
    if (isLoadingScript || !config.appId || step !== 5) {
      return;
    }
    const appId = config.appId;
    const redirectUrl = encodeURIComponent(config.redirectUrl);
    const id = 'lark_integration_qr_code';
    const goto = `https://passport.feishu.cn/suite/passport/oauth/authorize?client_id=${appId}&redirect_uri=${redirectUrl}&response_type=code&state=adminScan`;
    const qrLoginObj = QRLogin({
      id,
      goto,
      width: '250',
      height: '257',
      style: 'outline: none; border: none;',
    });
    const handleMessage = (event: MessageEvent) => {
      const origin = event.origin;
      if (qrLoginObj.matchOrigin(origin)) {
        const loginTmpCode = event.data;
        window.location.href = `${goto}&tmp_code=${loginTmpCode}`;
      }
    };
    window.addEventListener('message', handleMessage, false);
    return () => {
      window.removeEventListener('message', handleMessage, false);
    };
  }, [isLoadingScript, config.appId, config.redirectUrl, step]);

  const renderScan = () => {
    if (isLoadingScript) {
      return <Loading />;
    }
    return (
      <div className={styles.bindManagerScanArea}>
        <div className={styles.bindManagerTitle}>{t(Strings.lark_integration_step6_title)}</div>
        <div className={classNames(styles.bindManagerDesc)}>
          <p>{t(Strings.lark_integration_step6_content)}</p>
        </div>
        {step === 5 && <div id="lark_integration_qr_code" className={styles.larkIntegrationQrCode} />}
      </div>
    );
  };

  return (
    <div className={styles.bindManager}>
      {status === STATUS.Scan && renderScan()}
      <Script
        ref={scriptRef}
        src="https://sf3-cn.feishucdn.com/obj/static/lark/passport/qrcode/LarkSSOSDKWebQRCode-1.0.1.js"
        onload={() => {
          setIsLoadingScript(false);
        }}
        onerror={() => {
          scriptRef?.current?.reload(() => {
            Message.error({ content: t(Strings.something_went_wrong) });
          });
        }}
      />
    </div>
  );
};
