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

import { Modal as AntdModal } from 'antd';
import classNames from 'classnames';
import { FC, PropsWithChildren } from 'react';
import { createRoot } from 'react-dom/client';
import { Provider } from 'react-redux';
import { Button, lightColors, Typography } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { TComponent } from 'pc/components/common/t_component';
import { ScrollBar } from 'pc/components/scroll_bar';
import { usePlatform } from 'pc/hooks/use_platform';
import { store } from 'pc/store';
import { getEnvVariables, isMobileApp } from 'pc/utils/env';

export interface IPrivacyModalProps {
  title: string;
  onClose?: () => void;
}

const PrivacyModal: FC<PropsWithChildren<IPrivacyModalProps>> = props => {
  const { title, children, onClose } = props;
  const { desktop } = usePlatform();

  const _isMobileApp = isMobileApp();
  const linkToPrivacyPolicy = _isMobileApp ? Strings.privacy_policy_title : getEnvVariables().PRIVACY_POLICY_URL;

  return (
    <AntdModal
      visible
      className={classNames('vika-guide-privacy-modal', { ['vika-guide-modal-no-box-shadow']: true })}
      width={desktop ? 560 : '78vw'}
      closable={false}
      centered
      bodyStyle={{
        height: desktop ? '68vh' : '36vh',
      }}
      title={(
        <Typography
          variant='h4'
          style={{
            textAlign: 'center',
          }}
        >
          {title}
        </Typography>
      )}
      footer={(
        <div
          className='privacy-footer'
        >
          <div className='agreement'>
            <Typography variant='body3' style={{ textAlign: 'justify' }}>
              <TComponent
                tkey={t(Strings.guide_privacy_modal_content)}
                params={{
                  content: (
                    <>
                      <a href={linkToPrivacyPolicy} target='_blank' rel='noreferrer'>
                        {t(Strings.login_privacy_policy)}
                      </a>
                    </>
                  ),
                }}
              />
            </Typography>
          </div>
          <div style={{
            display: 'flex',
            justifyContent: 'center',
            marginTop: 16,
          }}>
            <Button
              onClick={onClose}
              color={lightColors.primaryColor}
              block={!desktop}
              style={{
                width: desktop ? 200 : '100%',
              }}
            >
              {t(Strings.understand_and_accept)}
            </Button>
          </div>
        </div>
      )}
      getContainer='.vika-guide-modal'
    >
      <ScrollBar>
        <div
          className={'vika-guide-notice-content'}
        >
          <div className={'vika-guide-notice-body'}>
            {children}
          </div>
        </div>
      </ScrollBar>
    </AntdModal>
  );
};
export const showPrivacyModal = (props: PropsWithChildren<IPrivacyModalProps>) => {
  const { children, ...rest } = props;
  const destroy = () => {
    const dom = document.querySelector('.vika-guide-modal');
    const node = dom && dom.parentNode;
    if (node) {
      setTimeout(() => {
        node && document.body.removeChild(node);
      });

    }
  };

  const render = () => {
    setTimeout(() => {
      const div = document.createElement('div');
      div.setAttribute('class', 'vika-guide-modal');
      document.body.appendChild(div);
      const root = createRoot(div);
      root.render(
        (<Provider store={store}><PrivacyModal {...rest}>{children}</PrivacyModal></Provider>));
    });
  };

  const run = () => {
    destroy();
    render();
  };

  run();
};

export const destroyPrivacyModal = () => {
  const destroy = () => {
    const dom = document.querySelector('.vika-guide-modal');
    dom && document.body.removeChild(dom);
  };
  destroy();
};
