import { Strings, t } from '@apitable/core';

export const COLUMNS = [
  {
    title: t(Strings.training_data_source_table_column_1),
    dataIndex: 'index',
    width: 60,
  },
  {
    title: t(Strings.training_data_source_table_column_2),
    dataIndex: 'datasource',
    ellipsis: true,
    width: 200,
  },
  {
    title: t(Strings.training_data_source_table_column_3),
    dataIndex: 'type',
    width: 100,
  },
  {
    title: t(Strings.training_data_source_table_column_4),
    dataIndex: 'characters',
    width: 200,
  },
];
