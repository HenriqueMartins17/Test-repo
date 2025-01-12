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

import { Popover } from 'antd';
import classNames from 'classnames';
import Image from 'next/image';
import React, { FC, useState } from 'react';
import { LinkButton, Typography, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { CloseOutlined } from '@apitable/icons';
import { ButtonPlus, Logo } from 'pc/components/common';
import { ComponentDisplay, ScreenSize } from 'pc/components/common/component_display';
import { Popup } from 'pc/components/common/mobile/popup';
import { TComponent } from 'pc/components/common/t_component';
import { getEnvVariables } from 'pc/utils/env';
import InvitePng from 'static/icon/signin/signin_img_invite.png';
import styles from './style.module.less';

enum Modes {
  Personal,
  Official,
}

export const GetInvitationCodePopover: FC<React.PropsWithChildren<any>> = ({ children }) => {
  // Whether to display the contents of the bubble
  const [visible, setVisible] = useState(false);
  const [mode, setMode] = useState(Modes.Personal);
  const colors = useThemeColors();

  const PersonalContent = () => {
    return (
      <div className={styles.personal}>
        <Typography className={styles.desc} variant="body2" color={colors.secondLevelText}>
          <TComponent
            tkey={t(Strings.personal_invitation_code_desc1)}
            params={{
              text: (
                <Typography variant="h7" color={colors.primaryColor}>
                  {t(Strings.personal_invite_code_usercenter)}
                </Typography>
              ),
            }}
          />
        </Typography>
        <Typography className={styles.desc} variant="body2" color={colors.secondLevelText}>
          <TComponent
            tkey={t(Strings.personal_invitation_code_desc2)}
            params={{
              text: (
                <Typography variant="h7" color={colors.primaryColor}>
                  {t(Strings.personal_invitation_code_desc2_text)}
                </Typography>
              ),
            }}
          />
        </Typography>
        <div className={styles.imgWrapper}>
          <span className={styles.invitePng}>
            <Image src={InvitePng} alt="" />
          </span>
        </div>
      </div>
    );
  };

  const OfficialContent = () => {
    return (
      <div className={styles.official}>
        <Typography className={styles.desc} variant="body2" color={colors.secondLevelText}>
          <TComponent
            tkey={t(Strings.official_invitation_code_desc1)}
            params={{
              text: (
                <Typography variant="h7" color={colors.primaryColor}>
                  {t(Strings.official_invitation_code)}
                </Typography>
              ),
            }}
          />
        </Typography>
        <Typography className={styles.desc} variant="body2" color={colors.secondLevelText}>
          <TComponent
            tkey={t(Strings.official_invitation_code_desc2)}
            params={{
              text: (
                <Typography variant="h7" color={colors.primaryColor}>
                  {t(Strings.v_500)}
                </Typography>
              ),
            }}
          />
        </Typography>
        <div className={styles.qrCodeWrapper}>
          <span className={styles.qrCode}>
            <Image src={getEnvVariables().WECHAT_MP_QR_CODE!} alt="QrCode" />
          </span>
          <Logo className={styles.logo} size="large" text={false} />
        </div>
        <Typography className={styles.figcaption} variant="body3" color={colors.thirdLevelText} align="center">
          {t(Strings.system_configuration_company_official_account)}
        </Typography>
      </div>
    );
  };

  const Content = () => {
    return (
      <div className={styles.contentWrapper}>
        <div className={classNames(styles.content)}>
          {mode === Modes.Personal ? <PersonalContent /> : <OfficialContent />}
          <div className={styles.line} />
        </div>
        <LinkButton className={styles.modeToggleBtn} onClick={handleModeChange} color={colors.thirdLevelText}>
          {mode === Modes.Personal ? t(Strings.official_mode) : t(Strings.personal_mode)}
        </LinkButton>
      </div>
    );
  };

  const handleModeChange = () => {
    setMode(mode === Modes.Personal ? Modes.Official : Modes.Personal);
  };

  const title = mode === Modes.Personal ? t(Strings.personal_mode) : t(Strings.official_mode);

  return (
    <>
      <ComponentDisplay minWidthCompatible={ScreenSize.md}>
        <Popover
          visible={visible}
          overlayClassName={styles.popover}
          onVisibleChange={visible => setVisible(visible)}
          trigger="click"
          title={
            <div className={styles.title}>
              <Typography variant="h5">{title}</Typography>
              <ButtonPlus.Icon
                className={styles.closeBtn}
                icon={<CloseOutlined color={colors.thirdLevelText} />}
                onClick={() => setVisible(false)}
              />
            </div>
          }
          content={<Content />}
        >
          <div>{children}</div>
        </Popover>
      </ComponentDisplay>

      <ComponentDisplay maxWidthCompatible={ScreenSize.md}>
        <div onClick={() => setVisible(true)}>{children}</div>
        <Popup
          className={styles.invitationCodePopup}
          height="auto"
          open={visible}
          placement="bottom"
          title={title}
          onClose={() => setVisible(false)}
          destroyOnClose
        >
          <Content />
        </Popup>
      </ComponentDisplay>
    </>
  );
};
