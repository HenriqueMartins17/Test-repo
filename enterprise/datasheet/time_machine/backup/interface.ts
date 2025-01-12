import React from 'react';
import { ConfigConstant, IDatasheetState, IDatasheetTablebundles } from '@apitable/core';

export interface IBackup {
  datasheetId: string;
  curPreview?: string | number;
  setCurPreview: React.Dispatch<React.SetStateAction<string | number | undefined>>;
}

export interface IBackupContext extends IBackup {
  data: IDatasheetTablebundles[];
  setData: React.Dispatch<React.SetStateAction<IDatasheetTablebundles[]>>;
  recoverTbdId: string;
  setRecoverTbdId: React.Dispatch<React.SetStateAction<string>>;
  curDatasheet?: IDatasheetState | null;
  type: ConfigConstant.NodeType;
}

export interface IBackupRecoverModal {
  setRecoverTbdId: React.Dispatch<React.SetStateAction<string>>;
  tbdId: string;
}
