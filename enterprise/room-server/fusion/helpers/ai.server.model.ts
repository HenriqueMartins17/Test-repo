import { IField } from '@apitable/core';

export type AiResponse<T> = {
    code: number;
    msg: string;
    data: T;
};

export enum DataSetType {
    DATASHEET = 'datasheet',
}

/**
 * example:
 * {
 *   "ai_id": "ai_kfhwWSwlUrXNM0A",
 *   "ai_info": {
 *    "ai_id": "ai_kfhwWSwlUrXNM0A",
 *    "current_training_id": null,
 *    "success_train_history": [],
 *    "locking_training_id": null
 *   },
 *   "new_training_id": "ai_kfhwWSwlUrXNM0A_20230827161023",
 *   "ai_model": {},
 *   "ai_setting": null,
 *   "ai_bot_type": "chat"
 * }
 */
export type PostTrainResult = {
    new_training_id: string;
    ai_bot_type: string;
};

export type DataSetRequest = {
    type: DataSetType;
    type_id: string;
};

export type TrainPredictResult = {
    type: DataSetType,
    type_id: string,
    words: number,
    characters: number,
    tokens: number,
    count: number,
    fields: {
        [key: string]: IField;
    },
    revision: number
};

export enum TrainingStatus {
    FAILED = 'failed',
    NEW = 'new',
    TRAINING = 'training',
    SUCCESS = 'success',
}

export type DataSourceMeta = {
    count: number;
    fields: {
        [key: string]: IField;
    },
    revision: number;
};

export type DataSet = {
    type: DataSetType;
    type_id: string;
    meta: DataSourceMeta;
    words: number;
    characters: number;
    tokens: number;
};

export type TrainingInfo = {
    ai_id: string;
    training_id: string;
    status: TrainingStatus;
    info: string;
    data_sources: DataSet[];
    started_at: number;
    finished_at: number;
};

export type AiInfoResponse = {
    ai_id: string;
    current_training_id: string;
    locking_training_id: string;
    success_train_history: string[];
    current_training_info: TrainingInfo;
    locking_training_info: TrainingInfo;
};

export type SuggestionList = string[];

enum MessageType {
    HUMAN = 'human',
    AI = 'ai',
}

type MessageItem = {
    content: string;
    additional_kwargs: any;
    example: boolean;
};

export type MessagesObject = {
    type: MessageType;
    data: MessageItem;
};

export interface IChatOptions {
    aiId: string;
    conversationId: string;
    content: string;

    onUpdate?: (message: string) => void;
    onFinish: (message: string) => void;
    onError?: (err: Error) => void;
    onAbort?: (controller: AbortController) => void;
}