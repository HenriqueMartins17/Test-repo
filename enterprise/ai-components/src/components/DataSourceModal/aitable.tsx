import { Input, Form } from 'antd';
import { RequiredMark } from 'antd/lib/form/Form';
import { useEffect, useState } from 'react';
import { colors } from '@apitable/components';
import { LinkOutlined, QuestionCircleOutlined } from '@apitable/icons';
import styles from './style.module.less';
import { IDataSourceType } from '@/shared/types';

const SelectAitable = ({ onUpdateFormData, setFormDataError }) => {
  const [form] = Form.useForm();

  const [aitableUrl, setAitableUrl] = useState<string>('');
  const [aitableApi, setAitableApi] = useState<string>('');

  const updateFormData = () => {
    const urlMatch = aitableUrl.match(urlPattern);

    let datasheetId = '';
    let viewId = '';

    if (urlMatch) {
      datasheetId = urlMatch[2];
      viewId = urlMatch[3];
    }

    const updatedFormData: IDataSourceType[] = [
      {
        type: 'aitable',
        aitable: {
          apiKey: aitableApi,
          datasheetId,
          viewId,
        },
      },
    ];

    if (onUpdateFormData) {
      onUpdateFormData(updatedFormData);
    }
  };

  const [requiredMark, setRequiredMarkType] = useState<RequiredMark>('optional');
  const onRequiredTypeChange = ({ requiredMarkValue }: { requiredMarkValue: RequiredMark }) => {
    setRequiredMarkType(requiredMarkValue);
  };

  const urlPattern = /^https?:\/\/(www\.)?aitable\.ai\/workbench\/(dst[a-zA-Z0-9_-]{15})\/(viw[a-zA-Z0-9_-]{10})(\?.*)?$/;
  const apiPattern = /^usk[a-zA-Z0-9_-]{20}$/;
  const [urlCheckPassed, setUrlCheckPassed] = useState(false);
  const [apiCheckPassed, setApiCheckPassed] = useState(false);
  const [urlInputFinished, setUrlInputFinished] = useState(false);
  const [apiInputFinished, setApiInputFinished] = useState(false);

  const handleUrlBlur = () => {
    if (aitableUrl === '') {
      return;
    }

    const isURLCorrect = aitableUrl.match(urlPattern);

    if (!isURLCorrect) {
      setFormDataError(true);
    }

    setUrlCheckPassed(!!isURLCorrect);
    setUrlInputFinished(true);
    updateFormData();
  };

  const handleApiBlur = () => {
    if (aitableApi === '') {
      return;
    }

    const isApiCorrect = aitableApi.match(apiPattern);

    if (!isApiCorrect) {
      setFormDataError(true);
    }

    setApiCheckPassed(!!isApiCorrect);
    setApiInputFinished(true);
    updateFormData();
  };

  useEffect(() => {
    if (apiCheckPassed && urlCheckPassed) {
      setFormDataError(false);
    }
  }, [apiCheckPassed, setFormDataError, urlCheckPassed]);

  return (
    <div className={styles.tabItem}>
      <div className={styles.tabItemHeader}>
        <div className={styles.headerTitle}>
          <div>AITable</div>
          <QuestionCircleOutlined size={12} color={colors.textCommonPrimary} />
        </div>
        <div className={styles.headerDescription}>
          To train an Agent using your table on AITable, so that the Agent can answer questions related to your AITable table
        </div>
      </div>
      <Form
        form={form}
        layout="vertical"
        initialValues={{ requiredMarkValue: requiredMark }}
        onValuesChange={onRequiredTypeChange}
        requiredMark={requiredMark}
      >
        <Form.Item label="AITable URL" required>
          <Input
            type="url"
            placeholder="Enter the URL of your table in AITable"
            prefix={<LinkOutlined />}
            onBlur={handleUrlBlur}
            value={aitableUrl}
            onChange={(e) => setAitableUrl(e.target.value)}
          />
          <div className={styles.formItemDescription}>{urlInputFinished && !urlCheckPassed && 'Invalid URL format.'}</div>
        </Form.Item>
        <Form.Item label="API Token" required>
          <Input placeholder="Enter your API Token." onBlur={handleApiBlur} value={aitableApi} onChange={(e) => setAitableApi(e.target.value)} />
          <div className={styles.formItemDescription}>{apiInputFinished && !apiCheckPassed && 'Invalid token format.'}</div>
        </Form.Item>
      </Form>
    </div>
  );
};

export default SelectAitable;
