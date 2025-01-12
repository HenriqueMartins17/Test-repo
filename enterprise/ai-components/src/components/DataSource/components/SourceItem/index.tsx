import {Typography, useThemeColors} from '@apitable/components';

interface ISourceItemProps {
  type?: string;
  fileName: string;
  extraName: string;
}

export const SourceItem: React.FC<ISourceItemProps> = ({ fileName, extraName }) => {
  const colors = useThemeColors()
  return (
    <div>
      <div>
        <Typography variant={'body3'} >123</Typography>
      </div>
      {extraName && <Typography variant={'body4'} color={colors.textCommonTertiary}>123</Typography>}
    </div>
  );
};
