import React, { useState } from 'react';
import { registerCUIComponent, CUIFormPanel, CUIFormSubmit, IMagicFormProps, ICUIChatProps, ICUIForm } from '@apitable/ai';
import { t, Strings, ILinkIds, ILinkField } from '@apitable/core';
import { ExpandLink, FetchForeignTimes } from 'pc/components/expand_record/expand_link';

type Props = IMagicFormProps & ICUIChatProps;

export default function CUIMagicFormLink(props: Props) {
  const { title, field, defaultValue, description, isComplete, datasheetId } = props;
  const [value, setValue] = useState(defaultValue || []);

  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading || (field.required && value.length === 0);

  return (
    <CUIFormPanel
      title={title}
      description={description}
    >
      <ExpandLink
        style={{}}
        editable
        editing
        width={100}
        height={90}
        datasheetId={datasheetId}
        recordId={datasheetId}
        field={field as ILinkField}
        onClick={() => {
          // console.log('click');
        }}
        onSave={(v) => {
          setValue(v);
        }}
        cellValue={value as ILinkIds}
        addBtnText={t(Strings.form_field_add_btn)}
        rightLayout={false}
        manualFetchForeignDatasheet={FetchForeignTimes.OnlyOnce}
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
registerCUIComponent('CUIMagicFormLink', {
  component: CUIMagicFormLink,
  getDisplayResultWithComponent: (form: ICUIForm, data: any) => {
    return t(Strings.cui_select_link_text, { links: data.length || 0 });
  }
});