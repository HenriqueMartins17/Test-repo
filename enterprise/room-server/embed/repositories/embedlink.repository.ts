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

import { EmbedLinkEntity, IEmbedLinkProperty } from 'enterprise/embed/entities/embedlink.entity';
import { EmbedLinkPropertyDto } from 'enterprise/embed/models/embedlink.model';
import { IdWorker } from 'shared/helpers';
import { EntityRepository, Repository } from 'typeorm';
import { UpdateResult } from 'typeorm/query-builder/result/UpdateResult';

export interface IEmbedLinkBaseInfo {
  linkId: string;
  spaceId: string;
  nodeId: string;
  props: IEmbedLinkProperty;
}

@EntityRepository(EmbedLinkEntity)
export class EmbedLinkRepository extends Repository<EmbedLinkEntity> {
  selectCountByEmbedLinkId(embedLinkId: string) {
    return this.count({
      where: {
        embedLinkId,
        isDeleted: 0,
      },
    });
  }

  createEmbedLink(userId: string, spaceId: string, nodeId: string, embedLinkId: string, property: EmbedLinkPropertyDto): Promise<EmbedLinkEntity> {
    return this.save({
      id: IdWorker.nextId().toString(),
      nodeId,
      spaceId,
      embedLinkId,
      createdBy: userId,
      updatedBy: userId,
      props: property,
    });
  }

  selectEmbedLinkIdAndPropsBySpaceIdAndNodeId(spaceId: string, nodeId: string): Promise<EmbedLinkEntity[]> {
    return this.find({
      select: ['props', 'embedLinkId'],
      where: {
        spaceId,
        nodeId,
        isDeleted: 0,
      },
    });
  }

  deleteByEmbedLinkId(embedLinkId: string, userId: string): Promise<UpdateResult> {
    return this.update(
      { embedLinkId },
      {
        isDeleted: true,
        updatedBy: userId,
      },
    );
  }

  selectUpdateByByEmbedLinkId(embedLinkId: string): Promise<string> {
    return this.findOne({
      select: ['updatedBy'],
      where: {
        embedLinkId,
        isDeleted: 0,
      },
    }).then(result => {
      return result!.updatedBy;
    });
  }

  selectSpaceIdAndNodIdAndPropsByEmbedLinkId(embedLinkId: string): Promise<IEmbedLinkBaseInfo> {
    return this.findOne({
      select: ['nodeId', 'spaceId', 'props'],
      where: {
        embedLinkId,
        isDeleted: 0,
      },
    }).then(result => {
      return {
        linkId: embedLinkId,
        nodeId: result!.nodeId!,
        spaceId: result!.spaceId!,
        props: result!.props!,
      };
    });
  }

  selectIsDeletedByEmbedLinkId(embedLinkId: string): Promise<boolean | null> {
    return this.findOne({
      select: ['isDeleted'],
      where: { embedLinkId },
    }).then(result => (result ? result.isDeleted : null));
  }

  updateIsDeletedByEmbedLinkId(embedLinkId: string, userId: string): Promise<UpdateResult> {
    return this.update(
      { embedLinkId },
      {
        isDeleted: false,
        updatedBy: userId,
      },
    );
  }

  selectCountByNodeIdAndSpaceId(nodeId: string, spaceId: string): Promise<number> {
    return this.count({
      where: { nodeId, spaceId, isDeleted: 0 },
    });
  }

  selectPermissionTypeByEmbedLinkId(embedLinkId: string): Promise<string> {
    return this.createQueryBuilder('vel')
      .select('props->\'$.payload.permissionType\' ', 'permissionType')
      .where('vel.embed_link_id = :embedLinkId', { embedLinkId })
      .andWhere('vel.is_deleted = 0')
      .getRawOne<{ permissionType: string }>()
      .then(result => result?.permissionType!);
  }

  selectSpaceIdByEmbedLinkId(embedLinkId: string): Promise<string | undefined> {
    return this.findOne({
      select: ['spaceId'],
      where: {
        embedLinkId,
        isDeleted: 0,
      },
    }).then(result => result?.spaceId);
  }

  selectNodeIdByEmbedLinkId(embedLinkId: string): Promise<string | undefined> {
    return this.findOne({
      select: ['nodeId'],
      where: {
        embedLinkId,
        isDeleted: 0,
      },
    }).then(result => result?.nodeId);
  }

  selectPropsByEmbedLinkId(embedLinkId: string): Promise<IEmbedLinkProperty> {
    return this.findOne({
      select: ['props'],
      where: {
        embedLinkId,
        isDeleted: 0,
      },
    }).then(result => result?.props!);
  }
}
