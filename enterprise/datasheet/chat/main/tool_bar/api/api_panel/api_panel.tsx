import { Tabs } from 'antd';
import classnames from 'classnames';
import { useState } from 'react';
import { useSelector } from 'react-redux';
import { Checkbox, IconButton, Typography, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { ApiOutlined, BookOutlined, CloseOutlined } from '@apitable/icons';
import { Message } from 'pc/components/common';
import { AccountCenterModal } from 'pc/components/navigation/account_center_modal';
import { getEnvVariables } from 'pc/utils/env';
// import styles from './styles.module.less';
import { Share } from '../../share';
import { ApiDoc } from '../api_doc/api_doc';
import styles from './style.module.less';

interface IApiPanelProps {
  close: () => void;
}

export const ApiPanel: React.FC<IApiPanelProps> = ({ close }) => {
  const colors = useThemeColors();
  const { aiId } = useSelector(state => state.pageParams);
  const apiToken = useSelector(state => state.user.info!.apiKey);
  const [showApiToken, _setShowApiToken] = useState(false);
  const token = showApiToken ? apiToken : t(Strings.api_your_token);
  const [shareNodeId, setShareNodeId] = useState('');
  const [showAccountCenter, setShowAccountCenter] = useState(false);

  const setShowApiToken = (checked: boolean) => {
    if (apiToken) {
      _setShowApiToken(checked);
    } else {
      Message.warning({ content: t(Strings.api_token_generate_tip), duration: 6 });
      setShowAccountCenter(true);
    }
  };

  return (
    <div className={styles.apiPanel}>
      <h1 className={classnames(styles.panelTitle, 'vk-flex vk-justify-between vk-items-center')}>
        <div className={'vk-flex vk-items-center'}>
          <ApiOutlined size={24} color={colors.primaryColor} />
          <span className={'vk-ml-1 vk-mr-4'}>
            {t(Strings.api_panel_title)}
          </span>
          {
            getEnvVariables().AI_API_HELP_URL && <span
              onClick={() => {
                window.open(getEnvVariables().AI_API_HELP_URL, '_blank', 'noopener=yes,noreferrer=yes');
              }}
              className={'vk-flex vk-items-center vk-space-x-1'}
            >
              <BookOutlined color={colors.textCommonTertiary} />
              <Typography color={colors.textCommonTertiary} variant={'body3'}>
                {t(Strings.document_detail)}
              </Typography>
            </span>
          }
        </div>
        <IconButton icon={CloseOutlined} size={'small'} color={'white'} onClick={close} />
      </h1>

      <div className={'vk-flex vk-justify-end'}>
        <Checkbox size={14} onChange={setShowApiToken} checked={showApiToken}>
          <Typography variant={'body3'} color={colors.textStaticPrimary} style={{ marginLeft: '8px !important' }}>
            {t(Strings.api_show_token)}
          </Typography>
        </Checkbox>
      </div>

      <Tabs defaultActiveKey={'fields'} hideAdd>
        <Tabs.TabPane tab={t(Strings.ai_api_tab_title_1)} key="fields">
          <ApiDoc
            token={token}
          />
        </Tabs.TabPane>
      </Tabs>
      <Typography
        color={colors.textCommonTertiary} variant={'body4'}
        style={{ margin: '16px 0', textAlign: 'center', textDecoration: 'underline', cursor: 'pointer' }}
        onClick={() => setShareNodeId(aiId!)}
      >
        {t(Strings.ai_api_footer_desc)}
      </Typography>
      <Share nodeId={shareNodeId} onClose={() => setShareNodeId('')} />
      {showAccountCenter && <AccountCenterModal defaultActiveItem={4} setShowAccountCenter={setShowAccountCenter} />}
    </div>
  );
};
