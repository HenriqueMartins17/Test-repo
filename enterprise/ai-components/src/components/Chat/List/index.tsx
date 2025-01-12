import { Row, Col, Divider } from 'antd';
import React, { useState } from 'react';
import { MessageExploreCard, MessageTrainingTips, MessageSuggestion, MessageSystemTip, Banner } from './components';
import Message from './Message';
import styles from './style.module.less';
import { CUI } from '@/components/CUI';
import * as Shared from '@/shared';
import { IAISettings } from '@/shared/types';
import { ChatType, IChatListItem } from '@/shared/types/chat';

export interface ToolBarState {
  isIdle: boolean;
  isRegisterSendURLMethod: boolean;
  isRegisterSendFormMethod: boolean;
  sendLinkMessage: () => void;
  startAIFormMode: () => Promise<void>;
  sendFeedback: (messageIndex: number, message: string, like: Shared.AIFeedbackType) => Promise<void>;
  config: IAISettings;
  formName?: string;
}

interface IChatListProps {
  chatList: IChatListItem[];
  refreshScroll?: () => void;
  refreshExploreCard?: () => Promise<void>;
  sendMessage?: (content: string) => boolean;
  chatPadding?: number;
  ignoreGrid?: boolean;
  preview?: boolean;
  userInfo?: {
    avatar: string;
    memberId?: string;
    nickName: string;
    avatarColor?: number | null;
  } | null;
  feedbackList?: Record<number, Shared.IAIFeedback>;
  toolBarState?: ToolBarState;
}

const PADDING = {
  [ChatType.System]: 4
};

export function ChatList(props: IChatListProps) {
  const { chatPadding = 40, chatList } = props;
  const [toolActiveId, setToolActiveId] = useState<string | null>(null);

  const renderChatComponent = (chat: IChatListItem) => {
    switch (chat.type) {
      case ChatType.Bot:
      case ChatType.User:
        const record = chat.index !== undefined ? props.feedbackList?.[chat.index] : undefined;
        const toolBarState = props.toolBarState ? {
          toolActiveId: toolActiveId,
          setToolActiveId: setToolActiveId,
          feedback: record,
          ...props.toolBarState,
        } : undefined;
        return (
          <Message
            userInfo={props.userInfo}
            preview={props.preview}
            feedback={chat.feedback}
            index={chat.index}
            status={chat.status}
            role={chat.type}
            content={chat.content}
            toolBarState={toolBarState}
          />
        );
      case ChatType.Banner:
        return <Banner content={chat.content} logo={chat.logo} title={chat.title} />;
      case ChatType.Divider:
        return <Divider>{chat.content}</Divider>;
      case ChatType.System:
        return <MessageSystemTip content={chat.content} status={chat.status} onClick={chat.onClick} layout={chat.layout} />;
      case ChatType.ExploreCard:
        return <MessageExploreCard small={chat.small} content={chat.content} refresh={props.refreshExploreCard} onClick={props.sendMessage} />;
      case ChatType.Suggestion:
        return <MessageSuggestion content={chat.content} onClick={props.sendMessage} />;
      case ChatType.TrainingTips:
        return <MessageTrainingTips />;
      default:
        return <></>;
    }
  };

  const grid = props.ignoreGrid
    ? { span: 24 }
    : {
      xs: { span: 24 },
      lg: { span: 24 },
      xl: { span: 20, offset: 2 },
      xxl: { span: 18, offset: 3 },
    };
  return (
    <>
      {chatList &&
        chatList.map((chat, index) => {
          if (chat.type === ChatType.Form) {
            return <CUI userInfo={props.userInfo} {...chat} refreshScroll={props.refreshScroll} key={index} />;
          }
          const padding = PADDING[chat.type] || chatPadding / 2;
          return (
            <Row key={chat.id || index}>
              <Col {...grid} className={styles.chat} style={{ padding: `${padding}px 0` }}>
                {renderChatComponent(chat)}
              </Col>
            </Row>
          );
        })}
    </>
  );
}
