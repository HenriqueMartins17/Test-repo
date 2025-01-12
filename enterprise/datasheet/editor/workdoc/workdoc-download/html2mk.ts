import TurndownService from 'turndown';

export const html2mk = (html: string) => {
  const turndownService = new TurndownService({
    headingStyle: 'atx',
    codeBlockStyle: 'fenced',
    emDelimiter: '*',
    strongDelimiter: '**',
    linkStyle: 'inlined',
    bulletListMarker: '-',
    hr: '---',
  });

  turndownService.addRule('taskList', {
    filter: function (node) {
      return (
        node.nodeName === 'LI' &&
        node.getAttribute('data-type') === 'taskItem'
      );
    },
    replacement: function (content, node) {
      const _content = content.replace(/\n/g, '') + '\n';
      return `- [${node.getAttribute('data-checked') === 'true' ? 'x' : ' '}] ${_content}`;
    }
  });

  // Add a custom rule to handle video elements
  turndownService.addRule('video', {
    filter: 'video',
    replacement: function (content, node) {
      const src = node.getAttribute('src');
      const title = node.getAttribute('title') || src;

      // Customize the Markdown syntax for the video element
      return `![${title}](${src})`;
    },
  });

  return turndownService.turndown(html);
};