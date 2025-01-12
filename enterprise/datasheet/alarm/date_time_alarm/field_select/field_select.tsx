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

import { useClickAway } from 'ahooks';
import classNames from 'classnames';
import Trigger from 'rc-trigger';
import { useMemo, useRef, useState } from 'react';
import * as React from 'react';
import { colorVars } from '@apitable/components';
import { FieldType, IFieldMap, Strings, t } from '@apitable/core';
import { ChevronDownOutlined } from '@apitable/icons';
import { ComponentDisplay, ScreenSize } from 'pc/components/common/component_display';
import { Popup } from 'pc/components/common/mobile/popup';
import { getFieldTypeIcon } from 'pc/components/multi_grid/field_setting';
// @ts-ignore
import { FieldList } from 'enterprise/alarm/date_time_alarm/field_select/field_list';
import styles from './style.module.less';

interface IFieldSelectProps {
  selectedFieldIds: string[];
  fieldMap: IFieldMap;
  onChange(ids: string[]): void;
}

export const FieldSelect: React.FC<IFieldSelectProps> = props => {
  const { selectedFieldIds, fieldMap, onChange } = props;

  const [visible, setVisible] = useState(false);

  const listData = useMemo(() => {
    return Object.values(fieldMap).filter(field => field.type === FieldType.Member);
  }, [fieldMap]);

  // TODO Solve the problem of the magic quote filter panel not closing, temporary solution, force it to close
  const refSelect = useRef<HTMLDivElement>(null);
  const refSelectItem = useRef<HTMLDivElement>(null);

  useClickAway(() => setVisible(false), [refSelect, refSelectItem], 'click');

  const renderPopup = () => {
    return (
      <div ref={refSelectItem}>
        <FieldList fields={listData} selectedFieldIds={selectedFieldIds} onChange={onChange} />
      </div>
    );
  };

  const selectedFieldsValue = (
    <div className={styles.selectedFieldsValue}>
      {selectedFieldIds.map(fieldId => {
        const field = fieldMap[fieldId];
        return (
          <div className={styles.value} key={fieldId}>
            <span className={styles.icon}>{getFieldTypeIcon(field.type, colorVars.thirdLevelText)}</span>
            {field.name}
          </div>
        );
      })}
    </div>
  );

  return (
    <div className={styles.select} ref={refSelect}>
      <ComponentDisplay minWidthCompatible={ScreenSize.md}>
        <Trigger
          action={['click']}
          popup={renderPopup()}
          destroyPopupOnHide
          popupAlign={{ points: ['tl', 'bl'], offset: [0, 8], overflow: { adjustX: true, adjustY: true } }}
          popupVisible={visible}
          onPopupVisibleChange={visible => setVisible(visible)}
          stretch="width,height"
          popupStyle={{ width: 248, height: 'max-content' }}
        >
          <div className={classNames(styles.displayBox, styles.option)}>
            {selectedFieldsValue}
            <div className={styles.iconArrow} style={{ transform: `rotate(${visible ? '180deg' : 0})` }}>
              <ChevronDownOutlined color={colorVars.black[500]} />
            </div>
          </div>
        </Trigger>
      </ComponentDisplay>

      <ComponentDisplay maxWidthCompatible={ScreenSize.md}>
        <div className={classNames(styles.displayBox, styles.option)} onClick={() => setVisible(!visible)}>
          {selectedFieldsValue}
          <div className={styles.iconArrow}>
            <ChevronDownOutlined size={16} color={colorVars.fourthLevelText} />
          </div>
        </div>
        <Popup
          title={t(Strings.please_choose)}
          height="90%"
          open={visible}
          onClose={() => setVisible(false)}
          className={styles.filterGeneralPopupWrapper}
        >
          {renderPopup()}
        </Popup>
      </ComponentDisplay>
    </div>
  );
};
