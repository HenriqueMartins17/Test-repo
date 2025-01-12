import { Editor } from '@tiptap/react';
import { last } from 'lodash';
import React from 'react';
// eslint-disable-next-line no-restricted-imports
import { Select } from '@apitable/components';
import { ELEMENT_ICONS, ElementType, INLINE_BASIC_ELEMENT } from '../../constant';
import { strings } from '../../strings';
import { list2Text, name2Command } from '../utils';
import styles from './styles.module.less';

interface INodeSelect {
  editor: Editor;
  triggerClass:string;
  setClose: () => void;
}

export const NodeSelect = (props: INodeSelect) => {
  const { editor, triggerClass, setClose } = props;
  const activeFns = INLINE_BASIC_ELEMENT.map(element => {
    const { name, args, activeArgs } = name2Command(element);
    return {
      element,
      isActive: () => editor.isActive(name, ...(activeArgs || args))
    };
  });

  const activeList = activeFns.filter(fn => fn.isActive());
  const activeItem = last(activeList)?.element || ElementType.PARAGRAPH;
  const listActiveFnName = list2Text(editor);

  return (
    <div className={styles.con} onClick={setClose}>
      <Select
        triggerCls={triggerClass}
        listCls={styles.listCls}
        dropdownMatchSelectWidth={false}
        value={activeItem}
        onSelected={(option) => {
          const value = option.value as ElementType;
          let { fnName, args } = name2Command(value);
          // When list active, select paragraph should toggle list
          if (listActiveFnName) {
            if (fnName === 'toggleNode') {
              fnName = listActiveFnName;
              args = [];
            }
            if (value === ElementType.QUOTE) {
              editor.chain().focus()[listActiveFnName]().run();
            }
          }
          // console.log('onSelected', value, fnName, args);
          editor.chain().focus()[fnName](...args).run();
        }}
        renderValue={(option) => {
          const Icon = ELEMENT_ICONS[option.value];
          return <Icon/> as any;
        }}
        options={INLINE_BASIC_ELEMENT.map(be => {
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
