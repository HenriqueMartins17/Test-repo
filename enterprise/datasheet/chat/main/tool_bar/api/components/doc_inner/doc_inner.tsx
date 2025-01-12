import classnames from 'classnames';
import { template } from 'lodash';
import { useMemo } from 'react';
import { Strings, t } from '@apitable/core';
import { CodeLanguage } from 'pc/components/api_panel/field_codes';
import { markdown } from 'pc/components/api_panel/field_codes/examples/markdown_render';
import mdStyles from 'pc/components/api_panel/field_codes/markdown.module.less';

interface IDocInnerProps {
  language: CodeLanguage;
  tempateConfig: {
    token: string;
    aiId: string
  }
}

const TEMPLATE_INTERPOLATE = /{{([\s\S]+?)}}/g;

export const DocInner: React.FC<IDocInnerProps> = ({ language, tempateConfig }) => {
  const templateStr = useMemo(() => {
    if (language === CodeLanguage.Js) {
      return t(Strings.ai_api_javascript_template);
    }
    if (language === CodeLanguage.Python) {
      return t(Strings.ai_api_python_template);
    }
    return t(Strings.ai_api_curl_template);
  }, [language]);

  const _docHtml = template(templateStr, { interpolate: TEMPLATE_INTERPOLATE })(tempateConfig);
  const docHtml = markdown.render(_docHtml);

  return <div
    dangerouslySetInnerHTML={{ __html: docHtml }}
    className={classnames(mdStyles.markdown, 'vk-prose [&_img]:vk-m-0 vk-pt-4 vk-max-w-none')}
  />;
};
