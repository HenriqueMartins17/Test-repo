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

import { DatasheetRecordAlarmEntity } from 'database/alarm/entities/datasheet.record.alarm.entity';
import { RecordAlarmStatus } from 'shared/enums/record.alarm.enum';
import { isEmpty } from 'lodash';
import { EntityRepository, Repository } from 'typeorm';

@EntityRepository(DatasheetRecordAlarmEntity)
export class DatasheetRecordAlarmRepository extends Repository<DatasheetRecordAlarmEntity> {
  async selectRecordAlarmByStatusAndTimeRange(
    status: RecordAlarmStatus,
    startTime: Date,
    endTime: Date
  ): Promise<DatasheetRecordAlarmEntity[] | null> {
    return await this.createQueryBuilder('alarm')
      .where('alarm.status = :status', { status: status })
      .andWhere('alarm.isDeleted = :isDeleted', { isDeleted: false })
      .andWhere('alarm.alarmAt <= :endTime', { endTime: endTime })
      .andWhere('alarm.alarmAt > :startTime', { startTime: startTime })
      .getMany();
  }

  async selectRecordAlarmByRecordIdsAndFieldIds(dstId: string, recordIds: string[], fieldIds: string[]): Promise<DatasheetRecordAlarmEntity[]> {
    return await this.createQueryBuilder('alarm')
      .where('alarm.isDeleted = :isDeleted', { isDeleted: false })
      .andWhere('alarm.dstId = :dstId', { dstId: dstId })
      .andWhere('alarm.recordId IN(:...recordIds)', { recordIds: recordIds })
      .andWhere('alarm.fieldId IN(:...fieldIds)', { fieldIds: fieldIds })
      .getMany();
  }

  async updateRecordAlarmStatusByIds(alarmIds: string[], status: RecordAlarmStatus) {
    if (isEmpty(alarmIds)) return;

    await this.createQueryBuilder()
      .update(DatasheetRecordAlarmEntity)
      .set({ status: status })
      .where('alarmId IN(:...ids)', { ids: alarmIds })
      .andWhere('isDeleted = :isDeleted', { isDeleted: false })
      .execute();
  }

  async batchCreateRecordAlarms(alarms: DatasheetRecordAlarmEntity[]) {
    if (isEmpty(alarms)) return;

    await this.createQueryBuilder().insert().into(DatasheetRecordAlarmEntity).values(alarms).updateEntity(false).execute();
  }

  async deleteRecordAlarmsByIds(alarmIds: string[], deletedBy: string) {
    if (isEmpty(alarmIds)) return;

    await this.createQueryBuilder()
      .update(DatasheetRecordAlarmEntity)
      .set({ isDeleted: true, updatedBy: deletedBy })
      .where('isDeleted = :isDeleted', { isDeleted: false })
      .andWhere('alarmId IN (:alarmIds)', { alarmIds: alarmIds })
      .execute();
  }
}
