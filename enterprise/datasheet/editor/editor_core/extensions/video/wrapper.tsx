import { NodeViewWrapper } from '@tiptap/react';
import classNames from 'classnames';
import React from 'react';
import { DownloadFilled } from '@apitable/icons';

export const Wrapper = (props: any) => {
  return (
    <NodeViewWrapper
      className={classNames('video-wrapper', {
        'video-selected': props.editor.isEditable ? props.selected : false,
      })}
    >
      <video {...props.node.attrs} controls controlsList="nodownload" width="100%">
        <source src={props.node.attrs.src} />
      </video>
      <div className="video-actions">
        <a
          href={`${props.node.attrs.src}?attname=${props.node.attrs.title}`}
          download={props.node.attrs.title}
          target="_blank"
          rel="noopener noreferrer"
        >
          <DownloadFilled size={16} />
        </a>
      </div>
    </NodeViewWrapper>
  );
};