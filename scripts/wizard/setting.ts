import { IGetRecordsReqParams } from '@vikadata/vika';

export interface ITableConfig {
  datasheetId: string;
  datasheetName: string;
  schema: 'array' | 'object';
  id?: boolean;
  setting?: IGetRecordsReqParams;
}

export interface ISetting {
  dirName: string;
  fileName: string;
  tables: ITableConfig[];
}

export const settingConfig: ISetting[] = [
  {
    dirName: 'vikadata/apitable/packages/core/src/config',
    fileName: 'system_config.source.json',
    tables: [
      {
        datasheetId: 'dst271nYuQMQqfjzfk',
        datasheetName: 'player.trigger',
        schema: 'array',
        setting: {
          sort: [JSON.stringify({ order: 'asc', field: 'id' }) as any],
        },
      },
      {
        datasheetId: 'dstMyrDBCfMUXg7sFs',
        datasheetName: 'player.events',
        schema: 'object',
        setting: {
          sort: [JSON.stringify({ order: 'asc', field: 'id' }) as any],
        },
      },
      {
        datasheetId: 'dstnPV0UZN6joLFKBM',
        datasheetName: 'player.rule',
        schema: 'array',
        setting: {
          sort: [JSON.stringify({ order: 'asc', field: 'id' }) as any],
        },
      },
      {
        datasheetId: 'dst3VM6j7VvaDziNhm',
        datasheetName: 'player.jobs',
        schema: 'object',
        setting: {
          sort: [JSON.stringify({ order: 'asc', field: 'id' }) as any],
        },
      },
      {
        datasheetId: 'dst7Wz9N98PYTpeoHi',
        datasheetName: 'player.action',
        schema: 'array',
        setting: {
          sort: [JSON.stringify({ order: 'asc', field: 'id' }) as any],
        },
      },
      {
        datasheetId: 'dstmCi0wFNmUSXwobM',
        datasheetName: 'player.tips',
        schema: 'object',
        setting: {
          sort: [JSON.stringify({ order: 'asc', field: 'id' }) as any],
        },
      },
      {
        datasheetId: 'dstgkcUKo1CfsSsBFM',
        datasheetName: 'guide.wizard',
        schema: 'object',
        setting: {
          sort: [JSON.stringify({ order: 'asc', field: 'id' }) as any],
        },
      },
      {
        datasheetId: 'dstcVUAsnLwE6yW1VG',
        datasheetName: 'guide.step',
        schema: 'object',
        setting: {
          sort: [JSON.stringify({ order: 'asc', field: 'id' }) as any],
        },
      }
    ],
  },
];
