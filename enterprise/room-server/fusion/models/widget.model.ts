import { ApiTipConstant, databus, IDashboardSnapshot } from '@apitable/core';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsDefined, IsNotEmpty, IsNumber, IsOptional, IsString, Min, ValidateNested } from 'class-validator';
import { ApiResponse } from 'fusion/vos/api.response';

export class DashboardParamRo {
  @ApiProperty({
    description: 'dashboard id',
    example: 'dsb****'
  })
  @IsString()
  dashboardId!: string;
}

export class DashboardWidgetParamRo extends DashboardParamRo {
  @ApiProperty({
    description: 'widget id',
    example: 'wdt****'
  })
  @IsString()
  widgetId!: string;
}

export class WidgetLayout {
  @ApiProperty({
    description: 'x axis position',
    example: '0'
  })
  @IsDefined()
  @IsNumber()
  x!: number;

  @ApiProperty({
    description: 'y axis position',
    example: '0'
  })
  @IsDefined()
  @IsNumber()
  y!: number;

  @ApiProperty({
    description: 'width',
    example: '10'
  })
  @IsDefined()
  @IsNumber()
  @Min(1)
  width!: number;

  @ApiProperty({
    description: 'height',
    example: '10'
  })
  @IsDefined()
  @IsNumber()
  @Min(1)
  height!: number;
}

export class UpdateWidgetRo {
  @ApiPropertyOptional({
    description: 'widget name, default use widget package name',
    example: 'test widget'
  })
  name?: string;

  @ApiPropertyOptional({
    type: WidgetLayout,
    description: 'The widget layout',
  })
  @IsOptional()
  @Type(() => WidgetLayout)
  @ValidateNested()
  layout?: WidgetLayout;

  overrideLayoutProperty(widgetId: string, snapshot: IDashboardSnapshot): databus.IDashboardChangeLayoutOptions[] | undefined {
    if (this.layout && snapshot.widgetInstallations.layout) {
      const layouts: databus.IDashboardChangeLayoutOptions[] = [];
      for (const layout of snapshot.widgetInstallations.layout) {
        if (layout.id === widgetId) {
          layouts.push({
            id: widgetId,
            row: this.layout!.x,
            column: this.layout!.y,
            heightInRoes: this.layout!.height,
            widthInColumns: this.layout!.width
          } as databus.IDashboardChangeLayoutOptions);
        } else {
          layouts.push(layout);
        }
      }
      return layouts;
    }
    return;
  }
}

export class CreateWidgetRo extends UpdateWidgetRo {
  @ApiProperty({
    description: 'widget package id',
    example: 'wpk*****'
  })
  @IsString()
  @IsNotEmpty({ message: ApiTipConstant.api_params_widget_package_id_error })
  widgetPackageId!: string;

  @ApiPropertyOptional({
    description: 'datasheetId, If there is a value, it will automatically link to the specified datasheet',
    example: 'dst'
  })
  datasheetId?: string;
}

export class WidgetDto {
  @ApiProperty({
    description: 'widget id',
    example: 'wdt*****'
  })
  widgetId!: string;

  @ApiPropertyOptional({
    description: 'widget name',
    example: 'test widget'
  })
  name!: string;

  @ApiPropertyOptional({
    description: 'widget layout',
    type: WidgetLayout
  })
  layout?: WidgetLayout;
}

export class WidgetVo extends ApiResponse<WidgetDto> {
  @ApiProperty({ type: WidgetDto })
  override data!: WidgetDto;
}
