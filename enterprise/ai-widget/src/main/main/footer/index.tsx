import React, { useState } from 'react';
import {
  AIType, getIsFromIframe, TextArea, TextAreaWrapper, TextButton, useAIContext, ScreenSize, useResponsive
} from '@apitable/ai';
import { IconButton, Tooltip, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { ClearOutlined, FormOutlined, LinkOutlined } from '@apitable/icons';
// import { SubscribeUsageTipType, triggerUsageAlert } from 'enterprise/billing';
import styles from './style.module.less';

export function Footer() {
  const {
    context,
    isTrainCompleted,
    isIdle,
    disabledSubmit,
    isRegisterSendURLMethod,
    isRegisterSendFormMethod,
    newConversation,
    sendLinkMessage,
    startAIFormMode,
    sendMessage,
  } = useAIContext();
  const [loading, setLoading] = useState(false);
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.md);
  const colors = useThemeColors();

  const { messageCreditLimit } = context.data;
  const remainCredit = (messageCreditLimit?.maxCreditNums - messageCreditLimit.remainCreditNums).toFixed(2);
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
      isMobile ? (
        <Tooltip content={t(Strings.ai_new_conversation_btn_text)}>
          <div>
            <IconButton disabled={disabled} onClick={newConversation} icon={ClearOutlined} style={{ color: colors.fc2 }} />
          </div>
        </Tooltip>
      ) : (
        <TextButton disabled={disabled} onClick={newConversation} prefixIcon={<ClearOutlined />}>
          {t(Strings.ai_new_conversation_btn_text)}
        </TextButton>
      ),
    );

    if (isTrainCompleted && isRegisterSendURLMethod) {
      const openUrlTitle = (context.config.type === AIType.Qa && context.config.openUrlTitle) || t(Strings.ai_open_url_btn_text);
      toolbar.push(
        isMobile ? (
          <Tooltip content={openUrlTitle}>
            <div>
              <IconButton disabled={disabled} onClick={sendLinkMessage} icon={LinkOutlined} style={{ color: colors.fc2 }} />
            </div>
          </Tooltip>
        ) : (
          <TextButton disabled={disabled} onClick={sendLinkMessage} prefixIcon={<LinkOutlined />}>
            {openUrlTitle}
          </TextButton>
        ),
      );
    }

    if (isTrainCompleted && isRegisterSendFormMethod) {
      toolbar.push(
        isMobile ? (
          <Tooltip content={context.data.formName || ''}>
            <div>
              <IconButton disabled={disabled || loading} onClick={sendForm} icon={FormOutlined} style={{ color: colors.fc2 }} />
            </div>
          </Tooltip>
        ) : (
          <TextButton
            disabled={disabled || loading}
            onClick={sendForm}
            prefixIcon={<FormOutlined />}
          >
            {context.data.formName}
          </TextButton>
        ),
      );
    }
    return toolbar;
  };

  const canSend = () => {
    // const result = triggerUsageAlert('maxMessageCredits', { usage: Number(remainCredit), alwaysAlert: true }, SubscribeUsageTipType.Alert);
    // if (result) {
    //   return false;
    // }
    return true;
  };

  return (
    <TextAreaWrapper leftToolbar={renderFooterToolbar()}>
      <TextArea canSend={canSend} sendMessage={sendMessage} disabled={disabledSubmit} />
    </TextAreaWrapper>
  );
}
