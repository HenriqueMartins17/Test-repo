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

import {
  Field,
  FieldType,
  IEventInstance,
  IMeta,
  IOPEvent,
  IRecordMap,
  IReduxState,
  IRemoteChangeset,
  ISnapshot,
  IViewProperty,
  IViewRow,
  OP2Event,
  OPEventManager,
  OPEventNameEnums,
  OTActionName,
  Selectors,
  truncateText,
  ViewDerivateBase,
} from '@apitable/core';
import { Span } from '@metinseylan/nestjs-opentelemetry';
import { Injectable } from '@nestjs/common';
import { CommandService } from 'database/command/services/command.service';
import { DatasheetMetaService } from 'database/datasheet/services/datasheet.meta.service';
import { DatasheetRecordService } from 'database/datasheet/services/datasheet.record.service';
import { DatasheetService } from 'database/datasheet/services/datasheet.service';
import { RecordCommentService } from 'database/datasheet/services/record.comment.service';
import { DatasheetPack, NodeRelInfo } from 'database/interfaces';
import { ICommonData } from 'database/ot/interfaces/ot.interface';
import { DatasheetRecordSubscriptionBaseService } from 'database/subscription/datasheet.record.subscription.base.service';
import { DatasheetRecordSubscriptionEntity } from 'database/subscription/entities/datasheet.record.subscription.entity';
import { isEmpty, map } from 'lodash';
import { NodeService } from 'node/services/node.service';
import { InjectLogger } from 'shared/common';
import { IdWorker } from 'shared/helpers/snowflake';
import { IAuthHeader } from 'shared/interfaces';
import { notificationQueueExchangeName } from 'shared/services/queue/queue.module';
import { QueueSenderBaseService } from 'shared/services/queue/queue.sender.base.service';
import { RestService } from 'shared/services/rest/rest.service';
import { In } from 'typeorm';
import { UnitInfoDto } from 'unit/dtos/unit.info.dto';
import { UnitMemberService } from 'unit/services/unit.member.service';
import { UnitService } from 'unit/services/unit.service';
import { UserService } from 'user/services/user.service';
import { Logger } from 'winston';
import { DatasheetRecordSubscriptionRepository } from '../repositories/datasheet.record.subscription.repository';

@Injectable()
export class DatasheetRecordSubscriptionService extends DatasheetRecordSubscriptionBaseService {
  // TODO Delete event handling code after changed Mongo CDC to process events asynchronously
  opEventManager: OPEventManager;

  constructor(
    @InjectLogger() private readonly logger: Logger,
    private readonly commandService: CommandService,
    private readonly datasheetService: DatasheetService,
    private readonly datasheetMetaService: DatasheetMetaService,
    private readonly datasheetRecordService: DatasheetRecordService,
    private readonly nodeService: NodeService,
    private readonly queueSenderService: QueueSenderBaseService,
    private readonly recordCommentService: RecordCommentService,
    private readonly recordSubscriptionRepo: DatasheetRecordSubscriptionRepository,
    private readonly restService: RestService,
    private readonly unitMemberService: UnitMemberService,
    private readonly userService: UserService,
    private readonly unitService: UnitService
  ) {
    super();
    const watchedEvents = [
      OPEventNameEnums.CellUpdated,
      OPEventNameEnums.RecordCommentUpdated,
      OPEventNameEnums.RecordArchived,
      OPEventNameEnums.RecordUnarchived,
    ];
    this.opEventManager = new OPEventManager({
      options: {
        enableVirtualEvent: false,
        enableCombEvent: false,
        enableEventComplete: false,
      },
      getState: () => {
        return {} as IReduxState;
      },
      op2Event: new OP2Event(watchedEvents),
    });
    this.initEventListener();
  }

  public override async subscribeDatasheetRecords(userId: string, dstId: string, recordIds: string[], mirrorId?: string | null, validation = true) {
    if (isEmpty(recordIds)) return;

    const existRecordIds = await this.datasheetRecordService.getIdsByDstIdAndRecordIds(dstId, recordIds);
    const validRecordIds = validation ? recordIds.filter((recordId: string) => existRecordIds!.includes(recordId)) : recordIds;

    const subscribedRecordIds = await this.getSubscribedRecordIds(userId, dstId);
    const newSubscribedRecordIds = validRecordIds.filter((recordId: string) => !subscribedRecordIds.includes(recordId));
    if (isEmpty(newSubscribedRecordIds)) return;

    await this.recordSubscriptionRepo
      .createQueryBuilder()
      .insert()
      .into(DatasheetRecordSubscriptionEntity)
      .values(
        newSubscribedRecordIds.map((recordId: string) => {
          return {
            id: IdWorker.nextId().toString(),
            dstId: dstId,
            mirrorId: mirrorId,
            recordId: recordId,
            createdBy: userId,
          } as DatasheetRecordSubscriptionEntity;
        })
      )
      .updateEntity(false)
      .execute();
  }

