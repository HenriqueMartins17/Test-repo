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

import classNames from 'classnames';
import { FC, PropsWithChildren, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Strings, t } from '@apitable/core';
import { UpdateSpaceModal } from 'pc/components/space_manage/upgrade_space/expand_upgrade_space';
import { getEnvVariables } from 'pc/utils/env';
//@ts-ignore
import { usageWarnModal } from 'enterprise/subscribe_system/usage_warn_modal/usage_warn_modal';

interface IBillingStripUiConfigProps {
  title: string;
  description: string;
  listHeader: string;
  listContent: string[];
  listFooter: string;
  url: string;
}

export interface IGuideBillingStripProps {
  confirmText?: string;
  skipText?: string;
  uiConfig: IBillingStripUiConfigProps;
  backdrop?: boolean;
  onClose?: () => void;
}

const Modal: FC<PropsWithChildren<IGuideBillingStripProps>> = (props) => {
  const { onClose: _onClose, backdrop = true } = props;

  const [show, setShow] = useState(true);

  const onClose = () => {
    setShow(false);
    _onClose?.();
  };

  const onConfirm = () => {
    onClose();
  };
  if (!getEnvVariables().IS_AITABLE) {
    usageWarnModal({
      title: t(Strings.payment_reminder_modal_title),
      alertContent: t(Strings.payment_reminder_modal_content),
    });
    return null;
  }
  return (
    <UpdateSpaceModal
      visible={show}
      className={classNames({ ['guide-modal-no-box-shadow']: backdrop })}
      closable
      maskClosable={false}
      centered
      mask={backdrop}
      footer={null}
      onCancel={onConfirm}
      getContainer={'.guide-billing-strip'}
    />
  );
};

export const showBillingStrip = (props: PropsWithChildren<IGuideBillingStripProps>) => {
  const { children, ...rest } = props;
  const existDom = document.querySelector('.guide-billing-strip');
  if (existDom) {
    document.body.removeChild(existDom);
  }
  const div = document.createElement('div');
  div.setAttribute('class', 'guide-billing-strip');
  document.body.appendChild(div);
  const root = createRoot(div);

  const destroy = () => {
    root.unmount();
    const dom = document.querySelector('.guide-billing-strip');
    dom && document.body.removeChild(dom);
  };

  const render = () => {
    setTimeout(() => {
      root.render(
        <Modal onClose={destroy} {...rest}>
          {children}
        </Modal>,
      );
    });
  };

  render();
};

export const destroyBillingStrip = () => {
  const destroy = () => {
    const dom = document.querySelector('.guide-billing-strip');
    dom && document.body.removeChild(dom);
  };
  destroy();
};
