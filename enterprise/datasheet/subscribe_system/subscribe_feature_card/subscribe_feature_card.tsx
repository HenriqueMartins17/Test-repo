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
import { Button, Typography, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { CheckOutlined, Star2Filled } from '@apitable/icons';
import { ILevelInfo } from '../config';
import styles from '../styles.module.less';

interface ISubscribeFeatureCardProps {
  levelInfo: ILevelInfo;
}

export const SubscribeFeatureCard: React.FC<ISubscribeFeatureCardProps> = (props) => {
  const { levelInfo } = props;
  const colors = useThemeColors();

  return <div className={styles.rightFeatureList} style={{ background: levelInfo.rightFeatureListBg }}>
    <p className={styles.header}>
      <Star2Filled color={levelInfo.activeColor} />
      <Typography variant={'body2'} className={styles.text}>
        {levelInfo.levelPowerTitle}
      </Typography>
    </p>
    {
      levelInfo.levelDesc.map(item => {
        return <p key={item} className={styles.item}>
          <CheckOutlined color={levelInfo.activeColor} />
          <Typography variant={'body3'} color={colors.fc2} className={styles.text}>
            {item}
          </Typography>
        </p>;
      })
    }
    <Button
      color={colors.defaultBg}
      block
      className={styles.featureButton}
      onClick={() => {
        window.open('/pricing', '_blank', 'noopener,noreferrer');
      }}
    >
      <Typography variant={'body2'} color={colors.fc2}>
        {t(Strings.plan_model_benefits_button)}
      </Typography>
    </Button>
  </div>;
};
