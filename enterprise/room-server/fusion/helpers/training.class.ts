import { DataSet, TrainingInfo, TrainingStatus } from './ai.server.model';

export class Training {
  id: string;
  status?: TrainingStatus;
  startedAt?: number;
  completedAt?: number;
  dataSets?: DataSet[];

  constructor(trainingInfo: TrainingInfo) {
    this.id = trainingInfo.training_id;
    this.status = trainingInfo.status;
    this.startedAt = trainingInfo.started_at && trainingInfo.started_at * 1000;
    this.completedAt = trainingInfo.finished_at && trainingInfo.finished_at * 1000;
    this.dataSets = trainingInfo.data_sources;
  }

  public latestTrainingChars(): number {
    if (!this.dataSets) {
      return 0;
    }
    const dataSet = this.dataSets[0];
    if (dataSet) {
      return dataSet.characters;
    }
    return 0;
  }
}