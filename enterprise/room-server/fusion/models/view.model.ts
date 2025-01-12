import {
  APIMetaViewType,
  ApiTipConstant,
  Field,
  FilterConjunction,
  FOperator,
  getNewId,
  IDPrefix,
  IFilterCondition,
  IReduxState,
  ISnapshot,
  IViewColumn,
  IViewProperty,
} from '@apitable/core';
import { ApiProperty, ApiPropertyOptional, PickType } from '@nestjs/swagger';
import { Transform, Type } from 'class-transformer';
import { ArrayNotEmpty, IsIn, IsNotEmpty, IsOptional, IsString, ValidateIf, ValidateNested } from 'class-validator';
import { ViewParamRo } from 'fusion/ros/view.param.ro';
import { assignInWith, isUndefined } from 'lodash';
import { ApiException } from 'shared/exception';
import { stringToArray } from 'shared/helpers/fusion.helper';

export class ViewFilterCondition {
  @ApiProperty({
    description: 'The field Id of the filter condition',
  })
  @IsNotEmpty({ message: ApiTipConstant.api_fieldid_empty_error })
  fieldId!: string;

  @ApiProperty({
    enum: FOperator,
    enumName: 'FOperator',
    description: 'filter conjunction',
  })
  @IsIn(
    [
      FOperator.Is,
      FOperator.IsNot,
      FOperator.Contains,
      FOperator.DoesNotContain,
      FOperator.IsEmpty,
      FOperator.IsNotEmpty,
      FOperator.IsGreater,
      FOperator.IsGreaterEqual,
      FOperator.IsLess,
      FOperator.IsLessEqual,
      FOperator.IsRepeat,
    ],
    {
      // message: ApiTipConstant.api_view_type_error,
    },
  )
  operator!: FOperator;
  @ApiPropertyOptional({
    type: 'array',
    items: {
      oneOf: [{ $ref: 'String' }, { $ref: 'Boolean' }],
    },
  })
  value!: string[] | boolean[];
}

export class ViewFilterInfo {
  @ApiProperty({
    type: [ViewFilterCondition],
    description: 'filter criteria',
  })
  @Type(() => ViewFilterCondition)
  @ArrayNotEmpty({ message: ApiTipConstant.api_view_filter_conditions_empty_error })
  @ValidateNested()
  conditions!: ViewFilterCondition[];

  @ApiProperty({
    enum: FilterConjunction,
    enumName: 'FilterConjunction',
    description: 'filter conjunction, possible values are or, and',
  })
  @IsOptional()
  @IsIn([FilterConjunction.Or, FilterConjunction.And])
  conjunction?: FilterConjunction = FilterConjunction.And;

  transformConditions(snapshot: ISnapshot) {
    const conditions = this.conditions.map(item => {
      const field = snapshot.meta.fieldMap[item.fieldId];
      if (!field) {
        throw ApiException.tipError(ApiTipConstant.api_view_fieldid_not_exist);
      }
      const acceptFilterOperators = Field.bindContext(field, {} as IReduxState).acceptFilterOperators;
      if (!acceptFilterOperators.includes(item.operator)) {
        throw ApiException.tipError(ApiTipConstant.api_view_filter_operator_not_support);
      }
      const condition: IFilterCondition = {
        conditionId: getNewId(IDPrefix.Condition, []),
        fieldId: item.fieldId,
        operator: item.operator,
        fieldType: field.type as any,
        value: item.value || Field.bindContext(field, {} as IReduxState).defaultValueForCondition(item as IFilterCondition),
      };
      return condition;
    });
    return { conjunction: this.conjunction, conditions };
  }
}

export class ViewSortRule {
  @ApiPropertyOptional({
    description: 'sort in descending order',
  })
  desc?: boolean = false;

  @ApiProperty({
    description: 'sort field Id',
  })
  @IsNotEmpty({ message: ApiTipConstant.api_fieldid_empty_error })
  fieldId!: string;
}

export class ViewSortInfo {
  @ApiPropertyOptional({
    description: 'automatic sorting',
  })
  keepSort?: boolean = true;

