import axios from 'axios';
import { useEffect, useMemo, useState } from 'react';
import useSWR from 'swr';
import { ConfigItem } from '@apitable/ai';
import { useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { HistoryDetailTable } from './components/history_detail_table';
import { HistoryTimeList } from './components/history_time_list';

interface IHistoryProps {
  aiId: string;
}

export const History: React.FC<IHistoryProps> = ({ aiId }) => {
  const colors = useThemeColors();
  const { data } = useSWR(`/ai/${aiId}/trainings`, (path: string) => axios.get(path!).then((res) => res.data.data), {
    revalidateOnFocus: false,
  });
  const [activeTrainId, setActiveTrainId] = useState('');

  useEffect(() => {
    if (activeTrainId || !data) return;
    setActiveTrainId(data?.[0].trainingId ?? '');
  }, [activeTrainId, data]);

  const historyTimeList = useMemo(() => {
    return (
      (data as any)?.map((item) => {
        return {
          finishedAt: item.finishedAt,
          trainId: item.trainingId,
          status: item.status,
          startedAt: item.startedAt,
          creditCost: item.creditCost,
        };
      }) ?? []
    );
  }, [data]);

  const tableData = useMemo(() => {
    if (!data) return [];
    const datasource = (data as any).find((item) => item.trainingId === activeTrainId);

    if (!datasource) return [];
    return (
      datasource.dataSources?.map((item, index) => {
        return {
          index: index + 1,
          type: item.type,
          words: item.words,
          datasource: item.typeId,
          characters: item.characters,
        };
      }) ?? []
    );
  }, [data, activeTrainId]);

  const trainInfo = useMemo(() => {
    return historyTimeList?.find((item) => item.trainId === activeTrainId) || {};
  }, [historyTimeList, activeTrainId]);

  return (
    <div>
      <ConfigItem configTitle={t(Strings.ai_training_history_title)} description={t(Strings.ai_training_history_desc)}>
        <div
          className={'vk-flex vk-p-4 vk-rounded-lg'}
          style={{
            backgroundColor: colors.bgCommonLower,
          }}
        >
          <HistoryTimeList setActiveTrainId={setActiveTrainId} activeTrainId={activeTrainId} data={historyTimeList} />
          <HistoryDetailTable trainInfo={trainInfo} tableData={tableData} />
        </div>
      </ConfigItem>
    </div>
  );
};
