import { Badge, Col, Row } from 'antd';
import * as React from 'react';
import { useCallback, useState } from 'react';
import { Insight, TextButton, useAIContext, Setting } from '@apitable/ai';
import { useThemeColors, useResponsive, ScreenSize } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { LogOutlined, PublishOutlined, SettingOutlined, TrainOutlined } from '@apitable/icons';
import { widgets } from './setting';
import { Share } from './share';
import styles from './style.module.less';
import { Train } from './train/train';

enum ModalType {
  Setting,
  Share,
  Train,
  Api,
  None,
  Insight,
}

export const ToolBar: React.FC = () => {
  const [visibleModal, setVisibleModal] = useState(ModalType.None);
  const { context } = useAIContext();
  const color = useThemeColors();
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.sm);
  const locale = localStorage.getItem('client-lang') ?? 'en-US';

  const hideSettingsModal = useCallback(() => {
    setVisibleModal(ModalType.None);
  }, []);

  return (
    <>
      <Row align={'middle'} className={styles.toolbar} style={{ paddingLeft: isMobile ? 0 : '' }}>
        <Col span={isMobile ? 21 : 8}>
          <div id={context.data.id}>{context.data.name}</div>
        </Col>
        {!isMobile && (
          <Col span={16}>
            <Row justify={'end'} align={'middle'}>
              <Col className={styles.operateItem}>
                <TextButton onClick={() => setVisibleModal(ModalType.Setting)} disabled={context.isWizardMode} prefixIcon={<SettingOutlined />}>
                  {t(Strings.ai_toolbar_setting_text)}
                </TextButton>
              </Col>
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
              <Col className={styles.operateItem}>
                <TextButton
                  onClick={() => setVisibleModal(ModalType.Share)}
                  disabled={context.isWizardMode}
                  prefixIcon={<PublishOutlined size={16} />}
                >
                  {t(Strings.ai_toolbar_publish_text)}
                </TextButton>
              </Col>
              <Col className={styles.operateItem}>
                <TextButton onClick={() => setVisibleModal(ModalType.Insight)} disabled={context.isWizardMode} prefixIcon={<LogOutlined size={16} />}>
                  {t(Strings.ai_toolbar_insight_text)}
                </TextButton>
              </Col>
            </Row>
          </Col>
        )}
      </Row>
      <Setting widgets={widgets} visible={visibleModal === ModalType.Setting} close={hideSettingsModal} locale={locale === 'zh-CN' ? 'zh' : 'en'} />
      <Share visible={visibleModal === ModalType.Share} close={hideSettingsModal} />
      <Insight id={context.data.id} visible={visibleModal === ModalType.Insight} close={hideSettingsModal} />
      <Train visible={visibleModal === ModalType.Train} close={hideSettingsModal} />
    </>
  );
};
