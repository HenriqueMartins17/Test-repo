import { StrictRJSFSchema, UiSchema } from '@rjsf5/utils';
import React from 'react';
import { ConfigConstant, IField } from '@apitable/core';
import { AIFeedbackType, AIType, IAIMode, TrainingStatus } from '../enum';
import { IAIAPIContextState } from '@/shared/api';
import { IHistoryState } from '@/shared/hook/use_history';
import { SendMessageParams } from '@/shared/hook/use_send_chat';
import { IChatAction, IChatState } from '@/shared/store/interface';

export * from './chat';

type ITimestamp = number;

export interface IPageResponse<T> {
  pageNum: number;
  pageSize: number;
  size: number;
  total: number;
  pages: number;
  startRow: number;
  endRow: number;
  prePage: number;
  nextPage: number;
  firstPage: boolean;
  lastPage: boolean;
  hasPreviousPage: boolean;
  hasNextPage: boolean;
  records: T[];
}

interface IAISettingsBase {
  prologue?: string;
  prompt?: string;
  type: AIType;
  model?: string;
  mode?: IAIMode;
  /** Whether to show more content after the dialogue */
  isEnabledPromptTips?: boolean;
  /** Whether to display the welcome prompt box */
  isEnabledPromptBox?: boolean;
  formName?: string;
}

export interface IAIQASettings extends IAISettingsBase {
  type: AIType.Qa;
  idk?: string;
  formId?: string;
  isEnableCollectInformation?: boolean;
  isEnableOpenUrl?: boolean;
  openUrl?: string;
  openUrlTitle?: string;
}

export interface IAIChatSettings extends IAISettingsBase {
  type: AIType.Chat;
}

export type IAISettings = IAIQASettings | IAIChatSettings;

export interface IAISourceDatasheet {
  nodeId: string;
  nodeName?: string;
  nodeType?: ConfigConstant.NodeType;
  setting?: {
    viewId: string;
    rows: number;
    fields: IField[];
  };
}

export type IAIInfoResponse = {
  type: AIType;
  id: string;
  name: string;
  picture: string;
  description: string;
  currentConversationId: string;
  currentConversationCreatedAt?: ITimestamp;
  created: ITimestamp;
  livemode: boolean;
  latestTrainingId: string;
  latestTrainingStatus: TrainingStatus;
  completed: string;
  createdAt: ITimestamp;
  node: {};
  isTrained: boolean;
  remainTrainingCounts?: number;
  latestTrainingCompletedAt?: ITimestamp;
  dataSourcesUpdated?: boolean;
  formName?: string;
  messageCreditLimit: {
    maxCreditNums: number;
    remainCreditNums: number;
    remainChatTimes: number;
  };
  dataSources: IAISourceDatasheet[];
  setting: IAISettings;
};

export interface IChatMessageResponse {
  type: string;
  data: {
    additional_kwargs: {};
    content: string;
    example: boolean;
  };
}

export interface IAIServerResponse<T = any> {
  success: boolean;
  message: string;
  code: number;
  data: T;
}

export interface IGetAIInfoResponse {
  ai: string;
  conversation: string;
  suggestions: string[];
}

export interface IGetAITrainingStatusResponse {
  status: TrainingStatus;
}

export interface IChatMessageListResponse {
  currentConversationCreatedAt: number;
  data: IChatMessageResponse[];
  hasMore: boolean;
}

export interface IAIFeedback {
  id: string;
  messageIndex: number;
  isLike: boolean;
  comment: string;
  state: AIFeedbackType;
  created: ITimestamp;
  creator: string;
}

export interface IAIFeedbackDetail extends IAIFeedback {
  memberId: string;
  avatar: string;
  conversationId: string;
  trainingId: string;
  aiModel: string;
  botType: AIType;
  conversationTime: string;
}

export interface IAISettingResponse {
  JSONSchema: StrictRJSFSchema;
  UISchema: UiSchema;
  data: Record<string, any>;
}

export interface IAIShareInfoResponse {
  isEnabled: boolean;
  shareId: string;
}

