import classNames from 'classnames';
import React, { useState } from 'react';
import { registerCUIComponent, CUIFormPanel, CUIFormSubmit, IMagicFormProps, ICUIChatProps, ICUIForm } from '@apitable/ai';
import { t, Strings } from '@apitable/core';
import { MemberFieldEditor } from 'pc/components/form_container/form_field_container/form_editors';
import styles from './style.module.less';

type Props = IMagicFormProps & ICUIChatProps;

export default function CUIMagicFormMember(props: Props) {
  const { title, field, defaultValue, description, isComplete, datasheetId } = props;
  const [value, setValue] = useState(defaultValue || []);
  const [isFocus, setIsFocus] = useState(false);

  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading || (field.required && value.length === 0);

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
        onClick={() => {
          setIsFocus(true);
          // @ts-ignore
          // ref.current?.onStartEdit();
        }}>
        <MemberFieldEditor
          editable
          isFocus
          // ref={ref}
          editing
          cellValue={value}
          datasheetId={datasheetId}
          width={100}
          style={{
            height: 40,
          }}
          field={field}
          height={90}
          onClose={() => {
            setIsFocus(false);
          }}
          onSave={(v) => {
            setValue(v);
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

registerCUIComponent('CUIMagicFormMember', {
  component: CUIMagicFormMember,
  getDisplayResultWithComponent: (form: ICUIForm, data: any) => {
    return t(Strings.cui_select_user_text, { users: data.length || 0 });
  }
});