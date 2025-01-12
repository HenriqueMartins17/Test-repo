import { Input, Form } from 'antd';
import { RequiredMark } from 'antd/lib/form/Form';
import { useEffect, useState } from 'react';
import { colors } from '@apitable/components';
import { LinkOutlined, QuestionCircleOutlined } from '@apitable/icons';
import styles from './style.module.less';
import { IDataSourceType } from '@/shared/types';

const SelectAirtable = ({ onUpdateFormData, setFormDataError }) => {
  const [form] = Form.useForm();

  const [airtableUrl, setAirtableUrl] = useState<string>('');
  const [airtableApi, setAirtableApi] = useState<string>('');

  const updateFormData = () => {
    const urlMatch = airtableUrl.match(urlPattern);

    let baseId = '';
    let tableId = '';

    if (urlMatch) {
      baseId = urlMatch[1];
      tableId = urlMatch[2];
    }

    const updatedFormData: IDataSourceType[] = [
      {
        type: 'airtable',
        airtable: {
          apiKey: airtableApi,
          baseId,
          tableId,
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

  // const urlPattern = /^https?:\/\/api\.airtable\.com\/v0\/[a-zA-Z0-9_-]+\/[a-zA-Z0-9_-]+$/;
  const urlPattern = /^https?:\/\/airtable\.com\/(app[a-zA-Z0-9]+)\/(tbl[a-zA-Z0-9]+)\/viw[a-zA-Z0-9]+(?:\?.*)?$/;
  const apiPattern = /^.{6,}$/;

  const [urlCheckPassed, setUrlCheckPassed] = useState(false);
  const [apiCheckPassed, setApiCheckPassed] = useState(false);
  const [urlInputFinished, setUrlInputFinished] = useState(false);
  const [apiInputFinished, setApiInputFinished] = useState(false);

  const handleUrlBlur = () => {
    if (airtableUrl === '') {
      return;
    }

    const isURLCorrect = airtableUrl.match(urlPattern);

    if (!isURLCorrect) {
      setFormDataError(true);
    }

    setUrlCheckPassed(!!isURLCorrect);
    setUrlInputFinished(true);
    updateFormData();
  };

  const handleApiBlur = () => {
    if (airtableApi === '') {
      return;
    }

    const isApiCorrect = airtableApi.match(apiPattern);

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
          <div>Airtable</div>
          <QuestionCircleOutlined size={12} color={colors.textCommonPrimary} />
        </div>
        <div className={styles.headerDescription}>
          To train an Agent using your table on Airtable, so that the Agent can answer questions related to your Airtable table
        </div>
      </div>
      <Form
        form={form}
        layout="vertical"
        initialValues={{ requiredMarkValue: requiredMark }}
        onValuesChange={onRequiredTypeChange}
        requiredMark={requiredMark}
      >
        <Form.Item label="Table URL" required>
          <Input
            type="url"
            placeholder="Enter the URL of your table in Airtable"
            prefix={<LinkOutlined />}
            onBlur={handleUrlBlur}
            value={airtableUrl}
            onChange={(e) => setAirtableUrl(e.target.value)}
          />
          <div className={styles.formItemDescription}>{urlInputFinished && !urlCheckPassed && 'Invalid URL format.'}</div>
        </Form.Item>
        <Form.Item label="API Token" required>
          <Input placeholder="Enter your API Token." onBlur={handleApiBlur} value={airtableApi} onChange={(e) => setAirtableApi(e.target.value)} />
          <div className={styles.formItemDescription}>{apiInputFinished && !apiCheckPassed && 'Invalid token format.'}</div>
        </Form.Item>
      </Form>
    </div>
  );
};

export default SelectAirtable;
