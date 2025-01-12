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

import { forwardRef, Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { DatasheetRecordAlarmRepository } from './repositories/datasheet.record.alarm.repository';
import { DatasheetRecordAlarmBaseService } from 'database/alarm/datasheet.record.alarm.base.service';
import { DatasheetRecordAlarmService } from './services/datasheet.record.alarm.service';
import { DatabaseModule } from 'database/database.module';

@Module({
  imports: [
    forwardRef(() => DatabaseModule),
    TypeOrmModule.forFeature([
      DatasheetRecordAlarmRepository,
    ]),
  ],
  providers: [
    {
      provide: DatasheetRecordAlarmBaseService,
      useClass: DatasheetRecordAlarmService
    },
  ],
  exports: [
    {
      provide: DatasheetRecordAlarmBaseService,
      useClass: DatasheetRecordAlarmService
    }, 
  ]
})
export class AlarmEnterpriseModule { }
