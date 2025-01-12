import { Table } from 'antd';
import dayjs from 'dayjs';
import { Typography, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { TimeOutlined } from '@apitable/icons';
import { columns } from './config';
import styles from './style.module.less';
import { ITrainingHistoryItem } from '@/shared';

interface IConfigItemProps {
  titleConfig: {
    icon: React.ReactElement;
    str: string;
  };
  content: React.ReactElement;
}

const ConfigItem: React.FC<IConfigItemProps> = ({ titleConfig, content }) => {
  const colors = useThemeColors();
  return (
    <div className={'vk-flex vk-flex-col vk-space-y-2'}>
      <div className={'vk-flex vk-items-center'}>
        {/*{titleConfig.icon}*/}
        <Typography variant={'body2'} color={colors.textCommonTertiary}>
          {titleConfig.str}
        </Typography>
      </div>
      {content}
    </div>
  );
};

interface IHistoryDetailTableProps {
  currentTraining: ITrainingHistoryItem | null;
}

export const HistoryDetailTable = (props: IHistoryDetailTableProps) => {
  const colors = useThemeColors();

  if (!props.currentTraining) return null;

  return (
    <div className={'vk-p-4 vk-w-3/4'}>
      <div className={'vk-flex vk-items-center vk-space-x-1'}>
        <Typography color={colors.textCommonPrimary} variant={'h7'}>
          train_id:
        </Typography>
        <Typography color={colors.textCommonPrimary} variant={'body2'}>
          {props.currentTraining.trainingId}
        </Typography>
      </div>
      <div className={'vk-flex vk-my-4 vk-space-x-10'}>
        <ConfigItem
          titleConfig={{
            icon: <TimeOutlined />,
            str: t(Strings.start_time),
          }}
          content={<Typography variant={'body2'}>{dayjs(props.currentTraining.startedAt).format('YYYY-MM-DD HH:mm:ss')}</Typography>}
        />
        <ConfigItem
          titleConfig={{
            icon: <TimeOutlined />,
            str: t(Strings.bind_state),
          }}
          content={<Typography variant={'body2'}>{props.currentTraining.status}</Typography>}
        />
        <ConfigItem
          titleConfig={{
            icon: <TimeOutlined />,
            str: 'Credit Cost',
          }}
          content={<Typography variant={'body2'}>{props.currentTraining.creditCost}</Typography>}
        />
      </div>
      <div className={styles.tableWrapper}>
        <Table columns={columns} dataSource={props.currentTraining.dataSources} pagination={{ hideOnSinglePage: true }} style={{ minHeight: 338 }} />
      </div>
    </div>
  );
};
