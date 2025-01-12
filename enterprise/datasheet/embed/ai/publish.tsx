/**
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import Head from 'next/head';
import { useRouter } from 'next/router';
import React from 'react';
import { useSelector } from 'react-redux';
import { ChatPageProvider } from '@apitable/ai';
import { IShareInfo, Navigation, Selectors, StoreActions, Strings, t } from '@apitable/core';
import { Logo } from 'pc/components/common/logo';
import { TComponent } from 'pc/components/common/t_component';
import { NoPermission } from 'pc/components/no_permission';
import { Router } from 'pc/components/route_manager/router';
import { IShareSpaceInfo } from 'pc/components/share/interface';
import { ShareFail } from 'pc/components/share/share_fail';
import { useMountShare } from 'pc/components/share/use_mount_share';
import { useAppDispatch } from 'pc/hooks/use_app_dispatch';
import { getPageParams, usePageParams } from 'pc/hooks/use_page_params';
import { ChatPageMain } from './main';
import { triggerUsageAlertUniversal } from 'enterprise/billing/trigger_usage_alert';
import styles from './style.module.less';

const PublishContext = React.createContext({} as { shareInfo: IShareSpaceInfo });

interface IShareProps {
  shareInfo: Required<IShareInfo> | undefined;
}

export const PublishPage: React.FC<React.PropsWithChildren<IShareProps>> = ({ shareInfo }) => {
  const dispatch = useAppDispatch();
  const router = useRouter();
  const theme = useSelector(Selectors.getTheme);
  const user = useSelector((state) => state.user);

  const { nodeId, aiId } = getPageParams(router.asPath);

  const { shareSpace, shareClose } = useMountShare(shareInfo);

  usePageParams();

  if (shareClose || !shareInfo) {
    return <ShareFail />;
  }

  if (!aiId) {
    Router.replace(Navigation.EMBED_AI_SPACE, {
      params: {
        shareId: shareInfo.shareId,
        nodeId: nodeId || shareInfo.shareNodeTree.nodeId,
      },
    });
  }

  // shareInfo.shareNodeTree.nodeId
  if (!shareSpace) {
    dispatch(StoreActions.setLoading(false));
    return <></>;
  }

  const onJump = () => {
    Router.newTab(Navigation.HOME, { query: { home: 1 } });
  };

  function triggerUsageAlert() {
    triggerUsageAlertUniversal(t(Strings.subscribe_credit_usage_over_limit));
  }

  return (
    <PublishContext.Provider value={{ shareInfo: shareSpace }}>
      <Head>
        <meta property="og:title" content={shareInfo?.shareNodeTree?.nodeName || t(Strings.og_site_name_content)} />
        <meta property="og:type" content="website" />
        <meta property="og:url" content={window.location.href} />
        <meta property="og:site_name" content={t(Strings.og_site_name_content)} />
        <meta property="og:description" content={t(Strings.og_product_description_content)} />
      </Head>
      <div className={styles.publish}>
        <div className={styles.publishPanel}>
          {aiId && (
            <ChatPageProvider
              key={aiId as string}
              aiId={aiId ?? ''}
              childrenNoPermission={<NoPermission />}
              triggerUsageAlert={triggerUsageAlert}
              isLogin={user.isLogin}
            >
              <ChatPageMain />
            </ChatPageProvider>
          )}
        </div>
        <div className={styles.publishPowered}>
          <TComponent
            tkey={t(Strings.brand_desc)}
            params={{
              logo: (
                <span className={styles.publishPoweredLogo} onClick={onJump} style={{ marginLeft: 4 }}>
                  <Logo size="mini" theme={theme} />
                </span>
              ),
            }}
          />
        </div>
      </div>
    </PublishContext.Provider>
  );
};