  public override async unsubscribeDatasheetRecords(userId: string, dstId: string, recordIds: string[]) {
    if (isEmpty(recordIds)) return;

    const existRecordIds = await this.datasheetRecordService.getIdsByDstIdAndRecordIds(dstId, recordIds);
    const validRecordIds = recordIds.filter((recordId: string) => existRecordIds!.includes(recordId));
    if (isEmpty(validRecordIds)) return;

    await this.recordSubscriptionRepo
      .createQueryBuilder()
      .update(DatasheetRecordSubscriptionEntity)
      .set({
        updatedBy: userId,
        isDeleted: true,
      })
      .where({ createdBy: userId, dstId, recordId: In(validRecordIds), isDeleted: false })
      .execute();
  }

  public override async getSubscribedRecordIds(userId: string, dstId: string): Promise<string[]> {
    return await this.recordSubscriptionRepo.selectRecordIdsByDstIdAndUserId(dstId, userId);
  }

  public override async getSubscriptionsByRecordId(dstId: string, recordId: string): Promise<DatasheetRecordSubscriptionEntity[]> {
    return await this.recordSubscriptionRepo.selectByDstIdAndRecordId(dstId, recordId);
  }

  public override async getSubscriptionsByRecordIds(dstId: string, recordIds: string[]): Promise<DatasheetRecordSubscriptionEntity[]> {
    return await this.recordSubscriptionRepo.selectByDstIdAndRecordIds(dstId, recordIds);
  }

  public override async handleChangesets(changesets: IRemoteChangeset[], context: any) {
    const events = await this.opEventManager.asyncHandleChangesets(changesets);
    this.handleEvents(events, context);
  }

  public async handleEvents(events: IEventInstance<IOPEvent>[], context: any) {
    const subscriptedEvents = await this.filterSubscriptedRecordEvents(events);
    if (subscriptedEvents.length === 0) {
      return;
    }
    this.opEventManager.handleEvents(subscriptedEvents, false, context);
  }

  private async filterSubscriptedRecordEvents(events: IEventInstance<IOPEvent>[]) {
    const watchedEvents = [
      OPEventNameEnums.CellUpdated,
      OPEventNameEnums.RecordCommentUpdated,
      OPEventNameEnums.RecordArchived,
      OPEventNameEnums.RecordUnarchived,
    ];
    if (events.length === 0) {
      return events;
    }
    const { datasheetId } = events[0]!.context;
    const recordUpdatedEvents = events.filter((event) => watchedEvents.includes(event.eventName));
    if (recordUpdatedEvents.length === 0) {
      return recordUpdatedEvents;
    }
    const recordIds: string[] = recordUpdatedEvents.map((event) => event.context.recordId);
    const subscribedRecordIds = await this.getSubscriptedRecordsInPagination(datasheetId, recordIds);
    return recordUpdatedEvents.filter((event) => subscribedRecordIds.has(event.context.recordId));
  }

  private async getSubscriptedRecordsInPagination(datasheetId: string, recordIds: string[]) {
    const pageSize = 1000;
    const pageNum = Math.ceil(recordIds.length / pageSize);
    const subscriptedRecordIds = new Set();
    for (let i = 0; i < pageNum; i++) {
      const offset = i * pageSize;
      const recordIdsInPagination = recordIds.slice(offset, offset + pageSize);
      const subscriptions = await this.getSubscriptionsByRecordIds(datasheetId, recordIdsInPagination);
      subscriptions.forEach((subscription) => {
        subscriptedRecordIds.add(subscription.recordId);
      });
    }
    return subscriptedRecordIds;
  }

  /**
   * This method handles automatic subscriptions and unsubscriptions for records,
   * using the processRecordSubscriptions function to perform the required actions.
   * It first checks whether there are any subscriptions or unsubscriptions to process,
   * and then retrieves necessary data such as subscriber members, datasheet metadata,
   * and record maps. Finally, it calls the processRecordSubscriptions function
   * for both subscribing and unsubscribing actions.
   */
  @Span()
  public override async handleRecordAutoSubscriptions(commonData: ICommonData, resultSet: { [key: string]: any }) {
    const { toCreateRecordSubscriptions, toCancelRecordSubscriptions, creatorAutoSubscribedRecordIds } = resultSet;
    const { spaceId, dstId, userId } = commonData;
    if (creatorAutoSubscribedRecordIds && creatorAutoSubscribedRecordIds.length > 0 && userId) {
      await this.subscribeDatasheetRecords(userId, dstId, creatorAutoSubscribedRecordIds, null, false);
    }
    if (!toCreateRecordSubscriptions && !toCancelRecordSubscriptions) {
      return;
    }
    if (toCreateRecordSubscriptions.length === 0 && toCancelRecordSubscriptions.length === 0) return;
    const allSubscriptions = [...toCreateRecordSubscriptions, ...toCancelRecordSubscriptions];
    // get all userIds from toCreateRecordSubscriptions( [{memberId, recordId}] )
    const subscriberUnitIds = allSubscriptions.map((sub: { unitId: string; recordId: string }) => sub.unitId);
    // get recordIds from toCreateRecordSubscriptions( [{userId, recordId}] )
    const recordIds = allSubscriptions.map((sub: { userId: string; recordId: string }) => sub.recordId);
    // const subscriberMembers = await this.unitMemberService.getMembersBaseInfoBySpaceIdAndUserIds(spaceId, subscriberUserIds);
    const subscribeMembers = await this.unitService.getMembersByUnitIds(spaceId, subscriberUnitIds);
    const metaMap = await this.datasheetMetaService.getMetaMapByDstIds([dstId], true);
    const datasheetMeta = metaMap[dstId];
    if (!datasheetMeta) return;
    const recordMap = await this.datasheetRecordService.getBasicRecordsByRecordIds(dstId, recordIds);
    await this.processRecordSubscriptions(
      toCreateRecordSubscriptions,
      'subscribeDatasheetRecords',
      'auto_create_record_subscription',
      spaceId,
      dstId,
      userId ?? '',
      datasheetMeta,
      recordMap,
      subscribeMembers
    );
    await this.processRecordSubscriptions(
      toCancelRecordSubscriptions,
      'unsubscribeDatasheetRecords',
      'auto_cancel_record_subscription',
      spaceId,
      dstId,
      userId ?? '',
      datasheetMeta,
      recordMap,
      subscribeMembers
    );
    if (creatorAutoSubscribedRecordIds.length >= 0 && userId) {
      // FIXME do nothing ???
    }
  }

