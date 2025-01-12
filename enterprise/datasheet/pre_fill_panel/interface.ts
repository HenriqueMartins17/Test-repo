import { Dispatch, SetStateAction } from 'react';
import { IFieldMap, IViewColumn } from '@apitable/core';
import { IFormData } from 'pc/components/form_container/interface';

export interface IPreFillPanel {
  formData: IFormData;
  fieldMap: IFieldMap;
  setPreFill: Dispatch<SetStateAction<boolean>>;
  columns: IViewColumn[];
}

export interface IShareContent {
  suffix: string;
}
