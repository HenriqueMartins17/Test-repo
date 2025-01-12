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

import { DatePicker } from 'antd-mobile';
import cls from 'classnames';
import dayjs from 'dayjs';
import { compact, isEqual, keyBy, omit, pick } from 'lodash';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { shallowEqual, useSelector } from 'react-redux';
import { IconButton, List, Switch, TextButton, Typography, useThemeColors } from '@apitable/components';
import {
  AlarmUsersType, Api, CollaCommandName, FieldType, IAlarmTypeKeys, ICellValue, IDPrefix, IMemberField, IRecordAlarmClient, Selectors, StoreActions,
  Strings, t, WithOptional,
} from '@apitable/core';
import { ChevronLeftOutlined, ChevronRightOutlined } from '@apitable/icons';
import { Message, MobileContextMenu } from 'pc/components/common';
import { MemberItem } from 'pc/components/multi_grid/cell/cell_member/member_item';
import { FilterGeneralSelect } from 'pc/components/tool_bar/view_filter/filter_value/filter_general_select';
import { resourceService } from 'pc/resource_service';
import { stopPropagation } from 'pc/utils';
import { ALARM_SUBTRACT, ALL_ALARM_SUBTRACT, CURRENT_ALARM_SUBTRACT, INNER_DAY_ALARM_SUBTRACT } from 'pc/utils/constant';
import { dispatch } from 'pc/worker/store';
import { useGetMemberStash } from '../../../space/member_stash/hooks/use_get_member_stash';
import { fakeMemberField } from '../date_time_alarm/date_time_alarm';
// @ts-ignore
import { AlarmTipText } from 'enterprise/alarm/alarm_tip_text';
// @ts-ignore
import { FieldSelect } from 'enterprise/alarm/date_time_alarm/field_select/field_select';
// @ts-ignore
import { convertAlarmStructure } from 'enterprise/alarm/date_time_alarm/utils';
import style from './style.module.less';

const inDayKeys = Object.keys(INNER_DAY_ALARM_SUBTRACT);

interface IMobileAlarmProps {
  setOpenAlarm: (openAlarm: boolean) => void;
  includeTime?: boolean;
  timeZone?: string;
  datasheetId: string;
  fieldId: string;
  recordId: string;
  cellValue?: ICellValue;
}

