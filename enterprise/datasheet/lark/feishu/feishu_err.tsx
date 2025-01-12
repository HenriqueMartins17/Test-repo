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
import Image, { StaticImageData } from 'next/image';
import * as React from 'react';
import { FC, useMemo } from 'react';
import { Button } from '@apitable/components';
import { Navigation, Settings, Strings, t } from '@apitable/core';
import { Wrapper } from 'pc/components/common';
import { navigationToUrl } from 'pc/components/route_manager/navigation_to_url';
import { Router } from 'pc/components/route_manager/router';
import { useQuery } from 'pc/hooks';
import BoundImage from 'static/icon/common/common_img_feishu_binding.png';
import FailureImage from 'static/icon/common/common_img_share_linkfailure.png';
import BothImg from 'static/icon/signin/signin_img_vika_feishu.png';
import styles from './style.module.less';

interface IFeishuDefaultErrProps {
  img: React.ReactNode | StaticImageData;
  desc: string;
  btnText: string;
  onClick: () => void;
}

export enum FeishuErrType {
  BOUND = 'bound',
  IDENTITY = 'identity',
  SELECT_VALID = 'select_valid',
  CONFIGURING = 'configuring',
}

const FeishuDefaultErr = (data: IFeishuDefaultErrProps) => {
  const { img, desc, btnText, onClick } = data;

  return (
    <Wrapper hiddenLogo className={styles.center}>
      <div
        className={classNames(
          styles.commonWrapper,
          styles.center,
          styles.feishuErr
        )}
      >
        <div className={styles.commonImgWrapper}>
          <Image src={BothImg} alt="" />
        </div>
        <span className={styles.mainImg}>
          <Image src={img as string} alt="" />
        </span>
        <div className={styles.desc}>{desc}</div>
        <Button color='primary' onClick={onClick}>
          {btnText}
        </Button>
      </div>
    </Wrapper>
  );
};

const FeishuErr: FC<{
  // type: 'bound' | 'identity' | 'select_valid' | 'configuring';
}> = () => {
  const query = useQuery();
  const key = query.get('key');
  const msg = query.get('msg');
  const appId = query.get('appId') || query.get('app_id');
  const info: { [key: string]: IFeishuDefaultErrProps } = React.useMemo(() => {
    return {
      bound: {
        img: BoundImage,
        desc: t(Strings.feishu_configure_err_of_bound),
        btnText: t(Strings.entry_space),
        onClick: () => {
          if (!appId) {
            Router.push(Navigation.LOGIN);
            return;
          }

          const url = new URL('https://applink.feishu.cn/client/web_app/open');
          url.searchParams.append('appId', appId);
          url.searchParams.append(
            'path',
            '/api/v1/social/feishu/workbench/callback?url=/'
          );
          navigationToUrl(url.href, { clearQuery: true });
        },
      },
      identity: {
        img: FailureImage,
        desc: t(Strings.feishu_configure_err_of_identity),
        btnText: t(Strings.know_more),
        onClick: () => {
          navigationToUrl(Settings.integration_feishu_help.value);
        },
      },
      select_valid: {
        img: BoundImage,
        desc: t(Strings.feishu_configure_err_of_select_valid),
        btnText: t(Strings.know_more),
        onClick: () => {
          navigationToUrl(Settings.integration_feishu_help.value);
        },
      },
      configuring: {
        img: BoundImage,
        desc: t(Strings.feishu_configure_err_of_configuring),
        btnText: t(Strings.know_more),
        onClick: () => {
          navigationToUrl(Settings.integration_feishu_help.value);
        },
      },
    };
  }, [appId]);

  const data = useMemo(() => {
    if (key && info[key]) {
      return info[key];
    }
    return {
      img: FailureImage,
      desc: msg || t(Strings.something_went_wrong),
      btnText: t(Strings.know_more),
      onClick: () => {
        navigationToUrl(Settings.integration_feishu_help.value);
      },
    };
  }, [key, msg, info]);

  return <FeishuDefaultErr {...data} />;
};

export default FeishuErr;
