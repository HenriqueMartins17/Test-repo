import EventEmitter from 'events';
import classNames from 'classnames';
import { useEffect, useRef } from 'react';
import { IconButton, Typography, Skeleton, useResponsive, ScreenSize } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import { CloseOutlined } from '@apitable/icons';
import styles from './index.module.less';
import { Popup } from '@/components/Popup';
import { IConversationHistoryItem } from '@/shared/types';

interface Props {
  open: boolean;
  history?: IConversationHistoryItem;
  close: () => void;
  setHistory: (item: IConversationHistoryItem) => void;
  refresh: () => Promise<void>;
  append: () => Promise<void>;
  hasNextPage: boolean;
  loading: boolean;
  data: IConversationHistoryItem[];
  currentConversation: {
    conversationId: string;
    trainingId: string;
  };
}

export function History(props: Props) {
  const { refresh, append, hasNextPage, loading, data, open, close } = props;
  const ref = useRef<HTMLDivElement>(null);

  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.md);

  const onScroll = async () => {
    if (ref.current && hasNextPage && !loading) {
      const { scrollTop, scrollHeight, clientHeight } = ref.current;
      if (scrollTop + clientHeight >= scrollHeight) {
        await append();
      }
    }
  };

  if (!props.open) {
    return null;
  }

  if (isMobile) {
    return (
      <Popup
        className={styles.popup}
        open={props.open}
        onClose={close}
        height="90%"
        title={
          <Typography variant='h6'>{ t(Strings.ai_agent_conversation_list) }</Typography>
        }
      >
        <div
          className={styles.historyList}
          onScroll={onScroll}
          ref={ref}>
          {data.map((item) => {
            return (
              <div
                className={
                  classNames(styles.historyItem, {
                    [styles.active]: (
                      (props.history && item.id === props.history.id && item.trainingId === props.history.trainingId) ||
                    (!props.history && props.currentConversation && item.id === props.currentConversation.conversationId && item.trainingId === props.currentConversation.trainingId)
                    ),
                  })
                }
                key={item.id}
                onClick={() => {
                  props.setHistory(item);
                  close();
                }}
                title={item.title}
              >
                <div className={styles.historyItemTitle}>{item.title}</div>
              </div>
            );
          })}
          {loading && (
            <>
              <Skeleton height="24px" />
              <Skeleton height="24px" />
              <Skeleton height="24px" />
            </>
          )}
        </div>
      </Popup>
    );
  }

  return (
    <div className={styles.history}>
      <div className={styles.historyTitle}>
        {/* {t(Strings.ai_agent_conversation_list)} */}
        <Typography variant='h6'>{ t(Strings.ai_agent_conversation_list) }</Typography>
        <IconButton onClick={props.close} icon={CloseOutlined} />
      </div>
      <div className={styles.historyList} onScroll={onScroll} ref={ref}>
        {data.map((item) => {
          return (
            <div className={
              classNames(styles.historyItem, {
                [styles.active]: (
                  (props.history && item.id === props.history.id && item.trainingId === props.history.trainingId) ||
                    (!props.history && props.currentConversation && item.id === props.currentConversation.conversationId && item.trainingId === props.currentConversation.trainingId)
                ),
              })
            } key={item.id} onClick={() => props.setHistory(item)}>
              <div className={styles.historyItemTitle}>{item.title}</div>
            </div>
          );
        })}
        {loading && (
          <>
            <Skeleton height="24px" />
            <Skeleton height="24px" />
            <Skeleton height="24px" />
          </>
        )}
      </div>
    </div>
  );
}