  @ApiProperty({
    type: [ViewSortRule],
    description: 'sort by',
  })
  @Type(() => ViewSortRule)
  @ArrayNotEmpty({ message: ApiTipConstant.api_view_rules_empty_error })
  @ValidateNested()
  rules!: ViewSortRule[];
}

export class ViewColumn implements IViewColumn {
  @ApiProperty({
    description: 'field id',
  })
  @IsNotEmpty({ message: ApiTipConstant.api_fieldid_empty_error })
  fieldId!: string;
}

export class ViewLock {
  @ApiPropertyOptional({
    description: 'view lock description',
  })
  @ValidateIf(o => o.description !== undefined)
  @IsString()
  description?: string;
}

export class CreateViewRo {
  @ApiPropertyOptional({
    description: 'view name',
  })
  name?: string;

  @ApiPropertyOptional({
    description: 'view name',
  })
  autoSave?: boolean;

  @ApiProperty({
    enum: APIMetaViewType,
    enumName: 'APIMetaViewType',
    description: 'view type, possible values are Grid, Gallery, Kanban, Gantt',
  })
  @IsIn(
    [
      APIMetaViewType.Grid,
      APIMetaViewType.Gallery,
      APIMetaViewType.Gantt,
      APIMetaViewType.Kanban,
      APIMetaViewType.Calendar,
      APIMetaViewType.Architecture,
    ],
    { message: ApiTipConstant.api_view_type_error },
  )
  type!: APIMetaViewType;

  @ApiPropertyOptional({
    type: [ViewColumn],
    description: 'view columns',
  })
  @Type(() => ViewColumn)
  @IsOptional()
  @ValidateNested({ each: true })
  columns?: ViewColumn[];

  @ApiPropertyOptional({
    type: ViewSortInfo,
    description: 'view columns',
  })
  @Type(() => ViewSortInfo)
  @IsOptional()
  @ValidateNested()
  sortInfo?: ViewSortInfo;

  @ApiPropertyOptional({
    type: [ViewSortRule],
    description: 'view grouping information',
  })
  @Type(() => ViewSortRule)
  @IsOptional()
  @ValidateNested()
  groupInfo?: ViewSortRule[];

  @ApiPropertyOptional({
    type: ViewFilterInfo,
    description: 'view filter configuration',
  })
  @IsOptional()
  @Type(() => ViewFilterInfo)
  @ValidateNested()
  filterInfo?: ViewFilterInfo;

  overrideViewProperty(snapshot: ISnapshot, defaultViewProperty: IViewProperty) {
    assignInWith(
      defaultViewProperty,
      {
        name: this.name,
        columns: this.columns,
        groupInfo: this.groupInfo,
        sortInfo: this.sortInfo,
        filterInfo: this.filterInfo?.transformConditions(snapshot),
      },
      (obj, src) => {
        return isUndefined(src) ? obj : src;
      },
    );
  }
}

export class UpdateViewRo extends PickType(CreateViewRo, ['name', 'autoSave'] as const) {
  @ApiPropertyOptional({
    type: ViewLock,
    description: 'view filter configuration',
  })
  @IsOptional()
  @Type(() => ViewLock)
  @ValidateNested()
  lockInfo?: ViewLock | null;

  @ApiPropertyOptional({
    description: 'view sequence',
  })
  sequence?: number;

}

export class ViewParam extends ViewParamRo {
  @ApiProperty({
    required: true,
    example: 'viw****',
    description: 'view Id',
  })
  @IsNotEmpty({ message: ApiTipConstant.api_param_viewid_empty_error })
  viewId!: string;
}

export class ViewDeleteRo {
  @ApiProperty({
    required: true,
    description: 'The set of recordId to be deleted',
    example: 'recwZ6yV3Srv3',
  })
  @ArrayNotEmpty({ message: ApiTipConstant.api_param_viewids_empty_error })
  @Transform(value => stringToArray(value), { toClassOnly: true })
  viewIds!: string[];
}