export interface IAIContextState {
  api: IAIAPIContextState;
  data: IAIInfoResponse;
  state: IChatState;
  isInit: boolean;
  config: IAISettings;
  isWizardMode: boolean;
  history: IHistoryState;
  baseURL: string;
  isLogin?: boolean;
  send: (params: SendMessageParams) => [Promise<void>, () => void];
  abort: () => void;
  dispatch: React.Dispatch<IChatAction>;
  fetchAIDetail: (reload?: boolean, triggerInit?: boolean) => Promise<void>;
  initMessageList: () => Promise<void>;
  clearCurrentConversation: () => void;
  triggerUsageAlert: () => void;
  setHistory: (item: IConversationHistoryItem) => void;
}

export interface IDataSourceAirtableConfig {
  apiKey: string;
  baseId: string;
  tableId: string;
}

export interface IDataSourceAirtableSetting {
  baseId: string;
  tableId: string;
}

export interface IDataSourceAirtable {
  type: 'airtable';
  airtable: IDataSourceAirtableConfig;
}

export interface IDataSourceAitableConfig {
  apiKey: string;
  datasheetId: string;
  viewId: string;
}

export interface IDataSourceAitableSetting {
  datasheetId: string;
  viewId: string;
}

export interface IDataSourceAitable {
  type: 'aitable';
  aitable: IDataSourceAitableConfig;
}

export interface IDataSourceFileConfig {
  name: string;
  url: string;
}

export interface IDataSourceFileSetting {
  name: string;
  url: string;
}

export interface IDataSourceFile {
  type: 'file';
  file: IDataSourceFileConfig;
}

export interface IDataSourceDatasheetConfig {
  datasheetId: string;
  viewId: string;
}

export interface IDataSourceDatasheetSetting {
  datasheetId?: string;
  datasheetName?: string;
  viewId?: string;
  viewName?: string;
  rows?: number;
  revision?: number;
  fields?: IField[];
}

export interface IDatasheetDataSource {
  type: 'datasheet';
  datasheet: IDataSourceDatasheetConfig[];
}

export interface IDataSource {
  id: string;
  type: IDataSourceTypeName;
  setting: IDataSourceTypeSetting;
}

export type IDataSourceTypeName = 'airtable' | 'aitable' | 'file' | 'datasheet';

export type IDataSourceType = IDataSourceAirtable | IDataSourceAitable | IDataSourceFile | IDatasheetDataSource;

export type IDataSourceTypeConfig =
  IDataSourceAirtableConfig
  | IDataSourceAitableConfig
  | IDataSourceFileConfig
  | IDataSourceDatasheetConfig;

export type IDataSourceTypeSetting =
  IDataSourceAirtableSetting
  | IDataSourceAitableSetting
  | IDataSourceFileSetting
  | IDataSourceDatasheetSetting;

export type IDataSourceUpdate = IDataSourceType[];

export type IDatasourceResponse = IDataSource[];

export interface ITrainingHistoryItem {
  aiId: string;
  characterSum: number;
  creditCost: number;
  dataSources: any[];
  finishedAt: number;
  startedAt: number;
  status: TrainingStatus;
  tokensSum: number;
  trainingId: string;
}

export type ITraningHistoryResponse = ITrainingHistoryItem[];

export interface IConversationHistoryItem {
  id: string;
  aiId: string;
  trainingId: string;
  title: string;
  origin: string;
  created: number;
  user: {
    name: string;
    avatar: string;
  };
  transaction: {
    totalAmount: number;
  };

}

export interface ICopilotInfo {
  firstTimeUsed: boolean;
  latestConversation: {
    id: string;
    title: string;
    type: string;
    model: string;
    prologue: string;
    created: number;
  }
}

export interface ICopilotChatMessage {
  type: string;
  data: {
    id: string;
    conversationId: string;
    openaiMessageId: string;
    openaiThreadId: string;
    openaiRunId: string;
    openaiAssistantId: string;
    fileIds: string[];
    content: string;
    additionalKwargs: Record<string, any>;
    createdAt: number;
  }
}

export interface ICopilotChatMessageResponse {
  hasMore: boolean;
  data: ICopilotChatMessage[];
}