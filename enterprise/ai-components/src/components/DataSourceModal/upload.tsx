import { Upload, message } from 'antd';
import type { UploadProps } from 'antd';
import React, { useState } from 'react';
import { Typography, colors } from '@apitable/components';
import { AddFilled, QuestionCircleOutlined } from '@apitable/icons';
import styles from './style.module.less';

const SelectUpload = () => {
  const [localUploadFileList, setLocalUploadFileList] = useState([]);

  console.log('localUploadFileList:', localUploadFileList);

  const uploadProps: UploadProps = {
    name: 'file',
    multiple: true,
    action: 'https://run.mocky.io/v3/435e224c-44fb-4773-9faf-380c5e6a2188',
    listType: 'picture',
    onChange(info) {
      const { status } = info.file;
      if (status !== 'uploading') {
        console.log(info.file, info.fileList);
      }
      if (status === 'done') {
        message.success(`${info.file.name} file uploaded successfully.`);
      } else if (status === 'error') {
        message.error(`${info.file.name} file upload failed.`);
      }
    },
    onDrop(e) {
      console.log('Dropped files', e.dataTransfer.files);
    },
  };

  return (
    <div className={styles.tabItem}>
      <div className={styles.tabItemHeader}>
        <div className={styles.headerTitle}>
          <div>Local files</div>
          <QuestionCircleOutlined size={12} color={colors.textCommonPrimary} />
        </div>
        <div className={styles.headerDescription}>
          Train your Agent using your local files, so that the Agent can answer questions related to your local files (currently supports
          .pdf/.doc/.docx/.md/.txt files, only one file is allowed to be uploaded at a time)
        </div>
      </div>
      <div
        style={{
          overflow: 'auto',
          maxHeight: 'calc(60vh - 200px)',
        }}
      >
        <Upload.Dragger {...uploadProps}>
          <div onClick={() => {}}>
            <AddFilled size={32} />
            <Typography style={{ marginTop: 8 }} variant="body2" className={styles.selectLocalFilesTypography} align="center">
              <div className={styles.typographyTitle}>Drag the file or click here to upload</div>
              <div className={styles.typographyDescription}>Only supports .pdf/.docx/.doc/.md/.txt files</div>
            </Typography>
          </div>
        </Upload.Dragger>
      </div>
    </div>
  );
};

export default SelectUpload;
