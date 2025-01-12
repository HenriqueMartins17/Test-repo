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

import { Modal } from 'antd';
import Image from 'next/image';
import * as React from 'react';
import { isMobile } from 'react-device-detect';
import { createRoot } from 'react-dom/client';
import { Provider, useSelector } from 'react-redux';
import { Button, LinkButton, ThemeProvider, Typography, useThemeColors } from '@apitable/components';
import { integrateCdnHost, Navigation, Selectors, Strings, t } from '@apitable/core';
import { CloseOutlined } from '@apitable/icons';
import { TComponent } from 'pc/components/common/t_component';
import { Router } from 'pc/components/route_manager/router';
import { store } from 'pc/store';
import { getEnvVariables } from 'pc/utils/env';
import QrCodePng from 'static/icon/datasheet/share/qrcode/datasheet_img_qr_bj.png';
import styles from './styles.module.less';

interface IOrderModalProps {
  onModalClose(): void;
  modalTitle: string | JSX.Element;
  modalSubTitle: string | ((cb: () => void) => JSX.Element) | JSX.Element;
  qrCodeUrl: string;
  btnText?: string;
  illustrations?: JSX.Element;
  disClearSearch?: boolean;
}

export const OrderModal: React.FC<IOrderModalProps> = ({ onModalClose, modalTitle, modalSubTitle, qrCodeUrl, btnText, illustrations }) => {
  const isStringTitle = typeof modalTitle === 'string';
  const isStringSubTitle = typeof modalSubTitle === 'string';
  const isFunctionSubTitle = typeof modalSubTitle === 'function';
  const colors = useThemeColors();
  return (
    <div className={styles.container}>
      {/* <IconButton icon={CloseOutlined} shape="square" className={styles.closeIcon} onClick={onModalClose} size={'large'} /> */}
      <div>{isStringTitle ? <Typography variant={'h5'}>{modalTitle}</Typography> : modalTitle}</div>
      {isStringSubTitle ? (
        <Typography variant={'h6'} style={{ marginTop: 24 }}>
          {modalSubTitle}
        </Typography>
      ) : isFunctionSubTitle ? (
        (modalSubTitle as Function)(onModalClose)
      ) : (
        modalSubTitle
      )}
      {qrCodeUrl && (
        <div className={styles.qrCode}>
          <Image src={QrCodePng} alt="qrcode background" width={240} height={240} layout={'fill'} />
          <Image src={qrCodeUrl} alt="" width={224} height={224} />
        </div>
      )}
      {Boolean(illustrations) && illustrations}
      <Button
        color={colors.fc0}
        onClick={() => {
          onModalClose();
        }}
        style={{ width: 240, height: 40, marginTop: '32px' }}
      >
        {btnText || t(Strings.player_contact_us_confirm_btn)}
      </Button>
    </div>
  );
};

export const OrderModalWithTheme = (props: IOrderModalProps) => {
  const cacheTheme = useSelector(Selectors.getTheme);
  return (
    <ThemeProvider theme={cacheTheme}>
      <OrderModal {...props} />
    </ThemeProvider>
  );
};

export const showOrderModal = (config: Omit<IOrderModalProps, 'onModalClose'>) => {
  const container = document.createElement('div');
  document.body.appendChild(container);
  const root = createRoot(container);
  const onModalClose = () => {
    root.unmount();
    container.parentElement!.removeChild(container);
    if (location.search && !config.disClearSearch) {
      location.search = '';
    }
  };

  root.render(
    <Provider store={store}>
      <Modal
        visible
        wrapClassName={styles.modalWrapper}
        closeIcon={<CloseOutlined />}
        onCancel={onModalClose}
        destroyOnClose
        width="440px"
        footer={null}
        centered
        zIndex={1100}
      >
        <OrderModalWithTheme onModalClose={onModalClose} {...config} />
      </Modal>
    </Provider>,
  );
};

type IOrderType = 'BUY' | 'RENEW' | 'UPGRADE';

const getOrderType = (orderType: IOrderType) => {
  switch (orderType) {
    case 'UPGRADE': {
      return t(Strings.upgrade);
    }
    case 'RENEW': {
      return t(Strings.renewal);
    }
    default: {
      return t(Strings.subscribe);
    }
  }
};

export const showOrderModalAfterPay = (descColor: string, orderType: IOrderType) => {
  showOrderModal({
    modalTitle: t(Strings.upgrade_success_model, { orderType: getOrderType(orderType) }),
    modalSubTitle: (cb: () => void) => (
      <>
        <div className={styles.desc1}>
          {
            <TComponent
              tkey={t(Strings.upgrade_success_1_desc)}
              params={{
                orderType: getOrderType(orderType),
                position: (
                  <LinkButton
                    className={styles.linkButton}
                    style={{
                      display: 'inline-block',
                    }}
                    onClick={() => {
                      cb();
                      if (isMobile) {
                        return;
                      }
                      Router.push(Navigation.SPACE_MANAGE, { params: { pathInSpace: 'overview' } });
                    }}
                  >
                    {t(Strings.space_overview)}
                  </LinkButton>
                ),
              }}
            />
          }
        </div>
        <Typography className={styles.desc2} variant={'body2'} color={descColor}>
          {t(Strings.upgrade_success_2_desc)}
        </Typography>
      </>
    ),
    qrCodeUrl: integrateCdnHost(getEnvVariables().BILLING_PAY_SUCCESS_QRCODE_IMG!),
  });
};

export const showOrderContactUs = () => {
  showOrderModal({
    modalTitle: t(Strings.contact_model_title),
    modalSubTitle: t(Strings.contact_model_desc),
    qrCodeUrl: integrateCdnHost(getEnvVariables().BILLING_PAYMENT_PAGE_CONTACT_US_IMG!),
    disClearSearch: true,
  });
};

export const showUpgradeContactUs = () => {
  showOrderModal({
    modalTitle: t(Strings.contact_model_title),
    modalSubTitle: t(Strings.space_dashboard_contact_desc),
    qrCodeUrl: integrateCdnHost(getEnvVariables().BILLING_PAY_SUCCESS_QRCODE_IMG!),
  });
};
