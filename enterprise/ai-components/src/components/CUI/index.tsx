import { Row, Col } from 'antd';
import classNames from 'classnames';
import React, { useCallback, useMemo } from 'react';
import { Typography, useThemeColors } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import * as components from './components';
import styles from './style.module.less';
import Message from '@/components/Chat/List/Message';
import { CUIChatRole, ICUIChat, ICUIForm, ICUISystemChat, ICUIUserChat, ICUIBotChat } from '@/shared/cui/types';
import { useAIContext } from '@/shared/hook/use_ai_content';
import { ConversationStatus } from '@/shared/store/interface';
import { ICUIItem, ChatStatus, ChatType } from '@/shared/types/chat';
export * from './components';

export type ICUIComponent = {
  component: React.FC<any>;
  getDisplayResultWithComponent?: (form: ICUIForm, data: any) => string | React.ReactNode;
};

const REGISTER_CUI_COMPONENT: Record<string, ICUIComponent> = {};

const getComponent = (name: string) => {
  if (REGISTER_CUI_COMPONENT[name]) {
    return REGISTER_CUI_COMPONENT[name].component;
  }
  return components[name];
};

export const registerCUIComponent = (name: string, component: ICUIComponent) => {
  if (REGISTER_CUI_COMPONENT[name]) {
    throw new Error(`CUI component ${name} already exists`);
  }
  REGISTER_CUI_COMPONENT[name] = component;
};

const getDisplayResultWithComponent = (form: ICUIForm, data: any): string | React.ReactNode => {
  const component = REGISTER_CUI_COMPONENT[form.component];
  if (component && component.getDisplayResultWithComponent) {
    return component.getDisplayResultWithComponent(form, data);
  }
  switch (form.component) {
    case 'CUIFormRadio': {
      // @ts-ignore
      const find = form.props.options.find((item) => item.value === data);
      return find && find.label ? find.label : data;
    }
    default: {
      if (typeof data === 'string') {
        return data;
      }
      return JSON.stringify(data);
    }
  }
};

type ICUIProps = ICUIItem & {
  refreshScroll?: () => void;
  userInfo?: {
    avatar: string;
    nickName: string;
    avatarColor?: number | null;
  } | null;
};

export function CUI(props: ICUIProps) {
  const { instance, result, form, status, refreshScroll } = props;
  const [throttlingChatList, setThrottlingChatList] = React.useState<ICUIChat[]>([]);
  const { context } = useAIContext();
  const color = useThemeColors();
  const ChatComponent = useCallback(
    (chat: ICUIChat) => {
      if (chat.role === CUIChatRole.Bot) {
        return <Message status={ChatStatus.Complete} role={ChatType.Bot} delay={chat.delay} content={chat.props.content} />;
      } else if (chat.role === CUIChatRole.User) {
        return <Message userInfo={props.userInfo} status={ChatStatus.Complete} role={ChatType.User} content={chat.props.content} />;
      } else if (chat.role === CUIChatRole.System) {
        const Components = getComponent(chat.component);
        if (Components) {
          if (context.state.conversationStatus === ConversationStatus.Form && context.state.currentActionsId === instance.id) {
            return (
              <>
                <div className={styles.right}>
                  <Components {...chat.props} />
                </div>
                {!context.isWizardMode && (
                  <div className={classNames([styles.right, styles.skip])}>
                    <Typography
                      variant="body2"
                      color={color.textCommonTertiary}
                      onClick={() => {
                        instance.exit();
                      }}
                    >
                      {t(Strings.cui_chat_exit_text)}
                    </Typography>
                  </div>
                )}
              </>
            );
          }
          return null;
        }
        return <>unknown component {JSON.stringify(chat)}</>;
      }
      return null;
    },
    [context.state.conversationStatus, context.state.currentActionsId, instance.id],
  );

  const renderChatItem = (key: string, index: number, chat: ICUIChat) => {
    return (
      <Row key={key + '-' + index}>
        <Col xs={{ span: 24 }} lg={{ span: 24 }} xl={{ span: 20, offset: 2 }} xxl={{ span: 18, offset: 3 }} className={styles.form}>
          <ChatComponent {...chat} />
        </Col>
      </Row>
    );
  };

  const chatList = useMemo(() => {
    const data: ICUIChat[] = [];
    const throttling: ICUIChat[] = [];
    const isComplete = status === ChatStatus.Complete;
    const loop = (item: ICUIForm) => {
      const isCompleteInput = item.field in result;
      const isThrottlingMode = !isCompleteInput && status === ChatStatus.Normal;
      if (item.message) {
        const messages = typeof item.message === 'string' ? [item.message] : item.message;
        messages.forEach((message) => {
          const delay = isThrottlingMode ? Math.min(500, message.length * 23.5) : 0;
          const bot: ICUIBotChat = {
            role: CUIChatRole.Bot,
            delay,
            props: { content: message },
          };
          isThrottlingMode ? throttling.push(bot) : data.push(bot);
        });
      }
      if (!isCompleteInput) {
        const chat: ICUISystemChat = {
          role: CUIChatRole.System,
          component: item.component,
          props: {
            ...item.props,
            onSubmit: async (val) => {
              const nextForm = instance.hasNextForm(item, val);
              const values = { ...result, [item.field]: val };
              if (!nextForm) {
                try {
                  await instance.finish(values);
                } catch (e) {
                  console.error(e);
                }
              } else {
                instance.update(values);
              }
            },
            onReset: () => {
              // @todo
              // const values = { ...result };
              // delete value[item.field];
              // instance.update(values);
            },
            showReset: !isComplete,
          },
        };
        // chat.props.defaultValue = result;
        // chat.props.isComplete = true;
        isThrottlingMode ? throttling.push(chat) : data.push(chat);
      } else {
        const value = result[item.field];
        const display = getDisplayResultWithComponent(item, value);
        const user: ICUIUserChat = {
          role: CUIChatRole.User,
          props: { content: display },
        };
        const nextForm = instance.hasNextForm(item, value);
        isThrottlingMode ? throttling.push(user) : data.push(user);
        if (nextForm) {
          loop(nextForm);
        }
      }
    };
    // go
    loop(form);
    // simulated delay effect
    setThrottlingChatList([]);
    if (throttling.length) {
      let delay = 0;
      throttling.forEach((item, index) => {
        setTimeout(
          () => {
            setThrottlingChatList((prev) => {
              return [...prev, item];
            });
            refreshScroll?.();
          },
          delay + index * 500,
        );
        delay += item.delay || 0;
      });
    }
    return data;
  }, [form, result, status, instance]);

  return (
    <>
      {chatList && chatList.map((chat, index) => renderChatItem('chat', index, chat))}
      {throttlingChatList && throttlingChatList.map((chat, index) => renderChatItem('throttling', index, chat))}
    </>
  );
}
