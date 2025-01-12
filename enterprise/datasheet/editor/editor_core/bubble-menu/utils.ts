import { Editor } from '@tiptap/react';
import { ElementType } from '../constant';

export const name2Command = (name: ElementType) => {
  switch (name) {
    case ElementType.PARAGRAPH:
      return {
        name: 'paragraph',
        fnName: 'toggleNode',
        args: ['paragraph', 'paragraph'],
        activeArgs: []
      };
    case ElementType.DIVIDER:
      return {
        name: 'horizontalRule',
        fnName: 'setHorizontalRule',
        args: [],
      };
    case ElementType.HEADING_ONE:
    case ElementType.HEADING_TWO:
    case ElementType.HEADING_THREE:
    case ElementType.HEADING_FOUR:
    case ElementType.HEADING_FIVE:
    case ElementType.HEADING_SIX:
      // split string and number
      const result = name.split(/(\d+)/);
      return {
        name: 'heading',
        fnName: 'toggleHeading',
        args: [{
          level: Number(result[1]),
        }]
      };
    case ElementType.LEFT:
    case ElementType.CENTER:
    case ElementType.RIGHT:
      return {
        name: 'paragraph',
        fnName: 'setTextAlign',
        args: [name],
        activeArgs: [{ textAlign: name }],
      };
    default:
      return {
        name,
        fnName: `toggle${name.charAt(0).toUpperCase()}${name.slice(1)}`,
        args: [],
      };
  }
};

export const list2Text = (editor: Editor) => {
  if(editor.isActive('taskList')) {
    return 'toggleTaskList';
  }
  if(editor.isActive('bulletList')) {
    return 'toggleBulletList';
  }
  if(editor.isActive('orderedList')) {
    return 'toggleOrderedList';
  }
  return '';
};

export const clearCode = (editor: Editor, element?: ElementType, ) => {
  if (element !== ElementType.CODE && editor.isActive(ElementType.CODE)) {
    editor.chain().focus().toggleCode().run();
  }
};
