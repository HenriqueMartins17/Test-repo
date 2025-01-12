import { Badge, Col, Row } from 'antd';
import * as React from 'react';
import { useCallback, useState } from 'react';
import { useSelector } from 'react-redux';
import { Insight, TextButton, useAIContext, Setting } from '@apitable/ai';
import { useThemeColors, useResponsive, ScreenSize } from '@apitable/components';
import { ConfigConstant, IReduxState, Strings, t } from '@apitable/core';
import { ApiOutlined, ListOutlined, LogOutlined, PublishOutlined, SettingOutlined, TrainOutlined } from '@apitable/icons';
import { NodeInfoBar } from 'pc/components/common/node_info_bar';
import { useSideBarVisible } from 'pc/hooks';
import { useAppSelector } from 'pc/store/react-redux';
import { ApiWrapper } from './api/api_wrapper';
import { widgets } from './setting';
import { Share } from './share';
import { Train } from './train/train';
import styles from './style.module.less';

enum ModalType {
    Setting,
    Train,
    Api,
    None,
    Insight,
}

export const ToolBar: React.FC = () => {
  const [visibleModal, setVisibleModal] = useState(ModalType.None);
  const [shareNodeId, setShareNodeId] = useState<string>('');
  const { context } = useAIContext();
  const aiId = context.data.id;
  const { shareId } = useSelector((state: IReduxState) => state.pageParams);
  const node = useSelector((state: IReduxState) =>
    state.catalogTree.treeNodesMap[aiId] || state.catalogTree.privateTreeNodesMap[aiId]
  );
  const locale = useAppSelector((state) => state.user.info?.locale);
  const aiName = node?.nodeName;
  const role = node?.role;
  const color = useThemeColors();
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.sm);
  const { setSideBarVisible } = useSideBarVisible();
  const colors = useThemeColors();

  const { editable } = node?.permissions || {};
  const isManager = role === ConfigConstant.Role.Manager;
  const hideSettingsModal = useCallback(() => {
    setVisibleModal(ModalType.None);
  }, []);

  return (
    <>
      <Row align={'middle'} className={styles.toolbar} style={{ paddingLeft: isMobile ? 0 : '' }}>
        {isMobile && (
          <Col span={isMobile ? 3 : 0}>
            <div onClick={() => setSideBarVisible(true)} className={styles.side}>
              <ListOutlined size={20} color={colors.black[50]} />
            </div>
          </Col>
        )}

        <Col span={isMobile ? 21 : 8}>
          <NodeInfoBar
            data={{
              nodeId: aiId!,
              type: ConfigConstant.NodeType.AI,
              icon: node?.icon,
              name: aiName,
              role: role === ConfigConstant.Role.Foreigner && editable ? ConfigConstant.Role.Editor : role,
              favoriteEnabled: node?.nodeFavorite,
              nameEditable: editable,
              iconEditable: editable,
            }}
            hiddenModule={{ favorite: Boolean(shareId) }}
          />
        </Col>
        {!isMobile && !shareId && (
          <Col span={16}>
            <Row justify={'end'} align={'middle'}>
              {isManager && (
                <Col className={styles.operateItem}>
                  <TextButton onClick={() => setVisibleModal(ModalType.Setting)} disabled={context.isWizardMode} prefixIcon={<SettingOutlined />}>
                    {t(Strings.ai_toolbar_setting_text)}
                  </TextButton>
                </Col>
              )}
              {isManager && (
                <Col className={styles.operateItem}>
                  {context.data.dataSourcesUpdated ? (
                    <Badge dot offset={[-14, 12]} style={{ boxShadow: 'unset', background: color.textDangerDefault, width: 8, height: 8 }}>
                      <TextButton
                        onClick={() => setVisibleModal(ModalType.Train)}
                        disabled={context.isWizardMode}
                        prefixIcon={<TrainOutlined size={16} />}
                      >
                        {t(Strings.ai_toolbar_training)}
                      </TextButton>
                    </Badge>
                  ) : (
                    <TextButton
                      onClick={() => setVisibleModal(ModalType.Train)}
                      disabled={context.isWizardMode}
                      prefixIcon={<TrainOutlined size={16} />}
                    >
                      {t(Strings.ai_toolbar_training)}
                    </TextButton>
                  )}
                </Col>
              )}
              {isManager && (
                <Col className={styles.operateItem}>
                  <TextButton
                    onClick={() => setShareNodeId(context.data.id)}
                    disabled={context.isWizardMode}
                    prefixIcon={<PublishOutlined size={16} />}
                  >
                    {t(Strings.ai_toolbar_publish_text)}
                  </TextButton>
                </Col>
              )}
              <Col className={styles.operateItem}>
                <TextButton disabled={context.isWizardMode} prefixIcon={<ApiOutlined size={16} />} onClick={() => setVisibleModal(ModalType.Api)}>
                                    API
                </TextButton>
              </Col>
              {isManager && (
                <Col className={styles.operateItem}>
                  <TextButton
                    onClick={() => setVisibleModal(ModalType.Insight)}
                    disabled={context.isWizardMode}
                    prefixIcon={<LogOutlined size={16} />}
                  >
                    {t(Strings.ai_toolbar_insight_text)}
                  </TextButton>
                </Col>
              )}
            </Row>
          </Col>
        )}
      </Row>

      <Setting locale={locale === 'zh-CN' ? 'zh' : 'en'} widgets={widgets} visible={visibleModal === ModalType.Setting} close={hideSettingsModal} />
      <Share nodeId={shareNodeId} onClose={() => setShareNodeId('')} />
      <ApiWrapper visible={visibleModal === ModalType.Api} close={hideSettingsModal} />
      <Insight id={context.data.id} visible={visibleModal === ModalType.Insight} close={hideSettingsModal} />
      <Train visible={visibleModal === ModalType.Train} close={hideSettingsModal} />
    </>
  );
};
