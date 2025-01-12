/**
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

import { FC, PropsWithChildren } from 'react';
import { createRoot } from 'react-dom/client';
import { MirrorFeatureWarn } from 'pc/components/mirror/mirror_feature_tip/mirror_feature_tip';

export interface IGuideCustomTemplateOptions {
  templateKey: string;
  onClose: () => void;
  backdrop?: boolean;
  video?: string;
  onPlay?: () => void;
  videoId?: string;
  autoPlay?: boolean;
}

const CustomTemplate: FC<PropsWithChildren<IGuideCustomTemplateOptions>> = props => {
  const { templateKey, onClose } = props;
  console.log({ templateKey, props });
  switch (templateKey) {
    case 'createMirrorTip': {
      return <MirrorFeatureWarn onModalClose={onClose} />;
    }
    default: {
      return null;
    }
  }

};

export const showCustomTemplate = (props: PropsWithChildren<IGuideCustomTemplateOptions>) => {
  const { children, ...rest } = props;
  let root: any;
  const destroy = () => {
    root?.unmount();
    const dom = document.querySelector('.vika-guide-modal');
    dom && document.body.removeChild(dom);
  };

  const render = () => {
    setTimeout(() => {
      const div = document.createElement('div');
      div.setAttribute('class', 'vika-guide-modal');
      document.body.appendChild(div);
      root = createRoot(div);
      root.render(
        (<CustomTemplate {...rest} onClose={destroy}>{children}</CustomTemplate>));
    });
  };

  const run = () => {
    destroy();
    render();
  };

  run();
};

export const destroyCustomTemplate = () => {
  const destroy = () => {
    const dom = document.querySelector('.vika-guide-modal');
    dom && document.body.removeChild(dom);
  };
  destroy();
};
