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

import { useContext } from 'react';
import { useThemeColors } from '@apitable/components';
import { KONVA_DATASHEET_ID, Selectors } from '@apitable/core';
import { NotificationOutlined } from '@apitable/icons';
import { generateTargetName, getDayjs } from 'pc/components/gantt_view';
import { Icon } from 'pc/components/konva_components';
import { GridCoordinate, KonvaGridContext, KonvaGridViewContext } from 'pc/components/konva_grid';
import { store } from 'pc/store';
// @ts-ignore
import { AlarmTipText } from 'enterprise/alarm/alarm_tip_text';

const NotificationSmallOutlinedPath = NotificationOutlined.toString();

export interface IAlarmIconProps {
  datasheetId: string;
  fieldId: string;
  recordId: string;
  instance: GridCoordinate;
  columnIndex: number;
  rowIndex: number;
  toggleEditing: () => Promise<boolean | void>;
}

export const AlarmIcon = (props: IAlarmIconProps) => {
  const colors = useThemeColors();
  const { datasheetId, fieldId, recordId, instance, columnIndex, rowIndex, toggleEditing } = props;
  const state = store.getState();
  const { snapshot } = useContext(KonvaGridViewContext);
  const { setTooltipInfo, clearTooltipInfo } = useContext(KonvaGridContext);
  const cellValue = Selectors.getCellValue(state, snapshot, recordId, fieldId);
  const alarm = Selectors.getDateTimeCellAlarm(snapshot, recordId, fieldId);

  if (!cellValue || !alarm) {
    return null;
  }
  const x = instance.getColumnOffset(columnIndex + 1) - 25;
  const y = instance.getRowOffset(rowIndex) + 8;
  const alarmDate = getDayjs(cellValue);
  const subtractMatch = alarm?.subtract?.match(/^([0-9]+)(\w{1,2})$/);
  if (subtractMatch) {
    alarmDate.subtract(Number(subtractMatch[1]), subtractMatch[2] as any);
  }
  return (
    <Icon
      key={`date-alarm-${fieldId}-${recordId}`}
      x={x}
      y={y}
      name={generateTargetName({
        targetName: KONVA_DATASHEET_ID.GRID_DATE_CELL_ALARM,
        fieldId,
        recordId
      })}
      data={NotificationSmallOutlinedPath}
      fill={colors.primaryColor}
      onClick={toggleEditing}
      onMouseEnter={() => {
        setTooltipInfo({
          placement: 'top',
          title: <AlarmTipText datasheetId={datasheetId} recordId={recordId} dateTimeFieldId={fieldId} />,
          visible: true,
          width: 16,
          height: 16,
          x: x,
          y: y,
          rowsNumber: 5
        });
      }}
      onMouseOut={clearTooltipInfo}
    />
  );
};
