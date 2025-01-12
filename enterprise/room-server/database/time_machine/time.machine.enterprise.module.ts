/**
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import { forwardRef, Module } from '@nestjs/common';
import { DatasheetModule } from 'database/datasheet/datasheet.module';
import { NodeModule } from 'node/node.module';
import { ResourceModule } from 'database/resource/resource.module';
import { UserModule } from 'user/user.module';
import { TimeMachineService } from './services/time.machine.service';
import { TimeMachineController } from './controllers/time.machine.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { TableBundleRepository } from './repositories/tablebundle.repository';
import { CommandModule } from 'database/command/command.module';
import { TimeMachineBaseService } from 'database/time_machine/time.machine.service.base';
import {DatabaseModule} from 'database/database.module';
import {RobotModule} from "../../../automation/robot.module";

@Module({
  imports: [
    forwardRef(()=>ResourceModule),
    forwardRef(()=>NodeModule),
    forwardRef(() => DatasheetModule),
    forwardRef(() => RobotModule),
    UserModule,
    forwardRef(() => DatabaseModule),
    CommandModule,
    TypeOrmModule.forFeature([
      TableBundleRepository
    ])
  ],
  providers: [
    {
      provide: TimeMachineBaseService,
      useClass: TimeMachineService
    }
  ],
  controllers: [TimeMachineController],
  exports: [{
    provide: TimeMachineBaseService,
    useClass: TimeMachineService
  }],
})
export class TimeMachineEnterpriseModule {}
