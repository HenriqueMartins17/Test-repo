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

import { ApiTipConstant, getViewTypeString } from '@apitable/core';
import { Body, Controller, Delete, Param, Post, Put, Query, Req, UseGuards, UseInterceptors } from '@nestjs/common';
import { ApiBearerAuth, ApiConsumes, ApiOkResponse, ApiOperation, ApiProduces, ApiTags, OmitType } from '@nestjs/swagger';
import { CreateViewRo, UpdateViewRo, ViewDeleteRo, ViewParam } from 'enterprise/fusion/models/view.model';
import { FusionApiEnterpriseService } from 'enterprise/fusion/services/fusion-api.enterprise.service';
import { EnterpriseGuard } from 'enterprise/share/guards/enterprise.guard';
import type { FastifyRequest } from 'fastify';
import { ApiAuthGuard } from 'fusion/middleware/guard/api.auth.guard';
import { ApiUsageGuard } from 'fusion/middleware/guard/api.usage.guard';
import { NodePermissionGuard } from 'fusion/middleware/guard/node.permission.guard';
import { ParseObjectPipe } from 'fusion/middleware/pipe/parse.pipe';
import { QueryPipe } from 'fusion/middleware/pipe/query.pipe';
import { ViewParamRo } from 'fusion/ros/view.param.ro';
import { ApiResponse } from 'fusion/vos/api.response';
import { ViewListVo } from 'fusion/vos/view.list.vo';
import { isUndefined } from 'lodash';
import { API_MAX_MODIFY_RECORD_COUNTS, DATASHEET_HTTP_DECORATE, SwaggerConstants, USER_HTTP_DECORATE } from 'shared/common';
import { ApiException } from 'shared/exception';
import { ApiUsageInterceptor } from 'shared/interceptor/api.usage.interceptor';
import { IAuthHeader } from 'shared/interfaces';
import { UnitService } from 'unit/services/unit.service';

@ApiTags(SwaggerConstants.ENTERPRISE_TAG)
@Controller('fusion/v1')
@ApiBearerAuth()
@UseGuards(ApiAuthGuard, ApiUsageGuard, EnterpriseGuard, NodePermissionGuard)
@UseInterceptors(ApiUsageInterceptor)
export class FusionViewController {
  constructor(
    private readonly unitService: UnitService,
    private readonly service: FusionApiEnterpriseService,
  ) {
  }

  @Post('/datasheets/:dstId/views')
  @ApiOperation({
    summary: 'Add a view to a specified datasheet',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The view has been successfully created.',
    type: ViewListVo,
  })
  public async createView(@Param() param: ViewParamRo, @Body(new ParseObjectPipe()) body: CreateViewRo,
                          @Req() request: FastifyRequest): Promise<ViewListVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    const view = await this.service.addView(auth, param.dstId, body);
    return ApiResponse.success({ views: [{ id: view.id, type: getViewTypeString(view.type), name: view.name }] });
  }

  @Post('/datasheets/:dstId/views/:viewId/duplicate')
  @ApiOperation({
    summary: 'copy a view at a specified datasheet',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The view has been successfully copied.',
    type: ViewListVo,
  })
  public async copyView(@Param() param: ViewParam, @Req() request: FastifyRequest): Promise<ViewListVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    const view = await this.service.copyView(auth, param.dstId, param.viewId, request[USER_HTTP_DECORATE]?.locale,);
    return ApiResponse.success({ views: [{ id: view.id, type: getViewTypeString(view.type), name: view.name }] });
  }

  @Delete('/datasheets/:dstId/views/:viewId')
  @ApiOperation({
    summary: 'Delete a view in a specified datasheet',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The view has been successfully deleted.',
    type: OmitType(ApiResponse, ['data'] as const),
  })
  public async deleteView(@Param() param: ViewParam, @Req() request: FastifyRequest): Promise<ApiResponse<undefined>> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    await this.service.deleteViews(auth, param.dstId, [param.viewId]);
    return ApiResponse.success(undefined);
  }

  @Delete('/datasheets/:dstId/views')
  @ApiOperation({
    summary: 'Batch delete views in a specified datasheet',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The view has been successfully deleted.',
    type: OmitType(ApiResponse, ['data'] as const),
  })
  public async batchDeleteView(@Param() param: ViewParamRo,
                               @Query(QueryPipe) query: ViewDeleteRo,
                               @Req() request: FastifyRequest): Promise<ApiResponse<undefined>> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    if (query.viewIds.length > API_MAX_MODIFY_RECORD_COUNTS) {
      throw ApiException.tipError(ApiTipConstant.api_params_max_count_error, { count: API_MAX_MODIFY_RECORD_COUNTS, property: 'viewIds' });
    }
    await this.service.deleteViews(auth, param.dstId, query.viewIds);
    return ApiResponse.success(undefined);
  }

  @Put('/datasheets/:dstId/views/:viewId')
  @ApiOperation({
    summary: 'Delete a view in a specified datasheet',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The view has been successfully updated.',
    type: ViewListVo,
  })
  public async updateView(@Param() param: ViewParam,
                          @Body(new ParseObjectPipe()) body: UpdateViewRo,
                          @Req() request: FastifyRequest): Promise<ViewListVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    let unitId: string | undefined;
    if (!isUndefined(body.lockInfo)) {
      const userId = request[USER_HTTP_DECORATE].id;
      const spaceId = request[DATASHEET_HTTP_DECORATE].spaceId;
      unitId = await this.unitService.getIdByUserIdAndSpaceId(userId, spaceId);
    }
    const view = await this.service.updateView(auth, param, body, unitId);
    return ApiResponse.success({ views: [{ id: view.id, type: getViewTypeString(view.type), name: view.name }] });
  }
}
