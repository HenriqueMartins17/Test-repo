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

import { omit } from 'lodash';
import { IRecordAlarm, IRecordAlarmClient } from '@apitable/core';

export const convertAlarmStructure = (alarm?: IRecordAlarmClient | undefined): IRecordAlarm | undefined => {
  if (!alarm) {
    return;
  }
  const alarmUsers = alarm.alarmUsers.map(id => {
    return {
      type: alarm.target,
      data: id
    };
  });
  return {
    ...omit(alarm, 'target'),
    alarmUsers
  };
};
