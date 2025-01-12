import { Extension } from '@tiptap/core';
import { Plugin } from '@tiptap/pm/state';
import { handleDragStart, handleClick, DRAG_HANDLE_WIDTH, nodeDOMAtCoords, absoluteRect } from './utils';

const dragHandle = () => {
  let dragHandleElement: HTMLElement | null = null;

  const hideDragHandle = () => {
    if (dragHandleElement) {
      dragHandleElement.classList.add('hidden');
    }
  };
  const showDragHandle = () => {
    if (dragHandleElement) {
      dragHandleElement.classList.remove('hidden');
    }
  };

  return new Plugin({
    view: (editorView) => {
      dragHandleElement = document.createElement('div');
      dragHandleElement.draggable = true;
      dragHandleElement.dataset.dragHandle = '';
      dragHandleElement.classList.add('drag-handle');
      dragHandleElement.addEventListener('dragstart', (event) => {
        handleDragStart(event, editorView);
      });
      dragHandleElement.addEventListener('click', (event) => {
        handleClick(event, editorView);
      });
      hideDragHandle();

      editorView?.dom?.parentElement?.appendChild(dragHandleElement);

      return {
        destroy: () => {
          dragHandleElement?.remove?.();
          dragHandleElement = null;
        },
      };
    },
    props: {
      handleDOMEvents: {
        mousemove: (view, event) => {
          if (!view.editable) {
            return;
          }

          const node = nodeDOMAtCoords({
            x: event.clientX + 50 + DRAG_HANDLE_WIDTH,
            y: event.clientY,
          });

          const isTitle = node?.matches('h1[data-title=true]');

          if (!(node instanceof Element) || isTitle) {
            hideDragHandle();
            return;
          }

          const compStyle = window.getComputedStyle(node);
          const lineHeight = parseInt(compStyle.lineHeight, 10);
          const paddingTop = parseInt(compStyle.paddingTop, 10);

          const rect = absoluteRect(node);

          rect.top += (lineHeight - 16) / 2;
          rect.top += paddingTop;
          // Li markers
          if (node.matches('ul:not([data-type=taskList]) li, ol li')) {
            rect.left -= DRAG_HANDLE_WIDTH;
          }
          rect.width = DRAG_HANDLE_WIDTH;

          if (!dragHandleElement) return;

          dragHandleElement.style.left = `${rect.left - rect.width}px`;
          dragHandleElement.style.top = `${rect.top}px`;
          showDragHandle();
        },
        keydown: () => {
          hideDragHandle();
        },
        mousewheel: () => {
          hideDragHandle();
        },
        // dragging class is used for CSS
        dragstart: (view) => {
          view.dom.classList.add('dragging');
        },
        drop: (view) => {
          view.dom.classList.remove('dragging');
        },
        dragend: (view) => {
          view.dom.classList.remove('dragging');
        },
      },
    },
  });
};

export const DragDrop = Extension.create({
  name: 'dragDrop',
  addProseMirrorPlugins() {
    return [
      dragHandle(),
    ];
  }
});
