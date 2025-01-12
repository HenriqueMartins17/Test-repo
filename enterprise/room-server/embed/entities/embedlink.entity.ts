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

export interface IEmbedLinkPayloadPrimarySideBar {
  collapsed?: boolean;
}

export interface IEmbedLinkPayloadViewToolBar {
  basicTools?: boolean;

  shareBtn?: boolean;

  widgetBtn?: boolean;

  apiBtn?: boolean;

  formBtn?: boolean;

  historyBtn?: boolean;

  robotBtn?: boolean;
}

export interface IEmbedLinkPayloadViewControl {
  viewId?: string;

  tabBar?: boolean;

  toolBar?: IEmbedLinkPayloadViewToolBar;

  collapsed?: boolean;
}

export interface IEmbedLinkPropertyPayload {
  primarySideBar?: IEmbedLinkPayloadPrimarySideBar;

  viewControl?: IEmbedLinkPayloadViewControl;

  bannerLogo?: boolean;

  permissionType?: string;
}

export interface IEmbedLinkProperty {
  payload?: IEmbedLinkPropertyPayload;

  theme?: string;
}

@Entity('embed_link')
export class EmbedLinkEntity extends BaseEntity {
  @Column({
    name: 'space_id',
    nullable: false,
    comment: 'space ID',
    length: 50,
  })
  spaceId!: string;

  @Column({
    name: 'node_id',
    nullable: false,
    comment: 'node Id',
    length: 50,
  })
  nodeId!: string;

  @Column({
    name: 'embed_link_id',
    nullable: false,
    unique: true,
    comment: 'unique embed link id',
    length: 50,
  })
  embedLinkId!: string;

  @Column({
    name: 'props',
    nullable: true,
    comment: 'ui attribute',
    type: 'json',
  })
  props?: IEmbedLinkProperty;
}
