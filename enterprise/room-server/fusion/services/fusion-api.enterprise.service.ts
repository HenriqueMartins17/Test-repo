import { ApiTipConstant, databus, ExecuteResult, getUniqName, getViewTypeByString, IViewProperty, IWidget, Strings } from '@apitable/core';
import { Injectable } from '@nestjs/common';
import { plainToClass } from 'class-transformer';
import { isNotEmpty } from 'class-validator';
import { MetaService } from 'database/resource/services/meta.service';
import {
  MemberDetailDto,
  MemberPageDto,
  MemberSensitiveQueryRo,
  MemberUpdateRo,
  RoleDetailDto,
  RolePageDto,
  RoleRo,
  RoleUnitDetailDto,
  RoleUpdateRo,
  SpaceUnitMemberQueryRo,
  SpaceUnitParamRo,
  SpaceUnitQueryRo,
  TeamDetailDto,
  TeamPageDto,
  TeamRo,
  TeamUpdateRo
} from 'enterprise/fusion/models/unit.model';
import { CreateViewRo, UpdateViewRo, ViewParam } from 'enterprise/fusion/models/view.model';
import { CreateWidgetRo, UpdateWidgetRo, WidgetDto, WidgetLayout } from 'enterprise/fusion/models/widget.model';
import { IPage, IRoleUnit, IUnitMember, IUnitRole, IUnitTeam } from 'enterprise/share/interface/rest.interface';
import { RestEnterpriseService } from 'enterprise/share/service/rest.enterprise.service';
import { DataBusService } from 'fusion/services/databus/databus.service';
import { find, isEqual, last } from 'lodash';
import { I18nService } from 'nestjs-i18n';
import { ApiException } from 'shared/exception';
import { IAuthHeader } from 'shared/interfaces';

@Injectable()
export class FusionApiEnterpriseService {
  constructor(
    private readonly databusService: DataBusService,
    private readonly i18n: I18nService,
    private readonly restEnterpriseService: RestEnterpriseService,
    private readonly metaService: MetaService) {
  }

  async addView(auth: IAuthHeader, datasheetId: string, body: CreateViewRo): Promise<IViewProperty> {
    const datasheet = await this.getDatasheet(auth, datasheetId);
    const view = (await datasheet.getView())!;
    const viewType = getViewTypeByString(body.type);
    const defaultViewProperty = await view.deriveDefaultViewProperty(viewType);
    if (!defaultViewProperty) {
      throw Error(`Unexpected view type ${viewType}!`);
    }
    body.overrideViewProperty(datasheet.snapshot, defaultViewProperty);
    const result = await datasheet.addViews([{ view: defaultViewProperty }], { auth });
    if (result.result !== ExecuteResult.Success) {
      throw ApiException.tipError(ApiTipConstant.api_insert_error);
    }
    return last(datasheet.snapshot.meta.views)!;
  }

  async copyView(auth: IAuthHeader, datasheetId: string, viewId: string, locale: string): Promise<IViewProperty> {
    const datasheet = await this.getDatasheet(auth, datasheetId);
    const index = datasheet.snapshot.meta.views.findIndex((view: IViewProperty) => view.id === viewId);
    const view = await datasheet.getView(viewId);
    if (!view) {
      throw ApiException.tipError(ApiTipConstant.api_param_view_not_exists);
    }
    const defaultViewProperty = await view.deriveDefaultViewProperty();
    if (!defaultViewProperty) {
      throw Error(`Unexpected view type ${view.type}!`);
    }
    const { id: newId } = defaultViewProperty;
    const result = await datasheet.addViews(
      [
        {
          startIndex: index + 1,
          view: {
            ...view.property,
            id: newId,
            name: getUniqName(
              view.name + (await this.i18n.translate(Strings.copy, { lang: locale })),
              datasheet.snapshot.meta.views.map((v: IViewProperty) => v.name),
            ),
            lockInfo: undefined,
          },
        },
      ],
      { auth },
    );
    if (result.result !== ExecuteResult.Success) {
      throw ApiException.tipError(ApiTipConstant.api_insert_error);
    }
    return find(datasheet.snapshot.meta.views, function(view) {
      return view.id === newId;
    })!;
  }

