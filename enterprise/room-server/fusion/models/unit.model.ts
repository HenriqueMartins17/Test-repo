import { ApiTipConstant } from '@apitable/core';
import { ApiProperty, ApiPropertyOptional, IntersectionType, OmitType, PickType } from '@nestjs/swagger';
import { IsNotEmpty, IsNumber, IsString, ValidateIf } from 'class-validator';
import { SpaceParamRo } from 'fusion/ros/space.param.ro';
import { ApiPage } from 'fusion/vos/api.page';
import { ApiResponse } from 'fusion/vos/api.response';

class UnitParamRo {
  @ApiProperty({
    description: 'Unique identifier for the object.',
    example: 'unitId'
  })
  @IsString()
  @IsNotEmpty({ message: ApiTipConstant.api_param_unit_id_required })
  unitId!: string;
}

export class SpaceUnitParamRo extends IntersectionType(SpaceParamRo, UnitParamRo) {

}

export class SpaceUnitQueryRo {
  @ApiPropertyOptional({
    type: Number,
    example: 100,
    description: 'Total number returned per page',
  })
  pageSize?: number = 100;

  @ApiPropertyOptional({
    type: Number,
    example: 1,
    description: 'Page numbering for pagination',
  })
  pageNum?: number = 1;
}

export class SpaceUnitMemberQueryRo extends SpaceUnitQueryRo {
  @ApiPropertyOptional({
    type: Boolean,
    example: true,
    description: 'Fill in “true“ to return member’s sensitive data, includes mobile number and email',
  })
  sensitiveData?: boolean = false;
}

export class MemberSensitiveQueryRo extends PickType(SpaceUnitMemberQueryRo, ['sensitiveData'] as const) {

}

export class RoleDto {
  @ApiProperty({
    description: 'Unique identifier for the role object.',
    example: 'roleUnitId'
  })
  unitId!: string;

  @ApiProperty({
    description: 'Role name',
    example: 'test role'
  })
  @IsNotEmpty({ message: ApiTipConstant.api_param_unit_name_required })
  name!: string;

  @ApiPropertyOptional({
    description: 'The order of the roles, sorted ascending, start from 2000 by default.',
    example: '2000'
  })
  sequence?: number;
}

export class RoleDetailDto {
  @ApiProperty({ type: RoleDto })
  role!: RoleDto;
}

export class RolePageDto extends OmitType(ApiPage<RoleDto[]>, ['records'] as const) {
  @ApiProperty({ type: [RoleDto] })
  roles!: RoleDto[];

  @ApiProperty({
    type: Number,
    example: 500,
    description: 'Total number of roles.',
  })
  override total!: number;

  @ApiProperty({
    type: Number,
    example: 100,
    description: 'Count of roles in this current page.',
  })
  override pageSize!: number;

  @ApiProperty({
    type: Number,
    example: 1,
    description: 'Current page number.',
  })
  override pageNum!: number;
}

export class RoleRo extends OmitType(RoleDto, ['unitId'] as const) {
}

export class RoleUpdateRo {
  @ApiProperty({
    description: 'Role name',
    example: 'test role'
  })
  @ValidateIf(o => o.sequence === undefined)
  @IsNotEmpty({ message: ApiTipConstant.api_param_unit_name_required })
  name?: string;

  @ApiPropertyOptional({
    description: 'The order of the roles, sorted ascending, start from 2000 by default.',
    example: '2000'
  })
  @ValidateIf(o => o.sequence !== undefined)
  @IsNumber({ allowNaN: false }, { message: ApiTipConstant.api_param_sequence_type_error })
  sequence?: number;
}

export class RoleVo extends ApiResponse<RoleDetailDto> {
  @ApiProperty({ type: RoleDetailDto })
  override data!: RoleDetailDto;
}

export class RolePageVo extends ApiResponse<RolePageDto> {
  @ApiProperty({ type: RolePageDto })
  override data!: RolePageDto;
}

export class TeamDto {
  @ApiProperty({
    description: 'Unique identifier for the object.',
    example: 'teamUnitId'
  })
  unitId!: string;

  @ApiProperty({
    description: 'Team name.',
    example: 'test team'
  })
  @IsNotEmpty({ message: ApiTipConstant.api_param_unit_name_required })
  name!: string;

  @ApiPropertyOptional({
    description: 'The order of the teams, sorted ascending, start from 1 by default.',
    example: '1'
  })
  sequence?: number;

  @ApiPropertyOptional({
    description: 'Team’s parent unitId.',
    example: '0'
  })
  parentUnitId?: string;

  @ApiPropertyOptional({
    type: [RoleDto],
    description: 'Role list the team belongs to.'
  })
  roles?: RoleDto[];
}

export class TeamDetailDto {
  @ApiProperty({ type: TeamDto })
  team!: TeamDto;
}

export class TeamPageDto extends OmitType(ApiPage<TeamDto[]>, ['records'] as const) {
  @ApiProperty({ type: [TeamDto] })
  teams!: TeamDto[];

  @ApiProperty({
    type: Number,
    example: 500,
    description: 'Total number of all teams',
  })
  override total!: number;

  @ApiProperty({
    type: Number,
    example: 100,
    description: 'Count of teams in this current page.',
  })
  override pageSize!: number;

