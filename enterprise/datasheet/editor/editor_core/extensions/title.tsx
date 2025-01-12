import { mergeAttributes, Node } from '@tiptap/core';
import { Plugin, PluginKey } from '@tiptap/pm/state';
import { Decoration, DecorationSet } from '@tiptap/pm/view';
import { Strings, t } from '@apitable/core';

export interface ITitleOptions {
  placeholder?: string;
  level: 1;
  HTMLAttributes: Record<string, any>;
}

export const Title = Node.create<ITitleOptions>({
  name: 'title',
  addOptions() {
    return {
      level: 1,
      placeholder: t(Strings.workdoc_title_placeholder),
      HTMLAttributes: {
        'data-title': true
      },
    };
  },
  content: 'text*',
  marks: '',
  group: 'block',
  defining: true,
  renderHTML({ HTMLAttributes }) {
    const level = this.options.level;
    return [`h${level}`, mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0];
  },
  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey('title'),
        props: {
          decorations: ({ doc }) => {
            const placeholder = this.options.placeholder;
            const decorations: Decoration[] = [];
            doc.descendants((node, pos) => {
              const isEmpty = !node.isLeaf && !node.childCount;
              const isTitle = node.type.name === 'title';
              if (isEmpty && isTitle) {
                const className = 'is-empty';
                const decoration = Decoration.node(pos, pos + node.nodeSize, {
                  class: className,
                  'data-placeholder': placeholder,
                });
                decorations.push(decoration);
              }
              return false;
            });
            return DecorationSet.create(doc, decorations);
          },
        },
      }),
    ];
  },
});
