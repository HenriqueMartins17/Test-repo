export interface IResponseBase {
  success: boolean;
  code: number;
  message: string;
}
export interface IMyInfo {
  nickName: string;
  avatar: string;
  mobile: string;
  email: string;
  spaceName: string;
  spaceLogo: string;
  creatorName: string;
  ownerName: string;
  createTime: number;
  memberNumber: number;
  teamNumber: number;
  fileNumber: number;
  recordNumber: number;
  usedSpace: number;
  maxMemory: number;
}


export interface IBindWechatCode {
  isBind: boolean;
}
