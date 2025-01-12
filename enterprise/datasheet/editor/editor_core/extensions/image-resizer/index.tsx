// https://github.com/breakerh/tiptap-image-resize
import { mergeAttributes } from '@tiptap/core';
import Image from '@tiptap/extension-image';
import { ReactNodeViewRenderer } from '@tiptap/react';
import { Resizer } from './resizer';

export interface IImageOptions {
  inline: boolean;
  allowBase64: boolean;
  HTMLAttributes: Record<string, any>;
  useFigure: boolean;
}
export const ImageResize = Image.extend<IImageOptions>({
  name: 'image',
  addOptions() {
    return {
      inline: false,
      allowBase64: false,
      HTMLAttributes: {},
      useFigure: false,
    };
  },
  addAttributes() {
    return {
      width: {
        default: '100%',
        renderHTML: (attributes) => {
          return {
            width: attributes.width,
          };
        },
      },
      height: {
        default: 'auto',
        renderHTML: (attributes) => {
          return {
            height: attributes.height,
          };
        },
      },
      src: {
        default: '',
        renderHTML: (attributes) => {
          return {
            src: attributes.src,
          };
        },
      },
      alt: {
        default: '',
        renderHTML: (attributes) => {
          return {
            alt: attributes.alt,
          };
        },
      },
      isDraggable: {
        default: true,
        renderHTML: () => {
          return {};
        },
      },
    };
  },
  parseHTML() {
    return [
      {
        tag: 'image-resizer',
      },
    ];
  },

  renderHTML({ HTMLAttributes }) {
    return ['image-resizer', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes)];
  },

  addNodeView() {
    return ReactNodeViewRenderer(Resizer);
  },
});
