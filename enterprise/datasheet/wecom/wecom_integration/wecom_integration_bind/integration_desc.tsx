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
import { useEffect, useState } from 'react';
import * as React from 'react';
import { Button, Checkbox } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import styles from './styles.module.less';

interface IIntegrationDescProps {
  nextStep: () => void;
}
export const IntegrationDesc: React.FC<IIntegrationDescProps> = ({ nextStep }) => {
  const [countDown, setCountDown] = useState<number>(5);
  const [cdInterval, setCdInterval] = useState<NodeJS.Timeout | null>(null);
  const [checkbox, setCheckbox] = useState<boolean>();
  useEffect(() => {
    const interval = setInterval(() => {
      if (countDown <= 0) {
        cdInterval && clearInterval(cdInterval);
        return;
      }
      setCountDown(countDown - 1);
    }, 1000);
    setCdInterval(interval);
    return () => clearInterval(interval);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [nextStep, countDown]);
  return (
    <div className={styles.integrationDesc}>
      <div className={styles.integrationDescTitle}>
        {t(Strings.welcome_use)}{t(Strings.system_configuration_company_name_short)} ×️ {t(Strings.marketplace_integration_app_name_wechatcp)}
      </div>
      <div
        className={classNames(
          styles.integrationDescContent,
          styles.mb24
        )}
        dangerouslySetInnerHTML={{ __html: t(Strings.integration_app_wecom_desc) }}
      />
      <div className={styles.integrationDescTitle}>{t(Strings.matters_needing_attention)}</div>
      <div
        className={styles.integrationDescContent}
        dangerouslySetInnerHTML={{ __html: t(Strings.integration_app_wecom_matters) }}
      />
      <div className={classNames(styles.buttonWrap, styles.buttonCheckboxWrap)}>
        <div className={styles.checkboxWrap}>
          <Checkbox checked={checkbox} onChange={() => setCheckbox(!checkbox)} >
            <span className={styles.checkboxText}>{t(Strings.wecom_integration_desc_check)}</span>
          </Checkbox>
        </div>
        <Button color="primary" size="middle" onClick={nextStep} block disabled={countDown > 0 || !checkbox}>
          {countDown > 0 ? `（${countDown}s）${t(Strings.please_read_carefully)}` : t(Strings.start_onfiguration)}
        </Button>
      </div>
    </div>
  );
};
