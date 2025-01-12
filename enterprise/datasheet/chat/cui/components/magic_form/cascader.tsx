import classNames from 'classnames';
import React, { useState } from 'react';
import { registerCUIComponent, CUIFormPanel, CUIFormSubmit, IMagicFormProps, ICUIChatProps, ICUIForm } from '@apitable/ai';
import { CascaderEditor } from 'pc/components/editors/cascader_editor';
import styles from './style.module.less';

type Props = IMagicFormProps & ICUIChatProps;

export default function CUIMagicFormCascader(props: Props) {
  const { title, field, defaultValue, description, isComplete, datasheetId } = props;
  const [value, setValue] = useState(defaultValue || []);
  const [isFocus, setIsFocus] = useState(false);
  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading || (field.required && value.length === 0);

  const ref = React.useRef(null);
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
        }}>

        <CascaderEditor
          editable
          ref={ref}
          showSearch={false}
          editing={isFocus}
          onSave={(v) => {
            setValue(v);
            setIsFocus(false);
            // @ts-ignore
            ref.current?.onStartEdit(v);
          }}
          datasheetId={datasheetId}
          width={0}
          style={{
            // height: 40,
            // lineHeight: '40px',
          }}
          field={field}
          height={40}
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

registerCUIComponent('CUIMagicFormCascader', {
  component: CUIMagicFormCascader,
  getDisplayResultWithComponent:  (form: ICUIForm, data: any) => {
    if (!data || !data.length) {
      return '';
    }
    return data[0].text;
  }
});