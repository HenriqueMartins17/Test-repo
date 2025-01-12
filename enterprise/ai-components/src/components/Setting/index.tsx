import { RegistryWidgetsType } from '@rjsf5/utils';
import { get, set } from 'lodash';
import { useEffect, useState } from 'react';
import { Button, Modal, Skeleton, Message } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import styles from './index.module.less';
import { ReactSchemaForm, DrawerWrapper } from '@/components';
import { useAIContext, IAISettingResponse } from '@/shared';
interface ISettingProps {
  visible: boolean;
  close: () => void;
  widgets?: RegistryWidgetsType;
  locale?: string;
}

export function Setting(props: ISettingProps) {
  const { visible, close } = props;
  const { context } = useAIContext();
  // const [type, setType] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [sumbitLoading, setSubmitLoading] = useState(false);
  const [setting, setSetting] = useState<IAISettingResponse>();
  // const [formData, setFormData] = useState<any>(null);
  const [isChanged, setIsChanged] = useState(false);

  const fetchSetting = async (_type?: string) => {
    setLoading(true);
    let type = _type;
    if (type && type === context.data.type) {
      type = undefined;
    }
    const res = await context.api.getAISetting(context.data.id, type);
    Object.keys(res.data.UISchema).forEach((key) => {
      if (res.data.UISchema[key]['ui:description']) {
        res.data.UISchema[key]['ui:description'] = t(res.data.UISchema[key]['ui:description']);
      }
      if (res.data.UISchema[key]['ui:title']) {
        res.data.UISchema[key]['ui:title'] = t(res.data.UISchema[key]['ui:title']);
      }
    });
    if (get(res.data, 'JSONSchema.properties.scoreThreshold.anyOf[0].title')) {
      // @ts-ignore
      set(res.data, 'JSONSchema.properties.scoreThreshold.anyOf[0].title', t(res.data.JSONSchema.properties.scoreThreshold.anyOf[0].title));
    }
    if (get(res.data, 'JSONSchema.properties.scoreThreshold.anyOf[1].title')) {
      // @ts-ignore
      set(res.data, 'JSONSchema.properties.scoreThreshold.anyOf[1].title', t(res.data.JSONSchema.properties.scoreThreshold.anyOf[1].title));
    }
    if (get(res.data, 'JSONSchema.properties.scoreThreshold.anyOf[2].title')) {
      // @ts-ignore
      set(res.data, 'JSONSchema.properties.scoreThreshold.anyOf[2].title', t(res.data.JSONSchema.properties.scoreThreshold.anyOf[2].title));
    }
    setSetting(res.data);
    setLoading(false);
  };

  useEffect(() => {
    if (visible) {
      setIsChanged(false);
      fetchSetting();
    }
  }, [visible]);

  // eslint-disable-next-line require-await
  const onSubmit = async (data) => {
    try {
      setSubmitLoading(true);
      await context.api.updateAIInfo(
        {
          // @ts-ignore
          setting: setting.data,
        },
        context.data.id,
      );
      await context.fetchAIDetail(true, false);
      close();
      Message.success({ content: t(Strings.ai_update_setting_success) });
    } catch (e: any) {
      console.log(e);
      if (e.errorFields) {
        Message.error({ content: e.errorFields[0].errors[0] });
      } else {
        Message.error({ content: e.message });
      }
    } finally {
      setSubmitLoading(false);
    }
  };

  const _hide = () => {
    if (isChanged) {
      Modal.confirm({
        title: t(Strings.ai_close_setting_tip_title),
        content: t(Strings.ai_close_setting_tip_content),
        onOk: () => {
          close();
        },
        okText: t(Strings.ai_discard_setting_edit_ok_text),
        cancelText: t(Strings.ai_discard_setting_edit_cancel_text),
      });
      return;
    }
    close();
  };

  return (
    <DrawerWrapper open={visible} close={_hide} config={{ title: t(Strings.ai_setting_title), documentLink: '', modalWidth: 640 }}>
      {loading || !setting ? (
        <>
          <Skeleton height="24px" />
          <Skeleton count={2} style={{ marginTop: '24px' }} height="80px" />
        </>
      ) : (
        <ReactSchemaForm
          locale={props.locale}
          schema={setting.JSONSchema}
          uiSchema={setting.UISchema}
          widgets={props.widgets}
          onSubmit={onSubmit}
          formData={setting.data}
          onChange={(data) => {
            if (setting.UISchema) {
              setIsChanged(true);
              if (data.formData.type !== setting.data.type) {
                fetchSetting(data.formData.type);
              } else {
                setSetting({ ...setting, data: data.formData });
              }
            }
          }}
        >
          <footer className={styles.footer}>
            <Button
              htmlType="submit"
              loading={sumbitLoading}
              disabled={!isChanged || loading}
              style={{ width: 140 }}
              color="primary"
            >
              {t(Strings.save)}
            </Button>
          </footer>
        </ReactSchemaForm>
      )}
    </DrawerWrapper>
  );
}
