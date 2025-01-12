import MarkdownIt from 'markdown-it';
import Prism from 'prismjs';
import { t, Strings } from '@apitable/core';
import 'prismjs/components/prism-bash';
import 'prismjs/components/prism-json';
import 'prismjs/components/prism-python';
import 'prismjs/components/prism-go';
import 'prismjs/components/prism-rust';
import 'prismjs/components/prism-c';
import 'prismjs/components/prism-cpp';
import 'prismjs/components/prism-csharp';
import 'prismjs/components/prism-java';
import 'prismjs/components/prism-lua';
import 'prismjs/components/prism-sql';
import 'prismjs/components/prism-yaml';
import 'prismjs/components/prism-docker';
import 'prismjs/components/prism-swift';
import 'prismjs/components/prism-typescript';
import 'prismjs/components/prism-ruby';
import styles from './style.module.less';

const markdown = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: false,
  smartypants: false,
});

const defaultRender =
  markdown.renderer.rules.link_open ||
  function (tokens, idx, options, env, self) {
    return self.renderToken(tokens, idx, options);
  };

markdown.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  // If you are sure other plugins can't add `target` - drop check below
  const aIndex = tokens[idx].attrIndex('target');

  if (aIndex < 0) {
    tokens[idx].attrPush(['target', '_blank']); // add new attribute
  } else {
    const target = tokens[idx];
    if (target.attrs && target.attrs[aIndex]) {
      target.attrs[aIndex][1] = '_blank'; // replace value of existing attr
    }
  }

  // pass token to default renderer.
  return defaultRender(tokens, idx, options, env, self);
};

markdown.set({
  highlight: function (str, lang) {
    const langObject = Prism.languages[lang];
    const copy = `<div class=${styles.codeHeaderCopy}><span data-clipboard-text="${markdown.utils.escapeHtml(
      str,
    )}" class="markdown-it-code-button-copy">${t(Strings.copy_link)}</span></div>`;
    const header = `<div class=${styles.codeHeader}><span>${lang || 'text'}</span>${copy}</div>`;
    if (lang && langObject) {
      try {
        const html = Prism.highlight(str, langObject, lang);
        return `<pre class="${styles.code}">
          ${header}
          <pre class="language-ai-${lang}"><code>${html}</code></pre>
        </pre>`;
      } catch (__) {
        console.error(__);
      }
    }
    return `<pre class="${styles.code}">
      ${header}
      <pre class="language-ai-${lang}"><code>${markdown.utils.escapeHtml(str)}</code></pre>
    </pre>`;
  },
});

export default markdown;