  async updateView(auth: IAuthHeader, param: ViewParam, body: UpdateViewRo, unitId?: string): Promise<IViewProperty> {
    const datasheet = await this.getDatasheet(auth, param.dstId);
    const view = await datasheet.getView(param.viewId);
    if (!view) {
      throw ApiException.tipError(ApiTipConstant.api_param_view_not_exists);
    }
    // TODO transaction
    await this.updateViewName(auth, view, body);
    await this.setViewLockInfo(auth, view, body, unitId);
    await this.setViewAutoSave(auth, view, body);
    await this.moveView(auth, view, body);
    return find(datasheet.snapshot.meta.views, o => o.id === param.viewId)!;
  }

  async deleteViews(auth: IAuthHeader, datasheetId: string, viewIds: string[]): Promise<void> {
    const datasheet = await this.getDatasheet(auth, datasheetId);
    for (const viewId of viewIds) {
      const view = await datasheet.getView(viewId);
      if (!view) {
        throw ApiException.tipError(ApiTipConstant.api_param_view_not_exists);
      }
    }
    const result = await datasheet.deleteViews(viewIds, { auth });
    if (result.result !== ExecuteResult.Success) {
      throw ApiException.tipError(ApiTipConstant.api_delete_error);
    }
  }

  async updateViewName(auth: IAuthHeader, view: databus.View, body: UpdateViewRo): Promise<void> {
    if (body.name && view.name !== body.name) {
      const result = await view.modify(
        {
          key: 'name',
          value: body.name,
        },
        { auth },
      );
      if (result.result !== ExecuteResult.Success) {
        throw ApiException.tipError(ApiTipConstant.api_update_error);
      }
    }
  }

  async setViewLockInfo(auth: IAuthHeader, view: databus.View, body: UpdateViewRo, unitId?: string): Promise<void> {
    if (!unitId) return;

    // pass null value to release lock
    if (body.lockInfo === undefined) {
      return;
    }
    if (isEqual(view.property.lockInfo, body.lockInfo) || (view.property.lockInfo === undefined && body.lockInfo === null)) {
      return;
    }
    const result = await view.setLockInfo(body.lockInfo === null ? null : { description: body.lockInfo!.description, unitId }, { auth });
    if (result.result !== ExecuteResult.Success) {
      throw ApiException.tipError(ApiTipConstant.api_set_view_lock_error);
    }
  }

  async setViewAutoSave(auth: IAuthHeader, view: databus.View, body: UpdateViewRo): Promise<void> {
    if (body.autoSave !== undefined && body.autoSave !== view.property.autoSave) {
      const result = await view.setAutoSave(body.autoSave, { auth });
      if (result.result !== ExecuteResult.Success) {
        throw ApiException.tipError(ApiTipConstant.api_set_view_lock_error);
      }
    }
  }

  async moveView(auth: IAuthHeader, view: databus.View, body: UpdateViewRo): Promise<void> {
    if (body.sequence !== undefined && view.index != body.sequence) {
      const result = await view.move({ newIndex: body.sequence }, { auth });
      if (result.result !== ExecuteResult.Success) {
        throw ApiException.tipError(ApiTipConstant.api_update_error);
      }
    }
  }

