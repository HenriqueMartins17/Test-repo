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

import { Body, Controller, Delete, Param, Post, Put, Req, UseGuards, UseInterceptors } from '@nestjs/common';
import { ApiBearerAuth, ApiConsumes, ApiOkResponse, ApiOperation, ApiProduces, ApiTags, OmitType } from '@nestjs/swagger';
import { CreateWidgetRo, DashboardParamRo, DashboardWidgetParamRo, UpdateWidgetRo, WidgetVo } from 'enterprise/fusion/models/widget.model';
import { FusionApiEnterpriseService } from 'enterprise/fusion/services/fusion-api.enterprise.service';
import { EnterpriseGuard } from 'enterprise/share/guards/enterprise.guard';
import type { FastifyRequest } from 'fastify';
import { ApiAuthGuard } from 'fusion/middleware/guard/api.auth.guard';
import { ApiUsageGuard } from 'fusion/middleware/guard/api.usage.guard';
import { ParseObjectPipe } from 'fusion/middleware/pipe/parse.pipe';
import { ApiResponse } from 'fusion/vos/api.response';
import { NodeService } from 'node/services/node.service';
import { SwaggerConstants } from 'shared/common';
import { ApiUsageInterceptor } from 'shared/interceptor/api.usage.interceptor';
import { IAuthHeader } from 'shared/interfaces';

@ApiTags(SwaggerConstants.ENTERPRISE_TAG)
@Controller('fusion/v1')
@ApiBearerAuth()
@UseGuards(ApiAuthGuard, ApiUsageGuard, EnterpriseGuard)
@UseInterceptors(ApiUsageInterceptor)
export class FusionDashboardController {
  constructor(
    private readonly nodeService: NodeService,
    private readonly service: FusionApiEnterpriseService,
  ) {
  }

  @Post('/dashboards/:dashboardId/widgets')
  @ApiOperation({
    summary: 'Add a widget to a specified dashboard',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The widget has been successfully created.',
    type: WidgetVo,
  })
  public async createWidget(@Param() param: DashboardParamRo, @Body(new ParseObjectPipe()) body: CreateWidgetRo,
                            @Req() request: FastifyRequest): Promise<WidgetVo> {
    if (body.datasheetId) {
      await this.nodeService.checkNodeIfExist(body.datasheetId);
    }
    const auth: IAuthHeader = { token: request.headers.authorization };
    const widget = await this.service.createWidget(auth, param.dashboardId, body);
    return ApiResponse.success(widget);
  }

  @Delete('/dashboards/:dashboardId/widgets/:widgetId')
  @ApiOperation({
    summary: 'delete widget in a specified dashboard',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The widget has been successfully deleted.',
    type: OmitType(ApiResponse, ['data'] as const),
  })
  public async deleteWidget(@Param() param: DashboardWidgetParamRo, @Req() request: FastifyRequest): Promise<ApiResponse<undefined>> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    await this.service.deleteWidget(auth, param.dashboardId, param.widgetId);
    return ApiResponse.success(undefined);
  }

  @Put('/dashboards/:dashboardId/widgets/:widgetId')
  @ApiOperation({
    summary: 'modify widget in a specified dashboard',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The widget has been successfully deleted.',
    type: OmitType(ApiResponse, ['data'] as const),
  })
  public async updateWidget(@Param() param: DashboardWidgetParamRo, @Body(new ParseObjectPipe()) body: UpdateWidgetRo,
                            @Req() request: FastifyRequest): Promise<WidgetVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    const widget = await this.service.updateWidget(auth, param.dashboardId, param.widgetId, body);
    return ApiResponse.success(widget);
  }

}
