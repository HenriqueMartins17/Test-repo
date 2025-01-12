import { EditorView } from '@tiptap/pm/view';
import { Editor } from '@tiptap/react';
import { getImageThumbSrc, Strings, t } from '@apitable/core';
import { uploadAttachToS3, UploadType } from '@apitable/widget-sdk';
import { Message } from 'pc/components/common';
import { joinPath } from 'pc/components/route_manager/helper';
import { execNoTraceVerification } from 'pc/utils';
import { getEnvVariables } from 'pc/utils/env';
import { uploadKey } from '../extensions/image-resizer/upload-image-placeholder';

export const uploadImage = (editor: Editor, documentId: string, file?: File) => {
  if (file) {
    // If a file is provided, upload it directly
    const pos = editor.view.state.selection.from;
    startImageUpload(file, editor.view, pos, documentId);
  } else {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = async () => {
      if (input.files?.length) {
        const file = input.files[0];
        const pos = editor.view.state.selection.from;
        await startImageUpload(file, editor.view, pos, documentId);
        // add a new paragraph after the image and focus on it
        editor.commands.insertContentAt(pos, '<p></p>');
        editor.chain().focus().run();
      }
    };
    input.click();
  }
};

// max size 100M
const MAX_SIZE = 1024 * 1024 * 100;

const startImageUpload = async (file: File, view: EditorView, pos: number, documentId: string) => {
  if (!file.type.includes('image/')) {
    Message.warning({ content: t(Strings.workdoc_only_image) });
    return;
  } else if (file.size > MAX_SIZE) {
    Message.warning({ content: t(Strings.workdoc_image_max_size, { size: '100MB' }) });
    return;
  }

  // A fresh object to act as the ID for this upload
  const id = {};

  // Replace the selection with a placeholder
  const tr = view.state.tr;
  if (!tr.selection.empty) tr.deleteSelection();
  const reader = new FileReader();
  reader.readAsDataURL(file);
  reader.onload = () => {
    tr.deleteSelection().setMeta(uploadKey, {
      add: {
        id,
        pos,
        src: reader.result,
      },
    });
    view.dispatch(tr);
  };

  const handleUploadFaild = () => {
    // delete placeholder
    const transaction = tr.setMeta(uploadKey, { remove: { id } });
    view.dispatch(transaction);
    Message.error({ content: t(Strings.workdoc_upload_failed) });
    return;
  };

  try {
    const upload = (): Promise<any> => {
      return new Promise((resolve) => {
        const request = async (nvcVal?: string) => {
          const res = await uploadAttachToS3({
            file: file,
            fileType: UploadType.Document,
            data: nvcVal,
            nodeId: documentId,
          });
          resolve(res);
        };
        window['nvc'] ? execNoTraceVerification(request) : request();
      });
    };
    const rlt = await upload();
    if (!rlt.data) {
      handleUploadFaild();
    }
    const {
      data: { data: imgData, success, message },
    } = rlt;
    if (!success) {
      console.log(message);
      Message.error({ content: message });
      return;
    }
    const { bucket, token } = imgData;
    const host = getEnvVariables()[bucket];
    const isSvgOrGif = /(svg|gif)/i.test(file.type);
    const imgUrl = getImageThumbSrc(joinPath([host, token]), isSvgOrGif ? undefined : { format: 'jpg', quality: 100 });
    const { schema } = view.state;
    const node = schema.nodes.image.create({ src: imgUrl, alt: file.name });
    const transaction = view.state.tr
      .deleteSelection()
      .replaceWith(pos - 1, pos, node)
      .setMeta(uploadKey, { remove: { id } });
    view.dispatch(transaction);
  } catch (e) {
    handleUploadFaild();
  }
};
