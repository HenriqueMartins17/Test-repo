import { Editor } from '@tiptap/react';
import { Tooltip } from 'antd';
import classNames from 'classnames';
import React from 'react';
import { IconButton } from '@apitable/components';
import { ELEMENT_ICONS, ElementType, MENU_ELEMENTS } from '../constant';
import { strings } from '../strings';
import { clearCode, name2Command } from './utils';
import styles from './styles.module.less';

interface IMenuBtn {
  editor: Editor;
}

export const MenuBtn = (props: IMenuBtn) => {
  const { editor } = props;
  const hideBold = editor.isActive('heading');
  return (
    <div className={styles.buttonGroup}>
      {MENU_ELEMENTS.filter(element => {
        return !(hideBold && element === ElementType.BOLD);
      }).map(element => {
        const isActive = editor.isActive(element);
        const { fnName } = name2Command(element);
        const Icon = ELEMENT_ICONS[element];
        return (
          <Tooltip key={element} title={strings[element]}>
            <IconButton
              icon={Icon}
              shape="square"
              component="button"
              className={classNames(styles.menuItem, isActive && styles.menuActive)}
              onClick={() => {
                clearCode(editor, element);
                if (editor.isActive('heading') && element === ElementType.BOLD) return;
                editor.chain().focus()[fnName]().run();
              }}
            />
          </Tooltip>
        );
      })}
    </div>
  );
};
