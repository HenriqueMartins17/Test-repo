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

import { useLocalStorageState } from 'ahooks';
import classNames from 'classnames';
import { get } from 'lodash';
import Head from 'next/head';
import Image from 'next/image';
import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import SplitPane from 'react-split-pane';
import { Button, ThemeName } from '@apitable/components';
import {
  ConfigConstant,
  findNode,
  IEmbedInfo,
  Navigation,
  PermissionType,
  Selectors,
  StoreActions,
  Strings,
  t,
  integrateCdnHost,
} from '@apitable/core';
import { CloseOutlined } from '@apitable/icons';
import { SystemTheme } from 'pc/common/theme';
import { DashboardPanel } from 'pc/components/dashboard_panel';
import { DataSheetPane } from 'pc/components/datasheet_pane';
import { FolderShowcase } from 'pc/components/folder_showcase';
import { FormPanel } from 'pc/components/form_panel';
import { Router } from 'pc/components/route_manager/router';
import { INodeTree } from 'pc/components/share/interface';
import { usePageParams, useRequest, useSideBarVisible, useSpaceRequest } from 'pc/hooks';
import { useAppDispatch } from 'pc/hooks/use_app_dispatch';
import { useNotifyNOdeNameChange } from 'pc/hooks/use_notify_node_name_change';
import { getEnvVariables } from 'pc/utils/env';
import apitableLogoDark from 'static/icon/datasheet/APITable_brand_dark.png';
import apitableLogoLight from 'static/icon/datasheet/APITable_brand_light.png';
import vikaLogoDark from 'static/icon/datasheet/vika_logo_brand_dark.png';
import vikaLogoLight from 'static/icon/datasheet/vika_logo_brand_light.png';
import { EmbedContext } from './embed_context';
import { EmbedFail } from './embed_fail';
import { useListenerIframeMessage } from './hooks/use_listener_iframe_message';
import styles from './style.module.less';

const _SplitPane: any = SplitPane;

export interface IEmbedProps {
  embedId: string;
}

