import { Col, Row } from 'antd';
import React from 'react';
import { getIsFromIframe } from '@apitable/ai';
import { IconButton, Typography, useThemeColors } from '@apitable/components';
import { CloseOutlined } from '@apitable/icons';
import ChatBotAvatar from 'static/icon/chat/chatbot_avatar.png';

export const WidgetBar: React.FC<{ name: string }> = ({ name }) => {
  const color = useThemeColors();
  const isFromIframe = getIsFromIframe();

  const onClose = () => {
    window.parent.postMessage({
      name: 'aitable-widget',
      action: 'close',
    }, '*');
  };

  return <Row
    align={'middle'}
    style={{
      height: 48,
      minHeight: 48,
      padding: '0 16px',
      background: color.bgCommonDefault,
      borderBottom: `1px solid ${color.borderCommonDefault}`,
    }}
  >
    <Col flex={1} style={{ display: 'flex', alignItems: 'center' }}>
      <span style={{ borderRadius: '50%', marginRight: 4 }}>
        <img src={ChatBotAvatar.src} alt="" width={32} height={32} />
      </span>
      <Typography variant={'h7'} component={'span'}>
        {name}
      </Typography>
    </Col>
    {
      isFromIframe && (
        <Col>
          <IconButton icon={CloseOutlined} onClick={onClose} />
        </Col>
      )
    }

  </Row>;
};
