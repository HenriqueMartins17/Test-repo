import classNames from 'classnames';
import React, { useState } from 'react';
import { AIType, getIsFromIframe, TextArea, TextAreaWrapper, TextButton, useAIContext } from '@apitable/ai';
import { IconButton, Tooltip, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { ClearOutlined, FormOutlined, LinkOutlined, HistoryOutlined } from '@apitable/icons';
import { ScreenSize } from 'pc/components/common/component_display';
import { useResponsive } from 'pc/hooks';
import { getEnvVariables } from 'pc/utils/env';
import { SubscribeUsageTipType, triggerUsageAlert } from 'enterprise/billing';
import styles from './style.module.less';

interface IProps {
  isFromIframe?: boolean;
  setOpenHistory?: (open: boolean) => void;
  openHistory?: boolean;
  isLogin?: boolean;
  isAllowHistorySend?: boolean;
}

export function Footer(props: IProps) {
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
  const isFromIframe = props.isFromIframe || getIsFromIframe();
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
      )
    );

    if (isTrainCompleted && isRegisterSendURLMethod && !context.history.current) {
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
        )
      );
    }

    if (isTrainCompleted && isRegisterSendFormMethod && !context.history.current) {
      toolbar.push(
        isMobile ? (
          <Tooltip content={context.data.setting.formName || ''}>
            <div>
              <IconButton disabled={disabled || loading} onClick={sendForm} icon={FormOutlined} style={{ color: colors.fc2 }} />
            </div>
          </Tooltip>
        ) : (
          <TextButton disabled={disabled || loading} onClick={sendForm} prefixIcon={<FormOutlined />}>
            {context.data.setting.formName}
          </TextButton>
        )
      );
    }
    return toolbar;
  };

  const renderRightCreditStatus = () => {
    const toolbar: React.ReactNode[] = [];
    if (props.isLogin && props.setOpenHistory) {
      toolbar.push(
        isMobile ? (
          <Tooltip content={t(Strings.ai_agent_history)}>
            <div>
              <IconButton
                className={classNames({
                  [styles.active]: props.openHistory,
                })}
                onClick={() => {
                  if (props.setOpenHistory) {
                    props.setOpenHistory(!props.openHistory);
                  }
                }}
                icon={HistoryOutlined}
                style={{ color: colors.fc2 }}
              />
            </div>
          </Tooltip>
        ) : (
          <TextButton
            className={classNames({
              [styles.active]: props.openHistory,
            })}
            onClick={() => {
              if (props.setOpenHistory) {
                props.setOpenHistory(!props.openHistory);
              }
            }}
            prefixIcon={<HistoryOutlined />}
          >
            {t(Strings.ai_agent_history)}
          </TextButton>
        )
      );
    }
    if (!isFromIframe) {
      toolbar.push(
        <Tooltip key="credit" content={t(Strings.ai_input_credit_usage_tooltip)}>
          <div className={styles.queryCount}>{t(Strings.ai_remain_credit_label, { credit: messageCreditLimit.remainChatTimes })}</div>
        </Tooltip>
      );
    }
    return toolbar;
  };

  const canSend = () => {
    if (getEnvVariables().IS_APITABLE) {
      const result = triggerUsageAlert('maxMessageCredits', { usage: Number(remainCredit), alwaysAlert: true }, SubscribeUsageTipType.Alert);
      if (result) {
        return false;
      }
    }
    return true;
  };

  return (
    <TextAreaWrapper leftToolbar={renderFooterToolbar()} rightToolbar={renderRightCreditStatus()}>
      <TextArea
        tooltip={(props.isAllowHistorySend === false && t(Strings.ai_agent_conversation_continue_not_supported)) || undefined}
        canSend={canSend}
        sendMessage={sendMessage}
        disabled={disabledSubmit || props.isAllowHistorySend === false}
      />
    </TextAreaWrapper>
  );
}
