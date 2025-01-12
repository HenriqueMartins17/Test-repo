import cls from 'classnames';
import React from 'react';
import { Typography } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import { CheckOutlined } from '@apitable/icons';
import styles from './style.module.less';
import { ChatStatus } from '@/shared/types/chat';

interface ISystemTip {
  content: React.ReactNode;
  status?: ChatStatus;
  layout?: 'center' | 'left';
  onClick?: () => Promise<void>;
}
export function MessageSystemTip(props: ISystemTip) {
  const { layout = 'center' } = props;
  return (
    <div
      className={cls({
        [styles.systemTipWrapper]: true,
        [styles.systemTipWrapperError]: props.status === ChatStatus.Error,
      })}
      style={{
        justifyContent: layout === 'center' ? 'center' : 'flex-start',
      }}
    >
      { layout === 'left' && (
        <CheckOutlined className={styles.systemTipWrapperIcon} />
      )}
      {typeof props.content === 'string' ? (
        <Typography variant='body4'>
          { props.content }
        </Typography>
      ) : props.content}
      { props.status === ChatStatus.Error && props.onClick && (
        // try again
        <p className={styles.systemTipWrapperErrorBtn} onClick={props.onClick}>
          {t(Strings.ai_try_again)}
        </p>
      )}
    </div>
  );
}
