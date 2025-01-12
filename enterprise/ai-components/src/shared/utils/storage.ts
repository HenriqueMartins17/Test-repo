import store from 'store2';

const NAME_SPACE = 'ai_conversation_id';

const conversation = store.namespace(NAME_SPACE);
export const saveConversationIdByAIId = (aiId: string, conversationId: string) => {
  conversation.set(aiId, conversationId);
};

export const getConversationIdByAIId = (aiId: string): string => {
  return conversation.get(aiId) || '';
};
