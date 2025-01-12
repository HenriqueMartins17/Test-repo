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

import { useEffect } from 'react';
import { Navigation } from '@apitable/core';
import { Loading } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery } from 'pc/hooks';

export const WecomInvite = () => {
  const query = useQuery();
  const authCode = query.get('auth_code') || '';
  const suiteId = query.get('suiteid') || '';
  useEffect(() => {
    setTimeout(() => {
      Router.push(Navigation.WECOM_SHOP_CALLBACK, {
        query: {
          suiteid: suiteId,
          auth_code: authCode,
        },
      });
    }, 3000);
  }, [authCode, suiteId]);
  return (
    <Loading />
  );
};
