import React from 'react';
import { DropdownSelect } from '@apitable/components';
import { CUIFormPanel, CUIFormSubmit } from '../layout';
import { ISelectProps, ICUIChatProps } from '@/shared/cui/types';

type Props = ISelectProps & ICUIChatProps;

export default function CUIFormSelect(props: Props) {
  const { title, options, defaultValue, description, isComplete } = props;
  const [value, setValue] = React.useState(defaultValue || null);
  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading || value === null;

  return (
    <CUIFormPanel title={title} description={description}>
      <DropdownSelect
        disabled={isLoading || isDisabled}
        value={value}
        options={options}
        placeholder={props.placeholder}
        onSelected={(item) => {
          setValue(item.value);
        }}
      />

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
