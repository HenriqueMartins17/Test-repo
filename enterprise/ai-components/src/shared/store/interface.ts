import * as Shared from '@/shared';
import { BaseAction } from '@/shared/cui/action';
import { ICUIForm } from '@/shared/cui/types';
import { ChatStatus, IChatListItem } from '@/shared/types/chat';

export interface IChatState {
  chatList: IChatListItem[];
  feedback: Record<number, Shared.IAIFeedback>;
  userInputValue: string;
  trainingId: string;
  conversationStatus: ConversationStatus;
  currentConversationId: string;
  currentActionsId: string;
}

export enum ConversationStatus {
  None,
  Idle,
  Loading,
  Form,
  Chain,
}

interface IUpdateChat {
  type: 'updateChat';
  value: {
    content: string;
    id: string;
  };
}

interface ICompleteChat {
  type: 'completeChat';
  value: {
    id: string;
  };
}

interface IErrorChat {
  type: 'errorChat';
  value: {
    error: string;
    id: string;
  };
}

interface IAbortChat {
  type: 'abortChat';
}

interface ICreateChat {
  type: 'createChat';
  value: {
    content: string;
    id: string;
  };
}

interface IInsertSystemTip {
  type: 'insertSystemTip';
  value: string;
  status: ChatStatus;
  // default center
  layout?: 'center' | 'left';
  onClick?: () => Promise<void>;
  id?: string;
}

interface IInsertBotMessage {
  type: 'insertBotMessage';
  value:
    | {
        content: string;
        status?: ChatStatus;
      }
    | string;
}

interface IInsertUserMessage {
  type: 'insertUserMessage';
  value:
    | {
        content: string;
        status?: ChatStatus;
      }
    | string;
}

interface IInsertExploreCard {
  type: 'insertExploreCard';
  value: string[];
  small?: boolean;
}

interface IUpdateExploreCard {
  type: 'updateExploreCard';
  value: string[];
}

interface IInsertMessageList {
  type: 'insertMessageList';
  value: {
    data: IChatListItem[];
    append?: boolean;
  };
}

interface IInsertFeedback {
  type: 'insertFeedback';
  value: Shared.IAIFeedback[] | Shared.IAIFeedback;
}

interface IInitMessageComplete {
  type: 'initMessageComplete';
  value: boolean;
}

interface IInsertSuggestion {
  type: 'insertSuggestion';
  value: string[];
}

interface IInsertTrainingTips {
  type: 'insertTrainingTips';
}

interface ISetCurrentConversationId {
  type: 'setCurrentConversationId';
  value: string;
}

interface ISetLatestTrainingId {
  type: 'setLatestTrainingId';
  value: string;
}

interface INewConversation {
  type: 'newConversation';
}

interface IStartCUIAction {
  type: 'startCUIAction';
  value: {
    form: ICUIForm;
    result: Record<string, any>;
    instance: BaseAction;
  };
}
interface IUpdateCUIAction {
  type: 'updateCUIAction';
  value: {
    id: string;
    form: ICUIForm;
    result: Record<string, any>;
  };
}

interface IFinishCUIAction {
  type: 'finishCUIAction';
  value: {
    id: string;
  };
}

interface IClearChatList {
  type: 'clearChatList';
}

interface IUpdateChatStatus {
  type: 'updateChatStatus';
  value: {
    id: string;
    status: ChatStatus;
  };
}

export type IChatAction =
  | IUpdateChat
  | ICreateChat
  | IInsertFeedback
  | IInsertSystemTip
  | IInsertBotMessage
  | IInsertUserMessage
  | IInsertExploreCard
  | IInsertTrainingTips
  | ICompleteChat
  | IInsertMessageList
  | IUpdateExploreCard
  | IErrorChat
  | IAbortChat
  | IInitMessageComplete
  | IInsertSuggestion
  | INewConversation
  | ISetCurrentConversationId
  | ISetLatestTrainingId
  | IFinishCUIAction
  | IUpdateCUIAction
  | IStartCUIAction
  | IUpdateChatStatus
  | IClearChatList;
