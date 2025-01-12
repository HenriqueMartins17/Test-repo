import { Color } from '@tiptap/extension-color';
import Document from '@tiptap/extension-document';
import Highlight from '@tiptap/extension-highlight';
import Link from '@tiptap/extension-link';
import Placeholder from '@tiptap/extension-placeholder';
import TaskItem from '@tiptap/extension-task-item';
import TaskList from '@tiptap/extension-task-list';
import TextAlign from '@tiptap/extension-text-align';
import TextStyle from '@tiptap/extension-text-style';
import Underline from '@tiptap/extension-underline';
import { StarterKit } from '@tiptap/starter-kit';
import { Strings, t } from '@apitable/core';
import { strings } from '../strings';
import { HeadingId } from './heading-id';
import { ImageResize } from './image-resizer';
import { UploadImagePlaceholderPlugin } from './image-resizer/upload-image-placeholder';
import { Title } from './title';
import { Video } from './video';

export const defaultExtensions = [
  StarterKit.configure({
    history: false,
    document: false,
    dropcursor: {
      color: 'var(--primaryColor)',
      width: 2,
      class: 'drop-cursor',
    },
  }),
  Document.extend({
    content: 'title block*',
  }),
  Title,
  Underline,
  TaskList,
  TaskItem.configure({
    nested: true,
  }),
  Link,
  TextStyle,
  TextAlign.configure({
    types: ['heading', 'paragraph', 'image'],
    defaultAlignment: 'left',
  }),
  Color,
  Video,
  ImageResize.extend({
    addProseMirrorPlugins() {
      return [UploadImagePlaceholderPlugin()];
    },
  }).configure({
    allowBase64: true,
  }),
  Highlight.configure({
    multicolor: true,
  }),
  HeadingId,
  Placeholder.configure({
    placeholder: ({ node, editor }) => {
      if (node.type.name === 'title') {
        return t(Strings.workdoc_title_placeholder);
      } else if (node.type.name === 'heading') {
        return strings[`heading${node.attrs.level}`];
      } else if (editor.isActive('codeBlock') || node.type.name === 'codeBlock') {
        return t(Strings.workdoc_code_placeholder);
      }

      return t(Strings.workdoc_text_placeholder);
    },
    includeChildren: false,
  }),
];