  private async onRecordCellUpdated(event: any, context: any) {
    const { datasheetId, fieldId, recordId } = event;
    const nodePrivate = await this.nodeService.nodePrivate(datasheetId);
    if (nodePrivate) {
      return;
    }
    const { spaceId, operatorUserId, authHeader, fromEditableSharedNode } = context;

    if (fromEditableSharedNode) return;

    const metaMap = await this.datasheetMetaService.getMetaMapByDstIds([datasheetId], true);
    const datasheetMeta = metaMap[datasheetId];
    if (!datasheetMeta) return;

    const recordMap = await this.datasheetRecordService.getBasicRecordsByRecordIds(datasheetId, [recordId]);
    const relatedRecord = recordMap[recordId];
    if (!relatedRecord) return;

    const relatedField = datasheetMeta.fieldMap[fieldId];
    if (!relatedField) return;

    const dataPack = await this.fetchDataPack(datasheetId, authHeader, recordId);
    if (!dataPack) return;

    const userInfo = await this.userService.getUserInfoBySpaceId(authHeader, dataPack.datasheet.spaceId);
    const store = this.commandService.fullFillStore(dataPack, userInfo);
    const state = store.getState();

    const subscriptions = await this.getSubscriptionsByRecordId(datasheetId, recordId);
    if (isEmpty(subscriptions)) return;

    // Don't notify operator
    const subscriberUserIds = subscriptions.map((sub) => sub.createdBy).filter((uid: string) => uid !== operatorUserId);
    if (isEmpty(subscriberUserIds)) return;

    const subscriptionByUserId = subscriptions.reduce<{ [key: string]: DatasheetRecordSubscriptionEntity }>((acc, cur) => {
      acc[cur.createdBy] = cur;
      return acc;
    }, {});

    const userIdByMemberId = {};
    const unitIdByMemberId = {};
    const subscriberMembers = await this.unitMemberService.getMembersBaseInfoBySpaceIdAndUserIds(spaceId, subscriberUserIds);
    Object.keys(subscriberMembers).forEach((userId: string) => {
      const memberId = subscriberMembers[userId]!.memberId;
      const unitId = subscriberMembers[userId]!.unitId;

      userIdByMemberId[memberId] = userId;
      unitIdByMemberId[memberId] = unitId;
    });

    const subscriberMemberIds = Object.keys(userIdByMemberId);
    this.logger.info(`datasheets[${datasheetId}].records[${recordId}] subscribers: ${subscriberMemberIds.join()}`);

    // Get related mirror nodes
    const involvedMirrorNodes = await this.getSubscriptionInvolvedMirrorNodes(subscriptions);

    // Get value of primary field
    const recordTitle = this.datasheetRecordService.getRecordTitle(relatedRecord, datasheetMeta, store);

    // Get changed value
    let oldDisplayValue = '';
    let newDisplayValue = '';
    if (relatedField.type !== FieldType.Link) {
      const oldRawValue = event.change.from;
      const newRawValue = event.change.to;
      oldDisplayValue = Field.bindContext(relatedField, state).cellValueToString(oldRawValue) || '';
      newDisplayValue = Field.bindContext(relatedField, state).cellValueToString(newRawValue) || '';
    }

    // message extra payload
    const msgTemplate = 'subscribed_record_cell_updated';
    const msgExtras = {
      recordTitle: truncateText(recordTitle),
      fieldName: truncateText(relatedField.name),
      oldDisplayValue: truncateText(oldDisplayValue),
      newDisplayValue: truncateText(newDisplayValue),
      recordId: recordId,
      viewId: datasheetMeta.views[0]!.id,
    };

    // Subscribers after filtering by field permission, check subscription source (datasheet of mirror)
    const userPermissionMap = await this.restService.getUsersNodePermission(authHeader, datasheetId, subscriberUserIds);
    // mirror cache
    const mirrorIdToViewIdMap: { [key: string]: string } = {};
    const mirrorVisibleRecordIds: { [key: string]: string[] } = {};

    await Promise.all(
      subscriberMemberIds.map(async (memberId: string) => {
        const userId = userIdByMemberId[memberId];
        const unitId = unitIdByMemberId[memberId];
        const subscription = subscriptionByUserId[userId];

        // If subscription comes from datasheet and subscriber has datasheet node permission, notify
        if (!subscription?.mirrorId) {
          this.logger.info(`datasheets[${datasheetId}].records[${recordId}] subscribers(with permission): ${userPermissionMap[userId]?.editable}`);
          if (userPermissionMap[userId]?.readable) {
            const message = this.buildNotificationMessage(spaceId, datasheetId, msgTemplate, operatorUserId, unitId, msgExtras);
            await this.queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
          }
          return;
        }

        // If subscription comes from mirror, check if record is visible in mirror, if so, notify
        let visibleRecordIds: string[];
        if (subscription.mirrorId in mirrorVisibleRecordIds) {
          // cache hit
          visibleRecordIds = mirrorVisibleRecordIds[subscription.mirrorId]!;
        } else {
          // cache miss
          const mirrorNode = involvedMirrorNodes[subscription.mirrorId];
          const viewId = mirrorNode.viewId;
          const viewInfo = this.getViewInfo(viewId, dataPack.snapshot, state);
          const visibleRows = this.getVisibleRows(viewInfo, state);

          // fill cache
          mirrorIdToViewIdMap[subscription.mirrorId] = viewId;
          visibleRecordIds = visibleRows.map((row) => row.recordId);
        }

        if (visibleRecordIds.includes(recordId)) {
          const mirrorId = subscription.mirrorId;
          const mirrorMsgExtras = { ...msgExtras, viewId: mirrorIdToViewIdMap[mirrorId] };
          const message = this.buildNotificationMessage(spaceId, mirrorId, msgTemplate, operatorUserId, unitId, mirrorMsgExtras);
          await this.queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
        }
      })
    );
  }

