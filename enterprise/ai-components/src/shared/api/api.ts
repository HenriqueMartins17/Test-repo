import { IServerFormPack, IServerResponse } from '@apitable/core';
import { AIFeedbackState, AIFeedbackType } from '@/shared/enum';
import {
  IAIFeedback,
  IAIFeedbackDetail,
  IAIInfoResponse,
  IChatMessageListResponse,
  IGetAIInfoResponse,
  IGetAITrainingStatusResponse,
  IAISettingResponse,
  IAIShareInfoResponse,
  IDatasourceResponse,
  IDataSourceUpdate,
  IPageResponse,
  IConversationHistoryItem,
} from '@/shared/types';

export type Response<T> = Promise<IServerResponse<T>>;

export interface IAIAPIContextState {
  baseURL: string;
  getAiInfo: (aiId: string) => Response<{ ai: IAIInfoResponse }>;
  getAISetting: (aiid: string, type?: string) => Response<IAISettingResponse>;
  getChatHistoryList: (
    aiId: string,
    search?: {
      cursor?: string;
      limit?: number;
      conversationId?: string;
      trainingId?: string;
    },
  ) => Response<IChatMessageListResponse>;
  updateAIInfo: (info: Partial<IAIInfoResponse>, aiId: string) => Response<{ ai: IAIInfoResponse }>;
  getTrainingDataSource: (aiId: string) => Response<IDatasourceResponse>;
  addTrainingDataSource: (aiId: string, data: IDataSourceUpdate) => Response<IDatasourceResponse>;
  deleteTrainingDataSource: (aiId: string, datasourceId: string) => Response<IDatasourceResponse>;
  getConversationSuggestion: (aiId: string, question?: string, n?: number) => Response<IGetAIInfoResponse>;
  getAITrainingStatus: (aiId: string) => Response<IGetAITrainingStatusResponse>;
  train: (aiId: string) => Response<IGetAITrainingStatusResponse>;
  sendAIFeedback: (
    aiId: string,
    data: {
      trainingId: string;
      conversationId: string;
      messageIndex: number;
      like: AIFeedbackType;
      comment: string;
    },
  ) => Response<IAIFeedback>;
  getAIFeedbackByConversationId: (aiId: string, conversationId: string, trainingId?: string) => Response<{ fb: IAIFeedback[] }>;
  getAIFeedback: (
    aiId: string,
    params: {
      pageNum: number;
      pageSize: number;
      state?: AIFeedbackState;
      search?: string;
    },
  ) => Response<{
    total: number;
    fb: IAIFeedbackDetail[];
    pageSize: number;
    pageNum: number;
  }>;
  updateAIFeedback: (
    aiId: string,
    feedbackId: string,
    data: {
      state: AIFeedbackState;
    },
  ) => Response<IAIFeedback>;
  trainPredict: (
    aiId: string,
    data: {
      dataSources: { nodeId: string }[];
    },
  ) => Response<{
    characters: number;
    words: number;
    creditCost: number;
  }>;
  fetchFormPackForAI: (aiId: string) => Response<IServerFormPack>;
  addFormRecordForAI: (aiId: string, recordData: Record<string, any>) => Response<any>;
  shareOpen: (aiId: string) => Response<any>;
  shareClose: (aiId: string) => Response<any>;
  shareInfo: (aiId: string) => Response<IAIShareInfoResponse>;
  getTrainingHistory: (aiId: string) => Response<any>;
  getConversationHistory: (aiId: string, pageNum: number, pageSize: number) => Response<IPageResponse<IConversationHistoryItem>>;
  getInsightConversations: (aiId: string, pageNum: number, pageSize: number) => Response<IPageResponse<IConversationHistoryItem>>;
}
