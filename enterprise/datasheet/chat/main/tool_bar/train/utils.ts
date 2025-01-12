import { TrainingItemStatus } from './components/task_item/enum';

export const convertStatus2Code = (status: 'success' | 'failed' | 'training') => {
  if (status === 'success') return TrainingItemStatus.SUCCESS;
  if (status === 'failed') return TrainingItemStatus.ERROR;
  return TrainingItemStatus.RUNNING;
};
