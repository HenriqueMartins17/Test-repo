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

import { DatasheetRecordSubscriptionEntity } from 'database/subscription/entities/datasheet.record.subscription.entity';
import { EntityRepository, In, Repository } from 'typeorm';

@EntityRepository(DatasheetRecordSubscriptionEntity)
export class DatasheetRecordSubscriptionRepository extends Repository<DatasheetRecordSubscriptionEntity> {

  async selectRecordIdsByDstIdAndUserId(dstId: string, userId: string): Promise<string[]> {
    const entities = await this.find({
      select: ['recordId'],
      where: [{ dstId, createdBy: userId, isDeleted: false }],
    });
    return entities.map(entity => entity.recordId);
  }

  async selectByDstIdAndRecordId(dstId: string, recordId: string): Promise<DatasheetRecordSubscriptionEntity[]> {
    return await this.find({ dstId, recordId: recordId, isDeleted: false });
  }

  async selectByDstIdAndRecordIds(dstId: string, recordIds: string[]): Promise<DatasheetRecordSubscriptionEntity[]> {
    return await this.find({ dstId, recordId: In(recordIds), isDeleted: false });
  }

}
