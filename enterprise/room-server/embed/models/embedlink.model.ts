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

import { ApiTipConstant } from '@apitable/core';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsBoolean, IsIn, IsNotEmpty, IsOptional, IsString, Matches, ValidateIf, ValidateNested } from 'class-validator';
import { InternalSpaceInfoVo, NodeBaseInfo } from 'database/interfaces';
import { ApiResponse } from 'fusion/vos/api.response';
import { IsNodeExist } from 'node/validations/validation.constraint';

export class CreateEmbedLinkParamRo {
  @ApiProperty({
    type: String,
    required: true,
    example: 'spcjXzqVrjaP3',
    description: 'space Id',
  })
  @IsNotEmpty({ message: ApiTipConstant.api_params_instance_space_id_error })
  spaceId!: string;

  @ApiProperty({
    type: String,
    required: true,
    example: 'dstS94qPZFXjC1LKns',
    description: 'node Id',
  })
  @Matches(/^(dst|dsb|fom).+/, { message: ApiTipConstant.api_embed_link_instance_limit })
  @IsNodeExist({ message: ApiTipConstant.api_datasheet_not_exist })
  @IsNotEmpty({ message: ApiTipConstant.api_param_node_id_not_empty_key })
  nodeId!: string;
}

export class DeleteEmbedLinkParamRo extends CreateEmbedLinkParamRo {
  @ApiProperty()
  @IsNotEmpty({ message: ApiTipConstant.api_param_embed_link_id_not_empty })
  linkId!: string;
}

export enum EmbedLinkTheme {
  Light = 'light',
  Dark = 'dark',
}

export enum EmbedPermissionType {
  ReadOnly = 'readOnly',
  PublicEdit = 'publicEdit',
  PrivateEdit = 'privateEdit',
}

export class EmbedLinkPayloadViewToolBarDto {
  @ApiPropertyOptional({
    description:
      'The default value is false, ' +
      'the basic toolbar (including: grouping, filtering, row height, hidden fields, sorting) is not displayed in the embedded interface.',
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_basic_tools_type_error })
  basicTools: boolean = false;

  // @ApiPropertyOptional({
  //   description: 'The default value is false, and the "Share" button is not displayed in the embedded interface',
  // })
  // @IsOptional()
  // @IsBoolean({ message: ApiTipConstant.api_param_share_btn_type_error })
  // shareBtn: boolean = false;

  @ApiPropertyOptional({
    description: 'The default value is false, and the "Applet" button is not displayed in the embedded interface',
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_widget_btn_type_error })
  widgetBtn: boolean = false;

  @ApiPropertyOptional({
    description: 'The default value is false, and the "API" button is not displayed in the embedded interface',
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_api_btn_type_error })
  apiBtn: boolean = false;

  // @ApiPropertyOptional({
  //   description: 'The default value is false, and the "magic form" button is not displayed in the embedded interface',
  // })
  // @IsOptional()
  // @IsBoolean({ message: ApiTipConstant.api_param_form_btn_type_error })
  // formBtn: boolean = false;

  @ApiPropertyOptional({
    description: 'The default value is false, and the "Time Machine" button is not displayed in the embedded interface',
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_history_btn_type_error })
  historyBtn: boolean = false;

  @ApiPropertyOptional({
    description: 'The default value is false, the "robot" button is not displayed in the embedded interface',
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_robot_btn_type_error })
  robotBtn: boolean = false;

  @ApiPropertyOptional({
    description: 'The default value is false, the "addWidget" button is not displayed in the embedded interface',
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_add_widget_btn_type_error })
  addWidgetBtn: boolean = false;

  @ApiPropertyOptional({
    description: 'The default value is false, the "addWidget" button is not displayed in the embedded interface',
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_full_screen_btn_type_error })
  fullScreenBtn: boolean = false;

  @ApiPropertyOptional({
    description: 'The default value is false, the "formSettingBtn" button is not displayed in the embedded interface',
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_form_setting_btn_type_error })
  formSettingBtn: boolean = false;
}

