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

import { useMount } from 'ahooks';
import classNames from 'classnames';
import Image from 'next/image';
import QRCode from 'qrcode';
import { FC, PropsWithChildren, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Button, Message, Typography, useThemeColors } from '@apitable/components';
import { ConfigConstant } from '@apitable/core';
import {
  AdviseOutlined, BugOutlined, CloseOutlined, LikeOutlined, QuestionCircleOutlined, ServiceOutlined, SolutionSmallOutlined, StarFilled,
} from '@apitable/icons';
import { Modal as ModalBase } from 'pc/components/common/modal/modal/modal';
import { store } from 'pc/store';
import { getEnvVariables } from 'pc/utils/env';
import DatasheetQRCodeBg from 'static/icon/datasheet/share/qrcode/datasheet_img_qr_bj.png';
import { isDingtalkFunc, isLarkFunc, isSocialPlatformEnabled, isWecomFunc } from '../../home/social_platform/utils';

type IconType = 'BugOutlined' | 'AdviseOutlined' | 'ServiceOutlined' | 'LikeOutlined'
  | 'SolutionSmallOutlined' | 'QuestionCircleOutlined';

interface IInofo {
  title: string;
  description?: string;
  list?: string;
  tip: string;
  originUrl?: string;
  imgUrl?: string;
}

interface IQrcode {
  vikaby: string;
  questionnaire: string;
  tip: string;
}

interface IQuestionnaireIcon {
  title: string;
  icon: IconType;
}

export interface IGuideContactUsOptions {
  uiInfo: {
    vikaby: IInofo;
    questionnaire: IInofo;
    website: IQrcode;
    dingtalk: IQrcode;
    wecom: IQrcode;
    feishu: IInofo;
  };
  backdrop?: boolean;
  onClose?: () => void;
  confirmText?: string;
}

const mapIcon = {
  BugOutlined: <BugOutlined />,
  AdviseOutlined: <AdviseOutlined />,
  ServiceOutlined: <ServiceOutlined />,
  LikeOutlined: <LikeOutlined />,
  SolutionSmallOutlined: <SolutionSmallOutlined />,
  QuestionCircleOutlined: <QuestionCircleOutlined />
};

/*
 * Hoverball shows customer service
 *    vikaby(onclick) => TriggerCommands(open_guide_wizard) => redux =>
 *      current_guide_step(subscribe) => guid.showUiFromConfig => here
 * Newbie guide shows customer service
 *    guide.questionnaire => guid.showUiFromConfig => here
 */
const prefix = 'vika-qrcode';

