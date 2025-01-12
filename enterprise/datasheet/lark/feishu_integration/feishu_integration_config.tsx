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

import { useRouter } from 'next/router';
import * as React from 'react';
import { useEffect, useState } from 'react';
import { Loading, Message, useThemeColors } from '@apitable/components';
import { Api, Strings, t } from '@apitable/core';
import { InfoCircleFilled } from '@apitable/icons';
import { useRequest, useUserRequest } from 'pc/hooks';
import { copyButton } from './detail/create_application';
// @ts-ignore
import { FormItem as WecomFormItem } from 'enterprise/wecom/wecom_integration/components/form_item/form_item';
import styles from './styles.module.less';

export const defaultFieldConfig = {
  appId: '',
  appSecret: '',
  pcUrl: '',
  mobileUrl: '',
  redirectUrl: '',
  encryptKey: '',
  verificationToken: '',
  eventUrl: '',
};

const FeishuConfig: React.FC = () => {
  const [formData, setFormData] = useState({ ...defaultFieldConfig });
  const router = useRouter();
  const { appInstanceId } = router.query as { appInstanceId: string };

  const { getLoginStatusReq } = useUserRequest();
  const { loading: isLoginStatusGetting } = useRequest(getLoginStatusReq);
  const colors = useThemeColors();

  useEffect(() => {
    if (isLoginStatusGetting || !appInstanceId) {
      return;
    }
    Api.getAppInstanceById(appInstanceId).then(res => {
      const { success, message, data } = res.data;
      if (success && data) {
        const { profile } = data.config;
        setFormData({
          ...profile,
          appId: profile.appKey,
          encryptKey: profile.eventEncryptKey,
          verificationToken: profile.eventVerificationToken,
        });
        return;
      }
      Message.error({ content: message });
    });
  }, [isLoginStatusGetting, appInstanceId]);

  if (isLoginStatusGetting) {
    return <Loading />;
  }

  const schema2 = {
    appId: {
      label: t(Strings.lark_integration_step2_appid),
      readonly: true,
    },
    appSecret: {
      label: t(Strings.lark_integration_step2_appsecret),
      readonly: true,
    },
    encryptKey: {
      label: t(Strings.lark_integration_step4_encryptkey),
      readonly: true,
    },
    verificationToken: {
      label: t(Strings.lark_integration_step4_verificationtoken),
      readonly: true,
    },
    pcUrl: {
      label: t(Strings.lark_integration_step3_desktop),
      readonly: true,
      suffix: copyButton(formData.pcUrl, colors),
    },
    mobileUrl: {
      label: t(Strings.lark_integration_step3_mobile),
      readonly: true,
      suffix: copyButton(formData.mobileUrl, colors),
    },
    redirectUrl: {
      label: t(Strings.lark_integration_step3_redirect),
      readonly: true,
      suffix: copyButton(formData.redirectUrl, colors),
    },
  };
  return (
    <>
      <div className={styles.feishuConfig}>
        <div className={styles.configTitle}>
          {`${t(Strings.application_integration_information)}-${t(Strings.marketplace_integration_app_name_feishu)}`}
        </div>
        <div className={styles.configTips}>
          <InfoCircleFilled size={20} />
          <span>{t(Strings.lark_integration_config_tip)}</span>
        </div>
        <div className={styles.infoWrap}>
          <div className={styles.infoTitle}>{t(Strings.lark_integration_config_title)}</div>
          {Object.keys(schema2).map(key => (
            <WecomFormItem key={key} formData={formData || {}} formItem={{ ...schema2[key], key }} />
          ))}
        </div>
      </div>
    </>
  );
};

export default FeishuConfig;
