import React from 'react';
import { useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { PauseOutlined } from '@apitable/icons';
import styles from './style.module.less';
import { ConversationStatus } from '@/shared/store/interface';

interface IProps {
  status: ConversationStatus;
  stopConversation: () => void;
}

export function ChatControl(props: IProps) {
  const color = useThemeColors();
  if (props.status === ConversationStatus.Loading) {
    return (
      <div className={styles.chatControl}>
        <div className={styles.stopConversation} onClick={props.stopConversation}>
          <PauseOutlined color={color.textBrandDefault} />
          {t(Strings.ai_stop_conversation)}
        </div>
      </div>
    );
  }
  return null;
}
