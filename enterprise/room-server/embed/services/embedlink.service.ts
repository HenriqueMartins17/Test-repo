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

import { ApiTipConstant, ConfigConstant, DEFAULT_EDITOR_PERMISSION, IDPrefix, IServerDashboardPack, Role } from '@apitable/core';
import { Injectable } from '@nestjs/common';
import { plainToClass } from 'class-transformer';
import { DashboardService } from 'database/dashboard/services/dashboard.service';
import { DatasheetMetaService } from 'database/datasheet/services/datasheet.meta.service';
import { DatasheetService } from 'database/datasheet/services/datasheet.service';
import { FormService } from 'database/form/services/form.service';
import { DatasheetPack, FormDataPack, NodeBaseInfo } from 'database/interfaces';
import { EmbedLinkDetailDto, EmbedLinkDto, EmbedLinkPropertyDto, EmbedPermissionType } from 'enterprise/embed/models/embedlink.model';
import { EmbedLinkRepository, IEmbedLinkBaseInfo } from 'enterprise/embed/repositories/embedlink.repository';
import { NodeService } from 'node/services/node.service';
import { EMBED_LINK_URL_TEMPLATE } from 'shared/common';
import { ApiException, PermissionException, ServerException } from 'shared/exception';
import { IAuthHeader } from 'shared/interfaces';
import { RestService } from 'shared/services/rest/rest.service';
import { UserService } from 'user/services/user.service';
import util from 'util';

@Injectable()
export class EmbedLinkService {
  constructor(
    private readonly embedlinkRepository: EmbedLinkRepository,
    private readonly nodeService: NodeService,
    private readonly datasheetService: DatasheetService,
    private readonly restService: RestService,
    private readonly userService: UserService,
    private readonly datasheetMetaService: DatasheetMetaService,
    private readonly formService: FormService,
    private readonly dashboardService: DashboardService
  ) {}

  async createEmbedLink(userId: string, spaceId: string, nodeId: string, embedLinkId: string, property: EmbedLinkPropertyDto): Promise<void> {
    const isDeleted = await this.embedlinkRepository.selectIsDeletedByEmbedLinkId(embedLinkId);
    if (null === isDeleted) {
      await this.embedlinkRepository.createEmbedLink(userId, spaceId, nodeId, embedLinkId, property);
    } else if (isDeleted) {
      await this.embedlinkRepository.updateIsDeletedByEmbedLinkId(embedLinkId, userId);
    }
  }

  async getAllEmbedLinks(spaceId: string, nodeId: string): Promise<EmbedLinkDto[]> {
    const properties = await this.embedlinkRepository.selectEmbedLinkIdAndPropsBySpaceIdAndNodeId(spaceId, nodeId);
    return properties.map((item) => {
      return {
        linkId: item.embedLinkId,
        url: util.format(EMBED_LINK_URL_TEMPLATE, item.embedLinkId),
        ...plainToClass(EmbedLinkPropertyDto, item.props),
      };
    });
  }

  /**
   * check embed link whether exist
   * @param embedLinkId embed link id
   * @param throwable default value is true to throw an ApiException
   * @return Promise<boolean>
   */
  async isEmbedLinkIdExist(embedLinkId: string, throwable: boolean = true): Promise<boolean> {
    const count = await this.embedlinkRepository.selectCountByEmbedLinkId(embedLinkId);
    if (throwable && count < 1) {
      throw ApiException.tipError(ApiTipConstant.api_embed_link_id_not_exist);
    }
    return count >= 1;
  }

  async deleteEmbedLink(embedLinkId: string, userId: string): Promise<void> {
    await this.isEmbedLinkIdExist(embedLinkId);
    await this.embedlinkRepository.deleteByEmbedLinkId(embedLinkId, userId);
  }

  async getEmbedLinkLastModifiedByUserId(embedLinkId: string): Promise<string> {
    return await this.embedlinkRepository.selectUpdateByByEmbedLinkId(embedLinkId);
  }

  async getEmbedLinkDetailInfoByEmbedLinkId(embedLinkId: string): Promise<EmbedLinkDetailDto> {
    const baseInfo: IEmbedLinkBaseInfo = await this.embedlinkRepository.selectSpaceIdAndNodIdAndPropsByEmbedLinkId(embedLinkId);
    let nodeInfo = (await this.nodeService.selectNodeBaseInfoByNodeId(baseInfo.nodeId)) as NodeBaseInfo;
    if (nodeInfo) {
      const revision = (await this.datasheetService.getRevisionByDstId(baseInfo.nodeId))?.revision;
      nodeInfo = { ...nodeInfo, revision };
    }
    const spaceInfo = await this.restService.getSpaceInfo(baseInfo.spaceId);
    return {
      spaceId: baseInfo.spaceId,
      nodeInfo,
      spaceInfo,
      embedInfo: {
        url: embedLinkId,
        linkId: embedLinkId,
        ...(baseInfo.props as EmbedLinkPropertyDto),
      },
    };
  }

  async getNodeEmbedLinkCounts(nodeId: string, spaceId: string) {
    return await this.embedlinkRepository.selectCountByNodeIdAndSpaceId(nodeId, spaceId);
  }

  async getEmbedLinkPermissionType(embedLinkId: string): Promise<string> {
    return await this.embedlinkRepository.selectPermissionTypeByEmbedLinkId(embedLinkId);
  }

