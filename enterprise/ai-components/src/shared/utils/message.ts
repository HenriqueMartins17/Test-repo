import { IChatMessageResponse, ICopilotChatMessage } from '@/shared/types';
import { ChatType, IChatListItem, ChatStatus } from '@/shared/types/chat';

export const convertMessageList2ChatList = (messages: (IChatMessageResponse | ICopilotChatMessage)[]): IChatListItem[] => {
  const data: IChatListItem[] = [];
  let index = 0;

  for (const v of messages || []) {
    switch (v.type) {
      case 'user':
      case 'human': {
        data.push({
          // @ts-ignore
          id: v.data.id,
          type: ChatType.User,
          content: v.data.content,
        });
        break;
      }
      case 'assistant':
      case 'ai': {
        data.push({
          // @ts-ignore
          id: v.data.id,
          type: ChatType.Bot,
          content: v.data.content,
          index: index++,
        });
        break;
      }
      case 'chat_break': {
        data.push({
          type: ChatType.System,
          content: v.data.content,
          status: ChatStatus.Normal,
        });
        break;
      }
      default:
        console.warn('ai chat messages:' + v.type + ' is not supported');
    }
  }

  return data;
};
