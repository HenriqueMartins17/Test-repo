import { NodeViewWrapper } from '@tiptap/react';
import classNames from 'classnames';
import { omit } from 'lodash';
import React from 'react';
import { TextLeftFilled, TextMiddleFilled, TextRightFilled } from '@apitable/icons';
import { expandWorkdocImage } from 'pc/components/preview_file/workdoc_image/expand_workdoc_image';

enum Direction {
  TopLeft,
  TopRight,
  BottomLeft,
  BottomRight,
}

export const Resizer = (props: any) => {
  const handler = (direction: Direction) => (mouseDownEvent: React.MouseEvent<HTMLImageElement>) => {
    const proseMirrorWidth = document.querySelector('.ProseMirror')?.clientWidth ?? 0;
    const parent = (mouseDownEvent.target as HTMLElement).closest('.image-resizer');
    const image = parent?.querySelector('img.primaryImage') ?? null;
    if (image === null) return;
    const startSize = { x: image.clientWidth, y: image.clientHeight };
    const startPosition = { x: mouseDownEvent.pageX, y: mouseDownEvent.pageY };

    function onMouseMove(mouseMoveEvent: MouseEvent) {
      let width = startSize.x;
      let height = startSize.y;
      const xOffset = startPosition.x - mouseMoveEvent.pageX;
      const yOffset = startPosition.y - mouseMoveEvent.pageY;
      switch (direction) {
        case Direction.TopLeft:
          if (startSize.x + xOffset > 0) {
            width += xOffset;
          }
          if (startSize.y + yOffset > 0) {
            height += yOffset;
          }
          break;
        case Direction.TopRight:
          if (startSize.x - xOffset > 0) {
            width -= xOffset;
          }
          if (startSize.y + yOffset > 0) {
            height += yOffset;
          }
          break;
        case Direction.BottomLeft:
          if (startSize.x + xOffset > 0) {
            width += xOffset;
          }
          if (startSize.y - yOffset > 0) {
            height -= yOffset;
          }
          break;
        case Direction.BottomRight:
          if (startSize.x - xOffset > 0) {
            width -= xOffset;
          }
          if (startSize.y - yOffset > 0) {
            height -= yOffset;
          }
          break;
      }
      width = Math.min(width, proseMirrorWidth);
      props.updateAttributes({ width, height });
    }
    function onMouseUp() {
      document.body.removeEventListener('mousemove', onMouseMove);
    }

    document.body.addEventListener('mousemove', onMouseMove);
    document.body.addEventListener('mouseup', onMouseUp, { once: true });
  };

  const align = props.node.attrs.textAlign;
  const style = {};
  if (align === 'center') {
    style['margin'] = '0 auto';
  } else if (align === 'right') {
    style['margin'] = '0 0 0 auto';
  }

  return (
    <NodeViewWrapper
      className={classNames('image-resizer', {
        'image-selected': props.selected,
      })}
      style={style}
    >
      <img
        {...omit(props.node.attrs, ['alt', 'textAlign'])}
        className="primaryImage"
        alt={props.node.attrs.alt || ''}
        onClick={() => {
          if (props.selected) {
            expandWorkdocImage({
              file: {
                name: props.node.attrs.alt || `image-${Date.now()}.png`,
                token: props.node.attrs.src.replace(/\?.*$/, ''),
                width: props.node.attrs.width,
                height: props.node.attrs.height,
                mimeType: 'image/*',
              },
              isEditable: props.editor.isEditable,
              onDelete: () => {
                props.editor.chain().focus().deleteSelection().run();
              }
            });
          }
        }}
      />
      {props.selected && <div id="workdocImage"/>}
      {props.editor.isEditable && (
        <div className="resize-trigger">
          <div className="resize-trigger-top-left" onMouseDown={handler(Direction.TopLeft)} />
          <div className="resize-trigger-top-right" onMouseDown={handler(Direction.TopRight)} />
          <div className="resize-trigger-bottom-left" onMouseDown={handler(Direction.BottomLeft)} />
          <div className="resize-trigger-bottom-right" onMouseDown={handler(Direction.BottomRight)} />
        </div>
      )}
      {props.editor.isEditable && (
        <div className="image-align">
          <div
            className={props.editor.isActive({ textAlign: 'left' }) ? 'active' : ''}
            onClick={() => {
              props.editor.chain().focus().setTextAlign('left').run();
            }}
          >
            <TextLeftFilled size={16} />
          </div>
          <div
            className={props.editor.isActive({ textAlign: 'center' }) ? 'active' : ''}
            onClick={() => {
              props.editor.chain().focus().setTextAlign('center').run();
            }}
          >
            <TextMiddleFilled size={16} />
          </div>
          <div
            className={props.editor.isActive({ textAlign: 'right' }) ? 'active' : ''}
            onClick={() => {
              props.editor.chain().focus().setTextAlign('right').run();
            }}
          >
            <TextRightFilled size={16} />
          </div>
        </div>
      )}
    </NodeViewWrapper>
  );
};
