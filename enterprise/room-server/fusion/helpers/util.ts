import crypto from 'crypto';

export const generateConversationId = (): string => {
  return `cs-${crypto.randomUUID()}`;
};