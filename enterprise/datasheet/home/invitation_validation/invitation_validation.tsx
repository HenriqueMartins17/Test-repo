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

import { Form, Input } from 'antd';
import Image from 'next/image';
import * as React from 'react';
import { FC, useState } from 'react';
import { useSelector } from 'react-redux';
import { Button, LinkButton, Typography, useThemeColors, ThemeName } from '@apitable/components';
import { ConfigConstant, Navigation, StatusCode, Strings, t } from '@apitable/core';
import { Modal, Wrapper } from 'pc/components/common';
import { TComponent } from 'pc/components/common/t_component';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useRequest, useUserRequest } from 'pc/hooks';
import InviteIcon from 'static/icon/signin/signin_img_invite_logo.svg';
import CreateSpaceIconDark from 'static/icon/space/space_add_name_dark.png';
import CreateSpaceIconLight from 'static/icon/space/space_add_name_light.png';
import { GetInvitationCodePopover } from './get_invitation_code_popover';
import styles from './style.module.less';

export const InvitationValidation: FC = () => {
  const colors = useThemeColors();
  const query = useQuery();
  const token = query.get('token') || '';
  const inviteCodeInUrl = query.get('inviteCode') || undefined;
  const loginTypeInUrl = query.get('loginType') || undefined;
  const [inviteCode, setInviteCode] = useState(inviteCodeInUrl);
  const [err, setErr] = useState('');
  const { signUpReq } = useUserRequest();
  const themeName = useSelector(state => state.theme);
  const WelcomePng = themeName === ThemeName.Light ? CreateSpaceIconLight : CreateSpaceIconDark;
  const { run: signUp, loading } = useRequest(signUpReq, {
    manual: true,
    onSuccess: res => {
      const { code, message } = res.data;
      if (code === StatusCode.TIME_OUT) {
        Modal.error({
          title: t(Strings.kindly_reminder),
          content: t(Strings.page_timeout),
          okText: t(Strings.refresh),
          onOk: () => {
            if (query.get('reference')) {
              window.location.href = query.get('reference')!;
              return;
            }
            Router.push(Navigation.LOGIN);
          },
        });
        return;
      }
      setErr(message);
    },
  });

  const changeHandler = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (err) {
      setErr('');
    }
    setInviteCode(value);
  };

  const submitHandler = (IgnoreCode?: boolean) => {
    const code = IgnoreCode ? undefined : (inviteCode && inviteCode.trim());
    signUp(token, code);
  };
  const isEmailCodeType = loginTypeInUrl && loginTypeInUrl === ConfigConstant.LoginTypes.EMAIL;
  return (
    <Wrapper>
      <div className={styles.invitationValidation}>
        <span className={styles.welcomePng}>
          <Image src={WelcomePng} alt='welcome vika' />
        </span>
        <div className={styles.title}>{t(Strings.register_invitation_code_title)}</div>
        <Typography className={styles.tip} variant='body2' color={colors.secondLevelText}>
          {!inviteCodeInUrl && !isEmailCodeType ?
            <TComponent
              tkey={t(Strings.invitation_code_tip)}
              params={{
                text:
                  <GetInvitationCodePopover>
                    <span className={styles.getInvitationCodeBtn}>{t(Strings.get_invitation_code)}</span>
                  </GetInvitationCodePopover>
              }}
            /> :
            t(Strings.default_invitation_code_tip)
          }
        </Typography>
        <Form className={styles.form}>
          <Typography className={styles.label} variant='body2' color={colors.firstLevelText}>
            {t(Strings.invite_code)}
          </Typography>
          <Input
            className={err && 'error'}
            prefix={<InviteIcon />}
            placeholder={t(Strings.invite_code_input_placeholder)}
            onChange={changeHandler}
            value={inviteCode}
            disabled={Boolean(inviteCodeInUrl)}
          />
          <div className={styles.errMsg}>{err}</div>
          <Button
            className={styles.submitBtn}
            color='primary'
            size='large'
            htmlType='submit'
            block
            disabled={loading || !inviteCode}
            onClick={() => {
              submitHandler();
            }}
            loading={loading}
          >
            {t(Strings.button_submit)}
          </Button>
          <LinkButton
            className={styles.skipBtn}
            color={colors.thirdLevelText}
            underline={false}
            onClick={() => {
              submitHandler(true);
            }}
          >
            {t(Strings.skip)}
          </LinkButton>
        </Form>
      </div>
    </Wrapper>
  );
};