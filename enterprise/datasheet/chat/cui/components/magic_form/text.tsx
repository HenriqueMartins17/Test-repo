import classNames from 'classnames';
import React, { useState, useMemo } from 'react';
import { registerCUIComponent, CUIFormPanel, CUIFormSubmit, IMagicFormProps, ICUIChatProps, ICUIForm } from '@apitable/ai';
import { TextEditor } from 'pc/components/editors/text_editor';
import styles from './style.module.less';

type Props = IMagicFormProps & ICUIChatProps;

export default function CUIMagicFormText(props: Props) {
  const { title, field, defaultValue, description, isComplete, datasheetId } = props;
  const [value, setValue] = useState(defaultValue || null);
  const [isFocus, setIsFocus] = useState(false);
  const ref = React.useRef(null);

  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = useMemo(() => {
    if (field.required) {
      if (value === null) {
        return true;
      }
      if (Array.isArray(value) && value.length === 1 && value[0].text === '') {
        return true;
      }
    }
    return isLoading;
  }, [isLoading, field.required, value]);

  return (
    <CUIFormPanel
      title={title}
      description={description}
    >
      <div
        tabIndex={0}
        className={classNames({
          [styles.item]: true,
          [styles.active]: isFocus
        })}
        onMouseDown={() => {
          setIsFocus(true);
          // @ts-ignore
          ref.current?.onStartEdit();
        }}>
        <TextEditor
          editable
          ref={ref}
          editing
          datasheetId={datasheetId}
          width={100}
          style={{}}
          field={field}
          minRows={4}
          height={90}
          needEditorTip={false}
          onChange={(v) => {
            setValue(v);
          }}
          onBlur={() => {
            setIsFocus(false);
          }}
        />
      </div>

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

registerCUIComponent('CUIMagicFormText', {
  component: CUIMagicFormText,
  getDisplayResultWithComponent: (form: ICUIForm, data: any) => {
    if (!data || !data.length) {
      return '';
    }
    return data[0].text;
  }
});