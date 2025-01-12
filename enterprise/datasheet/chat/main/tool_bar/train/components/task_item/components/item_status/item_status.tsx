import * as React from 'react';
import { useThemeColors } from '@apitable/components';
import { CheckCircleFilled, CheckCircleOutlined, PlayFilled, PlayOutlined, WarnCircleFilled, WarnCircleOutlined } from '@apitable/icons';
import { TrainingItemStatus } from '../../enum';

export const ItemStatus = ({ status, variant = 'outlined' }: { status: TrainingItemStatus; variant: 'outlined' | 'filled' }) => {
  const colors = useThemeColors();
  if (variant === 'outlined') {
    switch (status) {
      case TrainingItemStatus.SUCCESS:
        return <CheckCircleOutlined color={colors.textSuccessDefault} size={16} />;
      case TrainingItemStatus.ERROR:
        return <WarnCircleOutlined color={colors.textWarnDefault} size={16} />;
      case TrainingItemStatus.RUNNING:
        return <PlayOutlined color={colors.textBrandDefault} size={16} />;
      default: {
        return <PlayOutlined color={colors.textBrandDefault} size={16} />;
      }
    }
  }

  return (
    <>
      {status === TrainingItemStatus.RUNNING && <PlayFilled color={colors.textBrandDefault} size={16} />}
      {status === TrainingItemStatus.SUCCESS && <CheckCircleFilled color={colors.textSuccessDefault} size={16} />}
      {status === TrainingItemStatus.ERROR && <WarnCircleFilled color={colors.textWarnDefault} size={16} />}
    </>
  );
};
