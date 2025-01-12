import React, { useState } from 'react';
import { AIType, getIsFromIframe, TextArea, TextAreaWrapper, TextButton, useAIContext } from '@apitable/ai';
import { Tooltip, useThemeColors } from '@apitable/components';
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
  const colors = useThemeColors();

  // const remainCredit = (messageCreditLimit?.maxCreditNums - messageCreditLimit.remainCreditNums).toFixed(2);
  const isFromIframe = getIsFromIframe();
  const disabled = !isTrainCompleted || !isIdle;

  const renderFooterToolbar = () => {
    const toolbar: React.ReactNode[] = [];
    toolbar.push(
      <TextButton disabled={disabled} onClick={newConversation} prefixIcon={<ClearOutlined />}>
        {t(Strings.ai_new_conversation_btn_text)}
      </TextButton>,
    );

    if (isTrainCompleted && isRegisterSendURLMethod) {
      const openUrlTitle = (context.config.type === AIType.Qa && context.config.openUrlTitle) || t(Strings.ai_open_url_btn_text);
      toolbar.push(
        <TextButton disabled={disabled} onClick={sendLinkMessage} prefixIcon={<LinkOutlined />}>
          {openUrlTitle}
        </TextButton>,
      );
    }

    if (isTrainCompleted && isRegisterSendFormMethod) {
      toolbar.push(
        <TextButton
          disabled={disabled || loading}
          onClick={async () => {
            // prevent duplicate clicks
            try {
              setLoading(true);
              await startAIFormMode();
            } finally {
              setLoading(false);
            }
          }}
          prefixIcon={<FormOutlined />}
        >
          {context.data.formName}
        </TextButton>,
      );
    }
    return toolbar;
  };

  const renderRightCreditStatus = () => {
    if (!isFromIframe) {
      return [
        <Tooltip key="credit" content={t(Strings.ai_input_credit_usage_tooltip)}>
          <div className={styles.queryCount}>{t(Strings.ai_remain_credit_label, { credit: 111 })}</div>
        </Tooltip>,
      ];
    }
    return [];
  };

  const canSend = () => {
    // todo: check credit
    // if (getEnvVariables().IS_APITABLE) {
    //   const result = triggerUsageAlert('maxMessageCredits', { usage: Number(remainCredit), alwaysAlert: true }, SubscribeUsageTipType.Alert);
    //   if (result) {
    //     return false;
    //   }
    // }
    return true;
  };

  return (
    <TextAreaWrapper leftToolbar={renderFooterToolbar()} rightToolbar={renderRightCreditStatus()}>
      <TextArea canSend={canSend} sendMessage={sendMessage} disabled={disabledSubmit} />
    </TextAreaWrapper>
  );
}
