import { BubbleMenu as TiptapBubbleMenu, isNodeSelection } from '@tiptap/react';
import React, { useState } from 'react';
import { Divider } from '@apitable/components';
import { AlignSelect } from './align-select';
import { ColorSelect } from './color-select';
import { IBubbleMenu } from './interface';
import { LinkSelect } from './link-select';
import { MenuBtn } from './menu-btn';
import { NodeSelect } from './node-select';
import styles from './styles.module.less';

export const BubbleMenu = (props: IBubbleMenu) => {
  const { editor } = props;
  const [isLinkOpen, setIsLinkOpen] = useState(false);
  const [isColorOpen, setIsColorOpen] = useState(false);

  const hideAlign = editor.isActive('taskList') ||
    editor.isActive('bulletList') ||
    editor.isActive('orderedList') ||
    editor.isActive('blockquote');

  const hideLink = editor.isActive('code');

  const onlyNode = editor.isActive('codeBlock');

  const closeItemList = () => {
    setIsLinkOpen(false);
    setIsColorOpen(false); 
  };

  return (
    <TiptapBubbleMenu
      editor={editor}
      tippyOptions={{
        moveTransition: 'transform 0.15s ease-out',
        onHidden: () => {
          setIsLinkOpen(false);
          setIsColorOpen(false);
        },
      }}
      className={styles.bubbleMenu}
      shouldShow={({ view, state, editor }) => {
        const { selection } = state;
        const { empty } = selection;
        const hasEditorFocus = view.hasFocus();

        // don't show if image、title or codeBlock is selected
        if (
          editor.isActive('title') ||
          editor.isActive('image') ||
          !hasEditorFocus ||
          empty ||
          isNodeSelection(selection)
        ) {
          return false;
        }
        return true;
      }}
    >
      <NodeSelect editor={editor} triggerClass={styles.triggerSelect} setClose={closeItemList} />
      {!onlyNode && <Divider orientation="vertical" className={styles.divider}/>}
      {(!hideAlign && !onlyNode) && (
        <>
          <AlignSelect editor={editor} triggerClass={styles.triggerSelect} setClose={closeItemList} />
          <Divider orientation='vertical' className={styles.divider}/>
        </>
      )}
      {!onlyNode && (
        <>
          <MenuBtn editor={editor} />
          <Divider orientation='vertical' className={styles.divider}/>
        </>
      )}
      {(!hideLink && !onlyNode) && (
        <>
          <LinkSelect open={isLinkOpen} setOpen={setIsLinkOpen} editor={editor} clear={() => setIsColorOpen(false)}/>
          <Divider orientation='vertical' className={styles.divider}/>
        </>
      )}
      {!onlyNode && <ColorSelect open={isColorOpen} setOpen={setIsColorOpen} editor={editor} clear={() => setIsLinkOpen(false)}/>}
    </TiptapBubbleMenu>
  );
};
