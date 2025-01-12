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

import React from 'react';
import { ConfigConstant, Settings, Strings, t } from '@apitable/core';
import { navigationToUrl } from 'pc/components/route_manager/navigation_to_url';
import { ErrPromptBase, SocialPlatformMap } from '../../home';

export const DingtalkUnboundErr = () => {
  return (
    <ErrPromptBase
      headerLogo={SocialPlatformMap[ConfigConstant.SocialType.DINGTALK].logoWithVika as string}
      desc={t(Strings.feishu_configure_err_of_configuring)}
      btnText={t(Strings.know_more)}
      onClick={() => {
        navigationToUrl(window.location.origin + Settings.integration_dingtalk_help_url.value, { clearQuery: true });
      }}
    />
  );
};