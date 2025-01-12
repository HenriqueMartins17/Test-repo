import { Editor } from '@tiptap/core';
import { Extension, JSONContent } from '@tiptap/react';
import { CSSProperties } from 'react';

export interface IEditorCore {
  defaultValue?: JSONContent | string;
  extensions?: Extension[];
  onUpdate?: (editor?: Editor) => void | Promise<void>;
  documentId: string;
  style?: CSSProperties;
  className?: string;
  setTitle?: React.Dispatch<React.SetStateAction<string>>;
  editable?: boolean;
  expandable?: boolean;
  infoOpen?: boolean;
}

export interface IDownloadDocProps {
  editor: Editor | null;
  documentId: string;
}
