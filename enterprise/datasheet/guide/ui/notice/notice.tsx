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
import { Provider, useSelector } from 'react-redux';
import { Button, ThemeProvider } from '@apitable/components';
import { Selectors } from '@apitable/core';
import { ComponentDisplay } from 'pc/components/common/component_display';
import { ScreenSize } from 'pc/components/common/component_display/enum';
import { Method } from 'pc/components/route_manager/const';
import { navigationToUrl } from 'pc/components/route_manager/navigation_to_url';
import { store } from 'pc/store';
import { isSocialWecom } from '../../../home/social_platform/utils';
import { MobileModal } from './components/mobile_modal';
import { Modal } from './components/modal/modal';

export interface IGuideNoticeOptions {
  headerImg: string;
  readMoreTxt: string;
  readMoreUrl: string;
  onClose?: (...args: any) => void;
}

const Notice: FC<PropsWithChildren<IGuideNoticeOptions>> = props => {
  const { headerImg, readMoreTxt, readMoreUrl, children, onClose } = props;

  const spaceInfo = useSelector(state => state.space.curSpaceInfo);
  const isWecomSpace = isSocialWecom(spaceInfo);

  const navHistoryUpdatePage = () => {
    navigationToUrl(readMoreUrl, { method: Method.NewTab });
  };

  return (
    <>
      <ComponentDisplay minWidthCompatible={ScreenSize.md}>
        <Modal
          width={288}
          onClose={() => {
            onClose && onClose();
          }}
        >
          <div className='vika-guide-notice'>
            <div className={'vika-guide-notice-content'}>
              <div
                className={'vika-guide-notice-head-img'}
                style={{
                  backgroundImage: `url(${headerImg})`,
                }}
                onClick={navHistoryUpdatePage}
              />
              <div className={'vika-guide-notice-body'}>{children}</div>
            </div>
            {!isWecomSpace && (
              <div className='vika-guide-notice-btnWrap'>
                <Button color='primary' block onClick={navHistoryUpdatePage}>
                  {readMoreTxt}
                </Button>
              </div>
            )}
          </div>
        </Modal>
      </ComponentDisplay>
      {/* Mobile */}
      <ComponentDisplay maxWidthCompatible={ScreenSize.md}>
        <MobileModal
          onClose={() => {
            onClose && onClose();
          }}
        >
          <div className='vika-guide-notice'>
            <div className={'vika-guide-notice-content'}>
              <div>
                <span onClick={navHistoryUpdatePage}>
                  <img src={headerImg} style={{ width: '100%' }} alt="" />
                </span>
              </div>
              <div className={'vika-guide-notice-body'}>{children}</div>
            </div>
            {!isWecomSpace && (
              <div className='vika-guide-notice-btnWrap'>
                <Button color='primary' block onClick={navHistoryUpdatePage}>
                  {readMoreTxt}
                </Button>
              </div>
            )}
          </div>
        </MobileModal>
      </ComponentDisplay>
    </>
  );
};
export const showNotice = (props: PropsWithChildren<IGuideNoticeOptions>) => {
  const { children, ...rest } = props;
  const destroy = () => {
    const dom = document.querySelector('.vika-guide-modal');
    const node = dom && dom.parentNode;
    if (node) {
      setTimeout(() => {
        try {
          if (document.body.contains(node)) {
            node && document.body.removeChild(node);
          }
        } catch (e) {
          console.error(e);
        }
      });
    }
  };

  const NoticeWithTheme = (props: any) => {
    const { children, ...rest } = props;
    const cacheTheme = useSelector(Selectors.getTheme);
    return (
      <ThemeProvider theme={cacheTheme}>
        <Notice {...rest}>{children}</Notice>
      </ThemeProvider>
    );
  };

  const render = () => {
    setTimeout(() => {
      const div = document.createElement('div');
      div.setAttribute('class', 'vika-guide-modal');
      document.body.appendChild(div);
      const root = createRoot(div);
      root.render(
        <Provider store={store}>
          <NoticeWithTheme {...rest}>{children}</NoticeWithTheme>
        </Provider>,
      );
    });
  };

  const run = () => {
    destroy();
    render();
  };

  run();
};

export const destroyNotice = () => {
  const destroy = () => {
    const dom = document.querySelector('.vika-guide-modal');
    dom && document.body.removeChild(dom);
  };
  destroy();
};
