import urlcat from 'urlcat';
import { http, Url, IServerFormPack, IServerResponse } from '@apitable/core';
import { AIFeedbackState, AIFeedbackType } from '@/shared/enum';
import {
  IAIFeedback,
  IAIFeedbackDetail,
  IAIInfoResponse,
  IDataSourceUpdate,
  IDatasourceResponse,
  IChatMessageListResponse,
  IGetAIInfoResponse,
  IGetAITrainingStatusResponse,
  IAIShareInfoResponse,
  ITraningHistoryResponse,
  IPageResponse,
  IConversationHistoryItem,
  ICopilotInfo,
  ICopilotChatMessageResponse,
} from '@/shared/types';

type Response<T> = Promise<IServerResponse<T>>;

interface IProps {
  baseURL?: string;
}

export const getAPIProvider = (props: IProps) => {
  const { baseURL = Url.BASE_URL } = props;

  const PATH_GET_AI_INFO = 'ai/:aiId';
  const PATH_GET_AI_SETTING = 'ai/:aiId/setting';
  const PATH_GET_MESSAGE_LIST = 'ai/:aiId/messages';
  const PATH_GET_CONVERSATION_SUGGESTION = 'ai/:aiId/suggestions';
  const PATH_GET_AI_TRAINING_STATUS = 'ai/:aiId/training/status';
  const PATH_GET_AI_DATASOURCE = 'ai/:aiId/datasource';
  const PATH_TRAIN = 'ai/:aiId/train';
  const PATH_TRAIN_HISTORY = 'ai/:aiId/trainings';
  const PATH_TRAIN_PREDICT = 'ai/:aiId/train/predict';
  const PATH_CREATE_FEEDBACK = 'ai/:aiId/feedback';
  const PATH_GET_AI_FEEDBACK = 'ai/:aiId/feedback';
  const PATH_GET_AI_FEEDBACK_BY_CONVERSATIONS = 'ai/:aiId/conversations/:conversationId/feedback';
  const PATH_UPDATE_AI_FEEDBACK = 'ai/:aiId/feedback/:feedbackId';
  const PATH_GET_AI_FORM_META = 'ai/:aiId/form';
  const PATH_ADD_AI_FORM_RECORD = 'ai/:aiId/form';

  const PATH_AIRAGENT_SHARE = 'ai/sharing/{shareld)';
  const PATH_AIRAGENT_SHARE_OPEN = 'ai/:aiId/share/publish';
  const PATH_AIRAGENT_SHARE_CLOSE = 'ai/:aiId/share/close';
  const PATH_AIRAGENT_SHARE_INFO = 'ai/:aiId/share/info';

  const PATH_GET_AI_CONVERSATIONS = 'ai/:aiId/conversations';
  const PATH_GET_INSIGHT_CONVERSATIONS = 'insight/conversations';

  const PATH_COPILOT = 'ai/copilot';
  const PATH_COPILOT_HISTORY_CONVERSATIONS = 'ai/copilot/conversations/:conversationId/messages';
  const PATH_COPILOT_CANCEL_CONVERSATION = 'ai/copilot/conversations/:conversationId/cancel';
  // swicth agent
  const PATH_COPILOT_CREATE_CONVERSATION = 'ai/copilot/conversations';
  const PATH_COPILOT_SUGGESTIONS = 'ai/copilot/suggestions';

  const getAiInfo = (aiId: string): Response<{ ai: IAIInfoResponse }> => {
    return http.get(urlcat(PATH_GET_AI_INFO, { aiId }), { baseURL });
  };

  const getChatHistoryList = (
    aiId: string,
    search?: {
      cursor?: string;
      limit?: number;
      conversationId?: string;
      trainingId?: string;
    },
  ): Response<IChatMessageListResponse> => {
    return http.get(urlcat(PATH_GET_MESSAGE_LIST, { aiId }), {
      params: search,
      baseURL,
    });
  };

  const updateAIInfo = (info: Partial<IAIInfoResponse>, aiId: string): Response<{ ai: IAIInfoResponse }> => {
    return http.put(urlcat(PATH_GET_AI_INFO, { aiId }), info, {
      baseURL,
    });
  };

  const getTrainingDataSource = (aiId: string): Response<IDatasourceResponse> => {
    return http.get(urlcat(PATH_GET_AI_DATASOURCE, { aiId }), {
      baseURL,
    });
  };

  const addTrainingDataSource = (aiId: string, data: IDataSourceUpdate): Response<IDatasourceResponse> => {
    return http.post(urlcat(PATH_GET_AI_DATASOURCE, { aiId }), data, {
      baseURL,
    });
  };

  const deleteTrainingDataSource = (aiId: string, datasourceId: string): Response<IDatasourceResponse> => {
    return http.delete(urlcat(PATH_GET_AI_DATASOURCE, { aiId, datasourceId }), {
      baseURL,
    });
  };

  const getConversationSuggestion = (aiId: string, question?: string, n?: number): Response<IGetAIInfoResponse> => {
    return http.post(
      urlcat(PATH_GET_CONVERSATION_SUGGESTION, { aiId }),
      {
        question,
        n,
      },
      { baseURL },
    );
  };

  const getAITrainingStatus = (aiId: string): Response<IGetAITrainingStatusResponse> => {
    return http.get(urlcat(PATH_GET_AI_TRAINING_STATUS, { aiId }), {
      baseURL,
    });
  };

  const train = (aiId: string): Response<IGetAITrainingStatusResponse> => {
    return http.post(urlcat(PATH_TRAIN, { aiId }), null, {
      baseURL,
    });
  };

  const sendAIFeedback = (
    aiId: string,
    data: {
      trainingId: string;
      conversationId: string;
      messageIndex: number;
      like: AIFeedbackType;
      comment: string;
    },
  ): Response<IAIFeedback> => {
    return http.post(urlcat(PATH_CREATE_FEEDBACK, { aiId }), data, { baseURL });
  };

  const getAIFeedbackByConversationId = (
    aiId: string,
    conversationId: string,
    trainingId?: string,
  ): Response<{
    fb: IAIFeedback[];
  }> => {
    return http.get(urlcat(PATH_GET_AI_FEEDBACK_BY_CONVERSATIONS, { aiId, conversationId }), {
      params: {
        trainingId,
      },
      baseURL,
    });
  };

  const getAIFeedback = (
    aiId: string,
    params: {
      pageNum: number;
      pageSize: number;
      state?: AIFeedbackState;
      search?: string;
    },
  ): Response<{
    total: number;
    fb: IAIFeedbackDetail[];
    pageSize: number;
    pageNum: number;
  }> => {
    return http.get(urlcat(PATH_GET_AI_FEEDBACK, { aiId }), { params, baseURL });
  };

  const updateAIFeedback = (
    aiId: string,
    feedbackId: string,
    data: {
      state: AIFeedbackState;
    },
  ): Response<IAIFeedback> => {
    return http.put(urlcat(PATH_UPDATE_AI_FEEDBACK, { aiId, feedbackId }), data, { baseURL });
  };

  const trainPredict = (
    aiId: string,
    data: {
      dataSources: { nodeId: string }[];
    },
  ): Response<{
    characters: number;
    words: number;
    creditCost: number;
  }> => {
    return http.post(urlcat(PATH_TRAIN_PREDICT, { aiId }), data, { baseURL });
  };

  const fetchFormPackForAI = (aiId: string): Response<IServerFormPack> => {
    return http.get(urlcat(PATH_GET_AI_FORM_META, { aiId }), {
      baseURL: Url.NEST_BASE_URL,
    });
  };

  const addFormRecordForAI = (aiId: string, recordData: Record<string, any>): Response<any> => {
    return http.post(urlcat(PATH_ADD_AI_FORM_RECORD, { aiId }), recordData, { baseURL: Url.NEST_BASE_URL });
  };
  const getAISetting = (aiId: string, type?: string): Response<any> => {
    return http.get(urlcat(PATH_GET_AI_SETTING, { aiId }), {
      baseURL,
      params: { type },
    });
  };

  const shareOpen = (aiId: string): Response<any> => {
    return http.post(urlcat(PATH_AIRAGENT_SHARE_OPEN, { aiId }), null, { baseURL });
  };
  const shareClose = (aiId: string): Response<any> => {
    return http.patch(urlcat(PATH_AIRAGENT_SHARE_CLOSE, { aiId }), null, { baseURL });
  };

  const shareInfo = (aiId: string): Response<IAIShareInfoResponse> => {
    return http.get(urlcat(PATH_AIRAGENT_SHARE_INFO, { aiId }), { baseURL });
  };

  const getTrainingHistory = (aiId: string): Response<ITraningHistoryResponse> => {
    return http.get(urlcat(PATH_TRAIN_HISTORY, { aiId }), { baseURL });
  };

  const getConversationHistory = (aiId: string, pageNum: number, pageSize: number): Response<IPageResponse<IConversationHistoryItem>> => {
    return http.get(urlcat(PATH_GET_AI_CONVERSATIONS, { aiId }), {
      params: {
        pageNum,
        pageSize,
      },
      baseURL,
    });
  };

  const getInsightConversations = (aiId: string, pageNum: number, pageSize: number): Response<IPageResponse<IConversationHistoryItem>> => {
    return http.get(PATH_GET_INSIGHT_CONVERSATIONS, {
      params: {
        aiId,
        pageNum,
        pageSize,
      },
      baseURL,
    });
  };

  const getCopilotInfo = (spaceId: string): Response<ICopilotInfo> => {
    return http.get(PATH_COPILOT, {
      params: { spaceId },
      baseURL,
    });
  };

  const getCopilotHistoryConversations = (conversationId: string): Response<ICopilotChatMessageResponse> => {
    return http.get(urlcat(PATH_COPILOT_HISTORY_CONVERSATIONS, { conversationId }), {
      baseURL,
    });
  };

  const cancelCopilotConversation = (conversationId: string): Response<any> => {
    return http.post(urlcat(PATH_COPILOT_CANCEL_CONVERSATION, { conversationId }), null, {
      baseURL,
    });
  };
  const createCopilotConversation = (spaceId: string, type: string): Response<any> => {
    return http.post(PATH_COPILOT_CREATE_CONVERSATION, { spaceId, type }, {
      baseURL,
    });
  };

  const getCopilotSuggestion = (type: string): Response<string[]> => {
    return http.post(PATH_COPILOT_SUGGESTIONS, { type }, {
      baseURL,
    });
  };

  return {
    baseURL,
    getAiInfo,
    getAISetting,
    getChatHistoryList,
    updateAIInfo,
    getTrainingDataSource,
    addTrainingDataSource,
    deleteTrainingDataSource,
    getConversationSuggestion,
    getAITrainingStatus,
    train,
    sendAIFeedback,
    getAIFeedbackByConversationId,
    getAIFeedback,
    updateAIFeedback,
    trainPredict,
    fetchFormPackForAI,
    addFormRecordForAI,
    getTrainingHistory,
    shareOpen,
    shareClose,
    shareInfo,
    getConversationHistory,
    getInsightConversations,
    // copilot
    getCopilotInfo,
    getCopilotHistoryConversations,
    cancelCopilotConversation,
    createCopilotConversation,
    getCopilotSuggestion,
  };
};