export const MobileAlarm = (props: IMobileAlarmProps) => {
  const { setOpenAlarm, datasheetId, fieldId, recordId, includeTime, timeZone, cellValue } = props;
  const { snapshot, fieldMap, user, unitMap } = useSelector(state => {
    const dstId = Selectors.getActiveDatasheetId(state)!;
    return {
      snapshot: Selectors.getSnapshot(state)!,
      fieldMap: Selectors.getFieldMap(state, dstId)!,
      user: state.user.info,
      unitMap: Selectors.getUnitMap(state),
    };
  }, shallowEqual);

  const memberFieldOptions = useMemo(() => {
    return Object.values(fieldMap)
      .filter(field => field.type === FieldType.Member)
      .map(field => ({
        label: field.name,
        value: field.id,
      }));
  }, [fieldMap]);

  const alarm = Selectors.getDateTimeCellAlarmForClient(snapshot, recordId, fieldId);
  // const alarmMember = alarm?.alarmUsers[0];
  const isAlarmMemberField = alarm?.target === AlarmUsersType.Field;
  const [openSelect, setOpenSelect] = useState<IAlarmTypeKeys>();
  const colors = useThemeColors();
  const [curAlarm, setCurAlarm] = useState<WithOptional<IRecordAlarmClient, 'id'> | undefined>(alarm);
  const [pickerVisible, setPickerVisible] = useState(false);

  const { memberStashList: stashList } = useGetMemberStash();

  const handleOpen = (status: boolean) => {
    if (recordId) {
      const newAlarm = status ? {
        subtract: '',
        alarmAt: cellValue as string,
        alarmUsers: [user?.unitId || ''],
        target: AlarmUsersType.Member,
      } : undefined;
      setCurAlarm(newAlarm);
    }
  };

  const handleChangeAlarm = useCallback((item: { [key: string]: any }) => {
    setCurAlarm!({
      ...curAlarm!,
      ...item,
    });

  }, [curAlarm]);

  const changeAlarmTarget = useCallback((value: AlarmUsersType) => {
    if (value === AlarmUsersType.Field) {
      const memberField = Object.values(fieldMap).find(field => field.type === FieldType.Member);
      return handleChangeAlarm({ target: AlarmUsersType.Field, alarmUsers: memberField ? [memberField.id] : [] });
    }
    handleChangeAlarm({ target: AlarmUsersType.Member, alarmUsers: [user!.unitId] });
  }, [fieldMap, handleChangeAlarm, user]);

  const handleOk = () => {
    if (curAlarm?.alarmUsers.length === 0) {
      Message.warning({
        content: t(Strings.alarm_save_fail),
      });
      return;
    }
    if (!isEqual(alarm, curAlarm) && recordId) {
      let formatCurAlarm = curAlarm;

      if (curAlarm) {
        const subtractMatch = curAlarm?.subtract?.match(/^([0-9]+)(\w{1,2})$/)!;
        if (!curAlarm?.subtract || (subtractMatch[2] !== 'm' && subtractMatch[2] !== 'h')) {
          const noChange = curAlarm?.alarmAt && !curAlarm?.time;
          if (!noChange) {
            let alarmAt = timeZone ? dayjs(cellValue as string).tz(timeZone) : dayjs(cellValue as string);
            if (subtractMatch) {
              alarmAt = alarmAt.subtract(Number(subtractMatch[1]), (subtractMatch as any)[2]);
            }
            const time = curAlarm?.time || (timeZone ? dayjs(curAlarm?.alarmAt).tz(timeZone) : dayjs(curAlarm?.alarmAt)).format('HH:mm');
            alarmAt = dayjs.tz(`${alarmAt.format('YYYY-MM-DD')} ${time}`, timeZone);
            formatCurAlarm = {
              ...omit(formatCurAlarm, 'time'),
              alarmAt: alarmAt.valueOf() as any
            };
          }
        }
      }

      resourceService.instance!.commandManager!.execute({
        cmd: CollaCommandName.SetDateTimeCellAlarm,
        recordId,
        fieldId,
        alarm: convertAlarmStructure(formatCurAlarm as IRecordAlarmClient) || null,
      });
    }
    setOpenAlarm(false);
  };

  const pickTime = useMemo(() => {
    const time = curAlarm?.time || (timeZone ? dayjs(curAlarm?.alarmAt).tz(timeZone) : dayjs(curAlarm?.alarmAt)).format('HH:mm');
    if (time) {
      return time;
    }
    const nowTime = dayjs(new Date()).format('HH:mm');
    return includeTime ? nowTime : '09:00';
  }, [curAlarm?.alarmAt, curAlarm?.time, includeTime, timeZone]);

  const pickValue = useMemo(() => {
    const date = dayjs(new Date).format('YYYY/MM/DD');
    return new Date(`${date} ${pickTime}`);
  }, [pickTime]);

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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAlarmMemberField, user?.unitId, curAlarm?.alarmUsers, curAlarm?.target]);

  const unitInfo = useMemo(() => {
    if (!unitMap || !user?.unitId) {
      return;
    }
    // TODO Additional logic
    // if (alarmMember?.data && !isAlarmMemberField) {
    //   return unitMap[alarmMember?.data];
    // }
    return unitMap[user?.unitId];
  }, [unitMap, user?.unitId]);

  const alarmSelectedMember = useMemo(() => {
    return unitInfo && (
      <div className={style.selfSelect}>
        <MemberItem unitInfo={unitInfo} />
        <span className={style.selfSelectText}>{t(Strings.task_reminder_enable_member)}</span>
      </div>
    );
  }, [unitInfo]);

  const subtractOptions = useMemo(() => {
    let optionData: object;
    if (includeTime) { // All options can be selected for the date and time of opening
      optionData = ALL_ALARM_SUBTRACT;
    } else {
      // The date does not show the time, but the minute and hour levels were previously set
      let extraSubtract = {};
      if (curAlarm?.subtract && inDayKeys.includes(curAlarm?.subtract)) {
        extraSubtract = pick(ALL_ALARM_SUBTRACT, curAlarm?.subtract);
      }
      optionData = { ...CURRENT_ALARM_SUBTRACT, ...extraSubtract, ...ALARM_SUBTRACT };
    }
    return Object.keys(optionData).map(item => ({
      text: optionData[item],
      onClick: () => {
        const val = item === 'current' ? '' : item;
        const updateAlarm: any = { subtract: val };
        if (inDayKeys.includes(val as string) && curAlarm?.time) {
          updateAlarm.time = undefined;
        }
        handleChangeAlarm(updateAlarm);
      },
    }));
  }, [curAlarm?.subtract, curAlarm?.time, handleChangeAlarm, includeTime]);

  const contextMenuData = useMemo(() => {
    if (openSelect === 'subtract') {
      return [subtractOptions];
    }
    if (openSelect === 'target') {
      const fields = Object.values(fieldMap!);
      const isNotExistMemberField = fields.every(field => field.type !== FieldType.Member);
      return [
        [
          {
            value: AlarmUsersType.Member,
            text: t(Strings.alarm_specifical_member),
            onClick: () => {
              changeAlarmTarget(AlarmUsersType.Member);
            },
          },
          {
            value: AlarmUsersType.Field,
            text: t(Strings.alarm_specifical_field_member),
            disabled: isNotExistMemberField,
            disabledTip: t(Strings.alarm_no_member_field_tips),
            onClick: () => {
              changeAlarmTarget(AlarmUsersType.Field);
            },
          },
        ],
      ];
    }
    if (openSelect === 'alarmUsers') {
      return [
        [
          {
            label: alarmSelectedMember,
            value: user?.unitId || '',
          },
          ...memberFieldOptions,
        ].map(item => ({
          text: item.label,
          onClick: () => {
            const isField = item.value.startsWith(IDPrefix.Field);
            const val = [{
              type: isField ? AlarmUsersType.Field : AlarmUsersType.Member,
              data: item.value,
            }];
            handleChangeAlarm({
              alarmUsers: val,
            });
          },
        })),
      ];
    }
    return [];
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [alarmSelectedMember, handleChangeAlarm, memberFieldOptions, openSelect, subtractOptions, user?.unitId, changeAlarmTarget]);

  const changeAlarmField = (fieldIds: string[]) => {
    if (!fieldIds.length) {
      Message.warning({
        content: t(Strings.at_least_select_one_field),
      });
      return;
    }
    handleChangeAlarm({
      alarmUsers: fieldIds,
    });
  };

  const showTimePicker = Boolean(curAlarm?.time) || !includeTime ||
    // subtract Need to show specific time of day when selecting not in a day
    (!curAlarm?.subtract || !inDayKeys.includes(curAlarm?.subtract));

  const alarmConfigList = compact([
    {
      title: t(Strings.task_reminder_notify_date),
      value: ALL_ALARM_SUBTRACT[curAlarm?.subtract || 'current'],
      onClick: () => setOpenSelect('subtract'),
    }, showTimePicker && {
      title: t(Strings.task_reminder_notify_time),
      value: curAlarm?.time || pickTime,
    },
    {
      title: t(Strings.alarm_target_type),
      value: curAlarm?.target === AlarmUsersType.Member ? t(Strings.alarm_specifical_member) : t(Strings.alarm_specifical_field_member),
      onClick: () => setOpenSelect('target'),
    },
    {
      title: t(Strings.task_reminder_notify_who),
      value: curAlarm?.target === AlarmUsersType.Member ? <div onFocus={stopPropagation} className={style.cellMember}>
        <FilterGeneralSelect
          field={fakeMemberField as IMemberField}
          isMulti
          onChange={(unitIds) => {
            if (!Array.isArray(unitIds) || !unitIds.length) {
              Message.warning({
                content: t(Strings.at_least_select_one),
              });
              return;
            }
            handleChangeAlarm({ alarmUsers: unitIds });
          }}
          cellValue={curAlarm?.alarmUsers}
          listData={stashList}
        />
      </div> : <div className={style.fieldSelect}>
        <FieldSelect selectedFieldIds={curAlarm?.alarmUsers || []} fieldMap={fieldMap} onChange={changeAlarmField} />
      </div>,
    },
  ]);

  return (
    <div className={style.mobileAlarm}>
      <div className={style.mobileAlarmHeader}>
        <div className={style.mobileAlarmBack}>
          <IconButton
            component='button' icon={ChevronLeftOutlined}
            onClick={() => setOpenAlarm(false)}
          />
        </div>
        <Typography variant='h6'>
          {t(Strings.task_reminder_entry)}
        </Typography>
        <TextButton size='middle' color='primary' onClick={handleOk}>{t(Strings.save)}</TextButton>
      </div>
      <div className={style.mobileAlarmContent}>
        <div className={cls(style.mobileAlarmItem, style.wrapper)}>
          <Typography variant='body2'>
            {t(Strings.task_reminder_app_enable_switch)}
          </Typography>
          <Switch checked={Boolean(curAlarm)} onClick={handleOpen} />
        </div>
        {Boolean(curAlarm) && AlarmTipText && (
          <Typography variant='body4' className={style.mobileAlarmTitle}>
            <AlarmTipText datasheetId={datasheetId} dateTimeFieldId={fieldId} recordId={recordId} curAlarm={curAlarm} />
          </Typography>
        )}
        {Boolean(curAlarm) && (
          <List
            className={style.wrapper}
            data={alarmConfigList}
            renderItem={(item: any, index) => (
              <div className={cls(style.mobileAlarmItem, style.border)} key={index}>
                <Typography variant='body2'>
                  {item.title}
                </Typography>
                <div onClick={item.onClick} className={style.listRight}>
                  {showTimePicker && index === 1 ? (
                    <div>
                      <div className={style.dateItem} onClick={() => setPickerVisible(true)}>
                        <span className={style.listRightValue}>{item.value}</span>
                        <ChevronRightOutlined color={colors.fc3} size={16} />
                      </div>
                      <DatePicker
                        visible={pickerVisible}
                        precision='minute'
                        value={pickValue}
                        onConfirm={date => {
                          handleChangeAlarm({
                            time: dayjs(date).format('HH:mm'),
                          });
                        }}
                        onClose={() => setPickerVisible(false)}
                        className={style.timePicker}
                      />
                    </div>
                  ) : (
                    <>
                      <span className={style.listRightValue}>{item.value}</span>
                      <ChevronRightOutlined color={colors.fc3} size={16} />
                    </>
                  )}
                </div>
              </div>
            )}
          />
        )}
      </div>

      <MobileContextMenu
        title={t(Strings.operation)}
        visible={Boolean(openSelect)}
        onClose={() => { setOpenSelect(undefined); }}
        data={contextMenuData}
        height='auto'
      />

    </div>
  );
};
