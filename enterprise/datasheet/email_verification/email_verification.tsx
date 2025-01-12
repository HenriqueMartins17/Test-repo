
import Image from 'next/image';
import { useRouter } from 'next/router';
import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { Button, ThemeName, TextButton } from '@apitable/components';
import { Strings, t, Api, integrateCdnHost } from '@apitable/core';
import { Message } from 'pc/components/common';
import { useUserRequest } from 'pc/hooks';
import apitableLogoDark from 'static/icon/datasheet/APITable_brand_dark.png';
import apitableLogoLight from 'static/icon/datasheet/APITable_brand_light.png';
import emailEmptyDrak from 'static/icon/datasheet/email_empty_drak.png';
import emailEmptyLight from 'static/icon/datasheet/email_empty_light.png';
import { getEnvVariables } from '../../../pc/utils/env';
import styles from './style.module.less';

export const EmailVerification: React.FC = () => {
  const themeName = useSelector(state => state.theme);
  const router = useRouter();
  const email = router.query.email instanceof Array ? router.query.email[0] : router.query.email;
  const { IS_AITABLE, LONG_DARK_LOGO, LONG_LIGHT_LOGO } = getEnvVariables();
  const logo = themeName === ThemeName.Light ? (IS_AITABLE ? integrateCdnHost(LONG_LIGHT_LOGO!) : apitableLogoLight) :
    (IS_AITABLE ? integrateCdnHost(LONG_DARK_LOGO!) : apitableLogoDark);
  const EmailEmpty = themeName === ThemeName.Light ? emailEmptyLight : emailEmptyDrak;
  const { signOutReq } = useUserRequest();
  const [buttonLoading, setButtonLoading] = useState(false);

  const resentVerifyEmail = () => {
    setButtonLoading(true);
    const decodeEmail = decodeURIComponent(email as string);

    Api?.apitableResentVerifyEmail(decodeEmail).then(res => {
      setButtonLoading(false);
      const { success, message } = res.data;
      if (success) {
        Message.success({ content: t(Strings.reset_password_via_emai_success) });
      } else {
        Message.error({ content: t(Strings.reset_password_via_emai_failed, { error_message: message }) });
      }
      return;
    });

  };

  return (
    <div className={styles.emailVerifiacation}>
      <div className={styles.logo}>
        <Image src={logo} alt="Logo" width={132} height={29} />
      </div>
      <div className={styles.contentWrapper}>
        <div className={styles.content}>
          <Image src={EmailEmpty} alt="Email" width={240} height={180} />
          <h1>{ t(Strings.email_verify_warning_title) }</h1>
          <p>{ t(Strings.email_verify_warning_desc, { email_address: email }) }</p>
          <Button
            className={styles.backButton}
            color="primary"
            variant="fill"
            size="middle"
            loading={buttonLoading}
            block
            onClick={resentVerifyEmail}
          >
            {t(Strings.email_verify_warning_button_resend)}
          </Button>
          <TextButton size="middle" color="primary" onClick={() => {
            signOutReq();
          }}>{t(Strings.email_verify_warning_button_back)}</TextButton>
        </div>
      </div>
    </div>);
};

