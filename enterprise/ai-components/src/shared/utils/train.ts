import { IServerResponse } from '@apitable/core';
import { TrainingStatus } from '@/shared/enum';
import { IGetAITrainingStatusResponse } from '@/shared/types';

export const pollingTrainingStatus = (
  cb: (status: TrainingStatus) => void,
  fetch: () => Promise<IServerResponse<IGetAITrainingStatusResponse>>,
) => {
  let setTimeoutId: number | null = null;
  let abort = false;
  async function poll() {
    try {
      // getAITrainingStatus
      const res = await fetch();
      if (res.data.status !== TrainingStatus.TRAINING && res.data.status !== TrainingStatus.NEW) {
        cb(res.data.status);
      } else {
        if (!abort) setTimeoutId = window.setTimeout(poll, 1000);
      }
    } catch (e) {
      console.error(e);
      if (!abort) setTimeoutId = window.setTimeout(poll, 1000);
    }
  }
  poll();
  return () => {
    abort = true;
    if (setTimeoutId) {
      clearTimeout(setTimeoutId);
    }
  };
};
