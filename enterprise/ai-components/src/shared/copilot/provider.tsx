import React, { useEffect, useState, useReducer } from 'react';
import { Strings, t, Url } from '@apitable/core';
import { getAPIProvider } from '@/shared/api';
import { BillingUsageOverLimitError } from '@/shared/error';
import { useSendChat } from '@/shared/hook';
import { reducer, defaultState, IChatState, IChatAction, ConversationStatus } from '@/shared/store';
import { ICopilotInfo } from '@/shared/types';
import { ChatType, IChatListItem } from '@/shared/types/chat';
import { convertMessageList2ChatList } from '@/shared/utils';

export interface CopilotContextState {
  stopConversation: () => void;
  sendMessage: (content: string) => boolean;
  newConversation: () => void;
  setAgent: (type: string) => void;
  state: IChatState;
  data: ICopilotInfo;
  loading: boolean;
}

export const CopilotContext = React.createContext<CopilotContextState>({} as CopilotContextState);

interface IProviderProps {
  children: React.ReactNode;
  spaceId: string;
  context: {
    datasheetId: string;
    viewId: string;
  };
  triggerUsageAlert?: () => void;
  banner: {
    logo: React.ReactNode;
    title: string;
    content: string | ((type: string) => string);
  }
  privacy: React.ReactNode;
}

/**
 * Signle AI provider
 * @param param0
 * @returns
 */
export function CopilotProvider(props: IProviderProps) {
  const { children, spaceId } = props;
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<ICopilotInfo>({} as unknown as ICopilotInfo);
  const [state, dispatch] = useReducer<(state: IChatState, action: IChatAction) => IChatState>(reducer, defaultState);
  const { send, abort } = useSendChat({ dispatch });
  const api = getAPIProvider({ baseURL: Url.BASE_URL });

  const fetchHistoryMessages = async (data: ICopilotInfo) => {
    dispatch({ type: 'clearChatList' });
    const ret = await api.getCopilotHistoryConversations(data.latestConversation.id);
    if (ret.data && ret.data.data && ret.data.data.length) {
      const chatList: IChatListItem[] = [];
      chatList.push(...convertMessageList2ChatList(ret.data.data));
      dispatch({ type: 'insertMessageList', value: { data: chatList } });
    } else {
      const bannderDesc = typeof props.banner.content === 'function' ? props.banner.content(data.latestConversation.type) : props.banner.content;
      const suggestion = await api.getCopilotSuggestion(data.latestConversation.type);
      dispatch({ type: 'insertMessageList', value: {
        data: [{ type: ChatType.Banner, ...props.banner, content: bannderDesc }]
      } });
      if (suggestion.data.length) {
        dispatch({ type: 'insertExploreCard', value: suggestion.data, small: true });
      }
      if (data.latestConversation.prologue) {
        dispatch({ type: 'insertMessageList', value: {
          data: [{ type: ChatType.Bot, content: data.latestConversation.prologue }],
          append: true,
        } });
      }
      if (data.firstTimeUsed) {
        dispatch({ type: 'insertMessageList', value: {
          data: [{ type: ChatType.System, content: props.privacy }],
          append: true,
        } });
      }
    }
  };

  const setAgent = async (type: string) => {
    try {
      abort();
      setLoading(true);
      const ret = await api.createCopilotConversation(spaceId, type);
      setData((state) => {
        return {
          ...state,
          latestConversation: ret.data,
        };
      });
      await fetchHistoryMessages({ latestConversation: ret.data, firstTimeUsed: false });
      dispatch({ type: 'setCurrentConversationId', value: ret.data.id });
    } finally {
      setLoading(false);
    }
  };

  const fetch = async () => {
    try {
      const ret = await api.getCopilotInfo(spaceId);
      setData(ret.data);
      await fetchHistoryMessages(ret.data);
      dispatch({ type: 'setCurrentConversationId', value: ret.data.latestConversation.id });
      dispatch({ type: 'initMessageComplete', value: true });
      setLoading(false);
    } finally {
      // setLoading(false);
    }
  };

  useEffect(() => {
    fetch();
  }, [props.spaceId]);

  /**
   * Send message
   * @param content
   */
  const sendMessage = (content: string) => {
    if (state.conversationStatus === ConversationStatus.Idle) {
      const conversationId = state.currentConversationId;
      const endPoint = Url.BASE_URL + '/ai/copilot/chat/completions';
      const [promise] = send({
        endPoint,
        content,
        conversationId,
        context: props.context,
      });
      promise.catch((err) => {
        if (err instanceof BillingUsageOverLimitError) {
          // triggerUsageAlert();
        } else {
          console.error(err);
        }
      });
      return true;
    }
    return false;
  };

  const newConversation = async () => {
    abort();
    dispatch({ type: 'newConversation' });
    await setAgent(data.latestConversation.type);
  };

  const stopConversation = () => {
    abort();
    // api.cancelCopilotConversation(state.currentConversationId);
  };

  return (
    <CopilotContext.Provider
      value={{
        data,
        state,
        loading,
        setAgent,
        stopConversation,
        newConversation,
        sendMessage,
      }}
    >
      {children}
    </CopilotContext.Provider>
  );
}
