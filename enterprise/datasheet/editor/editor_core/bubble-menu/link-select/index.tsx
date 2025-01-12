import { Editor } from '@tiptap/react';
import { Tooltip } from 'antd';
import classNames from 'classnames';
import RcTrigger from 'rc-trigger';
import * as React from 'react';
import { useEffect, useRef, useState } from 'react';
import { Button, IconButton, TextInput } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import { DeleteOutlined } from '@apitable/icons';
import { ELEMENT_ICONS, ElementType } from '../../constant';
import { strings } from '../../strings';
import styles from './styles.module.less';

interface ILinkSelect {
  open: boolean;
  setOpen: (bool: boolean) => void;
  editor: Editor;
  clear: () => void;
}

export const LinkSelect = (props: ILinkSelect) => {
  const { open, setOpen, editor, clear } = props;

  const link = editor.getAttributes('link').href;

  const contentRef = useRef(null);
  const triggerRef = useRef(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const [inputLink, setInputLink] = useState<string>('');

  useEffect(() => {
    if(!open) {
      setInputLink('');
    } else {
      setInputLink(link || '');
    }
  }, [open, link]);

  const ref = useRef<HTMLDivElement>(null);

  const Icon = ELEMENT_ICONS[ElementType.LINK];
  return (
    <div ref={ref}>
      <RcTrigger
        getPopupContainer={() => ref.current!}
        popup={(
          <div className={styles.linkInput} ref={contentRef}>
            <TextInput
              size="small"
              placeholder={t(Strings.workdoc_link_placeholder)}
              ref={inputRef}
              value={inputLink}
              onChange={(e) => {
                setInputLink(e.target.value);
              }}
            />
            <Button
              color="primary" 
              size="small"
              disabled={!inputLink}
              onClick={() => {
                if (inputLink) {
                  let newLink = inputLink;
                  if (!/^https?:\/\//.test(inputLink)) {
                    newLink = `https://${inputLink}`;
                  }
                  editor.chain().focus().setLink({ href: newLink }).run();
                  setInputLink('');
                }
                setOpen(false);
              }}>{t(Strings.confirm)}</Button>
            {link !== undefined && (
              <IconButton
                icon={DeleteOutlined}
                shape="square"
                className={styles.deleteIcon}
                onClick={() => {
                  editor.chain().focus().unsetLink().run();
                  setOpen(false);
                }}
              />
            )}
          </div>
        )}
        popupAlign={{
          points: ['tl', 'br'],
          overflow: { adjustX: true, adjustY: true },
          offset: [-24, 9],
        }}
        action={'click'}
        popupVisible={open}
        onPopupVisibleChange={(visible) => {
          if (visible) {
            clear();
          }
          setOpen(visible);
        }}
      >
        <Tooltip title={strings.link}>
          <span ref={triggerRef} className={classNames(styles.linkIcon, {
            [styles.active]: editor.isActive('link')
          })}>
            <Icon />
          </span>
        </Tooltip>
      </RcTrigger>
    </div>
  );
};
