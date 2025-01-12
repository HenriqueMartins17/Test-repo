import React from 'react';
import { Button, LinkButton } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import styles from './index.module.less';

interface IProps {
  onSubmit: () => Promise<void>;
  onReset: () => void;
  isDisabled?: boolean;
  isComplete?: boolean;
  submitText?: string;
  resetText?: string;
  showReset?: boolean;
}
export default function CUIFormSubmit(props: IProps) {
  const { submitText = t(Strings.cui_next_text), resetText = 'Reset' } = props;
  const [loading, setLoading] = React.useState(false);
  if (props.isComplete) {
    if (props.showReset) {
      return (
        <div className={styles.button}>
          <LinkButton onClick={props.onReset} underline={false} component="button">
            {resetText}
          </LinkButton>
        </div>
      );
    }
    return null;
  }
  const onSubmit = async () => {
    setLoading(true);
    await props.onSubmit();
    setLoading(false);
  };
  return (
    <div className={styles.button}>
      <Button size="small" disabled={props.isDisabled} onClick={onSubmit} color="primary" loading={loading}>
        {submitText}
      </Button>
    </div>
  );
}
