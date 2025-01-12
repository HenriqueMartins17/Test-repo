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

import { useUnmount } from 'ahooks';
import React, { useEffect } from 'react';
import { useSelector } from 'react-redux';
import { IReduxState } from '@apitable/core';
import { isSocialWecom } from '../home';
import { useSpaceWatermark } from './use_watermark';

export const WatermarkWrapper = (props: any) => {
  const { unitTitle, children } = props;
  const spaceInfo = useSelector(state => state.space.curSpaceInfo);
  const watermarkEnable = useSelector((state: IReduxState) => state.space.spaceFeatures?.watermarkEnable);
  const { initSpaceWM, removeSpaceWM } = useSpaceWatermark({ manual: true, watermark_txt: unitTitle });

  const checkWx = isSocialWecom(spaceInfo) ? Boolean(unitTitle) : true;

  useEffect(() => {
    if (watermarkEnable && checkWx) {
      initSpaceWM();
    } else if (!watermarkEnable) {
      removeSpaceWM();
    }
  }, [watermarkEnable, initSpaceWM, removeSpaceWM, spaceInfo, checkWx]);

  useUnmount(() => {
    removeSpaceWM();
  });

  return (
    <>{children}</>
  );
};