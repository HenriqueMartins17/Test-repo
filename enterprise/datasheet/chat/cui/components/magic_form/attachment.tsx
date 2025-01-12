import React, { useRef } from 'react';
import {
  registerCUIComponent,
  CUIFormPanel, CUIFormSubmit,
  IMagicFormProps, ICUIChatProps, ICUIForm
} from '@apitable/ai';
import { IAttacheField, IAttachmentValue } from '@apitable/core';
import { ExpandAttachment, ExpandAttachContext } from 'pc/components/expand_record/expand_attachment';

type Props = IMagicFormProps & ICUIChatProps;

export default function CUIMagicFormAttachment(props: Props) {
  const { title, field, defaultValue, description, isComplete, datasheetId } = props;
  const [value, setValue] = React.useState(defaultValue || []);
  const attachmentRef = useRef<IAttachmentValue[]>(value);
  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading || (field.required && value.length === 0);

  return (
    <CUIFormPanel
      title={title}
      description={description}
    >
      <ExpandAttachContext.Provider value={{}}>
        <ExpandAttachment
          editable
          datasheetId={datasheetId}
          field={field as IAttacheField}
          recordId={'atcu2qa74N2iC'}
          cellValue={attachmentRef.current as IAttachmentValue[]}
          getCellValueFn={() => {
            return attachmentRef.current;
          }}
          onClick={(e) => {
            console.log('onClick', e);
          }}
          onSave={(e) => {
            setValue(e);
            attachmentRef.current = e;
          }}
          // onClick={onMouseDown}
        />
      </ExpandAttachContext.Provider>
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

registerCUIComponent('CUIMagicFormAttachment', {
  component: CUIMagicFormAttachment,
  getDisplayResultWithComponent:  (form: ICUIForm, data: any) => {
    if (data.length) {
      return data.map((item: any) => item.name).join(', ');
    }
    return '';
  }
});