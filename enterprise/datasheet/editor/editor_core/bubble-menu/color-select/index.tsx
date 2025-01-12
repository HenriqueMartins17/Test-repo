import { Editor } from '@tiptap/react';
import { Tooltip } from 'antd';
import classNames from 'classnames';
import RcTrigger from 'rc-trigger';
import * as React from 'react';
import { useRef } from 'react';
import { Button } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { ChevronDownOutlined } from '@apitable/icons';
import { ELEMENT_ICONS, ElementType, HIGHLIGHT_COLORS, HIGHLIGHT_BACKGROUNDS } from '../../constant';
import { strings } from '../../strings';
import { clearCode } from '../utils';
import styles from './styles.module.less';

interface ILinkSelect {
  open: boolean;
  setOpen: (bool: boolean) => void;
  editor: Editor;
  clear: () => void;
}

export const ColorSelect = (props: ILinkSelect) => {
  const { open, setOpen, editor, clear } = props;
  const activeColorItem = HIGHLIGHT_COLORS.find((color) =>
    editor.isActive('textStyle', { color })
  );

  const activeBackgroundItem = HIGHLIGHT_BACKGROUNDS.find((bg ) =>
    editor.isActive('highlight', { color: bg })
  );

  const ref = useRef<HTMLDivElement>(null);

  const Icon = ELEMENT_ICONS[ElementType.HIGHLIGHT];
  return (
    <div ref={ref}>
      <RcTrigger
        getPopupContainer={() => ref.current!}
        popup={(
          <div className={styles.colorSelect}>
            <div className={styles.title}>{t(Strings.workdoc_color_title)}</div>
            <div className={styles.content}>
              {HIGHLIGHT_COLORS.map(color => {
                return (
                  <button
                    style={{ color, border: activeColorItem === color ?`1px solid ${color}` : undefined }}
                    key={color}
                    className={activeColorItem === color ? styles.colorActive : ''}
                    onClick={() => {
                      clearCode(editor);
                      editor.chain().focus().setColor(color).run();
                    }}
                  >
                  A
                  </button>
                );
              })}
            </div>
            <div className={styles.title}>{t(Strings.workdoc_background_title)}</div>
            <div className={styles.content}>
              {HIGHLIGHT_BACKGROUNDS.map(bg => {
                return (
                  <button
                    style={{ backgroundColor: bg, color: bg }}
                    key={bg}
                    className={activeBackgroundItem === bg ? styles.bgActive : ''}
                    onClick={() => {
                      clearCode(editor);
                      editor.commands.unsetHighlight();
                      editor.commands.setHighlight({ color: bg });
                      editor.chain().focus().run();
                    }}
                  />
                );
              })}
            </div>
            <Button
              block
              size="small"
              className={styles.reset}
              disabled={!activeColorItem && !activeBackgroundItem}
              onClick={() => {
                editor.commands.unsetColor();
                editor.commands.unsetHighlight();
              }}
            >
              {t(Strings.workdoc_color_default)}
            </Button>
          </div>
        )}
        popupAlign={{
          points: ['tl', 'br'],
          overflow: { adjustX: true, adjustY: true },
          offset: [-44, 9],
        }}
        action={'click'}
        popupVisible={open}
        onPopupVisibleChange={() => {
          if (!open) {
            clear();
          }
          setOpen(!open);
        }}
      >
        <Tooltip title={strings.highlight}>
          <span className={classNames(styles.colorIcon, {
            [styles.active]: editor.isActive('highlight')||editor.isActive('textStyle')
          })}>
            <Icon />
            <ChevronDownOutlined className={classNames(styles.arraw, open && styles.active)} />
          </span>
        </Tooltip>
      </RcTrigger>
    </div>
  );
};
