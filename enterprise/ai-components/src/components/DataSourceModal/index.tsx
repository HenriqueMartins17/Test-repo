import { Modal, Tabs } from 'antd';
import React, { useState } from 'react';
import { colors } from '@apitable/components';
import { QuestionCircleOutlined } from '@apitable/icons';
import styles from './style.module.less';
import { DEFAULT_TAB_CONFIG } from '@/components/DataSourceModal/const';
import { IDataSourceModalProps } from '@/components/DataSourceModal/interface';
import { IDataSourceUpdate } from '@/shared';

export * from './interface';

export const DataSourceModal: React.FC<IDataSourceModalProps> = ({ open, setOpen, tabConfig, setDataSource, onChange }) => {
  const tabs = { ...DEFAULT_TAB_CONFIG, ...tabConfig };

  const [loading, setLoading] = useState<boolean>(false);

  const [formData, setFormData] = useState<IDataSourceUpdate>();
  const [formDataError, setFormDataError] = useState<boolean>(true);

  console.log('formDataError', formDataError);

  const handleUpdateFormData = (updatedFormData: React.SetStateAction<IDataSourceUpdate | undefined>) => {
    setFormData(updatedFormData);
  };

  const onOk = async () => {
    if (formDataError) {
      return;
    }
    if (!formData) {
      return;
    }

    if (onChange) {
      setLoading(true);

      try {
        await onChange(formData);
        setOpen(false);
      } finally {
        setLoading(false);
      }
    } else {
      setOpen(false);
    }
  };

  return (
    <Modal
      open={open}
      title={
        <>
          <div className={styles.modalTitle}>
            <div>Select Data Sources</div>
            <QuestionCircleOutlined color={colors.textCommonPrimary} />
          </div>
          <div className={styles.modalDescription}>Upload documents or add links to your knowledge base or website to Agent on your own data.</div>
        </>
      }
      onOk={onOk}
      onCancel={() => {
        setOpen(false);
      }}
      okButtonProps={{ disabled: formDataError }}
      confirmLoading={loading}
      width={800}
      centered
      className={styles.modalStyle}
      destroyOnClose
    >
      <Tabs defaultActiveKey={Object.keys(tabs)[0]} tabPosition={'left'}>
        {Object.keys(tabs).map((key) => {
          if (typeof tabs[key] !== 'function') return null;
          return (
            <Tabs.TabPane tab={key} key={key}>
              {tabs[key]({ setOpen, setDataSource, onUpdateFormData: handleUpdateFormData, setFormDataError })}
            </Tabs.TabPane>
          );
        })}
      </Tabs>
    </Modal>
  );
};
