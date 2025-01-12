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

import { useToggle } from 'ahooks';
import Trigger from 'rc-trigger';
import * as React from 'react';
import { useSelector } from 'react-redux';
import { lightColors, LinkButton } from '@apitable/components';
import { ApiInterface, AutoTestID, ConfigConstant, Navigation, Strings, t } from '@apitable/core';
import { DingdingFilled, FeishuFilled, QqFilled, WechatFilled } from '@apitable/icons';
import { Router } from 'pc/components/route_manager/router';
import { useUserRequest } from 'pc/hooks';
import { getEnvVariables } from 'pc/utils/env';
import { IdentifyingCodeLogin, ISubmitRequestParam } from '../../../home';
import { dingdingLogin, feishuLogin, qqLogin, wechatLogin } from '../../../login';
import { PasswordLogin } from '../password_login';
import { SSOLogin } from '../sso_login';
import commonLoginStyles from '../style.module.less';

interface IPhoneAndEmailLogin {
  mod?: string;
  setMod: (mod: string) => void;
}

export const PhoneAndEmailLogin = (props: IPhoneAndEmailLogin): JSX.Element => {
  const { mod, setMod } = props;
  const { ACCOUNT_RESET_PASSWORD_VISIBLE, LOGIN_SSO_VISIBLE, LOGIN_DEFAULT_VERIFY_TYPE } = getEnvVariables();
  const toggleLoginModBtnVisible = LOGIN_DEFAULT_VERIFY_TYPE?.includes(',');
  const isWecom = useSelector(state => state.space.envs?.weComEnv?.enabled);
  const { loginOrRegisterReq } = useUserRequest();
  const [isPopupVisible, { toggle: popupVisibleToggle }] = useToggle(false);

  const changeLoginMod = () => {
    let currentMod = 'identifying_code';
    if (LOGIN_SSO_VISIBLE) {
      currentMod = mod === ConfigConstant.PASSWORD_LOGIN ? ConfigConstant.SSO_LOGIN : ConfigConstant.PASSWORD_LOGIN;
    } else {
      currentMod = mod === ConfigConstant.IDENTIFY_CODE_LOGIN ?
        ConfigConstant.PASSWORD_LOGIN : ConfigConstant.IDENTIFY_CODE_LOGIN;
    }
    setMod(currentMod);
    localStorage.setItem('vika_login_mod', currentMod.toString());
  };

  const submitRequest = React.useCallback((data: ISubmitRequestParam) => {
    const loginData: ApiInterface.ISignIn = {
      areaCode: data.areaCode,
      username: data.account,
      type: data.type,
      credential: data.credential,
      data: data.nvcVal,
      mode: data.mode,
    };
    return loginOrRegisterReq(loginData);
  }, [loginOrRegisterReq]);

  const goResetPwd = () => {
    Router.push(Navigation.RESET_PWD);
  };

  const modConfig = React.useMemo(() => {
    let changeModText = '';
    let loginComponent: null | React.ReactNode = null;
    switch (mod) {
      case ConfigConstant.IDENTIFY_CODE_LOGIN:
        loginComponent = <IdentifyingCodeLogin submitRequest={submitRequest} />;
        changeModText = t(Strings.password_login);
        break;
      case ConfigConstant.PASSWORD_LOGIN:
        loginComponent = <PasswordLogin submitRequest={submitRequest} />;
        changeModText = LOGIN_SSO_VISIBLE ? t(Strings.sso_login) : t(Strings.verification_code_login);
        break;
      case ConfigConstant.SSO_LOGIN:
        loginComponent = <SSOLogin submitRequest={submitRequest} />;
        changeModText = t(Strings.password_login);
        break;
      default:
        break;
    }
    return { loginComponent, changeModText };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [submitRequest, mod]);

  const { loginComponent, changeModText } = modConfig;

  const loginOtherPopup = (
    <div className={commonLoginStyles.loginOtherPopup} onClick={() => popupVisibleToggle()}>
      <div className={commonLoginStyles.loginOtherItem} onClick={() => wechatLogin()}>
        <WechatFilled size={16} color={lightColors.rc04} />
        <span>{t(Strings.wechat)}</span>
      </div>
      <div className={commonLoginStyles.loginOtherItem} onClick={() => dingdingLogin()}>
        <DingdingFilled size={16} color={lightColors.rc02} />
        <span>{t(Strings.dingtalk)}</span>
      </div>
      <div className={commonLoginStyles.loginOtherItem} onClick={() => feishuLogin()}>
        <FeishuFilled size={16} />
        <span>{t(Strings.lark)}</span>
      </div>
      <div className={commonLoginStyles.loginOtherItem} onClick={() => qqLogin()}>
        <QqFilled size={16} color={lightColors.rc03} />
        <span>{t(Strings.qq)}</span>
      </div>
    </div>
  );

  return (
    <div>
      {loginComponent}
      <div className={commonLoginStyles.buttonGroup}>
        {isWecom ? (
          <div className={commonLoginStyles.buttonGroupDesktop}>
            {toggleLoginModBtnVisible &&
              <LinkButton
                underline={false}
                component='button'
                id={AutoTestID.CHANGE_MODE_BTN}
                className='toggleLoginModeBtn'
                onClick={changeLoginMod}
              >
                {changeModText}
              </LinkButton>
            }
            <Trigger
              action={['click']}
              popup={loginOtherPopup}
              destroyPopupOnHide
              popupAlign={{ points: ['b', 'c'], offset: [0, -20], overflow: { adjustX: true, adjustY: true } }}
              popupStyle={{ width: 240 }}
              popupVisible={isPopupVisible}
              onPopupVisibleChange={() => popupVisibleToggle()}
              zIndex={10000}
            >
              <LinkButton
                underline={false}
                component='button'
              >
                {t(Strings.other_login)}
              </LinkButton>
            </Trigger>
            {ACCOUNT_RESET_PASSWORD_VISIBLE &&
              <LinkButton
                underline={false}
                component='button'
                onClick={goResetPwd}
              >
                {t(Strings.retrieve_password)}
              </LinkButton>
            }
          </div>
        ) : (
          <>
            <div>
              {toggleLoginModBtnVisible &&
                <LinkButton
                  underline={false}
                  component='button'
                  id={AutoTestID.CHANGE_MODE_BTN}
                  className='toggleLoginModeBtn'
                  onClick={changeLoginMod}
                  style={{ paddingLeft: 0 }}
                >
                  {changeModText}
                </LinkButton>
              }
            </div>
            {ACCOUNT_RESET_PASSWORD_VISIBLE &&
              <LinkButton
                underline={false}
                component='button'
                onClick={goResetPwd}
                style={{ paddingRight: 0 }}
              >
                {t(Strings.retrieve_password)}
              </LinkButton>
            }
          </>
        )}
      </div>
    </div>
  );
};
