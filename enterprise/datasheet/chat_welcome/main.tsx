import { Row, Col } from 'antd';
import classNames from 'classnames';
import { useState } from 'react';
import { useSelector } from 'react-redux';
import {
  Chat,
  useAIContext,
  TextArea,
  AIType,
} from '@apitable/ai';
import { t, Strings } from '@apitable/core';
import { ClearOutlined, LinkOutlined, FormOutlined } from '@apitable/icons';
import { ScreenSize } from 'pc/components/common/component_display';
import { useResponsive } from 'pc/hooks';
import style from './index.module.less';

export const ChatWelcomeMain = () => {

  const {
    context,
    isTrainCompleted,
    isIdle,
    isRegisterSendURLMethod,
    isRegisterSendFormMethod,
    newConversation,
    sendLinkMessage,
    startAIFormMode,
  } = useAIContext();
  const [loading, setLoading] = useState(false);

  const user = useSelector((state) => state.user);
  const hook = useAIContext();
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.md);
  const disabled = !isTrainCompleted || !isIdle;

  const sendForm = async () => {
    // prevent duplicate clicks
    try {
      setLoading(true);
      await startAIFormMode();
    } finally {
      setLoading(false);
    }
  };

  const renderFooterToolbar = () => {
    const toolbar: React.ReactNode[] = [];
    toolbar.push(
      <div className={classNames(style.mainToolbarItem, { [style.disabled]: disabled })} onClick={newConversation}>
        <ClearOutlined />
        <em>{!isMobile && t(Strings.ai_new_conversation_btn_text)}</em>
      </div>
    );

    if (isTrainCompleted && isRegisterSendURLMethod) {
      const openUrlTitle = (context.config.type === AIType.Qa && context.config.openUrlTitle) || t(Strings.ai_open_url_btn_text);
      toolbar.push(
        <div className={classNames(style.mainToolbarItem, { [style.disabled]: disabled })} onClick={sendLinkMessage}>
          <LinkOutlined />
          <em>{!isMobile && openUrlTitle}</em>
        </div>
      );
    }

    if (isTrainCompleted && isRegisterSendFormMethod) {
      toolbar.push(
        <div className={classNames(style.mainToolbarItem, { [style.disabled]: disabled || loading })} onClick={sendForm}>
          <FormOutlined />
          <em>{!isMobile && context.data.setting.formName}</em>
        </div>
      );
    }
    return toolbar;
  };

  const grid = {
    xs: { span: 24 },
    lg: { span: 24 },
    xl: { span: 20, offset: 2 },
    xxl: { span: 18, offset: 3 },
  };
  return (
    <div className={classNames(style.main, { [style.mobile]: isMobile })}>
      <Chat
        isFusionMode
        userInfo={user.info}
        chatList={context.state.chatList}
        conversationStatus={context.state.conversationStatus}
        stopConversation={hook.stopConversation}
        sendMessage={hook.sendMessage}
        refreshExploreCard={hook.refreshExploreCard}
      />

      <Row className={style.mainFooter}>
        <Col {...grid} className={style.mainToolbar}>
          { renderFooterToolbar() }
        </Col>
      </Row>
      <Row className={style.mainFooter}>
        <Col {...grid}>
          <TextArea className={style.mainTextArea} type="copilot" sendMessage={hook.sendMessage} disabled={hook.disabledSubmit} />
        </Col>
      </Row>
    </div>
  );
};
