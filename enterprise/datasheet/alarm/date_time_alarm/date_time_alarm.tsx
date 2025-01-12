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

import cls from 'classnames';
import dayjs from 'dayjs';
import { keyBy, pick } from 'lodash';
import { useEffect, useMemo } from 'react';
import { shallowEqual, useSelector } from 'react-redux';
// eslint-disable-next-line no-restricted-imports
import { Select, Switch, Tooltip } from '@apitable/components';
import { AlarmUsersType, Api, FieldType, IMemberField, IRecordAlarmClient, Selectors, StoreActions, Strings, t, WithOptional } from '@apitable/core';
import { WarnCircleFilled } from '@apitable/icons';
import { useGetMemberStash } from 'modules/space/member_stash/hooks/use_get_member_stash';
import { Message } from 'pc/components/common';
import { TimePicker } from 'pc/components/editors/date_time_editor/time_picker_only';
import { FilterGeneralSelect } from 'pc/components/tool_bar/view_filter/filter_value/filter_general_select';
import { stopPropagation } from 'pc/utils';
import { ALARM_SUBTRACT, ALL_ALARM_SUBTRACT, CURRENT_ALARM_SUBTRACT, INNER_DAY_ALARM_SUBTRACT } from 'pc/utils/constant';
import { dispatch } from 'pc/worker/store';
import { AlarmTipText } from 'enterprise/alarm/alarm_tip_text';
import { FieldSelect } from 'enterprise/alarm/date_time_alarm/field_select/field_select';
import styles from './style.module.less';
// @ts-ignore
// @ts-ignore

const inDayKeys = Object.keys(INNER_DAY_ALARM_SUBTRACT);

interface IDateTimeAlarmProps {
  datasheetId: string;
  recordId: string;
  fieldId: string;
  handleDateTimeChange: (value: dayjs.Dayjs, isSetTime?: boolean) => void;
  handleDateAlarm(alarm: WithOptional<IRecordAlarmClient, 'id'> | undefined): void;
  curAlarm?: WithOptional<IRecordAlarmClient, 'id'>;
  timeValue?: string;
  dateValue: string;
  includeTime?: boolean;
  timeZone?: string;
}

/**
 * This data structure is primarily intended for use with the member selector,
 * the component comes from the filter function of the member, and since a field must be provided, the forged data is used here
 */
export const fakeMemberField = {
  name: 'fakeMemberField',
  type: FieldType.Member,
  id: 'fakeMemberField',
  property: {
    isMulti: true
  }
};

