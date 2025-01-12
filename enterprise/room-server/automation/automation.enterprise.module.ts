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
import { AutomationActionTypeRepository } from 'automation/repositories/automation.action.type.repository';
import { AutomationRobotRepository } from 'automation/repositories/automation.robot.repository';
import { AutomationServiceRepository } from 'automation/repositories/automation.service.repository';
import { AutomationTriggerRepository } from 'automation/repositories/automation.trigger.repository';
import { RobotModule } from 'automation/robot.module';
import { RobotActionTypeBaseService } from 'automation/services/robot.action.type.base.service';
import { AutomationScheduleHandler } from 'enterprise/automation/handler/automation.schedule.handler';
import { AutomationTriggerScheduleRepository } from 'enterprise/automation/repositories/automation.trigger.schedule.repository';
import { RobotActionTypeEnterpriseService } from './service/robot.action.type.enterprise.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      AutomationActionTypeRepository,
      AutomationServiceRepository,
      AutomationTriggerScheduleRepository,
      AutomationRobotRepository,
      AutomationTriggerRepository,
    ]),
    forwardRef(() => RobotModule),
  ],
  providers: [
    {
      provide: RobotActionTypeBaseService,
      useClass: RobotActionTypeEnterpriseService,
    },
    AutomationScheduleHandler,
  ],
  exports: [
    {
      provide: RobotActionTypeBaseService,
      useClass: RobotActionTypeEnterpriseService,
    },
  ],
})
export class AutomationEnterpriseModule {}
