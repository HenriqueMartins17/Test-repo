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
  ApiTipConstant,
  ConfigConstant,
  DEFAULT_EDITOR_PERMISSION,
  IDatasheetFieldPermission,
  IServerDashboardPack,
  IUnitValue,
  Role
} from '@apitable/core';
import { Controller, Get, Headers, Param, Query, UseGuards, UseInterceptors } from '@nestjs/common';
import { ApiOkResponse, ApiTags } from '@nestjs/swagger';
import { DashboardService } from 'database/dashboard/services/dashboard.service';
import { DatasheetMetaService } from 'database/datasheet/services/datasheet.meta.service';
import { DatasheetService } from 'database/datasheet/services/datasheet.service';
import { DatasheetPack } from 'database/interfaces';
import { ResourceDataInterceptor } from 'database/resource/middleware/resource.data.interceptor';
import { ResourceService } from 'database/resource/services/resource.service';
import { IEmbedLinkProperty } from 'enterprise/embed/entities/embedlink.entity';
import { EmbedGuard } from 'enterprise/embed/guards/embed.guard';
import { EmbedLinkDetailVo, EmbedLinkPropertyDto, EmbedPermissionType } from 'enterprise/embed/models/embedlink.model';
import { IEmbedLinkBaseInfo } from 'enterprise/embed/repositories/embedlink.repository';
import { EmbedLinkService } from 'enterprise/embed/services/embedlink.service';
import { ApiResponse } from 'fusion/vos/api.response';
import { SwaggerConstants } from 'shared/common';
import { ApiException, CommonException, ServerException } from 'shared/exception';
import { RestService } from 'shared/services/rest/rest.service';
import { UserService } from 'user/services/user.service';

@ApiTags(SwaggerConstants.ENTERPRISE_TAG)
@UseGuards(EmbedGuard)
@Controller('nest/v1')
export class EmbedlinkController {
  constructor(
    private readonly embedLinkService: EmbedLinkService,
    private readonly datasheetService: DatasheetService,
    private readonly userService: UserService,
    private readonly restService: RestService,
    private readonly resourceService: ResourceService,
    private readonly dashboardService: DashboardService,
    private readonly datasheetMetaService: DatasheetMetaService
  ) {
  }

  /**
   * @deprecated replace with getResourceDataPack()
   */
  @Get('embedlinks/:linkId/datasheets/:dstId/dataPack')
  @UseInterceptors(ResourceDataInterceptor)
  async getDataPack(@Headers('cookie') cookie: string, @Param('linkId') linkId: string, @Param('dstId') dstId: string): Promise<DatasheetPack> {
    // check if the node has been shared
    await this.embedLinkService.isEmbedLinkIdExist(linkId);
    const embedInfo: IEmbedLinkBaseInfo = await this.embedLinkService.getEmbedInfoByLinkId(linkId);
    // check view exist
    if (dstId == embedInfo.nodeId) {
      const viewId = embedInfo.props?.payload?.viewControl?.viewId;
      if (viewId && !(await this.datasheetMetaService.isViewIdExist(embedInfo.nodeId, viewId))) {
        throw ApiException.tipError(ApiTipConstant.api_embed_link_id_not_exist);
      }
    }
    const permissionType = embedInfo.props.payload!.permissionType;
    let userId = await this.userService.getUserIdBySpaceId({ cookie }, embedInfo.spaceId);
    if (EmbedPermissionType.PrivateEdit === permissionType && !userId) {
      throw new ServerException(CommonException.UNAUTHORIZED);
    }
    let dataPack: DatasheetPack;
    if ((EmbedPermissionType.PrivateEdit === permissionType || EmbedPermissionType.PublicEdit === permissionType) && userId) {
      dataPack = (await this.datasheetService.fetchDataPack(dstId, { cookie }, false)) as DatasheetPack;
    } else {
      userId = await this.embedLinkService.getEmbedLinkLastModifiedByUserId(linkId);
      dataPack = (await this.datasheetService.fetchShareDataPack(
        linkId,
        dstId,
        {
          userId,
        },
        false,
      )) as DatasheetPack;
    }
    if (dataPack.datasheet && dataPack.datasheet.permissions) {
      const props: IEmbedLinkProperty = embedInfo.props;
      if (props.payload?.permissionType == EmbedPermissionType.PublicEdit) {
        dataPack.datasheet.role = ConfigConstant.permission.editor as Role;
        dataPack.datasheet.permissions = { ...DEFAULT_EDITOR_PERMISSION };
      }
    }
    return dataPack;
  }

  @Get('embedlinks/:linkId/node/filed/permission')
  async getFieldPermission(
    @Headers('cookie') cookie: string,
    @Param('linkId') linkId: string,
    @Query('dstIds') dstIds: string[],
  ): Promise<ApiResponse<IDatasheetFieldPermission[]>> {
    // check if the node has been shared
    await this.embedLinkService.isEmbedLinkIdExist(linkId);
    const spaceId = await this.embedLinkService.getSpaceIdByLinkId(linkId);
    let userId = await this.userService.getUserIdBySpaceId({ cookie }, spaceId!);
    if (!userId) {
      userId = await this.embedLinkService.getEmbedLinkLastModifiedByUserId(linkId);
    }
    const data = await this.restService.getNodesFieldPermission({ userId, cookie }, dstIds);
    return ApiResponse.success(data);
  }

