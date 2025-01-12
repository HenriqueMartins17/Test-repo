import { Popover } from 'antd';
import classNames from 'classnames';
import RcTextArea from 'rc-textarea';
import React, { useState } from 'react';
import { useThemeColors, Typography, IconButton, Button, useResponsive, ScreenSize } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import { CloseOutlined } from '@apitable/icons';
import ToolBtn from './tool_btn';
import styles from './style.module.less';
import { Popup } from '@/components/Popup';
import * as Shared from '@/shared';

interface IIconProps {
  id?: string;
  status?: Shared.AIFeedbackType;
  children: React.ReactNode;
  feedbackActiveId?: string | null;
  setFeedbackActiveId?: (id: string | null) => void;
  sendFeedback?: (message: string) => Promise<void>;
}

export default function FeedbackItem(iconProps: IIconProps) {
  const { children, id = '', feedbackActiveId, status, setFeedbackActiveId, sendFeedback } = iconProps;
  const colors = useThemeColors();
  const visible = feedbackActiveId === id;
  const [loading, setLoading] = useState(false);
  const [value, setValue] = useState('');

  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.md);

  const onSend = async () => {
    try {
      setLoading(true);
      await sendFeedback?.(value);
    } finally {
      setLoading(false);
      setFeedbackActiveId?.(null);
      setValue('');
    }
  };

  if (isMobile) {
    if (!visible) {
      return (
        <ToolBtn
          active={visible}
          className={classNames({
            [styles.like]: status === Shared.AIFeedbackType.Like,
            [styles.dislike]: status === Shared.AIFeedbackType.DisLike,
          })}
          onClick={() => {
            setFeedbackActiveId?.(id);
          }}
        >
          {children}
        </ToolBtn>
      );
    }
    return (
      <Popup
        open={visible}
        title={<Typography variant="h6">{t(Strings.ai_feedback_box_title)}</Typography>}
        onClose={() => {
          setFeedbackActiveId?.(null);
        }}
      >
        <div className={styles.feedbackBoxBody}>
          <Typography style={{ marginBottom: 16 }} variant="body3" color={colors.textCommonTertiary}>
            {t(Strings.ai_feedback_box_alert)}
          </Typography>
          <RcTextArea
            maxLength={255}
            onChange={(e) => {
              setValue(e.target.value);
            }}
            placeholder={t(Strings.ai_feedback_box_placeholder)}
            className={styles.feedbackBoxBodyTextArea}
            value={value}
          />
        </div>
        <div className={styles.feedbackBoxFooter}>
          <Button color="primary" onClick={onSend} loading={loading} style={{ width: 148 }}>
            {t(Strings.ai_feedback_box_send)}
          </Button>
        </div>
      </Popup>
    );
  };

  return (
    <Popover
      open={visible}
      placement="bottom"
      overlayClassName={styles.feedbackBoxPopover}
      content={
        <div className={styles.feedbackBox}>
          <div className={styles.feedbackBoxTitle}>
            <Typography variant="h6">{t(Strings.ai_feedback_box_title)}</Typography>
            <IconButton
              color={colors.textCommonPrimary}
              icon={CloseOutlined}
              onClick={() => {
                setFeedbackActiveId?.(null);
              }}
            />
          </div>
          <div className={styles.feedbackBoxBody}>
            <Typography style={{ marginBottom: 16 }} variant="body3" color={colors.textCommonTertiary}>
              {t(Strings.ai_feedback_box_alert)}
            </Typography>
            <RcTextArea
              maxLength={255}
              onChange={(e) => {
                setValue(e.target.value);
              }}
              placeholder={t(Strings.ai_feedback_box_placeholder)}
              className={styles.feedbackBoxBodyTextArea}
              value={value}
            />
          </div>
          <div className={styles.feedbackBoxFooter}>
            <Button color="primary" onClick={onSend} loading={loading} style={{ width: 148 }}>
              {t(Strings.ai_feedback_box_send)}
            </Button>
          </div>
        </div>
      }
    >
      <ToolBtn
        active={visible}
        className={classNames({
          [styles.like]: status === Shared.AIFeedbackType.Like,
          [styles.dislike]: status === Shared.AIFeedbackType.DisLike,
        })}
        onClick={() => {
          setFeedbackActiveId?.(id);
        }}
      >
        {children}
      </ToolBtn>
    </Popover>
  );
}
