// enums definition
export enum ChangeType {
    INSERT = 'INSERT',
    UPDATE = 'UPDATE',
}

export enum AIType {
    'QA' = 'qa',
    'Chat' = 'chat',
}

export enum AIModel {
    GPT_3_5_TURBO = 'gpt-3.5-turbo',
    GPT_3_5_TURBO_16K = 'gpt-3.5-turbo-16k',
    GPT_3_5_TURBO_0613 = 'gpt-3.5-turbo-0613',
    GPT_3_5_TURBO_16K_0613 = 'gpt-3.5-turbo-16k-0613',
    GPT_4 = 'gpt-4',
    GPT_4_0613 = 'gpt-4-0613',
    GPT_4_32K = 'gpt-4-32k',
    GPT_4_32K_0613 = 'gpt-4-32k-0613',
}

export enum TransactionType {
    QUERY = 'query',
    TRAINING = 'training',
}

export enum ConversationOrigin {
    Internal = 'internal',
    Anonymous = 'anonymous',
}