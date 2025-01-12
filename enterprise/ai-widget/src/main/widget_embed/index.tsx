// @ts-nocheck
import React from 'react';
import { Typography } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { markdown } from 'pc/components/api_panel/field_codes/examples/markdown_render';
import { Modal } from 'pc/components/common';
import styles from './style.module.less';

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
<!-- Start of AITable (aitable.ai) code -->
<script>
  window.__aitable = window.__aitable || {};
  window.__aitable.share = "${shareId}";
  window.__aitable.baseUrl = "${url}";
  ;(function(w,d){var s=d.createElement("script");s.async=true,s.type="text/javascript",s.src="${url}/file/js/aitable_widget.js?v=0.0.1",d.head.appendChild(s)}(window,document));
</script>
<noscript><a href="https://support.google.com/adsense/answer/12654?hl=en/" rel="nofollow">Chat with us</a>, powered by <a href="https://aitable.ai" rel="noopener nofollow" target="_blank">AITable</a></noscript>
<!-- End of AITable code -->
\`\`\`
`
        )
      }}
    />
  </Modal>;
};
