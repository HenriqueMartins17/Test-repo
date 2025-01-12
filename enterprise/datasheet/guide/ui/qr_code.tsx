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

import QRCode from 'qrcode';
import { FC, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { Message } from '@apitable/components';
import { ConfigConstant, integrateCdnHost, IReduxState } from '@apitable/core';
import { getEnvVariables } from 'pc/utils/env';
import { isDingtalkFunc, isLarkFunc, isSocialPlatformEnabled, isWecomFunc } from 'enterprise/home/social_platform/utils';

export const ServiceQrCode: FC = () => {

  const spaceInfo = useSelector((state: IReduxState) => state.space.curSpaceInfo);

  // Get configuration of the customer service QR code
  const config = getEnvVariables().SYSTEM_CONFIGURATION_ERROR_MSG_QRCODE_IMG;
  const codeConfig = config ? JSON.parse(config) : { feishu: '', dingtalk: '', wecom: '', website: '' };
  const { feishu, dingtalk, wecom, website } = codeConfig;

  // Distinguish between platforms using different QR codes

  const isBindDingTalk = spaceInfo && isSocialPlatformEnabled(spaceInfo, ConfigConstant.SocialType.DINGTALK) || isDingtalkFunc();
  const isBindWecom = spaceInfo && isSocialPlatformEnabled(spaceInfo, ConfigConstant.SocialType.WECOM) || isWecomFunc();
  const isBindFeishu = spaceInfo && isSocialPlatformEnabled(spaceInfo, ConfigConstant.SocialType.FEISHU) || isLarkFunc();

  const platformImg = isBindDingTalk ? dingtalk : isBindWecom ? wecom : website;

  useEffect(() => {
    if (!isBindFeishu || !feishu) return;
    QRCode.toCanvas(feishu,
      {
        errorCorrectionLevel: 'H',
        margin: 1,
        width: 86,
      },
      (err, canvas) => {
        if (err) {
          Message.error({ content: 'Generate QrCode failed' });
        }
        const container = document.getElementById('shareQrCodeFeishu');
        if (container?.childNodes[0]) {
          container?.removeChild(container?.childNodes[0]);
        }
        container?.appendChild(canvas);
      });
  }, [isBindFeishu, feishu, spaceInfo]);

  return (
    <>
      {isBindFeishu ?
        <div id='shareQrCodeFeishu' />
        :
        <img src={integrateCdnHost(platformImg)} alt='' />
      }
    </>
  );
};
