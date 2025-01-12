import { useEffect, useState } from 'react';
import { useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { ITrainingHistoryItem, ITraningHistoryResponse, useAIContext } from '../../shared';
import { ConfigItem } from '../ConfigItem/index';
import { HistoryDetailTable } from './components/history_detail_table';
import { HistoryTimeList } from './components/history_time_list';

interface IHistoryProps {
  aiId: string;
}

export const TrainingHistory: React.FC<IHistoryProps> = ({ aiId }) => {
  const colors = useThemeColors();
  const { context } = useAIContext();

  const [trainingHistory, setTrainingHistory] = useState<ITraningHistoryResponse>([]);
  const [currentTraining, setCurrentTraining] = useState<ITrainingHistoryItem | null>(null);

  const getTraningHistoryRecord = async () => {
    const response = await context.api.getTrainingHistory(aiId);
    if (response?.data?.length) {
      setTrainingHistory(response.data);
      setCurrentTraining(response.data[0]);
    }
  };

  useEffect(() => {
    getTraningHistoryRecord();
  }, []);

  console.log('trainingHistory', trainingHistory);
  console.log('currentTraining', currentTraining);

  return (
    <div>
      <ConfigItem configTitle={t(Strings.ai_training_history_title)} description={t(Strings.ai_training_history_desc)}>
        <div
          className={'vk-flex vk-p-4 vk-rounded-lg'}
          style={{
            backgroundColor: colors.bgCommonLower,
          }}
        >
          <HistoryTimeList trainingHistory={trainingHistory} currentTraining={currentTraining} setCurrentTraining={setCurrentTraining} />
          <HistoryDetailTable currentTraining={currentTraining} />
        </div>
      </ConfigItem>
    </div>
  );
};
