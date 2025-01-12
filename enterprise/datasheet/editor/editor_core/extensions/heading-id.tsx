import { Node } from '@tiptap/core';

export const HeadingId = Node.create({
  name: 'headingId',
  group: 'block',
  atom: true,
  addGlobalAttributes() {
    return [
      {
        types: ['heading'],
        attributes: {
          id: {
            default: null,
          },
          class: {
            default: null,
            renderHTML: (attributes) => {
              return {
                class: attributes.class
              };
            }
          },
        },
      },
    ];
  },
});

