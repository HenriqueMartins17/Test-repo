import { Plugin, PluginKey } from '@tiptap/pm/state';
import { Decoration, DecorationSet } from '@tiptap/pm/view';
import { Strings, t } from '@apitable/core';

export const uploadKey = new PluginKey('upload-image');

export const UploadImagePlaceholderPlugin = () =>
  new Plugin({
    key: uploadKey,
    state: {
      init() {
        return DecorationSet.empty;
      },
      apply(tr, set) {
        set = set.map(tr.mapping, tr.doc);
        // See if the transaction adds or removes any placeholders
        // @ts-ignore
        const action = tr.getMeta(this);
        if (action && action.add) {
          const { id, pos, src } = action.add;

          const placeholder = document.createElement('div');
          placeholder.setAttribute('class', 'img-placeholder');

          const image = document.createElement('img');
          image.setAttribute('class', 'preview-img');
          image.src = src;
          placeholder.appendChild(image);

          const loadingContainer = document.createElement('div');
          loadingContainer.setAttribute('class', 'image-loading-container');

          const loadingIcon = document.createElement('span');
          loadingIcon.setAttribute('class', 'image-loading-icon');
          loadingContainer.appendChild(loadingIcon);

          const loading = document.createElement('div');
          loading.setAttribute('class', 'image-loading-content');
          loading.innerText = t(Strings.workdoc_attach_uploading);
          loadingContainer.appendChild(loading);

          placeholder.appendChild(loadingContainer);

          const deco = Decoration.widget(pos, placeholder, {
            id,
          });
          set = set.add(tr.doc, [deco]);
        } else if (action && action.remove) {
          // @ts-ignore
          set = set.remove(set.find(null, null, (spec) => spec.id == action.remove.id));
        }
        return set;
      },
    },
    props: {
      decorations(state) {
        return this.getState(state);
      },
    },
  });
