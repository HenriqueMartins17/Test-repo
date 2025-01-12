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

import { useMemo } from 'react';
import { ConfigConstant, Settings, Strings, t } from '@apitable/core';
import { navigationToUrl } from 'pc/components/route_manager/navigation_to_url';
import { useQuery } from 'pc/hooks';
import BoundImage from 'static/icon/common/common_img_feishu_binding.png';
import FailureImage from 'static/icon/common/common_img_share_linkfailure.png';
import { ErrPromptBase, IErrPromptBase, SocialPlatformMap } from '../../home';

const FeishuConfigureErr = () => {
  const query = useQuery();
  const key = query.get('key');

  const dataInfo: IErrPromptBase = useMemo(() => {
    switch (key) {
      case 'auth_fail' : {
        return {
          img: BoundImage,
          desc: t(Strings.feishu_configure_of_authorize_err),
          btnText: t(Strings.entry_space),
          onClick: () => {
            navigationToUrl(Settings.integration_feishu_help.value);
          },
        };
      }
      case 'is_not_admin': {
        return {
          img: FailureImage,
          desc: t(Strings.feishu_configure_of_idetiity_err),
          btnText: t(Strings.know_more),
          onClick: () => {
            navigationToUrl(Settings.integration_feishu_help.value);
          },
        };
      }
      default: {
        return {
          img: FailureImage,
          desc: t(Strings.something_went_wrong),
          btnText: t(Strings.know_more),
          onClick: () => {
            navigationToUrl(Settings.integration_feishu_help.value);
          },
        };
      }
    }
  }, [key]);

  return (
    <ErrPromptBase
      headerLogo={SocialPlatformMap[ConfigConstant.SocialType.FEISHU].logoWithVika as string}
      {...dataInfo}
    />
  );
};

export default FeishuConfigureErr;
