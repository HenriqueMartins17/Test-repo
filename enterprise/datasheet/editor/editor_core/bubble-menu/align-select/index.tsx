import { Editor } from '@tiptap/react';
import React from 'react';
// eslint-disable-next-line no-restricted-imports
import { Select } from '@apitable/components';
import { ELEMENT_ICONS, ElementType, ALIGN_ELEMENT } from '../../constant';
import { strings } from '../../strings';
import { name2Command } from '../utils';
import styles from './styles.module.less';

interface IAlignSelect {
  editor: Editor;
  triggerClass: string;
  setClose: ()=> void;
}

export const AlignSelect = (props: IAlignSelect) => {
  const { editor, triggerClass, setClose } = props;

  const activeFns = ALIGN_ELEMENT.map(element => {
    const { args, activeArgs } = name2Command(element);
    return {
      element,
      // should check paragraph and heading
      // @ts-ignore
      isActive: () => editor.isActive(...(activeArgs || args))
    };
  });

  const activeItem = activeFns.find(fn => fn.isActive())?.element || ElementType.LEFT;
  return (
    <div className={styles.con} onClick={setClose}>
      <Select
        triggerCls={triggerClass}
        listCls={styles.listCls}
        dropdownMatchSelectWidth={false}
        value={activeItem}
        onSelected={(option) => {
          const value = option.value as ElementType;
          const { fnName, args } = name2Command(value);
          editor.chain().focus()[fnName](...args).run();
        }}
        renderValue={(option) => {
          const Icon = ELEMENT_ICONS[option.value];
          return <Icon/> as any;
        }}
        options={ALIGN_ELEMENT.map(be => {
          const Icon = ELEMENT_ICONS[be];
          return {
            label: (
            <div className={styles.label}>
              <Icon/>
              {strings[be]}
            </div>
          ) as any,
            value: be,
          };
        })}
      />
    </div>
  );
};
