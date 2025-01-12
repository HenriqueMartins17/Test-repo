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
import { DatabaseModule } from 'database/database.module';
import { DatasheetRecordSubscriptionBaseService } from 'database/subscription/datasheet.record.subscription.base.service';
import { NodeModule } from 'node/node.module';
import { UnitModule } from 'unit/unit.module';
import { UserModule } from 'user/user.module';
import { DatasheetRecordSubscriptionRepository } from './repositories/datasheet.record.subscription.repository';
import { DatasheetRecordSubscriptionService } from './services/datasheet.record.subscription.service';
import { RecordSubscriptionEventExecutor } from './event/record.subscription.event.executor';

@Module({
  imports: [
    forwardRef(() => NodeModule),
    UnitModule,
    UserModule,
    forwardRef(() => DatabaseModule),
    TypeOrmModule.forFeature([
      DatasheetRecordSubscriptionRepository,
    ]),
  ],
  providers: [
    {
      provide: DatasheetRecordSubscriptionBaseService,
      useClass: DatasheetRecordSubscriptionService
    },
    DatasheetRecordSubscriptionService,
    RecordSubscriptionEventExecutor,
  ],
  exports: [
    {
      provide: DatasheetRecordSubscriptionBaseService,
      useClass: DatasheetRecordSubscriptionService
    }, 
  ]
})
export class SubscriptionEnterpriseModule { }
