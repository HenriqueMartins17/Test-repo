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

import * as React from 'react';
import { useThemeColors } from '@apitable/components';
import { IField } from '@apitable/core';
import { CommonList } from 'pc/components/list/common_list';
import { Check } from 'pc/components/list/common_list/check';
import { getFieldTypeIcon } from 'pc/components/multi_grid/field_setting';
import styles from './style.module.less';
interface IFieldListPops {
  selectedFieldIds: string[];
  fields: IField[];
  onChange: (fieldIds: string[]) => void;
}

export const FieldList: React.FC<IFieldListPops> = (props) => {
  const { selectedFieldIds, fields, onChange } = props;
  const colors = useThemeColors();

  const onClickItem = (_e: React.MouseEvent | null, index: number) => {
    const fieldId = fields[index].id;
    const _selectedFieldId = new Set(selectedFieldIds);
    if (_selectedFieldId.has(fieldId)) {
      _selectedFieldId.delete(fieldId);
    } else {
      _selectedFieldId.add(fieldId);
    }
    onChange([..._selectedFieldId]);
  };

  return <CommonList
    value={selectedFieldIds}
    onClickItem={onClickItem}
    inputStyle={{ padding: 8 }}
  >
    {
      fields.map((field, index) => {
        const isChecked = Boolean(selectedFieldIds && selectedFieldIds.includes(field.id!));
        return (
          <CommonList.Option
            key={field.id}
            currentIndex={index}
            id={field.id}
            onMouseDown={(e: React.MouseEvent) => {
              e.preventDefault();
            }}
            className={styles.memberOptionItemWrapper}
          >
            <div style={{ flex: 1, display: 'flex', color: isChecked ? colors.primaryColor : '' }}>
              <span style={{ marginRight: 4, display: 'flex', alignItems: 'center' }}>
                {getFieldTypeIcon(field.type, isChecked ? colors.primaryColor : colors.thirdLevelText)}
              </span>
              {field.name}
            </div>
            <Check isChecked={isChecked} />
          </CommonList.Option>
        );
      })
    }
  </CommonList>;
};
