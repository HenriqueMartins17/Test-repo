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

import { ApiTipConstant, IDPrefix } from '@apitable/core';
import { Body, Controller, Delete, Get, Param, Post, Req, UseGuards, UseInterceptors } from '@nestjs/common';
import { ApiBearerAuth, ApiConsumes, ApiCreatedResponse, ApiOkResponse, ApiOperation, ApiProduces, ApiTags, OmitType } from '@nestjs/swagger';
import { DatasheetMetaService } from 'database/datasheet/services/datasheet.meta.service';
import { EmbedGuard } from 'enterprise/embed/guards/embed.guard';
import {
  CreateEmbedLinkParamRo,
  CreateEmbedLinkVo,
  DeleteEmbedLinkParamRo,
  EmbedLinkDto,
  EmbedLinkListVo,
  EmbedLinkPropertyDto,
} from 'enterprise/embed/models/embedlink.model';
import { EmbedLinkService } from 'enterprise/embed/services/embedlink.service';
import type { FastifyRequest } from 'fastify';
import { ApiAuthGuard } from 'fusion/middleware/guard/api.auth.guard';
import { ApiUsageGuard } from 'fusion/middleware/guard/api.usage.guard';
import { NodePermissionGuard } from 'fusion/middleware/guard/node.permission.guard';
import { ParseObjectPipe } from 'fusion/middleware/pipe/parse.pipe';
import { ApiResponse } from 'fusion/vos/api.response';
import { EMBED_LINK_URL_TEMPLATE, NODE_MAX_EMBED_LINK_COUNTS, NodePermissions, SwaggerConstants, USER_HTTP_DECORATE } from 'shared/common';
import { NodePermissionEnum } from 'shared/enums/node.permission.enum';
import { ApiException } from 'shared/exception';
import { ApiUsageInterceptor } from 'shared/interceptor/api.usage.interceptor';
import { Md5 } from 'ts-md5';
import * as util from 'util';

@ApiTags(SwaggerConstants.ENTERPRISE_TAG)
@Controller('fusion/v1')
@ApiBearerAuth()
@UseGuards(ApiAuthGuard, ApiUsageGuard, EmbedGuard, NodePermissionGuard)
@UseInterceptors(ApiUsageInterceptor)
export class FusionEmbedController {
  constructor(
    private readonly embedLinkService: EmbedLinkService,
    private readonly datasheetMetaService: DatasheetMetaService,
  ) {
  }

  @Post('spaces/:spaceId/nodes/:nodeId/embedlinks')
  @ApiOperation({
    summary: 'Creates an "embed link" for the specified node',
    description:
      'If all parameter values in the payload are the same, the generated linkId (the last string of the url path) is the same. \n' +
      'If the parameter value of the payload is different, the generated linkId is also different',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiCreatedResponse({
    description: 'The record has been successfully created.',
    type: CreateEmbedLinkVo,
  })
  @NodePermissions(NodePermissionEnum.EDITABLE)
  public async createEmbedLink(
    @Param() param: CreateEmbedLinkParamRo,
    @Body(new ParseObjectPipe()) body: EmbedLinkPropertyDto,
    @Req() request: FastifyRequest,
  ): Promise<CreateEmbedLinkVo> {
    // check view id if exists
    const viewId = body.payload.viewControl.viewId;
    if (viewId && !(await this.datasheetMetaService.isViewIdExist(param.nodeId, viewId))) {
      throw ApiException.tipError(ApiTipConstant.api_param_view_not_exists);
    }
    // check limit
    const count = await this.embedLinkService.getNodeEmbedLinkCounts(param.nodeId, param.spaceId);
    if (count >= NODE_MAX_EMBED_LINK_COUNTS) {
      throw ApiException.tipError(ApiTipConstant.api_embed_link_limit, { value: NODE_MAX_EMBED_LINK_COUNTS });
    }
    const userId = request[USER_HTTP_DECORATE].id;
    const embedLinkId = IDPrefix.EmbedLink + Md5.hashStr(param.spaceId + param.nodeId + JSON.stringify(body)).slice(8, 24);
    await this.embedLinkService.createEmbedLink(userId, param.spaceId, param.nodeId, embedLinkId, body);
    return ApiResponse.success({ linkId: embedLinkId, url: util.format(EMBED_LINK_URL_TEMPLATE, embedLinkId), ...body });
  }

  @Get('spaces/:spaceId/nodes/:nodeId/embedlinks')
  @ApiOperation({
    summary: 'Get all embedded links for a specified node',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The record has been successfully created.',
    type: EmbedLinkListVo,
  })
  @NodePermissions(NodePermissionEnum.EDITABLE)
  public async getEmbedLinkList(@Param() param: CreateEmbedLinkParamRo): Promise<EmbedLinkListVo> {
    const dtos: EmbedLinkDto[] = await this.embedLinkService.getAllEmbedLinks(param.spaceId, param.nodeId);
    return ApiResponse.success(dtos);
  }

  @Delete('spaces/:spaceId/nodes/:nodeId/embedlinks/:linkId')
  @ApiOperation({
    summary: 'Removes the specified Advanced Embed link. After deleted, the link cannot be accessed.',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The record has been successfully deleted.',
    type: OmitType(ApiResponse, ['data'] as const),
  })
  @NodePermissions(NodePermissionEnum.EDITABLE)
  public async deleteEmbedLink(@Param() param: DeleteEmbedLinkParamRo, @Req() request: FastifyRequest): Promise<ApiResponse<undefined>> {
    const userId = request[USER_HTTP_DECORATE].id;
    await this.embedLinkService.deleteEmbedLink(param.linkId, userId);
    return ApiResponse.success(undefined);
  }
}
