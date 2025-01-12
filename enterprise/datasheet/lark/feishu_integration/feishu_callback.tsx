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

import * as React from 'react';
import { useEffect } from 'react';
import { Message } from '@apitable/components';
import { Loading } from 'pc/components/common';
import { useQuery } from 'pc/hooks';

/**
 * Click on the workbench to go to the middle page of the space station
 */
const FeishuCallback: React.FC = () => {
  const query = useQuery();
  const result = query.get('key');
  useEffect(() => {
    if (result) {
      Message.error({ content: result });
    }
  }, [result]);

  return <Loading />;
};

export default FeishuCallback;
