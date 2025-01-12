import { fetchEventSource } from '@microsoft/fetch-event-source';
import { useRef } from 'react';
import { v4 } from 'uuid';
import { Strings, t } from '@apitable/core';
import { ChatStatus } from '../types';
import { BillingErrorCode } from '@/shared/enum';
import { BillingUsageOverLimitError } from '@/shared/error';
import { IChatAction } from '@/shared/store/interface';

interface IFnRef {
  abort?: () => void;
  interval?: number;
  activeTime?: number;
}

interface IParams {
  dispatch: React.Dispatch<IChatAction>;
}

export interface SendMessageParams {
  endPoint: string;
  content: string;
  conversationId: string;
  context?: Record<string, any>;
  onOpen?: (response: Response) => void;
  onActions?: (actions: { name: string }[]) => void;
}

/**
 * 使用 useAIContext 中的 sendMessage 封装了
 * @param params
 * @returns
 */
export const useSendChat = (params: IParams) => {
  const { dispatch } = params;
  const chatRef = useRef<IFnRef>({});

  const clearIntervals = () => {
    if (chatRef.current.interval) {
      clearInterval(chatRef.current.interval);
      chatRef.current.interval = undefined;
    }
    chatRef.current.activeTime = 0;
  };

  const send = (params:SendMessageParams): [Promise<void>, () => void] => {
    const { endPoint, content, conversationId } = params;
    const uniqueId = v4();
    const controller = new AbortController();
    let actions: any[] = [];
    let lastMessageId = '';
    const abort = () => {
      if (controller && controller.abort) {
        controller.abort();
      }
      clearIntervals();
      dispatch({ type: 'abortChat' });
    };
    // 5s timeout
    clearIntervals();
    chatRef.current.interval = window.setInterval(() => {
      if (chatRef.current.activeTime && Date.now() - chatRef.current.activeTime > 1000 * 60) {
        abort();
        console.error('ai response timeout');
        clearIntervals();
      }
    }, 1000);
    chatRef.current.abort = abort;
    dispatch({ type: 'createChat', value: { content, id: uniqueId } });
    const promise = new Promise<void>((resolve, reject) => {
      fetchEventSource(endPoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        onmessage(ev) {
          chatRef.current.activeTime = Date.now();
          if (!ev.data) return;
          if (ev.data === '[DONE]') {
            clearIntervals();
            dispatch({ type: 'completeChat', value: { id: lastMessageId } });
            if (params.onActions && actions.length) {
              params.onActions(actions);
            }
            resolve();
          } else {
            const data = JSON.parse(ev.data);
            try {
              if (typeof data === 'object') {
                if (lastMessageId && lastMessageId !== data.id) {
                  dispatch({ type: 'updateChatStatus', value: { id: lastMessageId, status: ChatStatus.Complete } });
                }
                // Two error structures
                if (data.error) {
                  dispatch({ type: 'errorChat', value: { error: data.error, id: uniqueId } });
                  reject(data.error);
                } else if (data.status === 'error') {
                  const content = data.choices[0].delta?.content || data.choices[0].message.content;
                  dispatch({ type: 'errorChat', value: { error: content, id: data.id } });
                  reject(content);
                } else if (data.type === 'status') {
                  const content = data.choices[0].delta?.content || data.choices[0].message.content;
                  dispatch({
                    status: ChatStatus.Complete, type: 'insertSystemTip', value: t(Strings[content]), id: data.id, layout: 'left'
                  });
                } else {
                  const content = data.choices[0].delta.content;
                  dispatch({ type: 'updateChat', value: { content, id: data.id } });
                }
                if (data.actions && data.actions.length) {
                  actions = data.actions;
                }
                lastMessageId = data.id;
              }
            } catch (e) {
              dispatch({ type: 'errorChat', value: { error: (e as Error).message, id: uniqueId } });
            }
          }
        },
        async onopen(response) {
          if (response.ok) {
            const conversationId = response.headers.get('X-Conversation-Id');
            if (conversationId) {
              dispatch({ type: 'setCurrentConversationId', value: conversationId });
            }
          } else if (response.status >= 500) {
            throw new Error('Service is not available, please try it again later');
          } else if (response.status >= 400) {
            try {
              // 假定报错一定是JSON
              const body = await response.text();
              const data = JSON.parse(body);
              if (data.code === BillingErrorCode) {
                throw new BillingUsageOverLimitError();
              }
              throw new Error(data.message);
            } catch (e) {
              throw new Error(e.message);
            }
          }
          if (params.onOpen) {
            params.onOpen(response);
          }
        },
        body: JSON.stringify({
          content: content,
          conversationId,
          ...(params.context || {}),
        }),
        signal: controller.signal,
        onclose() {
          clearIntervals();
          // connection closed
          console.log('connection closed');
        },
        onerror(err) {
          dispatch({ type: 'errorChat', value: { error: err.message, id: uniqueId } });
          clearIntervals();
          reject(err);
          // stop retrying:
          throw err;
        },
      });
    });
    return [promise, abort];
  };

  const abort = () => {
    if (chatRef.current.abort) {
      chatRef.current.abort();
    }
  };

  return { send, abort };
};
