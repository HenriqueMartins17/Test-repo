import { EditorView } from '@tiptap/pm/view';
import { Editor } from '@tiptap/react';
import { Strings, t } from '@apitable/core';
import { uploadAttachToS3, UploadType } from '@apitable/widget-sdk';
import { Message } from 'pc/components/common';
import { joinPath } from 'pc/components/route_manager/helper';
import { getEnvVariables } from 'pc/utils/env';
import { uploadKey } from '../extensions/video/upload-video-placeholder';

export const uploadVideo = (editor: Editor, documentId: string, file?: File) => {
  if (file) {
    // If a file is provided, upload it directly
    const pos = editor.view.state.selection.from;
    startVideoUpload(file, editor.view, pos, documentId);
  } else {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'video/*';
    input.onchange = async () => {
      if (input.files?.length) {
        const file = input.files[0];
        const pos = editor.view.state.selection.from;
        await startVideoUpload(file, editor.view, pos, documentId);
        // add a new paragraph after the video and focus on it
        editor.commands.insertContentAt(pos, '<p></p>');
        editor.chain().focus().run();
      }
    };
    input.click();
  }
};

// max size 1GB for videos
const MAX_SIZE = 1024 * 1024 * 1024;

const startVideoUpload = async (file: File, view: EditorView, pos: number, documentId: string) => {
  if (!file.type.includes('video/')) {
    Message.warning({ content: t(Strings.workdoc_only_video) });
    return;
  } else if (file.size > MAX_SIZE) {
    Message.warning({ content: t(Strings.workdoc_video_max_size, { size: '1GB' }) });
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

  const handleUploadFailed = () => {
    // delete placeholder
    const transaction = tr.setMeta(uploadKey, { remove: { id } });
    view.dispatch(transaction);
    Message.error({ content: t(Strings.workdoc_upload_failed) });
    return;
  };

  try {
    const rlt = await uploadAttachToS3({
      file,
      nodeId: documentId,
      fileType: UploadType.Document, // Ensure this type supports video
    });

    if (!rlt.data) {
      handleUploadFailed();
    }

    const {
      data: { data: videoData, success, message },
    } = rlt;

    if (!success) {
      Message.error({ content: message });
      return;
    }

    const { bucket, token } = videoData;
    const host = getEnvVariables()[bucket];
    const videoUrl = joinPath([host, token]);
    const { schema } = view.state;

    const node = schema.nodes.video.create({ src: videoUrl, title: file.name, type: file.type });

    const transaction = view.state.tr
      .deleteSelection()
      .replaceWith(pos - 1, pos, node)
      .setMeta(uploadKey, { remove: { id } });
    view.dispatch(transaction);
  } catch (e) {
    handleUploadFailed();
  }
};
