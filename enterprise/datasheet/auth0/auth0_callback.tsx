
import { useMount } from 'ahooks';
import { Spin } from 'antd';
import React, { FC } from 'react';
import { Navigation } from '@apitable/core';
import { Router } from 'pc/components/route_manager/router';
import { useLinkInvite, useQuery } from 'pc/hooks';
import { isLocalSite } from 'pc/utils';
import { setStorage, StorageName } from 'pc/utils/storage/storage';
import styles from './style.module.less';

export const Auth0Callback: FC = () => {
  const { join } = useLinkInvite();
  const shareReference = localStorage.getItem('share_login_reference');
  const reference = localStorage.getItem('reference') || '';
  const inviteLinkData = localStorage.getItem('invite_link_data');
  const query = useQuery();
  const via = query.get('via') || '';
  useMount(() => {
    localStorage.removeItem('share_login_reference');
    localStorage.removeItem('reference');
    if (inviteLinkData) {
      join({ fromLocalStorage: true });
      return;
    }

    if (shareReference) {
      setStorage(StorageName.ShareLoginFailed, false);
      localStorage.removeItem('share_login_reference');
      window.location.href = shareReference;
      return;
    }

    if (reference && isLocalSite(window.location.href, reference)) {
      localStorage.removeItem('reference');
      window.location.href = reference;
      return;
    }

    Router.redirect(Navigation.WORKBENCH, { query: { via } });

  });

  return(
    <div className={styles.container}>
      <Spin />
    </div>
  );
};