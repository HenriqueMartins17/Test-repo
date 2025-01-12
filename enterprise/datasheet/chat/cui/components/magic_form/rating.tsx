import React, { useState } from 'react';
import { registerCUIComponent, CUIFormPanel, CUIFormSubmit, IMagicFormProps, ICUIChatProps, ICUIForm } from '@apitable/ai';
import { ConfigConstant } from '@apitable/core';
import { RatingEditor } from 'pc/components/editors/rating_editor';
import { emojiUrl } from 'pc/utils';

type Props = IMagicFormProps & ICUIChatProps;

export default function CUIMagicFormRating(props: Props) {
  const { title, field, defaultValue, description, isComplete, datasheetId } = props;
  const [value, setValue] = useState(defaultValue || null);

  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading || (field.required && value === null);

  return (
    <CUIFormPanel
      title={title}
      description={description}
    >
      <RatingEditor
        editable
        editing
        emojiSize={ConfigConstant.CELL_EMOJI_LARGE_SIZE}
        datasheetId={datasheetId}
        width={100}
        style={{}}
        field={field}
        height={90}
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
registerCUIComponent('CUIMagicFormRating', {
  component: CUIMagicFormRating,
  getDisplayResultWithComponent: (form: ICUIForm, data: any) => {
    if (form.component === 'CUIMagicFormRating') {
      const url = emojiUrl(form.props.field.property.icon);
      if (url&&data) {
        const icon: React.ReactNode[] = [];
        for (let i = 0; i < data; i++) {
          icon.push(
            <img
              key={i}
              src={url}
              style={{
                width: ConfigConstant.CELL_EMOJI_LARGE_SIZE,
                height: ConfigConstant.CELL_EMOJI_LARGE_SIZE,
                backgroundColor: 'transparent'
              }}
              alt=""
            />
          );
        }
        return (icon);
      }
    }
    return '';
  }
});