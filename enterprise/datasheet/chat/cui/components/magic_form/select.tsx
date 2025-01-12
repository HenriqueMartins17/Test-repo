import React from 'react';
import { registerCUIComponent, CUIFormPanel, CUIFormSubmit, IMagicFormProps, ICUIChatProps, ICUIForm } from '@apitable/ai';
import { OptionFieldEditor } from 'pc/components/form_container/form_field_container/form_editors';

type Props = IMagicFormProps & ICUIChatProps;

export default function CUIMagicFormSelect(props: Props) {
  const { title, field, datasheetId, defaultValue, description, isComplete } = props;
  const [value, setValue] = React.useState(defaultValue || []);
  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading || (field.required && value.length === 0);

  return (
    <CUIFormPanel
      title={title}
      description={description}
    >
      <OptionFieldEditor
        field={field}
        cellValue={value}
        datasheetId={datasheetId}
        width={200}
        height={40}
        style={{
          padding: '0 16px',
          // height: isMobile ? 48 : 40,
        }}
        onChange={(v) => {
          setValue(v);
        }}
      />

      <CUIFormSubmit
        {...props}
        isDisabled={isDisabled}
        isComplete={isComplete}
        onSubmit={async () => {
          setIsLoading(true);
          await props.onSubmit(value);
          setIsLoading(false);
        }}
        onReset={() => {
          props.onReset();
          setValue(null);
        }}
      />
    </CUIFormPanel>
  );
}

registerCUIComponent('CUIMagicFormSelect', {
  component: CUIMagicFormSelect,
  getDisplayResultWithComponent: (form: ICUIForm, data: any) => {
    if (form.component === 'CUIMagicFormSelect') {
      if (typeof data === 'string') {
        const find = form.props.field.property.options.find((option: any) => option.id === data);
        if (find) {
          return find.name;
        }
        return data;
      }
      const ret = data.map((item: any) => {
        const find = form.props.field.property.options.find((option: any) => option.id === item);
        if (find) {
          item = find.name;
        }
        return item;
      }).join(', ');
      return ret;
    }
  }
});