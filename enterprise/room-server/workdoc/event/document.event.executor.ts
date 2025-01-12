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

import { IEventInstance, IOPEvent, IWorkDocValue, OPEventNameEnums } from '@apitable/core';
import { Injectable, Logger, OnApplicationBootstrap } from '@nestjs/common';
import { IOtEventContext } from 'database/ot/interfaces/ot.interface';
import { BackendGrpcClient } from 'grpc/client/backend.grpc.client';
import { InjectLogger } from 'shared/common';
import { IEventExecutor, OTEventManager } from 'shared/event/ot.event.manager';

@Injectable()
export class DocumentEventExecutor implements OnApplicationBootstrap, IEventExecutor {
  constructor(
    // @ts-ignore
    @InjectLogger() private readonly logger: Logger,
    private readonly backendGrpcClient: BackendGrpcClient,
  ) {}

  onApplicationBootstrap(): any {
    this.logger.debug('Add document event executor');
    OTEventManager.addExecutor(DocumentEventExecutor.name, this);
  }

  async execute(events: IEventInstance<IOPEvent>[], opContext?: IOtEventContext) {
    this.logger.debug('Execute document event');

    const watchedEvents = [OPEventNameEnums.CellUpdated, OPEventNameEnums.RecordCreated, OPEventNameEnums.RecordDeleted];
    const documentEvents = events.filter((event) => watchedEvents.includes(event.eventName));
    if (documentEvents.length === 0) {
      return;
    }
    const removeNames: string[] = [];
    const recoverNames: string[] = [];
    for (const { context } of documentEvents) {
      const { action } = context;
      this.appendDocumentIds(recoverNames, action?.oi);
      this.appendDocumentIds(removeNames, action?.od);
    }
    const removeDocumentNames = removeNames.filter(item => !recoverNames.includes(item));
    const recoverDocumentNames = recoverNames.filter(item => !removeNames.includes(item));
    if (removeDocumentNames.length === 0 && recoverDocumentNames.length === 0) {
      return;
    }
    this.backendGrpcClient.documentOperate({ removeDocumentNames, recoverDocumentNames, userId: opContext?.operatorUserId });
  }

  private appendDocumentIds(documentNames: string[], object?: any) {
    if (!object) {
      return;
    }
    if (object.data) {
      for (const cellValue of Object.values(object.data)) {
        if (Array.isArray(cellValue)) {
          for (const value of cellValue as IWorkDocValue[]) {
            value.documentId && documentNames.push(value.documentId);
          }
        }
      }
    } else {
      if (Array.isArray(object)) {
        for (const value of object as IWorkDocValue[]) {
          value.documentId && documentNames.push(value.documentId);
        }
      }
    }
  }
}
