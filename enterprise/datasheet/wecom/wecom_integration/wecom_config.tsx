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

import * as React from 'react';
import { useEffect, useState } from 'react';
import { Loading, Message } from '@apitable/components';
import { Api, Strings, t } from '@apitable/core';
import { InfoCircleFilled } from '@apitable/icons';
import { useRequest, useUserRequest } from 'pc/hooks';
import { FormItem } from './components/form_item';
import { CopyButton } from './wecom_integration_bind/create_application';
import styles from './styles.module.less';

interface IFormData {
  corpId: string;
  agentId: number;
  agentSecret: string;
  domainName: string;
  appHomepageUrl: string
}

export const WecomConfig: React.FC = () => {
  const [formData, setFormData] = useState<IFormData>();
  const { getLoginStatusReq } = useUserRequest();
  const { loading: isLoginStatusGetting } = useRequest(getLoginStatusReq);

  useEffect(() => {
    if (isLoginStatusGetting) {
      return;
    }
    Api.socialWecomGetConfig().then(res => {
      const { success, message, data } = res.data;
      if (success && data) {
        setFormData({
          corpId: data.corpId,
          agentId: data.agentId,
          agentSecret: data.agentSecret,
          domainName: data.domainName,
          appHomepageUrl: `https://${data.domainName}/user/wecom_callback?corpId=${data.corpId}&agentId=${data.agentId}`
        });
        return;
      }
      Message.error({ content: message });
    });
  }, [isLoginStatusGetting]);

  if (isLoginStatusGetting) {
    return <Loading />;
  }

  const schema1 = {
    domainName: {
      label: t(Strings.integration_app_wecom_config_item1_title),
      readonly: true,
      suffix: CopyButton(formData?.domainName)
    },
  };

  const schema2 = {
    corpId: {
      label: t(Strings.integration_app_wecom_form1_item1_label),
      readonly: true
    },
    agentId: {
      label: t(Strings.integration_app_wecom_form1_item2_label),
      readonly: true
    },
    agentSecret: {
      label: t(Strings.integration_app_wecom_form1_item3_label),
      readonly: true
    },
    appHomepageUrl: {
      label: t(Strings.integration_app_wecom_form2_item1_label),
      readonly: true,
      suffix: CopyButton(formData?.appHomepageUrl)
    },
    domainName: {
      label: t(Strings.integration_app_wecom_form2_item2_label),
      readonly: true,
      suffix: CopyButton(formData?.domainName)
    }
  };
  return (
    <>
      <div className={styles.wecomConfig}>
        <div className={styles.configTitle}>
          {`${t(Strings.application_integration_information)}-${t(Strings.marketplace_integration_app_name_wechatcp)}`}
        </div>
        <div className={styles.configTips}>
          <InfoCircleFilled size={20} />
          <span>{t(Strings.integration_app_wecom_config_tips)}</span>
        </div>
        <div className={styles.infoWrap}>
          <div className={styles.infoTitle}>{t(Strings.integration_app_wecom_config_item1_title)}</div>
          <div className={styles.infoDesc}>{t(Strings.integration_app_wecom_config_item1_desc)}</div>
          {
            Object.keys(schema1).map(key => (
              <FormItem key={key} formData={formData || {}} formItem={{ ...schema1[key], key }}/>
            ))
          }
        </div>
        <div className={styles.infoWrap}>
          <div className={styles.infoTitle}>{t(Strings.integration_app_wecom_config_item2_title)}</div>
          {
            Object.keys(schema2).map(key => (
              <FormItem key={key} formData={formData || {}} formItem={{ ...schema2[key], key }}/>
            ))
          }
        </div>
      </div>
    </>
  );
};
