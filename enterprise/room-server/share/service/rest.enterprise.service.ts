/**
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import { ApiTipConstant, IWidget } from '@apitable/core';
import { HttpService } from '@nestjs/axios';
import { Injectable, Logger } from '@nestjs/common';
import { MemberUpdateRo, RoleRo, RoleUpdateRo, TeamRo, TeamUpdateRo } from 'enterprise/fusion/models/unit.model';
import { OrganizationException } from 'enterprise/share/exception/organization.exception';
import { IPage, IPageQueryInfo, IRoleUnit, IUnitMember, IUnitRole, IUnitTeam } from 'enterprise/share/interface/rest.interface';
import { lastValueFrom } from 'rxjs';
import { ApiException, CommonException, ResourceException, ServerException } from 'shared/exception';
import { HttpHelper } from 'shared/helpers';
import { IAuthHeader, IHttpSuccessResponse } from 'shared/interfaces';
import { responseCodeHandler as _responseCodeHandler } from 'shared/services/rest/response.code.handler';
import { sprintf } from 'sprintf-js';

/**
 * RestApi service
 */
@Injectable()
export class RestEnterpriseService {
  private TEAM_CHILDREN_LIST = '/internal/org/teams/%(unitId)s/children';

  private TEAM_MEMBER_LIST = '/internal/org/teams/%(unitId)s/members';

  private ADD_TEAM = '/internal/org/teams/create';

  private UPDATE_TEAM = '/internal/org/teams/update/%(unitId)s';

  private DELETE_TEAM = '/internal/org/teams/delete/%(unitId)s';

  private ROLE_LIST = '/internal/org/roles';

  private ROLE_UNIT_LIST = '/internal/org/roles/%(unitId)s/members';

  private ADD_ROLE = '/internal/org/roles';

  private UPDATE_ROLE = '/internal/org/roles/%(unitId)s';

  private MEMBER_INFO = '/internal/org/members/%(unitId)s';

  private CREATE_WIDGET = 'widget/create';

  private readonly logger = new Logger(RestEnterpriseService.name);

  constructor(private readonly httpService: HttpService) {
    // Intercept request
    this.httpService.axiosRef.interceptors.request.use(
      config => {
        config.headers!['X-Internal-Request'] = 'yes';
        return config;
      },
      error => {
        this.logger.error('Remote call failed', error);
        throw new ServerException(CommonException.SERVER_ERROR);
      },
    );
    this.httpService.axiosRef.interceptors.response.use(
      res => {
        const restResponse = res.data as IHttpSuccessResponse<any>;
        if (!restResponse.success) {
          this.logger.error(`Server request ${res.config.url} failed, error code:[${restResponse.code}], error:[${restResponse.message}]`);
          this.responseCodeHandler(restResponse.code);
        }
        return restResponse;
      },
      error => {
        // Request failed, may be network issue or HttpException
        this.logger.error('Request failed, may be network issue or server issue', error);
        throw new ServerException(CommonException.SERVER_ERROR);
      },
    );
  }

  async getSubTeamsPageInfo(auth: IAuthHeader, spaceId: string, unitId: string, page: IPageQueryInfo): Promise<IPage<IUnitTeam>> {
    const response = await lastValueFrom(
      this.httpService.get(sprintf(this.TEAM_CHILDREN_LIST, { unitId }), {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
        params: {
          pageObjectParams: page
        }
      })
    );
    return response!.data;
  }

  async createUnitTeam(auth: IAuthHeader, spaceId: string, body: TeamRo): Promise<IUnitTeam> {
    const response = await lastValueFrom(
      this.httpService.post(this.ADD_TEAM, {
        parentIdUnitId: body.parentUnitId || '0',
        teamName: body.name,
        sequence: body.sequence,
        roleUnitIds: body.roles
      }, {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
      })
    );
    return response!.data;
  }

  async updateUnitTeam(auth: IAuthHeader, spaceId: string, unitId: string, body: TeamUpdateRo): Promise<IUnitTeam> {
    const response = await lastValueFrom(
      this.httpService.post(sprintf(this.UPDATE_TEAM, { unitId }), {
        parentIdUnitId: body.parentUnitId || '0',
        teamName: body.name,
        sequence: body.sequence,
        roleUnitIds: body.roles
      }, {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
      })
    );
    return response!.data;
  }

  async deleteUnitTeam(auth: IAuthHeader, spaceId: string, unitId: string): Promise<void> {
    await lastValueFrom(
      this.httpService.delete(sprintf(this.DELETE_TEAM, { unitId }), {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
      })
    );
  }

  async getTeamMemberPageInfo(
    auth: IAuthHeader, spaceId: string, unitId: string, sensitiveData: boolean, page: IPageQueryInfo): Promise<IPage<IUnitMember>> {
    const response = await lastValueFrom(
      this.httpService.get(sprintf(this.TEAM_MEMBER_LIST, { unitId }), {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
        params: {
          pageObjectParams: page,
          sensitiveData
        },
      })
    );
    return response!.data;
  }

  async getRolesPageInfo(auth: IAuthHeader, spaceId: string, page: IPageQueryInfo): Promise<IPage<IUnitRole>> {
    const response = await lastValueFrom(
      this.httpService.get(this.ROLE_LIST, {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
        params: {
          pageObjectParams: page
        }
      })
    );
    return response!.data;
  }

