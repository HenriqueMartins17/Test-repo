import cls from 'classnames';
import React from 'react';
import { Radio, RadioGroup, Typography, useThemeColors } from '@apitable/components';
import { CUIFormPanel, CUIFormSubmit } from '../layout';
import styles from './index.module.less';
import { ICUIChatProps, IRadioProps } from '@/shared/cui/types';

type Props = IRadioProps & ICUIChatProps;

export default function CUIFormRadio(props: Props) {
  const { title, options, defaultValue, description, isComplete } = props;
  const [value, setValue] = React.useState(defaultValue || null);
  const colors = useThemeColors();
  const [isLoading, setIsLoading] = React.useState(false);
  const isDisabled = isLoading || value === null;

  return (
    <CUIFormPanel title={title} description={description}>
      <RadioGroup
        value={value}
        disabled={isComplete || isLoading}
        onChange={(e, val) => {
          setValue(val);
        }}
      >
        {options.map((item) => {
          return (
            <div
              key={item.value}
              className={cls({
                [styles.radio]: true,
                [styles.disabled]: isComplete,
              })}
              onClick={() => {
                if (!isComplete) setValue(item.value);
              }}
            >
              <Radio value={item.value}>
                <Typography variant="body2">{item.label}</Typography>
                <Typography variant="body4" color={colors.textCommonTertiary} style={{ marginTop: 2 }}>
                  {item.desc}
                </Typography>
              </Radio>
            </div>
          );
        })}
      </RadioGroup>

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
