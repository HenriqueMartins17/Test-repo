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
import { RabbitSubscribe } from '@golevelup/nestjs-rabbitmq';
import { forwardRef, Inject, Injectable } from '@nestjs/common';
import { RobotBaseInfoDto } from 'automation/dtos/robot.dto';
import { AutomationTriggerEntity } from 'automation/entities/automation.trigger.entity';
import { TriggerEventHelper } from 'automation/events/helpers/trigger.event.helper';
import { AutomationRobotRepository } from 'automation/repositories/automation.robot.repository';
import { AutomationTriggerRepository } from 'automation/repositories/automation.trigger.repository';
import { AutomationService } from 'automation/services/automation.service';
import { parseExpression } from 'cron-parser';
import dayjs from 'dayjs';
import timezone from 'dayjs/plugin/timezone';
import utc from 'dayjs/plugin/utc';
import { AutomationTriggerScheduleEntity, IScheduleConfig, TriggerStatus } from 'enterprise/automation/entities/automation.trigger.schedule.entity';
import { AutomationTriggerScheduleRepository } from 'enterprise/automation/repositories/automation.trigger.schedule.repository';
import { isEqual } from 'lodash';
import { QueueSenderBaseService } from 'shared/services/queue/queue.sender.base.service';
import { QueryDeepPartialEntity } from 'typeorm/query-builder/QueryPartialEntity';

dayjs.extend(timezone);
dayjs.extend(utc);

@Injectable()
export class AutomationScheduleHandler {
  constructor(
    private readonly automationTriggerScheduleRepository: AutomationTriggerScheduleRepository,
    private readonly automationRobotRepository: AutomationRobotRepository,
    private readonly automationTriggerRepository: AutomationTriggerRepository,
    private readonly queueSenderService: QueueSenderBaseService,
    @Inject(forwardRef(() => AutomationService))
    private readonly automationService: AutomationService,
    @Inject(forwardRef(() => TriggerEventHelper))
    private readonly triggerEventHelper: TriggerEventHelper,
  ) {}

  @RabbitSubscribe({
    queue: 'automation@release',
  })
  async handleScheduleTask(message: { scheduleId: string }): Promise<void> {
    // be careful not use winston logger, will to cause an error
    const { schedule, trigger,shouldReQueue } = await this.getScheduleTrigger(message);
    // requeue first
    if (shouldReQueue) {
      await this.queueSenderService.sendMessage('automation@exchange', 'automation.create', message);
    }
    if (!trigger || !schedule) {
      return;
    }
    const updateSchedule: QueryDeepPartialEntity<AutomationTriggerScheduleEntity> = {};
    try {
      const expression = this.getCronExpressionFromScheduleConfig(schedule.scheduleConf);
      if (!expression.hasNext()) {
        // don't need to handle message then remove message and requeue
        return;
      }
      if (!expression.hasPrev()) {
        // don't need to handle message then remove message and requeue
        return;
      }
      const prev = expression.prev();
      // current timestamp milliseconds
      const now = dayjs(new Date()).tz(schedule.scheduleConf.timeZone);
      // const nextTime = dayjs(next.getTime()).tz(schedule.scheduleConf.timeZone);
      updateSchedule.triggerNextTime = new Date(expression.next().getTime());
      // if between 1 minute then handle
      if (prev.getTime() <= now.valueOf() && prev.getTime() > now.subtract(1, 'minute').valueOf()) {
        updateSchedule.triggerStatus = TriggerStatus.RUNNING;
        updateSchedule.triggerLastTime = new Date(prev.getTime());
        await this.automationTriggerScheduleRepository.update(message.scheduleId, updateSchedule);
        try {
          const triggerInput = this.triggerEventHelper.renderInput(trigger.input!);
          await this.automationService.handleTask(trigger.robotId, {
            triggerId: trigger.triggerId,
            input: triggerInput,
            output: {},
          });
        } catch (e) {
          console.error('AutomationScheduleHandler:HandleScheduleError:' + message.scheduleId, e);
        }
        updateSchedule.triggerStatus = TriggerStatus.PENDING;
      }
    } catch (e) {
      console.error('AutomationScheduleHandler:parseScheduleError:' + message.scheduleId, e);
    }
    // done of handle schedule then requeue message and change status
    await this.automationTriggerScheduleRepository.update({ id: message.scheduleId }, updateSchedule);
  }

  getCronExpressionFromScheduleConfig(scheduleConfig: IScheduleConfig) {
    const expression = scheduleConfig.second
      ? `${scheduleConfig.second} ${scheduleConfig.minute} ${scheduleConfig.hour} ` +
        `${scheduleConfig.dayOfMonth} ${scheduleConfig.month} ${scheduleConfig.dayOfWeek}`
      : `${scheduleConfig.minute} ${scheduleConfig.hour} ` + `${scheduleConfig.dayOfMonth} ${scheduleConfig.month} ${scheduleConfig.dayOfWeek}`;
    return parseExpression(expression, {
      tz: scheduleConfig.timeZone,
    });
  }

  async getScheduleTrigger(message: { scheduleId: string }): Promise<{
    schedule: AutomationTriggerScheduleEntity | undefined;
    trigger: AutomationTriggerEntity | undefined;
    shouldReQueue: boolean;
  }> {
    const schedule = await this.automationTriggerScheduleRepository.selectBaseInfoById(message.scheduleId);
    // schedule deleted
    if (!schedule) {
      console.warn('AutomationScheduleHandler:scheduleDeleted' + message.scheduleId);
      return { schedule: undefined, trigger: undefined, shouldReQueue: false };
    }
    // trigger not configured
    if (isEqual(schedule.scheduleConf, {})) {
      console.warn('AutomationScheduleHandler:scheduleConfigEmpty:' + message.scheduleId);
      return { schedule: undefined, trigger: undefined, shouldReQueue: false };
    }
    const trigger = await this.automationTriggerRepository.selectTriggerByTriggerId(schedule.triggerId);
    // trigger deleted also
    if (!trigger) {
      console.warn('AutomationScheduleHandler:triggerDeleted:' + schedule.id);
      return { schedule: undefined, trigger: undefined, shouldReQueue: false };
    }
    // check schedule status
    if (schedule.triggerStatus == TriggerStatus.RUNNING) {
      console.warn('AutomationScheduleHandler:runningRequeue:' + schedule.id);
      // running more than 5 minutes should trigger again, because the database executes timeout for 5 minutes
      if (schedule.updatedAt && new Date().getTime() - Date.parse(schedule.updatedAt.toString()) > 5 * 60 * 1000) {
        console.warn('AutomationScheduleHandler:runningTimeout:' + schedule.id);
        return { schedule, trigger, shouldReQueue: true };
      }
      return { schedule: undefined, trigger: trigger, shouldReQueue: true };
    }
    // check robot status
    const robots: RobotBaseInfoDto[] = await this.automationRobotRepository.selectRobotBaseInfoDtoByRobotIds([trigger.robotId]);
    // robot was been deleted
    if (robots.length == 0) {
      console.warn('AutomationScheduleHandler:robotDeleted:' + message.scheduleId);
      return { schedule: undefined, trigger: undefined, shouldReQueue: false };
    }
    // check robot whether active
    for (const robot of robots) {
      if (!robot.isActive) {
        console.warn('AutomationScheduleHandler:robotUnActive:' + message.scheduleId);
        return { schedule: undefined, trigger: undefined, shouldReQueue: true };
      }
    }
    return { schedule, trigger, shouldReQueue: true };
  }
}
