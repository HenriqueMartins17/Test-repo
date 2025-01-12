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

import cls from 'classnames';
import { useRouter } from 'next/router';
import * as React from 'react';
import { useCallback, useEffect, useState } from 'react';
import { Button, useTheme } from '@apitable/components';
import { Api, Navigation, Strings, t } from '@apitable/core';
import { CheckOutlined } from '@apitable/icons';
import { Router } from 'pc/components/route_manager/router';
import { useRequest } from 'pc/hooks';
import { Loading } from '../../home';

import styles from './styles.module.less';

interface IFeishuSyncConcat {
  spaceId: string;
}

/**
 * Address Book Sync Middle Page
 */
const FeishuSyncConcat: React.FC<IFeishuSyncConcat> = (props) => {
  const theme = useTheme();
  const router = useRouter();
  const { appInstanceId } = router.query as { appInstanceId: string };
  const { spaceId } = props;
  const [complete, setComplete] = useState(false);

  const { data: result, cancel } = useRequest(() => Api.getAppInstanceById(appInstanceId!), {
    pollingInterval: 1000,
    pollingWhenHidden: false,
  });

  useEffect(() => {
    if (result) {
      const { success, data } = (result as any).data;
      if (!success || !data || (data && !data.config.profile.contactSyncDone)) {
        return;
      }
      cancel();
      setComplete(true);
    }
  }, [result, cancel]);

  const handleClick = useCallback(() => {
    Router.redirect(Navigation.WORKBENCH, {
      params: { spaceId },
    });
  }, [spaceId]);

  return (
    <div className={styles.completeWrap}>
      {
        complete ? (
          <>
            <div className={styles.completeIcon}>
              <div className={styles.completeCircle1} />
              <div className={styles.completeCircle2} />
              <div className={styles.completeCircle3}>
                <CheckOutlined size='36px' color={theme.color.fc8} />
              </div>
              {
                [1, 2, 3].map((v) => (
                  <div key={v} className={cls(styles.completeCircleGroup, styles[`completeCircleGroup${v}`])}>
                    <div className={styles.completeCircleBig} />
                    <div className={styles.completeCircleSmall} />
                  </div>
                ))
              }
            </div>
            <div className={styles.completeContent}>{t(Strings.lark_integration_sync_success)}</div>
            <Button onClick={handleClick} color='primary'>{t(Strings.lark_integration_sync_btn)}</Button>
          </>
        ) : <Loading style={{ background: theme.color.fc8 }} tip={t(Strings.lark_integration_sync_tip)} />
      }
    </div>
  );
};

export default FeishuSyncConcat;
