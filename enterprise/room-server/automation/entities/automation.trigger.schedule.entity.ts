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

import { BaseEntity } from 'shared/entities/base.entity';
import { Column, Entity } from 'typeorm';

export interface IScheduleConfig {
  second?: string;
  minute: string;
  hour: string;
  month: string;
  dayOfMonth: string;
  dayOfWeek: string;
  timeZone: string;
}

export enum TriggerStatus {
  PENDING,
  RUNNING,
  STOP,
}

@Entity('automation_trigger_schedule')
export class AutomationTriggerScheduleEntity extends BaseEntity {
  @Column({
    name: 'space_id',
    nullable: false,
    length: 50,
  })
    spaceId!: string;

  @Column({
    name: 'trigger_id',
    nullable: false,
    length: 50,
    unique: true,
  })
    triggerId!: string;

  @Column({
    name: 'schedule_conf',
    nullable: false,
    type: 'json',
  })
    scheduleConf!: IScheduleConfig;

  @Column({
    name: 'trigger_status',
    nullable: false,
    comment: 'Scheduling status: 0-pending, 1-running, 2-stop',
  })
    triggerStatus!: number;

  @Column({
    name: 'is_pushed',
    nullable: false,
  })
    isPushed!: boolean;

  @Column('timestamp', {
    name: 'trigger_last_time',
    nullable: true,
  })
    triggerLastTime?: Date;

  @Column('timestamp', {
    name: 'trigger_next_time',
    nullable: true,
  })
    triggerNextTime?: Date;
}
