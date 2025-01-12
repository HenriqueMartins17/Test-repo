import { find } from 'lodash';
import qs from 'qs';
import { Field, IFieldMap, IViewColumn } from '@apitable/core';
import { IFormData, IFormQuery } from 'pc/components/form_container/interface';
import { FORM_FIELD_TYPE } from 'pc/components/form_container/util';

export const formData2String = (formData: IFormData, fieldMap: IFieldMap, columns: IViewColumn[]) => {
  const newValue: IFormQuery = {};
  for(const key in formData) {
    let value = formData[key];
    const field = fieldMap[key];
    if (!field || !columns.some(column => column.fieldId === field.id)) { continue; }
    if (value && FORM_FIELD_TYPE.select.includes(field.type)) {
      const options = field.property.options;
      if (typeof value === 'string') {
        value = [find(options, { id: value }).name];
      } else {
        value = (value as string[]).map((item: string) => find(options, { id: item }).name);
      }
      newValue[key] = value as string[];
    } else if ([...FORM_FIELD_TYPE.primary, ...FORM_FIELD_TYPE.number].includes(field.type)) {
      newValue[key] = value as string;
    } else if (FORM_FIELD_TYPE.bool.includes(field.type)) {
      if (value === true) {
        newValue[key] = true;
      }
    } else if (!FORM_FIELD_TYPE.filter.includes(field.type)) {
      const cellString = Field.bindModel(field).cellValueToString(value);
      if (cellString !== null) {
        newValue[key] = cellString;
      }
    }
  }
  const urlString = qs.stringify(newValue);
  return urlString ? `?${urlString}` : '';
};
