import { DatasheetOutlined } from '@apitable/icons';
import { SourceType } from '../enum';

interface ISourceIconProps {
  type: SourceType;
}

export const SourceIcon: React.FC<ISourceIconProps> = ({ type }) => {
  if (type === SourceType.DATASHEET) {
    return <DatasheetOutlined />;
  }

  return null;
};
