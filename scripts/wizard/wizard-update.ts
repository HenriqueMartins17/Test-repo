import { Vika, IFieldValueMap, IRecord, IGetRecordsReqParams } from '@vikadata/vika';
import * as fs from 'fs';
import { resolve } from 'path';
import { ISetting, ITableConfig, settingConfig } from './setting';

const isString = (value: any): value is string => typeof value === 'string';
const isArray = (value: any): value is any[] => Array.isArray(value);

const convertToObject = (obj: any) => {
  const result = {};

  for (const key in obj) {
    const value = obj[key];
    const keys = key.split('.');

    keys.reduce((acc: any, cur, index) => {
      if (cur.includes('[')) {
        const arrayKey = cur.substring(0, cur.indexOf('['));
        const arrayIndex = cur.substring(cur.indexOf('[') + 1, cur.indexOf(']'));

        if (!acc[arrayKey]) {
          acc[arrayKey] = [];
        }

        if (index === keys.length - 1) {
          acc[arrayKey][arrayIndex] = value;
        } else {
          if (!acc[arrayKey][arrayIndex]) {
            acc[arrayKey][arrayIndex] = {};
          }
        }

        return acc[arrayKey][arrayIndex];
      } else {
        if (index === keys.length - 1) {
          acc[cur] = value;
        } else {
          if (!acc[cur]) {
            acc[cur] = {};
          }
        }

        return acc[cur];
      }
    }, result);
  }

  return result;
}

// 全局缓存
interface CacheRecord {
  id: string;
  dottedObject: object;
}

const recordsCache: { [key: string]: CacheRecord } = {};

// auth
const vika = new Vika({
  token: 'uskM7PcEPftF4wh0Ni1',
  host: 'https://integration.vika.ltd/fusion/v1',
});

/**
 * request data
 * @param datasheetId
 */
async function requestData(datasheetId: string, setting?: IGetRecordsReqParams): Promise<IRecord[]> {
  let records: IRecord[] = [];

  try {
    for await (const eachPageRecords of vika.datasheet(datasheetId).records.queryAll({ ...setting })) {
      records = [...records, ...eachPageRecords];
    }

    return records;
  } catch (err) {
    console.warn('Request data error', err);

    return [];
  }
}

function filter(obj: IFieldValueMap) {
  const ret: IFieldValueMap = {};
  for (let key in obj) {
    key = key.trim();
    if (key && key[0] !== '.') {
      ret[key] = obj[key];
    }
  }
  return ret;
}

/**
 * get datasheets data
 * @param tableConfigs
 */
async function parseTables(tableConfigs: ITableConfig[]): Promise<object> {
  const tableObjects: { [key: string]: object } = {};
  // 处理生成json所需要的多个表格组合数据
  for (const tableConfig of tableConfigs) {
    const recordList: object[] = [];
    // 请求获取表数据
    const records = await requestData(tableConfig.datasheetId, tableConfig.setting);
    console.log('Datasheet:[%s], Row: %d', tableConfig.datasheetName, records.length);
    // 循环行记录
    for (const record of records) {
      const { fields, recordId } = record;
      if (fields.id !== undefined) {
        // 没写id列的，就跳过
        const fieldId = fields.id;
        // 移除 . 开头的用于注释的 key，再解析。
        const dotObj = convertToObject(filter(fields)) as { [key: string]: any};
        // 移除为空的 KEY
        delete dotObj[''];
        // 缓存记录，以recordId作为key存储，为了后置关联数据处理
        recordsCache[recordId] = {
          id: fieldId as string,
          // 缓存起来 (Ref 指针引用)
          dottedObject: dotObj,
        };
        recordList.push(dotObj);
      } else {
        console.warn('行记录不存在id字段，recordId: %s, record: %s', recordId, JSON.stringify(record));
      }
    }
    // 以表名作为key构造json对象或数组
    if (tableConfig.schema === 'array') {
      tableObjects[tableConfig.datasheetName] = recordList;
    } else if (tableConfig.schema === 'object') {
      let bigObject: { [key: string]: any } = {};
      for (const recordObj of recordList) {

        const rObj = recordObj as { [key: string]: any };
        const key = rObj['id'];

        // isPrivateDeployment && privateModeHostTransform(tableConfig.datasheetName, recordObj);
        if (!tableConfig.id) {
          // 不用生成id
          delete rObj['id'];
        }
        bigObject[key] = rObj;
      }
      bigObject = convertToObject(bigObject) as { [key: string]: any };
      delete bigObject[''];
      tableObjects[tableConfig.datasheetName] = bigObject;
    }
  }
  const res = convertToObject(tableObjects) as { [key: string]: any };
  delete res[''];
  return res;
}

/**
 * 处理关联数据
 */
function handleRelateRecord() {
  for (const [recordId, recordObj] of Object.entries(recordsCache)) {
    const rowObject = recordObj.dottedObject as { [key: string]: any };
    for (const [key, cell] of Object.entries(rowObject)) {
      if (isArray(cell)) {
        let rowValue: string[] = [];
        for (const cellValue of cell) {
          if (isString(cellValue) && cellValue.startsWith('rec')) {
            if (Object.keys(recordsCache).includes(cellValue)) {
              rowValue.push(recordsCache[cellValue].id);
            } else {
              console.warn('The linked data not exist: %s', recordId);
            }
          } else if (cell.length === 1) {
            rowValue = cellValue;
            continue;
          } else {
            rowValue.push(cellValue);
          }
        }
        rowObject[key] = rowValue;
      }
    }
  }
}

async function generateJsonFile(config: ISetting) {
  const rootDir = process.cwd();
  const outputPath = resolve(rootDir, '../', config.dirName, config.fileName);
  console.log(`\n========== Start update ${config.fileName} wizards config ============`);
  console.log('Writing file path is: %s', outputPath);
  const begin = +new Date();
  // Get datasheets datas
  const tableData = await parseTables(config.tables);
  // 处理关联表数据替换
  handleRelateRecord();
  const primaryData = fs.readFileSync(outputPath, 'utf-8');
  const primaryJson = JSON.parse(primaryData);
  // 写入文件
  const outputJson = JSON.stringify({
    ...primaryJson,
    ...tableData,
  }, null, 2);
  fs.writeFile(outputPath, outputJson, err => {
    if (err !== null) {
      console.error('Writing failed');
      console.error(err);
      return;
    }
  });
  const end = +new Date();
  console.log('Writing file completed, taking %d seconds', (end - begin) / 1000);
  console.log('========== End ============');
}

/**
 * 生成配置文件
 */
async function main() {
  // 循环配置
  for (const config of settingConfig) {
    await generateJsonFile(config);
  }
}

main();