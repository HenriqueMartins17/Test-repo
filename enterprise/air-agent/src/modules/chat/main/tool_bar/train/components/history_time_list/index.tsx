import React, { Dispatch, SetStateAction } from 'react';
import { useThemeColors } from '@apitable/components';
// import { TaskItem } from 'pc/components/automation/run_history/list';
// import { IRunHistoryDatum } from 'pc/components/robot/robot_detail/robot_run_history';
import { convertStatus2Code } from '../../utils';

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
      {/* {data?.map((item) => (
        <TaskItem
          key={item.trainId}
          item={
            // {
            //   taskId: item.trainId,
            //   createdAt: item.finishedAt,
            //   robotId: '',
            //   status: convertStatus2Code(item.status),
            //   executedActions: [],
            // } as unknown as IRunHistoryDatum
            {}
          }
          activeId={activeTrainId}
          hideMoreOperation
          onClick={() => {
            setActiveTrainId(item.trainId);
          }}
        />
      ))} */}
    </div>
  );
};