const Modal: FC<PropsWithChildren<IGuideContactUsOptions>> = props => {
  const colors = useThemeColors();
  const { confirmText, backdrop = true, onClose, uiInfo } = props;
  const { vikaby, questionnaire, feishu, dingtalk, wecom, website } = uiInfo;
  const state = store.getState();
  const spaceInfo = state.space.curSpaceInfo;
  const [show, setShow] = useState(true);

  const isVikaby = true;
  /**
   * QR code first distinguish whether it is newbie guide jump or click customer service,
   * then distinguish platform, platform -> saas, nail, enterprise micro, fly book
   */
  // Third-party platforms do not distinguish between sweeps and clients, only space station attribution
  const isBindDingTalk = spaceInfo && isSocialPlatformEnabled(spaceInfo, ConfigConstant.SocialType.DINGTALK) || isDingtalkFunc();
  const isBindWecom = spaceInfo && isSocialPlatformEnabled(spaceInfo, ConfigConstant.SocialType.WECOM) || isWecomFunc();
  const isBindFeishu = spaceInfo && isSocialPlatformEnabled(spaceInfo, ConfigConstant.SocialType.FEISHU) || isLarkFunc();

  const platformImg = isBindDingTalk ? dingtalk : isBindWecom ? wecom : website;

  const finalClose = () => {
    setShow(false);
    onClose && onClose();
  };

  useMount(() => {
    if (!isBindFeishu || !feishu.originUrl) return;
    QRCode.toCanvas(feishu.originUrl,
      {
        errorCorrectionLevel: 'H',
        margin: 1,
        width: 184
      },
      (err, canvas) => {
        if (err) {
          Message.error({ content: 'Generate QrCode failed' });
        }
        const container = document.getElementById('shareQrCode');
        container?.appendChild(canvas);
      });
  });

  const renderOld = () => {
    const { title, imgUrl, tip, description } = feishu;
    return (
      <div className='vika-guide-connect-us-container'>
        <div className='vika-guide-connect-us-title'>{title}</div>
        <div className='vika-guide-connect-us-body'>
          <div
            className='vika-guide-connect-us-qrcode-wrapper'
            style={{ backgroundImage: `url(${DatasheetQRCodeBg})` }}
          >
            {imgUrl && <Image src={imgUrl} width={174} height={174} alt="" />}
            <div id='shareQrCode' className='vika-guide-connect-us-qrcode' />
          </div>
          {
            tip && <div className='vika-guide-connect-us-tip'>{tip}</div>
          }
          {
            description && <div className='vika-guide-connect-us-desc'>{description}</div>
          }
          <Button
            className='vika-guide-connect-us-btn'
            onClick={finalClose}
            color='primary'
            size='small'
          >
            {confirmText}
          </Button>
        </div>
      </div>
    );
  };

  const renderAvatar = (opacity: number) => (
    <div className={`${prefix}-avatar`} style={{ borderColor: `rgba(123, 103, 238, ${opacity})` }}>
      <div className={`${prefix}-avatar-icon`}>
        <Image src={getEnvVariables().ONBOARDING_CUSTOMER_SERVICE_QRCODE_AVATAR_IMG!} alt='' width={64} height={64} />
      </div>
      <div className={`${prefix}-avatar-star`}>
        <StarFilled color={colors.fc14} size='12px' />
      </div>
    </div>
  );

  const renderQrCode = (size: number) => (
    <div className={`${prefix}-img`} style={{ width: size, height: size }}>
      <div className={`${prefix}-corner`} />
      <div className={`${prefix}-corner`} />
      <div className={`${prefix}-corner`} />
      <div className={`${prefix}-corner`} />
      <Image
        width={size - 20}
        height={size - 20}
        src={isVikaby ? platformImg.vikaby : platformImg.questionnaire}
        alt=""
      />
    </div>
  );

  const renderModalContent = () => {
    const { list } = questionnaire;
    const iconList: IQuestionnaireIcon[] = list ? JSON.parse(list) : [];
    return (
      isVikaby ? (
        <div className={`${prefix}-customer`}>
          <div className={`${prefix}-introduce`}>
            {renderAvatar(0.3)}
            <Typography className={`${prefix}-customer-welcome`} variant='h5'>
              {vikaby.title}
            </Typography>
            <Typography className={`${prefix}-customer-desc`} variant='h5'>
              {vikaby.description}
            </Typography>
            <div className={`${prefix}-customer-list`} dangerouslySetInnerHTML={{ __html: vikaby.list || '' }} />
            <div className={`${prefix}-customer-more`}>
              ......
            </div>
          </div>
          <div className={`${prefix}-customer-content`}>
            <div className={`${prefix}-customer-content-border`}>
              <Typography variant='h6' className={`${prefix}-customer-wecom-tip`}>{isBindDingTalk ? dingtalk.tip : vikaby.tip}</Typography>
              {renderQrCode(194)}
            </div>
          </div>
        </div>
      ) : (
        <div className={`${prefix}-guide`} style={{ backgroundImage: `url(${getEnvVariables().ONBOARDING_CUSTOMER_SERVICE_BACKGROUND_IMG})` }}>
          {renderAvatar(1)}
          <Typography className={`${prefix}-guide-welcome`} variant='h4'>{questionnaire.title}</Typography>
          <Typography className={`${prefix}-guide-tip`} variant='body4'>{questionnaire.tip}</Typography>
          <div className={`${prefix}-guide-content`}>
            <div className={`${prefix}-guide-content-border`}>
              {
                iconList.map((v) => (
                  <Typography key={v.title} variant='body4' className={`${prefix}-guide-content-tag`}>
                    {mapIcon[v.icon]}
                    {v.title}
                  </Typography>
                ))
              }
              {renderQrCode(220)}
            </div>
          </div>
        </div>
      )
    );
  };

  const rest: any = {
    closeIcon: <CloseOutlined size={8} color={colors.fc3} />
  };
  if (!isBindFeishu) {
    rest.width = 720;
    rest.closeIcon = <CloseOutlined size={8} color={colors.fc8} />;
    rest.wrapClassName = 'vika-qrcode-close';
  }
  if (isVikaby) {
    rest.style = { height: 520 };
    rest.bodyStyle = { height: 520 };
  }

  return (
    <ModalBase
      visible={show}
      className={classNames({ ['vika-guide-modal-no-box-shadow']: backdrop })}
      closable
      maskClosable={false}
      centered
      mask={backdrop}
      footer={null}
      onCancel={finalClose}
      getContainer={'.vika-guide-connect-us'}
      {...rest}
    >
      {isBindFeishu ? renderOld() : renderModalContent()}
    </ModalBase>
  );
};

export const showContactUs = (props: PropsWithChildren<IGuideContactUsOptions>) => {
  const { children, ...rest } = props;
  const destroy = () => {
    const dom = document.querySelector('.vika-guide-connect-us');
    dom && document.body.removeChild(dom);
  };

  const render = () => {
    setTimeout(() => {
      const div = document.createElement('div');
      div.setAttribute('class', 'vika-guide-connect-us');
      document.body.appendChild(div);
      const root = createRoot(div);
      root.render(
        <Modal onClose={destroy} {...rest}>{children}</Modal>);
    });
  };

  const run = () => {
    destroy();
    render();
  };

  run();
};

export const destroyContactUs = () => {
  const destroy = () => {
    const dom = document.querySelector('.vika-guide-connect-us');
    dom && document.body.removeChild(dom);
  };
  destroy();
};