const DateTimeAlarm = (props: IDateTimeAlarmProps) => {
  const {
    datasheetId, recordId, fieldId, timeValue = '00:00', dateValue, includeTime, timeZone, curAlarm,
    handleDateTimeChange, handleDateAlarm
  } = props;
  const alarmTarget = curAlarm?.target;
  const alarmAtTime = timeZone ? dayjs(curAlarm?.alarmAt).tz(timeZone).format('HH:mm') : dayjs(curAlarm?.alarmAt).format('HH:mm');

  // Due to time zone, the value of subtract select may not be matched
  const alarmAt2DateFormat = useMemo(() => {
    if (!curAlarm?.alarmAt) return dateValue;
    const result = timeZone ? dayjs(curAlarm?.alarmAt).tz(timeZone) : dayjs(curAlarm?.alarmAt);
    return result.format('YYYY-MM-DD');
  }, [curAlarm?.alarmAt, dateValue, timeZone]);
  const dateTime2AlarmDateFormat = useMemo(() => {
    if (!dayjs(dateValue).isValid()) {
      return dateValue;
    }
    // example: 2020-12-12 1 is valid, need format to 2020-12-12
    const formatDateValue = dateValue.split(' ')[0];
    let result: dayjs.Dayjs;
    try {
      result = dayjs.tz(`${formatDateValue} ${timeValue}`, timeZone);
    } catch (e) {
      console.error('dayjs.tz error', e);
      result = dayjs(`${formatDateValue} ${timeValue}`);
    }
    const subtractMatch = curAlarm?.subtract?.match(/^([0-9]+)(\w{1,2})$/);
    if (subtractMatch && subtractMatch[2] !== 'm' && subtractMatch[2] !== 'h') {
      result = result.subtract(Number(subtractMatch[1]), subtractMatch[2] as any);
    }
    return result.format('YYYY-MM-DD');
  }, [curAlarm?.subtract, dateValue, timeValue, timeZone]);
  const checkSubtractMatch = alarmAt2DateFormat === dateTime2AlarmDateFormat;
  const tempSubtractOption = `${alarmAt2DateFormat} ${curAlarm?.time || alarmAtTime}`;

  const isAlarmMemberField = alarmTarget === AlarmUsersType.Field;
  const { fieldMap, user, unitMap } = useSelector(state => {
    return {
      fieldMap: Selectors.getFieldMap(state, datasheetId),
      user: state.user.info,
      unitMap: Selectors.getUnitMap(state)
    };
  }, shallowEqual);

  const { memberStashList: stashList } = useGetMemberStash();

  const handleOpen = (status: boolean) => {
    const nowTime = dayjs(new Date()).format('HH:mm');
    const time = includeTime ? (timeValue || nowTime) : '09:00';
    if (status && !dateValue) {
      const nowDate = dayjs(new Date()).format('YYYY-MM-DD');
      handleDateTimeChange(dayjs(`${nowDate} ${time}`), includeTime);
    }
    const newAlarm = status ? {
      subtract: '',
      time,
      alarmUsers: [user?.unitId || ''],
      target: AlarmUsersType.Member
    } : undefined;
    handleDateAlarm!(newAlarm);
  };

  const handleChangeAlarm = (item: { [key: string]: any }) => {
    handleDateAlarm!({
      ...curAlarm!,
      ...item
    });
  };

  const handleTimeChange = (newTime: string) => {
    handleChangeAlarm({ time: newTime });
  };

  const alertTargetOptions = useMemo(() => {
    const fields = Object.values(fieldMap!);
    const isNotExistMemberField = fields.every(field => field.type !== FieldType.Member);
    return [{
      value: AlarmUsersType.Member,
      label: t(Strings.alarm_specifical_member)
    }, {
      value: AlarmUsersType.Field,
      label: t(Strings.alarm_specifical_field_member),
      disabled: isNotExistMemberField,
      disabledTip: t(Strings.alarm_no_member_field_tips)
    }];
  }, [fieldMap]);

  // Asynchronous replenishment when no member data is available
  useEffect(() => {
    if (curAlarm?.target !== AlarmUsersType.Member) {
      return;
    }
    const missUnitIds: string[] = [];

    if (unitMap) {
      for (const unitId of curAlarm.alarmUsers) {
        if (unitMap[unitId]) {
          continue;
        }
        missUnitIds.push(unitId);
      }
    } else {
      missUnitIds.push(...curAlarm.alarmUsers);
    }

    if (!unitMap || missUnitIds.length) {
      Api.loadOrSearch({ unitIds: missUnitIds.join(',') }).then(res => {
        const { data: { data: resData, success } } = res;
        if (!resData.length || !success) {
          return;
        }
        dispatch(StoreActions.updateUnitMap(keyBy(resData, 'unitId')));
      });
    }
  }, [isAlarmMemberField, unitMap, user?.unitId, curAlarm?.alarmUsers, curAlarm?.target]);

  const subtractOptions = useMemo(() => {
    let optionData: object;
    if (includeTime) {
      optionData = ALL_ALARM_SUBTRACT;
    } else {
      let extraSubtract = {};
      if (curAlarm?.subtract && inDayKeys.includes(curAlarm?.subtract)) {
        extraSubtract = pick(ALL_ALARM_SUBTRACT, curAlarm?.subtract);
      }
      optionData = { ...CURRENT_ALARM_SUBTRACT, ...extraSubtract, ...ALARM_SUBTRACT };
    }
    return Object.keys(optionData).map(item => ({
      label: optionData[item],
      value: item,
      disabled: !includeTime && inDayKeys.includes(item),
      suffixIcon: !includeTime && inDayKeys.includes(item) ? (
        <Tooltip content={t(Strings.task_reminder_notify_time_warning)}>
          <span style={{ display: 'flex', alignItems: 'center' }}>
            <WarnCircleFilled color="#FFAB00" />
          </span>
        </Tooltip>
      ) : <></>
    }));
  }, [curAlarm?.subtract, includeTime]);

  if (!fieldMap) {
    return <></>;
  }

  const changeDateAlarmTarget = (option: any) => {
    if (option.value === AlarmUsersType.Field) {
      const memberField = Object.values(fieldMap).find(field => field.type === FieldType.Member)!;
      return handleChangeAlarm({ target: AlarmUsersType.Field, alarmUsers: [memberField.id] });
    }
    handleChangeAlarm({ target: AlarmUsersType.Member, alarmUsers: [user!.unitId] });
  };

  const changeAlarmField = (fieldIds: string[]) => {
    if (!fieldIds.length) {
      Message.warning({
        content: t(Strings.at_least_select_one_field)
      });
      return;
    }
    handleChangeAlarm({ target: AlarmUsersType.Field, alarmUsers: fieldIds });
  };

  const showTimePicker = checkSubtractMatch && (Boolean(curAlarm?.time) || !includeTime ||
    (!curAlarm?.subtract || !inDayKeys.includes(curAlarm?.subtract)));
  return (
    <div className={styles.dateTimeAlarm}>
      <div className={styles.alarmItem}>
        <div className={styles.alarmItemTitle}>{t(Strings.task_reminder_entry)}</div>
        <Switch checked={Boolean(curAlarm)} onClick={handleOpen} />
      </div>
      {Boolean(curAlarm) && (
        <>
          {AlarmTipText && <div className={styles.alertResult}>
            <AlarmTipText datasheetId={datasheetId} recordId={recordId} dateTimeFieldId={fieldId} notShowDetail curAlarm={curAlarm}/>
          </div>}
          <div className={styles.alarmItem}>
            <div className={styles.right}>
              <Select
                dropdownMatchSelectWidth={false}
                triggerCls={styles.select}
                triggerStyle={{ width: showTimePicker ? 184 : 248 }}
                options={checkSubtractMatch ? subtractOptions : [
                  { label: tempSubtractOption, value: tempSubtractOption, disabled: true },
                  ...subtractOptions,
                ]}
                value={checkSubtractMatch ? (curAlarm?.subtract || 'current') : tempSubtractOption}
                onSelected={(option) => {
                  const val = (option.value === 'current' ? '' : option.value) as string;
                  const updateAlarm: any = { subtract: val };
                  if (inDayKeys.includes(val) && curAlarm?.time) {
                    updateAlarm.time = undefined;
                  }
                  if (inDayKeys.includes(val) && curAlarm?.alarmAt) {
                    updateAlarm.alarmAt = undefined;
                  }

                  if (!inDayKeys.includes(val)) {
                    let primaryDate = dayjs.tz(`${dateValue} ${timeValue}`, timeZone);
                    const subtractMatch = val?.match(/^([0-9]+)(\w{1,2})$/);
                    if (subtractMatch && subtractMatch[2] !== 'm' && subtractMatch[2] !== 'h') {
                      primaryDate = primaryDate.subtract(Number(subtractMatch[1]), subtractMatch[2] as any);
                      updateAlarm.alarmAt = (timeZone ? primaryDate.tz(timeZone) : primaryDate).valueOf();
                    } else {
                      updateAlarm.alarmAt = (timeZone ? primaryDate.tz(timeZone) : primaryDate).valueOf();
                    }
                    if (!curAlarm?.time) {
                      updateAlarm.time = timeValue;
                    }
                  }
                  handleChangeAlarm(updateAlarm);
                }}
              />
            </div>
            {
              showTimePicker && <div className={styles.timePicker}>
                <TimePicker
                  placeholder="hh:mm"
                  minuteStep={30}
                  onChange={handleTimeChange}
                  value={curAlarm?.time || alarmAtTime}
                  align={{
                    points: ['bl', 'tl'],
                    offset: [0, 0]
                  }}
                />
              </div>
            }
          </div>
          <div className={styles.alertTarget}>
            <Select
              dropdownMatchSelectWidth={false}
              triggerStyle={{ width: 248 }}
              triggerCls={styles.select}
              options={alertTargetOptions}
              value={curAlarm?.target || AlarmUsersType.Member}
              onSelected={changeDateAlarmTarget}
            />
          </div>
          <div className={styles.alarmItem}>
            {
              curAlarm?.target === AlarmUsersType.Member ? <div onFocus={stopPropagation} className={styles.cellMember}>
                <FilterGeneralSelect
                  field={fakeMemberField as IMemberField}
                  isMulti
                  onChange={(unitIds) => {
                    if (!Array.isArray(unitIds) || !unitIds.length) {
                      Message.warning({
                        content: t(Strings.at_least_select_one)
                      });
                      return;
                    }
                    handleChangeAlarm({ alarmUsers: unitIds });
                  }}
                  cellValue={curAlarm?.alarmUsers}
                  listData={stashList}
                />
              </div> : <div className={cls(styles.right, styles.memberSelect)}>
                <FieldSelect selectedFieldIds={curAlarm?.alarmUsers || []} fieldMap={fieldMap} onChange={changeAlarmField} />
              </div>
            }
          </div>
        </>
      )}
    </div>
  );
};

export default DateTimeAlarm;
