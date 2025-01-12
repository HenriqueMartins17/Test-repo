import { FloatingMenu as TiptapFloatingMenu } from '@tiptap/react';
import classNames from 'classnames';
import React, { useCallback, useEffect, useState } from 'react';
import { name2Command } from '../bubble-menu/utils';
import { BASIC_ELEMENT, ELEMENT_ICONS, ElementType } from '../constant';
import { strings } from '../strings';
import { uploadImage } from '../utils/upload-image';
import { uploadVideo } from '../utils/upload-video';
import { IFloatingMenu } from './interface';
import styles from './styles.module.less';

export const FloatingMenu = (props: IFloatingMenu) => {
  const { editor, documentId } = props;

  // restIndex is adapt keyborad event
  const handleCommand = useCallback(
    (element: ElementType, restIndex = 0) => {
      const { fnName, args } = name2Command(element);
      const selection = editor.state.selection;

      // delete '/' and selection
      editor.commands.deleteRange({
        from: selection.from - restIndex - 1,
        to: selection.from,
      });

      switch (element) {
        case ElementType.IMAGE:
          uploadImage(editor, documentId);
          break;
        case ElementType.VIDEO:
          uploadVideo(editor, documentId);
          break;
        case ElementType.DIVIDER:
          editor.commands.insertContentAt(selection.from - restIndex, '<p></p>');
          editor
            .chain()
            .insertContentAt(selection.from - restIndex, '<hr />')
            .focus()
            [fnName](...args)
            .run();
          break;
        default:
          editor
            .chain()
            .focus()
            [fnName](...args)
            .run();
          break;
      }
    },
    [editor, documentId]
  );

  const [visible, setVisible] = useState(false);
  const [idx, setIndex] = useState<number>();

  const handleKeyDown = (e: KeyboardEvent) => {
    if (typeof idx === 'number') {
      // Only react to these keys if the menu is visible
      if (visible) {
        if (e.code === 'ArrowUp') {
          e.preventDefault();
          const nextIdx = idx === 0 ? BASIC_ELEMENT.length - 1 : idx - 1;
          setIndex(nextIdx);
          return;
        }
        if (e.code === 'ArrowDown') {
          e.preventDefault();
          const nextIdx = idx === BASIC_ELEMENT.length - 1 ? 0 : idx + 1;
          setIndex(nextIdx);
          return;
        }
      }
      if (e.code === 'Enter') {
        e.preventDefault();
        handleCommand(BASIC_ELEMENT[idx], 0);
        return;
      }
    }
  };

  useEffect(() => {
    document.addEventListener('keydown', handleKeyDown, true);
    return () => {
      document.removeEventListener('keydown', handleKeyDown, true);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [handleCommand, idx, visible]);

  useEffect(() => {
    setIndex(visible ? 0 : undefined);
  }, [visible]);

  return (
    <TiptapFloatingMenu
      tippyOptions={{
        duration: 300,
        showOnCreate: true,
        interactive: true,
        trigger: 'manual',
        placement: 'auto-start',
      }}
      editor={editor}
      // https://github.com/ueberdosis/tiptap/blob/develop/packages/extension-floating-menu/src/floating-menu-plugin.ts#L38
      shouldShow={({ view, state }) => {
        const { selection } = state;
        const { $anchor, empty } = selection;
        const isRootDepth = $anchor.depth === 1;
        const isHeadOffset = $anchor.parentOffset === 0;
        const textContent = $anchor.parent.textContent;

        if (
          editor.isActive('title') ||
          editor.isActive('codeBlock') ||
          !view.hasFocus() ||
          !empty ||
          isHeadOffset ||
          !isRootDepth ||
          (textContent !== '/' && textContent !== '、') ||
          !editor?.isEditable
        ) {
          setVisible(false);
          return false;
        }

        setVisible(true);
        return true;
      }}
      className={styles.floatingMenu}
    >
      <ul
        className={styles.menuGroup}
        onMouseOver={(e) => {
          const id = (e.target as HTMLElement).dataset.id;
          if (id) setIndex(Number(id));
        }}
      >
        {BASIC_ELEMENT.map((element, index) => {
          const Icon = ELEMENT_ICONS[element];
          return (
            <React.Fragment key={element}>
              {element === ElementType.IMAGE && <div className={styles.divider} />}
              <li
                data-id={index}
                className={classNames(styles.menuItem, {
                  [styles.selected]: idx === BASIC_ELEMENT.indexOf(element),
                })}
                onClick={() => {
                  handleCommand(element);
                }}
              >
                <Icon />
                {strings[element]}
              </li>
            </React.Fragment>
          );
        })}
      </ul>
    </TiptapFloatingMenu>
  );
};
