import { Theme as AntDTheme } from '@rjsf5/antd';
import { withTheme, IChangeEvent } from '@rjsf5/core';
import { RegistryWidgetsType, StrictRJSFSchema, UiSchema, TranslatableString, englishStringTranslator, replaceStringParameters } from '@rjsf5/utils';
import { customizeValidator } from '@rjsf5/validator-ajv8';
import localizer from 'ajv-i18n';
import { FormEvent } from 'react';
import { Message } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import style from './index.module.less';

const Form = withTheme(AntDTheme);

interface IProps {
  children: React.ReactNode;
  onSubmit: (data: IChangeEvent, event: FormEvent<any>) => void;
  onChange: (data: IChangeEvent, id?: string) => void;
  formData: any;
  widgets?: RegistryWidgetsType;
  schema: StrictRJSFSchema;
  uiSchema?: UiSchema;
  locale?: string;
}

export function ReactSchemaForm(props: IProps) {
  const validator = customizeValidator({}, localizer[props.locale || ''] || localizer.en);

  return (
    <Form
      widgets={props.widgets}
      formData={props.formData}
      noHtml5Validate
      idPrefix="form"
      className={style.form}
      schema={props.schema}
      uiSchema={props.uiSchema}
      onSubmit={props.onSubmit}
      onError={(e) => {
        Message.error({ content: e[0].message || 'form validator failed' });
      }}
      validator={validator}
      onChange={props.onChange}
    >
      {props.children}
    </Form>
  );
}
