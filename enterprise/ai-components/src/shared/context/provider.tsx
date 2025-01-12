import dayjs from 'dayjs';
import React, { useEffect, useState, useReducer, useRef, useCallback, useMemo, useContext } from 'react';
import { Skeleton } from '@apitable/components';
import { Strings, t, Url } from '@apitable/core';
import styles from './style.module.less';
import { getAPIProvider } from '@/shared/api';
import { AIContext } from '@/shared/context/context';
import { TrainingStatus } from '@/shared/enum';
import { useSendChat, useHistory } from '@/shared/hook';
import { reducer, defaultState, IChatState, IChatAction } from '@/shared/store';
import { IAIInfoResponse, IAISettings, IConversationHistoryItem } from '@/shared/types';
import { ChatStatus, IChatListItem, ChatType } from '@/shared/types/chat';
import { convertMessageList2ChatList, getConversationIdByAIId, pollingTrainingStatus } from '@/shared/utils';
const DATE_FORMAT = 'MMM DD,h:mm A';

interface IProviderProps {
  children: React.ReactNode;
  childrenNoPermission: React.ReactNode;
  aiId: string;
  isLogin: boolean;
  triggerUsageAlert?: () => void;
  baseURL?: string;
}

/**
 * Signle AI provider
 * @param param0
 * @returns
 */