  private async onRecordCommentUpdated(event: any, context: any) {
    const { datasheetId, recordId, action } = event;
    const nodePrivate = await this.nodeService.nodePrivate(datasheetId);
    if (nodePrivate) {
      return;
    }
    const { spaceId, operatorUserId, authHeader, fromEditableSharedNode } = context;

    if (fromEditableSharedNode) return;
    if (action.n !== OTActionName.ListInsert) return;

    const commentContent = this.recordCommentService.extractCommentTextFromAction(action);
    if (!commentContent) return;

    const metaMap = await this.datasheetMetaService.getMetaMapByDstIds([datasheetId], true);
    const datasheetMeta = metaMap[datasheetId];
    if (!datasheetMeta) return;

    const recordMap = await this.datasheetRecordService.getBasicRecordsByRecordIds(datasheetId, [recordId]);
    const relatedRecord = recordMap[recordId];
    if (!relatedRecord) return;

    const dataPack = await this.fetchDataPack(datasheetId, authHeader, recordId);
    if (!dataPack) return;

    const userInfo = await this.userService.getUserInfoBySpaceId(authHeader, dataPack.datasheet.spaceId);
    const store = this.commandService.fullFillStore(dataPack, userInfo);
    const state = store.getState();

    const subscriptions = await this.getSubscriptionsByRecordId(datasheetId, recordId);
    if (isEmpty(subscriptions)) return;

    // Don't notify operator
    const subscriberUserIds = subscriptions.map((sub) => sub.createdBy).filter((uid: string) => uid !== operatorUserId);
    if (isEmpty(subscriberUserIds)) return;

    const subscriptionByUserId = subscriptions.reduce<{ [key: string]: DatasheetRecordSubscriptionEntity }>((acc, cur) => {
      acc[cur.createdBy] = cur;
      return acc;
    }, {});

    const userIdByMemberId = {};
    const unitIdByMemberId = {};
    const subscriberMembers = await this.unitMemberService.getMembersBaseInfoBySpaceIdAndUserIds(spaceId, subscriberUserIds);
    Object.keys(subscriberMembers).forEach((userId: string) => {
      const memberId = subscriberMembers[userId]!.memberId;
      const unitId = subscriberMembers[userId]!.unitId;

      userIdByMemberId[memberId] = userId;
      unitIdByMemberId[memberId] = unitId;
    });

    const subscriberMemberIds = Object.keys(userIdByMemberId);

    // Obtain related mirror nodes
    const involvedMirrorNodes = await this.getSubscriptionInvolvedMirrorNodes(subscriptions);

    // Obtain value of primary field
    const recordTitle = this.datasheetRecordService.getRecordTitle(relatedRecord, datasheetMeta, store);

    // Get the subscription source (datasheet or mirror)
    const userPermissionMap = await this.restService.getUsersNodePermission(authHeader, datasheetId, subscriberUserIds);

    // mirror cache
    const mirrorIdToViewIdMap: { [key: string]: string } = {};
    const mirrorVisibleRecordIds: { [key: string]: string[] } = {};

    // message extra payload
    const msgTemplate = 'subscribed_record_commented';
    const msgExtras = {
      recordTitle: truncateText(recordTitle),
      content: truncateText(commentContent),
      recordId: recordId,
      viewId: datasheetMeta.views[0]!.id,
    };

    await Promise.all(
      subscriberMemberIds.map(async (memberId: string) => {
        const userId = userIdByMemberId[memberId];
        const unitId = unitIdByMemberId[memberId];
        const subscription = subscriptionByUserId[userId];

        // If subscription comes from datasheet and subscriber has datasheet node permission, notify
        if (!subscription?.mirrorId) {
          this.logger.info(`datasheets[${datasheetId}].records[${recordId}] subscribers(with permission): ${userPermissionMap[userId]?.editable}`);
          if (userPermissionMap[userId]?.readable) {
            const message = this.buildNotificationMessage(spaceId, datasheetId, msgTemplate, operatorUserId, unitId, msgExtras);
            await this.queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
          }
          return;
        }

        // If subscription comes from mirror, check if record is visible in mirror, if so, notify
        let visibleRecordIds: string[];
        if (subscription.mirrorId in mirrorVisibleRecordIds) {
          // cache hit
          visibleRecordIds = mirrorVisibleRecordIds[subscription.mirrorId]!;
        } else {
          // cache miss
          const mirrorNode = involvedMirrorNodes[subscription.mirrorId];
          const viewId = mirrorNode.viewId;
          const viewInfo = this.getViewInfo(viewId, dataPack.snapshot, state);
          const visibleRows = this.getVisibleRows(viewInfo, state);

          // fill cache
          mirrorIdToViewIdMap[subscription.mirrorId] = viewId;
          visibleRecordIds = visibleRows.map((row) => row.recordId);
        }

        if (visibleRecordIds.includes(recordId)) {
          const mirrorId = subscription.mirrorId;
          const mirrorMsgExtras = { ...msgExtras, viewId: mirrorIdToViewIdMap[mirrorId] };
          const message = this.buildNotificationMessage(spaceId, mirrorId, msgTemplate, operatorUserId, unitId, mirrorMsgExtras);
          await this.queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
        }
      })
    );
  }

