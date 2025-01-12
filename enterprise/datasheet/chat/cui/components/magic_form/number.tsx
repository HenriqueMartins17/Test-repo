import classNames from 'classnames';
import React, { useMemo, useRef, useState } from 'react';
import { registerCUIComponent, CUIFormPanel, CUIFormSubmit, ICUIChatProps, IMagicFormProps, ICUIForm } from '@apitable/ai';
import { FieldType, SymbolAlign } from '@apitable/core';
import { ExpandNumber } from 'pc/components/expand_record/expand_number';
import styles from './style.module.less';

type Props = IMagicFormProps & ICUIChatProps;

export default function CUIMagicFormNumber(props: Props) {
  const { title, field, datasheetId, defaultValue, description, isComplete } = props;
  const [value, setValue] = useState(defaultValue || null);
  const [value2, setValue2] = useState(defaultValue || null);
  const [isFocus, setIsFocus] = useState(false);
  const ref = useRef(null);
  const [isLoading, setIsLoading] = React.useState(false);

  const isDisabled = useMemo(() => {
    if (field.required) {
      if (value === null) {
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
          [styles.active]: isFocus,
        })}
        onMouseDown={() => {
          // @ts-ignore
          ref.current?.onStartEdit(value);
          setTimeout(() => {
            setIsFocus(true);
          }, 0);
        }}>
        <ExpandNumber
          ref={ref}
          field={field}
          isFocus={isFocus}
          editable
          editing
          cellValue={value2}
          datasheetId={datasheetId}
          width={200}
          height={40}
          onBlur={(e) => {
            setIsFocus(false);
            const val = field.type === FieldType.Percent ? Number(e.target.value) / 100 : Number(e.target.value);
            setValue(val);
            setValue2(val);
          }}
          className={styles.number}
          style={{}}
          onAiFormChange={(v) => {
            const val = field.type === FieldType.Percent ? Number(v) / 100 : Number(v);
            setValue(val);
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

registerCUIComponent('CUIMagicFormNumber', {
  component: CUIMagicFormNumber,
  getDisplayResultWithComponent: (form: ICUIForm, data: any) => {
    if (form.component === 'CUIMagicFormNumber') {
      if (data === null || data === undefined) {
        return '';
      }
      if (form.props.field.type === FieldType.Currency) {
        if (form.props.field.property.symbolAlign === SymbolAlign.right) {
          return data.toFixed(2) + form.props.field.property.symbol;
        }
        return form.props.field.property.symbol + data.toFixed(2);
      } else if (form.props.field.type === FieldType.Percent) {
        return data * 100 + '%';
      }
      return data;
    }
  }
});