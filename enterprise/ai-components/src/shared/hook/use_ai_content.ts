import { useContext, useMemo } from 'react';
import urlcat from 'urlcat';
import { AIContext } from '@/shared/context/context';
import { CUIActions, CUIActionType } from '@/shared/cui/action';
import { AIFeedbackType, AIType } from '@/shared/enum';
import { BillingUsageOverLimitError } from '@/shared/error';
import { ConversationStatus } from '@/shared/store/interface';
import { IAIContextState } from '@/shared/types';
import { ChatStatus } from '@/shared/types/chat';
import { saveConversationIdByAIId } from '@/shared/utils/storage';

const PATH_SEND_CHAT_MESSAGE = 'ai/:aiId/messages';

export interface IAIContextHook {
  isTrainCompleted: boolean;
  disabledSubmit: boolean;
  context: IAIContextState;
  isFormMode: boolean;
  isIdle: boolean;
  isRegisterSendURLMethod: boolean;
  isRegisterSendFormMethod: boolean;
  refreshExploreCard: () => Promise<void>;
  getAIFormId: () => string | null | void;
  startAIFormMode: () => Promise<void>;
  sendLinkMessage: () => void;
  sendBotMessage: (content: string | { content: string; status: ChatStatus }) => void;
  newConversation: () => void;
  stopConversation: () => void;
  sendMessage: (content: string) => boolean;
  sendFeedback: (messageIndex: number, message: string, like: AIFeedbackType) => Promise<void>;
}

export const useAIContext = (): IAIContextHook => {
  const context = useContext(AIContext);
  const { dispatch, data, state } = context;

  const isTrainCompleted = useMemo(() => {
    return !!data.isTrained;
  }, [data.isTrained]);

  const isFormMode = state.conversationStatus === ConversationStatus.Form;
  const isIdle = state.conversationStatus === ConversationStatus.Idle;

  const disabledSubmit = useMemo(() => {
    if (isFormMode) {
      return true;
    }
    if (!isIdle) {
      return true;
    }
    if (!isTrainCompleted) {
      return true;
    }
    return false;
  }, [state.conversationStatus, isTrainCompleted, isFormMode, isIdle]);

  const sendBotMessage = (content: string | { content: string; status: ChatStatus }) => {
    dispatch({ type: 'insertBotMessage', value: content });
  };

  /**
   * New chat
   */
  const newConversation = () => {
    context.clearCurrentConversation();
    context.initMessageList();
  };

  /**
   * Stop Send Conversation
   */
  const stopConversation = () => {
    context.abort();
  };

  const sendLinkMessage = () => {
    if (isTrainCompleted && !isFormMode && context.config.type === AIType.Qa) {
      const { openUrl } = context.config;
      if (openUrl) sendBotMessage(openUrl);
    }
  };

  /**
   * Send message
   * @param content
   */
  const sendMessage = (content: string) => {
    if (!disabledSubmit) {
      const conversationId = context.history.current ? context.history.current.id : context.state.currentConversationId;
      const baseURL = context.baseURL;
      const SEND_CHAT_MESSAGE = baseURL.endsWith('/') ? baseURL + PATH_SEND_CHAT_MESSAGE : `${baseURL}/${PATH_SEND_CHAT_MESSAGE}`;
      const endPoint = urlcat(SEND_CHAT_MESSAGE, { aiId: context.data.id });

      const [promise] = context.send({
        endPoint,
        content,
        conversationId,
        onOpen: (response) => {
          if (response.ok) {
            const conversationId = response.headers.get('X-Conversation-Id');
            if (conversationId && !context.isLogin) {
              saveConversationIdByAIId(context.data.id, conversationId);
            }
          }
        },
        onActions: (actions) => {
          actions.forEach((action) => {
            if (action.name === 'url') {
              sendLinkMessage();
            } else if (action.name === 'form') {
              startAIFormMode();
            }
          });
        }
      });
      promise.then(() => {
        if (context.config.isEnabledPromptTips) {
          context.api.getConversationSuggestion(data.id, content, 3).then((res) => {
            if (res.data.suggestions && res.data.suggestions.length) {
              const value = res.data.suggestions;
              dispatch({ type: 'insertSuggestion', value });
            }
          });
        }
        if (!conversationId) {
          context.history.refresh();
        }
      })
        .catch((err) => {
          if (err instanceof BillingUsageOverLimitError) {
            context.triggerUsageAlert();
          } else {
            console.error(err);
          }
        });
      return true;
    }
    return false;
  };

  const getAIFormId = () => {
    if (context.config.type === AIType.Qa && context.config.isEnableCollectInformation) {
      return context.config.formId;
    }
    return null;
  };

  const startAIFormMode = async () => {
    try {
      const Action = CUIActions[CUIActionType.MagicForm];
      if (Action) {
        // @ts-ignore
        const action = await Action.create(context);
        action.start();
      }
    } catch (error) {
      console.error(error);
    }
  };

  const refreshExploreCard = async () => {
    const res = await context.api.getConversationSuggestion(context.data.id);
    dispatch({
      type: 'updateExploreCard',
      value: res.data.suggestions,
    });
  };

  const sendFeedback = async (messageIndex: number, message: string, like: AIFeedbackType) => {
    if (messageIndex !== undefined) {
      const trainingId = context.history.current ? context.history.current.trainingId : context.state.trainingId;
      const conversationId = context.history.current ? context.history.current.id : context.state.currentConversationId;
      const ret = await context.api.sendAIFeedback(context.data.id, {
        trainingId,
        conversationId,
        comment: message,
        messageIndex,
        like,
      });
      context.dispatch({ type: 'insertFeedback', value: ret.data });
    }
  };

  const isRegisterSendURLMethod = useMemo(() => {
    if (context.config.type === AIType.Qa) {
      if (context.config.isEnableOpenUrl) {
        return true;
      }
    }
    return false;
    /** @ts-ignore */
  }, [context.config.type, context.config.isEnableOpenUrl]);

  const isRegisterSendFormMethod = useMemo(() => {
    if (context.config.type === AIType.Qa) {
      if (context.config.isEnableCollectInformation) {
        return true;
      }
    }
    return false;
    /** @ts-ignore */
  }, [context.config.type, context.config.isEnableCollectInformation]);

  // Here you can encapsulate some commonly used methods and expose them to components for use.
  return {
    isTrainCompleted,
    disabledSubmit,
    context,
    isFormMode,
    isIdle,
    isRegisterSendURLMethod,
    isRegisterSendFormMethod,
    refreshExploreCard,
    getAIFormId,
    startAIFormMode,
    sendLinkMessage,
    sendBotMessage,
    newConversation,
    stopConversation,
    sendMessage,
    sendFeedback,
  };
};
