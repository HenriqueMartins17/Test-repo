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

import classnames from 'classnames';
import Image from 'next/image';
import { Dispatch, FC, SetStateAction } from 'react';
import { shallowEqual, useSelector } from 'react-redux';
import { Button, ThemeName } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import UnauthorizedPngDark from 'static/icon/audit/set_enterprise_access_dark.png';
import UnauthorizedPngLight from 'static/icon/audit/set_enterprise_access_light.png';
// @ts-ignore
import { SubscribeUsageTipType, triggerUsageAlert } from 'enterprise/billing/trigger_usage_alert';
// @ts-ignore
import { labelMap, SubscribeGrade } from 'enterprise/subscribe_system/subscribe_label/subscribe_label';
import styles from './styles.module.less';

interface ITrialProps {
  setShowTrialModal: Dispatch<SetStateAction<boolean>>;
  title: string;
}

export const Trial: FC<ITrialProps> = ({ setShowTrialModal, title }) => {
  const spaceInfo = useSelector(state => state.space.curSpaceInfo);
  const subscription = useSelector(state => state.billing.subscription, shallowEqual);
  const social = spaceInfo?.social;
  const themeName = useSelector(state => state.theme);
  const UnauthorizedPng = themeName === ThemeName.Light ? UnauthorizedPngLight : UnauthorizedPngDark;
  const onTrial = () => {
    const result = triggerUsageAlert(
      'maxAuditQueryDays',
      { usage: subscription?.maxAuditQueryDays, grade: labelMap[SubscribeGrade.Enterprise](social?.appType), alwaysAlert: true },
      SubscribeUsageTipType.Alert,
    );

    if (result) return;

    setShowTrialModal(false);
  };

  return <div className={classnames([styles.logContainer, styles.logContainerUnauthorized])}>
    <div className={styles.unauthorizedBg}>
      <Image alt='' src={UnauthorizedPng} />
    </div>
    <h1 className={classnames([styles.unauthorizedTitle, styles.title])}>
      {title}
    </h1>
    <h2 className={styles.desc}>
      {t(Strings.space_log_trial_desc3)}
    </h2>
    <h2 className={styles.desc}>
      {t(Strings.space_log_trial_desc2)}
    </h2>
    <Button
      className={styles.trialButton}
      color='primary'
      onClick={onTrial}
    >
      {t(Strings.space_log_trial_button)}
    </Button>
  </div>;
};
