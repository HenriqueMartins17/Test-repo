import { Table as AntTable } from 'antd';
import { COLUMNS } from '../../config';
import { DataSourceStatus } from '../../enum';
import { ButtonArea } from '../ButtonArea';
import { SourceItem } from '../SourceItem';
import { StatusItem } from '../StatusItem';
import { TimeItem } from '../TimeItem';
import styles from './style.module.less';

const data = [
  {
    index: <SourceItem fileName={'123'} extraName={'123'} />,
    datasource: <TimeItem time={2928457616201} />,
    type: <StatusItem status={DataSourceStatus.Training} />,
    characters: <ButtonArea onDelete={() => {}} />,
  },
  {
    index: '1',
    datasource: <div style={{ color: 'red' }}>12312312</div>,
    type: 32,
    characters: 32,
  },
];

interface ITableProps {
  setIsTraining: (isTraining: boolean) => void;
}

export const Table: React.FC<ITableProps> = ({ setIsTraining }) => {
  return (
    <div className={styles.tableWrapper}>
      <AntTable columns={COLUMNS} dataSource={data} />
    </div>
  );
};
