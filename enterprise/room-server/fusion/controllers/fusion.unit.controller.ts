import { Body, Controller, Delete, Get, Param, Post, Put, Query, Req, UseGuards, UseInterceptors } from '@nestjs/common';
import { ApiBearerAuth, ApiConsumes, ApiCreatedResponse, ApiOkResponse, ApiOperation, ApiProduces, ApiTags, OmitType } from '@nestjs/swagger';
import {
  MemberCreateRo,
  MemberDetailDto,
  MemberPageDto,
  MemberPageVo,
  MemberSensitiveQueryRo,
  MemberUpdateRo,
  MemberVo,
  RolePageVo,
  RoleRo,
  RoleUnitListVo,
  RoleUpdateRo,
  RoleVo,
  SpaceUnitMemberQueryRo,
  SpaceUnitParamRo,
  SpaceUnitQueryRo,
  TeamPageVo,
  TeamRo,
  TeamUpdateRo,
  TeamVo,
} from 'enterprise/fusion/models/unit.model';
import { FusionApiEnterpriseService } from 'enterprise/fusion/services/fusion-api.enterprise.service';
import { EnterpriseGuard } from 'enterprise/share/guards/enterprise.guard';
import { FastifyRequest } from 'fastify';
import { ApiAuthGuard } from 'fusion/middleware/guard/api.auth.guard';
import { ApiUsageGuard } from 'fusion/middleware/guard/api.usage.guard';
import { SpaceParamRo } from 'fusion/ros/space.param.ro';
import { ApiResponse } from 'fusion/vos/api.response';
import { SwaggerConstants } from 'shared/common';
import { UnitTypeEnum } from 'shared/enums';
import { ApiUsageInterceptor } from 'shared/interceptor/api.usage.interceptor';
import { IAuthHeader } from 'shared/interfaces';
import { UnitService } from 'unit/services/unit.service';

@ApiTags(SwaggerConstants.ENTERPRISE_TAG)
@Controller('fusion/v1')
@ApiBearerAuth()
@UseGuards(ApiAuthGuard, ApiUsageGuard, EnterpriseGuard)
@UseInterceptors(ApiUsageInterceptor)
export class FusionUnitController {
  constructor(
    private readonly service: FusionApiEnterpriseService,
    private readonly unitService: UnitService
  ) {
  }

