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

import { FieldType, IAlarmUser, truncateText } from '@apitable/core';
import { Injectable } from '@nestjs/common';
import { Cron } from '@nestjs/schedule';
import { DatasheetRecordAlarmBaseService } from 'database/alarm/datasheet.record.alarm.base.service';
import { DatasheetRecordAlarmEntity } from 'database/alarm/entities/datasheet.record.alarm.entity';
import { CommandService } from 'database/command/services/command.service';
import { DatasheetMetaService } from 'database/datasheet/services/datasheet.meta.service';
import { DatasheetRecordService } from 'database/datasheet/services/datasheet.record.service';
import { DatasheetService } from 'database/datasheet/services/datasheet.service';
import { isEmpty } from 'lodash';
import { NodeService } from 'node/services/node.service';
import { InjectLogger } from 'shared/common';
import { RecordAlarmStatus } from 'shared/enums/record.alarm.enum';
import { notificationQueueExchangeName } from 'shared/services/queue/queue.module';
import { QueueSenderBaseService } from 'shared/services/queue/queue.sender.base.service';
import { Logger } from 'winston';

@Injectable()
export class SchedTaskService {
  constructor(
    @InjectLogger() private readonly logger: Logger,
    private readonly queueSenderService: QueueSenderBaseService,
    private readonly commandService: CommandService,
    private readonly datasheetService: DatasheetService,
    private readonly datasheetMetaService: DatasheetMetaService,
    private readonly recordService: DatasheetRecordService,
    private readonly recordAlarmService: DatasheetRecordAlarmBaseService,
    private readonly nodeService: NodeService,
  ) {}

  @Cron('0,30 * * * * *')
  async scheduleScanActivatedRecordAlarmJob() {
    const profiler = this.logger.startTimer();
    this.logger.info('Start scanning activated record alarms');

    let activatedRecordAlarms = await this.recordAlarmService.getCurrentActivatedRecordAlarms(35);
    if (isEmpty(activatedRecordAlarms)) {
      profiler.done({ message: 'Finished scanning activated record alarms, no alarms found' });
      return;
    }
    // filter private node
    let nodeIds = activatedRecordAlarms!.reduce((arr: string[], obj) => {
      if (!arr.includes(obj.dstId)) {
        arr.push(obj.dstId);
      }
      return arr;
    }, []);
    nodeIds = await this.nodeService.filterPrivateNode(nodeIds);
    activatedRecordAlarms = activatedRecordAlarms!.filter((i) => nodeIds.includes(i.dstId));
    if (isEmpty(activatedRecordAlarms)) {
      profiler.done({ message: 'No team datasheet found' });
      return;
    }
    const involvedAlarmIds = activatedRecordAlarms!.map((alarm) => alarm.alarmId);
    await this.recordAlarmService.batchUpdateStatusOfRecordAlarms(involvedAlarmIds, RecordAlarmStatus.PROCESSING);

    const involvedRecordIdsMap = activatedRecordAlarms!.reduce<Map<string, string[]>>((acc, cur: DatasheetRecordAlarmEntity) => {
      if (!acc.has(cur.dstId)) {
        acc.set(cur.dstId, []);
      }
      acc.get(cur.dstId)!.push(cur.recordId);
      return acc;
    }, new Map<string, string[]>());

    const involvedDstIds = Array.from(involvedRecordIdsMap.keys());
    const recordMaps = new Map(
      await Promise.all(
        involvedDstIds.map(async (dstId: string) => {
          const involvedRecordIds = involvedRecordIdsMap.get(dstId)!;
          return [dstId, await this.recordService.getBasicRecordsByRecordIds(dstId, involvedRecordIds)] as const;
        }),
      ),
    );

    const metaMap = await this.datasheetMetaService.getMetaMapByDstIds(involvedDstIds, true);
    const datasheetPacks = await this.datasheetService.getTinyBasePacks(involvedRecordIdsMap);
    const store = this.commandService.fillTinyStore(datasheetPacks);

    const enqueuedAlarmIds: string[] = [];
    await Promise.all(
      activatedRecordAlarms!.map(async (alarm: DatasheetRecordAlarmEntity) => {
        const dstMeta = metaMap[alarm.dstId];
        const relatedRecord = recordMaps.get(alarm.dstId)![alarm.recordId];
        if (!relatedRecord || !dstMeta) return;

        if (!relatedRecord.recordMeta || !relatedRecord.recordMeta.fieldExtraMap) return;

        const fieldExtraMap = relatedRecord.recordMeta.fieldExtraMap[alarm.fieldId];
        if (!fieldExtraMap) return;

        const alarmUsers: IAlarmUser[] = fieldExtraMap['alarm']!['alarmUsers']!;
        if (isEmpty(alarmUsers)) return;

        const receiverUnitIds = alarmUsers.reduce<string[]>((acc, cur: IAlarmUser) => {
          if (cur.type === 'field') {
            const memberField = dstMeta.fieldMap[cur.data]!;
            if (memberField.type !== FieldType.Member) {
              return acc;
            }

            const memberFieldValue = relatedRecord.data[memberField.id] as string[];
            if (isEmpty(memberFieldValue)) {
              return acc;
            }

            memberFieldValue.forEach((memberId: string) => acc.push(memberId));
          } else if (cur.type === 'member') {
            acc.push(cur.data);
          }
          return acc;
        }, []);

        if (isEmpty(receiverUnitIds)) return;

        const recordTitle = this.recordService.getRecordTitle(relatedRecord, dstMeta, store);
        const viewId = dstMeta.views[0]!.id;

        const message = {
          nodeId: alarm.dstId,
          spaceId: alarm.spaceId,
          body: {
            extras: {
              recordTitle: truncateText(recordTitle),
              taskExpireAt: relatedRecord.data[alarm.fieldId],
              recordId: alarm.recordId,
              viewId: viewId,
            },
          },
          templateId: 'task_reminder',
          toUnitId: receiverUnitIds,
          fromUserId: 0, // means from system
        };

        enqueuedAlarmIds.push(alarm.alarmId);
        await this.queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
        this.logger.info(`Alarm ${alarm.alarmId} is enqueued`);
      }),
    );
    await this.recordAlarmService.batchUpdateStatusOfRecordAlarms(enqueuedAlarmIds, RecordAlarmStatus.DONE);

    profiler.done({ message: `Finished scanning activated record alarms, enqueued ${enqueuedAlarmIds.length}/${involvedAlarmIds.length}` });
  }
}
