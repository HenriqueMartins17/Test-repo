import React, { Dispatch, SetStateAction } from 'react';
import { useThemeColors } from '@apitable/components';
import { convertStatus2Code } from '../../utils';
import { TaskItem } from '../task_item/task_item';

interface IHistoryTimeListProps {
  setActiveTrainId: Dispatch<SetStateAction<string>>;
  activeTrainId: string;
  data: {
    finishedAt: string;
    trainId: string;
    status: any;
  }[];
}

export const HistoryTimeList: React.FC<IHistoryTimeListProps> = ({ setActiveTrainId, data, activeTrainId }) => {
  const colors = useThemeColors();

  return (
    <div
      className={'vk-w-1/4 vk-p-2'}
      style={{
        backgroundColor: colors.bgCommonDefault,
      }}
    >
      {data?.map((item) => (
        <TaskItem
          key={item.trainId}
          item={{
            taskId: item.trainId,
            createdAt: item.finishedAt,
            robotId: '',
            status: convertStatus2Code(item.status),
          }}
          activeId={activeTrainId}
          hideMoreOperation
          onClick={() => {
            setActiveTrainId(item.trainId);
          }}
        />
      ))}
    </div>
  );
};
