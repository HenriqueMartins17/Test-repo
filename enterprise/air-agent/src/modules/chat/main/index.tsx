import { useGlobalContext } from 'context/global';
import React, { useEffect } from 'react';
import { Chat, getIsFromIframe, useAIContext, ConversationStatus } from '@apitable/ai';
import WizardAction from '../cui/action/wizard';
import { Footer } from './footer';
import styles from './style.module.less';
import { ToolBar } from './tool_bar/tool_bar';
import { WidgetBar } from './widget_bar/widget_bar';

interface IProps {
  isShareMode?: boolean;
}

export function ChatPageMain(props: IProps) {
  const hook = useAIContext();
  const { context } = hook;
  const { context: globalContext } = useGlobalContext();
  const user = globalContext.user;
  const { data: aiInfo } = context;
  const isFromIframe = getIsFromIframe();

  useEffect(() => {
    if (context.isWizardMode && context.state.conversationStatus !== ConversationStatus.Form) {
      if (globalContext.isLogin) {
        console.log('init wizard action');
        const action = WizardAction.create(context);
        action.start();
      } else {
        // AI 还未训练完成 未登录 这个可能性不大 不做处理
      }
    }
  }, [context.data.id]);

  return (
    <div className={styles.chatPage}>
      {(isFromIframe || props.isShareMode) ? <WidgetBar name={aiInfo.name} /> : <ToolBar />}
      <Chat hook={hook} userInfo={user as any} />
      <Footer />
    </div>
  );
}
