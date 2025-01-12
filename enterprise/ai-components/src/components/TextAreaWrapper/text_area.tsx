import { Col, Row } from 'antd';
import classNames from 'classnames';
import RcTextArea from 'rc-textarea';
import React, { ChangeEvent, useRef, useState } from 'react';
import { Button, Tooltip } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import styles from './style.module.less';
import { ScreenSize } from '@/shared';
import { useResponsive } from '@/shared/hook/use_responsive';

interface ITextAreaProps {
  sendMessage: (content: string) => boolean;
  canSend?: () => boolean;
  disabled?: boolean;
  type?: 'copilot' | 'chat';
  className?: string;
  tooltip?: string;
}

export function TextArea(props: ITextAreaProps) {
  const { type = 'chat', className } = props;
  const [focus, setFocus] = useState(false);
  const textarea = useRef<RcTextArea | null>(null);
  const [lastContent, setLastContent] = useState<string>('');
  const [input, setInput] = useState<string>('');
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.md);
  const onInput = (e: ChangeEvent<HTMLTextAreaElement>) => {
    setInput(e.target.value);
  };

  const send = () => {
    if (props.disabled) {
      return;
    }
    if (props.canSend && !props.canSend()) {
      return;
    }

    if (props.sendMessage(input)) {
      setInput('');
      setLastContent(input);
    }
  };

  const onSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    send();
  };

  const onEnterEvent = (e: React.KeyboardEvent) => {
    if (!input && e.code === 'ArrowUp') {
      e.preventDefault();
      setInput(lastContent);
    }
    // 中文输入法有个其他的事件监听 不是input 先用 keyCode
    if (e.keyCode === 13 && !e.shiftKey && input.trim()) {
      e.preventDefault();
      send();
    }
  };

  const onMouseUp = () => {
    setFocus(true);
    textarea.current?.focus();
  };

  return (
    <form
      onSubmit={onSubmit}
      className={classNames(styles.chatContainer, className, {
        [styles.focus]: focus,
      })}
      onMouseUp={onMouseUp}
    >
      <Row align={'middle'}>
        <Col flex={1}>
          <RcTextArea
            ref={textarea}
            rows={1}
            autoSize={{ maxRows: 4 }}
            className={classNames(styles.textArea, { [styles.copilot]: type === 'copilot' })}
            value={input}
            onInput={onInput}
            placeholder={t(Strings.ai_chat_textarea_placeholder)}
            onKeyDown={onEnterEvent}
            onBlur={() => {
              setFocus(false);
            }}
          />
        </Col>
        {type === 'copilot' && (
          <Col style={{ marginLeft: '16px' }}>
            <Button htmlType="submit" type="submit" color="primary" size={'small'} disabled={props.disabled || !input.length}>
              {t(Strings.send)}
            </Button>
          </Col>
        )}
      </Row>
      {type === 'chat' && (
        <Row align={'bottom'}>
          <Col flex={1}>{/*<span>听筒</span>*/}</Col>
          <Col style={{ marginLeft: '16px' }}>
            {
              props.tooltip ? (
                <Tooltip content={props.tooltip}>
                  <div>
                    <Button htmlType="submit" type="submit" color="primary" size={isMobile ? 'small' : 'middle'} disabled={props.disabled || !input.length}>
                      {t(Strings.send)}
                    </Button>
                  </div>
                </Tooltip>
              ) : (
                <Button htmlType="submit" type="submit" color="primary" size={isMobile ? 'small' : 'middle'} disabled={props.disabled || !input.length}>
                  {t(Strings.send)}
                </Button>
              )
            }
          </Col>
        </Row>
      )}

    </form>
  );
}