  async createWidget(auth: IAuthHeader, dashboardId: string, body: CreateWidgetRo): Promise<WidgetDto> {
    const widget: IWidget = await this.restEnterpriseService.createWidget(auth, dashboardId, body.widgetPackageId, body.name);
    const dashboard = await this.databusService.getDashboard(dashboardId, {
      loadOptions: { auth },
    });
    if (!dashboard) {
      throw ApiException.tipError(ApiTipConstant.api_dashboard_not_exist);
    }
    // TODO transaction
    await dashboard.addWidgets([widget.id], { auth });
    dashboard.setWidgetInstalled(widget);
    const revision = await this.metaService.selectRevisionByResourceId(dashboardId);
    dashboard.setRevision(revision?.revision!);
    if (body.datasheetId) {
      await dashboard.setWidgetDependencyDatasheet({ widgetId: widget.id, dstId: body.datasheetId }, { auth });
    }
    const newLayout = body.overrideLayoutProperty(widget.id, dashboard.snapshot);
    if (newLayout) {
      await dashboard.changeLayout(newLayout, { auth });
    }
    return {
      widgetId: widget.id,
      name: widget.snapshot.widgetName,
      layout: this.getWidgetLayout(widget.id, dashboard)
    };
  }

  async deleteWidget(auth: IAuthHeader, dashboardId: string, widgetId: string): Promise<void> {
    const dashboard = await this.databusService.getDashboard(dashboardId, {
      loadOptions: { auth },
    });
    if (!dashboard) {
      throw ApiException.tipError(ApiTipConstant.api_dashboard_not_exist);
    }
    if (!dashboard.widgetMap[widgetId]) {
      throw ApiException.tipError(ApiTipConstant.api_param_widget_id_not_exists);
    }
    // todo move to databus
    dashboard.setWidgetInstalled((dashboard.widgetMap[widgetId] as any) as IWidget);
    await dashboard.deleteWidget(widgetId, { auth });
  }

  async updateWidget(auth: IAuthHeader, dashboardId: string, widgetId: string, body: UpdateWidgetRo): Promise<WidgetDto> {
    const dashboard = await this.databusService.getDashboard(dashboardId, { loadOptions: { auth }});
    if (!dashboard) {
      throw ApiException.tipError(ApiTipConstant.api_dashboard_not_exist);
    }
    if (!dashboard.widgetMap[widgetId]) {
      throw ApiException.tipError(ApiTipConstant.api_param_widget_id_not_exists);
    }
    if (isNotEmpty(body.name)) {
      await dashboard.setWidgetName(widgetId, body.name!, { auth });
    }
    const newLayout = body.overrideLayoutProperty(widgetId, dashboard.snapshot);
    if (newLayout) {
      await dashboard.changeLayout(newLayout, { auth });
    }
    return {
      widgetId,
      name: body.name || dashboard.widgetMap[widgetId]!.snapshot.widgetName,
      layout: this.getWidgetLayout(widgetId, dashboard)
    };
  }

  async getSubTeams(auth: IAuthHeader, param: SpaceUnitParamRo, query: SpaceUnitQueryRo): Promise<TeamPageDto> {
    const teams: IPage<IUnitTeam> = await this.restEnterpriseService.getSubTeamsPageInfo(auth, param.spaceId, param.unitId, {
      pageNo: query.pageNum!,
      pageSize: query.pageSize!
    });
    return {
      pageSize: teams.size,
      total: teams.total,
      pageNum: teams.pageNum,
      teams: teams.records,
    };
  }

  async getTeamMembers(auth: IAuthHeader, param: SpaceUnitParamRo, query: SpaceUnitMemberQueryRo): Promise<MemberPageDto> {
    const result: IPage<IUnitMember> = await this.restEnterpriseService.getTeamMemberPageInfo(auth, param.spaceId, param.unitId,
      query.sensitiveData!, { pageNo: query.pageNum!, pageSize: query.pageSize! });
    return {
      pageSize: result.size,
      total: result.total,
      pageNum: result.pageNum,
      members: result.records
    };
  }

  async createTeam(auth: IAuthHeader, spaceId: string, body: TeamRo): Promise<TeamDetailDto> {
    const team: IUnitTeam = await this.restEnterpriseService.createUnitTeam(auth, spaceId, body);
    return plainToClass(TeamDetailDto, { team });
  }

