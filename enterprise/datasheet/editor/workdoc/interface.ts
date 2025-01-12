import * as React from 'react';
import { ICellValue, IWorkDocValue } from '@apitable/core';

export enum Status {
  Connecting = 'connecting',
  Connected = 'connected',
  Disconnected = 'disconnected',
  Reconnecting = 'reconnecting',
}

export interface IWorkdocProps {
  editable: boolean;
  editing: boolean;
  cellValue?: ICellValue;
  datasheetId: string;
  toggleEditing?: (next?: boolean) => void;
  recordId?: string;
  fieldId?: string;
  onSave?: (value: IWorkDocValue[]) => void;
}

export interface ICollaborationEditor extends Omit<IWorkdocProps, 'toggleEditing'> {
  title?: string;
  setTitle?: React.Dispatch<React.SetStateAction<string>>;
  setStatus?: React.Dispatch<React.SetStateAction<Status>>;
  formId?: string;
  mount?: boolean;
  isMobile?: boolean;
  status: Status;
  expandable?: boolean;
  infoOpen?: boolean;
  collaborators?: string[];
  setCollaborators?: React.Dispatch<React.SetStateAction<string[]>>;
}

export interface IWorkdocEditorProps extends ICollaborationEditor {
  onClose: (e: React.MouseEvent | React.KeyboardEvent) => void;
  status: Status;
}

export interface IWorkdocCollaborators {
  collaborators: string[];
}
