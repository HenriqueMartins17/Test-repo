import classNames from 'classnames';
import React, { useEffect, useMemo, useState } from 'react';
import { useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { DislikeFilled, LikeFilled, UserFilled } from '@apitable/icons';
import ChatBotAvatar from './chatbot_avatar.png';
import { TextLoading } from './loading';
import markdown from './markdown';
import MessageToolBar, { IToolBarStateProps } from './tool';
import styles from './style.module.less';
import { Avatar, AvatarSize } from '@/components';
import * as Shared from '@/shared';
import { ChatStatus, ChatType } from '@/shared/types/chat';
import { getIsFromIframe } from '@/shared/utils';

export interface IMessageProps {
  role: ChatType;
  index?: number;
  content?: string | React.ReactNode;
  style?: React.CSSProperties;
  status?: ChatStatus;
  delay?: number;
  preview?: boolean;
  feedback?: {
    like: Shared.AIFeedbackType;
    comment: string;
  };
  userInfo?: {
    avatar: string;
    nickName: string;
    avatarColor?: number | null;
  } | null;
  toolBarState?: IToolBarStateProps;
}

export default function Message(props: IMessageProps) {
  const { role, content, status = ChatStatus.Normal, delay, feedback } = props;
  const { avatar, nickName, avatarColor } = props.userInfo || {};
  const isIFrame = getIsFromIframe();
  const theme = useThemeColors();
  const [value, setValue] = useState(() => (delay ? '' : content));
  const colors = useThemeColors();

  const isError = status === ChatStatus.Error || status === ChatStatus.Abort;

  useEffect(() => {
    if (!delay) {
      setValue(content);
    }
  }, [delay, content]);

  useEffect(() => {
    let timer!: number;
    if (delay && content && typeof content === 'string') {
      setTimeout(() => {
        const interval = delay / content.length;
        let index = 0;
        timer = window.setInterval(() => {
          if (index < content.length) {
            const char = content[index];
            if (char) {
              setValue((prev) => {
                return prev + char;
              });
            }
            index++;
          } else {
            clearInterval(timer);
          }
        }, interval);
      }, 200);
    }
    return () => {
      if (timer) clearTimeout(timer);
    };
  }, [content, delay]);

  const renderAvatar = useMemo(() => {
    if (role === ChatType.User) {
      if (isIFrame) {
        return null;
      }
      if (!props.userInfo) {
        return (
          <div className={classNames([styles.messageAvatar, styles.bg])}>
            <UserFilled size={32} color={theme.textStaticPrimary} />
          </div>
        );
      }
      return (
        <div className={styles.messageAvatar}>
          <Avatar src={avatar} title={nickName || 'unknow'} avatarColor={avatarColor} size={AvatarSize.Size40} />
        </div>
      );
    }
    return (
      <div className={classNames(styles.messageAvatar)}>
        <img
          className={classNames({
            [styles.mini]: isIFrame,
          })}
          src={ChatBotAvatar}
        />
      </div>
    );
    // isIFrame
  }, [role, avatar, nickName, avatarColor, theme.textStaticPrimary, isIFrame, props.userInfo]);

  const renderContent = useMemo(() => {
    if (value) {
      if (role === ChatType.User) {
        return <div className={styles.messageItem}>{value}</div>;
      }
      if (typeof value === 'string') {
        return (
          <div className={classNames(styles.messageItem, { [styles.error]: isError })} dangerouslySetInnerHTML={{ __html: markdown.render(value) }} />
        );
      }
      <div className={classNames(styles.messageItem, { [styles.error]: isError })}>{value}</div>;
    }
    if (isError) {
      return (
        <div className={classNames(styles.messageItem, { [styles.error]: isError })}>
          <p className={styles.error}>{t(Strings.ai_send_cancel)}</p>
        </div>
      );
    }
    if (!value) {
      if (status === ChatStatus.WaitResponse || status === ChatStatus.Transmission || delay) {
        return (
          <div className={styles.messageItem}>
            <TextLoading />
          </div>
        );
      }
      if (role === ChatType.User) {
        return <div className={styles.messageItem}>The datasources have been selected, let's start the training</div>;
      }
      return (
        <div className={styles.messageItem}>
          <span style={{ opacity: 0.5 }}>{t(Strings.ai_empty)}</span>
        </div>
      );
    }

    return <div className={styles.messageItem} />;
  }, [value, role, isError, status, delay]);

  const renderToolBar = () => {
    if (props.index !== undefined && !props.preview && props.toolBarState) {
      return <MessageToolBar {...props} toolBarState={props.toolBarState} index={props.index} />;
    }
    return null;
  };

  return (
    <div data-index={props.index} className={classNames(styles.message, role === ChatType.User ? styles.user : styles.bot, 'markdown-body')}>
      {renderAvatar}
      <div className={styles.messageBox}>
        <div className={styles.messageBoxContent}>
          {renderToolBar()}
          {renderContent}
        </div>
        {(status === ChatStatus.Transmission || status === ChatStatus.WaitResponse) && <em className={styles.typing}>{t(Strings.ai_typing)}</em>}

        {feedback && (
          <div
            className={classNames({
              [styles.feedbackInfo]: true,
              [styles.like]: feedback.like === Shared.AIFeedbackType.Like,
              [styles.dislike]: feedback.like === Shared.AIFeedbackType.DisLike,
            })}
          >
            <span className={styles.feedbackInfoIcon}>
              {feedback.like === Shared.AIFeedbackType.DisLike ? (
                <DislikeFilled color={colors.textDangerDefault} />
              ) : (
                <LikeFilled color={colors.textBrandDefault} />
              )}
            </span>
            {feedback.comment && (
              <p className={styles.feedbackInfoContent} title={feedback.comment}>
                <span>{feedback.comment}</span>
              </p>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
