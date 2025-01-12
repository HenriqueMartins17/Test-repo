import classNames from 'classnames';
import React, { useEffect, useRef } from 'react';
import { IUserInfo } from '@apitable/core';
import { ChatControl } from './Control';
import { ChatList, ToolBarState } from './List';
import styles from './index.module.less';
import { ConversationStatus } from '@/shared/store/interface';
import { IChatListItem, IAIFeedback } from '@/shared/types';

interface IProps {
  userInfo?: IUserInfo | null;
  // 融合模式
  isFusionMode?: boolean;
  ignoreGrid?: boolean;
  ignoreBackground?: boolean;
  chatList: IChatListItem[];
  conversationStatus: ConversationStatus;
  sendMessage: (content: string) => boolean;
  stopConversation: () => void;
  toolBarState?: ToolBarState;
  refreshExploreCard?: () => Promise<void>;
  feedbackList?: Record<number, IAIFeedback>;
}

export function Chat(props: IProps) {
  const ref = useRef<HTMLDivElement>(null);
  const {
    toolBarState, chatList, conversationStatus, feedbackList,
    stopConversation, sendMessage, refreshExploreCard
  } = props;
  // const { context, isIdle, stopConversation } = props.hook;
  // refresh scroll

  const refreshScroll = () => {
    setTimeout(() => {
      if (ref.current) {
        ref.current.scrollBy(0, ref.current.scrollHeight);
      }
    }, 50);
  };

  useEffect(refreshScroll, [chatList.length, ref.current?.children]);

  useEffect(() => {
    if (ref.current && conversationStatus === ConversationStatus.Loading) {
      const { scrollHeight, scrollTop, clientHeight } = ref.current;
      if (scrollHeight - scrollTop - clientHeight < 150) {
        refreshScroll();
      }
    }
  }, [chatList, conversationStatus]);

  const renderChatList = () => {
    return (
      <ChatList
        ignoreGrid={props.ignoreGrid}
        userInfo={props.userInfo}
        sendMessage={sendMessage}
        refreshExploreCard={refreshExploreCard}
        chatList={chatList}
        feedbackList={feedbackList}
        refreshScroll={refreshScroll}
        toolBarState={toolBarState}
      />
    );
  };

  return (
    <div className={styles.chat}>
      <div
        className={classNames([styles.chatListContainer, { [styles.fusion]: props.isFusionMode }])}
        ref={ref}

      >
        {renderChatList()}
      </div>
      {!props.ignoreBackground && (
        <>
          <div className={styles.blueCubeColor} />
          <div className={styles.redCubeColor} />
        </>
      )}

      <ChatControl status={conversationStatus} stopConversation={stopConversation} />
    </div>
  );
}
