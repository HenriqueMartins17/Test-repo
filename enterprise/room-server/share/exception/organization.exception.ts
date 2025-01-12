import { IBaseException } from 'shared/exception';

export class OrganizationException implements IBaseException {
  private static AllValues: { [name: string]: OrganizationException } = {};

  static readonly ILLEGAL_MEMBER_PERMISSION = new OrganizationException(530, 'Illegal member permission');
  static readonly ILLEGAL_TEAM_PERMISSION = new OrganizationException(531, 'Illegal team permission');
  static readonly ILLEGAL_ROLE_PERMISSION = new OrganizationException(532, 'Illegal role permission');
  static readonly NO_ALLOW_OPERATE = new OrganizationException(411, 'Operation not allowed');
  static readonly DUPLICATION_TEAM_NAME = new OrganizationException(533, 'The team name already exists');
  static readonly GET_TEAM_ERROR = new OrganizationException(506, 'Department does not exist, please try again');
  static readonly DUPLICATION_ROLE_NAME = new OrganizationException(523, 'The role name already exists');
  static readonly NOT_EXIST_MEMBER = new OrganizationException(508, 'Sorry, the member does not exist');
  static readonly NOT_EXIST_ROLE = new OrganizationException(526, 'The role does not exist');
  static readonly GET_PARENT_TEAM_ERROR = new OrganizationException(534, 'Parent department does not exist, please try again');
  static readonly TEAM_HAS_SUB = new OrganizationException(504, 'There are sub-departments under this department, you need to delete the' +
    ' sub-departments under the department first');
  static readonly TEAM_HAS_MEMBER = new OrganizationException(505, 'You need to delete the members under the department first, and then delete the' +
    ' department');
  static readonly DELETE_SPACE_ADMIN_ERROR = new OrganizationException(512, 'Not allowed to delete primary admin');
  static readonly ROLE_EXIST_ROLE_MEMBER = new OrganizationException(528, 'There are members in this role');

  public constructor(public readonly code: number, public readonly message: string) {
    OrganizationException.AllValues[message] = this;
  }

  public getCode(): number {
    return this.code;
  }

  public getMessage(): string {
    return this.message;
  }

}
