import classNames from 'classnames';
import dayjs from 'dayjs';
import React, { useState } from 'react';
import { registerCUIComponent, CUIFormPanel, CUIFormSubmit, IMagicFormProps, ICUIChatProps, ICUIForm } from '@apitable/ai';
import { IDateTimeField, FieldType } from '@apitable/core';
import { DateTimeEditor } from 'pc/components/editors/date_time_editor';
import styles from './style.module.less';

type Props = IMagicFormProps & ICUIChatProps;

export default function CUIMagicFormDateTime(props: Props) {
  const { title, field, defaultValue, description, isComplete, datasheetId } = props;
  const [value, setValue] = useState(defaultValue || null);
  const [isFocus, setIsFocus] = useState(false);
  const ref = React.useRef(null);

  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading || (field.required && value === null);

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
          ref.current?.onStartEdit();
        }}>
        <DateTimeEditor
          editable
          ref={ref}
          editing
          dataValue={value}
          datasheetId={datasheetId}
          width={100}
          style={{
            height: 40,
          }}
          field={field as IDateTimeField}
          height={90}
          commandFn={(v) => {
            setValue(v);
            setTimeout(() => {
              setIsFocus(false);

            }, 100);
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
registerCUIComponent('CUIMagicFormDateTime', {
  component: CUIMagicFormDateTime,
  getDisplayResultWithComponent: (form: ICUIForm, data: any) => {
    if (form.component === 'CUIMagicFormDateTime') {
      if (data === null || data === undefined) {
        return '';
      }
      if (form.props.field.type === FieldType.DateTime) {
        if (form.props.field.property.includeTime) {
          return dayjs(data).format('YYYY-MM-DD HH:mm:ss');
        }
        return dayjs(data).format('YYYY-MM-DD');
      }
      return data;
    }
  }
});