  private async onRecordArchived(event: any, context: any) {
    const { datasheetId, recordId } = event;
    const nodePrivate = await this.nodeService.nodePrivate(datasheetId);
    if (nodePrivate) {
      return;
    }
    const { spaceId, operatorUserId, authHeader, fromEditableSharedNode } = context;

    if (fromEditableSharedNode) return;
    const metaMap = await this.datasheetMetaService.getMetaMapByDstIds([datasheetId], true);
    const datasheetMeta = metaMap[datasheetId];
    if (!datasheetMeta) return;

    const recordMap = await this.datasheetRecordService.getBasicRecordsByRecordIds(datasheetId, [recordId], false, true);
    const relatedRecord = recordMap[recordId];
    if (!relatedRecord) return;

    const dataPack = await this.fetchDataPack(datasheetId, authHeader, recordId, true);
    if (!dataPack) return;

    const userInfo = await this.userService.getUserInfoBySpaceId(authHeader, dataPack.datasheet.spaceId);
    const store = this.commandService.fullFillStore(dataPack, userInfo);
    const state = store.getState();

    const subscriptions = await this.getSubscriptionsByRecordId(datasheetId, recordId);
    if (isEmpty(subscriptions)) return;

    // Don't notify operator
    const subscriberUserIds = subscriptions.map((sub) => sub.createdBy).filter((uid: string) => uid !== operatorUserId);
    if (isEmpty(subscriberUserIds)) return;

    const subscriptionByUserId = subscriptions.reduce<{ [key: string]: DatasheetRecordSubscriptionEntity }>((acc, cur) => {
      acc[cur.createdBy] = cur;
      return acc;
    }, {});

    const userIdByMemberId = {};
    const unitIdByMemberId = {};
    const subscriberMembers = await this.unitMemberService.getMembersBaseInfoBySpaceIdAndUserIds(spaceId, subscriberUserIds);
    Object.keys(subscriberMembers).forEach((userId: string) => {
      const memberId = subscriberMembers[userId]!.memberId;
      const unitId = subscriberMembers[userId]!.unitId;

      userIdByMemberId[memberId] = userId;
      unitIdByMemberId[memberId] = unitId;
    });

    const subscriberMemberIds = Object.keys(userIdByMemberId);
    this.logger.info(`datasheets[${datasheetId}].records[${recordId}] subscribers: ${subscriberMemberIds.join()}`);

    const involvedMirrorNodes = await this.getSubscriptionInvolvedMirrorNodes(subscriptions);

    const recordTitle = this.datasheetRecordService.getRecordTitle(relatedRecord, datasheetMeta, store);

    // message extra payload
    const msgTemplate = 'subscribed_record_archived';
    const msgExtras = {
      recordTitle: truncateText(recordTitle),
      viewId: datasheetMeta.views[0]!.id,
    };

    // Subscribers after filtering by field permission, check subscription source (datasheet of mirror)
    const userPermissionMap = await this.restService.getUsersNodePermission(authHeader, datasheetId, subscriberUserIds);
    // mirror cache
    const mirrorIdToViewIdMap: { [key: string]: string } = {};
    const mirrorVisibleRecordIds: { [key: string]: string[] } = {};

    await Promise.all(
      subscriberMemberIds.map(async (memberId: string) => {
        const userId = userIdByMemberId[memberId];
        const unitId = unitIdByMemberId[memberId];
        const subscription = subscriptionByUserId[userId];

        // If subscription comes from datasheet and subscriber has datasheet node permission, notify
        if (!subscription?.mirrorId) {
          this.logger.info(`datasheets[${datasheetId}].records[${recordId}] subscribers(with permission): ${userPermissionMap[userId]?.editable}`);
          if (userPermissionMap[userId]?.readable) {
            const message = this.buildNotificationMessage(spaceId, datasheetId, msgTemplate, operatorUserId, unitId, msgExtras);
            await this.queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
          }
          return;
        }

        // If subscription comes from mirror, check if record is visible in mirror, if so, notify
        let visibleRecordIds: string[];
        if (subscription.mirrorId in mirrorVisibleRecordIds) {
          // cache hit
          visibleRecordIds = mirrorVisibleRecordIds[subscription.mirrorId]!;
        } else {
          // cache miss
          const mirrorNode = involvedMirrorNodes[subscription.mirrorId];
          const viewId = mirrorNode.viewId;
          const viewInfo = this.getViewInfo(viewId, dataPack.snapshot, state);
          const visibleRows = this.getVisibleRows(viewInfo, state);

          // fill cache
          mirrorIdToViewIdMap[subscription.mirrorId] = viewId;
          visibleRecordIds = visibleRows.map((row) => row.recordId);
        }

        if (visibleRecordIds.includes(recordId)) {
          const mirrorId = subscription.mirrorId;
          const mirrorMsgExtras = { ...msgExtras, viewId: mirrorIdToViewIdMap[mirrorId] };
          const message = this.buildNotificationMessage(spaceId, mirrorId, msgTemplate, operatorUserId, unitId, mirrorMsgExtras);
          await this.queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
        }
      })
    );
  }

