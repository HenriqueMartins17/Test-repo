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

/* eslint-disable react-hooks/exhaustive-deps */
import parser from 'html-react-parser';
import React, { FC, useEffect } from 'react';
import { ApiInterface, ConfigConstant, Strings, t, StoreActions } from '@apitable/core';
import { Wrapper } from 'pc/components/common';
import { useDispatch, useQuery, useUserRequest } from 'pc/hooks';
import { IdentifyingCodeLogin, ISubmitRequestParam } from '../login';
import styles from './style.module.less';

export const ImprovingInfo: FC = () => {
  const query = useQuery();
  const dispatch = useDispatch();
  const inviteLinkToken = query.get('inviteLinkToken') || '';

  useEffect(() => {
    // When invite link, should valid link and update invite state cache.
    if (inviteLinkToken) {
      dispatch(StoreActions.verifyLink(inviteLinkToken));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [inviteLinkToken]);

  // 微信公众号token
  const mpToken = query.get('token') || '';
  const reference = query.get('reference') || '';
  const inviteMailToken = query.get('inviteMailToken') || '';
  const improveType = query.get('improveType') || '';
  const { loginOrRegisterReq, bindPhoneReq } = useUserRequest();
  const mobileModLogin = (data: ISubmitRequestParam) => {
    const { areaCode, account, credential, type, nvcVal } = data;
    const loginData: ApiInterface.ISignIn = {
      areaCode: areaCode,
      username: account,
      type: type,
      credential: credential,
      data: nvcVal,
      token: mpToken,
    };
    return improveType === ConfigConstant.ImproveType.Phone ? bindPhoneReq({
      areaCode, 
      phone: account, 
      code: credential,
      reference,
      inviteLinkToken,
      inviteMailToken,
    }) : loginOrRegisterReq(loginData);
  };

  return (
    <Wrapper>
      <div className={styles.improvingInfoWrapper}>
        <div className={styles.improvingInfo}>
          <div className={styles.title}>{t(Strings.improving_info)}</div>
          <div className={styles.tip}>{t(Strings.improving_info_tip)}</div>
          <IdentifyingCodeLogin
            mode={ConfigConstant.LoginMode.PHONE}
            submitRequest={mobileModLogin}
            smsType={improveType === ConfigConstant.ImproveType.Phone ? ConfigConstant.SmsTypes.BIND_MOBILE : ConfigConstant.SmsTypes.LOGIN_ACCOUNT}
            submitText={t(Strings.confirm)}
            footer={parser(t(Strings.old_user_turn_to_home))}
          />
        </div>
      </div>
    </Wrapper>
  );
};