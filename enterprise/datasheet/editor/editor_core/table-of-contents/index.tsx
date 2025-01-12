import { Editor } from '@tiptap/react';
import { useLocalStorageState } from 'ahooks';
import { Tooltip } from 'antd';
import classNames from 'classnames';
import { omit } from 'lodash';
import React, { useCallback, useEffect, useState } from 'react';
import { IconButton } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import { CollapseOpenOutlined, CollapseOutlined } from '@apitable/icons';
import { IHeadings } from './interface';
import styles from './styles.module.less';

export const TableOfContents = ({ editor }: { editor: Editor }) => {
  const [open, setOpen] = useLocalStorageState<boolean>('workdoc_table_of_contents', {
    defaultValue: true,
  });
  const [active, setActive] = useState<number>();
  const [items, setItems] = useState<IHeadings[]>([]);

  const addFlash = useCallback((id: string) => {
    const transaction = editor.state.tr;
    editor.state.doc.descendants((node, pos) => {
      if (node.type.name === 'heading' && node.textContent.trim() !== '') {
        if (node.attrs.id === id) {
          transaction.setNodeMarkup(pos, undefined, {
            ...node.attrs,
            class: 'heading-flash'
          });
        }
      }
    });
    editor.view.dispatch(transaction);
  }, [editor]);

  const clearFlash = useCallback((id?: string) => {
    const transaction = editor.state.tr;
    editor.state.doc.descendants((node, pos) => {
      if (node.type.name === 'heading' && node.textContent.trim() !== '') {
        console.log(id, node.attrs.class === 'heading-flash');
        if (node.attrs.id === id || (!id && node.attrs.class === 'heading-flash')) {
          transaction.setNodeMarkup(pos, undefined, omit(node.attrs, ['class']));
        }
      }
    });
    editor.view.dispatch(transaction);
  }, [editor]);

  const handleUpdate = useCallback(() => {
    const transaction = editor.state.tr;
    const headings: IHeadings[] = [];
    editor.state.doc.descendants((node, pos) => {
      if (node.type.name === 'heading' && node.textContent.trim() !== '') {
        const id = `heading-${headings.length + 1}`;
        if (node.attrs.id !== id) {
          transaction.setNodeMarkup(pos, undefined, {
            ...node.attrs,
            id,
          });
        }
        headings.push({
          level: node.attrs.level,
          text: node.textContent,
          id,
        });
      }
    });
    transaction.setMeta('addToHistory', false);
    transaction.setMeta('preventUpdate', true);
    editor.view.dispatch(transaction);
    setItems(headings);
  }, [editor]);

  const handleHighLight = useCallback((id: string) => {
    addFlash(id);
    setTimeout(() => {
      clearFlash(id);
    }, 1000);
  }, [addFlash, clearFlash]);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(handleUpdate, []);

  useEffect(() => {
    if (!editor) {
      return;
    }
    editor.on('update', handleUpdate);
    return () => {
      editor.off('update', handleUpdate);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [editor]);

  useEffect(() => {
    if (!editor) {
      return;
    }

    const handleScroll = () => {
      const index = items.findIndex(({ id }) => {
        const element = document.getElementById(id);
        if (!element) {
          return false;
        }
        const { top } = element.getBoundingClientRect();
        return top > 20 && top < 200;
      });
      if (index >= 0) setActive(index);
    };
    const targetDom = document.querySelector('#workdocEditorContent');
    targetDom?.addEventListener('scroll', handleScroll);
    return () => {
      targetDom?.removeEventListener('scroll', handleScroll);
    };
  }, [editor, items]);

  useEffect(() => {
    if (!editor || active === undefined) {
      return;
    }
    const activeDom = document.querySelector(`a[href^='#heading-${active + 1}']`);
    activeDom?.scrollIntoView({ block: 'nearest' });
  }, [active, editor]);

  return (
    <div className={classNames(styles.toc, { [styles.open]: open })}>
      <div className={styles.tocBtn}>
        <Tooltip title={open ? t(Strings.workdoc_collapsed) : t(Strings.workdoc_expanded)}>
          <IconButton
            onClick={() => setOpen(!open)}
            icon={open ? CollapseOutlined : CollapseOpenOutlined}
            shape="square"
          />
        </Tooltip>
      </div>
      {open && <ul className={styles.tocList}>
        {items.map((item, index) => (
          <li
            key={index}
            className={classNames(styles.tocItem, styles[`tocItem${item.level}`], { [styles.active]: index === active })}
          >
            <a title={item.text} onClick={() => {
              setActive(index);
              handleHighLight(`heading-${index + 1}`);
            }} href={`#heading-${index + 1}`}>{item.text}</a>
          </li>
        ))}
      </ul>}
    </div>
  );
};