  async getSpaceIdByLinkId(linkId: string): Promise<string | undefined> {
    return await this.embedlinkRepository.selectSpaceIdByEmbedLinkId(linkId);
  }

  async getEmbedLinkPropsByLinkId(linkId: string): Promise<EmbedLinkPropertyDto> {
    return (await this.embedlinkRepository.selectPropsByEmbedLinkId(linkId)) as EmbedLinkPropertyDto;
  }

  async getEmbedInfoByLinkId(linkId: string): Promise<IEmbedLinkBaseInfo> {
    return await this.embedlinkRepository.selectSpaceIdAndNodIdAndPropsByEmbedLinkId(linkId);
  }

  async getResourceDataPack(
    auth: IAuthHeader,
    linkId: string,
    resourceId: string
  ): Promise<DatasheetPack | FormDataPack | IServerDashboardPack | undefined> {
    await this.isEmbedLinkIdExist(linkId);
    const embedInfo: IEmbedLinkBaseInfo = await this.getEmbedInfoByLinkId(linkId);
    const permissionType = embedInfo.props.payload!.permissionType;
    const userId = await this.userService.getUserIdBySpaceId(auth, embedInfo.spaceId);
    if (EmbedPermissionType.PrivateEdit === permissionType && !userId) {
      throw new ServerException(PermissionException.ACCESS_DENIED);
    }
    switch (resourceId.substring(0, 3)) {
      case IDPrefix.Table:
        return this.getDatasheetDataPack(auth, resourceId, !!userId, embedInfo);
      case IDPrefix.Form:
        return this.getFormDataPack(auth, resourceId, !!userId, embedInfo);
      case IDPrefix.Dashboard:
        return this.getDashboardDataPack(auth, resourceId, !!userId, embedInfo);
      default:
        return undefined;
    }
  }

  private async getFormDataPack(auth: IAuthHeader, formId: string, loggedIn: boolean, embedInfo: IEmbedLinkBaseInfo) {
    let dataPack: FormDataPack;
    const permissionType = embedInfo.props.payload!.permissionType;
    if ((EmbedPermissionType.PrivateEdit === permissionType || EmbedPermissionType.PublicEdit === permissionType) && loggedIn) {
      dataPack = await this.formService.fetchDataPack(formId, auth, undefined, embedInfo.linkId);
    } else {
      const userId = await this.getEmbedLinkLastModifiedByUserId(embedInfo.linkId);
      dataPack = await this.formService.fetchShareDataPack(formId, embedInfo.linkId, userId, { userId });
    }
    return dataPack;
  }

  private async getDashboardDataPack(auth: IAuthHeader, dashboardId: string, loggedIn: boolean, embedInfo: IEmbedLinkBaseInfo) {
    let dataPack: IServerDashboardPack;
    const permissionType = embedInfo.props.payload!.permissionType;
    if ((EmbedPermissionType.PrivateEdit === permissionType || EmbedPermissionType.PublicEdit === permissionType) && loggedIn) {
      dataPack = await this.dashboardService.fetchDashboardPack(dashboardId, auth);
    } else {
      const userId = await this.getEmbedLinkLastModifiedByUserId(embedInfo.linkId);
      dataPack = await this.dashboardService.fetchShareDashboardPack(embedInfo.linkId, dashboardId, {
        userId,
      });
    }
    if (dataPack.dashboard && dataPack.dashboard.permissions) {
      if (embedInfo.props.payload?.permissionType == EmbedPermissionType.PublicEdit) {
        dataPack.dashboard.role = ConfigConstant.permission.editor as Role;
        dataPack.dashboard.permissions = { ...DEFAULT_EDITOR_PERMISSION };
      }
    }
    return dataPack;
  }

  private async getDatasheetDataPack(auth: IAuthHeader, dstId: string, loggedIn: boolean, embedInfo: IEmbedLinkBaseInfo) {
    // check view exist
    if (dstId == embedInfo.nodeId) {
      const viewId = embedInfo.props?.payload?.viewControl?.viewId;
      if (viewId && !(await this.datasheetMetaService.isViewIdExist(embedInfo.nodeId, viewId))) {
        throw ApiException.tipError(ApiTipConstant.api_embed_link_id_not_exist);
      }
    }
    const permissionType = embedInfo.props.payload!.permissionType;
    let dataPack: DatasheetPack;
    if ((EmbedPermissionType.PrivateEdit === permissionType || EmbedPermissionType.PublicEdit === permissionType) && loggedIn) {
      dataPack = (await this.datasheetService.fetchDataPack(dstId, auth, false)) as DatasheetPack;
    } else {
      const userId = await this.getEmbedLinkLastModifiedByUserId(embedInfo.linkId);
      dataPack = (await this.datasheetService.fetchShareDataPack(
        embedInfo.linkId,
        dstId,
        {
          userId,
        },
        false
      )) as DatasheetPack;
    }
    if (dataPack.datasheet && dataPack.datasheet.permissions) {
      if (embedInfo.props.payload?.permissionType == EmbedPermissionType.PublicEdit) {
        dataPack.datasheet.role = ConfigConstant.permission.editor as Role;
        dataPack.datasheet.permissions = { ...DEFAULT_EDITOR_PERMISSION };
      }
    }
    return dataPack;
  }
}
