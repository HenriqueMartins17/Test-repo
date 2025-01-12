/* eslint-disable require-await */
import { Scope } from '@sentry/browser';
import * as Sentry from '@sentry/nextjs';
import { EditorContent, useEditor } from '@tiptap/react';
import { useDebounceFn } from 'ahooks';
import classNames from 'classnames';
import ErrorPage from 'error_page';
import isHotkey from 'is-hotkey';
import { get } from 'lodash';
import React, { useRef, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { IDPrefix, IReduxState, Strings, t } from '@apitable/core';
import { getAnonymousId } from 'pc/utils/get_anonymous_Id';
import { WorkdocDownload } from '../../workdoc/workdoc-download';
import { BubbleMenu } from '../bubble-menu';
import { defaultExtensions } from '../extensions';
import { FloatingMenu } from '../floating-menu';
import { TableOfContents } from '../table-of-contents';
import { uploadImage } from '../utils/upload-image';
import { uploadVideo } from '../utils/upload-video';
import { IEditorCore } from './interface';
import { WorkdocInfo } from 'enterprise/editor/workdoc/workdoc-info';
import styles from './styles.module.less';

const PREVENT_KEYS = ['mod+b', 'mod+i'];
const DRAG_THRESHOLD = 10;

export const EditorCore = (props: IEditorCore) => {
  const isBlur = useRef(false);
  const ref = useRef<HTMLDivElement>(null);
  const mouseDownRef = useRef<{ x: number; y: number }>();
  const isDragRef = useRef(false);
  const userInfo = useSelector((state: IReduxState) => state.user.info);

  const { extensions = [], documentId, style, className, setTitle, editable, expandable = true, infoOpen } = props;

  const { run: updateTitle } = useDebounceFn(
    (_editor: any) => {
      const content = _editor?.getJSON() || '';
      const title = get(content, 'content.0.content.0.text') || '';
      setTitle?.(title);
    },
    { wait: 600 }
  );

  const editor = useEditor({
    extensions: [...defaultExtensions, ...extensions],
    onUpdate: (e) => {
      updateTitle(e.editor);
    },
    editable
  });

  useEffect(() => {
    if (editor && userInfo == null) {
      const users = editor?.storage.collaborationCursor?.users || [];
      const currentAnonymousId = getAnonymousId(IDPrefix.WorkDocAonymousId);
      // broadcast anonymous user
      if (!users.some((user) => user.unitId === currentAnonymousId)) {
        editor
          .chain()
          .focus()
          .updateUser({
            name: t(Strings.alien),
            unitId: currentAnonymousId,
            color: '',
          })
          .run();
      }
    }
  }, [editor, userInfo]);

  // const copyImageToClipboard = async (imgElement) => {
  //   try {
  //     const canvas = document.createElement('canvas');
  //     const ctx = canvas.getContext('2d');
  //     const img = new Image();
  //     img.crossOrigin = 'anonymous';
  //     img.src = imgElement.src;
  //
  //     img.onload = async () => {
  //       canvas.width = img.width;
  //       canvas.height = img.height;
  //       ctx?.drawImage(img, 0, 0);
  //       canvas.toBlob(async (blob) => {
  //         if (blob) {
  //           await navigator.clipboard.write([new ClipboardItem({ 'image/png': blob })]);
  //           console.log('Image copied to clipboard');
  //         }
  //       }, 'image/png');
  //     };
  //   } catch (error) {
  //     console.error('Failed to copy image: ', error);
  //   }
  // };

  // const handleCopy = (e) => {
  //   const selection = window.getSelection();
  //
  //   if (!selection) return;
  //   if (!selection.rangeCount) return;
  //
  //   const range = selection.getRangeAt(0);
  //   const content = range.cloneContents();
  //
  //   if (content.querySelector('img')) {
  //     e.preventDefault();
  //     copyImageToClipboard(content.querySelector('img'));
  //   }
  // };
  //
  // useEffect(() => {
  //   const editorElement = ref.current;
  //   if (editorElement) {
  //     editorElement.addEventListener('copy', handleCopy);
  //   }
  //
  //   return () => {
  //     if (editorElement) {
  //       editorElement.removeEventListener('copy', handleCopy);
  //     }
  //   };
  // }, []);

  const handlePaste = (e: React.ClipboardEvent) => {
    if (!editor || !editable) return;

    const { items } = e.clipboardData;
    for (const item of items) {
      const file = item.getAsFile();
      if (file instanceof File) {
        if (file.type.startsWith('image')) {
          uploadImage(editor, props.documentId, file);
          e.preventDefault();
        } else if (file.type.startsWith('video')) {
          uploadVideo(editor, props.documentId, file);
          e.preventDefault();
        }
      }
    }
  };

  const editorStyle = {
    ...style, // existing styles
    paddingRight: infoOpen ? '300px' : '0',
  };

  return (
    <Sentry.ErrorBoundary fallback={ErrorPage} beforeCapture={beforeCapture as any}>
      <div
        ref={ref}
        tabIndex={0}
        className={classNames(styles.editor, className)}
        style={editorStyle}
        onKeyDown={(e) => {
          const isPrevent = PREVENT_KEYS.some((key) => isHotkey(key, e));
          if (isPrevent) {
            e.preventDefault();
            e.stopPropagation();
          }
          if (e.key === 'Tab') {
            e.preventDefault();
            e.stopPropagation();
            const isListSection = editor?.isActive('taskList') || editor?.isActive('bulletList') || editor?.isActive('orderedList');
            !isListSection && editor?.chain().insertContent('    ').run();
          }
          if (e.key === 'Enter') {
            editor?.chain().focus().unsetTextAlign().run();
          }
        }}
        onBlur={() => {
          if (isBlur.current) {
            isBlur.current = false;
            editor?.commands.focus();
          }
        }}
        onMouseDown={(e) => {
          mouseDownRef.current = { x: e.clientX, y: e.clientY };
        }}
        onMouseMove={(e) => {
          const { x, y } = mouseDownRef.current || {};
          if (x && y && (Math.abs(e.clientX - x) > DRAG_THRESHOLD || Math.abs(e.clientY - y) > DRAG_THRESHOLD)) {
            isDragRef.current = true;
          }
        }}
        onMouseUp={(e) => {
          mouseDownRef.current = undefined;
          if (isDragRef.current) {
            isDragRef.current = false;
            return;
          }
          if (e.target === ref.current) editor?.commands.focus('end');
        }}
        onPaste={handlePaste}
      >
        <EditorContent editor={editor} />
        {editor && <BubbleMenu editor={editor} />}
        {editor && <FloatingMenu editor={editor} documentId={documentId} />}
      </div>
      {expandable && editor && <TableOfContents editor={editor} />}
      {editor && infoOpen && <WorkdocInfo documentId={documentId} />}
      <WorkdocDownload editor={editor} documentId={documentId} />
    </Sentry.ErrorBoundary>
  );
};

const beforeCapture = (scope: Scope) => {
  scope.setTag('PageCrash', true);
};
