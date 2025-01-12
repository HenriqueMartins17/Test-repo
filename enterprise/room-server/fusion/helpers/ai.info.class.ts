import { AiInfoResponse, TrainingStatus } from './ai.server.model';
import { Training } from './training.class';

/**
 * This class is used to get information about AI
 */
export class AiInfo {
  private readonly info: AiInfoResponse;

  constructor(info: AiInfoResponse) {
    this.info = info;
  }

  public isTrainRunning(): boolean {
    return this.info.locking_training_id != null;
  }

  public hasTrained() {
    return this.info.success_train_history && this.info.success_train_history.length > 0;
  }

  public currentTrainingInfo(): Training | undefined {
    return this.info.current_training_info && new Training(this.info.current_training_info);
  }

  public latestSuccessTraining() {
    if (this.hasTrained()) {
      return this.currentTrainingInfo();
    }
    return undefined;
  }

  public latestTraining() {
    if (this.isTrainRunning()) {
      return new Training(this.info.locking_training_info);
    }
    return this.currentTrainingInfo();
  }

  public latestTrainingStatus() {
    if (this.isTrainRunning()) {
      return TrainingStatus.TRAINING;
    }
    const training = this.currentTrainingInfo();
    if (training) {
      return training.status;
    }
    return undefined;
  }
}