  @ApiProperty({
    type: Number,
    example: 1,
    description: 'Current page number.',
  })
  override pageNum!: number;
}

export class TeamRo extends PickType(TeamDto, ['name', 'sequence', 'parentUnitId'] as const) {
  @ApiPropertyOptional({
    description: 'A list of roles that the member belongs to. A member can belong to multiple roles.',
  })
  roles?: string[];
}

export class TeamUpdateRo extends PickType(TeamRo, ['roles', 'sequence', 'parentUnitId'] as const) {
  @ApiPropertyOptional({
    description: 'team name',
    example: 'teamName'
  })
  name?: string;
}

export class TeamVo extends ApiResponse<TeamDetailDto> {
  @ApiProperty({ type: TeamDetailDto })
  override data!: TeamDetailDto;
}

export class TeamPageVo extends ApiResponse<TeamPageDto> {
  @ApiProperty({ type: TeamPageDto })
  override data!: TeamPageDto;
}

export enum MemberType {
  PrimaryAdmin = 'PrimaryAdmin',
  SubAdmin = 'SubAdmin',
  Member = 'Member'
}

export class MemberMobile {
  @ApiProperty({
    description: 'Member\'s mobile number',
    example: '109***'
  })
  number!: string;

  @ApiProperty({
    description: 'The area code of the member\'s mobile',
    example: '+86'
  })
  areaCode!: string;
}

export class MemberDto {
  @ApiProperty({
    description: 'Unique identifier for the member object.',
    example: 'memberUnitId'
  })
  unitId!: string;

  @ApiProperty({
    description: 'Member name.',
    example: 'test member'
  })
  name!: string;

  @ApiPropertyOptional({
    description: 'Member avatar.',
    example: 'https://**'
  })
  avatar?: string;

  @ApiProperty({
    description: 'Member status (1: joined, 0: not joined).',
    example: '1'
  })
  status!: number;

  @ApiProperty({
    description: 'Member’s type such as PrimaryAdmin, SubAdmin, Member.Default: Member',
    example: 'Member',
    enum: MemberType,
    enumName: 'MemberType'
  })
  type!: string;

  @ApiPropertyOptional({
    description: 'Member email address.',
    example: '**@**.**'
  })
  email?: string;

  @ApiPropertyOptional({
    description: 'The mobile phone number information of the member, it will not be returned in the null value and invisible state.',
    type: MemberMobile
  })
  mobile?: MemberMobile;

  @ApiProperty({
    description: 'A list of teams that the member belongs to. A member can belong to multiple teams.',
    type: [TeamDto]
  })
  teams?: TeamDto[];

  @ApiPropertyOptional({
    description: 'A list of roles that the member belongs to. A member can belong to multiple roles.',
    type: [RoleDto]
  })
  roles?: RoleDto[];
}

export class MemberDetailDto {
  @ApiProperty({
    type: MemberDto,
  })
  member!: MemberDto;
}

export class MemberPageDto extends OmitType(ApiPage<MemberDto[]>, ['records'] as const) {
  @ApiProperty({ type: [MemberDto], description: 'The member’s detail information.' })
  members!: MemberDto[];

  @ApiProperty({
    type: Number,
    example: 500,
    description: 'Total number of members for the current team.',
  })
  override total!: number;

  @ApiProperty({
    type: Number,
    example: 100,
    description: 'Count of members in this current page.',
  })
  override pageSize!: number;

  @ApiProperty({
    type: Number,
    example: 1,
    description: 'Current page number.',
  })
  override pageNum!: number;
}

export class MemberCreateRo extends PickType(MemberDto, ['name', 'avatar', 'email', 'mobile'] as const) {
  @ApiProperty({
    description: 'A list of teams that the member belongs to. A member can belong to multiple teams.',
  })
  teams!: string[];

  @ApiPropertyOptional({
    description: 'A list of roles that the member belongs to. A member can belong to multiple roles.',
  })
  roles?: string[];
}

export class MemberUpdateRo {
  @ApiPropertyOptional({
    description: 'Member name.',
    example: 'test member'
  })
  name?: string;

  @ApiPropertyOptional({
    description: 'A list of teams that the member belongs to. A member can belong to multiple teams.',
  })
  teams?: string[];

  @ApiPropertyOptional({
    description: 'A list of roles that the member belongs to. A member can belong to multiple roles.',
  })
  roles?: string[];

}

export class MemberPageVo extends ApiResponse<MemberPageDto> {
  @ApiProperty({ type: MemberPageDto })
  override data!: MemberPageDto;
}

export class MemberVo extends ApiResponse<MemberDetailDto> {
  @ApiProperty({
    type: MemberDetailDto,
  })
  override data!: MemberDetailDto;
}

export class RoleUnitDetailDto {
  @ApiPropertyOptional({ type: [MemberDto], description: 'The members under role.' })
  members!: MemberDto[];

  @ApiProperty({ type: [TeamDto], description: 'The teams under role.' })
  teams!: TeamDto[];
}

export class RoleUnitListVo extends ApiResponse<RoleUnitDetailDto> {
  @ApiProperty({ type: RoleUnitDetailDto })
  override data!: RoleUnitDetailDto;
}
