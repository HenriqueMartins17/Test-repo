import { TrainingItemStatus } from './enum';

export interface ITrainingTaskItem {
  robotId: string;
  taskId: string;
  createdAt: string;
  status: TrainingItemStatus;
}
