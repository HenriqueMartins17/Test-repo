import { useEffect, useMemo, useState } from 'react';
import { useSelector } from 'react-redux';
import useSWR from 'swr';
import { useThemeColors } from '@apitable/components';
import { Api, Strings, t } from '@apitable/core';
import { ConfigItem } from '../../../config_item/config_item';
import { getHistory } from '../../api';
import { HistoryDetailTable } from '../history_detail_table';
import { HistoryTimeList } from '../history_time_list';

export const History = () => {
  const colors = useThemeColors();
  // const { aiId } = useSelector(state => state.pageParams);
  const { data } = useSWR(`/ai/${123}/trainings`, (path: string) => getHistory(path!), {
    revalidateOnFocus: false,
  });
  const [activeTrainId, setActiveTrainId] = useState('');

  useEffect(() => {
    if (activeTrainId || !data) return;
    setActiveTrainId(data?.[0].trainingId ?? '');
  }, [data]);

  const historyTimeList = useMemo(() => {
    return data?.map((item: any) => {
      return {
        finishedAt: item.finishedAt,
        trainId: item.trainingId,
        status: item.status,
        startedAt: item.startedAt,
        creditCost: item.creditCost,
      };
    });
  }, [data]);

  const tableData = useMemo(() => {
    if (!data) return [];
    const datasource = data.find((item: any) => item.trainingId === activeTrainId);

    if (!datasource || !datasource.dataSources) return [];
    return datasource.dataSources.map((item: any, index: any) => {
      return {
        index: index + 1,
        type: item.type,
        words: item.words,
        datasource: item.typeId,
        characters: item.characters,
      };
    });
  }, [data, activeTrainId]);

  const trainInfo = useMemo(() => {
    return historyTimeList?.find((item: any) => item.trainId === activeTrainId) || {};
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
