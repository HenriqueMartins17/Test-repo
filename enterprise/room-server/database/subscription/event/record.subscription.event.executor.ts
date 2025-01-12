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

import { IEventInstance, IOPEvent } from '@apitable/core';
import { Injectable, Logger, OnApplicationBootstrap } from '@nestjs/common';
import { IOtEventContext } from 'database/ot/interfaces/ot.interface';
import { InjectLogger } from 'shared/common';
import { IEventExecutor, OTEventManager } from 'shared/event/ot.event.manager';
import { DatasheetRecordSubscriptionService } from '../services/datasheet.record.subscription.service';

@Injectable()
export class RecordSubscriptionEventExecutor implements OnApplicationBootstrap, IEventExecutor {
  constructor(
    // @ts-ignore
    @InjectLogger() private readonly logger: Logger,
    private readonly recordSubscriptionService: DatasheetRecordSubscriptionService,
  ) {}

  onApplicationBootstrap(): any {
    this.logger.debug('Add record subscription event executor');
    OTEventManager.addExecutor(RecordSubscriptionEventExecutor.name, this);
  }

  async execute(events: IEventInstance<IOPEvent>[], context?: IOtEventContext) {
    this.logger.debug('Execute record subscription event');
    await this.recordSubscriptionService.handleEvents(events, context);
  }
}
