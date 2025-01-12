import { TrainingItemStatus } from './components/task_item/enum';
import { TrainingStatus } from '@/shared';

export const convertStatus2Code = (status: TrainingStatus) => {
  if (status === 'success') return TrainingItemStatus.SUCCESS;
  if (status === 'failed') return TrainingItemStatus.ERROR;
  return TrainingItemStatus.RUNNING;
};
