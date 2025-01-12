import React, { useEffect, useMemo } from 'react';
import { useSelector } from 'react-redux';
import { Chat,
  getIsFromIframe,
  useAIContext,
  ConversationStatus,
  History
} from '@apitable/ai';
import { t, Strings } from '@apitable/core';
import { LoadingOutlined } from '@apitable/icons';
import { Footer } from './footer';
import { ToolBar } from './tool_bar/tool_bar';
import { WidgetBar } from './widget_bar/widget_bar';
import { WizardAction } from 'enterprise/chat/cui/action';
import styles from './style.module.less';

interface IProps {
  isFromIframe?: boolean;
}

export function ChatPageMain(props: IProps) {
  const hook = useAIContext();
  const { context } = hook;
  const user = useSelector(state => state.user);
  const { data: aiInfo } = context;
  const isFromIframe = props.isFromIframe || getIsFromIframe();
  const { shareId } = useSelector((state) => state.pageParams);
  const currentConversation = {
    conversationId: context.state.currentConversationId,
    trainingId: context.state.trainingId,
  };

  useEffect(() => {
    if (context.isWizardMode && context.state.conversationStatus !== ConversationStatus.Form) {
      if (user.isLogin) {
        console.log('init wizard action');
        const action = WizardAction.create(context);
        action.start();
      } else {
        // AI 还未训练完成 未登录 这个可能性不大 不做处理
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [context.data.id]);

  const isAllowHistorySend = useMemo(() => {
    if (context.history.current) {
      if (!currentConversation.conversationId && context.history.current.id === context.history.data[0]?.id) {
        return true;
      }
      if (
        context.history.current.id !== currentConversation.conversationId ||
        context.history.current.trainingId !== currentConversation.trainingId
      ) {
        return false;
      }
    }
    return true;
  }, [context.history, currentConversation.conversationId, currentConversation.trainingId]);

  return (
    <div className={styles.chatPage}>
      <div className={styles.chatPageMain}>
        { isFromIframe || shareId ? <WidgetBar name={aiInfo.name} /> : <ToolBar /> }
        {
          !context.isInit ? (
            <div className={styles.pageLoading}>
              <LoadingOutlined className="circle-loading" />
              {t(Strings.ai_page_loading_text)}
            </div>
          ) : (
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
          )
        }
        <Footer
          isLogin={user.isLogin}
          openHistory={context.history.open}
          setOpenHistory={context.history.setOpen}
          isFromIframe={isFromIframe}
          isAllowHistorySend={isAllowHistorySend}
        />
      </div>
      {user.isLogin && (
        <History
          refresh={context.history.refresh}
          append={context.history.append}
          open={context.history.open}
          loading={context.history.loading}
          hasNextPage={context.history.hasNextPage}
          data={context.history.data}
          history={context.history.current}
          close={() => {
            context.history.setOpen(false);
          }}
          setHistory={context.setHistory}
          currentConversation={currentConversation}
        />

      )}

    </div>
  );
}

