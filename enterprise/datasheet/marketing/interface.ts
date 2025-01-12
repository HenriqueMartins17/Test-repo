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

export interface IStoreAppInstance {
  appId: string;
  appInstanceId: string; // The id of the application instantiated after it has been opened
  config: {
    type: AppType;
    profile: {
      [propsname: string]: any;
    }
  };
  createdAt: string;
  isEnabled: boolean; // Three states exist on the backend, on/deactivate/delete
  spaceId: string;
  type: AppType;
}

export interface IStoreApp {
  appId: string;
  name: string;
  type: AppType;
  appType: string;
  status: string;
  intro: string;
  helpUrl: string;
  description: string;
  displayImages: string[];
  notice: string;
  logoUrl: string;
  needConfigured: boolean;
  configureUrl: string;
  needAuthorize: boolean;
  instance?: IStoreAppInstance;
  stopActionUrl?: string;
}

export enum AppType {
  Lark = 'LARK',
  LarkStore = 'LARK_STORE',
  DingTalk = 'DINGTALK',
  DingtalkStore = 'DINGTALK_STORE',
  Wecom = 'WECOM',
  WecomStore = 'WECOM_STORE',
  OfficePreview = 'OFFICE_PREVIEW',
}

export enum AppStatus {
  Open = 'open',
  Close = 'close',
}

export interface IStoreAppConfig {
  type: AppStatus;
  data: IStoreApp[];
}
