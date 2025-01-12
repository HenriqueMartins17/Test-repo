import { NodeSelection } from '@tiptap/pm/state';
// @ts-ignore
import { EditorView, __serializeForClipboard } from '@tiptap/pm/view';

export const DRAG_HANDLE_WIDTH = 24;

export const nodeDOMAtCoords = (coords: { x: number; y: number }) => {
  return document.elementsFromPoint(coords.x, coords.y)
    .find((element: Element) =>
      element.parentElement?.matches?.('.ProseMirror') ||
      element.matches([
        'li',
        'p:not(:first-child)',
        'pre',
        'blockquote',
        'h1, h2, h3, h4, h5, h6',
      ].join(', '))
    );
};

const nodePosAtDOM = (node: Element, view: EditorView) => {
  const boundingRect = node.getBoundingClientRect();

  return view.posAtCoords({
    left: boundingRect.left + 1,
    top: boundingRect.top + 1,
  })?.inside;
};

export const handleDragStart = (event: DragEvent, view: EditorView) => {
  view.focus();
  if (!event.dataTransfer) return;

  const node = nodeDOMAtCoords({
    x: event.clientX + 50 + DRAG_HANDLE_WIDTH,
    y: event.clientY,
  });

  if (!(node instanceof Element)) return;

  const nodePos = nodePosAtDOM(node, view);
  if (!nodePos || nodePos < 0) return;

  view.dispatch(
    view.state.tr.setSelection(NodeSelection.create(view.state.doc, nodePos))
  );

  const slice = view.state.selection.content();
  const { dom, text } = __serializeForClipboard(view, slice);

  event.dataTransfer.clearData();
  event.dataTransfer.setData('text/html', dom.innerHTML);
  event.dataTransfer.setData('text/plain', text);
  event.dataTransfer.effectAllowed = 'copyMove';

  event.dataTransfer.setDragImage(node, 0, 0);
  view.dragging = { slice, move: event.ctrlKey };
};

export const handleClick = (event: MouseEvent, view: EditorView) => {
  view.focus();

  view.dom.classList.remove('dragging');

  const node = nodeDOMAtCoords({
    x: event.clientX + 50 + DRAG_HANDLE_WIDTH,
    y: event.clientY,
  });

  if (!(node instanceof Element)) return;

  const nodePos = nodePosAtDOM(node, view);
  if (!nodePos) return;

  view.dispatch(
    view.state.tr.setSelection(NodeSelection.create(view.state.doc, nodePos))
  );
};

export const absoluteRect = (node: Element) => {
  const data = node.getBoundingClientRect();

  return {
    top: data.top,
    left: data.left,
    width: data.width,
  };
};
