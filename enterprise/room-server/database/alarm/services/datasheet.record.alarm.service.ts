/**
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

import { FieldType, IField, IFieldExtraMapValue, IRecord, IRecordAlarm, IRecordMap } from '@apitable/core';
import { Injectable } from '@nestjs/common';
import { DatasheetRecordAlarmBaseService } from 'database/alarm/datasheet.record.alarm.base.service';
import { DatasheetRecordAlarmEntity } from 'database/alarm/entities/datasheet.record.alarm.entity';
import { DatasheetRecordEntity } from 'database/datasheet/entities/datasheet.record.entity';
import { DatasheetRecordService } from 'database/datasheet/services/datasheet.record.service';
import { ICommonData } from 'database/ot/interfaces/ot.interface';
import dayjs, { ManipulateType } from 'dayjs';
import { isEmpty } from 'lodash';
import { InjectLogger } from 'shared/common';
import { RecordAlarmStatus } from 'shared/enums/record.alarm.enum';
import { IdWorker } from 'shared/helpers';
import { EntityManager } from 'typeorm';
import { Logger } from 'winston';
import { DatasheetRecordAlarmRepository } from '../repositories/datasheet.record.alarm.repository';

@Injectable()
export class DatasheetRecordAlarmService extends DatasheetRecordAlarmBaseService {

  constructor(
    @InjectLogger() private readonly logger: Logger,
    private recordService: DatasheetRecordService,
    private repository: DatasheetRecordAlarmRepository,
  ) {
    super();
  }

  public override async getCurrentActivatedRecordAlarms(intervalSecond: number): Promise<DatasheetRecordAlarmEntity[] | null> {
    const endTime = dayjs();
    const startTime = endTime.subtract(intervalSecond, 's');
    return await this.repository.selectRecordAlarmByStatusAndTimeRange(
      RecordAlarmStatus.PENDING, startTime.toDate(), endTime.toDate()
    );
  }

  private async getRecordAlarmsByRecordIdsAndFieldIds(
    dstId: string,
    recordIds: string[],
    fieldIds: string[]
  ): Promise<DatasheetRecordAlarmEntity[]> {
    if (isEmpty(recordIds) || isEmpty(fieldIds)) return [];
    return await this.repository.selectRecordAlarmByRecordIdsAndFieldIds(dstId, recordIds, fieldIds);
  }

  override async handleRecordAlarms(
    manager: EntityManager,
    commonData: ICommonData,
    resultSet: { [key: string]: any },
  ) {
    const { dstId } = commonData;
    const profiler = this.logger.startTimer();
    this.logger.info(`[${dstId}] ====> Start processing alarm`);

    // delete alarms
    await this.deleteRecordAlarms(manager, commonData, resultSet);
    // create alarms
    await this.createRecordAlarms(manager, commonData, resultSet);
    // TODO(wuchen) refactor: should be handled by event in the future
    // update alarms, collect record alarms requiring update in CellValue changes of datatime fields from replaceCellMap
    await this.updateRecordAlarms(manager, commonData, resultSet);

    profiler.done({ message: `[${dstId}] ====> Finished processing alarm` });
  }

  private async createRecordAlarms(
    manager: EntityManager,
    commonData: ICommonData,
    resultSet: { [key: string]: any },
  ) {
    if (!resultSet.toCreateAlarms.size) return;
    const { userId, spaceId, dstId } = commonData;

    const profiler = this.logger.startTimer();
    this.logger.info(`Starting creating alarms. Size: ${resultSet.toCreateAlarms.size}`);

    const involvedRecordIds = Array.from(resultSet.toCreateAlarms.keys()) as string[];
    const involvedRecordMap: IRecordMap = await this.recordService.getBasicRecordsByRecordIds(dstId, involvedRecordIds);

    const newAlarmEntities = involvedRecordIds.reduce((acc, recordId: string) => {
      resultSet.toCreateAlarms.get(recordId).forEach((alarm: IRecordAlarm) => {
        const record = involvedRecordMap[recordId];
        const dateCellValue = this.getFieldValueOfRecord(recordId, record!, alarm.fieldId!, resultSet) as number;

        const alarmEntity = this.convertRecordAlarmToEntity(alarm, dateCellValue, spaceId, dstId, recordId, userId!);
        if (alarmEntity) {
          acc.push(alarmEntity as DatasheetRecordAlarmEntity);
          // Record updated alarms to avoid re-processing in updateRecordAlarms phase
          resultSet.updatedAlarmIds.push(alarmEntity.alarmId);
        }
      });
      return acc;
    }, [] as DatasheetRecordAlarmEntity[]);

    // Batch create alarms
    await this.batchCreateRecordAlarms(newAlarmEntities, userId!);

    // TODO(wuchen) refactor: O(2N) -> O(N)
    // Write alarms into datasheet_record.field_update_info (RecordMeta)
    await Promise.all(involvedRecordIds.map((recordId: string) => {
      const createAlarms: IRecordAlarm[] = resultSet.toCreateAlarms.get(recordId) || [];
      if (!involvedRecordMap[recordId]) {
        return null;
      }

      const recordMeta = involvedRecordMap[recordId]!.recordMeta || {};
      const existFieldExtraMap = recordMeta.fieldExtraMap || {};

      const fieldUpdatedMapChanges = createAlarms.reduce<{ [fieldId: string]: IFieldExtraMapValue }>((acc, cur: IRecordAlarm) => {
        const alarmCopy = { ...cur };
        delete alarmCopy.recordId;
        delete alarmCopy.fieldId;

        acc[cur.fieldId!] = { ...existFieldExtraMap[cur.fieldId!], alarm: alarmCopy };
        return acc;
      }, {});

      recordMeta.fieldExtraMap = Object.assign(existFieldExtraMap, fieldUpdatedMapChanges);

      return manager.createQueryBuilder()
        .update(DatasheetRecordEntity)
        .set({ recordMeta: recordMeta })
        .where([{ dstId, recordId }])
        .execute();
    }));
    profiler.done({ message: 'Finished creating alarms' });
  }

  private async updateRecordAlarms(
    manager: EntityManager,
    commonData: ICommonData,
    resultSet: { [key: string]: any },
  ) {
    if (!resultSet.replaceCellMap.size) return;
    const { userId, dstId } = commonData;

    const profiler = this.logger.startTimer();
    this.logger.info(`Start updating alarm. Size: ${resultSet.replaceCellMap.size}`);

    const { involvedRecordIds, involvedFieldIds } = this.getInvolvedRecordAndFieldIds(resultSet);

    const relatedAlarms = await this.getRecordAlarmsByRecordIdsAndFieldIds(dstId, involvedRecordIds, involvedFieldIds);

    const involvedAlarms = relatedAlarms.filter((alarm) => !resultSet.updatedAlarmIds.includes(alarm.alarmId));
    if (isEmpty(involvedAlarms)) return;

    const involvedRecordMap: IRecordMap = await this.recordService.getBasicRecordsByRecordIds(dstId, involvedRecordIds);
    const nowTime = dayjs(new Date());

    // TypeORM does not support update multiple entities to different values.
    // See  https://github.com/typeorm/typeorm/issues/5126
    await Promise.all(involvedAlarms.map(async(alarmEntity: DatasheetRecordAlarmEntity): Promise<void> => {
      const record = involvedRecordMap[alarmEntity.recordId]!;
      if (!record.recordMeta!.fieldExtraMap) return;

      const dateCellValue = this.getFieldValueOfRecord(record.id, record, alarmEntity.fieldId, resultSet);
      if (!dateCellValue) return;

      const fieldExtraInfo = record.recordMeta!.fieldExtraMap[alarmEntity.fieldId];
      if (!fieldExtraInfo || !fieldExtraInfo.alarm) return;

      const alarmMeta = fieldExtraInfo.alarm;
      const alarmAt = this.calculateAlarmAt(dateCellValue, alarmMeta.time!, alarmMeta.subtract!, alarmMeta.alarmAt);
      if (!alarmAt || nowTime.isAfter(alarmAt)) return;

      await manager.createQueryBuilder()
        .update(DatasheetRecordAlarmEntity)
        .set({
          alarmAt: alarmAt,
          status: RecordAlarmStatus.PENDING,
          updatedBy: userId,
          updatedAt: nowTime.toDate(),
        })
        .where('id = :id', { id: alarmEntity.id })
        .execute();
    }));
    profiler.done({ message: 'Finished updating alarms' });
  }

  private getInvolvedRecordAndFieldIds(resultSet: { [key: string]: any }): { involvedRecordIds: string[], involvedFieldIds: string[] } {
    const fieldMap = resultSet.temporaryFieldMap;
    const involvedRecordIds: string[] = [];
    const involvedFieldIds: string[] = [];
    Array.from(resultSet.replaceCellMap.keys()).forEach((recordId: any) => {
      const fieldData: { fieldId: string }[] = resultSet.replaceCellMap.get(recordId);
      fieldData.forEach(({ fieldId }) => {
        const field = fieldMap[fieldId] as IField;
        if (field && field.type === FieldType.DateTime) {
          involvedRecordIds.push(recordId);
          involvedFieldIds.push(fieldId);
        }
      });
    });
    return {
      involvedRecordIds,
      involvedFieldIds
    };
  }

  private getFieldValueOfRecord(recordId: string, record: IRecord, targetFieldId: string, resultSet: { [key: string]: any }) {
    if (resultSet.toCreateRecord.size) {
      const addRecordData = resultSet.toCreateRecord.get(recordId) || {};
      if (addRecordData[targetFieldId]) {
        return addRecordData[targetFieldId];
      }
    }

    if (resultSet.replaceCellMap.size) {
      const fieldData: { fieldId: string, data: any }[] = resultSet.replaceCellMap.get(recordId) || [];

      const matchedFieldData = fieldData.filter(({ fieldId }) => fieldId === targetFieldId);
      if (matchedFieldData.length > 0) {
        return matchedFieldData[0]!.data;
      }
    }

    if (!record) return null;

    return record.data[targetFieldId];
  }

  private async deleteRecordAlarms(
    manager: EntityManager,
    commonData: ICommonData,
    resultSet: { [key: string]: any },
  ) {
    if (!resultSet.toDeleteAlarms.size) return;
    const { userId, dstId } = commonData;

    const profiler = this.logger.startTimer();
    this.logger.info(`Start deleting alarms. Size: ${resultSet.toDeleteAlarms.size}`);

    let deletedAlarmIds: string[] = [];
    const deletedAlarmRecordIdsByFieldId = {};
    Array.from(resultSet.toDeleteAlarms.keys()).forEach((recordId: any) => {
      const deleteAlarms = resultSet.toDeleteAlarms.get(recordId) || [];
      if (isEmpty(deleteAlarms)) return;
      deletedAlarmIds = deletedAlarmIds.concat(deleteAlarms.map((alarm: IRecordAlarm) => alarm.id));

      deleteAlarms.forEach((alarm: IRecordAlarm) => {
        if (!deletedAlarmRecordIdsByFieldId[alarm.fieldId!]) {
          deletedAlarmRecordIdsByFieldId[alarm.fieldId!] = [];
        }
        deletedAlarmRecordIdsByFieldId[alarm.fieldId!].push(recordId);
      });
    });

    // Batch delete alarms
    await this.batchDeleteRecordAlarms(deletedAlarmIds, userId!);

    // Batch delete alarms from datasheet_record.field_update_info (RecordMeta)
    await Promise.all(Object.keys(deletedAlarmRecordIdsByFieldId).map((fieldId: string) => {
      const relatedRecordIds = deletedAlarmRecordIdsByFieldId[fieldId];
      const jsonParam = `'$.fieldExtraMap.${fieldId}.alarm'`;

      return manager.createQueryBuilder()
        .update(DatasheetRecordEntity)
        .set({ recordMeta: () => `JSON_REMOVE(field_updated_info, ${jsonParam})` })
        .where('dstId = :dstId', { dstId: dstId })
        .andWhere('recordId IN (:recordIds)', { recordIds: relatedRecordIds })
        .execute();
    }));
    profiler.done({ message: 'Finished deleting alarms' });
  }

  public override async batchUpdateStatusOfRecordAlarms(alarmIds: string[], status: RecordAlarmStatus) {
    return await this.repository.updateRecordAlarmStatusByIds(alarmIds, status);
  }

  public async batchCreateRecordAlarms(alarms: DatasheetRecordAlarmEntity[], updatedBy: string) {
    if (isEmpty(alarms)) return;
    const alarmMap = alarms.reduce<{ [key: string]: DatasheetRecordAlarmEntity }>((acc, cur: DatasheetRecordAlarmEntity) => {
      acc[cur.alarmId] = cur;
      return acc;
    }, {});

    const existAlarms = await this.repository.createQueryBuilder('alarm')
      .where('alarm.alarmId IN(:...ids)', { ids: Object.keys(alarmMap) })
      .getMany();

    const existAlarmIds = existAlarms.map(a => a.alarmId);

    // Create alarms
    const newAlarms = alarms.filter(a => !existAlarmIds.includes(a.alarmId));
    await this.repository.batchCreateRecordAlarms(newAlarms);

    // Update or recover (soft deleted) existing alarms
    // TypeORM does not support update multiple entities to different values at the same time.
    // See (https://github.com/typeorm/typeorm/issues/5126)
    const nowTime = dayjs(new Date());
    await Promise.all(existAlarms.map((alarm: DatasheetRecordAlarmEntity) => {
      const sourceAlarm = alarmMap[alarm.alarmId]!;
      return this.repository.createQueryBuilder()
        .update(DatasheetRecordAlarmEntity)
        .set({
          alarmAt: sourceAlarm.alarmAt,
          status: nowTime.isBefore(sourceAlarm.alarmAt) ? RecordAlarmStatus.PENDING : alarm.status,
          isDeleted: false,
          updatedBy: updatedBy,
          updatedAt: nowTime.toDate()
        })
        .where('alarmId = :alarmId', { alarmId: alarm.alarmId })
        .execute();
    }));
  }

  public async batchDeleteRecordAlarms(alarmIds: string[], deletedBy: string) {
    if (isEmpty(alarmIds)) return;
    return await this.repository.deleteRecordAlarmsByIds(alarmIds, deletedBy);
  }

  public convertRecordAlarmToEntity(
    alarm: IRecordAlarm, dateValue: number, spaceId: string, dstId: string, recordId: string, operatorUserId: string
  ): Omit<DatasheetRecordAlarmEntity, 'status' | 'isDeleted' | 'createdAt' | 'updatedAt' | 'beforeInsert'> | null {
    if (!dateValue || isEmpty(alarm.alarmUsers)) return null;

    const alarmAt = this.calculateAlarmAt(dateValue, alarm.time!, alarm.subtract!, alarm.alarmAt);
    if (!alarmAt) {
      this.logger.error(`Invalid alarm time from record alarm ${alarm.id}`);
      return null;
    }

    return {
      id: IdWorker.nextId().toString(),
      alarmId: alarm.id,
      spaceId: spaceId,
      dstId: dstId,
      recordId: recordId,
      fieldId: alarm.fieldId!,
      alarmAt: alarmAt,
      createdBy: operatorUserId,
      updatedBy: operatorUserId,
    };
  }

  /**
   * calculate alarm at timestamp.
   * parameter `alarmAtTime` is deprecated since v0.19.0
   *
   * @param dateValue date value
   * @param alarmAtTime deprecated
   * @param alarmAtSubtract alarm subtract
   * @param alarmAtTimestamp alarmAt timestamp having value when specific the time
   */
  public calculateAlarmAt(dateValue: dayjs.ConfigType, alarmAtTime: string, alarmAtSubtract: string, alarmAtTimestamp?: string): Date {
    if (alarmAtTimestamp) {
      return dayjs(alarmAtTimestamp).toDate();
    }
    let alarmAt = dayjs(dateValue);
    // subtract: ['', '100ms', '2m', '3h', '4d', '5Q', '6y']
    let subtractValue = 0;
    let subtractUnit = 's' as ManipulateType;
    if (alarmAtSubtract) {
      const subtractMatches = alarmAtSubtract.match(/^([0-9]+)(\w{1,2})$/);
      if (subtractMatches && subtractMatches.length === 3) {
        subtractValue = parseInt(subtractMatches[1]!);
        subtractUnit = subtractMatches[2] as ManipulateType;

        alarmAt = alarmAt.subtract(subtractValue, subtractUnit);
      }
    }

    // time: ['', '09:45', '20:00']
    if (alarmAtTime) {
      const timeMatches = alarmAtTime.match(/^([0-9]+):([0-9]{2})$/);
      if (timeMatches && timeMatches.length === 3) {
        const hourPart = parseInt(timeMatches[1]!);
        const minutePart = parseInt(timeMatches[2]!);

        alarmAt = alarmAt.set('hour', hourPart).set('minute', minutePart);
      }
    }

    return alarmAt.toDate();
  }
}
