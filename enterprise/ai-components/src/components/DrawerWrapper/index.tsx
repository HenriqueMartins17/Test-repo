import { Drawer } from 'antd';
import classnames from 'classnames';
import React from 'react';
import { CloseOutlined, QuestionCircleOutlined } from '@apitable/icons';
import styles from './style.module.less';

interface IModalWrapperProps {
  open: boolean;
  close: () => void;
  footer?: React.ReactElement;
  classNames?: string;
  config: {
    title: string | null;
    documentLink?: string;
    modalWidth?: number;
    submitText?: string;
    cancelText?: string;
    onSubmit?: () => void;
  };
}

export const DrawerWrapper: React.FC<React.PropsWithChildren<IModalWrapperProps>> = React.memo(
  ({ children, open, close, config, footer, classNames }) => {

    return (
      <Drawer
        zIndex={200}
        open={open}
        onClose={close}
        title={
          config.title && (
            <div className={styles.modalTitle}>
              <div className={styles.titleLeft}>
                <span style={{ marginRight: 8 }}>{config.title}</span>
                <a href={config.documentLink}>
                  <QuestionCircleOutlined />
                </a>
              </div>
              <div className={styles.titleRight} onClick={close}>
                <CloseOutlined />
              </div>
            </div>
          )
        }
        className={classnames(styles.drawerWrapper, classNames)}
        destroyOnClose
        closable={false}
        width={config.modalWidth || '80%'}
        footer={footer}
      >
        {children}
      </Drawer>
    );
  },
);
