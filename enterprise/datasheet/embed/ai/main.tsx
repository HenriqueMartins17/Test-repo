import React from 'react';
import { useSelector } from 'react-redux';
import { Chat, getIsFromIframe, useAIContext } from '@apitable/ai';
import { Footer } from 'enterprise/chat/main/footer';
import { WidgetBar } from 'enterprise/chat/main/widget_bar/widget_bar';
import styles from './style.module.less';
import 'enterprise/chat/cui/action';

export function ChatPageMain() {
  const hook = useAIContext();
  const { context } = hook;
  const user = useSelector((state) => state.user);
  const { data: aiInfo } = context;
  const isFromIframe = getIsFromIframe();

  return (
    <div className={styles.chatPage}>
      <div className={styles.chatPageMain}>
        <WidgetBar name={aiInfo.name} />
        <Chat
          userInfo={user.info}
          chatList={context.state.chatList}
          conversationStatus={context.state.conversationStatus}
          stopConversation={hook.stopConversation}
          toolBarState={{
            config: context.config,
            formName: context.data.formName,
            isIdle: hook.isIdle,
            isRegisterSendURLMethod: hook.isRegisterSendURLMethod,
            isRegisterSendFormMethod: hook.isRegisterSendFormMethod,
            sendLinkMessage: hook.sendLinkMessage,
            startAIFormMode: hook.startAIFormMode,
            sendFeedback: hook.sendFeedback,
          }}
          sendMessage={hook.sendMessage}
          refreshExploreCard={hook.refreshExploreCard}
          feedbackList={context.state.feedback}
        />
        <Footer isFromIframe={isFromIframe} />
      </div>
    </div>
  );
}
