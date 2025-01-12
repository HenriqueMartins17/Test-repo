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

import { StaticImageData } from 'next/image';
import { ReactNode } from 'react';
import { colorVars } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { EnterpriseDarkFilled, EnterpriseLightFilled, GoldDarkFilled, GoldLightFilled, SilverDarkFilled, SilverLightFilled } from '@apitable/icons';
import { ThemeIcon } from 'pc/components/common/theme_icon/theme_icon';
import EnterPrise from 'static/icon/enterprise_bj@2x.png';
import GoldImg from 'static/icon/gold_banner@2x.png';
import SilverImg from 'static/icon/silver_bj@2x.png';

export interface ILevelInfo {
  level: string;
  levelName: string;
  levelPowerTitle: string;
  headBgSrc: ReactNode | StaticImageData;
  levelIcon: ReactNode;
  activeLevelNameColor: string;
  normalLevelNameColor: string;
  cardSelectBg: string;
  rightFeatureListBg: string;
  seatNums: number[];
  activeColor: string;
  levelDesc: string[];
}

export const paySystemConfig = {
  SILVER: {
    level: 'SILVER',
    levelName: t(Strings.plan_model_choose_space_level, { space_level: t(Strings.silver) }),
    levelPowerTitle: t(Strings.plan_model_benefits_title, { space_level: t(Strings.silver) }),
    headBgSrc: SilverImg,
    levelIcon: <ThemeIcon darkIcon={<SilverDarkFilled size={20} />} lightIcon={<SilverLightFilled size={20} />} />,
    activeLevelNameColor: colorVars.indigo[400],
    normalLevelNameColor: '#636363', // No theme switching, fixed color values
    activeColor: colorVars.indigo[400],
    cardSelectBg: colorVars.extraLightIndigo,
    rightFeatureListBg: colorVars.extraLightIndigo,
    seatNums: [2, 5, 10, 20, 30, 40, 50],
    levelDesc: t(Strings.plan_model_benefits_sliver)
      .split(';')
      .filter((item) => Boolean(item)),
  },
  GOLD: {
    level: 'GOLD',
    levelName: t(Strings.plan_model_choose_space_level, { space_level: t(Strings.gold) }),
    levelPowerTitle: t(Strings.plan_model_benefits_title, { space_level: t(Strings.gold) }),
    headBgSrc: GoldImg,
    levelIcon: <ThemeIcon darkIcon={<GoldDarkFilled size={20} />} lightIcon={<GoldLightFilled size={20} />} />,
    activeLevelNameColor: colorVars.orange[500],
    normalLevelNameColor: '#636363',
    cardSelectBg: colorVars.extraLightOrange,
    rightFeatureListBg: colorVars.extraLightOrange,
    seatNums: [2, 5, 10, 20, 30, 40, 50],
    activeColor: colorVars.orange[500],
    levelDesc: t(Strings.plan_model_benefits_gold)
      .split(';')
      .filter((item) => Boolean(item)),
  },
  ENTERPRISE: {
    level: 'ENTERPRISE',
    levelName: t(Strings.plan_model_choose_space_level, { space_level: t(Strings.enterprise) }),
    levelPowerTitle: t(Strings.plan_model_benefits_title, { space_level: t(Strings.enterprise) }),
    headBgSrc: EnterPrise,
    levelIcon: <ThemeIcon darkIcon={<EnterpriseDarkFilled size={20} />} lightIcon={<EnterpriseLightFilled size={20} />} />,
    cardSelectBg: colorVars.extraLightOrange,
    rightFeatureListBg: colorVars.extraLightOrange,
    activeLevelNameColor: colorVars.indigo[600],
    normalLevelNameColor: '#636363',
    seatNums: [2, 5, 10, 20, 30, 40, 50],
    activeColor: colorVars.indigo[600],
    levelDesc: [
      '彩虹标签高级功能',
      '彩虹标签高级功能',
      '彩虹标签高级功能',
      '彩虹标签高级功能',
      '彩虹标签高级功能',
      '彩虹标签高级功能',
      '彩虹标签高级功能',
      '彩虹标签高级功能',
    ],
  },
};

export const monthMap = {
  1: t(Strings.one_month),
  6: t(Strings.six_months),
  12: t(Strings.one_year),
};

export enum SubscribePageType {
  Subscribe,
  Renewal,
  Upgrade,
}