export class EmbedLinkPayloadViewControlDto {
  @ApiPropertyOptional()
  @ValidateIf(o => o.viewId !== undefined)
  @IsString({ message: ApiTipConstant.api_param_viewid_type_error })
  @IsNotEmpty({ message: ApiTipConstant.api_param_viewid_empty_error })
  viewId!: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_tabbar_type_error })
  tabBar: boolean = true;

  @ApiPropertyOptional()
  @Type(() => EmbedLinkPayloadViewToolBarDto)
  @IsOptional()
  @ValidateNested()
  toolBar: EmbedLinkPayloadViewToolBarDto = new EmbedLinkPayloadViewToolBarDto();

  @ApiPropertyOptional({
    description: 'Whether to collapse the view tab bar (this property is not open in the first phase)',
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_collapsed_type_error })
  collapsed: boolean = false;

  @ApiPropertyOptional({
    description: ''
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_node_info_bar_type_error })
  nodeInfoBar: boolean = true;

  @ApiPropertyOptional()
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_collaborator_status_bar_type_error })
  collaboratorStatusBar: boolean = true;
}

export class EmbedLinkPayloadPrimarySideBarDto {
  @ApiPropertyOptional({
    description: 'The initial state of the working directory, defaults to false, which means the default collapse.',
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_collapsed_type_error })
  collapsed: boolean = false;
}

export class EmbedLinkPayloadDto {
  // @ApiPropertyOptional({
  //   description: 'Personalize the working directory panel, hide the working directory by default',
  // })
  // @Type(() => EmbedLinkPayloadPrimarySideBarDto)
  // @IsOptional()
  // @ValidateNested()
  // primarySideBar: EmbedLinkPayloadPrimarySideBarDto = new EmbedLinkPayloadPrimarySideBarDto();

  @ApiPropertyOptional({
    description: 'Personalize the view area, the view tab bar and view toolbar are not displayed by default',
  })
  @Type(() => EmbedLinkPayloadViewControlDto)
  @IsOptional()
  @ValidateNested()
  viewControl: EmbedLinkPayloadViewControlDto = new EmbedLinkPayloadViewControlDto();

  @ApiPropertyOptional({
    description:
      'Whether to display the brand logo, the default is true, indicating that the logo is displayed. (This property parameter is only' +
      ' available at enterprise level)',
  })
  @IsOptional()
  @IsBoolean({ message: ApiTipConstant.api_param_payload_banner_logo_type_error })
  bannerLogo: boolean = true;

  @ApiPropertyOptional({
    enum: EmbedPermissionType,
    enumName: 'EmbedPermissionType',
  })
  @IsOptional()
  @IsIn([EmbedPermissionType.ReadOnly, EmbedPermissionType.PublicEdit, EmbedPermissionType.PrivateEdit], {
    message: ApiTipConstant.api_param_embed_permission_type_error,
  })
  permissionType: EmbedPermissionType = EmbedPermissionType.ReadOnly;
}

export class EmbedLinkPropertyDto {
  @ApiPropertyOptional()
  @IsOptional()
  @Type(() => EmbedLinkPayloadDto)
  @ValidateNested()
  payload: EmbedLinkPayloadDto = new EmbedLinkPayloadDto();

  @ApiPropertyOptional({
    enum: EmbedLinkTheme,
    enumName: 'EmbedLinkTheme',
  })
  @IsOptional()
  @IsIn([EmbedLinkTheme.Dark, EmbedLinkTheme.Light], { message: ApiTipConstant.api_param_theme_type_error })
  theme: EmbedLinkTheme = EmbedLinkTheme.Light;
}

export class EmbedLinkDto extends EmbedLinkPropertyDto {
  @ApiProperty()
  linkId!: string;

  @ApiProperty()
  url!: string;
}

export class CreateEmbedLinkVo extends ApiResponse<EmbedLinkDto> {
  @ApiProperty({
    type: EmbedLinkDto,
  })
  override data!: EmbedLinkDto;
}

export class EmbedLinkListVo extends ApiResponse<EmbedLinkDto[]> {
  @ApiProperty({
    type: [EmbedLinkDto],
  })
  override data!: EmbedLinkDto[];
}

export class EmbedLinkDetailDto {
  @ApiProperty({
    type: NodeBaseInfo,
  })
  nodeInfo!: NodeBaseInfo;

  @ApiProperty({
    type: InternalSpaceInfoVo,
  })
  spaceInfo!: InternalSpaceInfoVo;

  /**
   * @deprecated
   */
  @ApiProperty(
    {
      deprecated: true,
    }
  )
  spaceId!: string;

  @ApiProperty({
    type: EmbedLinkDto,
  })
  embedInfo!: EmbedLinkDto;
}

export class EmbedLinkDetailVo extends ApiResponse<EmbedLinkDetailDto> {
  @ApiProperty({
    type: EmbedLinkDetailDto,
  })
  override data!: EmbedLinkDetailDto;
}