  @Get('spaces/:spaceId/teams/:unitId/children')
  @ApiOperation({
    summary: 'Get the list of sub teams of a team by UnitId.',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'Page of team list',
    type: TeamPageVo,
  })
  public async subTeamList(@Param() param: SpaceUnitParamRo, @Query() query: SpaceUnitQueryRo, @Req() request: FastifyRequest): Promise<TeamPageVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    const teams = await this.service.getSubTeams(auth, param, query);
    return ApiResponse.success(teams);
  }

  @Post('spaces/:spaceId/teams')
  @ApiOperation({
    summary: 'Create a team for a specified space.',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiCreatedResponse({
    description: 'Created team.',
    type: TeamVo,
  })
  public async createTeam(@Param() param: SpaceParamRo, @Body() body: TeamRo, @Req() request: FastifyRequest): Promise<TeamVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    if (body.roles) {
      await this.unitService.checkUnitIdsExists(Object.assign([], body.roles), param.spaceId, UnitTypeEnum.ROLE);
    }
    const team = await this.service.createTeam(auth, param.spaceId, body);
    return ApiResponse.success(team);
  }

  @Put('spaces/:spaceId/teams/:unitId')
  @ApiOperation({
    summary: 'Update a for a specified space',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'Updated team.',
    type: TeamVo,
  })
  public async updateTeam(@Param() param: SpaceUnitParamRo, @Body() body: TeamUpdateRo, @Req() request: FastifyRequest): Promise<TeamVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    // check roles
    if (body.roles) {
      await this.unitService.checkUnitIdsExists(Object.assign([], body.roles), param.spaceId, UnitTypeEnum.ROLE);
    }
    const team = await this.service.updateTeam(auth, param, body);
    return ApiResponse.success(team);
  }

  @Delete('spaces/:spaceId/teams/:unitId')
  @ApiOperation({
    summary: 'Delete a team for a specified space',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The team has been successfully deleted.',
    type: OmitType(ApiResponse, ['data'] as const),
  })
  public async deleteTeam(@Param() param: SpaceUnitParamRo, @Req() request: FastifyRequest): Promise<ApiResponse<undefined>> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    await this.service.deleteTeam(auth, param.spaceId, param.unitId);
    return ApiResponse.success(undefined);
  }

  @Get('spaces/:spaceId/teams/:unitId/members')
  @ApiOperation({
    summary: 'List members under team.',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'Page of member list',
    type: MemberPageVo,
  })
  public async teamMemberList(
    @Param() param: SpaceUnitParamRo, @Query() query: SpaceUnitMemberQueryRo, @Req() request: FastifyRequest): Promise<MemberPageVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    const members: MemberPageDto = await this.service.getTeamMembers(auth, param, query);
    return ApiResponse.success(members);
  }

  @Get('spaces/:spaceId/roles')
  @ApiOperation({
    summary: 'Get roles for a specified space',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'Page of role list',
    type: RolePageVo,
  })
  public async roleList(@Param() param: SpaceParamRo, @Query() query: SpaceUnitQueryRo, @Req() request: FastifyRequest): Promise<RolePageVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    const roles = await this.service.getRoles(auth, param.spaceId, query);
    return ApiResponse.success(roles);
  }

  @Post('spaces/:spaceId/roles')
  @ApiOperation({
    summary: 'Create a role for a specified space.',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiCreatedResponse({
    description: 'Created role.',
    type: RoleVo,
  })
  public async createRole(@Param() param: SpaceParamRo, @Body() body: RoleRo, @Req() request: FastifyRequest): Promise<RoleVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    const role = await this.service.createRole(auth, param.spaceId, body);
    return ApiResponse.success(role);
  }

  @Put('spaces/:spaceId/roles/:unitId')
  @ApiOperation({
    summary: 'Update roles for a specified space',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'Updated role.',
    type: RoleVo,
  })
  public async updateRole(@Param() param: SpaceUnitParamRo, @Body() body: RoleUpdateRo, @Req() request: FastifyRequest): Promise<RoleVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    const role = await this.service.updateRole(auth, param.spaceId, param.unitId, body);
    return ApiResponse.success(role);
  }

  @Delete('spaces/:spaceId/roles/:unitId')
  @ApiOperation({
    summary: 'Delete a role for a specified space',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The role has been successfully deleted.',
    type: OmitType(ApiResponse, ['data'] as const),
  })
  public async deleteRole(@Param() param: SpaceUnitParamRo, @Req() request: FastifyRequest): Promise<ApiResponse<undefined>> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    await this.service.deleteRole(auth, param.spaceId, param.unitId);
    return ApiResponse.success(await undefined);
  }

  @Get('spaces/:spaceId/roles/:unitId/units')
  @ApiOperation({
    summary: 'Get the organizational units under the specified role unitId, the returned data includes teams and members.',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The units under the specified role',
    type: RoleUnitListVo,
  })
  public async roleUnitList(
    @Param() param: SpaceUnitParamRo, @Query() query: MemberSensitiveQueryRo, @Req() request: FastifyRequest): Promise<RoleUnitListVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    const roleUnits = await this.service.getRoleUnits(auth, param, query);
    return ApiResponse.success(roleUnits);
  }

  @Get('spaces/:spaceId/members/:unitId')
  @ApiOperation({
    summary: 'Get member details information',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'Member details information',
    type: MemberVo,
  })
  public async memberDetail(
    @Param() param: SpaceUnitParamRo, @Query() query: MemberSensitiveQueryRo, @Req() request: FastifyRequest): Promise<MemberVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    const member: MemberDetailDto = await this.service.getMemberDetail(auth, param, query);
    return ApiResponse.success(member);
  }

  @Post('spaces/:spaceId/members')
  @ApiOperation({
    summary: 'Create a member for a specified space',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiCreatedResponse({
    description: 'Created member',
    type: MemberVo,
  })
  public async createMember(@Param() param: SpaceParamRo, @Body() body: MemberCreateRo,): Promise<MemberVo> {
    console.log(param);
    console.log(body);
    return ApiResponse.success(await new MemberDetailDto());
  }

  @Put('spaces/:spaceId/members/:unitId')
  @ApiOperation({
    summary: 'Update a member for a specified space.',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'Updated member.',
    type: MemberVo,
  })
  public async updateMember(@Param() param: SpaceUnitParamRo, @Query() query: MemberSensitiveQueryRo,
                            @Body() body: MemberUpdateRo, @Req() request: FastifyRequest): Promise<MemberVo> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    // check roles
    if (body.roles) {
      await this.unitService.checkUnitIdsExists(Object.assign([], body.roles), param.spaceId, UnitTypeEnum.ROLE);
    }
    // check teams
    if (body.teams) {
      await this.unitService.checkUnitIdsExists(Object.assign([], body.teams), param.spaceId, UnitTypeEnum.TEAM);
    }
    const member: MemberDetailDto = await this.service.updateMember(auth, param, query, body);
    return ApiResponse.success(member);
  }

  @Delete('spaces/:spaceId/members/:unitId')
  @ApiOperation({
    summary: 'Delete a member for a specified space.',
  })
  @ApiProduces('application/json')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'The member has been successfully deleted.',
    type: OmitType(ApiResponse, ['data'] as const),
  })
  public async deleteMember(@Param() param: SpaceUnitParamRo, @Req() request: FastifyRequest): Promise<ApiResponse<undefined>> {
    const auth: IAuthHeader = { token: request.headers.authorization };
    await this.service.deleteMember(auth, param.spaceId, param.unitId);
    return ApiResponse.success(undefined);
  }
}
