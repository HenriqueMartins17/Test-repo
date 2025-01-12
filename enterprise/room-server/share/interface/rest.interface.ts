export interface IUnit {
  unitId: string;
  name: string;
}

export interface IUnitRole extends IUnit {
  sequence: number;
}

export interface IUnitTeam extends IUnit {
  sequence: number;
  roles: IUnitRole[];
}

export interface IMemberMobile {
  areaCode: string;
  number: string;
}

export interface IUnitMember extends IUnit {
  avatar: string;
  status: number;
  type: string;
  email: string;
  mobile: IMemberMobile;
  teams: Omit<IUnitTeam, 'roles'>[];
  roles: IUnitRole[];
}

export interface IRoleUnit {
  members: IUnitMember[];
  teams: IUnitTeam[];
}

export interface IPageQueryInfo {
  pageNo: number;
  pageSize: number
}

export interface IPage<T> {
  pageNum: number;
  pageSize: number;
  size: number;
  total: number;
  pages: number;
  startRow: number;
  endRow: number;
  prePage: number;
  nextPage: number;
  firstPage: number;
  lastPage: number;
  hasPreviousPage: boolean;
  hasNextPage: boolean;
  records: T[];
}

