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
import { useMemo } from 'react';
import * as React from 'react';
import { useSelector } from 'react-redux';
import { Tooltip, Typography, useThemeColors } from '@apitable/components';
import { ISocialAppType, isPrivateDeployment, SocialAppType, Strings, t } from '@apitable/core';
import { getEnvVariables, isMobileApp } from 'pc/utils/env';
import styles from './styles.module.less';

export enum SubscribeGrade {
  Silver = 'Silver',
  Gold = 'Gold',
  Enterprise = 'Enterprise',
  Atlas = 'Atlas'
}

interface ISubscribeLabelProps {
  grade: SubscribeGrade;
}

export const labelMap = {
  [SubscribeGrade.Silver]: (appType?: ISocialAppType) => {
    if(getEnvVariables().IS_APITABLE){
      return t(Strings.subscribe_grade_plus);
    }
    if (appType === SocialAppType.ThirdPartyAtt) {
      return t(Strings.standard);
    }
    return t(Strings.silver);
  },
  [SubscribeGrade.Gold]: (appType?: ISocialAppType, product?: SubscribeGrade) => {
    if(getEnvVariables().IS_APITABLE){
      return t(Strings.subscribe_grade_pro);
    }
    if (appType === SocialAppType.ThirdPartyAtt) {
      return t(Strings.profession);
    }
    if (product === SubscribeGrade.Atlas) {
      return t(Strings.atlas);
    }
    return t(Strings.gold);
  },
  [SubscribeGrade.Enterprise]: (appType?: ISocialAppType) => {
    if (appType === SocialAppType.ThirdPartyAtt) {
      return t(Strings.enterprise_third_app);
    }
    return t(Strings.enterprise);
  },
};

export const SubscribeLabel: React.FC<ISubscribeLabelProps> = (props) => {
  const colors = useThemeColors();
  const { grade } = props;
  const { spaceInfo } = useSelector(state => {
    return {
      spaceInfo: state.space.curSpaceInfo,
    };
  });
  const product = useSelector(state => state.billing.subscription?.product);
  const social = spaceInfo?.social;

  const innerText = useMemo(() => {
    const gradeName = labelMap[grade](social?.appType, product);
    if (grade === SubscribeGrade.Silver) {
      return <Typography
        variant={'body4'} className={classnames(styles.baseLabel, styles.gradeSilver)} component={'span'} color={colors.rainbowIndigo4}>
        {gradeName}
      </Typography>;
    }

    if (grade === SubscribeGrade.Gold) {
      return <Typography
        variant={'body4'}
        className={classnames(styles.baseLabel, styles.gradeGold)} component={'span'} color={colors.rainbowOrange5}>
        {gradeName}
      </Typography>;
    }

    return <Typography variant={'body4'} className={classnames(styles.baseLabel, styles.gradeEnterprise)} component={'span'}
      color={colors.rainbowIndigo5}>
      {gradeName}
    </Typography>;
  }, [grade, social?.appType, product, colors.rainbowIndigo5, colors.rainbowIndigo4, colors.rainbowOrange5]);

  if (isPrivateDeployment() || isMobileApp()) {
    return null;
  }

  return <Tooltip content={t(Strings.subscribe_label_tooltip, { grade: labelMap[grade](social?.appType) })}>
    <span style={{ marginLeft: 4 }}>
      {innerText}
    </span>
  </Tooltip>;
};
