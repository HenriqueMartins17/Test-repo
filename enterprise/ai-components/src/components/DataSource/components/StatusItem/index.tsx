import { COLOR_CONFIG, DataSourceStatus } from '../../enum';

interface IStatusItemProps {
  status: DataSourceStatus;
}

export const StatusItem: React.FC<IStatusItemProps> = ({ status }) => {
  const config = COLOR_CONFIG[status];
  return (
    <div
      style={{
        color: config.color,
        border: `1px solid ${config.borderColor}`,
        backgroundColor: config.bgColor,
        borderRadius: '4px',
        padding: '2px 8px',
        width: 'fit-content',
      }}
    >
      {config.content}
    </div>
  );
};