  private async onRecordUnarchived(event: any, context: any) {
    const { datasheetId, recordId } = event;
    const nodePrivate = await this.nodeService.nodePrivate(datasheetId);
    if (nodePrivate) {
      return;
    }
    const { spaceId, operatorUserId, authHeader, fromEditableSharedNode } = context;

    if (fromEditableSharedNode) return;
    const metaMap = await this.datasheetMetaService.getMetaMapByDstIds([datasheetId], true);
    const datasheetMeta = metaMap[datasheetId];
    if (!datasheetMeta) return;

    const recordMap = await this.datasheetRecordService.getBasicRecordsByRecordIds(datasheetId, [recordId]);
    const relatedRecord = recordMap[recordId];
    if (!relatedRecord) return;

    const dataPack = await this.fetchDataPack(datasheetId, authHeader, recordId);
    if (!dataPack) return;

    const userInfo = await this.userService.getUserInfoBySpaceId(authHeader, dataPack.datasheet.spaceId);
    const store = this.commandService.fullFillStore(dataPack, userInfo);
    const state = store.getState();

    const subscriptions = await this.getSubscriptionsByRecordId(datasheetId, recordId);
    if (isEmpty(subscriptions)) return;

    // Don't notify operator
    const subscriberUserIds = subscriptions.map((sub) => sub.createdBy).filter((uid: string) => uid !== operatorUserId);
    if (isEmpty(subscriberUserIds)) return;

    const subscriptionByUserId = subscriptions.reduce<{ [key: string]: DatasheetRecordSubscriptionEntity }>((acc, cur) => {
      acc[cur.createdBy] = cur;
      return acc;
    }, {});

    const userIdByMemberId = {};
    const unitIdByMemberId = {};
    const subscriberMembers = await this.unitMemberService.getMembersBaseInfoBySpaceIdAndUserIds(spaceId, subscriberUserIds);
    Object.keys(subscriberMembers).forEach((userId: string) => {
      const memberId = subscriberMembers[userId]!.memberId;
      const unitId = subscriberMembers[userId]!.unitId;

      userIdByMemberId[memberId] = userId;
      unitIdByMemberId[memberId] = unitId;
    });

    const subscriberMemberIds = Object.keys(userIdByMemberId);
    this.logger.info(`datasheets[${datasheetId}].records[${recordId}] subscribers: ${subscriberMemberIds.join()}`);

    const involvedMirrorNodes = await this.getSubscriptionInvolvedMirrorNodes(subscriptions);

    const recordTitle = this.datasheetRecordService.getRecordTitle(relatedRecord, datasheetMeta, store);

    // message extra payload
    const msgTemplate = 'subscribed_record_unarchived';
    const msgExtras = {
      recordTitle: truncateText(recordTitle),
      recordId: recordId,
      viewId: datasheetMeta.views[0]!.id,
    };

    // Subscribers after filtering by field permission, check subscription source (datasheet of mirror)
    const userPermissionMap = await this.restService.getUsersNodePermission(authHeader, datasheetId, subscriberUserIds);
    // mirror cache
    const mirrorIdToViewIdMap: { [key: string]: string } = {};
    const mirrorVisibleRecordIds: { [key: string]: string[] } = {};

    await Promise.all(
      subscriberMemberIds.map(async (memberId: string) => {
        const userId = userIdByMemberId[memberId];
        const unitId = unitIdByMemberId[memberId];
        const subscription = subscriptionByUserId[userId];

        // If subscription comes from datasheet and subscriber has datasheet node permission, notify
        if (!subscription?.mirrorId) {
          this.logger.info(`datasheets[${datasheetId}].records[${recordId}] subscribers(with permission): ${userPermissionMap[userId]?.editable}`);
          if (userPermissionMap[userId]?.readable) {
            const message = this.buildNotificationMessage(spaceId, datasheetId, msgTemplate, operatorUserId, unitId, msgExtras);
            await this.queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
          }
          return;
        }

        // If subscription comes from mirror, check if record is visible in mirror, if so, notify
        let visibleRecordIds: string[];
        if (subscription.mirrorId in mirrorVisibleRecordIds) {
          // cache hit
          visibleRecordIds = mirrorVisibleRecordIds[subscription.mirrorId]!;
        } else {
          // cache miss
          const mirrorNode = involvedMirrorNodes[subscription.mirrorId];
          const viewId = mirrorNode.viewId;
          const viewInfo = this.getViewInfo(viewId, dataPack.snapshot, state);
          const visibleRows = this.getVisibleRows(viewInfo, state);

          // fill cache
          mirrorIdToViewIdMap[subscription.mirrorId] = viewId;
          visibleRecordIds = visibleRows.map((row) => row.recordId);
        }

        if (visibleRecordIds.includes(recordId)) {
          const mirrorId = subscription.mirrorId;
          const mirrorMsgExtras = { ...msgExtras, viewId: mirrorIdToViewIdMap[mirrorId] };
          const message = this.buildNotificationMessage(spaceId, mirrorId, msgTemplate, operatorUserId, unitId, mirrorMsgExtras);
          await this.queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
        }
      })
    );
  }