export function ChatPageProvider(props: IProviderProps) {
  const { children, childrenNoPermission, aiId, baseURL = Url.BASE_URL } = props;
  const api = getAPIProvider({ baseURL });
  const [loading, setLoading] = useState(true);
  const intervalRef = useRef<() => void>();
  const reloadRef = useRef(false);
  const [data, setData] = useState<IAIInfoResponse>({} as unknown as IAIInfoResponse);
  const [config, setConfig] = useState<IAISettings>({} as unknown as IAISettings);
  const [state, dispatch] = useReducer<(state: IChatState, action: IChatAction) => IChatState>(reducer, defaultState);
  const [isInit, setIsInit] = useState(false);

  const { send, abort } = useSendChat({ dispatch });

  const history = useHistory({
    getConversationHistory: (pageNum: number, pageSize: number) => {
      return api.getConversationHistory(data.id, pageNum, pageSize);
    }
  });
  const isWizardMode = useMemo(() => {
    if (!data.latestTrainingStatus) {
      return true;
    }
    if (!data.isTrained && data.latestTrainingStatus === TrainingStatus.FAILED) {
      return true;
    }
    return false;
  }, [data.latestTrainingStatus, data.isTrained]);

  const fetchAIDetail = useCallback(
    async (reload?: boolean, triggerInit?: boolean) => {
      if (!reload) setLoading(true);
      try {
        const ret = await api.getAiInfo(aiId as string);
        setData(ret.data.ai);
        setConfig(ret.data.ai.setting || {} as unknown as IAISettings);
        if (triggerInit) {
          reloadRef.current = false;
        } else {
          reloadRef.current = reload || false;
        }
      } finally {
        if (!reload) setLoading(false);
      }
    },
    [aiId],
  );

  const clearCurrentConversation = () => {
    abort();
    dispatch({ type: 'newConversation' });
    dispatch({ type: 'setLatestTrainingId', value: data.latestTrainingId });
    history.setCurrent(null);
  };

  const initMessageList = async (conversationId?: string) => {
    // console.log('init ai data.getHistoryMessage', conversationId);
    const count = await getHistoryMessage(conversationId);
    if (!count) {
      dispatch({
        type: 'insertSystemTip',
        value: data.currentConversationCreatedAt
          ? dayjs(data.currentConversationCreatedAt).format(DATE_FORMAT)
          : dayjs(new Date()).format(DATE_FORMAT),
        status: ChatStatus.Normal,
      });
      if (config.isEnabledPromptBox) {
        try {
          const res = await api.getConversationSuggestion(aiId);
          dispatch({ type: 'insertExploreCard', value: res.data.suggestions });
        } catch (error) {
          console.error(error);
        }
      }
      if (config.prologue) {
        dispatch({
          type: 'insertBotMessage',
          value: data.setting.prologue || t(Strings.ai_default_prologue),
        });
      }
    }
    dispatch({ type: 'initMessageComplete', value: true });
    setIsInit(true);
  };

  const getHistoryMessage = async (conversationId?: string) => {
    if (conversationId) {
      // console.log('getHistoryMessage', conversationId);
      const ret = await Promise.all([api.getChatHistoryList(aiId, { conversationId }), api.getAIFeedbackByConversationId(aiId, conversationId)]);
      const data1 = ret[0];
      const data2 = ret[1];
      if (data1.data && data1.data.data && data1.data.data.length) {
        const chatList: IChatListItem[] = [
          {
            type: ChatType.System,
            content: dayjs(data.currentConversationCreatedAt || data.created).format(DATE_FORMAT),
            status: ChatStatus.Normal,
          },
        ];
        chatList.push(...convertMessageList2ChatList(data1.data.data));
        dispatch({ type: 'insertMessageList', value: { data: chatList } });
      }
      dispatch({ type: 'insertFeedback', value: data2.data.fb });

      return data1.data.data?.length || 0;
    }
    return 0;
  };

  const insertTrainingFailedMessage = () => {
    if (props.isLogin) {
      if (data.isTrained) {
        dispatch({
          type: 'insertSystemTip',
          value: t(Strings.ai_training_failed_message_and_allow_send_message),
          status: ChatStatus.Error,
          onClick: async () => {
            clearCurrentConversation();
            await api.train(aiId);
            await fetchAIDetail(true, true);
          }
        });
      } else {
        dispatch({
          type: 'insertSystemTip',
          value: t(Strings.ai_training_failed_message),
          status: ChatStatus.Error,
          onClick: async () => {
            clearCurrentConversation();
            await api.train(aiId);
            await fetchAIDetail(false);
          }
        });
      }
    }
    dispatch({ type: 'initMessageComplete', value: false });
    setIsInit(true);
  };

  const init = async () => {
    // console.log('init ai');
    const latestTrainingId = data.latestTrainingId;
    dispatch({ type: 'setLatestTrainingId', value: latestTrainingId });
    const conversationId = props.isLogin ? data.currentConversationId : getConversationIdByAIId(data.id);
    dispatch({ type: 'setCurrentConversationId', value: conversationId });
    // console.log('init ai isWizardMode', isWizardMode);
    if (isWizardMode) {
      setIsInit(true);
    } else {
      switch (data.latestTrainingStatus) {
        case TrainingStatus.SUCCESS: {
          await initMessageList(conversationId);
          break;
        }
        case TrainingStatus.TRAINING:
        case TrainingStatus.NEW: {
          setIsInit(true);
          if (props.isLogin || !data.isTrained) {
            dispatch({ type: 'insertTrainingTips' });
            intervalRef.current = pollingTrainingStatus(
              async (status) => {
                fetchAIDetail(true); // Get the latest AI info
                if (status === TrainingStatus.SUCCESS) {
                  await initMessageList(conversationId);
                } else if (status === TrainingStatus.FAILED) {
                  insertTrainingFailedMessage();
                }
              },
              () => api.getAITrainingStatus(aiId),
            );
          }
          break;
        }
        case TrainingStatus.FAILED: {
          if (data.isTrained) {
            await initMessageList(conversationId);
          }
          insertTrainingFailedMessage();
          break;
        }
        default:
          console.log('init ai data.latestTrainingStatus is unknown', data.latestTrainingStatus);

      }
    }
  };

  useEffect(() => {
    setIsInit(false);
    fetchAIDetail();
    abort();
  }, [aiId, fetchAIDetail]);

  // Only when the data changes, it needs to be reinitialized. In other cases, it is not necessary.
  useEffect(() => {
    if (data.id) {
      if (!reloadRef.current) {
        init().catch(console.error);
      }
      reloadRef.current = false;
    }
    return () => {
      intervalRef.current && intervalRef.current();
    };
  }, [data]);

  const setHistory = async (item: IConversationHistoryItem) =>{
    abort();
    dispatch({ type: 'clearChatList' });
    history.setCurrent(item);
    // insert loading
    await getHistoryMessage(item.id);
  };

  if (loading) {
    return (
      <div className={styles.skeletonWrapper}>
        <Skeleton height="24px" />
        <Skeleton count={2} style={{ marginTop: '24px' }} height="80px" />
      </div>
    );
  }
  if (!data.id) {
    return <>{childrenNoPermission}</>;
  }

  function triggerUsageAlert() {
    if (props.triggerUsageAlert) {
      props.triggerUsageAlert();
    }
    // triggerUsageAlertUniversal(t(Strings.subscribe_credit_usage_over_limit));
  }

  return (
    <AIContext.Provider
      value={{
        isLogin: props.isLogin,
        baseURL,
        setHistory,
        history,
        api,
        send,
        abort,
        clearCurrentConversation,
        data,
        fetchAIDetail,
        isInit,
        initMessageList,
        state,
        dispatch,
        config,
        isWizardMode,
        triggerUsageAlert,
      }}
    >
      {children}
    </AIContext.Provider>
  );
}
