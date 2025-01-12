import classNames from 'classnames';
import React, { useState } from 'react';
import { registerCUIComponent, CUIFormPanel, CUIFormSubmit, IMagicFormProps, ICUIChatProps, ICUIForm } from '@apitable/ai';
import { EnhanceTextEditor } from 'pc/components/editors/enhance_text_editor';
import styles from './style.module.less';

type Props = IMagicFormProps & ICUIChatProps;

export default function CUIMagicFormEnhanceText(props: Props) {
  const { title, field, defaultValue, description, isComplete, datasheetId } = props;
  const [value, setValue] = useState(defaultValue || null);
  const [isFocus, setIsFocus] = useState(false);

  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading || (field.required && value === null);

  return (
    <CUIFormPanel
      title={title}
      description={description}
    >
      <div
        tabIndex={0}
        onBlur={() => {
          setIsFocus(false);
        }}
        className={classNames({
          [styles.item]: true,
          [styles.active]: isFocus
        })}
        onClick={() => {
          setIsFocus(true);
        }}>

        <EnhanceTextEditor
          editable
          isForm
          recordId="0"
          editing
          datasheetId={datasheetId}
          width={100}
          style={{}}
          field={field}
          height={90}
          onChange={(v) => {
            setValue(v);
          }}
        />
      </div>

      <CUIFormSubmit
        {...props}
        isComplete={isComplete}
        isDisabled={isDisabled}
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

registerCUIComponent('CUIMagicFormEnhanceText', {
  component: CUIMagicFormEnhanceText,
  getDisplayResultWithComponent: (form: ICUIForm, data: any) => {
    if (!data || !data.length) {
      return '';
    }
    return data[0].text;
  }
});