  async getRoleUnits(auth: IAuthHeader, spaceId: string, unitId: string, sensitiveData: boolean = false): Promise<IRoleUnit> {
    const response = await lastValueFrom(
      this.httpService.get(sprintf(this.ROLE_UNIT_LIST, { unitId }), {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
        params: {
          sensitiveData
        }
      })
    );
    return response!.data;
  }

  async createUnitRole(auth: IAuthHeader, spaceId: string, body: RoleRo): Promise<IUnitRole> {
    const response = await lastValueFrom(
      this.httpService.post(this.ADD_ROLE, {
        roleName: body.name,
        position: body.sequence,
      }, {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
      })
    );
    return response!.data;
  }

  async updateUnitRole(auth: IAuthHeader, spaceId: string, unitId: string, body: RoleUpdateRo): Promise<IUnitRole> {
    const response = await lastValueFrom(
      this.httpService.post(sprintf(this.UPDATE_ROLE, { unitId }), {
        roleName: body.name,
        position: body.sequence,
      }, {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
      })
    );
    return response!.data;
  }

  async deleteUnitRole(auth: IAuthHeader, spaceId: string, unitId: string): Promise<void> {
    await lastValueFrom(
      this.httpService.delete(sprintf(this.UPDATE_ROLE, { unitId }), {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
      })
    );
  }

  async getUnitMemberDetail(
    auth: IAuthHeader, spaceId: string, unitId: string, sensitiveData: boolean): Promise<IUnitMember> {
    const response = await lastValueFrom(
      this.httpService.get(sprintf(this.MEMBER_INFO, { unitId }), {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
        params: {
          sensitiveData
        },
      })
    );
    return response!.data;
  }

  async updateUnitMember(auth: IAuthHeader, spaceId: string, unitId: string, sensitiveData: boolean, body: MemberUpdateRo): Promise<IUnitMember> {
    const response = await lastValueFrom(
      this.httpService.post(sprintf(this.MEMBER_INFO, { unitId }), {
        memberName: body.name,
        teamUnitIds: body.teams,
        roleUnitIds: body.roles
      }, {
        params: {
          sensitiveData
        },
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
      })
    );
    return response!.data;
  }

  async deleteUnitMember(auth: IAuthHeader, spaceId: string, unitId: string): Promise<void> {
    await lastValueFrom(
      this.httpService.delete(sprintf(this.MEMBER_INFO, { unitId }), {
        headers: HttpHelper.withSpaceIdHeader(HttpHelper.createAuthHeaders(auth), spaceId),
      })
    );
  }

  async createWidget(headers: IAuthHeader, dashboardId: string, widgetPackageId: string, name?: string): Promise<IWidget> {
    const response = await lastValueFrom(
      this.httpService.post(
        this.CREATE_WIDGET,
        {
          nodeId: dashboardId,
          widgetPackageId,
          name,
        },
        {
          headers: HttpHelper.createAuthHeaders(headers),
        },
      ),
    );
    return response!.data;
  }

  private responseCodeHandler(code: number) {
    switch (code) {
      case OrganizationException.ILLEGAL_TEAM_PERMISSION.code:
        throw ApiException.tipError(ApiTipConstant.api_org_permission_team_deny);
      case OrganizationException.ILLEGAL_MEMBER_PERMISSION.code:
        throw ApiException.tipError(ApiTipConstant.api_org_permission_member_deny);
      case OrganizationException.ILLEGAL_ROLE_PERMISSION.code:
        throw ApiException.tipError(ApiTipConstant.api_org_permission_role_deny);
      case OrganizationException.NO_ALLOW_OPERATE.code:
        throw ApiException.tipError(ApiTipConstant.api_org_update_deny_for_social_space);
      case OrganizationException.DUPLICATION_TEAM_NAME.code:
        throw ApiException.tipError(ApiTipConstant.api_org_team_name_unique_error);
      case OrganizationException.DUPLICATION_ROLE_NAME.code:
        throw ApiException.tipError(ApiTipConstant.api_org_role_name_unique_error);
      case OrganizationException.GET_TEAM_ERROR.code:
      case OrganizationException.NOT_EXIST_ROLE.code:
      case OrganizationException.NOT_EXIST_MEMBER.code:
        throw ApiException.tipError(ApiTipConstant.api_param_unit_not_exists);
      case OrganizationException.GET_PARENT_TEAM_ERROR.code:
        throw ApiException.tipError(ApiTipConstant.api_param_parent_unit_not_exists);
      case OrganizationException.TEAM_HAS_SUB.code:
      case OrganizationException.TEAM_HAS_MEMBER.code:
        throw ApiException.tipError(ApiTipConstant.api_org_team_delete_error);
      case OrganizationException.DELETE_SPACE_ADMIN_ERROR.code:
        throw ApiException.tipError(ApiTipConstant.api_org_member_delete_primary_admin_error);
      case OrganizationException.ROLE_EXIST_ROLE_MEMBER.code:
        throw ApiException.tipError(ApiTipConstant.api_org_role_delete_error);
      case ResourceException.WIDGET_NUMBER_LIMIT.code:
        throw ApiException.tipError(ApiTipConstant.api_widget_number_limit);
      default:
        _responseCodeHandler(code);
    }
  }
}