  async updateTeam(auth: IAuthHeader, param: SpaceUnitParamRo, body: TeamUpdateRo): Promise<TeamDetailDto> {
    const team: IUnitTeam = await this.restEnterpriseService.updateUnitTeam(auth, param.spaceId, param.unitId, body);
    return plainToClass(TeamDetailDto, { team });
  }

  async deleteTeam(auth: IAuthHeader, spaceId: string, unitId: string): Promise<void> {
    await this.restEnterpriseService.deleteUnitTeam(auth, spaceId, unitId);
  }

  async getRoles(auth: IAuthHeader, spaceId: string, query: SpaceUnitQueryRo): Promise<RolePageDto> {
    const result: IPage<IUnitRole> = await this.restEnterpriseService.getRolesPageInfo(auth, spaceId, {
      pageNo: query.pageNum!,
      pageSize: query.pageSize!
    });
    return {
      pageSize: result.size,
      total: result.total,
      pageNum: result.pageNum,
      roles: result.records
    };
  }

  async getRoleUnits(auth: IAuthHeader, param: SpaceUnitParamRo, query: MemberSensitiveQueryRo): Promise<RoleUnitDetailDto> {
    const result: IRoleUnit = await this.restEnterpriseService.getRoleUnits(auth, param.spaceId, param.unitId, query.sensitiveData);
    return {
      members: result.members,
      teams: result.teams,
    };
  }

  async createRole(auth: IAuthHeader, spaceId: string, body: RoleRo): Promise<RoleDetailDto> {
    const role: IUnitRole = await this.restEnterpriseService.createUnitRole(auth, spaceId, body);
    return plainToClass(RoleDetailDto, { role });
  }

  async updateRole(auth: IAuthHeader, spaceId: string, unitId: string, body: RoleUpdateRo): Promise<RoleDetailDto> {
    const role: IUnitRole = await this.restEnterpriseService.updateUnitRole(auth, spaceId, unitId, body);
    return plainToClass(RoleDetailDto, { role });
  }

  async deleteRole(auth: IAuthHeader, spaceId: string, unitId: string): Promise<void> {
    await this.restEnterpriseService.deleteUnitRole(auth, spaceId, unitId);
  }

  async getMemberDetail(auth: IAuthHeader, param: SpaceUnitParamRo, query: MemberSensitiveQueryRo): Promise<MemberDetailDto> {
    const member: IUnitMember = await this.restEnterpriseService.getUnitMemberDetail(auth, param.spaceId, param.unitId,
      query.sensitiveData!);
    return plainToClass(MemberDetailDto, { member });
  }

  async updateMember(auth: IAuthHeader, param: SpaceUnitParamRo, query: MemberSensitiveQueryRo, body: MemberUpdateRo): Promise<MemberDetailDto> {
    const member: IUnitMember = await this.restEnterpriseService.updateUnitMember(auth, param.spaceId, param.unitId, query.sensitiveData!, body);
    return plainToClass(MemberDetailDto, { member });
  }

  async deleteMember(auth: IAuthHeader, spaceId: string, unitId: string): Promise<void> {
    await this.restEnterpriseService.deleteUnitMember(auth, spaceId, unitId);
  }

  private async getDatasheet(auth: IAuthHeader, datasheetId: string): Promise<databus.Datasheet> {

    const datasheet = await this.databusService.getDatasheet(datasheetId, {
      loadOptions: {
        auth,
      },
    });
    if (!datasheet) {
      throw ApiException.tipError(ApiTipConstant.api_datasheet_not_exist);
    }
    return datasheet;
  }

  private getWidgetLayout(widgetId: string, dashboard: databus.Dashboard): WidgetLayout | undefined {
    let layout;
    const originalLayout = find(dashboard.snapshot.widgetInstallations.layout, { id: widgetId });
    if (originalLayout) {
      layout = {
        x: originalLayout.row,
        y: originalLayout.column,
        width: originalLayout.widthInColumns,
        height: originalLayout.heightInRoes
      } as WidgetLayout;
    }
    return layout;
  }
}
