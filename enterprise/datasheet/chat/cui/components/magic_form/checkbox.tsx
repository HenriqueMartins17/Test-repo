import classNames from 'classnames';
import React, { useState } from 'react';
import { registerCUIComponent, CUIFormPanel, CUIFormSubmit, IMagicFormProps, ICUIChatProps } from '@apitable/ai';
import { CheckboxEditor } from 'pc/components/editors/checkbox_editor';
import styles from './style.module.less';

type Props = IMagicFormProps & ICUIChatProps;

export default function CUIMagicFormCheckbox(props: Props) {
  const { title, field, defaultValue, description, isComplete, datasheetId } = props;
  const [value, setValue] = useState(defaultValue || false);
  // const [isFocus, setIsFocus] = useState(false);
  const ref = React.useRef(null);

  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading;

  return (
    <CUIFormPanel
      title={title}
      description={description}
    >
      <div
        tabIndex={0}
        className={classNames({
          [styles.item]: true,
        })}
        style={{ width: 50 }}
        onClick={() => {
          // setIsFocus(true);
          // @ts-ignore
          // ref.current?.onStartEdit();
        }}>
        <CheckboxEditor
          editable
          ref={ref}
          editing
          cellValue={value}
          datasheetId={datasheetId}
          width={0}
          style={{
            height: 40,
          }}
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
          await props.onSubmit(!!value);
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
registerCUIComponent('CUIMagicFormCheckbox', {
  component: CUIMagicFormCheckbox,

});