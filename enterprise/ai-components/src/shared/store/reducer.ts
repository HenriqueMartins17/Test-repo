import produce from 'immer';
import { t, Strings } from '@apitable/core';
import { ConversationStatus, IChatAction, IChatState } from './interface';
import { ChatStatus, ChatType, ISystemChatItem } from '@/shared/types/chat';

export const reducer = produce((draftState: IChatState, action: IChatAction) => {
  switch (action.type) {
    case 'createChat':
      draftState.conversationStatus = ConversationStatus.Loading;
      draftState.chatList = draftState.chatList.filter((item) => item.type !== ChatType.Suggestion);
      draftState.chatList.push(
        { type: ChatType.User, content: action.value.content },
        // add new Bot object without data , because request is not sent yet
        { type: ChatType.Bot, status: ChatStatus.WaitResponse, id: action.value.id, content: '' },
      );
      return draftState;

    case 'updateChat': {
      const find = draftState.chatList.findIndex((item) => item.type === ChatType.Bot && item.id === action.value.id);
      if (find !== -1) {
        const item = draftState.chatList[find];
        const content = item.content + action.value.content;
        const chat = { ...item, content, status: ChatStatus.Transmission };
        // @ts-ignore
        draftState.chatList[find] = chat;
      } else {
        draftState.chatList.push({ type: ChatType.Bot, content: action.value.content, status: ChatStatus.Transmission, id: action.value.id });
      }
      const findBot = draftState.chatList.findIndex((item) => item.type === ChatType.Bot && item.status === ChatStatus.WaitResponse && !item.content);
      if (findBot !== -1) {
        draftState.chatList.splice(findBot, 1);
      }
      return draftState;
    }
    case 'updateChatStatus': {
      const find = draftState.chatList.findIndex((item) => item.id === action.value.id);
      if (find !== -1) {
        draftState.chatList = draftState.chatList.map((item) => {
          const chat = { ...item, status: action.value.status };
          return chat;
        });
      }
      return draftState;
    }
    case 'completeChat': {
      draftState.conversationStatus = ConversationStatus.Idle;
      const find = draftState.chatList.findIndex((item) => item.type === ChatType.Bot && item.id === action.value.id);
      if (find !== -1) {
        const maxIndex = draftState.chatList.reduce((pre, cur) => {
          if (cur.type === ChatType.Bot && cur.index !== undefined) {
            return Math.max(pre, cur.index);
          }
          return pre;
        }, -1);
        draftState.chatList = draftState.chatList.map((item) => {
          if ((item.type === ChatType.Bot || item.type === ChatType.User) && item.id === action.value.id) {
            if (item.type === ChatType.Bot) {
              item.index = maxIndex + 1;
            }
            const chat = { ...item, status: ChatStatus.Complete };
            return chat;
          }
          return item;
        });
      }
      return draftState;
    }

    case 'errorChat': {
      draftState.conversationStatus = ConversationStatus.Idle;
      const find = draftState.chatList.findIndex((item) => item.type === ChatType.Bot && item.id === action.value.id);
      if (find !== -1) {
        draftState.chatList = draftState.chatList.map((item) => {
          if ((item.type === ChatType.Bot || item.type === ChatType.User) && item.id === action.value.id) {
            const content = `${item.content}\n${action.value.error}`.trim();
            const chat = { ...item, content, status: ChatStatus.Error };
            return chat;
          }
          return item;
        });
      } else {
        draftState.chatList.push({ type: ChatType.Bot, content: action.value.error, status: ChatStatus.Error, id: action.value.id });
      }
      const findBot = draftState.chatList.findIndex((item) => item.type === ChatType.Bot && item.status === ChatStatus.WaitResponse && !item.content);
      if (findBot !== -1) {
        draftState.chatList.splice(findBot, 1);
      }
      return draftState;
    }

    case 'abortChat': {
      draftState.conversationStatus = ConversationStatus.Idle;
      const message = draftState.chatList[draftState.chatList.length - 1];
      if (message && message.type !== ChatType.User) {
        draftState.chatList[draftState.chatList.length - 1] = { ...message, status: ChatStatus.Error };
      }
      return draftState;
    }

    case 'insertMessageList': {
      if (action.value.append) {
        draftState.chatList = draftState.chatList.concat(action.value.data);
      } else {
        draftState.chatList = action.value.data;
      }
      return draftState;
    }

    case 'initMessageComplete': {
      draftState.conversationStatus = ConversationStatus.Idle;
      if (action.value) {
        // TrainingTips replaced by system message
        draftState.chatList = draftState.chatList.map((item) => {
          if (item.type === ChatType.TrainingTips) {
            return { type: ChatType.System, content: t(Strings.ai_train_complete_message), status: ChatStatus.Complete };
          }
          return item;
        });
      } else {
        draftState.chatList = draftState.chatList.filter((item) => item.type !== ChatType.TrainingTips);
      }
      return draftState;
    }

    case 'insertSuggestion': {
      if (draftState.conversationStatus === ConversationStatus.Idle) {
        draftState.chatList.push({
          type: ChatType.Suggestion,
          content: action.value,
        });
        return draftState;
      }
      return draftState;
    }

    case 'insertUserMessage': {
      if (typeof action.value === 'string') {
        draftState.chatList.push({ type: ChatType.User, content: action.value });
      } else {
        draftState.chatList.push({ type: ChatType.User, content: action.value.content, status: action.value.status });
      }
      return draftState;
    }

    case 'insertBotMessage': {
      if (typeof action.value === 'string') {
        draftState.chatList.push({ type: ChatType.Bot, content: action.value });
      } else {
        draftState.chatList.push({ type: ChatType.Bot, content: action.value.content, status: action.value.status });
      }
      return draftState;
    }

    case 'insertSystemTip': {
      const find = draftState.chatList.findIndex((item) => item.type === ChatType.System && item.id === action.id);
      if (action.id && find !== -1) {
        draftState.chatList = draftState.chatList.map((item) => {
          if (item.type === ChatType.System && item.id === action.id) {
            const chat = { ...item, content: action.value, status: action.status, onClick: action.onClick, layout: action.layout };
            return chat;
          }
          return item;
        }
        );
      } else {
        draftState.chatList.push({ type: ChatType.System, content: action.value, status: action.status, onClick: action.onClick, layout: action.layout });
      }
      const findBot = draftState.chatList.findIndex((item) => item.type === ChatType.Bot && item.status === ChatStatus.WaitResponse && !item.content);
      if (findBot !== -1) {
        const bot = draftState.chatList.splice(findBot, 1);
        draftState.chatList.push(bot[0]);
      }

      return draftState;
    }

    case 'insertExploreCard': {
      draftState.chatList.push({ type: ChatType.ExploreCard, content: action.value, small: action.small });
      return draftState;
    }

    case 'insertTrainingTips': {
      draftState.chatList.push({ type: ChatType.TrainingTips });
      return draftState;
    }

    case 'insertFeedback': {
      if (Array.isArray(action.value)) {
        action.value.forEach((item) => {
          draftState.feedback[item.messageIndex] = item;
        });
      } else {
        draftState.feedback[action.value.messageIndex] = action.value;
      }
      return draftState;
    }
    case 'updateExploreCard': {
      const exploreCard = draftState.chatList.find((item) => item.type === ChatType.ExploreCard);

      if (!exploreCard) return draftState;

      exploreCard.content = action.value;
      return draftState;
    }

    case 'setCurrentConversationId': {
      draftState.currentConversationId = action.value;
      return draftState;
    }

    case 'setLatestTrainingId': {
      draftState.trainingId = action.value;
      return draftState;
    }

    case 'clearChatList': {
      draftState.chatList = [];
      draftState.feedback = {};
      return draftState;
    }

    case 'newConversation': {
      draftState.chatList = [];
      draftState.feedback = {};
      draftState.currentConversationId = '';
      return draftState;
    }

    case 'startCUIAction': {
      if (draftState.conversationStatus !== ConversationStatus.Form) {
        draftState.conversationStatus = ConversationStatus.Form;
        draftState.currentActionsId = action.value.instance.id;
        draftState.chatList.push({
          instance: action.value.instance,
          type: ChatType.Form,
          form: action.value.form,
          result: action.value.result,
          status: ChatStatus.Normal,
        });
      }
      return draftState;
    }

    case 'updateCUIAction': {
      if (draftState.conversationStatus === ConversationStatus.Form) {
        const item = draftState.chatList.find((item) => item.type === ChatType.Form && item.instance.id === action.value.id);
        if (item && item.type === ChatType.Form) {
          item.form = action.value.form;
          item.result = action.value.result;
          item.status = ChatStatus.Normal;
          draftState.chatList[draftState.chatList.length - 1] = { ...item };
        }
      }
      return draftState;
    }

    case 'finishCUIAction': {
      if (draftState.conversationStatus === ConversationStatus.Form) {
        const item = draftState.chatList.find((item) => item.type === ChatType.Form && item.instance.id === action.value.id);
        if (item && item.type === ChatType.Form) {
          item.status = ChatStatus.Complete;
          draftState.chatList[draftState.chatList.length - 1] = { ...item };
          draftState.conversationStatus = ConversationStatus.Idle;
        }
        if (draftState.currentActionsId === action.value.id) {
          draftState.currentActionsId = '';
        }
      }
      return draftState;
    }
  }
  return draftState;
});

export const defaultState: IChatState = {
  chatList: [],
  feedback: {},
  trainingId: '',
  currentConversationId: '',
  currentActionsId: '',
  userInputValue: '',
  conversationStatus: ConversationStatus.None,
};
