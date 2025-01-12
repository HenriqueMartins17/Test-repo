import React, { useEffect } from 'react';
import { TextInput } from '@apitable/components';
import { CUIFormPanel, CUIFormSubmit } from '../layout';
import { IInputProps, ICUIChatProps } from '@/shared/cui/types';

type Props = IInputProps & ICUIChatProps;

export default function CUIFormInput(props: Props) {
  const { title, defaultValue, description, isComplete } = props;
  const ref = React.useRef<HTMLInputElement>(null);
  const [value, setValue] = React.useState(defaultValue);
  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading || !value;

  useEffect(() => {
    setTimeout(() => {
      if (!isComplete) {
        ref.current?.focus();
      }
    }, 0);
  }, [isComplete]);

  return (
    <CUIFormPanel title={title} description={description}>
      <TextInput
        onKeyDown={(e) => {
          if (e.key === 'Enter') {
            props.onSubmit(value);
          }
        }}
        disabled={isComplete || isLoading}
        block
        ref={ref}
        value={value}
        onChange={(e) => {
          setValue(e.target.value);
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
