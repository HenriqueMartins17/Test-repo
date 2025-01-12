import axios from 'axios';
import { sprintf } from 'sprintf-js';
import fetch, { Response } from 'node-fetch';
import { INestChatOptions } from 'enterprise/fusion/models/ai.model';
import {
  AiInfoResponse,
  AiResponse,
  DataSetRequest,
  IChatOptions,
  MessagesObject,
  PostTrainResult,
  SuggestionList,
  TrainingInfo,
  TrainPredictResult
} from './ai.server.model';
import { AiInfo } from './ai.info.class';

const TRAIN = '/ai/trainers/%(aiId)s/train';
const TRAIN_PREDICT = '/ai/trainers/%(aiId)s/train/predict';
const PREDICT = '/ai/trainers/predict';
const GET_TRAINING_INFO = '/ai/trainers/%(aiId)s/trainings/%(trainingId)s';
const GET_AI_INFO = '/ai/inference/%(aiId)s';
const GET_AI_TRAINING_LIST = '/ai/inference/%(aiId)s/trainings';
const GET_SUGGESTIONS = '/ai/inference/%(aiId)s/suggestions';
const GET_MESSAGES = '/ai/inference/%(aiId)s/conversations/%(conversationId)s';
const GET_CONVERSATION_MESSAGES = '/ai/inference/%(aiId)s/trainings/%(trainingId)s/conversations/%(conversationId)s';
const CHAT_COMPLETIONS = '/ai/inference/%(aiId)s/chat/completions';

const aiServerFactory = (baseUrl: string) => {
  axios.interceptors.response.use((response) => {
    return response;
  }, (error) => {
    // this is for the case when the ai server is error
    return Promise.reject(error);
  });
  return {
    baseUrl,
    postTrain: async (aiId: string): Promise<PostTrainResult> => {
      const url = `${baseUrl}` + sprintf(TRAIN, { aiId });
      return (await axios.post<AiResponse<PostTrainResult>>(url)).data.data;
    },
    predictTrain: async (aiId: string): Promise<TrainPredictResult[]> => {
      const url = `${baseUrl}` + sprintf(TRAIN_PREDICT, { aiId });
      return (await axios.post<AiResponse<TrainPredictResult[]>>(url)).data.data;
    },
    predict: async (payload: DataSetRequest[]): Promise<TrainPredictResult[]> => {
      const url = `${baseUrl}` + PREDICT;
      return (await axios.post<AiResponse<TrainPredictResult[]>>(url, payload)).data.data;
    },
    getTrainingInfo: async (aiId: string, trainingId: string): Promise<TrainingInfo> => {
      const url = `${baseUrl}` + sprintf(GET_TRAINING_INFO, { aiId, trainingId });
      return (await axios.get<AiResponse<TrainingInfo>>(url)).data.data;
    },
    getAiInfo: async (aiId: string): Promise<AiInfo | undefined> => {
      const url = `${baseUrl}` + sprintf(GET_AI_INFO, { aiId });
      const data = (await axios.get<AiResponse<AiInfoResponse>>(url)).data.data;
      return data && new AiInfo(data);
    },
    getAiTrainings: async (aiId: string): Promise<TrainingInfo[]> => {
      const url = `${baseUrl}` + sprintf(GET_AI_TRAINING_LIST, { aiId });
      return (await axios.get<AiResponse<TrainingInfo[]>>(url)).data.data;
    },
    getSuggestions: async (aiId: string, data: { question?: string, n?: number }): Promise<SuggestionList> => {
      const url = `${baseUrl}` + sprintf(GET_SUGGESTIONS, { aiId });
      return (await axios.post<AiResponse<SuggestionList>>(url, data)).data.data;
    },
    getMessages: async (aiId: string, conversationId: string, cursor?: string, limit?: number): Promise<MessagesObject[]> => {
      const url = `${baseUrl}` + sprintf(GET_MESSAGES, {
        aiId,
        conversationId
      }) + sprintf('?cursor=%(cursor)s&limit=%(limit)s', { cursor, limit });
      return (await axios.get<AiResponse<MessagesObject[]>>(url)).data.data;
    },
    getConversationMessages: async (aiId: string, trainingId: string, conversationId: string): Promise<MessagesObject[]> => {
      const url = `${baseUrl}` + sprintf(GET_CONVERSATION_MESSAGES, { aiId, trainingId, conversationId });
      return (await axios.get<AiResponse<MessagesObject[]>>(url)).data.data;
    },
    chatCompletionsStream: (option: IChatOptions) => {
      const url = `${baseUrl}` + sprintf(CHAT_COMPLETIONS, { aiId: option.aiId });
      const controller = new AbortController();
      // cancel requests after 12s
      const timeout = setTimeout(
        () => {
          controller.abort();
        },
        12000
      );
      option.onAbort?.(controller);

      let finished = false;

      const finish = () => {
        if (!finished) {
          option.onFinish('');
          finished = true;
        }
      };

      controller.signal.onabort = finish;

      let requestPayload;
      if (option instanceof INestChatOptions) {
        requestPayload = { ...option.chatRo, conversation_id: option.conversationId };
      } else {
        requestPayload = {
          conversation_id: option.conversationId,
          messages: [{ role: 'user', content: option.content }],
          stream: true
        };
      }
      fetch(url, {
        method: 'POST',
        body: JSON.stringify(requestPayload),
        headers: { 'Content-Type': 'application/json' },
        signal: controller.signal
      })
        .then((res: Response) => {
          res.body.on('data', (chunk: string) => {
            option.onUpdate?.(chunk.toString());
          });

          res.body.on('close', () => {
            finish();
          });

          res.body.on('end', () => {
            finish();
          });

          res.body.on('error', (err: Error) => {
            option.onError?.(err);
            throw err;
          });
        })
        .catch((err: Error) => {
          option.onError?.(err);
        })
        .finally(() => {
          clearTimeout(timeout);
        });
    }
  };
};

const baseUrl = process.env.AI_SERVER_URL || 'http://127.0.0.1:8626';

export const aiServer = aiServerFactory(baseUrl);

// Usage Example:
// console.log("The httpClient's baseUrl is "+ aiServer.baseUrl);
// const data = aiServer.getSuggestions("ai_");