  private async getSubscriptionInvolvedMirrorNodes(subscriptions: DatasheetRecordSubscriptionEntity[]) {
    const involvedMirrorIds = subscriptions.reduce<string[]>((acc, cur) => {
      if (cur.mirrorId && !acc.includes(cur.mirrorId)) {
        acc.push(cur.mirrorId);
      }
      return acc;
    }, []);

    if (isEmpty(involvedMirrorIds)) return [];

    const involvedMirrorNodes = await this.nodeService.getNodeRelInfoByIds(involvedMirrorIds);
    return involvedMirrorNodes.reduce<{ [key: string]: NodeRelInfo }>((acc, cur) => {
      if (cur.relNodeId) {
        acc[cur.relNodeId] = cur;
      }
      return acc;
    }, {});
  }

  // todo: move to view? service
  private getViewInfo(viewId: string, snapshot: ISnapshot, state: IReduxState): IViewProperty {
    const view = Selectors.getViewByIdWithDefault(state, snapshot.datasheetId, viewId);
    const rows = map(snapshot.recordMap, (record) => {
      return { recordId: record.id };
    });
    return { ...view, id: viewId, rows } as IViewProperty;
  }

  // todo: move to view? service
  private getVisibleRows(view: IViewProperty, state: IReduxState): IViewRow[] {
    const datasheetId = Selectors.getSnapshot(state)?.datasheetId;
    if (!datasheetId) {
      return [];
    }
    const rows = new ViewDerivateBase(state, datasheetId).getViewDerivation(view).visibleRows;

    return rows && rows.length ? rows : [];
  }

  private buildNotificationMessage(spaceId: string, nodeId: string, templateId: string, fromUserId: string, toUnitId: string, extras: any) {
    return {
      nodeId: nodeId,
      spaceId: spaceId,
      body: {
        extras: extras,
      },
      templateId: templateId,
      toUnitId: [toUnitId],
      fromUserId: fromUserId,
    };
  }

  private async fetchDataPack(dstId: string, auth: IAuthHeader, recordId: string, includeArchivedRecords: boolean = false) {
    try {
      return (await this.datasheetService.fetchDataPack(dstId, auth, false, {
        recordIds: [recordId],
        includeArchivedRecords: includeArchivedRecords,
      })) as DatasheetPack;
    } catch (error) {
      this.logger.error(`Fetching DataPack failed, dstId: ${dstId}, recordId: ${recordId}, err: ${error}.`);
    }
    return undefined;
  }

  private initEventListener() {
    this.opEventManager.addEventListener(
      OPEventNameEnums.CellUpdated,
      // @ts-ignore
      (event, context) => {
        void this.onRecordCellUpdated(event, context);
      }
    );
    this.opEventManager.addEventListener(
      OPEventNameEnums.RecordCommentUpdated,
      // @ts-ignore
      (event, context) => {
        void this.onRecordCommentUpdated(event, context);
      }
    );
    this.opEventManager.addEventListener(
      OPEventNameEnums.RecordArchived,
      // @ts-ignore
      (event, context) => {
        void this.onRecordArchived(event, context);
      }
    );
    this.opEventManager.addEventListener(
      OPEventNameEnums.RecordUnarchived,
      // @ts-ignore
      (event, context) => {
        void this.onRecordUnarchived(event, context);
      }
    );
  }

