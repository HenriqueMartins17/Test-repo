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

import classNames from 'classnames';
import parser from 'html-react-parser';
import Image from 'next/image';
import { ConfigConstant, Strings, t } from '@apitable/core';
import { LoginCard, Wrapper } from 'pc/components/common';
import BothImg from 'static/icon/signin/signin_img_vika_feishu.png';
import { IdentifyingCodeLogin, IIdentifyingCodeLoginProps } from '../../home';
import styles from './style.module.less';

export const FeiShuLogin = (data: IIdentifyingCodeLoginProps) => {
  const {
    submitText = t(Strings.feishu_admin_login_btn),
    hiddenProtocol = true,
    mode = ConfigConstant.LoginMode.PHONE,
    mobileCodeType,
    footer = parser(t(Strings.new_user_turn_to_home)),
    ...rest
  } = data;

  return (
    <Wrapper hiddenLogo className={styles.center}>
      <div className={classNames(styles.commonWrapper, styles.center)}>
        <div className={styles.commonImgWrapper}>
          <Image src={BothImg} alt="" />
        </div>
        <LoginCard className={styles.commonLoginCardWrapper}>
          <div className={styles.commonCardTitle}>
            {t(Strings.feishu_admin_login_title)}
          </div>
          <div className={styles.commonCardSubTitle}>
            {t(Strings.lark_admin_login_and_config_sub_title)}
          </div>
          <IdentifyingCodeLogin
            submitText={submitText}
            hiddenProtocol={hiddenProtocol}
            mode={mode}
            smsType={mobileCodeType}
            footer={footer}
            {...rest}
          />
        </LoginCard>
      </div>
    </Wrapper>
  );
};
