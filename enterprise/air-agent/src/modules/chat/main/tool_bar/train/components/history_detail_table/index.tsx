import { Table } from 'antd';
import dayjs from 'dayjs';
import { Typography, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { TimeOutlined } from '@apitable/icons';
import { columns } from './config';

interface IConfigItemProps {
  titleConfig: {
    icon: React.ReactElement;
    str: string
  }
  content: React.ReactElement;
}

const ConfigItem: React.FC<IConfigItemProps> = ({ titleConfig, content }) => {
  const colors = useThemeColors();
  return <div className={'vk-flex vk-flex-col vk-space-y-2'}>
    <div className={'vk-flex vk-items-center vk-space-x-1'}>
      {/*{titleConfig.icon}*/}
      <Typography variant={'body2'} color={colors.textCommonTertiary}>
        {titleConfig.str}
      </Typography>
    </div>
    {content}
  </div>;
};

interface IHistoryDetailTableProps {
  tableData: {
    index: number;
    type: string;
    tokens: number;
    datasource: string;
    characters: number;
  }[];
  trainInfo: {
    finishedAt: string;
    trainId: string;
    startedAt: string;
    status: string;
    creditCost: number;
  }
}

export const HistoryDetailTable: React.FC<IHistoryDetailTableProps> = ({ tableData, trainInfo }) => {
  const colors = useThemeColors();
  const totalCharacters = tableData.reduce((pre, cur) => {
    return pre + cur.characters;
  }, 0);
  console.log(trainInfo);

  return <div className={'vk-p-4 vk-w-3/4'}>
    <div className={'vk-flex vk-items-center vk-space-x-1'}>
      <Typography color={colors.textCommonPrimary} variant={'h7'}>
        train_id:
      </Typography>
      <Typography color={colors.textCommonPrimary} variant={'body2'}>
        {trainInfo.trainId}
      </Typography>
    </div>
    <div className={'vk-flex vk-my-4 vk-justify-between'}>
      <ConfigItem
        titleConfig={{
          icon: <TimeOutlined />,
          str: t(Strings.ai_training_history_train_info_create_time),
        }}
        content={
          <Typography variant={'body2'}>
            {dayjs(trainInfo.startedAt).format('YYYY-MM-DD HH:mm:ss')}
          </Typography>
        }
      />
      <ConfigItem
        titleConfig={{
          icon: <TimeOutlined />,
          str: t(Strings.ai_training_history_train_info_status),
        }}
        content={
          <Typography variant={'body2'}>
            {trainInfo.status}
          </Typography>
        }
      />
      <ConfigItem
        titleConfig={{
          icon: <TimeOutlined />,
          str: t(Strings.ai_training_history_train_info_credit_cost),
        }}
        content={
          <Typography variant={'body2'}>
            {trainInfo.creditCost}
          </Typography>
        }
      />
    </div>
    <Table columns={columns} dataSource={tableData} pagination={{ hideOnSinglePage: true }} style={{ minHeight: 338 }} />
  </div>;
};