  /**
   * This function processes a list of record subscriptions, either subscribing or unsubscribing
   * users to datasheets, based on the provided method. If the list of subscriptions is empty,
   * the function returns immediately.
   *
   * For each subscription, the function:
   * 1. Calls the provided method (either 'subscribeDatasheetRecords' or 'unsubscribeDatasheetRecords')
   *    with the user ID, datasheet ID, and record IDs.
   * 2. Retrieves the title of the record.
   * 3. Sends a notification message with the details of the operation.
   *
   * @param recordSubscriptions - An array of tuples, where each tuple contains a user ID and a record ID.
   * @param method - The method to be used for processing ('subscribeDatasheetRecords' or 'unsubscribeDatasheetRecords').
   * @param msgTemplate - The template to be used for the notification message ('auto_create_record_subscription' for subscription or 'auto_cancel_record_subscriptionb' for unsubscription).
   * @param spaceId - The ID of the space where the datasheet is located.
   * @param dstId - The ID of the datasheet.
   * @param userId - The ID of the user performing the operation.
   * @param datasheetMeta - Metadata about the datasheet.
   * @param recordMap - A map of the records in the datasheet.
   * @param subscriberMembers - A map of the subscriber members, with each key being a user ID and each value being a member object.
   */
  private async processRecordSubscriptions(
    recordSubscriptions: { unitId: string; recordId: string }[],
    method: 'subscribeDatasheetRecords' | 'unsubscribeDatasheetRecords',
    msgTemplate: 'auto_create_record_subscription' | 'auto_cancel_record_subscription',
    spaceId: string,
    dstId: string,
    userId: string,
    datasheetMeta: IMeta,
    recordMap: IRecordMap,
    subscriberMembers: { [unitId: string]: UnitInfoDto[] }[]
  ) {
    if (recordSubscriptions.length === 0) {
      return;
    }

    // const recordSubscriptionsReplacedUnitIds = this.replaceUnitIdsWithUserIds(recordSubscriptions, unitUserMap);
    const recordSubscriptionMap = this.convertUserRecordRefIntoMap(recordSubscriptions);
    const notificationPrimaryKeys: string[] = [];
    for (const subscribeUnitId of Object.keys(recordSubscriptionMap)) {
      const recordIds = recordSubscriptionMap[subscribeUnitId] || [];
      const members = subscriberMembers[subscribeUnitId];
      for (const member of members) {
        await this[method](member.userId, dstId, recordIds);
        for (const recordId of recordIds) {
          // Prevent duplicate notifications
          const notificationPrimaryKey = `${member.userId}-${recordId}`;
          if (notificationPrimaryKeys.includes(notificationPrimaryKey)) {
            continue;
          }
          notificationPrimaryKeys.push(notificationPrimaryKey);
          const recordTitle = this.getRecordTitle(datasheetMeta, recordMap, dstId, recordId);
          await this.sendNotificationMessage({
            spaceId,
            datasheetId: dstId,
            msgTemplate,
            operatorUserId: userId,
            unitId: member.unitId,
            recordId,
            recordTitle,
          });
        }
      }
    }
  }

  /**
   * This method converts an array of tuples, each containing a user ID and a record ID,
   * into a map where each key is a user ID and its corresponding value is an array of
   * record IDs. This conversion is performed using the Array.prototype.reduce method.
   *
   * @param userRecordRefs - An array of tuples, where each tuple contains a user ID and a record ID.
   * @returns An object where each key is a user ID and its corresponding value is an array of record IDs.
   */
  private convertUserRecordRefIntoMap(userRecordRefs: { unitId: string; recordId: string }[]): { [key: string]: string[] } {
    return userRecordRefs.reduce((acc, cur) => {
      const { unitId, recordId } = cur;
      if (!acc[unitId]) {
        acc[unitId] = [];
      }
      if (!acc[unitId].includes(recordId)) {
        acc[unitId]?.push(recordId);
      }
      return acc;
    }, {});
  }

  /**
   * This method prepares and sends a notification message. It first destructures the necessary properties
   * from the input DTO, truncates the record title, and builds the notification message. Then it sends the
   * message through the queueSenderService.
   *
   * @param dto - An object containing necessary data for sending a notification message, including spaceId, datasheetId,
   *              msgTemplate, operatorUserId, unitId, recordId, and recordTitle.
   */
  private async sendNotificationMessage({
    spaceId,
    datasheetId,
    msgTemplate,
    operatorUserId,
    unitId,
    recordId,
    recordTitle,
  }: {
    spaceId: string;
    datasheetId: string;
    msgTemplate: string;
    operatorUserId: string;
    unitId: string;
    recordId: string;
    recordTitle: string;
  }) {
    const msgExtras = {
      recordId,
      recordTitle: truncateText(recordTitle),
    };
    const nodePrivate = await this.nodeService.nodePrivate(datasheetId);
    if (nodePrivate) {
      return;
    }
    const message = this.buildNotificationMessage(spaceId, datasheetId, msgTemplate, operatorUserId, unitId, msgExtras);

    await this.queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
  }

  /**
   * This method retrieves the title of a record. It first fetches the data pack for the record,
   * then retrieves the user information associated with the record's space ID. After preparing
   * the store data, it gets the related record from the record map. If the related record is found,
   * it retrieves the record title; otherwise, it returns an empty string.
   *
   * @param datasheetMeta - The datasheet metadata.
   * @param recordMap - The map of records.
   * @param dstId - The datasheet ID.
   * @param recordId - The record ID.
   * @returns A Promise that resolves with the record title or an empty string if the related record is not found.
   */
  private getRecordTitle(datasheetMeta: IMeta, recordMap: IRecordMap, dstId: string, recordId: string): string {
    const store = this.commandService.fillTinyStore([
      {
        snapshot: {
          meta: datasheetMeta,
          recordMap: recordMap,
          datasheetId: dstId,
        },
        datasheet: {
          id: dstId,
        } as any,
      },
    ]);
    const relatedRecord = recordMap[recordId];
    if (!relatedRecord) {
      return '';
    }
    return this.datasheetRecordService.getRecordTitle(relatedRecord, datasheetMeta, store);
  }
}