export const Embed: React.FC<IEmbedProps> = (embedProps) => {
  const { sideBarVisible, setSideBarVisible } = useSideBarVisible();
  const { embedId } = embedProps;
  const { datasheetId, folderId, viewId, dashboardId, formId } = useSelector((state) => state.pageParams);
  const treeNodesMap = useSelector((state) => state.catalogTree.treeNodesMap);
  const [nodeTree, setNodeTree] = useState<INodeTree>();
  const [parentFolderId, setParentFolderId] = useState();

  const [embedClose, setEmbedClose] = useState(false);
  const { getEmbedInfoReq } = useSpaceRequest();

  const dispatch = useAppDispatch();
  const [embedConfig, setEmbedCofig] = useState<IEmbedInfo>();
  const { data: embedData, loading } = useRequest<any>(() => getEmbedInfoReq(embedId));
  const isLogin = useSelector((state) => state.user.isLogin);
  const [isShowLoginButton, setIsShowLoginButton] = useState<boolean>(true);
  const { IS_APITABLE, IS_AITABLE, LONG_DARK_LOGO, LONG_LIGHT_LOGO } = getEnvVariables();
  const LightLogo = IS_AITABLE ? integrateCdnHost(LONG_LIGHT_LOGO!) : IS_APITABLE ? apitableLogoLight : vikaLogoLight;
  const DarkLogo = IS_AITABLE ? integrateCdnHost(LONG_DARK_LOGO!) : IS_APITABLE ? apitableLogoDark : vikaLogoDark;
  const [themeName, setTheme] = useLocalStorageState<ThemeName>('theme', {
    defaultValue: (getEnvVariables().SYSTEM_CONFIGURATION_DEFAULT_THEME as ThemeName) || ThemeName.Light,
  });
  const [, setSystemTheme] = useLocalStorageState<SystemTheme>('systemTheme', { defaultValue: SystemTheme.Close });

  usePageParams();
  useNotifyNOdeNameChange();
  useListenerIframeMessage();

  useEffect(() => {
    window.dispatchEvent(new Event('resize'));
  }, [sideBarVisible]);

  const loginHandler = () => {
    const reference = window.location.href;
    Router.redirect(Navigation.LOGIN, { query: { reference } });
  };

  useEffect(() => {
    if (!embedData || !embedData.nodeInfo) {
      setEmbedClose(true);
      return;
    }

    setEmbedClose(false);
    const { embedInfo, nodeInfo, spaceId, spaceInfo } = embedData;
    const {
      labs: { viewManualSave, robot },
    } = spaceInfo;
    const { nodeTree = [], shareNodeIcon = '', theme, payload: embedSetting, linkId } = embedInfo;

    const { nodeName, id: nodeId, icon, parentId } = nodeInfo;

    if (embedSetting.permissionType === PermissionType.PRIVATEEDIT && !isLogin) {
      loginHandler();
    }

    if (theme) {
      dispatch(StoreActions.setTheme(theme));
      setTheme(theme);
      setSystemTheme(SystemTheme.Close);
      const html = document.querySelector('html');
      html?.setAttribute('data-theme', theme);
    }

    setSideBarVisible(false);
    setNodeTree({
      nodeId,
      nodeName,
      type: ConfigConstant.NodeType.DATASHEET,
      icon: icon,
      children: nodeTree,
    });

    setParentFolderId(parentId);
    setEmbedCofig(embedSetting);

    if (embedInfo.isFolder && nodeTree.length === 0) {
      return;
    }

    dispatch(StoreActions.addNodeToMap(Selectors.flatNodeTree([...nodeTree, { nodeId, nodeName, icon: shareNodeIcon }])));
    dispatch(StoreActions.setLabs([robot ? 'robot' : '']));

    const isShowEmbedToolBar =
      get(embedSetting, 'viewControl.toolBar.basicTools', false) ||
      get(embedSetting, 'viewControl.toolBar.shareBtn', false) ||
      get(embedSetting, 'viewControl.toolBar.apiBtn', false) ||
      get(embedSetting, 'viewControl.toolBar.formBtn', false) ||
      get(embedSetting, 'viewControl.toolBar.historyBtn', false) ||
      get(embedSetting, 'viewControl.toolBar.robotBtn', false) ||
      get(embedSetting, 'viewControl.toolBar.widgetBtn', false);

    dispatch(StoreActions.setEmbedInfo({ ...embedSetting, spaceId, isShowEmbedToolBar, viewManualSave }));
    if (datasheetId) {
      return;
    }
    setTimeout(() => {
      Router.push(Navigation.EMBED_SPACE, {
        params: { embedId: linkId, nodeId, viewId: embedSetting.viewControl.viewId },
      });
    }, 0);

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(embedData)]);

  // Embed Close
  if (((viewId && embedConfig?.viewControl?.viewId && viewId !== embedConfig?.viewControl?.viewId) || embedClose) && !loading) {
    return <EmbedFail />;
  }

  const getComponent = () => {
    if (!nodeTree) {
      return null;
    }

    if (datasheetId) {
      return <DataSheetPane />;
    }

    if (dashboardId) {
      return <DashboardPanel />;
    }

    if (formId) {
      return <FormPanel loading={loading} />;
    }

    if (folderId) {
      const parentNode = findNode([nodeTree], folderId);
      const childNodes = (parentNode && parentNode.children) ?? [];
      return (
        <FolderShowcase
          nodeInfo={{
            name: treeNodesMap[folderId]?.nodeName || '',
            id: folderId,
            icon: treeNodesMap[folderId]?.icon || '',
          }}
          childNodes={childNodes}
          readOnly
        />
      );
    }
    return null;
  };

  return (
    <EmbedContext.Provider value={{ folderId: parentFolderId }}>
      <Head>
        <meta property="og:title" content={embedData?.nodeInfo?.nodeName || t(Strings.og_site_name_content)} />
        <meta property="og:type" content="website" />
        <meta property="og:url" content={window.location.href} />
        <meta property="og:site_name" content={t(Strings.og_site_name_content)} />
        <meta property="og:description" content={t(Strings.og_product_description_content)} />
      </Head>
      <div
        className={classNames(styles.share, {
          [styles.hiddenCatalog]: !sideBarVisible,
          [styles.iframeShareContainer]: true,
        })}
      >
        <_SplitPane
          split="vertical"
          minSize={320}
          defaultSize={0}
          maxSize={640}
          style={{ overflow: 'none' }}
          size={0}
          allowResize={sideBarVisible}
          onChange={() => {
            window.dispatchEvent(new Event('resize'));
          }}
          pane2Style={{ overflow: 'hidden' }}
          resizerStyle={{ backgroundColor: 'transparent', minWidth: 'auto' }}
        >
          <div className={styles.splitLeft} />
          <div
            className={classNames(styles.embedContainer, {
              // [styles.containerAfter]: !isIframe(),
              [styles.iframeShareContainer]: true,
            })}
            style={{
              height: '100%',
              paddingBottom: embedConfig?.bannerLogo ? '40px' : '',
              minWidth: dashboardId ? 'unset' : '',
            }}
          >
            <div className={styles.wrapper}>{getComponent()}</div>
          </div>
        </_SplitPane>
        {embedConfig?.bannerLogo && (
          <div className={styles.brandContainer}>
            {
              <Image
                src={themeName === ThemeName.Light ? LightLogo : DarkLogo}
                width={IS_AITABLE ? 132 : IS_APITABLE ? 111 : 75}
                height={IS_AITABLE ? 29 : 20}
                alt=""
              />
            }
          </div>
        )}
        {!isLogin && embedConfig?.permissionType === PermissionType.PUBLICEDIT && isShowLoginButton && (
          <div className={styles.loginBg}>
            <div className={styles.loginButton}>
              <p>{t(Strings.share_editor)}</p>
              <Button color="primary" size="small" className={styles.applicationBtn} onClick={loginHandler}>
                {t(Strings.login)}
              </Button>
              <CloseOutlined className={styles.closeBtn} onClick={() => setIsShowLoginButton(false)} />
            </div>
          </div>
        )}
      </div>
    </EmbedContext.Provider>
  );
};