  @Get('embedlinks/:linkId')
  @ApiOkResponse({
    description: 'get embedlinks description.',
    type: EmbedLinkDetailVo,
  })
  // @UseInterceptors(ResourceDataInterceptor)
  async getEmbedLinkInfo(@Param('linkId') linkId: string): Promise<EmbedLinkDetailVo> {
    // check if the node has been shared
    await this.embedLinkService.isEmbedLinkIdExist(linkId);
    const embedLinkDetail = await this.embedLinkService.getEmbedLinkDetailInfoByEmbedLinkId(linkId);
    return ApiResponse.success(embedLinkDetail);
  }

  @Get('embedlinks/:linkId/resources/:resourceId/foreignDatasheets/:foreignDatasheetId/dataPack')
  // @UseInterceptors(ResourceDataInterceptor)
  async getForeignDataPack(
    @Headers('cookie') cookie: string,
    @Param('linkId') linkId: string,
    @Param('resourceId') resourceId: string,
    @Param('foreignDatasheetId') foreignDatasheetId: string,
  ): Promise<DatasheetPack> {
    // check if the node has been shared
    await this.embedLinkService.isEmbedLinkIdExist(linkId);
    const permissionType = await this.embedLinkService.getEmbedLinkPermissionType(linkId);
    if (await this.userService.session(cookie) && permissionType == EmbedPermissionType.PrivateEdit) {
      // has login: use internal permission
      return await this.resourceService.fetchForeignDatasheetPack(resourceId, foreignDatasheetId, { cookie }, true);
    }
    // use default permission
    const userId = await this.embedLinkService.getEmbedLinkLastModifiedByUserId(linkId);
    return await this.resourceService.fetchForeignDatasheetPack(resourceId, foreignDatasheetId, { cookie, userId }, true, linkId);
  }

  @Get('embedlinks/:linkId/org/loadOrSearch')
  async unitLoadOrSearch(
    @Headers('cookie') cookie: string,
    @Param('linkId') linkId: string,
    @Query('unitIds') unitIds: string,
    @Query('filterIds') filterIds: string,
    @Query('all') all: boolean,
    @Query('searchEmail') searchEmail: boolean,
    @Query('keyword') keyword: string,
  ): Promise<ApiResponse<IUnitValue[]>> {
    // check if the node has been shared
    await this.embedLinkService.isEmbedLinkIdExist(linkId);
    const spaceId = await this.embedLinkService.getSpaceIdByLinkId(linkId);
    let userId = await this.userService.getUserIdBySpaceId({ cookie }, spaceId!);
    if (!userId) {
      userId = await this.embedLinkService.getEmbedLinkLastModifiedByUserId(linkId);
    }
    const data = await this.restService.unitLoadOrSearch({ userId, cookie }, spaceId!, { unitIds, filterIds, all, searchEmail, keyword, userId });
    return ApiResponse.success(data);
  }

  /**
   * @deprecated replace with getResourceDataPack()
   */
  @Get('embedlinks/:linkId/dashboards/:dashboardId/dataPack')
  @UseInterceptors(ResourceDataInterceptor)
  async getDashboardPack(@Headers('cookie') cookie: string, @Param('linkId') linkId: string, @Param('dashboardId') dashboardId: string) {
    // check if the node has been shared
    await this.embedLinkService.isEmbedLinkIdExist(linkId);
    const permissionType = await this.embedLinkService.getEmbedLinkPermissionType(linkId);
    const spaceId = await this.embedLinkService.getSpaceIdByLinkId(linkId);
    let userId = await this.userService.getUserIdBySpaceId({ cookie }, spaceId!);
    if (EmbedPermissionType.PrivateEdit === permissionType && !userId) {
      throw new ServerException(CommonException.UNAUTHORIZED);
    }
    let dataPack: IServerDashboardPack;
    if ((EmbedPermissionType.PrivateEdit === permissionType || EmbedPermissionType.PublicEdit === permissionType) && userId) {
      dataPack = await this.dashboardService.fetchDashboardPack(dashboardId, { cookie, token: '' });
    } else {
      userId = await this.embedLinkService.getEmbedLinkLastModifiedByUserId(linkId);
      dataPack = await this.dashboardService.fetchShareDashboardPack(linkId, dashboardId, {
        userId,
      });
    }
    if (dataPack.dashboard && dataPack.dashboard.permissions) {
      const props: EmbedLinkPropertyDto = await this.embedLinkService.getEmbedLinkPropsByLinkId(linkId);
      if (props.payload.permissionType == EmbedPermissionType.PublicEdit) {
        dataPack.dashboard.role = ConfigConstant.permission.editor as Role;
        dataPack.dashboard.permissions = { ...DEFAULT_EDITOR_PERMISSION };
      }
    }
    return dataPack;
  }

  @Get('embedlinks/:linkId/resources/:resourceId/dataPack')
  @UseInterceptors(ResourceDataInterceptor)
  async getResourceDataPack(@Headers('cookie') cookie: string, @Param('linkId') linkId: string, @Param('resourceId') resourceId: string) {
    return await this.embedLinkService.getResourceDataPack({ cookie }, linkId, resourceId);
  }
}
