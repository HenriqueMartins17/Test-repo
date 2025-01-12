import { Strings, t } from '@apitable/core';

export const columns = [
  {
    title: '#',
    dataIndex: 'index',
    width: 60,
  },
  {
    title: t(Strings.ai_training_history_table_column_datasource),
    dataIndex: 'datasource',
    ellipsis: true,
    width: 200,
  },
  {
    title: t(Strings.ai_training_history_table_column_type),
    dataIndex: 'type',
    width: 100,
  },
  {
    title: t(Strings.ai_training_history_table_column_total_characters),
    dataIndex: 'characters',
    width: 120,
  },
];
