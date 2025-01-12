import { useThemeColors } from '@apitable/components';
import { ITrainingHistoryItem, ITraningHistoryResponse } from '../../../../shared';
import { convertStatus2Code } from '../../utils';
import { TaskItem } from '../task_item/task_item';

interface IHistryTimeListProps {
  trainingHistory: ITraningHistoryResponse;
  currentTraining: ITrainingHistoryItem | null;
  setCurrentTraining: any;
}

export const HistoryTimeList = (props: IHistryTimeListProps) => {
  const colors = useThemeColors();

  return (
    <div
      className={'vk-w-1/4 vk-p-2'}
      style={{
        backgroundColor: colors.bgCommonDefault,
      }}
    >
      {props.trainingHistory.map((item) => (
        <TaskItem
          key={item.trainingId}
          item={{
            taskId: item.trainingId,
            createdAt: new Date(item.startedAt).toISOString(),
            robotId: '',
            status: convertStatus2Code(item.status),
          }}
          activeId={props.currentTraining?.trainingId}
          hideMoreOperation
          onClick={() => {
            props.setCurrentTraining(item);
          }}
        />
      ))}
    </div>
  );
};
