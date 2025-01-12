
import { Modal } from 'antd';

import Clipboard from 'clipboard';
import MarkdownIt from 'markdown-it';
import Prism from 'prismjs';
import React from 'react';
import { Typography } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import 'prismjs/components/prism-bash';
import 'prismjs/components/prism-json';
import 'prismjs/components/prism-python';
import 'prismjs/components/prism-go';
import styles from './style.module.less';

export const copyOutlinedStr = `<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
<path fill-rule="evenodd" clip-rule="evenodd" d="M4.5 11.5H2.75C2.05964 11.5 1.5 10.9404 1.5 10.25L1.50004 2.74999C1.50004 2.05964 2.05969 1.5 2.75004 1.5H10.25C10.9404 1.5 11.5 2.05964 11.5 2.75V4.5H13.25C13.9404 4.5 14.5 5.05964 14.5 5.75V13.25C14.5 13.9404 13.9404 14.5 13.25 14.5H5.75C5.05964 14.5 4.5 13.9404 4.5 13.25V11.5ZM3 10L3.00004 3H10V4.5H5.75C5.05964 4.5 4.5 5.05964 4.5 5.75V10H3ZM6 13V6H13V13H6Z" fill="#D9D9D9"/>
</svg>`;

export const debugOutlinedStr = `<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
<path fill-rule="evenodd" clip-rule="evenodd" d="M10.25 2.5C9.13059 2.5 8.18302 3.23572 7.86445 4.25H1.75C1.33579 4.25 1 4.58579 1 5C1 5.41421 1.33579 5.75 1.75 5.75H7.86445C8.18302 6.76428 9.13059 7.5 10.25 7.5C11.3694 7.5 12.317 6.76428 12.6355 5.75H14.25C14.6642 5.75 15 5.41421 15 5C15 4.58579 14.6642 4.25 14.25 4.25H12.6355C12.317 3.23572 11.3694 2.5 10.25 2.5ZM9.25 5C9.25 4.44772 9.69772 4 10.25 4C10.8023 4 11.25 4.44772 11.25 5C11.25 5.55228 10.8023 6 10.25 6C9.69772 6 9.25 5.55228 9.25 5Z" fill="#D9D9D9"/>
<path fill-rule="evenodd" clip-rule="evenodd" d="M5.75 8.5C4.63059 8.5 3.68302 9.23572 3.36445 10.25H1.75C1.33579 10.25 1 10.5858 1 11C1 11.4142 1.33579 11.75 1.75 11.75H3.36445C3.68302 12.7643 4.63059 13.5 5.75 13.5C6.86941 13.5 7.81698 12.7643 8.13555 11.75H14.25C14.6642 11.75 15 11.4142 15 11C15 10.5858 14.6642 10.25 14.25 10.25H8.13555C7.81698 9.23572 6.86941 8.5 5.75 8.5ZM4.75 11C4.75 10.4477 5.19772 10 5.75 10C6.30228 10 6.75 10.4477 6.75 11C6.75 11.5523 6.30228 12 5.75 12C5.19772 12 4.75 11.5523 4.75 11Z" fill="#D9D9D9"/>
</svg>`;

if (!process.env.SSR) {
  new Clipboard('.markdown-it-code-button-copy');
}

const md = new MarkdownIt({
  highlight: (str, lang) => {
    if (lang) {
      const langObject = Prism.languages[lang];
      try {
        return `<pre
            class="language-${lang}"
            style="position: relative; white-space: normal;"
          ><code style="white-space: pre-wrap;">${Prism.highlight(str, langObject, lang)}</code>
            <div class="markdown-it-code-button-wrap"><button
              data-clipboard-text="${md.utils.escapeHtml(str)}"
              class="markdown-it-code-button-copy">${copyOutlinedStr}${t(Strings.copy_link)}</button>
          </pre>`;
      } catch (err) {
        console.warn('! ' + err);
      }
    }
    return `<pre class="language-${lang}"><code>` + md.utils.escapeHtml(str) + '</code></pre>';
  },
}) as any;

// Remember old renderer, if overridden, or proxy to default renderer
const defaultRender =
  md.renderer.rules.link_open ||
  function render(tokens: any, idx: any, options: any, _env: any, self: any) {
    return self.renderToken(tokens, idx, options);
  };

md.renderer.rules.link_open = (tokens: any[], idx: number, options: any, env: any, self: any) => {
  // If you are sure other plugins can't add `target` - drop check below
  const aIndex = tokens[idx].attrIndex('target');
  if (aIndex < 0) {
    tokens[idx].attrPush(['target', '_blank']); // add new attribute
  } else {
    tokens[idx].attrs[aIndex][1] = '_blank'; // replace value of existing attr
  }
  // pass token to default renderer.
  return defaultRender(tokens, idx, options, env, self);
};

export const markdown = md;

interface IWidgetEmbedProps {
  visible: boolean;
  shareId: string;
  hide: () => void
}

export const WidgetEmbed: React.FC<IWidgetEmbedProps> = ({ visible, hide, shareId }) => {
  const url = window ? window.location.origin : '';
  return <Modal
    visible={visible}
    width={600}
    wrapClassName={styles.modalWrapper}
    onCancel={hide}
    footer={null}
    centered
  >
    <Typography variant={'h6'} style={{ marginBottom: 16 }}>
      { t(Strings.ai_embed_website) }
    </Typography>
    <div
      key={shareId}
      className={styles.chatInner}
      dangerouslySetInnerHTML={{
        __html: markdown.render(`${t(Strings.ai_embed_website_iframe_tips)}
\`\`\`javascript
<iframe
  src="${url}/embed/ai/${shareId}"
  style="width: 100%; height: 100%; min-height: 700px" frameborder="0"
></iframe>
\`\`\`
${t(Strings.ai_embed_website_javascript_tips)}
\`\`\`javascript
<!-- Start of AirAgent (airagent.ai) code -->
<script>
  window.__aitable = window.__aitable || {};
  window.__aitable.share = "${shareId}";
  window.__aitable.baseUrl = "${url}";
  ;(function(w,d){var s=d.createElement("script");s.async=true,s.type="text/javascript",s.src="${url}/file/js/aitable_widget.js?v=0.0.1",d.head.appendChild(s)}(window,document));
</script>
<noscript><a href="https://support.google.com/adsense/answer/12654?hl=en/" rel="nofollow">Chat with us</a>, powered by <a href="https://airagent.ai" rel="noopener nofollow" target="_blank">AITable</a></noscript>
<!-- End of AirAgent code -->
\`\`\`
`
        )
      }}
    />
  </Modal>;
};
